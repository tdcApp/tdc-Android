package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.dao.AnnouncementDao
import com.bagadbille.tdc.data.local.entity.AnnouncementEntity
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
        classId: String?,
        tag: String = "GENERAL",
        targetScreen: String? = null,
        targetId: String? = null
    ): Result<Announcement>
    suspend fun deleteAnnouncement(id: String): Result<Unit>
}

@Singleton
class AnnouncementRepositoryImpl @Inject constructor(
    private val announcementApi: AnnouncementApi,
    private val announcementDao: AnnouncementDao
) : AnnouncementRepository {

    override suspend fun getAnnouncements(): Result<List<Announcement>> = runCatching {
        try {
            val remote = announcementApi.getAnnouncements()
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt ?: "" }
            announcementDao.replaceAnnouncements(remote.map { AnnouncementEntity.fromDomain(it) })
            remote
        } catch (e: Exception) {
            val cached = announcementDao.getAllAnnouncements()
            if (cached.isNotEmpty()) {
                cached.map { it.toDomain() }
            } else {
                throw e
            }
        }
    }

    override suspend fun createAnnouncement(
        title: String,
        content: String,
        audience: String,
        classId: String?,
        tag: String,
        targetScreen: String?,
        targetId: String?
    ): Result<Announcement> = runCatching {
        val finalAudience = if (!classId.isNullOrBlank()) "CLASS" else audience.trim().uppercase()
        val finalClassId = if (finalAudience == "CLASS") classId?.trim() else null

        val req = CreateAnnouncementRequest(
            title = title.trim(),
            content = content.trim(),
            audience = finalAudience,
            classId = finalClassId,
            tag = tag.trim().uppercase(),
            targetScreen = targetScreen?.trim()?.uppercase(),
            targetId = targetId?.trim()
        )
        val created = announcementApi.createAnnouncement(req).toDomain()
        announcementDao.insertAnnouncement(AnnouncementEntity.fromDomain(created))
        created
    }

    override suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        announcementApi.deleteAnnouncement(id)
        announcementDao.deleteAnnouncement(id)
    }
}
