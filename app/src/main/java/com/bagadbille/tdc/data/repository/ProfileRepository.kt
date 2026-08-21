package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(name: String?, phone: String?, classInfo: String?, section: String?, avatarUrl: String?): Result<UserProfile>
}

@Singleton
class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {
    private var mockProfile = UserProfile("user_001", "Akash", "akash@example.com", null, "+91 9876543210", "Class 12", "A", "2026-01-15T10:30:00Z")

    override suspend fun getProfile(): Result<UserProfile> { delay(800); return Result.success(mockProfile) }

    override suspend fun updateProfile(name: String?, phone: String?, classInfo: String?, section: String?, avatarUrl: String?): Result<UserProfile> {
        delay(800)
        mockProfile = mockProfile.copy(
            name = name ?: mockProfile.name, phone = phone ?: mockProfile.phone,
            classInfo = classInfo ?: mockProfile.classInfo, section = section ?: mockProfile.section,
            avatarUrl = avatarUrl ?: mockProfile.avatarUrl
        )
        return Result.success(mockProfile)
    }
}
