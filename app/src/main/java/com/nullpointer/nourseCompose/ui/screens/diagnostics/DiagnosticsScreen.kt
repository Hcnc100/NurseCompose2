package com.nullpointer.nourseCompose.ui.screens.diagnostics

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.share.AppTopBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph
@Destination
@Composable
fun DiagnosticsScreen(
    destinationsNavigator: DestinationsNavigator,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.exportedReport.collect { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.title_diagnostics))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, context.getString(R.string.action_share_diagnostics)))
        }
    }

    val errorCount = logs.count { it.severity == "ERROR" }
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_diagnostics),
                onBack = { destinationsNavigator.popBackStack() },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.message_diagnostics_description), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.label_diagnostics_count, logs.size, errorCount), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = viewModel::exportLogs, enabled = logs.isNotEmpty(), shape = MaterialTheme.shapes.medium) { Text(stringResource(R.string.action_export_diagnostics)) }
            Text(stringResource(R.string.message_diagnostics_privacy), style = MaterialTheme.typography.bodySmall)
        }
    }
}
