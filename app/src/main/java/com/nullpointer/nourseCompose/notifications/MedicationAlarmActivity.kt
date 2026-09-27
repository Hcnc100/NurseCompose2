package com.nullpointer.nourseCompose.notifications

import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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
    private var alarmRingtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        alarmRingtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.isLooping = true
            it.play()
        }
        val id = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)
        val name = intent.getStringExtra("reminder_name").orEmpty()
        val dosage = intent.getStringExtra("reminder_dosage").orEmpty()
        val photo = intent.getStringExtra("reminder_photo")
        logEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED, "type=MEDICATION; fullScreenActivityOpened=true; showWhenLocked=true; turnScreenOn=true")
        setContent {
            val context = LocalContext.current
            val bitmap = photo?.let {
                runCatching { contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream) }.getOrNull()
            }
            val pulse by rememberInfiniteTransition(label = "alarm_pulse").animateFloat(
                0.94f,
                1.06f,
                infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "alarm_scale",
            )
            val clockComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.clock))
            val clockProgress by animateLottieCompositionAsState(
                composition = clockComposition,
                iterations = LottieConstants.IterateForever,
            )
            MaterialTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF4A1030), Color(0xFF241021), Color(0xFF120C16)),
                            ),
                        )
                        .padding(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            color = Color.White.copy(alpha = 0.15f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_alarm_24),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = getString(R.string.title_alarm_now),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.92f),
                        )
                        Spacer(Modifier.height(22.dp))
                        Surface(
                            modifier = Modifier
                                .size(196.dp * pulse)
                                .border(3.dp, Color(0xFFFF7A9C), CircleShape),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.10f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(176.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    LottieAnimation(
                                        composition = clockComposition,
                                        progress = { clockProgress },
                                        modifier = Modifier.size(150.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text(
                            text = name.ifBlank { getString(R.string.title_alarm_now) },
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                        )
                        if (dosage.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.14f),
                            ) {
                                Text(
                                    text = dosage,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = Color.White.copy(alpha = 0.88f),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                        Spacer(Modifier.height(36.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                logEvent(AlarmLogEvent.ALARM_DISMISSED, "Alarm marked as taken")
                                NotificationManagerCompat.from(context).cancel(id.toInt())
                                finishAndRemoveTask()
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_alarm_24),
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(getString(R.string.action_taken_alarm))
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                logEvent(AlarmLogEvent.ALARM_SNOOZED, "Alarm snoozed for 10 minutes")
                                sendBroadcast(
                                    Intent(this@MedicationAlarmActivity, MedicationReminderReceiver::class.java)
                                        .setAction(MedicationReminderScheduler.ACTION_SNOOZE)
                                        .putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, id),
                                )
                                finishAndRemoveTask()
                            },
                        ) {
                            Text(getString(R.string.action_snooze_alarm))
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        alarmRingtone?.stop()
        alarmRingtone = null
        super.onDestroy()
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
