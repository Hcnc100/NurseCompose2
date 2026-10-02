package com.nullpointer.nourseCompose.ui.screens.settings.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.share.AppTopBar
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppbarSettings(
    actionBack: () -> Unit
) {
    AppTopBar(title = stringResource(R.string.title_settings), onBack = actionBack)
}

@SimplePreview
@Composable
private fun AppbarSettingsPreview() {
    AppbarSettings(actionBack = {})
}
