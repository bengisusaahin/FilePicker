package com.bengisusahin.filepicker.data.remote

import android.content.Context
import android.net.Uri
import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.model.UploadStatus
import com.bengisusahin.filepicker.utils.NotificationHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for uploading files to file.io
 * Provides free file hosting with direct download links
 */
@Singleton
class FileIoService @Inject constructor(
    private val context: Context,
    private val httpClient: OkHttpClient
) {
    
    companion object {
        private const val FILE_IO_UPLOAD_URL = "https://file.io"
        private const val MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024 // 2GB
    }
    
    /**
     * Uploads a file to file.io and returns the download URL
     */
    suspend fun uploadFile(fileItem: FileItem): Flow<FileItem> = callbackFlow {
        try {
            // Check file size
            if (fileItem.size > MAX_FILE_SIZE) {
                val errorFile = fileItem.copy(
                    uploadStatus = UploadStatus.FAILED,
                    errorMessage = "File size exceeds 2GB limit"
                )
                
                // Show error notification
                NotificationHelper.showGeneralNotification(
                    context = context,
                    title = "Upload Failed",
                    message = "File size exceeds 2GB limit"
                )
                
                trySend(errorFile)
                close()
                return@callbackFlow
            }
            
            // Start upload - 10%
            val uploadingFile = fileItem.copy(
                uploadProgress = 0.1f,
                uploadStatus = UploadStatus.UPLOADING
            )
            trySend(uploadingFile)
            
            // Show upload progress notification
            NotificationHelper.showUploadProgressNotification(
                context = context,
                fileName = fileItem.name,
                progress = 10,
                isCompleted = false
            )
            
            // Create temporary file from URI - 20%
            val tempFile = createTempFileFromUri(fileItem.uri, fileItem.name)
            trySend(uploadingFile.copy(uploadProgress = 0.2f))
            
            // Update notification progress
            NotificationHelper.showUploadProgressNotification(
                context = context,
                fileName = fileItem.name,
                progress = 20,
                isCompleted = false
            )
            
            // Prepare multipart request - 30%
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileItem.name,
                    tempFile.asRequestBody(fileItem.mimeType.toMediaType())
                )
                .build()
            
            val request = Request.Builder()
                .url(FILE_IO_UPLOAD_URL)
                .post(requestBody)
                .build()
            
            // Request prepared - 40%
            trySend(uploadingFile.copy(uploadProgress = 0.4f))
            
            // Update notification progress
            NotificationHelper.showUploadProgressNotification(
                context = context,
                fileName = fileItem.name,
                progress = 40,
                isCompleted = false
            )
            
            // Execute upload
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    val failedFile = fileItem.copy(
                        uploadStatus = UploadStatus.FAILED,
                        errorMessage = "Upload failed: ${e.message}"
                    )
                    
                    // Show error notification
                    NotificationHelper.showGeneralNotification(
                        context = context,
                        title = "Upload Failed",
                        message = "Failed to upload ${fileItem.name}: ${e.message}"
                    )
                    
                    trySend(failedFile)
                    close(e)
                    
                    // Cleanup temp file
                    tempFile.delete()
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            
                            // Parse response to get download URL
                            val downloadUrl = parseFileIoResponse(responseBody)
                            
                            if (downloadUrl != null) {
                                // Upload successful - 100%
                                val completedFile = fileItem.copy(
                                    uploadProgress = 1.0f,
                                    uploadStatus = UploadStatus.COMPLETED,
                                    uploadUrl = downloadUrl
                                )
                                
                                // Show completion notification
                                NotificationHelper.showUploadProgressNotification(
                                    context = context,
                                    fileName = fileItem.name,
                                    progress = 100,
                                    isCompleted = true
                                )
                                
                                trySend(completedFile)
                                close()
                            } else {
                                val failedFile = fileItem.copy(
                                    uploadStatus = UploadStatus.FAILED,
                                    errorMessage = "Failed to parse upload response"
                                )
                                
                                // Show error notification
                                NotificationHelper.showGeneralNotification(
                                    context = context,
                                    title = "Upload Failed",
                                    message = "Failed to parse upload response for ${fileItem.name}"
                                )
                                
                                trySend(failedFile)
                                close()
                            }
                        } else {
                            val failedFile = fileItem.copy(
                                uploadStatus = UploadStatus.FAILED,
                                errorMessage = "Upload failed with code: ${response.code}"
                            )
                            
                            // Show error notification
                            NotificationHelper.showGeneralNotification(
                                context = context,
                                title = "Upload Failed",
                                message = "Upload failed with code ${response.code} for ${fileItem.name}"
                            )
                            
                            trySend(failedFile)
                            close()
                        }
                    } finally {
                        // Cleanup temp file
                        tempFile.delete()
                        response.close()
                    }
                }
            })
            
        } catch (e: Exception) {
            val failedFile = fileItem.copy(
                uploadStatus = UploadStatus.FAILED,
                errorMessage = "Unexpected error: ${e.message}"
            )
            
            // Show error notification
            NotificationHelper.showGeneralNotification(
                context = context,
                title = "Upload Error",
                message = "Unexpected error uploading ${fileItem.name}: ${e.message}"
            )
            
            trySend(failedFile)
            close(e)
        }
        
        awaitClose {
            // Cleanup if needed
        }
    }
    
    /**
     * Creates a temporary file from URI
     */
    private fun createTempFileFromUri(uri: Uri, fileName: String): File {
        val tempFile = File.createTempFile("upload_", "_$fileName", context.cacheDir)
        
        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        return tempFile
    }
    
    /**
     * Parses file.io response to extract download URL
     * Response format: {"success":true,"key":"abc123","link":"https://file.io/abc123"}
     */
    private fun parseFileIoResponse(responseBody: String?): String? {
        return try {
            if (responseBody?.contains("\"success\":true") == true) {
                // Extract link from response
                val linkPattern = "\"link\":\"([^\"]+)\"".toRegex()
                val matchResult = linkPattern.find(responseBody)
                matchResult?.groupValues?.get(1)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 