package com.bagadbille.tdc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bagadbille.tdc.data.model.Announcement

@Entity(tableName = "cached_announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdBy: String,
    val classId: String? = null,
    val audience: String,
    val tag: String = "GENERAL",
    val targetScreen: String? = null,
    val targetId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Announcement = Announcement(
        id = id,
        title = title,
        content = content,
        createdBy = createdBy,
        classId = classId,
        audience = audience,
        tag = tag,
        targetScreen = targetScreen,
        targetId = targetId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(announcement: Announcement): AnnouncementEntity = AnnouncementEntity(
            id = announcement.id,
            title = announcement.title,
            content = announcement.content,
            createdBy = announcement.createdBy,
            classId = announcement.classId,
            audience = announcement.audience,
            tag = announcement.tag,
            targetScreen = announcement.targetScreen,
            targetId = announcement.targetId,
            createdAt = announcement.createdAt,
            updatedAt = announcement.updatedAt
        )
    }
}

