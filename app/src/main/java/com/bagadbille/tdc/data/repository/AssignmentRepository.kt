package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentAttachment
import com.bagadbille.tdc.data.model.AssignmentStatus
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface AssignmentRepository {
    suspend fun getAssignments(): Result<List<Assignment>>
    suspend fun getAssignmentById(id: String): Result<Assignment>
}

@Singleton
class AssignmentRepositoryImpl @Inject constructor() : AssignmentRepository {
    private val mock = listOf(
        Assignment(
            id = "asg_001",
            title = "Wave Optics Lab Report",
            subject = "Physics",
            description = "Write a detailed lab report on the double-slit experiment conducted in class. Include observations, calculations, and diagrams.",
            dueDate = "2026-09-10",
            status = AssignmentStatus.ONGOING,
            attachments = listOf(
                AssignmentAttachment("att_001", "lab_instructions.pdf", "https://example.com/lab_instructions.pdf")
            )
        ),
        Assignment(
            id = "asg_002",
            title = "Calculus Problem Set #5",
            subject = "Mathematics",
            description = "Complete problems 1–20 from Chapter 7: Integration Techniques. Show all working steps.",
            dueDate = "2026-09-08",
            status = AssignmentStatus.ONGOING,
            attachments = emptyList()
        ),
        Assignment(
            id = "asg_003",
            title = "Organic Chemistry Worksheet",
            subject = "Chemistry",
            description = "Answer all questions on the nomenclature and reactions of alkenes and alkynes.",
            dueDate = "2026-09-15",
            status = AssignmentStatus.ONGOING,
            attachments = listOf(
                AssignmentAttachment("att_002", "worksheet.pdf", "https://example.com/worksheet.pdf")
            )
        ),
        Assignment(
            id = "asg_004",
            title = "Data Structures Essay",
            subject = "Computer Science",
            description = "Write a 1500-word essay comparing the time complexities of common sorting algorithms.",
            dueDate = "2026-08-20",
            status = AssignmentStatus.PAST,
            attachments = listOf(
                AssignmentAttachment("att_003", "rubric.pdf", "https://example.com/rubric.pdf"),
                AssignmentAttachment("att_004", "my_submission.pdf", "https://example.com/my_submission.pdf")
            )
        ),
        Assignment(
            id = "asg_005",
            title = "English Literature Review",
            subject = "English",
            description = "Analyze the themes of identity and belonging in the prescribed novel. Submit a 2-page critical review.",
            dueDate = "2026-08-15",
            status = AssignmentStatus.PAST,
            attachments = listOf(
                AssignmentAttachment("att_005", "novel_excerpt.pdf", "https://example.com/novel_excerpt.pdf")
            )
        )
    )

    override suspend fun getAssignments(): Result<List<Assignment>> {
        delay(500)
        return Result.success(mock)
    }

    override suspend fun getAssignmentById(id: String): Result<Assignment> {
        delay(300)
        val assignment = mock.find { it.id == id }
        return if (assignment != null) Result.success(assignment)
        else Result.failure(Exception("Assignment not found"))
    }
}
