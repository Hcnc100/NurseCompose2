package com.nullpointer.nourseCompose.ui.screens.home.widgets.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nullpointer.nourseCompose.R

@Composable
fun DrawerActionDialog(
    closeDialog: (Boolean) -> Unit,
    title: String,
    message: String,
) {


    AlertDialog(
        onDismissRequest = { closeDialog(false) },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = { closeDialog(true) }) {
                Text(text = stringResource(R.string.message_accept_dialog))
            }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog(false) }) {
                Text(text = stringResource(R.string.message_cancel_dialog))
            }
        }
    )
}