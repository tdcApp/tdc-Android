package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.AssignmentDto
import com.bagadbille.tdc.data.remote.dto.AssignmentSubmissionDto
import com.bagadbille.tdc.data.remote.dto.AssignmentSubmissionRequest
import com.bagadbille.tdc.data.remote.dto.CreateAssignmentRequest
import retrofit2.http.*

interface AssignmentApi {
    @GET("assignments")
    suspend fun getAssignments(): List<AssignmentDto>

    @GET("assignments/{id}")
    suspend fun getAssignment(@Path("id") id: String): AssignmentDto

    @POST("assignments")
    suspend fun createAssignment(@Body request: CreateAssignmentRequest): AssignmentDto

    @POST("assignments/{id}/submit")
    suspend fun submitAssignment(
        @Path("id") id: String,
        @Body request: AssignmentSubmissionRequest
    ): AssignmentSubmissionDto

    @GET("assignments/{id}/my-submission")
    suspend fun getMySubmission(@Path("id") id: String): AssignmentSubmissionDto

    @GET("assignments/{id}/submissions")
    suspend fun getAllSubmissions(@Path("id") id: String): List<AssignmentSubmissionDto>

    @DELETE("assignments/{id}")
    suspend fun deleteAssignment(@Path("id") id: String)
}
