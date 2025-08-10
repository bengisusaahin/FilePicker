package com.bengisusahin.filepicker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bengisusahin.filepicker.presentation.screen.filepicker.FilePickerScreen
import com.bengisusahin.filepicker.presentation.screen.uploadedfiles.UploadedFilesScreen
import com.bengisusahin.filepicker.presentation.screen.webview.WebViewScreen

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object FilePicker : Screen("file_picker")
    object UploadedFiles : Screen("uploaded_files")
    object WebView : Screen("web_view")
}

/**
 * Main navigation component for the app
 */
@Composable
fun FilePickerNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.FilePicker.route
    ) {
        composable(Screen.FilePicker.route) {
            FilePickerScreen(
                onNavigateToUploadedFiles = {
                    navController.navigate(Screen.UploadedFiles.route)
                },
                onNavigateToWebView = {
                    navController.navigate(Screen.WebView.route)
                }
            )
        }
        
        composable(Screen.UploadedFiles.route) {
            UploadedFilesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.WebView.route) {
            WebViewScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFilesSelected = { files ->
                    // Navigate back to file picker with selected files
                    navController.popBackStack()
                }
            )
        }
    }
} 