package com.bengisusahin.filepicker.presentation.screen.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bengisusahin.filepicker.domain.model.FileItem
import com.bengisusahin.filepicker.domain.model.UploadStatus
import java.util.UUID
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    onBackClick: () -> Unit,
    onFilesSelected: (List<FileItem>) -> Unit
) {
    var selectedFiles by remember { mutableStateOf(listOf<FileItem>()) }
    var currentTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Picker") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Selected: ${selectedFiles.size} files")
                    Button(
                        onClick = {
                            if (selectedFiles.isNotEmpty()) {
                                onFilesSelected(selectedFiles)
                                onBackClick()
                            }
                        },
                        enabled = selectedFiles.isNotEmpty()
                    ) {
                        Text("Use Selected Files")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = currentTab) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = { Text("File.io") },
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = { Text("Google Drive") },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                )
            }
            
            when (currentTab) {
                0 -> {
                    // File.io Full Page WebView
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    // Enable file upload
                                    allowFileAccessFromFileURLs = true
                                    allowUniversalAccessFromFileURLs = true
                                    // Enable mixed content
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    // Enable database
                                    databaseEnabled = true
                                    // Enable geolocation
                                    setGeolocationEnabled(true)
                                    // Enable plugins
                                    setPluginState(WebSettings.PluginState.ON)
                                }
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                    }
                                    
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        url?.let { view?.loadUrl(it) }
                                        return true
                                    }
                                }
                                
                                webChromeClient = object : WebChromeClient() {
                                    override fun onShowFileChooser(
                                        webView: WebView?,
                                        filePathCallback: ValueCallback<Array<Uri>>?,
                                        fileChooserParams: FileChooserParams?
                                    ): Boolean {
                                        // Handle file chooser
                                        return true
                                    }
                                }
                                
                                loadUrl("https://file.io")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Google Drive Section
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🌐 Google Drive",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                Text(
                                    text = "Browse your Google Drive files:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                AndroidView(
                                    factory = { context ->
                                        WebView(context).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.allowFileAccess = true
                                            
                                            webViewClient = object : WebViewClient() {
                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                }
                                            }
                                            loadUrl("https://drive.google.com")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 