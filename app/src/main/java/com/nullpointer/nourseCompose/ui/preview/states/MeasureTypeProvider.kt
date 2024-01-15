package com.nullpointer.nourseCompose.ui.preview.states


import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.nullpointer.nourseCompose.models.types.MeasureType

class MeasureTypeProvider : PreviewParameterProvider<MeasureType> {
    override val values: Sequence<MeasureType> = MeasureType.values().asSequence()
}