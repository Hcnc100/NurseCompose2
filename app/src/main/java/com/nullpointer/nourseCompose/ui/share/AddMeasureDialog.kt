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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.models.types.MeasureType


@Composable
fun AddMeasureDialog(
    measureType: MeasureType,
    onDismissDialog: (Float?) -> Unit,
    maxLong: Long = 7
) {

    val (measureValue, changeMeasureValue) = rememberSaveable {
        mutableStateOf("")
    }
    val (hasError, changeHasError) = rememberSaveable {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = { onDismissDialog(null) },
        confirmButton = {
            TextButton(
                content = {
                    Text(text = "Save")
                },
                onClick = {
                    val value = measureValue.toFloatOrNull()
                    if (value == null) {
                        changeHasError(true)
                    } else {
                        onDismissDialog(value)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = { onDismissDialog(null) }) {
                Text(text = "Cancel")
            }
        },
        text = {
            Column {
                Text(text = "Add New Measure", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(10.dp))
                MeasureInputField(
                    measureType = measureType,
                    hasError = hasError,
                    measureValue = measureValue,
                    maxLong = maxLong,
                    changeHasError = changeHasError,
                    changeMeasureValue = changeMeasureValue,
                )
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
            placeholder = { Text(text = measureType.name) },
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
        text = "Not is a valid number",
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