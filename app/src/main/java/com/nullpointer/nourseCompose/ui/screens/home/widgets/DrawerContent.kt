package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions

@Composable
fun DrawerContent(
    drawerAction: (DrawerActions) -> Unit
) {
    Column {
        ContainerDrawer()

        DrawerActions.values().map {
            ListItem(
                modifier = Modifier
                    .clickable { drawerAction(it) },
                headlineContent = {
                    Text(
                        stringResource(id = it.title),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(it.icon),
                        contentDescription = stringResource(id = it.title),
                    )
                }
            )
        }
    }
}
