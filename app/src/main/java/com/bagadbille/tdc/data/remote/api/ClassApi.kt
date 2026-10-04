package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.ClassInfoDto
import retrofit2.http.GET

interface ClassApi {
    @GET("classes")
    suspend fun getClasses(): List<ClassInfoDto>
}
