package com.nullpointer.nourseCompose.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullpointer.nourseCompose.models.entity.MeasureEntity


@Database(
    version = 3,
    entities = [MeasureEntity::class],
    exportSchema = false
)
abstract class NurseDatabase : RoomDatabase() {

    abstract fun getMeasureDAO(): MeasureDAO
}