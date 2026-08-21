package com.bagadbille.tdc.data.model

data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val createdAt: String,
    val category: String? = null
)
