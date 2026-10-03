package com.nullpointer.nourseCompose.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.screens.settings.components.AppbarSettings
import com.nullpointer.nourseCompose.ui.screens.settings.components.NumberMeasureOption
import com.nullpointer.nourseCompose.ui.screens.settings.viewModel.SettingsViewModel
import com.nullpointer.nourseCompose.ui.screens.home.viewModel.HomeViewModel
import com.nullpointer.nourseCompose.ui.screens.destinations.DiagnosticsScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.AlarmLogScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@RootNavGraph
@Destination
@Composable
fun SettingsScreen(
    destinationsNavigator: DestinationsNavigator,
    settingsViewModel: SettingsViewModel,
    dataViewModel: HomeViewModel = hiltViewModel(),
) {
    val settingsData by settingsViewModel.settingsData.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var confirmation by rememberSaveable { mutableStateOf<String?>(null) }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.openInputStream(it) }
                .onSuccess { input -> if (input == null) dataViewModel.reportImportError() else dataViewModel.importMeasureDatabase(input) }
                .onFailure { dataViewModel.reportImportError() }
        }
    }
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            runCatching { context.contentResolver.openOutputStream(it) }
                .onSuccess { output -> if (output == null) dataViewModel.reportExportError() else dataViewModel.exportMeasureDatabase(output) }
                .onFailure { dataViewModel.reportExportError() }
        }
    }
    LaunchedEffect(dataViewModel) { dataViewModel.message.collect { snackbar.showSnackbar(context.getString(it)) } }
    Scaffold(
        topBar = { AppbarSettings(destinationsNavigator::popBackStack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsHeading(stringResource(R.string.settings_display))
            NumberMeasureOption(settingsData = settingsData,
                updateMeasureGraph = settingsViewModel::updateNumberMeasureGraph)
            HorizontalDivider()
            SettingsHeading(stringResource(R.string.settings_data_files))
            Text(stringResource(R.string.settings_csv_scope), style = MaterialTheme.typography.bodyMedium)
            SettingsAction(R.string.settings_import_records, R.drawable.outline_upload_24, enabled = !dataViewModel.isLoading) {
                confirmation = "import"
            }
            SettingsAction(R.string.settings_export_records, R.drawable.outline_download_24, enabled = !dataViewModel.isLoading) {
                exportFile.launch("nurseapp_measurements_${System.currentTimeMillis()}.csv")
            }
            HorizontalDivider()
            SettingsHeading(stringResource(R.string.settings_help_support))
            Text(stringResource(R.string.settings_support_description), style = MaterialTheme.typography.bodyMedium)
            SettingsAction(R.string.title_diagnostics, R.drawable.baseline_build_24) {
                destinationsNavigator.navigate(DiagnosticsScreenDestination)
            }
            SettingsAction(R.string.title_technical_alarm_log, R.drawable.baseline_history_24) {
                destinationsNavigator.navigate(AlarmLogScreenDestination(technical = true))
            }
            HorizontalDivider()
            SettingsHeading(stringResource(R.string.settings_delete_section))
            SettingsAction(R.string.settings_delete_measurements, R.drawable.baseline_delete_24,
                destructive = true, enabled = !dataViewModel.isLoading) { confirmation = "delete" }
            if (dataViewModel.isLoading) CircularProgressIndicator()
        }
    }
    if (confirmation != null) {
        val deleting = confirmation == "delete"
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = { Text(stringResource(if (deleting) R.string.settings_delete_measurements else R.string.settings_import_records)) },
            text = { Text(stringResource(if (deleting) R.string.settings_delete_scope else R.string.message_import_data)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = null
                    if (deleting) dataViewModel.deleterAllData() else importFile.launch("*/*")
                }) { Text(stringResource(if (deleting) R.string.settings_delete_measurements else R.string.settings_import_records)) }
            },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text(stringResource(R.string.message_cancel_dialog)) } },
        )
    }
}

@Composable
private fun SettingsHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { heading() })
}

@Composable
private fun SettingsAction(title: Int, icon: Int, destructive: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(enabled = enabled, onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        headlineContent = { Text(stringResource(title), color = color) },
        leadingContent = { Icon(painterResource(icon), contentDescription = null, tint = color) },
    )
}



