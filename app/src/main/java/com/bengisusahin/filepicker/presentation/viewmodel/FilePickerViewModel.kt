package com.bengisusahin.filepicker.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.model.UploadStatus
import com.bengisusahin.filepicker.domain.usecase.UploadFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for handling file picker operations
 */
@HiltViewModel
class FilePickerViewModel @Inject constructor(
    private val uploadFilesUseCase: UploadFilesUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "FilePickerViewModel"
    }
    
    private val _uiState = MutableStateFlow(FilePickerUiState())
    val uiState: StateFlow<FilePickerUiState> = _uiState.asStateFlow()
    
    init {
        // Removed loadUploadedFiles()
    }
    
    /**
     * Adds selected files to the upload queue
     */
    fun addFiles(uris: List<Uri>, fileInfos: List<Triple<String, String, Long>>) {
        val newFiles = uris.mapIndexed { index, uri ->
            val (name, mimeType, size) = fileInfos[index]
            FileItem(
                id = UUID.randomUUID().toString(),
                name = name,
                uri = uri,
                mimeType = mimeType,
                size = size,
                uploadStatus = UploadStatus.PENDING
            )
        }
        
        _uiState.value = _uiState.value.copy(
            selectedFiles = _uiState.value.selectedFiles + newFiles
        )
    }
    
    /**
     * Removes a file from the selected files list
     */
    fun removeFile(fileId: String) {
        _uiState.value = _uiState.value.copy(
            selectedFiles = _uiState.value.selectedFiles.filter { it.id != fileId }
        )
    }
    
    /**
     * Starts uploading all selected files
     */
    fun uploadFiles() {
        val filesToUpload = _uiState.value.selectedFiles.filter { 
            it.uploadStatus == UploadStatus.PENDING 
        }
        
        if (filesToUpload.isEmpty()) return
        
        Log.d(TAG, "Starting upload for ${filesToUpload.size} files")
        _uiState.value = _uiState.value.copy(isUploading = true, errorMessage = null)
        
        viewModelScope.launch {
            Log.d(TAG, "ViewModel: Calling uploadFilesUseCase")
            uploadFilesUseCase(filesToUpload)
                .catch { throwable ->
                    Log.e(TAG, "ViewModel: Upload error", throwable)
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        errorMessage = throwable.message ?: "Upload failed"
                    )
                }
                .collectLatest { updatedFiles -> 
                    Log.d(TAG, "ViewModel: Received ${updatedFiles.size} file updates")
                    updatedFiles.forEach { file ->
                        Log.d(TAG, "ViewModel: File ${file.name} - Progress: ${file.uploadProgress}, Status: ${file.uploadStatus}")
                    }
                    
                    // Group all progress updates by file NAME to get the complete progress history
                    // (Progress updates might have different IDs but same names)
                    val progressByFileName = updatedFiles.groupBy { it.name }
                    Log.d(TAG, "ViewModel: Progress updates grouped by file name: ${progressByFileName.keys}")
                    
                    // Update the selected files with progress - use the LATEST progress for each file
                    val updatedSelectedFiles = _uiState.value.selectedFiles.map { existingFile ->
                        val progressUpdates = progressByFileName[existingFile.name]
                        if (progressUpdates != null) {
                            Log.d(TAG, "ViewModel: Found ${progressUpdates.size} updates for file ${existingFile.name}")
                            progressUpdates.forEach { file ->
                                Log.d(TAG, "ViewModel: Progress update: ${file.uploadProgress} (${file.uploadProgress.javaClass.simpleName})")
                            }
                            
                            // Find the file with the HIGHEST progress for this file name
                            val latestProgress = progressUpdates.maxByOrNull { it.uploadProgress.toDouble() }
                            
                            if (latestProgress != null) {
                                Log.d(TAG, "ViewModel: Updating file ${existingFile.name} with LATEST progress ${latestProgress.uploadProgress} (status: ${latestProgress.uploadStatus})")
                                
                                // If progress is 100% but status is still UPLOADING, update it to COMPLETED
                                val finalProgress = if (latestProgress.uploadProgress >= 1.0f && latestProgress.uploadStatus == UploadStatus.UPLOADING) {
                                    Log.d(TAG, "ViewModel: Progress is 100%, updating status to COMPLETED for ${existingFile.name}")
                                    latestProgress.copy(uploadStatus = UploadStatus.COMPLETED)
                                } else {
                                    latestProgress
                                }
                                
                                finalProgress
                            } else {
                                Log.d(TAG, "ViewModel: No latest progress found for ${existingFile.name}")
                                existingFile
                            }
                        } else {
                            Log.d(TAG, "ViewModel: No progress updates found for file ${existingFile.name}")
                            existingFile
                        }
                    }
                    
                    Log.d(TAG, "ViewModel: Updating UI state with ${updatedSelectedFiles.size} files")
                    _uiState.value = _uiState.value.copy(
                        selectedFiles = updatedSelectedFiles,
                        isUploading = updatedFiles.any { it.uploadStatus == UploadStatus.UPLOADING }
                    )
                    
                    // If all uploads are complete, refresh the uploaded files list
                    if (updatedFiles.all { it.uploadStatus == UploadStatus.COMPLETED }) {
                        Log.d(TAG, "ViewModel: All uploads completed, refreshing uploaded files")
                        // Removed loadUploadedFiles()
                        clearSelectedFiles()
                    }
                }
        }
    }
    
    /**
     * Loads previously uploaded files
     */
    // Removed loadUploadedFiles()
    
    /**
     * Clears all selected files
     */
    fun clearSelectedFiles() {
        _uiState.value = _uiState.value.copy(selectedFiles = emptyList())
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Refreshes the uploaded files list
     */
    // Removed refreshUploadedFiles()
}

/**
 * UI State for File Picker screen
 */
data class FilePickerUiState(
    val selectedFiles: List<FileItem> = emptyList(),
    val isUploading: Boolean = false,
    val errorMessage: String? = null
) {
    val hasSelectedFiles: Boolean get() = selectedFiles.isNotEmpty()
    val canUpload: Boolean get() = hasSelectedFiles && !isUploading
    val uploadProgress: Float get() {
        if (selectedFiles.isEmpty()) return 0f
        val totalProgress = selectedFiles.sumOf { it.uploadProgress.toDouble() }
        return (totalProgress / selectedFiles.size).toFloat()
    }
} 