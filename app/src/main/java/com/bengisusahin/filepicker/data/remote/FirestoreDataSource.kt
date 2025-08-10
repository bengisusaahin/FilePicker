package com.bengisusahin.filepicker.data.remote

import android.net.Uri
import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.model.UploadStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore implementation for file operations
 * Stores file metadata and URLs from external file hosting services
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val filesCollection = firestore.collection("files")
    
    /**
     * Uploads a single file metadata to Firestore
     * Note: Actual file upload should be done to external service (file.io, imgur, etc.)
     */
    suspend fun uploadFile(fileItem: FileItem, externalFileUrl: String): Flow<FileItem> = callbackFlow {
        try {
            // Check if user is authenticated
            if (auth.currentUser == null) {
                val errorFile = fileItem.copy(
                    uploadStatus = UploadStatus.FAILED,
                    errorMessage = "User not authenticated"
                )
                trySend(errorFile)
                close()
                return@callbackFlow
            }
            
            val userId = auth.currentUser?.uid ?: "anonymous"
            val fileId = UUID.randomUUID().toString()
            
            // Create file document data
            val fileData = hashMapOf(
                "id" to fileId,
                "name" to fileItem.name,
                "mimeType" to fileItem.mimeType,
                "size" to fileItem.size,
                "externalUrl" to externalFileUrl,
                "uploadedBy" to userId,
                "uploadedAt" to FieldValue.serverTimestamp(),
                "status" to "completed",
                "thumbnailUrl" to fileItem.thumbnailUrl
            )
            
            // Update progress
            val uploadingFile = fileItem.copy(
                id = fileId,
                uploadProgress = 0.5f,
                uploadStatus = UploadStatus.UPLOADING
            )
            trySend(uploadingFile)
            
            // Save to Firestore
            filesCollection.document(fileId).set(fileData)
                .addOnSuccessListener {
                    val completedFile = fileItem.copy(
                        id = fileId,
                        uploadProgress = 1.0f,
                        uploadStatus = UploadStatus.COMPLETED,
                        uploadUrl = externalFileUrl
                    )
                    trySend(completedFile)
                    close()
                }
                .addOnFailureListener { exception ->
                    val failedFile = fileItem.copy(
                        id = fileId,
                        uploadStatus = UploadStatus.FAILED,
                        errorMessage = "Failed to save file metadata: ${exception.message}"
                    )
                    trySend(failedFile)
                    close(exception)
                }
            
        } catch (e: Exception) {
            val failedFile = fileItem.copy(
                uploadStatus = UploadStatus.FAILED,
                errorMessage = "Unexpected error: ${e.message}"
            )
            trySend(failedFile)
            close(e)
        }
        
        awaitClose {
            // Cleanup if needed
        }
    }
} 