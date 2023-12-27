package com.nullpointer.nourseCompose.domain.measure

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.constants.Constants
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.datasource.settings.local.SettingsLocalDataSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class MeasureRepoImpl(
    private val measureLocalDataSource: MeasureLocalDataSource,
    private val settingsLocalDataSource: SettingsLocalDataSource,
) : MeasureRepository {
    override fun getListMeasureByType(type: MeasureType): Flow<List<MeasureData>> =
        settingsLocalDataSource.getMeasureSettingsData()
            .map { it?.numberMeasureGraph ?: Constants.DEFAULT_MEASURE_GRAPH }
            .flatMapLatest {
                measureLocalDataSource.getListMeasureByType(type, it)
            }

    override suspend fun addMeasure(type: MeasureType, value1: Float, value2: Float?) =
        measureLocalDataSource.addMeasure(type, value1, value2)


    override suspend fun deleterMeasureData(measureData: MeasureData) =
        measureLocalDataSource.deleterMeasureData(measureData)

    override suspend fun deleterMeasureData(measureData: List<MeasureData>) =
        measureLocalDataSource.deleterMeasureData(measureData)

    override suspend fun updateMeasureData(measureData: MeasureData) =
        measureLocalDataSource.updateMeasureData(measureData)

    override fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity> =
        measureLocalDataSource.getPagingMeasureByType(type)

    override suspend fun exportDatabase(outputStream: OutputStream) =
        measureLocalDataSource.exportDatabase(outputStream)

    override suspend fun importDatabase(inputStream: InputStream) =
        measureLocalDataSource.importDatabase(inputStream)

    override suspend fun deleterAllMeasures() =
        measureLocalDataSource.deleterAllMeasures()


}