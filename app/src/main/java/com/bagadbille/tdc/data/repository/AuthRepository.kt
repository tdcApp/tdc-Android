package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.BuildConfig
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.model.isMentorRole
import com.bagadbille.tdc.data.model.toDomain
import com.bagadbille.tdc.data.remote.api.AuthApi
import com.bagadbille.tdc.data.remote.api.FirebaseAuthApi
import com.bagadbille.tdc.data.remote.api.ProfileApi
import com.bagadbille.tdc.data.remote.dto.FirebaseSignInRequest
import com.bagadbille.tdc.data.remote.dto.UserProfileDto
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import com.bagadbille.tdc.data.remote.api.NotificationApi
import com.bagadbille.tdc.data.remote.dto.RegisterFcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

interface AuthRepository {
    fun isLoggedIn(): Flow<Boolean>
    fun isProfileComplete(): Flow<Boolean>
    fun getUserRole(): Flow<String?>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val firebaseAuthApi: FirebaseAuthApi,
    private val authApi: AuthApi,
    private val profileApi: ProfileApi,
    private val notificationApi: NotificationApi
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
            !token.isNullOrBlank() && !email.isNullOrBlank()
        }

    override fun isProfileComplete(): Flow<Boolean> = dataStoreManager.isProfileComplete

    override fun getUserRole(): Flow<String?> = dataStoreManager.userRole

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        var firebaseUid: String? = null
        var freshIdToken: String? = null
        var refreshToken: String? = null

        // 1. Generate fresh token from Firebase via Google Identity Toolkit
        try {
            val response = firebaseAuthApi.signInWithPassword(
                apiKey = BuildConfig.FIREBASE_API_KEY,
                request = FirebaseSignInRequest(
                    email = cleanEmail,
                    password = cleanPassword,
                    returnSecureToken = true
                )
            )
            freshIdToken = response.idToken
            firebaseUid = response.localId
            refreshToken = response.refreshToken
        } catch (e: Exception) {
            // If REST call throws, try Firebase SDK as secondary
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth!!.signInWithEmailAndPassword(cleanEmail, cleanPassword).awaitTask()
                    firebaseUid = authResult.user?.uid
                    val tokenResult = authResult.user?.getIdToken(true)?.awaitTask()
                    freshIdToken = tokenResult?.token
                } catch (sdkError: Exception) {
                    return Result.failure(IllegalArgumentException(parseAuthError(sdkError)))
                }
            } else {
                return Result.failure(IllegalArgumentException(parseAuthError(e)))
            }
        }

        val uid = firebaseUid ?: return Result.failure(IllegalArgumentException("Authentication failed"))
        val token = freshIdToken ?: return Result.failure(IllegalArgumentException("Failed to acquire auth token"))

        // Persist fresh tokens immediately so OkHttp Interceptor sends Authorization: Bearer <freshIdToken>
        dataStoreManager.saveAuthToken(token)
        dataStoreManager.saveFirebaseUid(uid)
        dataStoreManager.saveUserEmail(cleanEmail)
        refreshToken?.let { dataStoreManager.saveRefreshToken(it) }

        // Also sync Firebase Auth SDK state if available
        try {
            firebaseAuth?.signInWithEmailAndPassword(cleanEmail, cleanPassword)
        } catch (_: Exception) {}

        // 2. Fetch profile from NestJS backend & Neon PostgreSQL database
        val bearerToken = "Bearer $token"
        var profileDto: UserProfileDto? = null
        var profileFound = false

        try {
            val authBootstrap = authApi.getAuthBootstrap(bearerToken)
            if (authBootstrap.profileExists && authBootstrap.profile != null) {
                profileDto = authBootstrap.profile
                profileFound = true
            }
        } catch (e: Exception) {
            // Fallback to /users/me
            try {
                profileDto = profileApi.getProfile(bearerToken)
                profileFound = true
            } catch (fallbackError: Exception) {
                val isNotFound = (e is HttpException && e.code() == 404) ||
                        (fallbackError is HttpException && fallbackError.code() == 404)
                if (!isNotFound) {
                    return Result.failure(Exception("Cannot connect to TDC backend: ${e.message ?: fallbackError.message}"))
                }
                profileFound = false
            }
        }

        if (profileFound && profileDto != null) {
            val role = profileDto.role.lowercase()
            val isMentor = isMentorRole(role)
            // Mentors/Teachers/Admins NEVER enroll in student class tracks; their profile is always complete
            val isComplete = isMentor || (!profileDto.name.isNullOrBlank() && !profileDto.effectiveLanguageClassId.isNullOrBlank())

            dataStoreManager.saveProfileComplete(isComplete)
            dataStoreManager.saveUserRole(role)
            profileDto.name?.let { dataStoreManager.saveUserName(it) }
            dataStoreManager.saveMobileNumber(profileDto.effectiveMobile)
            profileDto.effectiveLanguageClassId?.let { dataStoreManager.saveLanguageClassId(it) }
            profileDto.effectiveTechnologyClassId?.let { dataStoreManager.saveTechnologyClassId(it) }

            // Register device FCM token with backend immediately
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.awaitTask()
                if (!fcmToken.isNullOrBlank()) {
                    notificationApi.registerFcmToken(RegisterFcmTokenRequest(token = fcmToken))
                }
            } catch (_: Exception) {}

            return Result.success(profileDto.toDomain(fallbackUid = uid, fallbackEmail = cleanEmail, fallbackComplete = isComplete))
        } else {
            // New user in Firebase, but does not exist in Neon DB yet -> needs student onboarding
            dataStoreManager.saveProfileComplete(false)
            dataStoreManager.saveUserRole("student")
            return Result.success(
                UserProfile(
                    id = uid,
                    email = cleanEmail,
                    role = "student",
                    isProfileComplete = false
                )
            )
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        var firebaseUid: String? = null
        var freshIdToken: String? = null
        var refreshToken: String? = null

        try {
            val response = firebaseAuthApi.signUp(
                apiKey = BuildConfig.FIREBASE_API_KEY,
                request = FirebaseSignInRequest(
                    email = cleanEmail,
                    password = cleanPassword,
                    returnSecureToken = true
                )
            )
            freshIdToken = response.idToken
            firebaseUid = response.localId
            refreshToken = response.refreshToken
        } catch (e: Exception) {
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth!!.createUserWithEmailAndPassword(cleanEmail, cleanPassword).awaitTask()
                    firebaseUid = authResult.user?.uid
                    val tokenResult = authResult.user?.getIdToken(true)?.awaitTask()
                    freshIdToken = tokenResult?.token
                } catch (sdkError: Exception) {
                    return Result.failure(IllegalArgumentException(sdkError.localizedMessage ?: "Registration failed"))
                }
            } else {
                return Result.failure(IllegalArgumentException(parseAuthError(e)))
            }
        }

        val uid = firebaseUid ?: return Result.failure(IllegalArgumentException("Registration failed: No UID returned"))
        val token = freshIdToken ?: return Result.failure(IllegalArgumentException("Registration failed: No token returned"))

        dataStoreManager.saveAuthToken(token)
        dataStoreManager.saveFirebaseUid(uid)
        dataStoreManager.saveUserEmail(cleanEmail)
        refreshToken?.let { dataStoreManager.saveRefreshToken(it) }
        dataStoreManager.saveUserRole("student")
        dataStoreManager.saveProfileComplete(false)

        return Result.success(
            UserProfile(
                id = uid,
                email = cleanEmail,
                role = "student",
                isProfileComplete = false
            )
        )
    }

    override suspend fun logout() {
        try {
            val token = FirebaseMessaging.getInstance().token.awaitTask()
            if (!token.isNullOrBlank()) {
                notificationApi.unregisterFcmToken(token)
            }
        } catch (_: Exception) {}
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        dataStoreManager.clearSession()
    }

    private fun parseAuthError(exception: Exception): String {
        val msg = exception.message ?: ""
        return when {
            msg.contains("EMAIL_NOT_FOUND", ignoreCase = true) -> "No account found with this email"
            msg.contains("INVALID_PASSWORD", ignoreCase = true) -> "Incorrect password"
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> "Invalid email or password"
            msg.contains("USER_DISABLED", ignoreCase = true) -> "This account has been disabled"
            msg.contains("EMAIL_EXISTS", ignoreCase = true) -> "An account with this email already exists"
            else -> "Authentication failed: ${exception.localizedMessage ?: "Please try again"}"
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWith(Result.failure(it)) }
        }
}
