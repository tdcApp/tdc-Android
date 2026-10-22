package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.model.isMentorRole
import com.bagadbille.tdc.data.model.toDomain
import com.bagadbille.tdc.data.remote.api.AuthApi
import com.bagadbille.tdc.data.remote.api.ProfileApi
import com.bagadbille.tdc.data.remote.dto.CreateProfileRequest
import com.bagadbille.tdc.data.remote.dto.UpdateProfileRequest
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
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
    suspend fun updateProfile(name: String?, phone: String?, year: String? = null, enrollmentNumber: String? = null): Result<UserProfile>
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val profileApi: ProfileApi,
    private val authApi: AuthApi
) : ProfileRepository {

    private suspend fun currentEmail(): String =
        dataStoreManager.userEmail.first() ?: ""

    private suspend fun currentUid(): String =
        dataStoreManager.firebaseUid.first() ?: ""

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val email = currentEmail()
        val uid = currentUid()

        try {
            val profileDto = profileApi.getProfile()
            val role = profileDto.role.lowercase()
            val isMentor = isMentorRole(role)
            val isComplete = true

            dataStoreManager.saveProfileComplete(true)
            dataStoreManager.saveUserRole(role)
            profileDto.name?.takeIf { it.isNotBlank() }?.let { dataStoreManager.saveUserName(it) }
            if (profileDto.effectiveMobile.isNotBlank()) {
                dataStoreManager.saveMobileNumber(profileDto.effectiveMobile)
            }
            profileDto.effectiveLanguageClassId?.let { dataStoreManager.saveLanguageClassId(it) }
            profileDto.effectiveTechnologyClassId?.let { dataStoreManager.saveTechnologyClassId(it) }

            val cachedName = dataStoreManager.userName.first()
            val effectiveName = profileDto.name?.takeIf { it.isNotBlank() } ?: cachedName ?: email.substringBefore("@")
            profileDto.toDomain(fallbackUid = uid, fallbackEmail = email, fallbackComplete = true).copy(name = effectiveName, isProfileComplete = true)
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 404) {
                // Profile genuinely does not exist in Neon DB yet
                dataStoreManager.saveProfileComplete(false)
                dataStoreManager.saveUserName("")
                UserProfile(id = uid, email = email, role = "student", isProfileComplete = false)
            } else {
                // Network error, 5xx, or temporary issue: do NOT wipe isProfileComplete to false!
                try {
                    val bootstrap = authApi.getAuthBootstrap()
                    if (bootstrap.profileExists && bootstrap.profile != null) {
                        val p = bootstrap.profile
                        val role = p.role.lowercase()
                        val isMentor = isMentorRole(role)
                        val isComplete = isMentor || !p.name.isNullOrBlank()

                        dataStoreManager.saveProfileComplete(isComplete)
                        dataStoreManager.saveUserRole(role)
                        p.name?.let { dataStoreManager.saveUserName(it) }
                        dataStoreManager.saveMobileNumber(p.effectiveMobile)
                        p.effectiveLanguageClassId?.let { dataStoreManager.saveLanguageClassId(it) }
                        p.effectiveTechnologyClassId?.let { dataStoreManager.saveTechnologyClassId(it) }

                        p.toDomain(fallbackUid = uid, fallbackEmail = email, fallbackComplete = isComplete)
                    } else if (bootstrap.profileExists == false) {
                        // Backend explicitly confirmed user profile doesn't exist in DB
                        dataStoreManager.saveProfileComplete(false)
                        dataStoreManager.saveUserName("")
                        UserProfile(id = uid, email = email, role = "student", isProfileComplete = false)
                    } else {
                        // Retain cached values from DataStore
                        val cachedName = dataStoreManager.userName.first()
                        val cachedRole = dataStoreManager.userRole.first() ?: "student"
                        val cachedComplete = dataStoreManager.isProfileComplete.first()
                        UserProfile(
                            id = uid,
                            email = email,
                            name = cachedName,
                            role = cachedRole,
                            isProfileComplete = cachedComplete
                        )
                    }
                } catch (_: Exception) {
                    // Retain cached values from DataStore on network failure
                    val cachedName = dataStoreManager.userName.first()
                    val cachedRole = dataStoreManager.userRole.first() ?: "student"
                    val cachedComplete = dataStoreManager.isProfileComplete.first()
                    UserProfile(
                        id = uid,
                        email = email,
                        name = cachedName,
                        role = cachedRole,
                        isProfileComplete = cachedComplete
                    )
                }
            }
        }
    }

    override suspend fun setupProfile(
        name: String,
        languageClassId: String?,
        technologyClassId: String?,
        enrollmentNumber: String?,
        year: String?,
        mobileNumber: String?
    ): Result<UserProfile> = runCatching {
        val cleanName = name.trim()
        val cleanMobile = mobileNumber?.trim().orEmpty()
        val langId = languageClassId?.trim().orEmpty()
        val techId = technologyClassId?.trim().orEmpty()

        if (cleanName.length < 2) {
            throw IllegalArgumentException("Full name must be at least 2 characters")
        }
        if (!cleanMobile.matches(Regex("^\\d{10}$"))) {
            throw IllegalArgumentException("Mobile number must be exactly 10 digits")
        }
        if (langId.isBlank()) {
            throw IllegalArgumentException("Please select a language class")
        }
        if (techId.isBlank()) {
            throw IllegalArgumentException("Please select a technology class")
        }

        val cleanYear = year?.trim()?.ifBlank { null }
        val cleanEnrollment = enrollmentNumber?.trim()?.ifBlank { null }

        try {
            // POST /users/profile to NestJS backend to persist into Neon PostgreSQL
            val createdDto = profileApi.createProfile(
                CreateProfileRequest(
                    name = cleanName,
                    mobileNumber = cleanMobile,
                    languageClassId = langId,
                    technologyClassId = techId,
                    year = cleanYear,
                    enrollmentNumber = cleanEnrollment
                )
            )

            dataStoreManager.saveProfileComplete(true)
            dataStoreManager.saveUserRole(createdDto.role.lowercase())
            dataStoreManager.saveUserName(createdDto.name ?: cleanName)
            dataStoreManager.saveMobileNumber(createdDto.effectiveMobile)
            dataStoreManager.saveLanguageClassId(langId)
            dataStoreManager.saveTechnologyClassId(techId)

            createdDto.toDomain(fallbackUid = currentUid(), fallbackEmail = currentEmail(), fallbackComplete = true)
        } catch (e: HttpException) {
            if (e.code() == 409) {
                // Profile already exists in Neon DB -> refresh and return existing profile
                return getProfile()
            }
            val parsedMsg = parseHttpError(e)
            android.util.Log.e("TDC_PROFILE", "setupProfile HTTP ${e.code()}: $parsedMsg", e)
            throw Exception(parsedMsg, e)
        }
    }

    private fun parseHttpError(e: HttpException): String {
        return try {
            val raw = e.response()?.errorBody()?.string() ?: return "Server error (${e.code()})"
            android.util.Log.e("TDC_PROFILE", "HTTP ${e.code()} raw body: $raw")
            // Check if message is a single string: "message": "..."
            val singleMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(raw)
            if (singleMatch != null) return singleMatch.groupValues[1]

            // Check if message is an array: "message": ["...", "..."]
            val arrayMatch = Regex("\"message\"\\s*:\\s*\\[([^\\]]+)\\]").find(raw)
            if (arrayMatch != null) {
                val items = Regex("\"([^\"]+)\"").findAll(arrayMatch.groupValues[1]).map { it.groupValues[1] }.toList()
                if (items.isNotEmpty()) return items.joinToString(", ")
            }
            "Server error (${e.code()})"
        } catch (_: Exception) {
            "Server error (${e.code()})"
        }
    }

    override suspend fun setupProfile(name: String, enrollmentNumber: String, year: String): Result<UserProfile> =
        setupProfile(name, null, null, enrollmentNumber, year)

    override suspend fun updateProfile(name: String?, phone: String?, year: String?, enrollmentNumber: String?): Result<UserProfile> = runCatching {
        val cleanName = name?.trim()
        val cleanPhone = phone?.trim()
        val cleanYear = year?.trim()?.ifBlank { null }
        val cleanEnrollment = enrollmentNumber?.trim()?.ifBlank { null }

        val updatedDto = profileApi.updateProfile(
            UpdateProfileRequest(
                name = cleanName,
                mobileNumber = cleanPhone,
                year = cleanYear,
                enrollmentNumber = cleanEnrollment
            )
        )
        dataStoreManager.saveUserName(updatedDto.name ?: cleanName ?: "")
        dataStoreManager.saveMobileNumber(updatedDto.effectiveMobile)

        updatedDto.toDomain(fallbackUid = currentUid(), fallbackEmail = currentEmail())
    }
}
