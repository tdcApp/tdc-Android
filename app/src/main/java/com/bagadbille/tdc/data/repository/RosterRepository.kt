package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.model.toDomain
import com.bagadbille.tdc.data.remote.api.ClassApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/** Students per batch. Used by mentor screens (submissions and Batches tab). */
interface RosterRepository {
    suspend fun getStudentsByClass(classId: String): Result<List<UserProfile>>
    suspend fun getClassStudentCounts(classIds: Collection<String>): Result<Map<String, Int>>
}

@Singleton
class RosterRepositoryImpl @Inject constructor(
    private val classApi: ClassApi,
    private val classRepo: dagger.Lazy<ClassRepository>
) : RosterRepository {

    override suspend fun getStudentsByClass(classId: String): Result<List<UserProfile>> = runCatching {
        try {
            classApi.getStudentsByClass(classId).map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getClassStudentCounts(classIds: Collection<String>): Result<Map<String, Int>> = runCatching {
        val allClasses = classRepo.get().getClasses().getOrDefault(emptyList())
        if (allClasses.isNotEmpty()) {
            val countMap = allClasses.associate { it.id to it.studentCount }
            classIds.distinct().associateWith { countMap[it] ?: 0 }
        } else {
            coroutineScope {
                classIds.distinct().map { id ->
                    async {
                        val count = try {
                            classApi.getStudentsByClass(id).size
                        } catch (_: Exception) {
                            0
                        }
                        id to count
                    }
                }.awaitAll().toMap()
            }
        }
    }
}
