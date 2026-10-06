package com.nullpointer.nourseCompose.data.settings.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
    private val onboardingMarker: File,
) {
    companion object {
        private const val measureSettingsKey = "key"
    }

    private val measureSettingsStringKey = stringPreferencesKey(measureSettingsKey)
    private val onboardingCompleted = MutableStateFlow(onboardingMarker.isFile)


    fun getMeasureSettingsData(): Flow<SettingsData?> {
        return combine(dataStore.data, onboardingCompleted) { pref, completed ->
            val settings = pref[measureSettingsStringKey]?.let {
                Json.decodeFromString<SettingsData>(it)
            } ?: SettingsData()
            // Ignore the legacy restored flag: completion belongs to this installation.
            settings.copy(onboardingCompleted = completed)
        }
    }

    suspend fun saveNewMeasureSettingsData(settingsData: SettingsData) {
        dataStore.edit { pref ->
            Json.encodeToString(settingsData.copy(onboardingCompleted = false)).let {
                pref[measureSettingsStringKey] = it
            }
        }
        if (settingsData.onboardingCompleted) {
            onboardingMarker.parentFile?.mkdirs()
            if (!onboardingMarker.isFile) onboardingMarker.createNewFile()
            check(onboardingMarker.isFile)
            onboardingCompleted.value = true
        }
    }
}
