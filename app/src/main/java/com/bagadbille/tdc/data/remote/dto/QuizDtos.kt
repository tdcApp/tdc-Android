package com.bagadbille.tdc.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String, val title: String, val subject: String,
    val description: String? = null, val questionCount: Int,
    val timeLimitMinutes: Int, val status: String,
    val scheduledAt: String? = null, val deadlineAt: String? = null
)

@Serializable
data class QuizDetailDto(
    val id: String, val title: String, val subject: String,
    val timeLimitMinutes: Int, val questions: List<QuizQuestionDto>
)

@Serializable
data class QuizQuestionDto(
    val id: String, val questionText: String,
    val options: List<QuizOptionDto>, val isMultiAnswer: Boolean, val order: Int
)

@Serializable
data class QuizOptionDto(val id: String, val text: String)

@Serializable
data class QuizSubmissionRequest(val answers: List<QuizAnswerDto>)

@Serializable
data class QuizAnswerDto(val questionId: String, val selectedOptionIds: List<String>)

@Serializable
data class QuizSubmissionResponse(val status: String, val submittedAt: String)

@Serializable
data class QuizResultDto(
    val quizId: String, val status: String,
    val score: Int? = null, val totalScore: Int? = null,
    val percentage: Double? = null, val breakdown: List<QuestionResultDto>? = null
)

@Serializable
data class QuestionResultDto(
    val questionId: String, val questionText: String,
    val selectedOptionIds: List<String>, val correctOptionIds: List<String>,
    val isCorrect: Boolean
)
