package com.bagadbille.tdc.data.model

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: String
)

enum class NotificationType { ANNOUNCEMENT, QUIZ_REMINDER, CLASS_UPDATE, GENERAL }
