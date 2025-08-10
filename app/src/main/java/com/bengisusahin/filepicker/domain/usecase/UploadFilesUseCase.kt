package com.bengisusahin.filepicker.domain.usecase

import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.repository.FileRepository
import com.bengisusahin.filepicker.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Use case for uploading files
 * Handles the business logic of file uploads including notifications
 */
class UploadFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val notificationRepository: NotificationRepository
) {

    /**
     * Uploads multiple files and shows progress notifications
     * @param files List of files to upload
     * @return Flow of updated file items with progress
     */
    suspend operator fun invoke(files: List<FileItem>): Flow<List<FileItem>> {
        return fileRepository.uploadFiles(files)
            .onEach { updatedFiles ->
                // Show progress notification for first file being uploaded
                val uploadingFile = updatedFiles.firstOrNull { it.uploadProgress > 0 && it.uploadProgress < 1 }
                uploadingFile?.let { file ->
                    notificationRepository.showUploadProgressNotification(
                        fileName = file.name,
                        progress = (file.uploadProgress * 100).toInt(),
                        isCompleted = file.uploadProgress >= 1.0f
                    )
                }

                // Cancel notification when all uploads are complete
                val allCompleted = updatedFiles.all { it.uploadProgress >= 1.0f }
                if (allCompleted) {
                    notificationRepository.cancelUploadProgressNotification()
                }
            }
    }
}