package com.bagadbille.tdc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "quiz_id") val quizId: String,
    val question: String,
    @ColumnInfo(name = "option_a") val optionA: String,
    @ColumnInfo(name = "option_b") val optionB: String,
    @ColumnInfo(name = "option_c") val optionC: String,
    @ColumnInfo(name = "option_d") val optionD: String,
    @ColumnInfo(name = "correct_option") val correctOption: String,
    @ColumnInfo(name = "question_order") val questionOrder: Int
)
