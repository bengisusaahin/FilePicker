package com.bengisusahin.filepicker.data.notification

import android.util.Log
import com.bengisusahin.filepicker.domain.model.NotificationData
import com.bengisusahin.filepicker.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService as GoogleFirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

/**
 * Firebase Cloud Messaging service to handle push notifications
 */
@AndroidEntryPoint
class AppFirebaseMessagingService : GoogleFirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            handleNotificationMessage(notification)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to your server or save locally
        // You might want to send this token to your backend server
        sendTokenToServer(token)
    }
    
    /**
     * Handles data messages (background notifications)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "File Picker"
        val body = data["body"] ?: "You have a new message"
        val actionUrl = data["action_url"]
        val imageUrl = data["image_url"]
        
        val notificationData = NotificationData(
            title = title,
            body = body,
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            data = data
        )
        
        // Check permission before showing notification
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationHelper.showGeneralNotification(
                context = this,
                title = title,
                message = body
            )
        }
    }
    
    /**
     * Handles notification messages (foreground notifications)
     */
    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
        val title = notification.title ?: "File Picker"
        val body = notification.body ?: "You have a new message"
        
        // Check permission before showing notification
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationHelper.showGeneralNotification(
                context = this,
                title = title,
                message = body
            )
        }
    }
    
    /**
     * Send token to your backend server
     */
    private fun sendTokenToServer(token: String) {
        // TODO: Implement sending token to your backend server
        // This is where you would typically make an API call to your server
        // to register this device token for push notifications
        
        Log.d(TAG, "Token sent to server: $token")
        
        // Example:
        // apiService.registerDeviceToken(token)
    }
    
    /**
     * Subscribe to topic for receiving targeted notifications
     */
    private fun subscribeToTopics() {
        // Example: Subscribe to general file picker notifications
        // FirebaseMessaging.getInstance().subscribeToTopic("filepicker_general")
    }
} 