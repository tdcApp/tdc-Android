package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun isLoggedIn(): Flow<Boolean>
    fun isProfileComplete(): Flow<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    // Requires the email too, so sessions saved before email persistence existed go back to Login.
    override fun isLoggedIn(): Flow<Boolean> =
        combine(dataStoreManager.authToken, dataStoreManager.userEmail) { token, email ->
            token != null && email != null
        }

    override fun isProfileComplete(): Flow<Boolean> = dataStoreManager.isProfileComplete

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            // TODO: Replace with POST /auth/login to NestJS backend
            delay(1000)
            val user = MockUsers.authenticate(email, password)
                ?: return Result.failure(IllegalArgumentException("Invalid email or password"))

            dataStoreManager.saveAuthToken("mock_jwt_token_${System.currentTimeMillis()}")
            dataStoreManager.saveUserEmail(user.email)
            dataStoreManager.saveUserRole(user.role)
            dataStoreManager.saveProfileComplete(user.isProfileComplete)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        dataStoreManager.clearSession()
    }
}
