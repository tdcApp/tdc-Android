package com.bagadbille.tdc.data.model

data class ClassInfo(
    val id: String,
    val name: String,
    val type: String,
    val mentorUid: String? = null,
    val mentorName: String? = null,
    val mentorEmail: String? = null,
    val mentorPhone: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val studentCount: Int = 0
)
