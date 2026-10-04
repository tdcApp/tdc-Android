package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.ScheduleDto
import retrofit2.http.GET

interface ScheduleApi {
    @GET("schedules")
    suspend fun getSchedules(): List<ScheduleDto>
}
