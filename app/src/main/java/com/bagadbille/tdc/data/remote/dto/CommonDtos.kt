package com.bagadbille.tdc.data.remote.dto

import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.model.Schedule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val content: String,
    val createdBy: String? = null,
    @SerialName("created_by") val createdBySnake: String? = null,
    val classId: String? = null,
    @SerialName("class_id") val classIdSnake: String? = null,
    val audience: String = "ALL",
    val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null
) {
    fun toDomain(): Announcement = Announcement(
        id = id,
        title = title,
        content = content,
        createdBy = createdBy ?: createdBySnake ?: "",
        classId = classId ?: classIdSnake,
        audience = audience.lowercase(),
        createdAt = createdAt ?: createdAtSnake ?: "",
        updatedAt = updatedAt ?: updatedAtSnake
    )
}

@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val content: String,
    val audience: String,
    val classId: String? = null
)

@Serializable
data class ClassInfoDto(
    val id: String,
    val name: String,
    val type: String,
    @SerialName("mentorUid") val mentorUid: String? = null,
    @SerialName("mentor_uid") val mentorUidSnake: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null,
    @SerialName("studentCount") val studentCount: Int = 0,
    @SerialName("student_count") val studentCountSnake: Int = 0
)

@Serializable
data class ScheduleDto(
    val id: String,
    val teacherUid: String? = null,
    @SerialName("teacher_uid") val teacherUidSnake: String? = null,
    val classId: String? = null,
    @SerialName("class_id") val classIdSnake: String? = null,
    val dayOfWeek: Int = 1,
    @SerialName("day_of_week") val dayOfWeekSnake: Int? = null,
    val startTime: String? = null,
    @SerialName("start_time") val startTimeSnake: String? = null,
    val endTime: String? = null,
    @SerialName("end_time") val endTimeSnake: String? = null,
    val subject: String = "",
    val room: String? = null,
    val className: String? = null,
    val classType: String? = null,
    val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null
) {
    fun toDomain(): Schedule = Schedule(
        id = id,
        teacherUid = teacherUid ?: teacherUidSnake ?: "",
        classId = classId ?: classIdSnake ?: "",
        dayOfWeek = dayOfWeekSnake ?: dayOfWeek,
        startTime = startTime ?: startTimeSnake ?: "",
        endTime = endTime ?: endTimeSnake ?: "",
        subject = subject,
        room = room,
        createdAt = createdAt ?: createdAtSnake ?: "",
        updatedAt = updatedAt ?: updatedAtSnake
    )
}

@Serializable
data class CreateScheduleRequest(
    val classId: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val room: String? = null
)

@Serializable
data class UpdateScheduleRequest(
    val classId: String? = null,
    val dayOfWeek: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val subject: String? = null,
    val room: String? = null
)

@Serializable
data class AssignmentDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val classId: String? = null,
    @SerialName("class_id") val classIdSnake: String? = null,
    val dueAt: String? = null,
    @SerialName("due_at") val dueAtSnake: String? = null,
    val mentorUid: String? = null,
    @SerialName("mentor_uid") val mentorUidSnake: String? = null,
    val createdBy: String? = null,
    @SerialName("created_by") val createdBySnake: String? = null,
    val createdByName: String? = null,
    @SerialName("created_by_name") val createdByNameSnake: String? = null,
    val creatorRole: String? = null,
    @SerialName("creator_role") val creatorRoleSnake: String? = null,
    val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null
) {
    fun toDomain(): Assignment = Assignment(
        id = id,
        title = title,
        description = description ?: "",
        classId = classId ?: classIdSnake ?: "",
        dueAt = dueAt ?: dueAtSnake ?: "",
        mentorUid = mentorUid ?: mentorUidSnake ?: "",
        createdAt = createdAt ?: createdAtSnake ?: "",
        updatedAt = updatedAt ?: updatedAtSnake,
        createdByName = createdByName ?: createdByNameSnake,
        creatorRole = creatorRole ?: creatorRoleSnake
    )
}

@Serializable
data class CreateAssignmentRequest(
    val title: String,
    val description: String? = null,
    val classId: String,
    val dueAt: String
)

@Serializable
data class AssignmentSubmissionDto(
    val assignmentId: String? = null,
    @SerialName("assignment_id") val assignmentIdSnake: String? = null,
    val menteeUid: String? = null,
    @SerialName("mentee_uid") val menteeUidSnake: String? = null,
    val submissionUrl: String? = null,
    @SerialName("submission_url") val submissionUrlSnake: String? = null,
    val submittedAt: String? = null,
    @SerialName("submitted_at") val submittedAtSnake: String? = null,
    val studentName: String? = null,
    @SerialName("student_name") val studentNameSnake: String? = null,
    val studentEmail: String? = null,
    @SerialName("student_email") val studentEmailSnake: String? = null,
    val studentEnrollment: String? = null,
    @SerialName("student_enrollment") val studentEnrollmentSnake: String? = null
) {
    fun toDomain(): AssignmentSubmission = AssignmentSubmission(
        assignmentId = assignmentId ?: assignmentIdSnake ?: "",
        menteeUid = menteeUid ?: menteeUidSnake ?: "",
        submissionUrl = submissionUrl ?: submissionUrlSnake ?: "",
        submittedAt = submittedAt ?: submittedAtSnake ?: "",
        studentName = studentName ?: studentNameSnake,
        studentEmail = studentEmail ?: studentEmailSnake,
        studentEnrollment = studentEnrollment ?: studentEnrollmentSnake
    )
}

@Serializable
data class AssignmentSubmissionRequest(
    val submissionUrl: String
)

@Serializable
data class NotificationDto(
    val id: String, val title: String, val body: String,
    val type: String, val isRead: Boolean, val createdAt: String
)

@Serializable
data class RegisterFcmTokenRequest(
    val token: String,
    val platform: String = "ANDROID"
)

