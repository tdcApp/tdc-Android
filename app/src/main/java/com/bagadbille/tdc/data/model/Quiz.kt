package com.bagadbille.tdc.data.model

data class Quiz(
    val id: String,
    val title: String,
    val subject: String,
    val description: String? = null,
    val questionCount: Int,
    val timeLimitMinutes: Int,
    val status: QuizStatus,
    val scheduledAt: String? = null,
    val deadlineAt: String? = null
)

enum class QuizStatus { AVAILABLE, UPCOMING, COMPLETED, SUBMITTED }

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<QuizOption>,
    val isMultiAnswer: Boolean,
    val order: Int
)

data class QuizOption(val id: String, val text: String)

data class QuizAnswer(val questionId: String, val selectedOptionIds: List<String>)

data class QuizResult(
    val quizId: String,
    val status: QuizResultStatus,
    val score: Int? = null,
    val totalScore: Int? = null,
    val percentage: Double? = null,
    val breakdown: List<QuestionResult>? = null
)

enum class QuizResultStatus { PENDING, RELEASED }

data class QuestionResult(
    val questionId: String,
    val questionText: String,
    val selectedOptionIds: List<String>,
    val correctOptionIds: List<String>,
    val isCorrect: Boolean
)
