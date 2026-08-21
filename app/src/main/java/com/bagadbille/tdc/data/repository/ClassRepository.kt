package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface ClassRepository {
    suspend fun getClasses(): Result<List<ClassInfo>>
    suspend fun getClassDetail(id: String): Result<ClassDetail>
}

@Singleton
class ClassRepositoryImpl @Inject constructor() : ClassRepository {
    private val mockClasses = listOf(
        ClassInfo("cls_001", "Physics", "Physics", "Dr. Sharma", "Mon, Wed, Fri - 9:00 AM", "Room 301", "Advanced Physics - Wave Optics & Modern Physics"),
        ClassInfo("cls_002", "Mathematics", "Mathematics", "Mrs. Gupta", "Tue, Thu - 10:00 AM", "Room 205", "Calculus & Linear Algebra"),
        ClassInfo("cls_003", "Chemistry", "Chemistry", "Mr. Patel", "Mon, Wed - 11:00 AM", "Lab 102", "Organic Chemistry & Spectroscopy"),
        ClassInfo("cls_004", "English", "English", "Ms. Reddy", "Tue, Thu, Sat - 8:00 AM", "Room 104", "Literature & Creative Writing"),
        ClassInfo("cls_005", "Computer Science", "CS", "Mr. Kumar", "Fri - 2:00 PM", "CS Lab", "Data Structures & Algorithms"),
    )

    override suspend fun getClasses(): Result<List<ClassInfo>> { delay(700); return Result.success(mockClasses) }

    override suspend fun getClassDetail(id: String): Result<ClassDetail> {
        delay(500)
        val cls = mockClasses.find { it.id == id } ?: return Result.failure(Exception("Class not found"))
        return Result.success(ClassDetail(cls, listOf(
            ClassMaterial("mat_001", "Chapter 5 Notes", "document", "https://example.com/notes.pdf", "2026-08-15"),
            ClassMaterial("mat_002", "Lecture Recording", "video", "https://example.com/video.mp4", "2026-08-14"),
            ClassMaterial("mat_003", "Practice Problems Set", "document", "https://example.com/problems.pdf", "2026-08-13"),
        )))
    }
}
