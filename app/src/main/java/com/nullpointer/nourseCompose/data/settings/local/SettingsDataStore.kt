package com.nullpointer.nourseCompose.data.settings.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val measureSettingsKey = "key"
    }

    private val measureSettingsStringKey = stringPreferencesKey(measureSettingsKey)


    fun getMeasureSettingsData(): Flow<SettingsData?> {
        return dataStore.data.map { pref ->
            pref[measureSettingsStringKey]?.let {
                Json.decodeFromString<SettingsData>(it)
            }
        }
    }

    suspend fun saveNewMeasureSettingsData(settingsData: SettingsData) {
        dataStore.edit { pref ->
            Json.encodeToString(settingsData).let {
                pref[measureSettingsStringKey] = it
            }
        }
    }
}