package com.bagadbille.tdc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val questionText: String,
    val optionsJson: String,
    val isMultiAnswer: Boolean,
    val order: Int
)
