package com.nullpointer.nourseCompose.data.csv.local

import android.database.Cursor
import androidx.core.database.getFloatOrNull
import androidx.core.database.getLongOrNull
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.nullpointer.nourseCompose.constants.Constants
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import java.io.InputStream
import java.io.OutputStream

class BackUpDatabase {

    private val listTableNames = listOf(
        Constants.MEASURE_ID_NAME,
        Constants.MEASURE_VALUE1_NAME,
        Constants.MEASURE_VALUE2_NAME,
        Constants.MEASURE_TYPE_NAME,
        Constants.MEASURE_CREATE_AT_NAME
    )

    private fun extractInfoRow(cursor: Cursor): List<*>? = with(cursor) {
        try {
            val id = getInt(getColumnIndexOrThrow(Constants.MEASURE_ID_NAME))
            val value1 = getFloatOrNull(getColumnIndexOrThrow(Constants.MEASURE_VALUE1_NAME))
            val value2 = getFloatOrNull(getColumnIndexOrThrow(Constants.MEASURE_VALUE2_NAME))
            val type = getString(getColumnIndexOrThrow(Constants.MEASURE_TYPE_NAME))
            val createAt = getLongOrNull(getColumnIndexOrThrow(Constants.MEASURE_CREATE_AT_NAME))
            return listOf(id, value1, value2, type, createAt)
        } catch (e: Exception) {
            println("Error $e")
            return null
        }
    }

    fun exportMeasureToCSVFile(outputStream: OutputStream, cursor: Cursor) {
        csvWriter().open(outputStream) {
            writeRow(listTableNames)
            if (cursor.moveToFirst()) {
                do {
                    extractInfoRow(cursor)?.let(::writeRow)
                } while (cursor.moveToNext())
            }
        }
    }

    fun importMeasureFromCSVFile(inputSystem: InputStream): List<MeasureEntity> {
        return csvReader().open(inputSystem) {
            readAllWithHeaderAsSequence().mapNotNull { row ->
                val id = row[Constants.MEASURE_ID_NAME]?.toIntOrNull()
                val value1 = row[Constants.MEASURE_VALUE1_NAME]?.toFloatOrNull()
                val value2 = row[Constants.MEASURE_VALUE2_NAME]?.toFloatOrNull()
                val type = row[Constants.MEASURE_TYPE_NAME]
                val createAt = row[Constants.MEASURE_CREATE_AT_NAME]?.toLongOrNull()

                try {
                    MeasureEntity(
                        id = id!!,
                        value1 = value1!!,
                        value2 = value2,
                        type = MeasureType.valueOf(type!!),
                        createAt = createAt!!
                    )
                } catch (e: Exception) {
                    null
                }
            }.toList()
        }
    }


}