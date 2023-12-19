package com.nullpointer.nourseCompose.datasource.measure.local

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

interface MeasureLocalDataSource {

    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity>

    suspend fun addMeasure(value: Float, type: MeasureType)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)
}