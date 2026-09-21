package com.nullpointer.nourseCompose.ui.share.measureItem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "MEASURE_ANIMATION_SELECT"
    )
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 10.dp,
        modifier = modifier,
        content = content
    )
}
