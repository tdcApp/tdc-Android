package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val audience: String,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("class_id") val classId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class QuizQuestionDto(
    val id: String,
    val question: String,
    @SerialName("option_a") val optionA: String,
    @SerialName("option_b") val optionB: String,
    @SerialName("option_c") val optionC: String,
    @SerialName("option_d") val optionD: String,
    @SerialName("correct_option") val correctOption: String,
    @SerialName("question_order") val questionOrder: Int,
    @SerialName("quiz_id") val quizId: String
)

@Serializable
data class QuizSubmitRequest(
    val answers: List<QuizAnswerDto>,
    @SerialName("client_submission_id") val clientSubmissionId: String
)

@Serializable
data class QuizAnswerDto(
    @SerialName("question_id") val questionId: String,
    @SerialName("selected_option") val selectedOption: String
)

@Serializable
data class QuizSubmissionDto(
    @SerialName("quiz_id") val quizId: String,
    @SerialName("student_uid") val studentUid: String,
    val score: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("client_submission_id") val clientSubmissionId: String? = null,
    @SerialName("submitted_at") val submittedAt: String
)
