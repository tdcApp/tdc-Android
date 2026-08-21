package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.ClassDetailDto
import com.bagadbille.tdc.data.remote.dto.ClassInfoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ClassApi {
    @GET("classes")
    suspend fun getClasses(): List<ClassInfoDto>

    @GET("classes/{id}")
    suspend fun getClassDetail(@Path("id") id: String): ClassDetailDto
}
