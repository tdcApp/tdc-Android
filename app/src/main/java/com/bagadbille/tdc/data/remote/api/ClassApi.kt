package com.bagadbille.tdc.data.remote.api

import com.bagadbille.tdc.data.remote.dto.ClassInfoDto
import com.bagadbille.tdc.data.remote.dto.UserProfileDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ClassApi {
    @GET("classes")
    suspend fun getClasses(@Query("type") type: String? = null): List<ClassInfoDto>

    @GET("classes/{id}/students")
    suspend fun getStudentsByClass(@Path("id") classId: String): List<UserProfileDto>
}
