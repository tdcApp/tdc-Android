package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.ClassChangeRequestDto
import com.bagadbille.tdc.data.remote.dto.ClassInfoDto
import com.bagadbille.tdc.data.remote.dto.CreateClassChangeRequest
import com.bagadbille.tdc.data.remote.dto.CreateClassRequest
import com.bagadbille.tdc.data.remote.dto.ReviewClassChangeRequest
import com.bagadbille.tdc.data.remote.dto.TeacherUserDto
import com.bagadbille.tdc.data.remote.dto.UpdateClassRequest
import com.bagadbille.tdc.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClassApi {
    @GET("classes")
    suspend fun getClasses(@Query("type") type: String? = null): List<ClassInfoDto>

    @POST("classes")
    suspend fun createClass(@Body request: CreateClassRequest): ClassInfoDto

    @GET("classes/my")
    suspend fun getMyClasses(): List<ClassInfoDto>

    @GET("classes/{id}/students")
    suspend fun getStudentsByClass(@Path("id") classId: String): List<UserProfileDto>

    @PATCH("classes/{id}")
    suspend fun updateClass(
        @Path("id") id: String,
        @Body request: UpdateClassRequest
    ): ClassInfoDto

    @POST("classes/change-request")
    suspend fun createChangeRequest(@Body request: CreateClassChangeRequest): ClassChangeRequestDto

    @GET("classes/change-request/my")
    suspend fun getMyChangeRequest(): ClassChangeRequestDto?

    @GET("classes/change-requests")
    suspend fun getChangeRequests(@Query("status") status: String? = null): List<ClassChangeRequestDto>

    @PATCH("classes/change-requests/{id}/review")
    suspend fun reviewChangeRequest(
        @Path("id") id: String,
        @Body request: ReviewClassChangeRequest
    ): ClassChangeRequestDto

    @GET("users/teachers")
    suspend fun getTeachers(): List<TeacherUserDto>
}
