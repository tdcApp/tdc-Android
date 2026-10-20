package com.bagadbille.tdc.data.remote.dto

import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizQuestion
import com.bagadbille.tdc.data.model.QuizSubmission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val audience: String = "ALL",
    val targetYears: String? = null,
    @SerialName("target_years") val targetYearsSnake: String? = null,
    val createdBy: String? = null,
    @SerialName("created_by") val createdBySnake: String? = null,
    val classId: String? = null,
    @SerialName("class_id") val classIdSnake: String? = null,
    val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null,
    val isSubmitted: Boolean = false,
    @SerialName("is_submitted") val isSubmittedSnake: Boolean? = null
) {
    fun toDomain(): Quiz = Quiz(
        id = id,
        title = title,
        audience = audience.lowercase(),
        targetYears = targetYears ?: targetYearsSnake,
        createdBy = createdBy ?: createdBySnake,
        classId = classId ?: classIdSnake,
        createdAt = createdAt ?: createdAtSnake,
        updatedAt = updatedAt ?: updatedAtSnake,
        isSubmitted = isSubmitted || (isSubmittedSnake == true)
    )
}

@Serializable
data class QuizQuestionDto(
    val id: String,
    val question: String,
    val optionA: String = "",
    @SerialName("option_a") val optionASnake: String? = null,
    val optionB: String = "",
    @SerialName("option_b") val optionBSnake: String? = null,
    val optionC: String = "",
    @SerialName("option_c") val optionCSnake: String? = null,
    val optionD: String = "",
    @SerialName("option_d") val optionDSnake: String? = null,
    val correctOption: String? = null,
    @SerialName("correct_option") val correctOptionSnake: String? = null,
    val questionOrder: Int = 1,
    @SerialName("question_order") val questionOrderSnake: Int? = null,
    val quizId: String? = null,
    @SerialName("quiz_id") val quizIdSnake: String? = null
) {
    fun toDomain(fallbackQuizId: String = ""): QuizQuestion = QuizQuestion(
        id = id,
        question = question,
        optionA = optionA.ifBlank { optionASnake ?: "" },
        optionB = optionB.ifBlank { optionBSnake ?: "" },
        optionC = optionC.ifBlank { optionCSnake ?: "" },
        optionD = optionD.ifBlank { optionDSnake ?: "" },
        correctOption = (correctOption ?: correctOptionSnake ?: "a").lowercase(),
        questionOrder = questionOrderSnake ?: questionOrder,
        quizId = quizId ?: quizIdSnake ?: fallbackQuizId
    )
}

@Serializable
data class QuizDetailDto(
    val id: String,
    val title: String,
    val audience: String = "ALL",
    val targetYears: String? = null,
    @SerialName("target_years") val targetYearsSnake: String? = null,
    val createdBy: String? = null,
    @SerialName("created_by") val createdBySnake: String? = null,
    val classId: String? = null,
    @SerialName("class_id") val classIdSnake: String? = null,
    val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null,
    val isSubmitted: Boolean = false,
    @SerialName("is_submitted") val isSubmittedSnake: Boolean? = null,
    val questions: List<QuizQuestionDto> = emptyList()
) {
    fun toDomain(): Quiz = Quiz(
        id = id,
        title = title,
        audience = audience.lowercase(),
        targetYears = targetYears ?: targetYearsSnake,
        createdBy = createdBy ?: createdBySnake,
        classId = classId ?: classIdSnake,
        createdAt = createdAt ?: createdAtSnake,
        updatedAt = updatedAt ?: updatedAtSnake,
        isSubmitted = isSubmitted || (isSubmittedSnake == true)
    )
}

@Serializable
data class CreateQuizRequest(
    val title: String,
    val audience: String,
    val targetYears: String? = null,
    val classId: String? = null,
    val questions: List<CreateQuizQuestionRequest>
)

@Serializable
data class CreateQuizQuestionRequest(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String
)

@Serializable
data class QuizSubmitRequest(
    val clientSubmissionId: String,
    val answers: List<QuizAnswerDto>
)

@Serializable
data class QuizAnswerDto(
    val questionId: String,
    val selectedOption: String
)

@Serializable
data class QuizSubmissionDto(
    val quizId: String? = null,
    @SerialName("quiz_id") val quizIdSnake: String? = null,
    val studentUid: String? = null,
    @SerialName("student_uid") val studentUidSnake: String? = null,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    @SerialName("total_questions") val totalQuestionsSnake: Int? = null,
    val clientSubmissionId: String? = null,
    @SerialName("client_submission_id") val clientSubmissionIdSnake: String? = null,
    val submittedAt: String? = null,
    @SerialName("submitted_at") val submittedAtSnake: String? = null,
    val studentName: String? = null,
    @SerialName("student_name") val studentNameSnake: String? = null,
    val studentEmail: String? = null,
    @SerialName("student_email") val studentEmailSnake: String? = null,
    val studentEnrollment: String? = null,
    @SerialName("student_enrollment") val studentEnrollmentSnake: String? = null
) {
    fun toDomain(): QuizSubmission = QuizSubmission(
        quizId = quizId ?: quizIdSnake ?: "",
        studentUid = studentUid ?: studentUidSnake ?: "",
        score = score,
        totalQuestions = if (totalQuestions > 0) totalQuestions else (totalQuestionsSnake ?: 0),
        clientSubmissionId = clientSubmissionId ?: clientSubmissionIdSnake,
        submittedAt = submittedAt ?: submittedAtSnake ?: ""
    )
}
