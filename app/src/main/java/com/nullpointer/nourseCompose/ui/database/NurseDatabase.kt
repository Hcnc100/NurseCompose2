package com.nullpointer.nourseCompose.ui.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullpointer.nourseCompose.ui.models.entity.MeasureEntity


@Database(
    version = 1,
    entities = [MeasureEntity::class],
    exportSchema = true
)
abstract class NurseDatabase : RoomDatabase() {
}