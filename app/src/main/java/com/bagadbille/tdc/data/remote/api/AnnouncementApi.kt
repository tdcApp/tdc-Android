package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.AnnouncementDto
import retrofit2.http.GET

interface AnnouncementApi {
    @GET("announcements")
    suspend fun getAnnouncements(): List<AnnouncementDto>
}
