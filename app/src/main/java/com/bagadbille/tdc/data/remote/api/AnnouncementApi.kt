package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.AnnouncementDto
import com.bagadbille.tdc.data.remote.dto.CreateAnnouncementRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AnnouncementApi {
    @GET("announcements")
    suspend fun getAnnouncements(): List<AnnouncementDto>

    @POST("announcements")
    suspend fun createAnnouncement(@Body request: CreateAnnouncementRequest): AnnouncementDto

    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(@Path("id") id: String)
}
