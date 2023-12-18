package com.nullpointer.nourseCompose.measure

import com.nullpointer.nourseCompose.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

class MeasureRepoImpl(
    private val measureLocalDataSource: MeasureLocalDataSource
) : MeasureRepository {
    override fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>> =
        measureLocalDataSource.getListMeasureByType(type, limit)

    override suspend fun addMeasure(measureData: MeasureData) =
        measureLocalDataSource.addMeasure(measureData)

    override suspend fun deleterMeasureData(measureData: MeasureData) =
        measureLocalDataSource.addMeasure(measureData)

    override suspend fun updateMeasureData(measureData: MeasureData) =
        measureLocalDataSource.updateMeasureData(measureData)
}