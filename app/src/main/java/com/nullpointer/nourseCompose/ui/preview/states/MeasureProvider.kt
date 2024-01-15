package com.nullpointer.nourseCompose.ui.preview.states

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

class MeasureProvider : PreviewParameterProvider<MeasureData> {
    override val values: Sequence<MeasureData> = sequenceOf(
        MeasureData(
            id = 1,
            value1 = 98f,
            value2 = null,
            type = MeasureType.OXYGEN,
            createAt = System.currentTimeMillis()
        ),
        MeasureData(
            id = 2,
            value1 = 36f,
            value2 = null,
            type = MeasureType.TEMPERATURE,
            createAt = System.currentTimeMillis()
        ),
        MeasureData(
            id = 3,
            value1 = 80F,
            value2 = null,
            type = MeasureType.GLUCOSE,
            createAt = System.currentTimeMillis()
        ),
        MeasureData(
            id = 4,
            value1 = 120F,
            value2 = 80F,
            type = MeasureType.PRESSURE,
            createAt = System.currentTimeMillis()
        ),
    )
}