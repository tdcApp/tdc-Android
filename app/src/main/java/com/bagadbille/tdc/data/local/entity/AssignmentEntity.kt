package com.bagadbille.tdc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bagadbille.tdc.data.model.Assignment

@Entity(tableName = "cached_assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    val dueAt: String? = null,
    val mentorUid: String,
    val classId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Assignment = Assignment(
        id = id,
        title = title,
        description = description,
        dueAt = dueAt,
        mentorUid = mentorUid,
        classId = classId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(assignment: Assignment): AssignmentEntity = AssignmentEntity(
            id = assignment.id,
            title = assignment.title,
            description = assignment.description,
            dueAt = assignment.dueAt,
            mentorUid = assignment.mentorUid,
            classId = assignment.classId,
            createdAt = assignment.createdAt,
            updatedAt = assignment.updatedAt
        )
    }
}
