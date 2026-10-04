package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** Students per batch. Used by mentor screens (submissions now, Batches tab later). */
interface RosterRepository {
    suspend fun getStudentsByClass(classId: String): Result<List<UserProfile>>
    suspend fun getClassStudentCounts(classIds: Collection<String>): Result<Map<String, Int>>
}

@Singleton
class RosterRepositoryImpl @Inject constructor() : RosterRepository {

    override suspend fun getStudentsByClass(classId: String): Result<List<UserProfile>> = runCatching {
        delay(300)
        // TODO: Replace with GET /classes/{id}/students to NestJS backend
        MockUsers.studentsInClass(classId)
    }

    override suspend fun getClassStudentCounts(classIds: Collection<String>): Result<Map<String, Int>> = runCatching {
        delay(200)
        // TODO: Replace with GET /classes/student-counts to NestJS backend
        classIds.distinct().associateWith { MockUsers.studentsInClass(it).size }
    }
}
