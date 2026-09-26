package com.nullpointer.nourseCompose.notifications

import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicationAlarmActivity : ComponentActivity() {
    @Inject lateinit var alarmLogRepository: AlarmLogRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val id = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)
        val name = intent.getStringExtra("reminder_name").orEmpty()
        val dosage = intent.getStringExtra("reminder_dosage").orEmpty()
        val photo = intent.getStringExtra("reminder_photo")
        setContent {
            val context = LocalContext.current
            val bitmap = photo?.let { runCatching { contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream) }.getOrNull() }
            val pulse by rememberInfiniteTransition(label = "alarm_pulse").animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alarm_scale")
            MaterialTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer), contentAlignment = Alignment.Center) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(96.dp * pulse).background(MaterialTheme.colorScheme.error, CircleShape))
                        Text(getString(R.string.title_alarm_now), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(name, style = MaterialTheme.typography.headlineSmall)
                        if (dosage.isNotBlank()) Text(dosage)
                        bitmap?.let { Image(it.asImageBitmap(), null, Modifier.height(150.dp), contentScale = ContentScale.Crop) }
                        Button(onClick = { logEvent(AlarmLogEvent.ALARM_DISMISSED, "Alarm marked as taken"); NotificationManagerCompat.from(context).cancel(id.toInt()); finishAndRemoveTask() }) { Text(getString(R.string.action_taken_alarm)) }
                        Button(onClick = { logEvent(AlarmLogEvent.ALARM_SNOOZED, "Alarm snoozed for 10 minutes"); sendBroadcast(Intent(this@MedicationAlarmActivity, MedicationReminderReceiver::class.java).setAction(MedicationReminderScheduler.ACTION_SNOOZE).putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, id)); finishAndRemoveTask() }) { Text(getString(R.string.action_snooze_alarm)) }
                    }
                }
            }
        }
    }

    private fun logEvent(eventType: String, details: String) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmLogRepository.record(
                AlarmLogEntity(
                    reminderId = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1),
                    reminderName = intent.getStringExtra("reminder_name").orEmpty(),
                    eventType = eventType,
                    success = true,
                    details = details,
                )
            )
        }
    }
}
