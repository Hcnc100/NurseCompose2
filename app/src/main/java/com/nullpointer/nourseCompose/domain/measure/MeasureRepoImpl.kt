package com.nullpointer.nourseCompose.domain.measure

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

class MeasureRepoImpl(
    private val measureLocalDataSource: MeasureLocalDataSource
) : MeasureRepository {
    override fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>> =
        measureLocalDataSource.getListMeasureByType(type, limit)

    override suspend fun addMeasure(value: Float, type: MeasureType) =
        measureLocalDataSource.addMeasure(value, type)


    override suspend fun deleterMeasureData(measureData: MeasureData) =
        measureLocalDataSource.deleterMeasureData(measureData)

    override suspend fun updateMeasureData(measureData: MeasureData) =
        measureLocalDataSource.updateMeasureData(measureData)

    override fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity> =
        measureLocalDataSource.getPagingMeasureByType(type)

}