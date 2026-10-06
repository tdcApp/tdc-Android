package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: UserProfileDto)

@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    val phone: String = "",
    @SerialName("mobile_number") val mobileNumber: String = "",
    val name: String? = null,
    val role: String = "student",
    @SerialName("enrollment_number") val enrollmentNumber: String? = null,
    val year: String? = null,
    @SerialName("is_profile_complete") val isProfileComplete: Boolean = false,
    @SerialName("language_class_id") val languageClassId: String? = null,
    @SerialName("technology_class_id") val technologyClassId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SetupProfileRequest(
    val name: String,
    @SerialName("enrollment_number") val enrollmentNumber: String,
    @SerialName("mobile_number") val mobileNumber: String? = null,
    val year: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    @SerialName("mobile_number") val mobileNumber: String? = null
)
