package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.AppNotification
import com.bagadbille.tdc.data.model.NotificationType
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<AppNotification>>
    suspend fun markAsRead(id: String): Result<Unit>
}

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    // TODO: Replace with NestJS API calls
    private val mock = mutableListOf(
        AppNotification("n_001", "Selection Quiz Live!", "TDC Selection Round - C++ quiz is now available. Attempt before Aug 25.", NotificationType.QUIZ_REMINDER, false, "2026-08-20T10:00:00Z"),
        AppNotification("n_002", "Python Batch Schedule Changed", "Python DSA class moved to Thursday 9-10 PM on Discord.", NotificationType.CLASS_UPDATE, false, "2026-08-19T16:00:00Z"),
        AppNotification("n_003", "New Android Dev Batch", "Registrations for Kotlin + Jetpack Compose batch are now open.", NotificationType.ANNOUNCEMENT, true, "2026-08-18T09:00:00Z"),
        AppNotification("n_004", "Assignment Due Reminder", "Your React To-Do App assignment is due in 3 days.", NotificationType.CLASS_UPDATE, true, "2026-08-17T14:00:00Z"),
        AppNotification("n_005", "DSA Challenge Results Out", "LeetCode Challenge results have been posted. Check your score!", NotificationType.QUIZ_REMINDER, true, "2026-08-16T11:30:00Z"),
    )

    override suspend fun getNotifications(): Result<List<AppNotification>> { delay(500); return Result.success(mock.toList()) }

    override suspend fun markAsRead(id: String): Result<Unit> {
        delay(200)
        val i = mock.indexOfFirst { it.id == id }
        if (i != -1) mock[i] = mock[i].copy(isRead = true)
        return Result.success(Unit)
    }
}
