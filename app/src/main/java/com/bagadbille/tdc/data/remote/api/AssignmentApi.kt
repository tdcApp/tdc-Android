package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.AssignmentDto
import com.bagadbille.tdc.data.remote.dto.AssignmentSubmissionDto
import com.bagadbille.tdc.data.remote.dto.AssignmentSubmissionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AssignmentApi {
    @GET("assignments")
    suspend fun getAssignments(): List<AssignmentDto>

    @GET("assignments/{id}")
    suspend fun getAssignment(@Path("id") id: String): AssignmentDto

    @POST("assignments/{id}/submit")
    suspend fun submitAssignment(
        @Path("id") id: String,
        @Body request: AssignmentSubmissionRequest
    ): AssignmentSubmissionDto

    @GET("assignments/{id}/submission")
    suspend fun getSubmission(@Path("id") id: String): AssignmentSubmissionDto?
}
