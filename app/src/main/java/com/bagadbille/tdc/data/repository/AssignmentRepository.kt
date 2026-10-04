package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.Instant
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
    private val dataStoreManager: DataStoreManager
) : AssignmentRepository {
    // TODO: Replace all mock data with NestJS API calls
    private val assignments = mutableListOf(
        Assignment(
            id = "asg_001",
            title = "Build a Linked List in C++",
            description = "Implement a singly linked list with insert, delete, search, and reverse operations. Ensure proper memory management and handle edge cases. Push your code to GitHub and submit the repository link.",
            dueAt = "2026-10-15T23:59:00Z",
            mentorUid = "mentor_001",
            classId = "cls_001",
            createdAt = "2026-09-28T09:00:00Z"
        ),
        Assignment(
            id = "asg_002",
            title = "Python Sorting Visualizer",
            description = "Create a visualizer that demonstrates Bubble Sort, Merge Sort, and Quick Sort step-by-step. You can use Pygame, Tkinter, or terminal curses. Submit your GitHub repo with documentation.",
            dueAt = "2026-10-10T23:59:00Z",
            mentorUid = "mentor_001",
            classId = "cls_002",
            createdAt = "2026-09-25T10:00:00Z"
        ),
        Assignment(
            id = "asg_003",
            title = "React Task Management Dashboard",
            description = "Build a responsive task board using React, TypeScript, and Tailwind CSS / styled-components. Support drag-and-drop or categorized status columns. Deploy on Vercel or Netlify and submit the live link.",
            dueAt = "2026-10-25T23:59:00Z",
            mentorUid = "mentor_001",
            classId = "cls_003",
            createdAt = "2026-10-01T09:00:00Z"
        ),
        Assignment(
            id = "asg_004",
            title = "LeetCode Challenge - 5 Array Problems",
            description = "Solve 5 medium-difficulty array and two-pointer problems on LeetCode. Submit your GitHub repository containing the clean solutions with time and space complexity analysis.",
            dueAt = "2026-09-20T23:59:00Z",
            mentorUid = "mentor_001",
            classId = "cls_002",
            createdAt = "2026-09-01T09:00:00Z"
        ),
        Assignment(
            id = "asg_005",
            title = "Kotlin Jetpack Compose Calculator",
            description = "Build a polished calculator app in Android Studio using Jetpack Compose and Material 3 design system. Support basic arithmetic operations and landscape orientation.",
            dueAt = "2026-09-15T23:59:00Z",
            mentorUid = "mentor_001",
            classId = "cls_004",
            createdAt = "2026-08-25T09:00:00Z"
        ),
    )

    private val submissions = mutableListOf(
        AssignmentSubmission("asg_004", "user_001", "https://github.com/tdc-student/leetcode-solutions-5", "2026-09-19T14:30:00Z"),
        AssignmentSubmission("asg_004", "user_005", "https://github.com/sneha-g/leetcode-arrays", "2026-09-20T10:00:00Z"),
        AssignmentSubmission("asg_004", "user_006", "https://github.com/rahulv/lc-5", "2026-09-21T08:15:00Z"),
        AssignmentSubmission("asg_005", "user_004", "https://github.com/arjunm/compose-calc", "2026-09-14T18:00:00Z"),
        AssignmentSubmission("asg_005", "user_008", "https://github.com/karanj/calculator", "2026-09-16T11:20:00Z"),
        AssignmentSubmission("asg_002", "user_005", "https://github.com/sneha-g/sort-visualizer", "2026-10-03T16:45:00Z"),
        AssignmentSubmission("asg_001", "user_003", "https://github.com/riyap/cpp-linked-list", "2026-10-02T12:10:00Z"),
    )

    private suspend fun currentUserId(): String {
        val email = dataStoreManager.userEmail.first() ?: throw IllegalStateException("Not logged in")
        return MockUsers.find(email)?.id ?: throw IllegalStateException("User not found")
    }

    override suspend fun getAssignments(): Result<List<Assignment>> {
        delay(400)
        return Result.success(assignments.toList())
    }

    override suspend fun getAssignmentById(id: String): Result<Assignment> {
        delay(250)
        val assignment = assignments.find { it.id == id }
        return if (assignment != null) Result.success(assignment)
        else Result.failure(Exception("Assignment not found"))
    }

    // ─── Student ───

    override suspend fun submitAssignment(assignmentId: String, submissionUrl: String): Result<AssignmentSubmission> = runCatching {
        delay(700)
        // TODO: Replace with POST /assignments/{id}/submit to NestJS backend
        val userId = currentUserId()
        val submission = AssignmentSubmission(assignmentId, userId, submissionUrl, Instant.now().toString())
        submissions.removeAll { it.assignmentId == assignmentId && it.menteeUid == userId }
        submissions.add(submission)
        submission
    }

    override suspend fun getSubmission(assignmentId: String): Result<AssignmentSubmission?> = runCatching {
        delay(200)
        val userId = currentUserId()
        submissions.find { it.assignmentId == assignmentId && it.menteeUid == userId }
    }

    override suspend fun getSubmissions(): Result<Map<String, AssignmentSubmission>> = runCatching {
        delay(200)
        val userId = currentUserId()
        submissions.filter { it.menteeUid == userId }.associateBy { it.assignmentId }
    }

    // ─── Mentor ───

    override suspend fun getMentorAssignments(): Result<List<Assignment>> = runCatching {
        delay(400)
        // TODO: Replace with GET /assignments?mentor=me to NestJS backend
        val userId = currentUserId()
        assignments.filter { it.mentorUid == userId }
    }

    override suspend fun createAssignment(
        title: String, description: String?, classId: String, dueAt: String
    ): Result<Assignment> = runCatching {
        delay(600)
        // TODO: Replace with POST /assignments to NestJS backend
        val now = Instant.now().toString()
        val assignment = Assignment(
            id = "asg_${System.currentTimeMillis()}",
            title = title,
            description = description,
            dueAt = dueAt,
            mentorUid = currentUserId(),
            classId = classId,
            createdAt = now,
            updatedAt = now
        )
        assignments.add(assignment)
        assignment
    }

    override suspend fun updateAssignment(assignment: Assignment): Result<Assignment> = runCatching {
        delay(600)
        // TODO: Replace with PATCH /assignments/{id} to NestJS backend
        val index = assignments.indexOfFirst { it.id == assignment.id }
        if (index == -1) throw IllegalArgumentException("Assignment not found")
        val updated = assignment.copy(updatedAt = Instant.now().toString())
        assignments[index] = updated
        updated
    }

    override suspend fun deleteAssignment(id: String): Result<Unit> = runCatching {
        delay(500)
        // TODO: Replace with DELETE /assignments/{id} to NestJS backend
        if (!assignments.removeAll { it.id == id }) throw IllegalArgumentException("Assignment not found")
        submissions.removeAll { it.assignmentId == id }
    }

    override suspend fun getSubmissionsForAssignment(assignmentId: String): Result<List<AssignmentSubmission>> = runCatching {
        delay(300)
        // TODO: Replace with GET /assignments/{id}/submissions to NestJS backend
        submissions.filter { it.assignmentId == assignmentId }
    }

    override suspend fun getAllSubmissions(): Result<List<AssignmentSubmission>> = runCatching {
        delay(200)
        submissions.toList()
    }
}
