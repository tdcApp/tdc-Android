package com.bagadbille.tdc.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
    }

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
}
