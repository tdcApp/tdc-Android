package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

interface AuthRepository {
    fun isLoggedIn(): Flow<Boolean>
    fun isProfileComplete(): Flow<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    override fun isLoggedIn(): Flow<Boolean> =
        combine(dataStoreManager.authToken, dataStoreManager.userEmail) { token, email ->
            token != null && email != null
        }

    override fun isProfileComplete(): Flow<Boolean> = dataStoreManager.isProfileComplete

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        // 1. Try Firebase Authentication first if configured
        var firebaseUid: String? = null
        if (firebaseAuth != null) {
            try {
                val authResult = firebaseAuth!!.signInWithEmailAndPassword(cleanEmail, cleanPassword).awaitTask()
                firebaseUid = authResult.user?.uid
            } catch (e: Exception) {
                // If Firebase auth fails, check mock dev credentials before reporting failure
                val mockUser = MockUsers.authenticate(cleanEmail, cleanPassword)
                if (mockUser == null) {
                    return Result.failure(IllegalArgumentException("You're not a part of TDC"))
                }
                firebaseUid = mockUser.firebaseUid
            }
        } else {
            // Fallback for dev / offline
            val mockUser = MockUsers.authenticate(cleanEmail, cleanPassword)
                ?: return Result.failure(IllegalArgumentException("You're not a part of TDC"))
            firebaseUid = mockUser.firebaseUid
        }

        val uid = firebaseUid ?: return Result.failure(IllegalArgumentException("You're not a part of TDC"))

        // 2. Query Neon Database for user profile
        // TODO: Replace with GET /users/{uid} to NestJS / Neon backend
        val neonUser = MockUsers.findByFirebaseUid(uid) ?: MockUsers.find(cleanEmail)

        return if (neonUser != null && neonUser.isProfileComplete) {
            // Profile exists in Neon -> Load role and UI
            dataStoreManager.saveAuthToken("token_${System.currentTimeMillis()}")
            dataStoreManager.saveFirebaseUid(uid)
            dataStoreManager.saveUserEmail(cleanEmail)
            dataStoreManager.saveUserRole(neonUser.role)
            dataStoreManager.saveProfileComplete(true)
            Result.success(neonUser)
        } else {
            // Firebase user exists, but Neon profile is not yet completed -> send to Complete Profile
            dataStoreManager.saveAuthToken("token_${System.currentTimeMillis()}")
            dataStoreManager.saveFirebaseUid(uid)
            dataStoreManager.saveUserEmail(cleanEmail)
            dataStoreManager.saveUserRole("student") // Self-registered role defaults to student
            dataStoreManager.saveProfileComplete(false)
            val studentProfile = neonUser ?: MockUsers.register(cleanEmail, cleanPassword, uid)
            Result.success(studentProfile)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        var firebaseUid: String = "user_${System.currentTimeMillis()}"

        // 1. Register with Firebase Auth
        if (firebaseAuth != null) {
            try {
                val authResult = firebaseAuth!!.createUserWithEmailAndPassword(cleanEmail, cleanPassword).awaitTask()
                authResult.user?.uid?.let { firebaseUid = it }
            } catch (e: Exception) {
                // If email already exists or Firebase error, fail with clean message
                return Result.failure(IllegalArgumentException(e.localizedMessage ?: "Registration failed"))
            }
        }

        // 2. Newly registered users are ALWAYS students ("mentor's profile will be created by admin")
        val studentProfile = MockUsers.register(cleanEmail, cleanPassword, firebaseUid)

        dataStoreManager.saveAuthToken("token_${System.currentTimeMillis()}")
        dataStoreManager.saveFirebaseUid(firebaseUid)
        dataStoreManager.saveUserEmail(cleanEmail)
        dataStoreManager.saveUserRole("student")
        dataStoreManager.saveProfileComplete(false)

        return Result.success(studentProfile)
    }

    override suspend fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        dataStoreManager.clearSession()
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWith(Result.failure(it)) }
        }
}
