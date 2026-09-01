package com.bagadbille.tdc.data.model

data class Assignment(
    val id: String,
    val title: String,
    val subject: String,
    val description: String,
    val dueDate: String,
    val status: AssignmentStatus,
    val attachments: List<AssignmentAttachment> = emptyList()
)

enum class AssignmentStatus { ONGOING, PAST }

data class AssignmentAttachment(
    val id: String,
    val fileName: String,
    val fileUrl: String
)
