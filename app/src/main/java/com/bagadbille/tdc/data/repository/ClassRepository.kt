package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.dao.ClassDao
import com.bagadbille.tdc.data.local.entity.ClassEntity
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.remote.api.ClassApi
import javax.inject.Inject
import javax.inject.Singleton

interface ClassRepository {
    suspend fun getClasses(): Result<List<ClassInfo>>
}

@Singleton
class ClassRepositoryImpl @Inject constructor(
    private val classApi: ClassApi,
    private val classDao: ClassDao
) : ClassRepository {

    private val fallbackClasses = listOf(
        ClassInfo("c1b0364c-771f-4ee7-b8ad-2046a66f9664", "C++", "language", "2026-10-07T07:42:11.866Z"),
        ClassInfo("fb84e2e9-d564-4a3b-a465-433d7e7e9e2d", "Python", "language", "2026-10-07T07:42:11.866Z"),
        ClassInfo("9c6fa0d5-6f43-4a7a-98e4-19c341e1ee5a", "Java", "language", "2026-10-07T07:42:11.866Z"),
        ClassInfo("81d6be69-ba46-4661-83f0-287aec5de95b", "AI/ML", "technology", "2026-10-07T07:42:11.866Z"),
        ClassInfo("c3cad44a-ab83-4353-9580-09f5073af910", "Web Development", "technology", "2026-10-07T07:42:11.866Z"),
        ClassInfo("6d9540e6-8d48-4fbe-8469-5928b3a0703f", "App Development", "technology", "2026-10-07T07:42:11.866Z"),
        ClassInfo("ddc2c3b8-4764-46c0-bb45-3424ba7a1397", "Cyber Security", "technology", "2026-10-07T07:42:11.866Z")
    )

    override suspend fun getClasses(): Result<List<ClassInfo>> = runCatching {
        try {
            val remoteClasses = classApi.getClasses()
            if (remoteClasses.isNotEmpty()) {
                val mapped = remoteClasses.map { dto ->
                    ClassInfo(
                        id = dto.id,
                        name = dto.name,
                        type = dto.type.lowercase(),
                        createdAt = dto.createdAt ?: dto.createdAtSnake ?: ""
                    )
                }
                classDao.replaceClasses(mapped.map { ClassEntity.fromDomain(it) })
                mapped
            } else {
                val cached = classDao.getAllClasses()
                if (cached.isNotEmpty()) cached.map { it.toDomain() } else fallbackClasses
            }
        } catch (_: Exception) {
            val cached = classDao.getAllClasses()
            if (cached.isNotEmpty()) cached.map { it.toDomain() } else fallbackClasses
        }
    }
}
