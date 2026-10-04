package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.Announcement
import kotlinx.coroutines.delay
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface AnnouncementRepository {
    suspend fun getAnnouncements(): Result<List<Announcement>>
    // ─── Mentor ───
    suspend fun createAnnouncement(
        title: String,
        content: String,
        audience: String,      // "all" | "language" | "technology"
        classId: String?
    ): Result<Announcement>
    suspend fun deleteAnnouncement(id: String): Result<Unit>
}

@Singleton
class AnnouncementRepositoryImpl @Inject constructor() : AnnouncementRepository {

    private val announcements = mutableListOf(
        Announcement("ann_001", "TDC Selection Quiz Round 2 - This Saturday!", "The second round of the TDC selection quiz for C++ and Python batches will be held this Saturday at 8 PM. Make sure to join the Google Meet link shared in your class.", "mentor_001", "cls_001", "all", "2026-08-20T09:00:00Z"),
        Announcement("ann_002", "New Batch: Android Dev (Kotlin) Starting Soon", "We're launching a new Android Development batch using Kotlin & Jetpack Compose. Interested members can appear for the screening quiz next week.", "mentor_004", null, "all", "2026-08-19T14:30:00Z"),
        Announcement("ann_003", "DSA Marathon - Register Now!", "A 3-day DSA marathon covering Arrays, Trees, and Graphs is happening next weekend. Open to all TDC members. Registration link in Discord.", "mentor_002", null, "all", "2026-08-18T11:00:00Z"),
        Announcement("ann_004", "Web Dev Batch Schedule Change", "The React Web Dev batch timings have been moved to Tuesday & Friday 7-8 PM instead of Mon/Wed. Updated schedule is live in the app.", "mentor_003", "cls_003", "technology", "2026-08-13T08:00:00Z"),
    )

    override suspend fun getAnnouncements(): Result<List<Announcement>> {
        delay(400)
        // TODO: Replace with GET /announcements from NestJS backend
        return Result.success(announcements.sortedByDescending { it.createdAt }.toList())
    }

    override suspend fun createAnnouncement(
        title: String,
        content: String,
        audience: String,
        classId: String?
    ): Result<Announcement> = runCatching {
        delay(500)
        // TODO: Replace with POST /announcements to NestJS backend
        val announcement = Announcement(
            id = "ann_${System.currentTimeMillis()}",
            title = title,
            content = content,
            createdBy = "mentor_001",
            classId = classId,
            audience = audience,
            createdAt = Instant.now().toString()
        )
        announcements.add(0, announcement)
        announcement
    }

    override suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        delay(300)
        // TODO: Replace with DELETE /announcements/{id} to NestJS backend
        if (!announcements.removeAll { it.id == id }) throw IllegalArgumentException("Announcement not found")
    }
}
