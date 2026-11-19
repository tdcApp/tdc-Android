package com.bagadbille.tdc.data.remote.dto

import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.model.ClassChangeRequest
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.HeroBanner
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
    val tag: String? = null,
    val targetScreen: String? = null,
    @SerialName("target_screen") val targetScreenSnake: String? = null,
    val targetId: String? = null,
    @SerialName("target_id") val targetIdSnake: String? = null,
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
        tag = (tag ?: "GENERAL").uppercase(),
        targetScreen = targetScreen ?: targetScreenSnake,
        targetId = targetId ?: targetIdSnake,
        createdAt = createdAt ?: createdAtSnake ?: "",
        updatedAt = updatedAt ?: updatedAtSnake
    )
}

@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val content: String,
    val audience: String,
    val classId: String? = null,
    val tag: String? = null,
    val targetScreen: String? = null,
    val targetId: String? = null
)

@Serializable
data class ClassInfoDto(
    val id: String,
    val name: String,
    val type: String,
    @SerialName("mentorUid") val mentorUid: String? = null,
    @SerialName("mentor_uid") val mentorUidSnake: String? = null,
    @SerialName("mentorName") val mentorName: String? = null,
    @SerialName("mentor_name") val mentorNameSnake: String? = null,
    @SerialName("mentorEmail") val mentorEmail: String? = null,
    @SerialName("mentor_email") val mentorEmailSnake: String? = null,
    @SerialName("mentorPhone") val mentorPhone: String? = null,
    @SerialName("mentor_phone") val mentorPhoneSnake: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null,
    @SerialName("studentCount") val studentCount: Int = 0,
    @SerialName("student_count") val studentCountSnake: Int = 0
) {
    fun toDomain(): ClassInfo = ClassInfo(
        id = id,
        name = name,
        type = type.lowercase(),
        mentorUid = mentorUid ?: mentorUidSnake,
        mentorName = mentorName ?: mentorNameSnake,
        mentorEmail = mentorEmail ?: mentorEmailSnake,
        mentorPhone = mentorPhone ?: mentorPhoneSnake,
        createdAt = createdAt ?: createdAtSnake ?: "",
        updatedAt = updatedAt ?: updatedAtSnake,
        studentCount = if (studentCount > 0) studentCount else studentCountSnake
    )
}

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
    @SerialName("updated_at") val updatedAtSnake: String? = null,
    val isSubmitted: Boolean = false,
    @SerialName("is_submitted") val isSubmittedSnake: Boolean? = null,
    val submissionUrl: String? = null,
    @SerialName("submission_url") val submissionUrlSnake: String? = null,
    val submittedAt: String? = null,
    @SerialName("submitted_at") val submittedAtSnake: String? = null,
    val submissionCount: Int = 0,
    @SerialName("submission_count") val submissionCountSnake: Int = 0
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
        creatorRole = creatorRole ?: creatorRoleSnake,
        isSubmitted = isSubmitted || (isSubmittedSnake == true),
        submissionUrl = submissionUrl ?: submissionUrlSnake,
        submittedAt = submittedAt ?: submittedAtSnake,
        submissionCount = if (submissionCount > 0) submissionCount else submissionCountSnake
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
data class UpdateAssignmentRequest(
    val title: String? = null,
    val description: String? = null,
    val classId: String? = null,
    val dueAt: String? = null
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

@Serializable
data class HeroBannerDto(
    val id: String? = null,
    val title: String = "TDC Hackathon & Events",
    val subtitle: String? = "Stay tuned for upcoming hackathons, workshops, and challenges",
    val tag: String = "UPCOMING",
    val badgeText: String? = null,
    @SerialName("badge_text") val badgeTextSnake: String? = null,
    val buttonText: String = "Learn More",
    @SerialName("button_text") val buttonTextSnake: String? = null,
    val actionUrl: String? = null,
    @SerialName("action_url") val actionUrlSnake: String? = null,
    val actionType: String = "URL",
    @SerialName("action_type") val actionTypeSnake: String? = null,
    val isActive: Boolean = false,
    @SerialName("is_active") val isActiveSnake: Boolean? = null
) {
    fun toDomain(): HeroBanner = HeroBanner(
        id = id,
        title = title.ifBlank { "TDC Hackathon & Events" },
        subtitle = subtitle,
        tag = tag.ifBlank { "UPCOMING" },
        badgeText = badgeText ?: badgeTextSnake,
        buttonText = buttonText.ifBlank { buttonTextSnake ?: "Learn More" },
        actionUrl = actionUrl ?: actionUrlSnake,
        actionType = actionType.ifBlank { actionTypeSnake ?: "URL" },
        isActive = isActiveSnake ?: isActive
    )
}

@Serializable
data class UpdateHeroBannerRequest(
    val title: String,
    val subtitle: String? = null,
    val tag: String = "UPCOMING",
    val badgeText: String? = null,
    val buttonText: String = "Learn More",
    val actionUrl: String? = null,
    val actionType: String = "URL",
    val isActive: Boolean = false
)

@Serializable
data class TeacherUserDto(
    @SerialName("firebaseUid") val firebaseUid: String? = null,
    val id: String? = null,
    val name: String? = null,
    val email: String = "",
    @SerialName("mobileNumber") val mobileNumber: String = "",
    @SerialName("mobile_number") val mobileNumberSnake: String = "",
    val role: String = "TEACHER"
) {
    val effectiveUid: String get() = firebaseUid ?: id ?: ""
    val effectiveMobile: String get() = mobileNumber.ifBlank { mobileNumberSnake }
}

@Serializable
data class CreateClassRequest(
    val name: String,
    val type: String,
    val mentorUid: String
)

@Serializable
data class UpdateClassRequest(
    val name: String? = null,
    val type: String? = null,
    val mentorUid: String? = null
)

@Serializable
data class CreateClassChangeRequest(
    val requestedLanguageClassId: String? = null,
    val requestedTechnologyClassId: String? = null,
    val reason: String? = null
)

@Serializable
data class ReviewClassChangeRequest(
    val status: String
)

@Serializable
data class ClassChangeRequestDto(
    val id: String,
    @SerialName("studentUid") val studentUid: String? = null,
    @SerialName("student_uid") val studentUidSnake: String? = null,
    @SerialName("studentName") val studentName: String? = null,
    @SerialName("student_name") val studentNameSnake: String? = null,
    @SerialName("studentEmail") val studentEmail: String? = null,
    @SerialName("student_email") val studentEmailSnake: String? = null,
    @SerialName("studentEnrollmentNumber") val studentEnrollmentNumber: String? = null,
    @SerialName("student_enrollment_number") val studentEnrollmentNumberSnake: String? = null,
    @SerialName("studentYear") val studentYear: String? = null,
    @SerialName("student_year") val studentYearSnake: String? = null,
    @SerialName("currentLanguageClassId") val currentLanguageClassId: String? = null,
    @SerialName("current_language_class_id") val currentLanguageClassIdSnake: String? = null,
    @SerialName("currentLanguageClassName") val currentLanguageClassName: String? = null,
    @SerialName("current_language_class_name") val currentLanguageClassNameSnake: String? = null,
    @SerialName("currentTechnologyClassId") val currentTechnologyClassId: String? = null,
    @SerialName("current_technology_class_id") val currentTechnologyClassIdSnake: String? = null,
    @SerialName("currentTechnologyClassName") val currentTechnologyClassName: String? = null,
    @SerialName("current_technology_class_name") val currentTechnologyClassNameSnake: String? = null,
    @SerialName("requestedLanguageClassId") val requestedLanguageClassId: String? = null,
    @SerialName("requested_language_class_id") val requestedLanguageClassIdSnake: String? = null,
    @SerialName("requestedLanguageClassName") val requestedLanguageClassName: String? = null,
    @SerialName("requested_language_class_name") val requestedLanguageClassNameSnake: String? = null,
    @SerialName("requestedTechnologyClassId") val requestedTechnologyClassId: String? = null,
    @SerialName("requested_technology_class_id") val requestedTechnologyClassIdSnake: String? = null,
    @SerialName("requestedTechnologyClassName") val requestedTechnologyClassName: String? = null,
    @SerialName("requested_technology_class_name") val requestedTechnologyClassNameSnake: String? = null,
    val reason: String? = null,
    val status: String = "PENDING",
    @SerialName("reviewedBy") val reviewedBy: String? = null,
    @SerialName("reviewed_by") val reviewedBySnake: String? = null,
    @SerialName("reviewerName") val reviewerName: String? = null,
    @SerialName("reviewer_name") val reviewerNameSnake: String? = null,
    @SerialName("reviewedAt") val reviewedAt: String? = null,
    @SerialName("reviewed_at") val reviewedAtSnake: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null
) {
    fun toDomain(): ClassChangeRequest = ClassChangeRequest(
        id = id,
        studentUid = studentUid ?: studentUidSnake ?: "",
        studentName = studentName ?: studentNameSnake,
        studentEmail = studentEmail ?: studentEmailSnake,
        studentEnrollmentNumber = studentEnrollmentNumber ?: studentEnrollmentNumberSnake,
        studentYear = studentYear ?: studentYearSnake,
        currentLanguageClassId = currentLanguageClassId ?: currentLanguageClassIdSnake,
        currentLanguageClassName = currentLanguageClassName ?: currentLanguageClassNameSnake,
        currentTechnologyClassId = currentTechnologyClassId ?: currentTechnologyClassIdSnake,
        currentTechnologyClassName = currentTechnologyClassName ?: currentTechnologyClassNameSnake,
        requestedLanguageClassId = requestedLanguageClassId ?: requestedLanguageClassIdSnake,
        requestedLanguageClassName = requestedLanguageClassName ?: requestedLanguageClassNameSnake,
        requestedTechnologyClassId = requestedTechnologyClassId ?: requestedTechnologyClassIdSnake,
        requestedTechnologyClassName = requestedTechnologyClassName ?: requestedTechnologyClassNameSnake,
        reason = reason,
        status = status.uppercase(),
        reviewedBy = reviewedBy ?: reviewedBySnake,
        reviewerName = reviewerName ?: reviewerNameSnake,
        reviewedAt = reviewedAt ?: reviewedAtSnake,
        createdAt = createdAt ?: createdAtSnake ?: "",
        updatedAt = updatedAt ?: updatedAtSnake ?: ""
    )
}



