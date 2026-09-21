package com.nullpointer.nourseCompose.ui.screens.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.combine

@Destination
@HomeGraph
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    destinationsNavigator: DestinationsNavigator,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val count by viewModel.totalCount.collectAsState(initial = 0)
    var includeReminders by remember { mutableStateOf(true) }
    var selectedTypes by remember { mutableStateOf(MeasureType.entries.toSet()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { output -> viewModel.writePdf(selectedTypes, includeReminders, output) } }
    }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.title_data_export)) }, navigationIcon = { IconButton(onClick = { destinationsNavigator.popBackStack() }) { Icon(painterResource(R.drawable.baseline_arrow_back_24), stringResource(R.string.action_back)) } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.message_data_export_disclaimer), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.message_data_export_contents, count), style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = includeReminders, onCheckedChange = { includeReminders = it })
                Text(stringResource(R.string.export_include_reminders))
            }
            MeasureType.entries.forEach { type ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = type in selectedTypes, onCheckedChange = { checked -> selectedTypes = if (checked) selectedTypes + type else selectedTypes - type })
                    Text(stringResource(type.titleMeasure))
                }
            }
            Button(enabled = includeReminders || selectedTypes.isNotEmpty(), onClick = { launcher.launch("nurseapp_health_data.pdf") }) { Text(stringResource(R.string.action_export_all_pdf)) }
        }
    }
}
