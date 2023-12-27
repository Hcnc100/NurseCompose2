package com.nullpointer.nourseCompose.domain.settings

import com.nullpointer.nourseCompose.datasource.settings.local.SettingsLocalDataSource
import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.Flow

class SettingsRepoImpl(
    private val settingsLocalDataSource: SettingsLocalDataSource
) : SettingsRepository {

    override suspend fun saveMeasureSettingsData(settingsData: SettingsData) =
        settingsLocalDataSource.saveMeasureSettingsData(settingsData = settingsData)

    override fun getMeasureSettingsData(): Flow<SettingsData?> =
        settingsLocalDataSource.getMeasureSettingsData()
}