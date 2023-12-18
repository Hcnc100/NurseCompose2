package com.nullpointer.nourseCompose.domain.measure

import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

class MeasureRepoImpl(
    private val measureLocalDataSource: MeasureLocalDataSource
) : MeasureRepository {
    override fun getListMeasureByType(type: MeasureType): Flow<List<MeasureData>> =
        measureLocalDataSource.getListMeasureByType(type)

    override suspend fun addMeasure(value: Float, type: MeasureType) =
        measureLocalDataSource.addMeasure(value, type)


    override suspend fun deleterMeasureData(measureData: MeasureData) =
        measureLocalDataSource.deleterMeasureData(measureData)

    override suspend fun updateMeasureData(measureData: MeasureData) =
        measureLocalDataSource.updateMeasureData(measureData)
}