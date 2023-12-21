package com.nullpointer.nourseCompose.ui.screens.home.actions

import androidx.annotation.DrawableRes
import com.nullpointer.nourseCompose.R

enum class DrawerActions(
    val title: String,
    @DrawableRes
    val icon: Int,
) {
    IMPORT(
        title = "Import",
        icon = R.drawable.baseline_backup_24
    ),
    EXPORT(
        title = "Export",
        icon = R.drawable.baseline_cloud_download_24
    ),
    SETTINGS(
        title = "Settings",
        icon = R.drawable.baseline_build_24
    ),
    CLEAR_DATA(
        title = "Clear all Data",
        icon = R.drawable.baseline_delete_24
    );
}