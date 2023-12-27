package com.nullpointer.nourseCompose.models.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class SettingsData(
    val numberMeasureGraph: Int = 10
)
