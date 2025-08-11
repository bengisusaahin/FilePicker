package com.bengisusahin.filepicker.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bengisusahin.filepicker.R
import com.bengisusahin.filepicker.MainActivity

object NotificationHelper {
    
    // Notification channel IDs
    const val FILE_UPLOAD_CHANNEL_ID = "file_upload_channel"
    const val GENERAL_CHANNEL_ID = "general_channel"
    
    // Notification IDs
    const val FILE_UPLOAD_NOTIFICATION_ID = 1001
    const val GENERAL_NOTIFICATION_ID = 1002
    
    /**
     * Creates all necessary notification channels
     * Should be called once when the app starts
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // File Upload Channel
            val uploadChannel = NotificationChannel(
                FILE_UPLOAD_CHANNEL_ID,
                "File Upload Progress",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows progress of file uploads and completion status"
                enableVibration(false)
                setSound(null, null)
            }
            
            // General Notifications Channel
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications and messages"
                enableVibration(true)
            }
            
            // Create channels
            notificationManager.createNotificationChannels(
                listOf(uploadChannel, generalChannel)
            )
        }
    }
    
    /**
     * Shows a file upload progress notification
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showUploadProgressNotification(
        context: Context,
        fileName: String,
        progress: Int,
        isCompleted: Boolean = false
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        val notification = NotificationCompat.Builder(context, FILE_UPLOAD_CHANNEL_ID)
            .setContentTitle(
                if (isCompleted) "Upload Complete" else "Uploading Files"
            )
            .setContentText(
                if (isCompleted) "$fileName uploaded successfully" 
                else "Uploading $fileName..."
            )
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(100, progress, false)
            .setOngoing(!isCompleted)
            .setAutoCancel(isCompleted)
            .build()
        
        notificationManager.notify(FILE_UPLOAD_NOTIFICATION_ID, notification)
    }
    
    /**
     * Shows a general notification (for Firebase messages)
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showGeneralNotification(
        context: Context,
        title: String,
        message: String
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(GENERAL_NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancels upload progress notification
     */
    fun cancelUploadNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(FILE_UPLOAD_NOTIFICATION_ID)
    }

    /**
     * Shows a bulk upload completion notification
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showBulkUploadNotification(
        context: Context,
        totalFiles: Int,
        successfulFiles: Int,
        failedFiles: Int
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        val message = when {
            failedFiles == 0 -> "All $totalFiles files uploaded successfully!"
            successfulFiles == 0 -> "All $totalFiles files failed to upload"
            else -> "$successfulFiles/$totalFiles files uploaded successfully, $failedFiles failed"
        }
        
        val notification = NotificationCompat.Builder(context, FILE_UPLOAD_CHANNEL_ID)
            .setContentTitle("Bulk Upload Complete")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(FILE_UPLOAD_NOTIFICATION_ID + 1, notification)
    }
} 