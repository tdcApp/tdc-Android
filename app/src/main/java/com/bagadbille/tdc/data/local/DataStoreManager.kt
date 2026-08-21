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
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_THEME_KEY] ?: true }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(AUTH_TOKEN_KEY) }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[IS_DARK_THEME_KEY] = isDark }
    }
}
