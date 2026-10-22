package com.bagadbille.tdc.data.model

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val createdBy: String,
    val classId: String? = null,
    val audience: String,
    val tag: String = "GENERAL",
    val targetScreen: String? = null,
    val targetId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

