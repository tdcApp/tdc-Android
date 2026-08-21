package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.AuthVerifyRequest
import com.bagadbille.tdc.data.remote.dto.AuthVerifyResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/verify")
    suspend fun verifyToken(@Body request: AuthVerifyRequest): AuthVerifyResponse
}
