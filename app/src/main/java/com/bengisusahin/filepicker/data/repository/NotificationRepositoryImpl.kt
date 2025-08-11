package com.bengisusahin.filepicker.data.repository

import android.content.Context
import com.bengisusahin.filepicker.domain.model.NotificationData
import com.bengisusahin.filepicker.domain.repository.NotificationRepository
import com.bengisusahin.filepicker.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NotificationRepository using Firebase Messaging and NotificationHelper
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging
) : NotificationRepository {
    
    override suspend fun showNotification(notification: NotificationData) {
        NotificationHelper.showGeneralNotification(
            context = context,
            title = notification.title,
            message = notification.body
        )
    }
    
    override suspend fun showUploadProgressNotification(
        fileName: String,
        progress: Int,
        isCompleted: Boolean
    ) {
        NotificationHelper.showUploadProgressNotification(
            context = context,
            fileName = fileName,
            progress = progress,
            isCompleted = isCompleted
        )
    }
    
    override suspend fun cancelUploadProgressNotification() {
        NotificationHelper.cancelUploadNotification(context)
    }
    
    override suspend fun getFCMToken(): Result<String> {
        return try {
            val token = firebaseMessaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 