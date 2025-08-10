package com.bengisusahin.filepicker.data.repository

import com.bengisusahin.filepicker.data.remote.FirestoreDataSource
import com.bengisusahin.filepicker.data.remote.FileIoService
import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.model.UploadStatus
import com.bengisusahin.filepicker.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FileRepository using Firestore and file.io
 */
@Singleton
class FileRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val fileIoService: FileIoService
) : FileRepository {
    
    override suspend fun uploadFiles(files: List<FileItem>): Flow<List<FileItem>> {
        // Convert list to flow and upload each file concurrently
        return files.asFlow()
            .flatMapMerge(concurrency = 3) { file ->
                uploadFile(file)
            }
            .toList()
            .let { flowOf(it) }
    }
    
    override suspend fun uploadFile(file: FileItem): Flow<FileItem> {
        return fileIoService.uploadFile(file)
            .flatMapMerge { uploadedFile ->
                if (uploadedFile.uploadStatus == UploadStatus.COMPLETED) {
                    // Save metadata to Firestore
                    firestoreDataSource.uploadFile(uploadedFile, uploadedFile.uploadUrl ?: "")
                } else {
                    // Pass through progress updates and other statuses
                    flowOf(uploadedFile)
                }
            }
    }
} 