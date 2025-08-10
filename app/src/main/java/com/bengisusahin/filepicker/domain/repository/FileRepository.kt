package com.bengisusahin.filepicker.domain.repository

import com.bengisusahin.filepicker.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for file operations
 * This defines the contract for file handling operations
 */
interface FileRepository {
    
    /**
     * Uploads multiple files to storage
     * @param files List of files to upload
     * @return Flow of updated file items with progress
     */
    suspend fun uploadFiles(files: List<FileItem>): Flow<List<FileItem>>
    
    /**
     * Uploads a single file to storage
     * @param file File to upload
     * @return Flow of updated file item with progress
     */
    suspend fun uploadFile(file: FileItem): Flow<FileItem>
} 