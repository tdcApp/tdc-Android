package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.dao.AssignmentDao
import com.bagadbille.tdc.data.local.entity.AssignmentEntity
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.remote.api.AssignmentApi
import com.bagadbille.tdc.data.remote.dto.AssignmentSubmissionRequest
import com.bagadbille.tdc.data.remote.dto.CreateAssignmentRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

interface AssignmentRepository {
    suspend fun getAssignments(): Result<List<Assignment>>
    suspend fun getAssignmentById(id: String): Result<Assignment>

    // ─── Student ───
    suspend fun submitAssignment(assignmentId: String, submissionUrl: String): Result<AssignmentSubmission>
    /** The current student's submission for one assignment. */
    suspend fun getSubmission(assignmentId: String): Result<AssignmentSubmission?>
    /** The current student's submissions, keyed by assignment id. */
    suspend fun getSubmissions(): Result<Map<String, AssignmentSubmission>>

    // ─── Mentor ───
    /** Assignments created by the current mentor. */
    suspend fun getMentorAssignments(): Result<List<Assignment>>
    suspend fun createAssignment(title: String, description: String?, classId: String, dueAt: String): Result<Assignment>
    suspend fun updateAssignment(assignment: Assignment): Result<Assignment>
    suspend fun deleteAssignment(id: String): Result<Unit>
    /** Every student's submission for one assignment. */
    suspend fun getSubmissionsForAssignment(assignmentId: String): Result<List<AssignmentSubmission>>
    /** Every submission across all assignments (used for counts). */
    suspend fun getAllSubmissions(): Result<List<AssignmentSubmission>>
}

@Singleton
class AssignmentRepositoryImpl @Inject constructor(
    private val assignmentApi: AssignmentApi,
    private val assignmentDao: AssignmentDao
) : AssignmentRepository {

    override suspend fun getAssignments(): Result<List<Assignment>> = runCatching {
        try {
            val remote = assignmentApi.getAssignments().map { it.toDomain() }
            if (remote.isNotEmpty()) {
                assignmentDao.replaceAssignments(remote.map { AssignmentEntity.fromDomain(it) })
            }
            val cached = assignmentDao.getAllAssignments().map { it.toDomain() }
            (remote + cached).distinctBy { it.id }
        } catch (_: Exception) {
            val cached = assignmentDao.getAllAssignments()
            cached.map { it.toDomain() }
        }
    }

    override suspend fun getAssignmentById(id: String): Result<Assignment> = runCatching {
        val cached = assignmentDao.getAssignmentById(id)?.toDomain()
        if (cached != null) {
            cached
        } else {
            val remote = assignmentApi.getAssignment(id).toDomain()
            assignmentDao.insertAssignment(AssignmentEntity.fromDomain(remote))
            remote
        }
    }

    // ─── Student ───

    override suspend fun submitAssignment(
        assignmentId: String,
        submissionUrl: String
    ): Result<AssignmentSubmission> = runCatching {
        val req = AssignmentSubmissionRequest(submissionUrl = submissionUrl.trim())
        assignmentApi.submitAssignment(assignmentId, req).toDomain()
    }

    override suspend fun getSubmission(assignmentId: String): Result<AssignmentSubmission?> = runCatching {
        try {
            assignmentApi.getMySubmission(assignmentId).toDomain()
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
    }

    override suspend fun getSubmissions(): Result<Map<String, AssignmentSubmission>> = runCatching {
        val assignments = getAssignments().getOrDefault(emptyList())
        assignments.filter { it.isSubmitted }.associate { a ->
            a.id to AssignmentSubmission(
                assignmentId = a.id,
                menteeUid = "",
                submissionUrl = a.submissionUrl ?: "",
                submittedAt = a.submittedAt ?: ""
            )
        }
    }

    // ─── Mentor ───

    override suspend fun getMentorAssignments(): Result<List<Assignment>> = runCatching {
        getAssignments().getOrDefault(emptyList())
    }

    override suspend fun createAssignment(
        title: String,
        description: String?,
        classId: String,
        dueAt: String
    ): Result<Assignment> = runCatching {
        val req = CreateAssignmentRequest(
            title = title.trim(),
            description = description?.trim()?.ifBlank { null },
            classId = classId.trim(),
            dueAt = dueAt.trim()
        )
        val created = assignmentApi.createAssignment(req).toDomain()
        assignmentDao.insertAssignment(AssignmentEntity.fromDomain(created))
        created
    }

    override suspend fun updateAssignment(assignment: Assignment): Result<Assignment> = runCatching {
        assignmentDao.insertAssignment(AssignmentEntity.fromDomain(assignment))
        assignment
    }

    override suspend fun deleteAssignment(id: String): Result<Unit> = runCatching {
        assignmentDao.deleteAssignment(id)
        assignmentApi.deleteAssignment(id)
    }

    override suspend fun getSubmissionsForAssignment(assignmentId: String): Result<List<AssignmentSubmission>> = runCatching {
        try {
            assignmentApi.getAllSubmissions(assignmentId).map { it.toDomain() }
        } catch (e: HttpException) {
            if (e.code() == 404) emptyList() else throw e
        }
    }

    override suspend fun getAllSubmissions(): Result<List<AssignmentSubmission>> = runCatching {
        val mentorAssignments = try {
            assignmentApi.getAssignments()
        } catch (_: Exception) {
            emptyList()
        }
        val all = mutableListOf<AssignmentSubmission>()
        for (a in mentorAssignments) {
            try {
                all.addAll(assignmentApi.getAllSubmissions(a.id).map { it.toDomain() })
            } catch (_: Exception) {}
        }
        all
    }
}
