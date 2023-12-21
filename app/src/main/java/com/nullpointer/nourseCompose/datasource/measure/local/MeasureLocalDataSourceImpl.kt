package com.nullpointer.nourseCompose.datasource.measure.local

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.data.csv.local.BackUpDatabase
import com.nullpointer.nourseCompose.data.measure.local.MeasureDAO
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class MeasureLocalDataSourceImpl(
    private val measureDAO: MeasureDAO,
    private val backUpDatabase: BackUpDatabase,
) : MeasureLocalDataSource {
    override fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>> =
        measureDAO.getListMeasureByTypes(type, limit).map { measureList ->
            measureList.map(MeasureData::fromMeasureEntity)
        }

    override fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity> =
        measureDAO.getPagingMeasureByTypes(type)

    override suspend fun addMeasure(type: MeasureType, value1: Float, value2: Float?) {
        val measureEntity = MeasureEntity(
            type = type,
            value1 = value1,
            value2 = value2,
        )
        measureDAO.insertMeasure(measureEntity)
    }


    override suspend fun deleterMeasureData(measureData: MeasureData) {
        val measureEntity = MeasureEntity.fromMeasureData(measureData)
        measureDAO.deleter(measureEntity)
    }

    override suspend fun updateMeasureData(measureData: MeasureData) {
        val measureEntity = MeasureEntity.fromMeasureData(measureData)
        measureDAO.updateMeasure(measureEntity)
    }

    override suspend fun exportDatabase(outputStream: OutputStream) {
        measureDAO.getCursorMeasure().use {
            backUpDatabase.exportMeasureToCSVFile(outputStream, it)
        }
    }


    override suspend fun importDatabase(inputStream: InputStream) {
        val list = backUpDatabase.importMeasureFromCSVFile(inputStream)
        measureDAO.updateAll(list)
    }

    override suspend fun deleterAllMeasures() = measureDAO.deleterAll()


}