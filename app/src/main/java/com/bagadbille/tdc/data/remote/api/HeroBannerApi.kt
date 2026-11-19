package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.HeroBannerDto
import com.bagadbille.tdc.data.remote.dto.UpdateHeroBannerRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface HeroBannerApi {
    @GET("hero-banner")
    suspend fun getHeroBanner(): HeroBannerDto

    @PUT("hero-banner")
    suspend fun updateHeroBanner(@Body request: UpdateHeroBannerRequest): HeroBannerDto
}
