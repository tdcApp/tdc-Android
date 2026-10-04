package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizApi {
    @GET("quizzes")
    suspend fun getQuizzes(): List<QuizDto>

    @GET("quizzes/{id}/questions")
    suspend fun getQuizQuestions(@Path("id") id: String): List<QuizQuestionDto>

    @POST("quizzes/{id}/submit")
    suspend fun submitQuiz(@Path("id") id: String, @Body submission: QuizSubmitRequest): QuizSubmissionDto

    @GET("quizzes/{id}/result")
    suspend fun getQuizResult(@Path("id") id: String): QuizSubmissionDto
}
