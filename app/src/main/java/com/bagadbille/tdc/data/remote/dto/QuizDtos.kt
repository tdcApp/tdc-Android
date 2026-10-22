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
    val createdByName: String? = null,
    @SerialName("created_by_name") val createdByNameSnake: String? = null,
    val creatorRole: String? = null,
    @SerialName("creator_role") val creatorRoleSnake: String? = null,
    val classId: String? = null,
    @SerialName("class_id") val classIdSnake: String? = null,
    val createdAt: String? = null,
    @SerialName("created_at") val createdAtSnake: String? = null,
    val updatedAt: String? = null,
    @SerialName("updated_at") val updatedAtSnake: String? = null,
    val isSubmitted: Boolean = false,
    @SerialName("is_submitted") val isSubmittedSnake: Boolean? = null,
    val questionCount: Int = 0,
    @SerialName("question_count") val questionCountSnake: Int = 0,
    val submissionCount: Int = 0,
    @SerialName("submission_count") val submissionCountSnake: Int = 0,
    val avgScore: Double = 0.0,
    @SerialName("avg_score") val avgScoreSnake: Double = 0.0,
    val startTime: String? = null,
    @SerialName("start_time") val startTimeSnake: String? = null,
    val enteringWindow: Int? = null,
    @SerialName("entering_window") val enteringWindowSnake: Int? = null,
    val totalTime: Int = 30,
    @SerialName("total_time") val totalTimeSnake: Int? = null,
    val hasActiveAttempt: Boolean = false,
    @SerialName("has_active_attempt") val hasActiveAttemptSnake: Boolean? = null
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
        isSubmitted = isSubmitted || (isSubmittedSnake == true),
        createdByName = createdByName ?: createdByNameSnake,
        creatorRole = creatorRole ?: creatorRoleSnake,
        questionCount = if (questionCount > 0) questionCount else questionCountSnake,
        submissionCount = if (submissionCount > 0) submissionCount else submissionCountSnake,
        avgScore = if (avgScore > 0.0) avgScore else avgScoreSnake,
        startTime = startTime ?: startTimeSnake,
        enteringWindow = enteringWindow ?: enteringWindowSnake,
        totalTime = totalTimeSnake ?: totalTime,
        hasActiveAttempt = hasActiveAttempt || (hasActiveAttemptSnake == true)
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
data class ActiveQuizAttemptDto(
    val startedAt: String,
    @SerialName("started_at") val startedAtSnake: String? = null,
    val warningCount: Int = 0,
    @SerialName("warning_count") val warningCountSnake: Int = 0,
    val remainingSeconds: Int = 0,
    @SerialName("remaining_seconds") val remainingSecondsSnake: Int = 0,
    val isDisqualified: Boolean = false,
    @SerialName("is_disqualified") val isDisqualifiedSnake: Boolean = false
) {
    fun toDomain(): com.bagadbille.tdc.data.model.ActiveQuizAttempt = com.bagadbille.tdc.data.model.ActiveQuizAttempt(
        startedAt = startedAt.ifBlank { startedAtSnake ?: "" },
        warningCount = if (warningCount > 0) warningCount else warningCountSnake,
        remainingSeconds = if (remainingSeconds > 0) remainingSeconds else remainingSecondsSnake,
        isDisqualified = isDisqualified || isDisqualifiedSnake
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
    val startTime: String? = null,
    @SerialName("start_time") val startTimeSnake: String? = null,
    val enteringWindow: Int? = null,
    @SerialName("entering_window") val enteringWindowSnake: Int? = null,
    val totalTime: Int = 30,
    @SerialName("total_time") val totalTimeSnake: Int? = null,
    val activeAttempt: ActiveQuizAttemptDto? = null,
    @SerialName("active_attempt") val activeAttemptSnake: ActiveQuizAttemptDto? = null,
    val questions: List<QuizQuestionDto> = emptyList()
) {
    fun toDomain(): Quiz {
        val attempt = (activeAttempt ?: activeAttemptSnake)?.toDomain()
        return Quiz(
            id = id,
            title = title,
            audience = audience.lowercase(),
            targetYears = targetYears ?: targetYearsSnake,
            createdBy = createdBy ?: createdBySnake,
            classId = classId ?: classIdSnake,
            createdAt = createdAt ?: createdAtSnake,
            updatedAt = updatedAt ?: updatedAtSnake,
            isSubmitted = isSubmitted || (isSubmittedSnake == true),
            startTime = startTime ?: startTimeSnake,
            enteringWindow = enteringWindow ?: enteringWindowSnake,
            totalTime = totalTimeSnake ?: totalTime,
            activeAttempt = attempt,
            hasActiveAttempt = attempt != null
        )
    }
}

@Serializable
data class QuizStartResponse(
    val status: String,
    val isResume: Boolean = false,
    @SerialName("is_resume") val isResumeSnake: Boolean? = null,
    val startedAt: String? = null,
    @SerialName("started_at") val startedAtSnake: String? = null,
    val warningCount: Int = 0,
    @SerialName("warning_count") val warningCountSnake: Int? = null,
    val remainingSeconds: Int = 0,
    @SerialName("remaining_seconds") val remainingSecondsSnake: Int? = null,
    val isDisqualified: Boolean = false,
    @SerialName("is_disqualified") val isDisqualifiedSnake: Boolean? = null,
    val isExpired: Boolean = false,
    @SerialName("is_expired") val isExpiredSnake: Boolean? = null
)

@Serializable
data class QuizStrikeResponse(
    val warningCount: Int = 0,
    @SerialName("warning_count") val warningCountSnake: Int? = null,
    val isDisqualified: Boolean = false,
    @SerialName("is_disqualified") val isDisqualifiedSnake: Boolean? = null
)

@Serializable
data class CreateQuizRequest(
    val title: String,
    val audience: String,
    val targetYears: String? = null,
    val classId: String? = null,
    val startTime: String? = null,
    val enteringWindow: Int? = null,
    val totalTime: Int = 30,
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
    val answers: List<QuizAnswerDto>,
    val startedAt: String? = null,
    val timeTakenSeconds: Int? = null,
    val isDisqualified: Boolean = false
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
    @SerialName("student_enrollment") val studentEnrollmentSnake: String? = null,
    @SerialName("enrollment_number") val enrollmentNumberSnake: String? = null,
    val startedAt: String? = null,
    @SerialName("started_at") val startedAtSnake: String? = null,
    val timeTakenSeconds: Int? = null,
    @SerialName("time_taken_seconds") val timeTakenSecondsSnake: Int? = null
) {
    fun toDomain(): QuizSubmission = QuizSubmission(
        quizId = quizId ?: quizIdSnake ?: "",
        studentUid = studentUid ?: studentUidSnake ?: "",
        score = score,
        totalQuestions = if (totalQuestions > 0) totalQuestions else (totalQuestionsSnake ?: 0),
        clientSubmissionId = clientSubmissionId ?: clientSubmissionIdSnake,
        submittedAt = submittedAt ?: submittedAtSnake ?: "",
        startedAt = startedAt ?: startedAtSnake,
        timeTakenSeconds = timeTakenSeconds ?: timeTakenSecondsSnake
    )
}
