package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizApi {
    @GET("quizzes")
    suspend fun getQuizzes(): List<QuizDto>

    @GET("quizzes/{id}")
    suspend fun getQuizDetail(@Path("id") id: String): QuizDetailDto

    @POST("quizzes")
    suspend fun createQuiz(@Body request: CreateQuizRequest): QuizDetailDto

    @POST("quizzes/{id}/submit")
    suspend fun submitQuiz(
        @Path("id") id: String,
        @Body request: QuizSubmitRequest
    ): QuizSubmissionDto

    @GET("quizzes/{id}/my-submission")
    suspend fun getMySubmission(@Path("id") id: String): QuizSubmissionDto

    @GET("quizzes/{id}/submissions")
    suspend fun getAllSubmissions(@Path("id") id: String): List<QuizSubmissionDto>

    @DELETE("quizzes/{id}")
    suspend fun deleteQuiz(@Path("id") id: String)
}
