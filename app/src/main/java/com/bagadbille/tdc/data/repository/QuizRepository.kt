package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.*
import kotlinx.coroutines.delay
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface QuizRepository {
    suspend fun getQuizzes(): Result<List<Quiz>>
    suspend fun getQuizQuestions(quizId: String): Result<List<QuizQuestion>>
    suspend fun submitQuiz(quizId: String, answers: List<QuizAnswer>, clientSubmissionId: String): Result<QuizSubmission>
    suspend fun getQuizResult(quizId: String): Result<QuizResult>

    // ─── Mentor ───
    suspend fun createQuiz(
        title: String,
        audience: String,
        classId: String?,
        questions: List<QuizQuestionDraft>
    ): Result<Quiz>
    suspend fun deleteQuiz(quizId: String): Result<Unit>
    suspend fun getQuizStats(quizId: String): Result<QuizStats>
    suspend fun getQuizSubmissionsWithDetails(quizId: String): Result<List<QuizSubmissionDetail>>
}

@Singleton
class QuizRepositoryImpl @Inject constructor() : QuizRepository {

    private val quizzes = mutableListOf(
        Quiz("quiz_001", "TDC Selection Round - C++", "all", "mentor_001", "cls_001", "2026-08-20T09:00:00Z"),
        Quiz("quiz_002", "Python Basics Screening", "all", "mentor_002", "cls_002", "2026-08-18T10:00:00Z"),
        Quiz("quiz_003", "Web Dev Aptitude Test", "all", "mentor_003", "cls_003", "2026-08-28T09:00:00Z"),
        Quiz("quiz_004", "DSA Weekly Challenge #4", "technology", "mentor_002", "cls_002", "2026-08-15T10:00:00Z"),
    )

    private val questionsMap = mutableMapOf<String, MutableList<QuizQuestion>>(
        "quiz_001" to mutableListOf(
            QuizQuestion("q_001", "What is the output of: cout << (5 / 2);",
                "2", "2.5", "2.0", "Compilation Error", "a", 1, "quiz_001"),
            QuizQuestion("q_002", "Which of the following is NOT a valid C++ data type?",
                "int", "float", "real", "double", "c", 2, "quiz_001"),
            QuizQuestion("q_003", "What does STL stand for in C++?",
                "Standard Type Library", "Standard Template Library", "Static Template Library", "System Type Library", "b", 3, "quiz_001"),
        ),
        "quiz_002" to mutableListOf(
            QuizQuestion("q_004", "What is the output of: print(type([]))?",
                "<class 'tuple'>", "<class 'list'>", "<class 'dict'>", "<class 'set'>", "b", 1, "quiz_002"),
            QuizQuestion("q_005", "Which keyword is used to define a function in Python?",
                "func", "define", "def", "function", "c", 2, "quiz_002"),
        ),
        "quiz_003" to mutableListOf(
            QuizQuestion("q_006", "What is the correct HTML element for inserting a line break?",
                "<break>", "<br>", "<lb>", "<newline>", "b", 1, "quiz_003"),
            QuizQuestion("q_007", "Which CSS property controls the text size?",
                "font-style", "text-size", "font-size", "text-style", "c", 2, "quiz_003"),
        ),
        "quiz_004" to mutableListOf(
            QuizQuestion("q_008", "What is the time complexity of binary search?",
                "O(1)", "O(n)", "O(log n)", "O(n^2)", "c", 1, "quiz_004"),
        )
    )

    private val submissions = mutableListOf(
        QuizSubmission("quiz_001", "user_001", 3, 3, "sub_q_01", "2026-08-20T10:30:00Z"),
        QuizSubmission("quiz_001", "user_003", 2, 3, "sub_q_02", "2026-08-20T11:15:00Z"),
        QuizSubmission("quiz_001", "user_004", 3, 3, "sub_q_03", "2026-08-20T12:00:00Z"),
        QuizSubmission("quiz_001", "user_005", 1, 3, "sub_q_04", "2026-08-20T14:20:00Z"),
        QuizSubmission("quiz_001", "user_006", 2, 3, "sub_q_05", "2026-08-20T15:45:00Z"),
        QuizSubmission("quiz_002", "user_001", 2, 2, "sub_q_06", "2026-08-18T10:45:00Z"),
        QuizSubmission("quiz_002", "user_005", 2, 2, "sub_q_07", "2026-08-18T11:30:00Z"),
        QuizSubmission("quiz_002", "user_006", 1, 2, "sub_q_08", "2026-08-18T13:00:00Z"),
    )

    override suspend fun getQuizzes(): Result<List<Quiz>> {
        delay(300)
        // TODO: Replace with GET /quizzes from NestJS backend
        return Result.success(quizzes.sortedByDescending { it.createdAt ?: "" }.toList())
    }

    override suspend fun getQuizQuestions(quizId: String): Result<List<QuizQuestion>> {
        delay(300)
        // TODO: Replace with GET /quizzes/{id}/questions from NestJS backend
        return Result.success(questionsMap[quizId]?.toList() ?: emptyList())
    }

    override suspend fun submitQuiz(quizId: String, answers: List<QuizAnswer>, clientSubmissionId: String): Result<QuizSubmission> {
        delay(600)
        // TODO: Replace with POST /quizzes/{id}/submit to NestJS backend
        val questions = questionsMap[quizId] ?: emptyList()
        val score = answers.count { answer ->
            val q = questions.firstOrNull { it.id == answer.questionId }
            q != null && q.correctOption.equals(answer.selectedOption, ignoreCase = true)
        }
        val sub = QuizSubmission(
            quizId = quizId,
            studentUid = "user_001",
            score = score,
            totalQuestions = questions.size,
            clientSubmissionId = clientSubmissionId,
            submittedAt = Instant.now().toString()
        )
        submissions.add(sub)
        return Result.success(sub)
    }

    override suspend fun getQuizResult(quizId: String): Result<QuizResult> {
        delay(300)
        // TODO: Replace with GET /quizzes/{id}/result from NestJS backend
        val lastSub = submissions.lastOrNull { it.quizId == quizId && it.studentUid == "user_001" }
        return if (lastSub != null) {
            Result.success(QuizResult(quizId, lastSub.score, lastSub.totalQuestions, lastSub.submittedAt))
        } else {
            Result.success(QuizResult(quizId, 0, questionsMap[quizId]?.size ?: 0, null))
        }
    }

    override suspend fun createQuiz(
        title: String,
        audience: String,
        classId: String?,
        questions: List<QuizQuestionDraft>
    ): Result<Quiz> = runCatching {
        delay(500)
        // TODO: Replace with POST /quizzes and POST /quizzes/{id}/questions to NestJS backend
        val quizId = "quiz_${System.currentTimeMillis()}"
        val now = Instant.now().toString()
        val newQuiz = Quiz(
            id = quizId,
            title = title,
            audience = audience,
            createdBy = "mentor_001",
            classId = classId,
            createdAt = now,
            updatedAt = now
        )
        quizzes.add(0, newQuiz)

        val createdQuestions = questions.mapIndexed { index, draft ->
            QuizQuestion(
                id = "q_${quizId}_${index + 1}",
                question = draft.question,
                optionA = draft.optionA,
                optionB = draft.optionB,
                optionC = draft.optionC,
                optionD = draft.optionD,
                correctOption = draft.correctOption.lowercase(),
                questionOrder = index + 1,
                quizId = quizId
            )
        }
        questionsMap[quizId] = createdQuestions.toMutableList()
        newQuiz
    }

    override suspend fun deleteQuiz(quizId: String): Result<Unit> = runCatching {
        delay(400)
        // TODO: Replace with DELETE /quizzes/{id} to NestJS backend
        if (!quizzes.removeAll { it.id == quizId }) {
            throw IllegalArgumentException("Quiz not found")
        }
        questionsMap.remove(quizId)
        submissions.removeAll { it.quizId == quizId }
    }

    override suspend fun getQuizStats(quizId: String): Result<QuizStats> = runCatching {
        delay(200)
        val quizSubs = submissions.filter { it.quizId == quizId }
        val totalQuestions = questionsMap[quizId]?.size ?: 0
        if (quizSubs.isEmpty()) {
            QuizStats(quizId, 0, 0.0, 0, totalQuestions)
        } else {
            val totalScore = quizSubs.sumOf { it.score }
            val avg = totalScore.toDouble() / quizSubs.size
            val max = quizSubs.maxOf { it.score }
            QuizStats(quizId, quizSubs.size, avg, max, totalQuestions)
        }
    }

    override suspend fun getQuizSubmissionsWithDetails(quizId: String): Result<List<QuizSubmissionDetail>> = runCatching {
        delay(300)
        val quizSubs = submissions.filter { it.quizId == quizId }.sortedByDescending { it.score }
        quizSubs.map { sub ->
            val user = MockUsers.findById(sub.studentUid)
            QuizSubmissionDetail(
                submission = sub,
                studentName = user?.name ?: user?.email ?: "Student (${sub.studentUid})",
                studentEmail = user?.email ?: "student@gmail.com",
                studentEnrollment = user?.enrollmentNumber
            )
        }
    }
}
