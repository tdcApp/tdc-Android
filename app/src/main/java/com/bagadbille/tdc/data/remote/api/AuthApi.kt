package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.AuthBootstrapResponse
import retrofit2.http.GET

interface AuthApi {
    @GET("auth/me")
    suspend fun getAuthBootstrap(
        @retrofit2.http.Header("Authorization") authHeader: String? = null
    ): AuthBootstrapResponse
}
