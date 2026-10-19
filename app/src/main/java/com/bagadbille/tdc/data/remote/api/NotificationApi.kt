package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.NotificationDto
import com.bagadbille.tdc.data.remote.dto.RegisterFcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String)

    @POST("users/fcm-token")
    suspend fun registerFcmToken(@Body request: RegisterFcmTokenRequest)

    @DELETE("users/fcm-token")
    suspend fun unregisterFcmToken(@Query("token") token: String)
}
