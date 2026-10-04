package com.bagadbille.tdc.data.model

data class UserProfile(
    val id: String,
    val email: String,
    val phone: String,
    val name: String? = null,
    val role: String = "student",
    val enrollmentNumber: String? = null,
    val year: String? = null,
    val isProfileComplete: Boolean = false,
    val languageClassId: String? = null,
    val technologyClassId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** Mentors and admins both get the mentor-side UI. */
fun isMentorRole(role: String?): Boolean = role == "mentor" || role == "admin"

val UserProfile.isMentor: Boolean
    get() = isMentorRole(role)
