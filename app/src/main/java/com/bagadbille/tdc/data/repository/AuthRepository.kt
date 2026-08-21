package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun isLoggedIn(): Flow<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<UserProfile>
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override fun isLoggedIn(): Flow<Boolean> = dataStoreManager.authToken.map { it != null }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> = try {
        // TODO: Replace with Firebase Auth + POST /auth/verify token exchange
        delay(1500)
        dataStoreManager.saveAuthToken("mock_jwt_token_${System.currentTimeMillis()}")
        Result.success(UserProfile("user_001", "Test User", email, null, null, "Class 10", "A"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<UserProfile> = try {
        delay(1500)
        dataStoreManager.saveAuthToken("mock_jwt_token_${System.currentTimeMillis()}")
        Result.success(UserProfile("user_002", name, email))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun signInWithGoogle(): Result<UserProfile> =
        Result.failure(NotImplementedError("Google Sign-In not yet implemented"))

    override suspend fun logout() { dataStoreManager.clearSession() }
}
