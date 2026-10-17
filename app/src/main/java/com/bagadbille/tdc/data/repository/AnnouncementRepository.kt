package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.remote.api.AnnouncementApi
import com.bagadbille.tdc.data.remote.dto.CreateAnnouncementRequest
import javax.inject.Inject
import javax.inject.Singleton

interface AnnouncementRepository {
    suspend fun getAnnouncements(): Result<List<Announcement>>
    // ─── Mentor ───
    suspend fun createAnnouncement(
        title: String,
        content: String,
        audience: String,      // "all" | "language" | "technology" | "class"
        classId: String?
    ): Result<Announcement>
    suspend fun deleteAnnouncement(id: String): Result<Unit>
}

@Singleton
class AnnouncementRepositoryImpl @Inject constructor(
    private val announcementApi: AnnouncementApi
) : AnnouncementRepository {

    override suspend fun getAnnouncements(): Result<List<Announcement>> = runCatching {
        announcementApi.getAnnouncements()
            .map { it.toDomain() }
            .sortedByDescending { it.createdAt ?: "" }
    }

    override suspend fun createAnnouncement(
        title: String,
        content: String,
        audience: String,
        classId: String?
    ): Result<Announcement> = runCatching {
        val finalAudience = if (!classId.isNullOrBlank()) "CLASS" else audience.trim().uppercase()
        val finalClassId = if (finalAudience == "CLASS") classId?.trim() else null

        val req = CreateAnnouncementRequest(
            title = title.trim(),
            content = content.trim(),
            audience = finalAudience,
            classId = finalClassId
        )
        announcementApi.createAnnouncement(req).toDomain()
    }

    override suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        announcementApi.deleteAnnouncement(id)
    }
}
