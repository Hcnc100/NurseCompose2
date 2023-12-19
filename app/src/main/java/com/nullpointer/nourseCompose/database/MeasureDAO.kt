package com.nullpointer.nourseCompose.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow


@Dao
interface MeasureDAO {

    @Query("SELECT * FROM measures WHERE type = (:type) ORDER BY createAt DESC LIMIT (:limit)")
    fun getListMeasureByTypes(type: MeasureType, limit: Int): Flow<List<MeasureEntity>>

    @Query("SELECT * FROM measures WHERE type = (:type) ORDER BY createAt DESC")
    fun getPagingMeasureByTypes(type: MeasureType): PagingSource<Int, MeasureEntity>

    @Insert
    suspend fun insertMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun updateMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun deleter(measureEntity: MeasureEntity)
}