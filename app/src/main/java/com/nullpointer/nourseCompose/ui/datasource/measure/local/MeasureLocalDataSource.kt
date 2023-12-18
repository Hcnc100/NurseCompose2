package com.nullpointer.nourseCompose.ui.datasource.measure.local

import com.nullpointer.nourseCompose.ui.models.data.MeasureData
import com.nullpointer.nourseCompose.ui.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

interface MeasureLocalDataSource {

    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    suspend fun addMeasure(measureData: MeasureData)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)
}