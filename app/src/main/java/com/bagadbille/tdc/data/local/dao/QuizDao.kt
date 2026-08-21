package com.bagadbille.tdc.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bagadbille.tdc.data.local.entity.PendingSubmissionEntity
import com.bagadbille.tdc.data.local.entity.QuizQuestionEntity

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM cached_quiz_questions WHERE quizId = :quizId ORDER BY `order`")
    suspend fun getQuestionsByQuizId(quizId: String): List<QuizQuestionEntity>

    @Query("DELETE FROM cached_quiz_questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsByQuizId(quizId: String)

    @Insert
    suspend fun insertPendingSubmission(submission: PendingSubmissionEntity)

    @Query("SELECT * FROM pending_submissions ORDER BY createdAt ASC")
    suspend fun getPendingSubmissions(): List<PendingSubmissionEntity>

    @Delete
    suspend fun deletePendingSubmission(submission: PendingSubmissionEntity)
}
