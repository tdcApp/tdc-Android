package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.ClassInfo
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface ClassRepository {
    suspend fun getClasses(): Result<List<ClassInfo>>
}

@Singleton
class ClassRepositoryImpl @Inject constructor() : ClassRepository {
    override suspend fun getClasses(): Result<List<ClassInfo>> {
        delay(700)
        // TODO: Replace with GET /classes from NestJS backend
        return Result.success(listOf(
            ClassInfo("cls_001", "C++ Fundamentals", "language", "2026-06-01T00:00:00Z"),
            ClassInfo("cls_002", "Python DSA", "language", "2026-06-01T00:00:00Z"),
            ClassInfo("cls_003", "Web Development (React)", "technology", "2026-06-15T00:00:00Z"),
            ClassInfo("cls_004", "Android Dev (Kotlin + Jetpack)", "technology", "2026-07-01T00:00:00Z"),
            ClassInfo("cls_005", "Java OOP & Design Patterns", "language", "2026-07-01T00:00:00Z"),
        ))
    }
}
