package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.local.dao.ClassDao
import com.bagadbille.tdc.data.local.entity.ClassEntity
import com.bagadbille.tdc.data.model.ClassChangeRequest
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.remote.api.ClassApi
import com.bagadbille.tdc.data.remote.dto.CreateClassChangeRequest
import com.bagadbille.tdc.data.remote.dto.CreateClassRequest
import com.bagadbille.tdc.data.remote.dto.ReviewClassChangeRequest
import com.bagadbille.tdc.data.remote.dto.TeacherUserDto
import com.bagadbille.tdc.data.remote.dto.UpdateClassRequest
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

interface ClassRepository {
    suspend fun getClasses(): Result<List<ClassInfo>>
    suspend fun getMyClasses(): Result<List<ClassInfo>>
    suspend fun updateClass(id: String, name: String?, type: String?, mentorUid: String?): Result<ClassInfo>
    suspend fun createClass(name: String, type: String, mentorUid: String): Result<ClassInfo>
    suspend fun getTeachers(): Result<List<TeacherUserDto>>
    suspend fun submitChangeRequest(
        requestedLanguageClassId: String?,
        requestedTechnologyClassId: String?,
        reason: String?
    ): Result<ClassChangeRequest>
    suspend fun getMyChangeRequest(): Result<ClassChangeRequest?>
    suspend fun getChangeRequests(status: String? = null): Result<List<ClassChangeRequest>>
    suspend fun reviewChangeRequest(id: String, approved: Boolean): Result<ClassChangeRequest>
}

@Singleton
class ClassRepositoryImpl @Inject constructor(
    private val classApi: ClassApi,
    private val classDao: ClassDao,
    private val dataStoreManager: DataStoreManager
) : ClassRepository {

    private val fallbackClasses = listOf(
        ClassInfo("c1b0364c-771f-4ee7-b8ad-2046a66f9664", "C++", "language", createdAt = "2026-10-07T07:42:11.866Z"),
        ClassInfo("fb84e2e9-d564-4a3b-a465-433d7e7e9e2d", "Python", "language", createdAt = "2026-10-07T07:42:11.866Z"),
        ClassInfo("9c6fa0d5-6f43-4a7a-98e4-19c341e1ee5a", "Java", "language", createdAt = "2026-10-07T07:42:11.866Z"),
        ClassInfo("81d6be69-ba46-4661-83f0-287aec5de95b", "AI/ML", "technology", createdAt = "2026-10-07T07:42:11.866Z"),
        ClassInfo("c3cad44a-ab83-4353-9580-09f5073af910", "Web Development", "technology", createdAt = "2026-10-07T07:42:11.866Z"),
        ClassInfo("6d9540e6-8d48-4fbe-8469-5928b3a0703f", "App Development", "technology", createdAt = "2026-10-07T07:42:11.866Z"),
        ClassInfo("ddc2c3b8-4764-46c0-bb45-3424ba7a1397", "Cyber Security", "technology", createdAt = "2026-10-07T07:42:11.866Z")
    )

    override suspend fun getClasses(): Result<List<ClassInfo>> = runCatching {
        try {
            val remoteClasses = classApi.getClasses()
            if (remoteClasses.isNotEmpty()) {
                val mapped = remoteClasses.map { it.toDomain() }
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

    override suspend fun getMyClasses(): Result<List<ClassInfo>> = runCatching {
        val role = dataStoreManager.userRole.firstOrNull()?.lowercase()
        val langId = dataStoreManager.languageClassId.firstOrNull()
        val techId = dataStoreManager.technologyClassId.firstOrNull()

        try {
            val remote = classApi.getMyClasses().map { it.toDomain() }
            if (remote.isNotEmpty()) {
                remote
            } else if (role == "student" && (!langId.isNullOrBlank() || !techId.isNullOrBlank())) {
                val allClasses = getClasses().getOrDefault(emptyList())
                allClasses.filter { it.id == langId || it.id == techId }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            val allClasses = getClasses().getOrDefault(emptyList())
            if (role == "student" && (!langId.isNullOrBlank() || !techId.isNullOrBlank())) {
                allClasses.filter { it.id == langId || it.id == techId }
            } else if (role == "teacher" || role == "admin") {
                allClasses
            } else {
                allClasses.filter { it.id == langId || it.id == techId }
            }
        }
    }

    override suspend fun updateClass(
        id: String,
        name: String?,
        type: String?,
        mentorUid: String?
    ): Result<ClassInfo> = runCatching {
        val dto = classApi.updateClass(
            id = id,
            request = UpdateClassRequest(
                name = name?.trim(),
                type = type?.uppercase(),
                mentorUid = mentorUid?.trim()
            )
        )
        val domain = dto.toDomain()
        classDao.insertClasses(listOf(ClassEntity.fromDomain(domain)))
        domain
    }

    override suspend fun createClass(
        name: String,
        type: String,
        mentorUid: String
    ): Result<ClassInfo> = runCatching {
        val dto = classApi.createClass(
            CreateClassRequest(
                name = name.trim(),
                type = type.uppercase(),
                mentorUid = mentorUid.trim()
            )
        )
        val domain = dto.toDomain()
        classDao.insertClasses(listOf(ClassEntity.fromDomain(domain)))
        domain
    }

    override suspend fun getTeachers(): Result<List<TeacherUserDto>> = runCatching {
        classApi.getTeachers()
    }

    override suspend fun submitChangeRequest(
        requestedLanguageClassId: String?,
        requestedTechnologyClassId: String?,
        reason: String?
    ): Result<ClassChangeRequest> = runCatching {
        val dto = classApi.createChangeRequest(
            CreateClassChangeRequest(
                requestedLanguageClassId = requestedLanguageClassId,
                requestedTechnologyClassId = requestedTechnologyClassId,
                reason = reason?.trim()?.ifBlank { null }
            )
        )
        dto.toDomain()
    }

    override suspend fun getMyChangeRequest(): Result<ClassChangeRequest?> = runCatching {
        classApi.getMyChangeRequest()?.toDomain()
    }

    override suspend fun getChangeRequests(status: String?): Result<List<ClassChangeRequest>> = runCatching {
        classApi.getChangeRequests(status).map { it.toDomain() }
    }

    override suspend fun reviewChangeRequest(id: String, approved: Boolean): Result<ClassChangeRequest> = runCatching {
        val status = if (approved) "APPROVED" else "REJECTED"
        val dto = classApi.reviewChangeRequest(id, ReviewClassChangeRequest(status))
        dto.toDomain()
    }
}
