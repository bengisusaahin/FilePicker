package com.bengisusahin.filepicker.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.model.UploadStatus
import com.bengisusahin.filepicker.presentation.theme.*

/**
 * Card component for displaying file information
 */
@Composable
fun FileItemCard(
    fileItem: FileItem,
    onRemoveClick: () -> Unit,
    showRemoveButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = getFileIcon(fileItem),
                contentDescription = null,
                tint = getFileColor(fileItem),
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // File info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileItem.getFormattedSize(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Upload status
                    when (fileItem.uploadStatus) {
                        UploadStatus.PENDING -> {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Pending",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        UploadStatus.UPLOADING -> {
                            CircularProgressIndicator(
                                progress = { fileItem.uploadProgress },
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        UploadStatus.COMPLETED -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                modifier = Modifier.size(16.dp),
                                tint = FileUploadGreen
                            )
                        }
                        UploadStatus.FAILED -> {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Failed",
                                modifier = Modifier.size(16.dp),
                                tint = FileUploadRed
                            )
                        }
                        UploadStatus.CANCELLED -> {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Cancelled",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Progress bar for uploading files
                if (fileItem.uploadStatus == UploadStatus.UPLOADING) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { fileItem.uploadProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                
                // Error message
                fileItem.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = FileUploadRed,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Remove button
            if (showRemoveButton) {
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Returns appropriate icon for file type
 */
private fun getFileIcon(fileItem: FileItem): ImageVector {
    return when {
        fileItem.isImage() -> Icons.Default.Image
        fileItem.isVideo() -> Icons.Default.VideoFile
        fileItem.isDocument() -> Icons.Default.Description
        fileItem.mimeType.startsWith("audio/") -> Icons.Default.AudioFile
        else -> Icons.Default.InsertDriveFile
    }
}

/**
 * Returns appropriate color for file type
 */
private fun getFileColor(fileItem: FileItem) = when {
    fileItem.isImage() -> ImageFileColor
    fileItem.isVideo() -> VideoFileColor
    fileItem.isDocument() -> DocumentFileColor
    fileItem.mimeType.startsWith("audio/") -> AudioFileColor
    else -> DefaultFileColor
} 