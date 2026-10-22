package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.AppNotification
import com.bagadbille.tdc.data.model.NotificationType
import com.bagadbille.tdc.data.remote.api.AnnouncementApi
import com.bagadbille.tdc.data.remote.api.AssignmentApi
import com.bagadbille.tdc.data.remote.api.NotificationApi
import com.bagadbille.tdc.data.remote.api.QuizApi
import com.bagadbille.tdc.data.remote.api.ScheduleApi
import com.bagadbille.tdc.data.remote.dto.RegisterFcmTokenRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<AppNotification>>
    suspend fun markAsRead(id: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun registerFcmToken(token: String): Result<Unit>
    suspend fun unregisterFcmToken(token: String): Result<Unit>
    fun hasUnread(): Flow<Boolean>
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val announcementApi: AnnouncementApi,
    private val scheduleApi: ScheduleApi,
    private val quizApi: QuizApi,
    private val assignmentApi: AssignmentApi,
    private val notificationApi: NotificationApi,
    private val dataStoreManager: DataStoreManager
) : NotificationRepository {

    private val _hasUnread = MutableStateFlow(false)
    private val cachedNotifIds = mutableSetOf<String>()
    private var lastFetchTime = 0L
    private var cachedNotifications = emptyList<AppNotification>()

    override fun hasUnread(): Flow<Boolean> = _hasUnread.asStateFlow()

    override suspend fun getNotifications(): Result<List<AppNotification>> = runCatching {
        if (System.currentTimeMillis() - lastFetchTime < 15_000L && cachedNotifications.isNotEmpty()) {
            return@runCatching cachedNotifications
        }
        val readIds = dataStoreManager.readNotifIds.first()
        val notifications = mutableListOf<AppNotification>()

        coroutineScope {
            val announcementsDef = async {
                runCatching { announcementApi.getAnnouncements() }.getOrDefault(emptyList())
            }
            val schedulesDef = async {
                runCatching { scheduleApi.getSchedules() }.getOrDefault(emptyList())
            }
            val quizzesDef = async {
                runCatching { quizApi.getQuizzes() }.getOrDefault(emptyList())
            }
            val assignmentsDef = async {
                runCatching { assignmentApi.getAssignments() }.getOrDefault(emptyList())
            }

            val announcements = announcementsDef.await()
            val schedules = schedulesDef.await()
            val quizzes = quizzesDef.await()
            val assignments = assignmentsDef.await()

            // 1. Convert real announcements into in-app notifications
            announcements.forEach { a ->
                val notifId = "ann_${a.id}"
                notifications.add(
                    AppNotification(
                        id = notifId,
                        title = a.title,
                        body = a.content,
                        type = NotificationType.ANNOUNCEMENT,
                        isRead = readIds.contains(notifId),
                        createdAt = a.createdAt ?: a.createdAtSnake ?: ""
                    )
                )
            }

            // 2. Convert real enrolled schedules into class update notifications
            schedules.forEach { s ->
                val notifId = "sch_${s.id}"
                val dayName = when (s.dayOfWeek) {
                    0 -> "Sunday"
                    1 -> "Monday"
                    2 -> "Tuesday"
                    3 -> "Wednesday"
                    4 -> "Thursday"
                    5 -> "Friday"
                    6 -> "Saturday"
                    else -> "Day ${s.dayOfWeek}"
                }
                notifications.add(
                    AppNotification(
                        id = notifId,
                        title = "Class Update: ${s.subject}",
                        body = "${s.subject} scheduled for $dayName (${s.startTime} - ${s.endTime})${if (!s.room.isNullOrBlank()) " in ${s.room}" else ""}",
                        type = NotificationType.CLASS_UPDATE,
                        isRead = readIds.contains(notifId),
                        createdAt = s.createdAt ?: s.createdAtSnake ?: ""
                    )
                )
            }

            // 3. Convert quizzes into in-app notifications
            quizzes.forEach { q ->
                val notifId = "quiz_${q.id}"
                notifications.add(
                    AppNotification(
                        id = notifId,
                        title = "New Quiz: ${q.title}",
                        body = "A new technical quiz is active in the community. Tap to take the quiz.",
                        type = NotificationType.QUIZ_REMINDER,
                        isRead = readIds.contains(notifId),
                        createdAt = q.createdAt ?: q.createdAtSnake ?: ""
                    )
                )
            }

            // 4. Convert assignments into in-app notifications
            assignments.forEach { a ->
                val notifId = "asgn_${a.id}"
                val due = a.dueAt ?: a.dueAtSnake
                val dueText = if (!due.isNullOrBlank()) " • Due: ${due.take(10)}" else ""
                notifications.add(
                    AppNotification(
                        id = notifId,
                        title = "New Assignment: ${a.title}",
                        body = "${if (!a.description.isNullOrBlank()) a.description else "New coding assignment posted."}$dueText",
                        type = NotificationType.GENERAL,
                        isRead = readIds.contains(notifId),
                        createdAt = a.createdAt ?: a.createdAtSnake ?: ""
                    )
                )
            }
        }

        cachedNotifIds.clear()
        cachedNotifIds.addAll(notifications.map { it.id })

        val hasAnyUnread = notifications.any { !it.isRead }
        _hasUnread.value = hasAnyUnread

        val sorted = notifications.sortedByDescending { it.createdAt }
        cachedNotifications = sorted
        lastFetchTime = System.currentTimeMillis()
        sorted
    }

    override suspend fun markAsRead(id: String): Result<Unit> = runCatching {
        dataStoreManager.markNotifRead(id)
        val readIds = dataStoreManager.readNotifIds.first() + id
        _hasUnread.value = cachedNotifIds.any { !readIds.contains(it) }
    }

    override suspend fun markAllAsRead(): Result<Unit> = runCatching {
        if (cachedNotifIds.isEmpty()) {
            getNotifications()
        }
        if (cachedNotifIds.isNotEmpty()) {
            dataStoreManager.markNotifsRead(cachedNotifIds)
        }
        _hasUnread.value = false
    }

    override suspend fun registerFcmToken(token: String): Result<Unit> = runCatching {
        notificationApi.registerFcmToken(RegisterFcmTokenRequest(token = token))
    }

    override suspend fun unregisterFcmToken(token: String): Result<Unit> = runCatching {
        notificationApi.unregisterFcmToken(token)
    }
}

