package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("class_id") val classId: String? = null,
    val audience: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ClassInfoDto(
    val id: String,
    val name: String,
    val type: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ScheduleDto(
    val id: String,
    @SerialName("teacher_uid") val teacherUid: String,
    @SerialName("class_id") val classId: String,
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val subject: String,
    val room: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AssignmentDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("mentor_uid") val mentorUid: String,
    @SerialName("class_id") val classId: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AssignmentSubmissionDto(
    @SerialName("assignment_id") val assignmentId: String,
    @SerialName("mentee_uid") val menteeUid: String,
    @SerialName("submission_url") val submissionUrl: String,
    @SerialName("submitted_at") val submittedAt: String
)

@Serializable
data class AssignmentSubmissionRequest(
    @SerialName("submission_url") val submissionUrl: String
)

@Serializable
data class NotificationDto(
    val id: String, val title: String, val body: String,
    val type: String, val isRead: Boolean, val createdAt: String
)
