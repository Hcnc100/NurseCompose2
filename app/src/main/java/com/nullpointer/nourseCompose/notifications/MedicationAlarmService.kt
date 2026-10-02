package com.nullpointer.nourseCompose.notifications

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Owns ringing independently of the alarm Activity. Only one occurrence rings at a time. */
@AndroidEntryPoint
class MedicationAlarmService : Service() {
    @Inject lateinit var scheduler: MedicationReminderScheduler
    @Inject lateinit var alarmLogRepository: AlarmLogRepository
    private var reminderName = ""
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var reminderId = -1L
    private var activeNotification: Notification? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { log("ALARM_RINGING_TIMEOUT", "Maximum ringing duration reached: 5 minutes"); stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) { stopSelf(); return START_NOT_STICKY }
        val id = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)
        if (intent.action == STOP || intent.action == SNOOZE || intent.action == DISMISS_NOTIFICATION) {
            // Superseded reminders retain independent controls. Acting on one must not
            // silence the different occurrence currently owned by this service.
            if (id >= 0 && id != reminderId && reminderId >= 0) {
                if (intent.action == SNOOZE) scheduler.snooze(id)
                NotificationManagerCompat.from(this).cancel(id.toInt())
                log("ALARM_RINGING_STOPPED", "action=${intent.action}; anotherReminderStillRinging=$reminderId",
                    id = id, name = intent.getStringExtra("reminder_name").orEmpty())
                return START_NOT_STICKY
            }
            if (id == reminderId || reminderId == -1L) {
                if (reminderId == -1L) {
                    reminderId = id
                    reminderName = intent.getStringExtra("reminder_name").orEmpty()
                }
                if (intent.action == SNOOZE) scheduler.snooze(id)
                log("ALARM_RINGING_STOPPED", "action=${intent.action}")
                stopSelf()
            }
            return START_NOT_STICKY
        }
        val notification = intent.getParcelableExtra<Notification>("notification")
        if (notification == null || id < 0) { stopSelf(); return START_NOT_STICKY }
        val previousId = reminderId
        val previousNotification = activeNotification
        releaseRinging()
        if (previousId >= 0 && previousId != id) {
            log("ALARM_RINGING_REPLACED", "Audio transferred to reminderId=$id; previous reminder controls retained")
        }
        reminderId = id
        reminderName = intent.getStringExtra("reminder_name").orEmpty()
        activeNotification = notification
        startForeground(id.toInt(), notification)
        if (previousId >= 0 && previousId != id && previousNotification != null) {
            val retained = androidx.core.app.NotificationCompat.Builder(this, previousNotification)
                .setOngoing(false).setOnlyAlertOnce(true).setFullScreenIntent(null, false).build()
            retained.flags = retained.flags and Notification.FLAG_FOREGROUND_SERVICE.inv()
            try {
                NotificationManagerCompat.from(this).notify(previousId.toInt(), retained)
            } catch (denied: SecurityException) {
                log("ALARM_NOTIFICATION_PERMISSION_DENIED", "Could not retain previous alarm controls", id = previousId)
            }
        }
        if (intent.getBooleanExtra("sound", true)) {
            val candidate = MediaPlayer()
            player = candidate
            runCatching {
                candidate.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                candidate.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                candidate.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
                candidate.isLooping = true
                candidate.prepare()
                candidate.start()
            }.onFailure { candidate.release(); player = null; log("ALARM_AUDIO_FAILED", "${it.javaClass.simpleName}: ${it.message}", false) }
        }
        if (intent.getBooleanExtra("vibrate", true)) {
            vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
        }
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, 5 * 60 * 1000L)
        log("ALARM_RINGING_STARTED", "audioPlaying=${player?.isPlaying == true}; vibrationRequested=${intent.getBooleanExtra("vibrate", true)}; timeoutMinutes=5")
        return START_NOT_STICKY
    }

    private fun releaseRinging() {
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun log(event: String, details: String, success: Boolean = true,
        id: Long = reminderId, name: String = reminderName) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmLogRepository.record(AlarmLogEntity(reminderId = id, reminderName = name,
                eventType = event, success = success, details = details,
                severity = if (success) "INFO" else "ERROR"))
        }
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        releaseRinging()
        stopForeground(true)
        super.onDestroy()
    }

    companion object {
        const val STOP = "com.nullpointer.nourseCompose.STOP_RINGING"
        const val DISMISS_NOTIFICATION = "com.nullpointer.nourseCompose.DISMISS_ALARM_NOTIFICATION"
        const val SNOOZE = "com.nullpointer.nourseCompose.SNOOZE_RINGING"
    }
}
