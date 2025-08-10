package com.bengisusahin.filepicker.di

import com.bengisusahin.filepicker.data.repository.FileRepositoryImpl
import com.bengisusahin.filepicker.data.repository.NotificationRepositoryImpl
import com.bengisusahin.filepicker.domain.repository.FileRepository
import com.bengisusahin.filepicker.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to their implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindFileRepository(
        fileRepositoryImpl: FileRepositoryImpl
    ): FileRepository
    
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
} 