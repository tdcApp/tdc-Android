package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.UpdateProfileRequest
import com.bagadbille.tdc.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApi {
    @GET("profile/me")
    suspend fun getProfile(): UserProfileDto

    @PATCH("profile/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserProfileDto
}
