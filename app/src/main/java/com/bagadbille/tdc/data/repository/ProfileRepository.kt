package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun setupProfile(
        name: String,
        languageClassId: String?,
        technologyClassId: String?,
        enrollmentNumber: String? = null,
        year: String? = null,
        mobileNumber: String? = null
    ): Result<UserProfile>
    suspend fun setupProfile(name: String, enrollmentNumber: String, year: String): Result<UserProfile>
    suspend fun updateProfile(name: String?, phone: String?): Result<UserProfile>
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ProfileRepository {

    private suspend fun currentEmail(): String =
        dataStoreManager.userEmail.first() ?: throw IllegalStateException("Not logged in")

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        delay(800)
        // TODO: Replace with GET /profile/me to NestJS backend
        val email = currentEmail()
        MockUsers.find(email) ?: MockUsers.register(email, MockUsers.DEV_PASSWORD)
    }

    override suspend fun setupProfile(
        name: String,
        languageClassId: String?,
        technologyClassId: String?,
        enrollmentNumber: String?,
        year: String?,
        mobileNumber: String?
    ): Result<UserProfile> = runCatching {
        delay(1000)
        // TODO: Replace with PATCH /profile/me/setup to NestJS backend
        val updated = MockUsers.update(currentEmail()) {
            it.copy(
                name = name,
                mobileNumber = mobileNumber ?: it.mobileNumber,
                languageClassId = languageClassId ?: it.languageClassId,
                technologyClassId = technologyClassId ?: it.technologyClassId,
                enrollmentNumber = enrollmentNumber ?: it.enrollmentNumber,
                year = year ?: it.year,
                role = "student", // Role is always student; mentors are created by admin
                isProfileComplete = true
            )
        }
        dataStoreManager.saveProfileComplete(true)
        dataStoreManager.saveUserRole("student")
        updated
    }

    override suspend fun setupProfile(name: String, enrollmentNumber: String, year: String): Result<UserProfile> =
        setupProfile(name, null, null, enrollmentNumber, year)

    override suspend fun updateProfile(name: String?, phone: String?): Result<UserProfile> = runCatching {
        delay(800)
        // TODO: Replace with PATCH /profile/me to NestJS backend
        MockUsers.update(currentEmail()) {
            it.copy(name = name ?: it.name, mobileNumber = phone ?: it.mobileNumber)
        }
    }
}
