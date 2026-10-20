package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.*
import com.bagadbille.tdc.data.remote.api.ClassApi
import com.bagadbille.tdc.data.remote.api.QuizApi
import com.bagadbille.tdc.data.remote.dto.CreateQuizQuestionRequest
import com.bagadbille.tdc.data.remote.dto.CreateQuizRequest
import com.bagadbille.tdc.data.remote.dto.QuizAnswerDto
import com.bagadbille.tdc.data.remote.dto.QuizSubmitRequest
import com.bagadbille.tdc.data.remote.dto.UserProfileDto
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface QuizRepository {
    suspend fun getQuizzes(): Result<List<Quiz>>
    suspend fun getQuiz(quizId: String): Result<Quiz>
    suspend fun getQuizQuestions(quizId: String): Result<List<QuizQuestion>>
    suspend fun submitQuiz(quizId: String, answers: List<QuizAnswer>, clientSubmissionId: String, isDisqualified: Boolean = false): Result<QuizSubmission>
    suspend fun getQuizResult(quizId: String): Result<QuizResult>
    suspend fun isQuizSubmitted(quizId: String): Boolean
    suspend fun markQuizSubmittedLocally(quizId: String)

    // ─── Mentor ───
    suspend fun createQuiz(
        title: String,
        audience: String,
        classId: String?,
        targetYears: String? = null,
        questions: List<QuizQuestionDraft>
    ): Result<Quiz>
    suspend fun deleteQuiz(quizId: String): Result<Unit>
    suspend fun getQuizStats(quizId: String): Result<QuizStats>
    suspend fun getQuizSubmissionsWithDetails(quizId: String): Result<List<QuizSubmissionDetail>>
}

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val quizApi: QuizApi,
    private val classApi: ClassApi,
    private val dataStoreManager: DataStoreManager
) : QuizRepository {

    private val studentCache = ConcurrentHashMap<String, UserProfileDto>()

    override suspend fun getQuizzes(): Result<List<Quiz>> = runCatching {
        val localSubmitted = dataStoreManager.submittedQuizIds.firstOrNull() ?: emptySet()
        val dtos = quizApi.getQuizzes()
        dtos.map { dto ->
            val quiz = dto.toDomain()
            if (quiz.isSubmitted) {
                dataStoreManager.markQuizSubmitted(quiz.id)
                quiz
            } else if (quiz.id in localSubmitted) {
                quiz.copy(isSubmitted = true)
            } else {
                quiz
            }
        }
    }

    override suspend fun getQuiz(quizId: String): Result<Quiz> = runCatching {
        val localSubmitted = dataStoreManager.submittedQuizIds.firstOrNull() ?: emptySet()
        val detail = quizApi.getQuizDetail(quizId)
        val quiz = detail.toDomain()
        if (quiz.isSubmitted || quiz.id in localSubmitted) {
            dataStoreManager.markQuizSubmitted(quiz.id)
            quiz.copy(isSubmitted = true)
        } else {
            quiz
        }
    }

    override suspend fun isQuizSubmitted(quizId: String): Boolean {
        val localSubmitted = dataStoreManager.submittedQuizIds.firstOrNull() ?: emptySet()
        if (quizId in localSubmitted) return true

        return try {
            val sub = quizApi.getMySubmission(quizId)
            dataStoreManager.markQuizSubmitted(quizId)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun markQuizSubmittedLocally(quizId: String) {
        dataStoreManager.markQuizSubmitted(quizId)
    }

    override suspend fun getQuizQuestions(quizId: String): Result<List<QuizQuestion>> = runCatching {
        val detail = quizApi.getQuizDetail(quizId)
        detail.questions.map { it.toDomain(fallbackQuizId = quizId) }
    }

    override suspend fun submitQuiz(
        quizId: String,
        answers: List<QuizAnswer>,
        clientSubmissionId: String,
        isDisqualified: Boolean
    ): Result<QuizSubmission> = runCatching {
        val mappedAnswers = answers.map {
            QuizAnswerDto(
                questionId = it.questionId,
                selectedOption = it.selectedOption.trim().uppercase()
            )
        }
        val request = QuizSubmitRequest(
            clientSubmissionId = clientSubmissionId,
            answers = mappedAnswers
        )
        val submission = quizApi.submitQuiz(quizId, request).toDomain()
        dataStoreManager.markQuizSubmitted(quizId)
        submission
    }

    override suspend fun getQuizResult(quizId: String): Result<QuizResult> = runCatching {
        val sub = quizApi.getMySubmission(quizId)
        dataStoreManager.markQuizSubmitted(quizId)
        QuizResult(
            quizId = sub.quizId ?: quizId,
            score = sub.score,
            totalQuestions = sub.totalQuestions,
            submittedAt = sub.submittedAt
        )
    }

    override suspend fun createQuiz(
        title: String,
        audience: String,
        classId: String?,
        targetYears: String?,
        questions: List<QuizQuestionDraft>
    ): Result<Quiz> = runCatching {
        val finalAudience = if (!classId.isNullOrBlank()) "CLASS" else audience.trim().uppercase()
        val finalClassId = if (finalAudience == "CLASS") classId?.trim() else null
        val cleanTargetYears = targetYears?.trim()?.ifBlank { null }

        val request = CreateQuizRequest(
            title = title.trim(),
            audience = finalAudience,
            targetYears = cleanTargetYears,
            classId = finalClassId,
            questions = questions.map {
                CreateQuizQuestionRequest(
                    question = it.question.trim(),
                    optionA = it.optionA.trim(),
                    optionB = it.optionB.trim(),
                    optionC = it.optionC.trim(),
                    optionD = it.optionD.trim(),
                    correctOption = it.correctOption.trim().uppercase()
                )
            }
        )
        quizApi.createQuiz(request).toDomain()
    }

    override suspend fun deleteQuiz(quizId: String): Result<Unit> = runCatching {
        quizApi.deleteQuiz(quizId)
    }

    override suspend fun getQuizStats(quizId: String): Result<QuizStats> = runCatching {
        val submissions = try {
            quizApi.getAllSubmissions(quizId)
        } catch (_: Exception) {
            emptyList()
        }
        val totalAttempts = submissions.size
        val avgScore = if (totalAttempts > 0) submissions.map { it.score }.average() else 0.0
        val highestScore = submissions.maxOfOrNull { it.score } ?: 0
        val totalQuestions = submissions.firstOrNull()?.totalQuestions ?: 0

        QuizStats(
            quizId = quizId,
            totalAttempts = totalAttempts,
            averageScore = avgScore,
            highestScore = highestScore,
            totalQuestions = totalQuestions
        )
    }

    override suspend fun getQuizSubmissionsWithDetails(quizId: String): Result<List<QuizSubmissionDetail>> = runCatching {
        val currentUid = dataStoreManager.firebaseUid.firstOrNull()
        val currentName = dataStoreManager.userName.firstOrNull()
        val currentEmail = dataStoreManager.userEmail.firstOrNull()

        val submissions = try {
            quizApi.getAllSubmissions(quizId)
        } catch (_: Exception) {
            emptyList()
        }

        val missingUids = submissions
            .filter { (it.studentName.isNullOrBlank() || it.studentName == "Student") && (it.studentUid != currentUid) && !studentCache.containsKey(it.studentUid) }
            .map { it.studentUid }
            .toSet()

        if (missingUids.isNotEmpty()) {
            try {
                val classes = classApi.getClasses()
                for (cls in classes) {
                    try {
                        val students = classApi.getStudentsByClass(cls.id)
                        for (st in students) {
                            val uid = st.effectiveUid
                            if (uid.isNotBlank()) {
                                studentCache[uid] = st
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        submissions.map { subDto ->
            val sub = subDto.toDomain()
            val cachedStudent = studentCache[sub.studentUid]

            val studentName = subDto.studentName?.ifBlank { null }?.takeIf { it != "Student" }
                ?: subDto.studentNameSnake?.ifBlank { null }?.takeIf { it != "Student" }
                ?: cachedStudent?.name?.ifBlank { null }
                ?: (if (sub.studentUid == currentUid && !currentName.isNullOrBlank()) currentName else null)
                ?: cachedStudent?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: subDto.studentEmail?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: (if (sub.studentUid == currentUid && !currentEmail.isNullOrBlank()) currentEmail.substringBefore("@") else null)
                ?: "Candidate (${sub.studentUid.take(6)})"

            val studentEmail = subDto.studentEmail?.ifBlank { null }
                ?: subDto.studentEmailSnake?.ifBlank { null }
                ?: cachedStudent?.email?.ifBlank { null }
                ?: (if (sub.studentUid == currentUid) currentEmail else null)
                ?: ""

            val studentEnrollment = subDto.studentEnrollment
                ?: subDto.studentEnrollmentSnake

            QuizSubmissionDetail(
                submission = sub,
                studentName = studentName,
                studentEmail = studentEmail,
                studentEnrollment = studentEnrollment
            )
        }
    }
}
