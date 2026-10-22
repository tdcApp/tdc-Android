package com.bagadbille.tdc.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tdc_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val IS_PROFILE_COMPLETE_KEY = booleanPreferencesKey("is_profile_complete")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val FIREBASE_UID_KEY = stringPreferencesKey("firebase_uid")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val MOBILE_NUMBER_KEY = stringPreferencesKey("mobile_number")
        private val LANGUAGE_CLASS_ID_KEY = stringPreferencesKey("language_class_id")
        private val TECH_CLASS_ID_KEY = stringPreferencesKey("tech_class_id")
        private val READ_NOTIF_IDS_KEY = stringSetPreferencesKey("read_notif_ids")
        private val SUBMITTED_QUIZ_IDS_KEY = stringSetPreferencesKey("submitted_quiz_ids")
        private val HAS_COMPLETED_ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { it[HAS_COMPLETED_ONBOARDING_KEY] ?: false }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }

    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_THEME_KEY] ?: true }

    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE_KEY] }

    val isProfileComplete: Flow<Boolean> = context.dataStore.data.map { it[IS_PROFILE_COMPLETE_KEY] ?: false }

    val readNotifIds: Flow<Set<String>> = context.dataStore.data.map { it[READ_NOTIF_IDS_KEY] ?: emptySet() }

    val submittedQuizIds: Flow<Set<String>> = context.dataStore.data.map { it[SUBMITTED_QUIZ_IDS_KEY] ?: emptySet() }

    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }

    val firebaseUid: Flow<String?> = context.dataStore.data.map { it[FIREBASE_UID_KEY] }

    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }

    val mobileNumber: Flow<String?> = context.dataStore.data.map { it[MOBILE_NUMBER_KEY] }

    val languageClassId: Flow<String?> = context.dataStore.data.map { it[LANGUAGE_CLASS_ID_KEY] }

    val technologyClassId: Flow<String?> = context.dataStore.data.map { it[TECH_CLASS_ID_KEY] }

    suspend fun saveFirebaseUid(uid: String) {
        context.dataStore.edit { it[FIREBASE_UID_KEY] = uid }
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { it[USER_EMAIL_KEY] = email }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { it[REFRESH_TOKEN_KEY] = token }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[USER_NAME_KEY] = name }
    }

    suspend fun saveMobileNumber(mobile: String) {
        context.dataStore.edit { it[MOBILE_NUMBER_KEY] = mobile }
    }

    suspend fun saveLanguageClassId(id: String) {
        context.dataStore.edit { it[LANGUAGE_CLASS_ID_KEY] = id }
    }

    suspend fun saveTechnologyClassId(id: String) {
        context.dataStore.edit { it[TECH_CLASS_ID_KEY] = id }
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { it[USER_ROLE_KEY] = role }
    }

    suspend fun saveProfileComplete(isComplete: Boolean) {
        context.dataStore.edit { it[IS_PROFILE_COMPLETE_KEY] = isComplete }
    }

    suspend fun markNotifRead(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[READ_NOTIF_IDS_KEY] ?: emptySet()
            prefs[READ_NOTIF_IDS_KEY] = current + id
        }
    }

    suspend fun markNotifsRead(ids: Collection<String>) {
        context.dataStore.edit { prefs ->
            val current = prefs[READ_NOTIF_IDS_KEY] ?: emptySet()
            prefs[READ_NOTIF_IDS_KEY] = current + ids
        }
    }

    suspend fun markQuizSubmitted(quizId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SUBMITTED_QUIZ_IDS_KEY] ?: emptySet()
            prefs[SUBMITTED_QUIZ_IDS_KEY] = current + quizId
        }
    }

    suspend fun unmarkQuizSubmitted(quizId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SUBMITTED_QUIZ_IDS_KEY] ?: emptySet()
            prefs[SUBMITTED_QUIZ_IDS_KEY] = current - quizId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(AUTH_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
            it.remove(USER_ROLE_KEY)
            it.remove(IS_PROFILE_COMPLETE_KEY)
            it.remove(USER_EMAIL_KEY)
            it.remove(FIREBASE_UID_KEY)
            it.remove(USER_NAME_KEY)
            it.remove(MOBILE_NUMBER_KEY)
            it.remove(LANGUAGE_CLASS_ID_KEY)
            it.remove(TECH_CLASS_ID_KEY)
            it.remove(READ_NOTIF_IDS_KEY)
            it.remove(SUBMITTED_QUIZ_IDS_KEY)
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[IS_DARK_THEME_KEY] = isDark }
    }

    suspend fun setCompletedOnboarding(completed: Boolean = true) {
        context.dataStore.edit { it[HAS_COMPLETED_ONBOARDING_KEY] = completed }
    }

    suspend fun saveActiveQuiz(quizId: String, startedAtMillis: Long, totalDurationSeconds: Int, initialStrikes: Int = 0) {
        context.dataStore.edit { prefs ->
            prefs[longPreferencesKey("quiz_started_at_$quizId")] = startedAtMillis
            prefs[intPreferencesKey("quiz_duration_$quizId")] = totalDurationSeconds
            prefs[intPreferencesKey("quiz_strikes_$quizId")] = initialStrikes
        }
    }

    suspend fun getActiveQuizStartedAt(quizId: String): Long? {
        val prefs = context.dataStore.data.firstOrNull() ?: return null
        return prefs[longPreferencesKey("quiz_started_at_$quizId")]
    }

    suspend fun getActiveQuizStrikes(quizId: String): Int {
        val prefs = context.dataStore.data.firstOrNull() ?: return 0
        return prefs[intPreferencesKey("quiz_strikes_$quizId")] ?: 0
    }

    suspend fun getActiveQuizDuration(quizId: String): Int {
        val prefs = context.dataStore.data.firstOrNull() ?: return 1800
        return prefs[intPreferencesKey("quiz_duration_$quizId")] ?: 1800
    }

    suspend fun incrementQuizStrikes(quizId: String): Int {
        var newStrikes = 0
        context.dataStore.edit { prefs ->
            val key = intPreferencesKey("quiz_strikes_$quizId")
            val current = prefs[key] ?: 0
            newStrikes = current + 1
            prefs[key] = newStrikes
        }
        return newStrikes
    }

    suspend fun setQuizStrikes(quizId: String, strikes: Int) {
        context.dataStore.edit { prefs ->
            prefs[intPreferencesKey("quiz_strikes_$quizId")] = strikes
        }
    }

    suspend fun saveQuizAnswer(quizId: String, questionId: String, answer: String) {
        context.dataStore.edit { prefs ->
            val key = stringPreferencesKey("quiz_ans_${quizId}_$questionId")
            if (answer.isBlank()) {
                prefs.remove(key)
            } else {
                prefs[key] = answer
            }
        }
    }

    suspend fun getSavedQuizAnswers(quizId: String, questionIds: List<String>): Map<String, String> {
        val prefs = context.dataStore.data.firstOrNull() ?: return emptyMap()
        val map = mutableMapOf<String, String>()
        for (qId in questionIds) {
            val ans = prefs[stringPreferencesKey("quiz_ans_${quizId}_$qId")]
            if (!ans.isNullOrBlank()) {
                map[qId] = ans
            }
        }
        return map
    }

    suspend fun saveQuizReviewStatus(quizId: String, questionId: String, isReviewed: Boolean) {
        context.dataStore.edit { prefs ->
            val key = booleanPreferencesKey("quiz_rev_${quizId}_$questionId")
            if (isReviewed) {
                prefs[key] = true
            } else {
                prefs.remove(key)
            }
        }
    }

    suspend fun getSavedQuizReviews(quizId: String, questionIds: List<String>): Set<String> {
        val prefs = context.dataStore.data.firstOrNull() ?: return emptySet()
        val set = mutableSetOf<String>()
        for (qId in questionIds) {
            if (prefs[booleanPreferencesKey("quiz_rev_${quizId}_$qId")] == true) {
                set.add(qId)
            }
        }
        return set
    }

    suspend fun clearActiveQuiz(quizId: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(longPreferencesKey("quiz_started_at_$quizId"))
            prefs.remove(intPreferencesKey("quiz_duration_$quizId"))
            prefs.remove(intPreferencesKey("quiz_strikes_$quizId"))
            val keysToRemove = prefs.asMap().keys.filter {
                it.name.startsWith("quiz_ans_${quizId}_") || it.name.startsWith("quiz_rev_${quizId}_")
            }
            keysToRemove.forEach { key -> prefs.remove(key) }
        }
    }
}
