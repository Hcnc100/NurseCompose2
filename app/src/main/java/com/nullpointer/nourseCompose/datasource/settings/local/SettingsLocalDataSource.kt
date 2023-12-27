package com.nullpointer.nourseCompose.datasource.settings.local

import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    suspend fun saveMeasureSettingsData(settingsData: SettingsData)
    fun getMeasureSettingsData(): Flow<SettingsData?>
}