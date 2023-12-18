package com.nullpointer.nourseCompose.ui.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nullpointer.nourseCompose.ui.models.types.MeasureType

@Entity(tableName = "measures")
data class MeasureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val createAt: Double,
    val type: MeasureType,
    val dateInMillis: Long = System.currentTimeMillis(),
)