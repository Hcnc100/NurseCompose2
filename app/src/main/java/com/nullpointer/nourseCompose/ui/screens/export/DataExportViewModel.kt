package com.nullpointer.nourseCompose.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepository
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.reports.HealthDataPdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.OutputStream
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

@HiltViewModel
class DataExportViewModel @Inject constructor(private val measures: MeasureRepository, private val reminders: MedicationReminderRepository, @ApplicationContext private val context: Context) : ViewModel() {
    private val allMeasures = MeasureType.entries.map { measures.getListMeasureByType(it) }.combineAll()
    private val allReminders = reminders.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val totalCount = combine(allMeasures, allReminders) { m, r -> m.size + r.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    fun writePdf(selectedTypes: Set<MeasureType>, includeReminders: Boolean, output: OutputStream) = HealthDataPdfExporter.write(context, allMeasures.value.filter { it.type in selectedTypes }, if (includeReminders) allReminders.value else emptyList(), output)
}

private fun List<kotlinx.coroutines.flow.Flow<List<MeasureData>>>.combineAll(): kotlinx.coroutines.flow.StateFlow<List<MeasureData>> {
    var result: kotlinx.coroutines.flow.Flow<List<MeasureData>> = first()
    drop(1).forEach { next -> result = combine(result, next) { left, right -> left + right } }
    return result.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), SharingStarted.Eagerly, emptyList())
}
