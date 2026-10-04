package com.bagadbille.tdc.data.model

data class Quiz(
    val id: String,
    val title: String,
    val audience: String,
    val createdBy: String? = null,
    val classId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String,
    val questionOrder: Int,
    val quizId: String
)

data class QuizAnswer(val questionId: String, val selectedOption: String)

data class QuizResult(
    val quizId: String,
    val score: Int,
    val totalQuestions: Int,
    val submittedAt: String? = null
)

data class QuizQuestionDraft(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String = "a"
)

data class QuizStats(
    val quizId: String,
    val totalAttempts: Int,
    val averageScore: Double,
    val highestScore: Int,
    val totalQuestions: Int
)

data class QuizSubmissionDetail(
    val submission: QuizSubmission,
    val studentName: String,
    val studentEmail: String,
    val studentEnrollment: String?
)

