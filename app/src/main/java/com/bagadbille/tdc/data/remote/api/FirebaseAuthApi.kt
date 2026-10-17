package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.FirebaseRefreshTokenResponse
import com.bagadbille.tdc.data.remote.dto.FirebaseSignInRequest
import com.bagadbille.tdc.data.remote.dto.FirebaseSignInResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface FirebaseAuthApi {
    @POST("v1/accounts:signInWithPassword")
    suspend fun signInWithPassword(
        @Query("key") apiKey: String,
        @Body request: FirebaseSignInRequest
    ): FirebaseSignInResponse

    @POST("v1/accounts:signUp")
    suspend fun signUp(
        @Query("key") apiKey: String,
        @Body request: FirebaseSignInRequest
    ): FirebaseSignInResponse
}

interface FirebaseTokenApi {
    @FormUrlEncoded
    @POST("v1/token")
    suspend fun refreshToken(
        @Query("key") apiKey: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): FirebaseRefreshTokenResponse
}
