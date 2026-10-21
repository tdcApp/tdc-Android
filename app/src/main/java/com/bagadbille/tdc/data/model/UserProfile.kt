package com.bagadbille.tdc.data.model

data class UserProfile(
    val id: String, // Maps to Neon firebase_uid varchar(128) PK
    val email: String,
    val mobileNumber: String = "",
    val name: String? = null,
    val role: String = "student", // "student", "mentor", "admin"
    val enrollmentNumber: String? = null,
    val year: String? = null,
    val isProfileComplete: Boolean = false,
    val languageClassId: String? = null,
    val technologyClassId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val firebaseUid: String get() = id
    /** Backward-compat alias */
    val phone: String get() = mobileNumber
}

/** Mentors and admins (or teachers in backend) both get the mentor-side UI. */
fun isMentorRole(role: String?): Boolean =
    role != null && (
        role.equals("mentor", ignoreCase = true) ||
        role.equals("teacher", ignoreCase = true) ||
        role.equals("admin", ignoreCase = true)
    )

val UserProfile.isMentor: Boolean
    get() = isMentorRole(role)

fun com.bagadbille.tdc.data.remote.dto.UserProfileDto.toDomain(
    fallbackUid: String = "",
    fallbackEmail: String = "",
    fallbackComplete: Boolean = true
): UserProfile {
    val cleanRole = role.lowercase()
    val isMentor = isMentorRole(cleanRole)
    val complete = if (isMentor) true else if (isProfileComplete) true else (!name.isNullOrBlank() && fallbackComplete)
    return UserProfile(
        id = effectiveUid.ifBlank { fallbackUid },
        email = email.ifBlank { fallbackEmail },
        mobileNumber = effectiveMobile,
        name = name,
        role = cleanRole,
        enrollmentNumber = effectiveEnrollmentNumber,
        year = year,
        isProfileComplete = complete,
        languageClassId = effectiveLanguageClassId,
        technologyClassId = effectiveTechnologyClassId,
        createdAt = createdAt ?: createdAtSnake,
        updatedAt = updatedAt ?: updatedAtSnake
    )
}
