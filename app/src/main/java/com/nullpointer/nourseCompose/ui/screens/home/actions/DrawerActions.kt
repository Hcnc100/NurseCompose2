package com.nullpointer.nourseCompose.ui.screens.home.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.nullpointer.nourseCompose.R

enum class DrawerActions(
    @StringRes
    val title: Int,
    @DrawableRes
    val icon: Int,
) {
    IMPORT(
        title = R.string.title_option_import,
        icon = R.drawable.baseline_backup_24
    ),
    EXPORT(
        title = R.string.title_export_option,
        icon = R.drawable.baseline_cloud_download_24
    ),

    SETTINGS(
        title = R.string.title_settings_option,
        icon = R.drawable.baseline_build_24
    ),
    CLEAR_DATA(
        title = R.string.title_clear_all_data_option,
        icon = R.drawable.baseline_delete_24
    );
}