package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseSignInRequest(
    val email: String,
    val password: String,
    @SerialName("returnSecureToken") val returnSecureToken: Boolean = true
)

@Serializable
data class FirebaseSignInResponse(
    val idToken: String? = null,
    val email: String? = null,
    val refreshToken: String? = null,
    val expiresIn: String? = null,
    val localId: String? = null,
    val registered: Boolean? = null,
    val displayName: String? = null
)

@Serializable
data class FirebaseRefreshTokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: String? = null,
    @SerialName("user_id") val userId: String? = null
)

@Serializable
data class AuthBootstrapResponse(
    @SerialName("profileExists") val profileExists: Boolean,
    @SerialName("profile") val profile: UserProfileDto? = null
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: UserProfileDto)

@Serializable
data class UserProfileDto(
    @SerialName("firebaseUid") val firebaseUid: String? = null,
    val id: String? = null,
    val email: String = "",
    val phone: String = "",
    @SerialName("mobileNumber") val mobileNumber: String = "",
    @SerialName("mobile_number") val mobileNumberSnake: String = "",
    val name: String? = null,
    val role: String = "STUDENT",
    val enrollmentNumber: String? = null,
    @SerialName("enrollment_number") val enrollmentNumberSnake: String? = null,
    val year: String? = null,
    @SerialName("is_profile_complete") val isProfileComplete: Boolean = false,
    @SerialName("languageClassId") val languageClassId: String? = null,
    @SerialName("language_class_id") val languageClassIdSnake: String? = null,
    @SerialName("technologyClassId") val technologyClassId: String? = null,
    @SerialName("technology_class_id") val technologyClassIdSnake: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null
) {
    val effectiveUid: String
        get() = firebaseUid ?: id ?: ""

    val effectiveMobile: String
        get() = mobileNumber.ifBlank { mobileNumberSnake.ifBlank { phone } }

    val effectiveEnrollmentNumber: String?
        get() = enrollmentNumber ?: enrollmentNumberSnake

    val effectiveLanguageClassId: String?
        get() = languageClassId ?: languageClassIdSnake

    val effectiveTechnologyClassId: String?
        get() = technologyClassId ?: technologyClassIdSnake
}

@Serializable
data class CreateProfileRequest(
    val name: String,
    val mobileNumber: String,
    val languageClassId: String,
    val technologyClassId: String,
    val year: String? = null,
    val enrollmentNumber: String? = null
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
    val mobileNumber: String? = null,
    val year: String? = null,
    @SerialName("enrollment_number") val enrollmentNumber: String? = null
)
