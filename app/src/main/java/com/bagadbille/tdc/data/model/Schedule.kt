package com.bagadbille.tdc.data.model

data class Schedule(
    val id: String,
    val teacherUid: String,
    val classId: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val room: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
