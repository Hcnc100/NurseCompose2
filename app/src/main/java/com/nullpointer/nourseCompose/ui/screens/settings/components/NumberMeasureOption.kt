package com.nullpointer.nourseCompose.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.SettingsData
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview

@Composable
fun NumberMeasureOption(
    settingsData: SettingsData?,
    updateMeasureGraph: (Int) -> Unit
) {


    val items = listOf(10, 20, 30, 40, 50, 100)
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedValue by rememberSaveable(settingsData) {
        mutableIntStateOf(settingsData?.numberMeasureGraph ?: items[0])
    }
    val iconDropdown = when (expanded) {
        true -> R.drawable.baseline_arrow_drop_up_24
        false -> R.drawable.baseline_arrow_drop_down_24
    }
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(50.dp)
    ) {
        Text(
            text = stringResource(R.string.message_number_items_graph),
            modifier = Modifier.weight(3f),
            fontSize = 12.sp
        )

        Box(
            modifier = Modifier.weight(2f)
        ) {

            OutlinedTextField(
                value = selectedValue.toString(),
                onValueChange = {},
                enabled = false,
                modifier = Modifier.clickable { expanded = true },
                textStyle = TextStyle(
                    textAlign = TextAlign.End
                ),
                leadingIcon = {

                    Icon(
                        imageVector = ImageVector.vectorResource(iconDropdown),
                        contentDescription = null
                    )
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEach { label ->
                    DropdownMenuItem(
                        text = { Text(text = label.toString()) },
                        onClick = {
                            selectedValue = label
                            expanded = false
                            updateMeasureGraph(label)
                        },
                    )
                }
            }
        }
    }
}

@SimplePreview
@Composable
private fun NumberMeasureOptionPreview() {
    NumberMeasureOption(
        settingsData = SettingsData(
            numberMeasureGraph = 10
        ),
        updateMeasureGraph = {}
    )
}
