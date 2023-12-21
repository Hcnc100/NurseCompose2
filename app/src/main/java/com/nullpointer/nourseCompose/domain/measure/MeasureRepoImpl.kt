package com.nullpointer.nourseCompose.domain.measure

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

class MeasureRepoImpl(
    private val measureLocalDataSource: MeasureLocalDataSource
) : MeasureRepository {
    override fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>> =
        measureLocalDataSource.getListMeasureByType(type, limit)

    override suspend fun addMeasure(type: MeasureType, value1: Float, value2: Float?) =
        measureLocalDataSource.addMeasure(type, value1, value2)


    override suspend fun deleterMeasureData(measureData: MeasureData) =
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