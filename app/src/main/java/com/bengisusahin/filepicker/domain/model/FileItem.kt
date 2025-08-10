package com.bengisusahin.filepicker.domain.model

import android.net.Uri

/**
 * Domain model representing a file item
 * This is used across all layers of the application
 */
data class FileItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val mimeType: String,
    val size: Long,
    val uploadProgress: Float = 0f,
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    val uploadUrl: String? = null,
    val errorMessage: String? = null,
    val thumbnailUrl: String? = null
) {
    /**
     * Returns human-readable file size
     */
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * Returns file extension from name
     */
    fun getExtension(): String {
        return name.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * Checks if file is an image
     */
    fun isImage(): Boolean {
        return mimeType.startsWith("image/") || 
                getExtension() in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    }
    
    /**
     * Checks if file is a video
     */
    fun isVideo(): Boolean {
        return mimeType.startsWith("video/") || 
                getExtension() in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv")
    }
    
    /**
     * Checks if file is a document
     */
    fun isDocument(): Boolean {
        return mimeType.startsWith("application/") || 
                getExtension() in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
    }
}

/**
 * Enum representing the upload status of a file
 */
enum class UploadStatus {
    PENDING,
    UPLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
} 