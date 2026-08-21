package com.bagadbille.tdc.data.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val classInfo: String? = null,
    val section: String? = null,
    val createdAt: String? = null
)
