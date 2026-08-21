package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizApi {
    @GET("quizzes")
    suspend fun getQuizzes(): List<QuizDto>

    @GET("quizzes/{id}")
    suspend fun getQuizDetail(@Path("id") id: String): QuizDetailDto

    @POST("quizzes/{id}/submit")
    suspend fun submitQuiz(@Path("id") id: String, @Body submission: QuizSubmissionRequest): QuizSubmissionResponse

    @GET("quizzes/{id}/results")
    suspend fun getQuizResults(@Path("id") id: String): QuizResultDto
}
