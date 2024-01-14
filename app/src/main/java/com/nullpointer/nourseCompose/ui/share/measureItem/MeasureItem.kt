package com.nullpointer.nourseCompose.ui.share.measureItem

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.share.TimeMeasureIndicator
import com.nullpointer.runningcompose.ui.preview.config.ThemePreviews

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasureItem(
    measureData: MeasureData,
    modifier: Modifier = Modifier,
    addMeasureSelected: (MeasureData) -> Unit,
    isSelectedEnable: Boolean,
    isSelected: Boolean,
) {
    ContainerMeasureItem(
        modifier = modifier,
        isSelected = isSelected,
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        if (isSelectedEnable) {
                            addMeasureSelected(measureData)
                        }
                    },
                    onLongClick = {
                        if (!isSelectedEnable) {
                            addMeasureSelected(measureData)
                        }
                    }
                )
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = measureData.type.titleMeasure),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                TimeMeasureIndicator(createAt = measureData.createAt)
            }
            Text(
                text = measureData.showValue,
                modifier = Modifier.padding(top = 5.dp)
            )
        }

    }
}

@ThemePreviews
@Composable
private fun MeasureItemPreview() {
    MeasureItem(
        measureData = MeasureData(
            createAt = System.currentTimeMillis(),
            id = 1,
            type = MeasureType.PRESSURE,
            value1 = 120f,
            value2 = 80f
        ),
        addMeasureSelected = {},
        isSelected = false,
        isSelectedEnable = true
    )
}
