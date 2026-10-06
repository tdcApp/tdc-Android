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

/** Mentors and admins both get the mentor-side UI. */
fun isMentorRole(role: String?): Boolean = role == "mentor" || role == "admin"

val UserProfile.isMentor: Boolean
    get() = isMentorRole(role)
