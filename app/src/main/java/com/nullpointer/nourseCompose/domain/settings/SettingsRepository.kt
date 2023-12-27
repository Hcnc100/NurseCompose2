package com.nullpointer.nourseCompose.domain.settings

import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveMeasureSettingsData(settingsData: SettingsData)
    fun getMeasureSettingsData(): Flow<SettingsData?>
}