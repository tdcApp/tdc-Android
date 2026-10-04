package com.bagadbille.tdc.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val IS_PROFILE_COMPLETE_KEY = booleanPreferencesKey("is_profile_complete")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_THEME_KEY] ?: true }

    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE_KEY] }

    val isProfileComplete: Flow<Boolean> = context.dataStore.data.map { it[IS_PROFILE_COMPLETE_KEY] ?: false }

    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { it[USER_EMAIL_KEY] = email }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { it[USER_ROLE_KEY] = role }
    }

    suspend fun saveProfileComplete(isComplete: Boolean) {
        context.dataStore.edit { it[IS_PROFILE_COMPLETE_KEY] = isComplete }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(AUTH_TOKEN_KEY)
            it.remove(USER_ROLE_KEY)
            it.remove(IS_PROFILE_COMPLETE_KEY)
            it.remove(USER_EMAIL_KEY)
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[IS_DARK_THEME_KEY] = isDark }
    }
}
