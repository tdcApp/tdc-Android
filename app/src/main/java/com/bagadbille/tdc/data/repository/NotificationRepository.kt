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
    private val mock = mutableListOf(
        AppNotification("n_001", "New Quiz Available", "Physics - Wave Optics quiz is now available. Due by Aug 25.", NotificationType.QUIZ_REMINDER, false, "2026-08-20T10:00:00Z"),
        AppNotification("n_002", "Class Schedule Updated", "Mathematics class moved to Room 301 for this week.", NotificationType.CLASS_UPDATE, false, "2026-08-19T16:00:00Z"),
        AppNotification("n_003", "Mid-Term Schedule", "Mid-term examination schedule has been released.", NotificationType.ANNOUNCEMENT, true, "2026-08-18T09:00:00Z"),
        AppNotification("n_004", "Study Material Uploaded", "New Chemistry chapter notes available.", NotificationType.CLASS_UPDATE, true, "2026-08-17T14:00:00Z"),
        AppNotification("n_005", "Quiz Results Released", "Data Structures Basics quiz results are now available.", NotificationType.QUIZ_REMINDER, true, "2026-08-16T11:30:00Z"),
    )

    override suspend fun getNotifications(): Result<List<AppNotification>> { delay(500); return Result.success(mock.toList()) }

    override suspend fun markAsRead(id: String): Result<Unit> {
        delay(200)
        val i = mock.indexOfFirst { it.id == id }
        if (i != -1) mock[i] = mock[i].copy(isRead = true)
        return Result.success(Unit)
    }
}
