package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.CreateProfileRequest
import com.bagadbille.tdc.data.remote.dto.UpdateProfileRequest
import com.bagadbille.tdc.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ProfileApi {
    @GET("users/me")
    suspend fun getProfile(
        @retrofit2.http.Header("Authorization") authHeader: String? = null
    ): UserProfileDto

    @POST("users/profile")
    suspend fun createProfile(@Body request: CreateProfileRequest): UserProfileDto

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserProfileDto
}
