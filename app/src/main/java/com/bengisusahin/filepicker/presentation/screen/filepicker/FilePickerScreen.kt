package com.bengisusahin.filepicker.presentation.screen.filepicker

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bengisusahin.filepicker.presentation.component.FileItemCard
import com.bengisusahin.filepicker.presentation.component.UploadProgressCard
import com.bengisusahin.filepicker.presentation.viewmodel.FilePickerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Main file picker screen
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FilePickerScreen(
    onNavigateToUploadedFiles: () -> Unit,
    onNavigateToWebView: () -> Unit,
    viewModel: FilePickerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission state for file access
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
        )
    )

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val fileInfos = uris.map { uri ->
                getFileInfo(context, uri)
            }
            viewModel.addFiles(uris, fileInfos)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            filePickerLauncher.launch("*/*")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "File Picker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (permissionsState.allPermissionsGranted) {
                        filePickerLauncher.launch("*/*")
                    } else {
                        permissionLauncher.launch(permissionsState.permissions.map { it.permission }.toTypedArray())
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Files")
            }

            OutlinedButton(
                onClick = onNavigateToWebView,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Web, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Web Upload")
            }
        }

        OutlinedButton(
            onClick = onNavigateToUploadedFiles,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Uploaded Files")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upload progress
        if (uiState.isUploading) {
            UploadProgressCard(
                progress = uiState.uploadProgress,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Selected files section
        if (uiState.hasSelectedFiles) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Files (${uiState.selectedFiles.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    if (uiState.canUpload) {
                        Button(
                            onClick = { viewModel.uploadFiles() }
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.clearSelectedFiles() }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Files list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.selectedFiles) { file ->
                FileItemCard(
                    fileItem = file,
                    onRemoveClick = { viewModel.removeFile(file.id) },
                    showRemoveButton = !uiState.isUploading
                )
            }
        }
    }
}

/**
 * Helper function to get file information from URI
 */
private fun getFileInfo(context: Context, uri: Uri): Triple<String, String, Long> {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)

    var name = "Unknown"
    var size = 0L

    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

            if (nameIndex != -1) {
                name = it.getString(nameIndex) ?: "Unknown"
            }

            if (sizeIndex != -1) {
                size = it.getLong(sizeIndex)
            }
        }
    }

    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

    return Triple(name, mimeType, size)
} 