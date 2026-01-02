package com.chronos.alarm.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.chronos.alarm.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    
    private object PreferencesKeys {
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val UNINSTALL_PROTECTION = booleanPreferencesKey("uninstall_protection")
        val VOLUME_OVERRIDE = booleanPreferencesKey("volume_override")
        val REBOOT_PROTECTION = booleanPreferencesKey("reboot_protection")
        val THEME = stringPreferencesKey("theme")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            timeFormat = preferences[PreferencesKeys.TIME_FORMAT] ?: "24h",
            uninstallProtection = preferences[PreferencesKeys.UNINSTALL_PROTECTION] ?: false,
            volumeOverride = preferences[PreferencesKeys.VOLUME_OVERRIDE] ?: false,
            rebootProtection = preferences[PreferencesKeys.REBOOT_PROTECTION] ?: false,
            theme = preferences[PreferencesKeys.THEME] ?: "system"
        )
    }

    suspend fun updateSettings(updater: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
            val current = AppSettings(
                timeFormat = preferences[PreferencesKeys.TIME_FORMAT] ?: "24h",
                uninstallProtection = preferences[PreferencesKeys.UNINSTALL_PROTECTION] ?: false,
                volumeOverride = preferences[PreferencesKeys.VOLUME_OVERRIDE] ?: false,
                rebootProtection = preferences[PreferencesKeys.REBOOT_PROTECTION] ?: false,
                theme = preferences[PreferencesKeys.THEME] ?: "system"
            )
            val updated = updater(current)
            
            preferences[PreferencesKeys.TIME_FORMAT] = updated.timeFormat
            preferences[PreferencesKeys.UNINSTALL_PROTECTION] = updated.uninstallProtection
            preferences[PreferencesKeys.VOLUME_OVERRIDE] = updated.volumeOverride
            preferences[PreferencesKeys.REBOOT_PROTECTION] = updated.rebootProtection
            preferences[PreferencesKeys.THEME] = updated.theme
        }
    }
}
