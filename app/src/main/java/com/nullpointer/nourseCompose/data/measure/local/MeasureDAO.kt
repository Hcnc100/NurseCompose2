package com.nullpointer.nourseCompose.data.measure.local

import android.database.Cursor
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Insert
    suspend fun insertMeasure(measureEntity: List<MeasureEntity>)


    @Update
    suspend fun updateMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun deleter(measureEntity: MeasureEntity)

    @Query("SELECT * FROM measures")
    fun getCursorMeasure(): Cursor

    @Query("DELETE FROM measures")
    suspend fun deleterAll()

    @Transaction
    suspend fun updateAll(list: List<MeasureEntity>) {
        deleterAll()
        insertMeasure(list)
    }


    @Query("DELETE FROM measures WHERE id IN (:listIds)")
    suspend fun deleter(listIds: List<Int>)
}