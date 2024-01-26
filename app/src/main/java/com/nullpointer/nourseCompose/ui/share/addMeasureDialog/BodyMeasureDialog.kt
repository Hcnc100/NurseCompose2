package com.nullpointer.nourseCompose.ui.share.addMeasureDialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview
import com.nullpointer.nourseCompose.ui.preview.states.MeasureTypeProvider

@Composable
fun BodyMeasureDialog(
    maxLong: Long,
    measureType: MeasureType,
    measureValue1: String,
    hasError1: Boolean,
    changeHasError1: (Boolean) -> Unit,
    changeMeasureValue1: (String) -> Unit,
    measureValue2: String,
    hasError2: Boolean,
    changeHasError2: (Boolean) -> Unit,
    changeMeasureValue2: (String) -> Unit,
) {

    val titleMeasure = stringResource(measureType.titleMeasure)
    val message = stringResource(R.string.title_dialog_add_measure, titleMeasure)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = message, style = MaterialTheme.typography.h6)
        MeasureInputField(
            measureType = measureType,
            hasError = hasError1,
            measureValue = measureValue1,
            maxLong = maxLong,
            changeHasError = changeHasError1,
            changeMeasureValue = changeMeasureValue1,
        )
        if (measureType == MeasureType.PRESSURE) {
            MeasureInputField(
                measureType = measureType,
                hasError = hasError2,
                measureValue = measureValue2,
                maxLong = maxLong,
                changeHasError = changeHasError2,
                changeMeasureValue = changeMeasureValue2,
            )
        }
    }
}

@SimplePreview
@Composable
private fun BodyMeasureDialogSuccessPreview(
    @PreviewParameter(MeasureTypeProvider::class)
    measureType: MeasureType,
) {

    BodyMeasureDialog(
        maxLong = 7,
        measureType = measureType,
        measureValue1 = "",
        hasError1 = false,
        changeHasError1 = {},
        changeMeasureValue1 = {},
        measureValue2 = "",
        hasError2 = false,
        changeHasError2 = {},
        changeMeasureValue2 = {},
    )
}

@SimplePreview
@Composable
private fun BodyMeasureDialogErrorPreview(
    @PreviewParameter(MeasureTypeProvider::class)
    measureType: MeasureType,
) {

    BodyMeasureDialog(
        maxLong = 7,
        measureType = measureType,
        measureValue1 = "",
        hasError1 = true,
        changeHasError1 = {},
        changeMeasureValue1 = {},
        measureValue2 = "",
        hasError2 = true,
        changeHasError2 = {},
        changeMeasureValue2 = {},
    )
}