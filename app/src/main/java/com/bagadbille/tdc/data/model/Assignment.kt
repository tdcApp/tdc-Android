package com.bagadbille.tdc.data.model

data class Assignment(
    val id: String,
    val title: String,
    val description: String? = null,
    val dueAt: String? = null,
    val mentorUid: String,
    val classId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdByName: String? = null,
    val creatorRole: String? = null,
    val isSubmitted: Boolean = false,
    val submissionUrl: String? = null,
    val submittedAt: String? = null,
    val submissionCount: Int = 0
)
