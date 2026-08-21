package com.bagadbille.tdc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_submissions")
data class PendingSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: String,
    val answersJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
