package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthVerifyRequest(val idToken: String)

@Serializable
data class AuthVerifyResponse(val token: String, val user: UserProfileDto)

@Serializable
data class UserProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val classInfo: String? = null,
    val section: String? = null,
    val createdAt: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val classInfo: String? = null,
    val section: String? = null,
    val avatarUrl: String? = null
)
