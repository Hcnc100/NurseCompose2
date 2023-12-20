package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.nullpointer.nourseCompose.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DrawerContent(
    exportDatabase: () -> Unit,
    importDatabase: () -> Unit
) {
    Column {
        ListItem(
            modifier = Modifier.clickable {

            },
            text = { Text("Settings") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_build_24),
                    contentDescription = null
                )
            }
        )
        ListItem(
            modifier = Modifier.clickable {
                exportDatabase()
            },
            text = { Text("Export") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_import_export_24),
                    contentDescription = null
                )
            }
        )
        ListItem(
            modifier = Modifier.clickable {
                importDatabase()
            },
            text = { Text("Import") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_import_export_24),
                    contentDescription = null
                )
            }
        )
    }
}