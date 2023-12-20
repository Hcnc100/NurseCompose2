package com.nullpointer.nourseCompose.domain.measure

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import java.io.File

interface MeasureRepository {
    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    suspend fun addMeasure(type: MeasureType, value1: Float, value2: Float?)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)

    fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity>

    suspend fun exportDatabase(file: File)

    suspend fun importDatabase(file: File)
}