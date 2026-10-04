package com.bagadbille.tdc.data.model

data class QuizSubmission(
    val quizId: String,
    val studentUid: String,
    val score: Int,
    val totalQuestions: Int,
    val clientSubmissionId: String? = null,
    val submittedAt: String
)
