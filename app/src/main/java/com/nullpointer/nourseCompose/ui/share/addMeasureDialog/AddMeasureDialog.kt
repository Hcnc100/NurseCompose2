package com.nullpointer.nourseCompose.ui.share.addMeasureDialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview
import com.nullpointer.nourseCompose.ui.preview.states.MeasureTypeProvider


@Composable
fun AddMeasureDialog(
    maxLong: Long = 7,
    measureType: MeasureType,
    onDismissDialog: (Float?, Float?) -> Unit,

    ) {

    val (measureValue1, changeMeasureValue1) = rememberSaveable {
        mutableStateOf("")
    }
    val (hasError1, changeHasError1) = rememberSaveable {
        mutableStateOf(false)
    }

    val (measureValue2, changeMeasureValue2) = rememberSaveable {
        mutableStateOf("")
    }
    val (hasError2, changeHasError2) = rememberSaveable {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = { onDismissDialog(null, null) },
        confirmButton = {
            TextButton(
                content = {
                    Text(text = stringResource(R.string.button_save_title))
                },
                onClick = {
                    val value1 = measureValue1.toFloatOrNull()
                    val value2 = measureValue2.toFloatOrNull()

                    if (value1 == null) {
                        changeHasError1(true)
                        return@TextButton
                    }

                    if (value2 == null && measureType == MeasureType.PRESSURE) {
                        changeHasError2(true)
                        return@TextButton
                    }


                    onDismissDialog(value1, value2)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = { onDismissDialog(null, null) }) {
                Text(text = stringResource((R.string.button_calcel_title)))
            }
        },
        text = {
            BodyMeasureDialog(
                maxLong = maxLong,
                measureType = measureType,
                measureValue1 = measureValue1,
                hasError1 = hasError1,
                changeHasError1 = changeHasError1,
                changeMeasureValue1 = changeMeasureValue1,
                measureValue2 = measureValue2,
                hasError2 = hasError2,
                changeHasError2 = changeHasError2,
                changeMeasureValue2 = changeMeasureValue2,
            )
        }
    )
}

@SimplePreview
@Composable
private fun AddMeasureDialogPreview(
    @PreviewParameter(MeasureTypeProvider::class)
    measureType: MeasureType,
) {
    AddMeasureDialog(
        maxLong = 7,
        measureType = measureType,
        onDismissDialog = { _, _ -> }
    )
}
