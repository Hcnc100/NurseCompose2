package com.nullpointer.nourseCompose.ui.screens.onboarding

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.R
import kotlinx.coroutines.launch

/** Full-screen first-run introduction; it never overlays the application as a dialog. */
@Composable
fun MedicationIntroDialog(onComplete: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.SpaceBetween) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                    when (page) {
                        0 -> IntroPage(stringResource(R.string.intro_welcome_title), stringResource(R.string.intro_welcome_body))
                        1 -> {
                            IntroPage(stringResource(R.string.intro_permissions_title), stringResource(R.string.intro_permissions_body))
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) { Text(stringResource(R.string.intro_allow_notifications)) }
                            TextButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            }) { Text(stringResource(R.string.intro_exact_alarms)) }
                            TextButton(onClick = {
                                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                            }) { Text(stringResource(R.string.intro_notification_settings)) }
                            Text(stringResource(R.string.intro_full_screen_permission), style = MaterialTheme.typography.bodySmall)
                        }
                        else -> IntroPage(stringResource(R.string.intro_disclaimer_title), stringResource(R.string.medication_report_disclaimer))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(3) { index -> Box(Modifier.padding(4.dp).size(if (index == pagerState.currentPage) 10.dp else 7.dp).background(if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = .25f), CircleShape)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onComplete) { Text(stringResource(R.string.intro_skip)) }
                Button(onClick = { if (pagerState.currentPage == 2) onComplete() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) { Text(stringResource(if (pagerState.currentPage == 2) R.string.intro_start else R.string.intro_next)) }
            }
        }
    }
}

@Composable private fun IntroPage(title: String, body: String) { Column { Text(title, style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(20.dp)); Text(body, style = MaterialTheme.typography.bodyLarge) } }
