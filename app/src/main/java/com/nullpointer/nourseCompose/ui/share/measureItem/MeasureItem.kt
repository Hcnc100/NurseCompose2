package com.nullpointer.nourseCompose.ui.share.measureItem

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview
import com.nullpointer.nourseCompose.ui.preview.states.MeasureProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasureItem(
    isSelected: Boolean,
    measureData: MeasureData,
    isSelectedEnable: Boolean,
    modifier: Modifier = Modifier,
    addMeasureSelected: (MeasureData) -> Unit,
) {
    ContainerMeasureItem(
        modifier = modifier,
        isSelected = isSelected,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = measureData.type.titleMeasure),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                )
                TimeMeasureIndicator(createAt = measureData.createAt)
            }
            Text(
                text = measureData.formattedValue(
                    androidx.core.os.ConfigurationCompat.getLocales(
                        androidx.compose.ui.platform.LocalConfiguration.current
                    )[0] ?: java.util.Locale.getDefault()
                ),
                modifier = Modifier.padding(top = 5.dp)
            )
        }

    }
}

@SimplePreview
@Composable
private fun MeasureItemPreview(
    @PreviewParameter(MeasureProvider::class)
    measureData: MeasureData
) {
    MeasureItem(
        measureData = measureData,
        addMeasureSelected = {},
        isSelected = false,
        isSelectedEnable = true
    )
}

@SimplePreview
@Composable
private fun MeasureItemSelectedPreview(
    @PreviewParameter(MeasureProvider::class)
    measureData: MeasureData
) {
    MeasureItem(
        measureData = measureData,
        addMeasureSelected = {},
        isSelected = true,
        isSelectedEnable = true
    )
}

