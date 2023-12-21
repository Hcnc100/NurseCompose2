package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DrawerContent(
    drawerAction: (DrawerActions) -> Unit
) {
    Column {
        ContainerDrawer()

        DrawerActions.values().map {
            ListItem(
                modifier = Modifier.clickable { drawerAction(it) },
                text = { Text(it.title) },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(it.icon),
                        contentDescription = it.title,
                    )
                }
            )
        }
    }
}