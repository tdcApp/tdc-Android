package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface QuizRepository {
    suspend fun getQuizzes(): Result<List<Quiz>>
    suspend fun getQuizDetail(id: String): Result<List<QuizQuestion>>
    suspend fun submitQuiz(quizId: String, answers: List<QuizAnswer>): Result<Unit>
    suspend fun getQuizResults(quizId: String): Result<QuizResult>
}

@Singleton
class QuizRepositoryImpl @Inject constructor() : QuizRepository {
    override suspend fun getQuizzes(): Result<List<Quiz>> {
        delay(700)
        return Result.success(listOf(
            Quiz("quiz_001", "Physics - Wave Optics", "Physics", "Test your knowledge on wave optics", 15, 30, QuizStatus.AVAILABLE, null, "2026-08-25T23:59:00Z"),
            Quiz("quiz_002", "Calculus Mid-Term", "Mathematics", "Chapters 1-5 comprehensive test", 25, 45, QuizStatus.AVAILABLE, null, "2026-08-22T15:00:00Z"),
            Quiz("quiz_003", "Organic Chemistry Quiz", "Chemistry", "Nomenclature and reactions", 10, 20, QuizStatus.UPCOMING, "2026-08-28T09:00:00Z", "2026-08-28T09:30:00Z"),
            Quiz("quiz_004", "English Literature", "English", "Shakespeare & Modern Poetry", 20, 40, QuizStatus.UPCOMING, "2026-09-01T10:00:00Z", null),
            Quiz("quiz_005", "Data Structures Basics", "CS", "Arrays, Linked Lists, Stacks", 12, 25, QuizStatus.COMPLETED, null, null),
            Quiz("quiz_006", "Physics - Kinematics", "Physics", "Motion in 1D and 2D", 10, 20, QuizStatus.SUBMITTED, null, null),
        ))
    }

    override suspend fun getQuizDetail(id: String): Result<List<QuizQuestion>> {
        delay(500)
        return Result.success(listOf(
            QuizQuestion("q_001", "What is the wavelength of light used in Young's double slit experiment if the fringe width is 0.5 mm?",
                listOf(QuizOption("a", "400 nm"), QuizOption("b", "500 nm"), QuizOption("c", "600 nm"), QuizOption("d", "700 nm")), false, 1),
            QuizQuestion("q_002", "Which of the following phenomena support the wave theory of light? (Select all that apply)",
                listOf(QuizOption("e", "Interference"), QuizOption("f", "Diffraction"), QuizOption("g", "Photoelectric effect"), QuizOption("h", "Polarization")), true, 2),
            QuizQuestion("q_003", "In a single slit diffraction pattern, the first minimum occurs at angle θ. What is sin θ equal to?",
                listOf(QuizOption("i", "λ/a"), QuizOption("j", "2λ/a"), QuizOption("k", "λ/2a"), QuizOption("l", "a/λ")), false, 3),
        ))
    }

    override suspend fun submitQuiz(quizId: String, answers: List<QuizAnswer>): Result<Unit> { delay(1000); return Result.success(Unit) }

    override suspend fun getQuizResults(quizId: String): Result<QuizResult> {
        delay(600)
        return if (quizId == "quiz_006") {
            Result.success(QuizResult(quizId, QuizResultStatus.PENDING))
        } else {
            Result.success(QuizResult(quizId, QuizResultStatus.RELEASED, 8, 12, 66.7, listOf(
                QuestionResult("q_001", "Sample question 1", listOf("b"), listOf("b"), true),
                QuestionResult("q_002", "Sample question 2", listOf("e", "f"), listOf("e", "f", "h"), false),
            )))
        }
    }
}
