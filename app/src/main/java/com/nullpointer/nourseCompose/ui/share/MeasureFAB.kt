package com.nullpointer.nourseCompose.ui.share

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.nullpointer.nourseCompose.R

@Composable
fun MeasureFAB(
    showDialogAdd: () -> Unit,
    isSelectedEnable: Boolean,
    deleterMeasureSelected: () -> Unit,
) {
    val scale by animateFloatAsState(
        if (isSelectedEnable) 0f else 1f,
        label = "FAB_MEASURE_ANIMATION"
    )

    AnimatedVisibility(
        visible = !isSelectedEnable,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) + scaleIn(
            animationSpec = tween(
                durationMillis = 500
            )
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 500)) + scaleOut(
            animationSpec = tween(
                durationMillis = 500
            )
        )
    ) {
        FloatingActionButton(
            onClick = showDialogAdd,
            modifier = Modifier.scale(scale)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_add_24),
                contentDescription = null,
            )
        }
    }

    val scaleDelete = animateFloatAsState(if (isSelectedEnable) 1f else 0f, label = "")

    AnimatedVisibility(
        visible = isSelectedEnable,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) + scaleIn(
            animationSpec = tween(
                durationMillis = 500
            )
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 500)) + scaleOut(
            animationSpec = tween(
                durationMillis = 500
            )
        )
    ) {
        FloatingActionButton(
            onClick = deleterMeasureSelected,
            modifier = Modifier.scale(scaleDelete.value)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_delete_24),
                contentDescription = null,
            )
        }
    }
}
