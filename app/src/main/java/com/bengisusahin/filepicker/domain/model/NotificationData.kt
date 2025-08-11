package com.bengisusahin.filepicker.domain.model

/**
 * Domain model representing notification data from Firebase
 */
data class NotificationData(
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val data: Map<String, String> = emptyMap()
) 