package com.nullpointer.nourseCompose.datasource.settings.local

import com.nullpointer.nourseCompose.data.settings.local.SettingsDataStore
import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.Flow

class SettingsLocalDataSourceImpl(
    private val settingsDataStore: SettingsDataStore
) : SettingsLocalDataSource {
    override suspend fun saveMeasureSettingsData(settingsData: SettingsData) =
        settingsDataStore.saveNewMeasureSettingsData(settingsData)

    override fun getMeasureSettingsData(): Flow<SettingsData?> =
        settingsDataStore.getMeasureSettingsData()
}