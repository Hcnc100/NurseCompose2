package com.nullpointer.nourseCompose.ui.share

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullpointer.nourseCompose.models.data.MeasureData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasureItem(
    measureData: MeasureData,
    modifier: Modifier = Modifier,
    addMeasureSelected: (MeasureData) -> Unit,
    isSelectedEnable: Boolean,
    isSelected: Boolean
) {


    val backgroundColor: Color by animateColorAsState(
        if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        label = "MEASURE_ANIMATION_SELECT"
    )


    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp),
        elevation = 10.dp,
        modifier = modifier
            .padding(horizontal = 10.dp)
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
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${measureData.type}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = measureData.createAt,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                    ),
                )
            }
            Text(
                text = measureData.showValue,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}