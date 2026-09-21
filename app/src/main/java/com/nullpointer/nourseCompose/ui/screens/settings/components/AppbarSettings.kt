package com.nullpointer.nourseCompose.ui.screens.settings.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppbarSettings(
    actionBack: () -> Unit
) {
    TopAppBar(
        windowInsets = WindowInsets.statusBars,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White),
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
