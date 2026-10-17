package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.CreateScheduleRequest
import com.bagadbille.tdc.data.remote.dto.ScheduleDto
import com.bagadbille.tdc.data.remote.dto.UpdateScheduleRequest
import retrofit2.http.*

interface ScheduleApi {
    @GET("schedules/me")
    suspend fun getSchedules(): List<ScheduleDto>

    @GET("schedules/class/{classId}")
    suspend fun getClassSchedules(@Path("classId") classId: String): List<ScheduleDto>

    @POST("schedules")
    suspend fun createSchedule(@Body request: CreateScheduleRequest): ScheduleDto

    @PATCH("schedules/{id}")
    suspend fun updateSchedule(
        @Path("id") id: String,
        @Body request: UpdateScheduleRequest
    ): ScheduleDto

    @DELETE("schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: String)
}
