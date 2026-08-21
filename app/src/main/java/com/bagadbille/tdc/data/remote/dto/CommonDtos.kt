package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    val id: String, val title: String, val body: String,
    val authorName: String, val authorAvatarUrl: String? = null,
    val createdAt: String, val category: String? = null
)

@Serializable
data class ClassInfoDto(
    val id: String, val name: String, val subject: String,
    val teacher: String, val schedule: String? = null,
    val room: String? = null, val description: String? = null
)

@Serializable
data class ClassDetailDto(val classInfo: ClassInfoDto, val materials: List<ClassMaterialDto>)

@Serializable
data class ClassMaterialDto(
    val id: String, val title: String, val type: String,
    val url: String, val uploadedAt: String
)

@Serializable
data class NotificationDto(
    val id: String, val title: String, val body: String,
    val type: String, val isRead: Boolean, val createdAt: String
)
