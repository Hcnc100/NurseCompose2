package com.nullpointer.nourseCompose.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullpointer.nourseCompose.data.measure.local.MeasureDAO
import com.nullpointer.nourseCompose.models.entity.MeasureEntity


@Database(
    version = 4,
    entities = [MeasureEntity::class],
    exportSchema = false
)
abstract class NurseDatabase : RoomDatabase() {

    abstract fun getMeasureDAO(): MeasureDAO
}