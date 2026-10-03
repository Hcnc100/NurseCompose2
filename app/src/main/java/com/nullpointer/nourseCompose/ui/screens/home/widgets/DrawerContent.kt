package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ListItemDefaults
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
    Column(Modifier.verticalScroll(rememberScrollState())) {
        ContainerDrawer()

        listOf(DrawerActions.ALARM_LOGS, DrawerActions.EXPORT_MEDICATION_PDF, DrawerActions.SETTINGS).forEach {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier
                    .clickable { drawerAction(it) },
                headlineContent = {
                    Text(
                        stringResource(id = it.title),
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(it.icon),
                        contentDescription = null,
                    )
                }
            )
        }
    }
}
