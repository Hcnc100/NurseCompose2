package com.nullpointer.nourseCompose.ui.share

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.types.MeasureType


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

            val titleMeasure = stringResource(measureType.titleMeasure)
            val message = stringResource(R.string.title_dialog_add_measure, titleMeasure)

            Column {
                Text(text = message, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(10.dp))
                MeasureInputField(
                    measureType = measureType,
                    hasError = hasError1,
                    measureValue = measureValue1,
                    maxLong = maxLong,
                    changeHasError = changeHasError1,
                    changeMeasureValue = changeMeasureValue1,
                )
                if (measureType == MeasureType.PRESSURE) {
                    Spacer(modifier = Modifier.height(10.dp))
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
    )
}

@Composable
fun MeasureInputField(
    measureType: MeasureType,
    hasError: Boolean,
    measureValue: String,
    maxLong: Long,
    changeHasError: (Boolean) -> Unit,
    changeMeasureValue: (String) -> Unit
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            isError = hasError,
            modifier = Modifier.padding(vertical = 10.dp),
            value = measureValue,
            onValueChange = {
                changeHasError(false)
                if (it.length <= maxLong) {
                    changeMeasureValue(it)
                }
            },
            maxLines = 1,
            singleLine = true,
            placeholder = { Text(text = stringResource(id = measureType.titleMeasure)) },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = KeyboardType.Decimal
            ),
        )
        if (hasError) {
            ErrorText()
        } else {
            LengthText(measureValue.length, maxLong)
        }
    }
}

@Composable
fun ErrorText() {
    Text(
        text = stringResource(R.string.error_invalid_value),
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.error
    )
}

@Composable
fun LengthText(currentLength: Int, maxLength: Long) {
    Text(
        text = "$currentLength/$maxLength",
        style = MaterialTheme.typography.caption,
    )
}