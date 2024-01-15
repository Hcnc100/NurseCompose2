package com.nullpointer.nourseCompose.ui.share.measureItem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContainerMeasureItem(
    isSelected: Boolean,
    modifier: Modifier =Modifier,
    content: @Composable () -> Unit
) {

    val backgroundColor: Color by animateColorAsState(
        if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        label = "MEASURE_ANIMATION_SELECT"
    )
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp),
        elevation = 10.dp,
        modifier = modifier,
        content = content
    )
}