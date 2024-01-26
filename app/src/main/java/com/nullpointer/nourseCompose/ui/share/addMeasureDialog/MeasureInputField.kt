package com.nullpointer.nourseCompose.ui.share.addMeasureDialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview
import com.nullpointer.nourseCompose.ui.preview.states.MeasureTypeProvider

@Composable
fun MeasureInputField(
    maxLong: Long,
    hasError: Boolean,
    measureValue: String,
    measureType: MeasureType,
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

@SimplePreview
@Composable
private fun MeasureInputFieldPreview(
    @PreviewParameter(MeasureTypeProvider::class)
    measureType: MeasureType
) {
    MeasureInputField(
        maxLong = 7,
        hasError = false,
        measureValue = "",
        measureType = measureType,
        changeHasError = {},
        changeMeasureValue = {}
    )
}

@Composable
private fun ErrorText() {
    Text(
        text = stringResource(R.string.error_invalid_value),
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.error
    )
}

@Composable
private fun LengthText(currentLength: Int, maxLength: Long) {
    Text(
        text = "$currentLength/$maxLength",
        style = MaterialTheme.typography.caption,
    )
}