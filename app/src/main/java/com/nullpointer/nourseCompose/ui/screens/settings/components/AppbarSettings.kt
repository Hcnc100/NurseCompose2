package com.nullpointer.nourseCompose.ui.screens.settings.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview

@Composable
fun AppbarSettings(
    actionBack: () -> Unit
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        title = { Text(text = stringResource(R.string.title_settings)) },
        navigationIcon = {
            IconButton(
                onClick = actionBack,
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = null
                    )
                }
            )
        }
    )
}

@SimplePreview
@Composable
private fun AppbarSettingsPreview() {
    AppbarSettings(actionBack = {})
}