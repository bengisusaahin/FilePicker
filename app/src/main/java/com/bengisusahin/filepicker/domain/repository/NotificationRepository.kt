package com.bengisusahin.filepicker.domain.repository

import com.bengisusahin.filepicker.domain.model.NotificationData

/**
 * Repository interface for notification operations
 */
interface NotificationRepository {
    
    /**
     * Shows a local notification
     * @param notification Notification data to display
     */
    suspend fun showNotification(notification: NotificationData)
    
    /**
     * Shows upload progress notification
     * @param fileName Name of the file being uploaded
     * @param progress Upload progress (0-100)
     * @param isCompleted Whether upload is completed
     */
    suspend fun showUploadProgressNotification(
        fileName: String,
        progress: Int,
        isCompleted: Boolean = false
    )
    
    /**
     * Cancels upload progress notification
     */
    suspend fun cancelUploadProgressNotification()
    
    /**
     * Gets Firebase messaging token
     * @return FCM token for push notifications
     */
    suspend fun getFCMToken(): Result<String>
    
    /**
     * Subscribes to a topic for receiving notifications
     * @param topic Topic name to subscribe to
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    
    /**
     * Unsubscribes from a topic
     * @param topic Topic name to unsubscribe from
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
} 