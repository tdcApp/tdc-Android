package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.NotificationDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String)
}
