package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.eduLensDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "edulens_accessibility_prefs"
)

data class AccessibilityPreferences(
    val selectedLanguageCode: String = "en",
    val lowBandwidthMode: Boolean = false,
    val offlineAccessibilityMode: Boolean = false,
    val autoSaveOffline: Boolean = true,
    val voiceSpeechRate: Float = 1.0f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("selected_language_code")
        val LOW_BANDWIDTH = booleanPreferencesKey("low_bandwidth_mode")
        val OFFLINE_MODE = booleanPreferencesKey("offline_accessibility_mode")
        val AUTO_SAVE_OFFLINE = booleanPreferencesKey("auto_save_offline")
        val VOICE_SPEED = floatPreferencesKey("voice_speech_rate")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferences: Flow<AccessibilityPreferences> = context.eduLensDataStore.data.map { prefs ->
        val storedTheme = prefs[Keys.THEME_MODE]?.let { raw ->
            runCatching { ThemeMode.valueOf(raw) }.getOrNull()
        } ?: ThemeMode.SYSTEM

        AccessibilityPreferences(
            selectedLanguageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            lowBandwidthMode = prefs[Keys.LOW_BANDWIDTH] ?: false,
            offlineAccessibilityMode = prefs[Keys.OFFLINE_MODE] ?: false,
            autoSaveOffline = prefs[Keys.AUTO_SAVE_OFFLINE] ?: true,
            voiceSpeechRate = prefs[Keys.VOICE_SPEED] ?: 1.0f,
            themeMode = storedTheme
        )
    }

    suspend fun setLanguageCode(code: String) {
        context.eduLensDataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun setLowBandwidthMode(enabled: Boolean) {
        context.eduLensDataStore.edit { it[Keys.LOW_BANDWIDTH] = enabled }
    }

    suspend fun setOfflineAccessibilityMode(enabled: Boolean) {
        context.eduLensDataStore.edit { it[Keys.OFFLINE_MODE] = enabled }
    }

    suspend fun setAutoSaveOffline(enabled: Boolean) {
        context.eduLensDataStore.edit { it[Keys.AUTO_SAVE_OFFLINE] = enabled }
    }

    suspend fun setVoiceSpeechRate(rate: Float) {
        context.eduLensDataStore.edit { it[Keys.VOICE_SPEED] = rate }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.eduLensDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
