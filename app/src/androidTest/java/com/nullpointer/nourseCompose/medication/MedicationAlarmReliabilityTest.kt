package com.nullpointer.nourseCompose.medication

import android.Manifest
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.datasource.medication.local.MedicationReminderLocalDataSourceImpl
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepoImpl
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepoImpl
import com.nullpointer.nourseCompose.inject.database.MeasureDatabaseModule
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.notifications.MedicationAlarmActivity
import com.nullpointer.nourseCompose.notifications.MedicationAlarmService
import com.nullpointer.nourseCompose.notifications.MedicationReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

/** Run only on disposable QA emulators. Long cases use real elapsed time, not clock jumps. */
@RunWith(AndroidJUnit4::class)
class MedicationAlarmReliabilityTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val database by lazy { MeasureDatabaseModule.provideNurseDatabase(context, MeasureDatabaseModule.provideNameDatabase()) }
    private val reminders by lazy { MedicationReminderRepoImpl(MedicationReminderLocalDataSourceImpl(database.getMedicationReminderDao())) }
    private val logs by lazy { AlarmLogRepoImpl(database.getAlarmLogDao()) }
    private val scheduler by lazy { MedicationReminderScheduler(context, logs) }
    private val notifications get() = context.getSystemService(NotificationManager::class.java)
    private val alarms get() = context.getSystemService(AlarmManager::class.java)
    private val fixtures = mutableListOf<MedicationReminderEntity>()

    @Before fun prepare() {
        assumeTrue("Opt-in suite: use -e reliabilitySuite true on a disposable emulator",
            InstrumentationRegistry.getArguments().getString("reliabilitySuite") == "true")
        instrumentation.runOnMainSync {
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        SystemClock.sleep(2_000)
    }

    @After fun cleanUp() {
        shell("cmd deviceidle unforce")
        shell("dumpsys battery reset")
        shell("input keyevent 224")
        shell("wm dismiss-keyguard")
        context.stopService(Intent(context, MedicationAlarmService::class.java))
        instrumentation.runOnMainSync {
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            listOf(Stage.CREATED, Stage.STARTED, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED)
                .flatMap { monitor.getActivitiesInStage(it).toList() }.distinct()
                .filter { it is MedicationAlarmActivity || it is MainActivity }.forEach { it.finish() }
        }
        fixtures.forEach { reminder ->
            scheduler.cancel(reminder.id)
            notifications.cancel(reminder.id.toInt())
            runBlocking {
                reminders.delete(reminder)
                database.openHelper.writableDatabase.execSQL(
                    "DELETE FROM alarm_logs WHERE reminderId = ? AND reminderName = ?",
                    arrayOf(reminder.id, reminder.name),
                )
            }
        }
        database.close()
    }

    @Test fun deniedNotificationsAreReportedWithoutRinging() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        assertEquals(PackageManager.PERMISSION_DENIED, context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
        val reminder = fixture()
        scheduler.schedule(reminder)
        await("Denied notification recorded", 30_000) {
            events(reminder).any { it.eventType == AlarmLogEvent.ALARM_FAILED &&
                !it.success && it.details.orEmpty().contains("POST_NOTIFICATIONS denied") }
        }
        assertTrue(events(reminder).none { it.eventType == "ALARM_RINGING_STARTED" })
        assertTrue(notifications.activeNotifications.none { it.id == reminder.id.toInt() })
    }

    @Test fun deniedExactAlarmUsesExplicitInexactFallback() {
        assertFalse("Runner must deny exact alarm access first", alarms.canScheduleExactAlarms())
        val reminder = fixture(delay = 600_000)
        scheduler.schedule(reminder)
        await("Inexact fallback recorded") {
            events(reminder).any { it.eventType == AlarmLogEvent.ALARM_SCHEDULED &&
                it.details.orEmpty().contains("exact=false; exactPermission=false") }
        }
        // This checks scheduling/fallback, not a guarantee of punctual delivery without permission.
        assertNull(alarms.nextAlarmClock)
    }

    @Test fun blockedChannelDoesNotReportSuccessfulDeliveryOrRing() {
        assumeTrue(Build.VERSION.SDK_INT >= 26)
        // Dedicated QA emulator only: this channel is intentionally blocked.
        val reminder = fixture().copy(soundEnabled = false, vibrationEnabled = false)
        val channelId = "medication_silent_still_alarm"
        notifications.createNotificationChannel(NotificationChannel(channelId, "QA blocked", NotificationManager.IMPORTANCE_NONE))
        val result = scheduler.showNotification(reminder)
        assertFalse("Blocked channel must not be reported as posted", result.posted)
        assertTrue(result.details.contains("channel blocked"))
        SystemClock.sleep(1_000)
        assertTrue(events(reminder).none { it.eventType == "ALARM_RINGING_STARTED" })
    }

    @Test fun simultaneousAlarmsKeepBothReminderControls() {
        val first = fixture(delay = 5_000)
        val second = fixture(delay = 5_000)
        scheduler.schedule(first)
        scheduler.schedule(second)
        await("Both alarms delivered", 35_000) {
            listOf(first, second).all { r -> events(r).any { it.eventType == "ALARM_RINGING_STARTED" } }
        }
        assertTrue("First reminder controls must not disappear",
            notifications.activeNotifications.any { it.id == first.id.toInt() })
        assertTrue("Second reminder controls must remain",
            notifications.activeNotifications.any { it.id == second.id.toInt() })
        // Snooze the superseded occurrence; it must not stop the currently ringing occurrence.
        val current = listOf(first, second).maxBy { r ->
            events(r).first { it.eventType == "ALARM_RINGING_STARTED" }.occurredAt
        }
        val earlier = if (current.id == first.id) second else first
        val notification = notifications.activeNotifications.first { it.id == earlier.id.toInt() }
        notification.notification.actions.last().actionIntent.send()
        await("Earlier notification closes on Snooze") {
            notifications.activeNotifications.none { it.id == earlier.id.toInt() }
        }
        assertTrue("Other reminder still has ringing controls",
            notifications.activeNotifications.any { it.id == current.id.toInt() })
    }

    @Test(timeout = 360_000) fun ringingStopsAfterRealFiveMinuteTimeout() {
        val reminder = fixture()
        scheduler.schedule(reminder)
        await("Ringing started", 30_000) { events(reminder).any { it.eventType == "ALARM_RINGING_STARTED" } }
        await("Real five-minute timeout", 320_000) { events(reminder).any { it.eventType == "ALARM_RINGING_TIMEOUT" } }
        await("Foreground notification removed") {
            notifications.activeNotifications.none { it.id == reminder.id.toInt() }
        }
    }

    @Test(timeout = 960_000) fun snoozeIsDeliveredAfterRealTenMinutes() {
        val reminder = fixture()
        scheduler.schedule(reminder)
        await("First occurrence rings", 30_000) { events(reminder).count { it.eventType == "ALARM_RINGING_STARTED" } == 1 }
        val notification = notifications.activeNotifications.first { it.id == reminder.id.toInt() }
        val snoozeAt = SystemClock.elapsedRealtime()
        notification.notification.actions.last().actionIntent.send()
        await("Snooze stops ringing") { events(reminder).any { it.eventType == "ALARM_RINGING_STOPPED" } }
        shell("input keyevent 3")
        shell("input keyevent 223")
        await("Real postponed occurrence delivered", 900_000) {
            events(reminder).count { it.eventType == "ALARM_RINGING_STARTED" } >= 2
        }
        val elapsed = SystemClock.elapsedRealtime() - snoozeAt
        assertTrue("Snooze must not fire early: $elapsed", elapsed >= 590_000)
        assertTrue("Snooze should arrive near ten minutes with exact access: $elapsed", elapsed <= 660_000)
    }

    @Test fun fullScreenDeliverySurvivesForcedDoze() {
        val reminder = fixture(delay = 15_000)
        scheduler.schedule(reminder)
        shell("input keyevent 3")
        shell("dumpsys battery unplug")
        shell("input keyevent 223")
        shell("cmd deviceidle force-idle")
        await("Alarm delivered despite forced idle", 60_000) {
            events(reminder).any { it.eventType == "ALARM_RINGING_STARTED" }
        }
    }

    @Test fun deniedFullScreenAccessKeepsNotificationAndSoundWithoutOpeningActivity() {
        assumeTrue(Build.VERSION.SDK_INT >= 34)
        assertFalse("Runner must deny full-screen access", notifications.canUseFullScreenIntent())
        val reminder = fixture(delay = 10_000)
        scheduler.schedule(reminder)
        shell("input keyevent 3")
        shell("input keyevent 223")
        await("Fallback alarm rings", 45_000) {
            events(reminder).any { it.eventType == "ALARM_RINGING_STARTED" }
        }
        assertTrue(notifications.activeNotifications.any { it.id == reminder.id.toInt() })
        SystemClock.sleep(2_000)
        assertTrue("Denied access must not open a background full-screen activity",
            events(reminder).none { it.eventType == AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED })
    }

    @Test fun fullScreenAlarmIsVisibleOverSecureKeyguard() {
        val keyguard = context.getSystemService(KeyguardManager::class.java)
        assertTrue("Runner must configure a PIN on its disposable AVD", keyguard.isDeviceSecure)
        val reminder = fixture(delay = 10_000)
        scheduler.schedule(reminder)
        shell("input keyevent 3")
        shell("input keyevent 223")
        await("Alarm activity opens over secure keyguard", 45_000) {
            events(reminder).any { it.eventType == AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED }
        }
        assertTrue("Alarm must not unlock the protected device", keyguard.isKeyguardLocked)
        assertTrue(events(reminder).any { it.eventType == "ALARM_RINGING_STARTED" })
    }

    @Test fun prepareFixtureForRealReboot() {
        val reminder = fixture(delay = 180_000)
        scheduler.schedule(reminder)
        await("Initial schedule persisted") {
            events(reminder).any { it.eventType == AlarmLogEvent.ALARM_SCHEDULED }
        }
        assertTrue(context.getSharedPreferences("qa_alarm_reliability", 0).edit()
            .putLong("reboot_fixture", reminder.id).commit())
        // Intentionally leave this sole QA fixture for the host's actual adb reboot.
        fixtures.clear()
    }

    @Test fun recoverAbandonedReliabilityFixtures() {
        fixtures.addAll(runBlocking {
            reminders.observeAll().first().filter { it.name.startsWith("__QA_RELIABILITY__") }
        })
        context.getSharedPreferences("qa_alarm_reliability", 0).edit().clear().commit()
    }

    @Test(timeout = 300_000) fun verifyFixtureAfterRealReboot() {
        val id = context.getSharedPreferences("qa_alarm_reliability", 0).getLong("reboot_fixture", -1)
        val reminder = runBlocking { reminders.observeAll().first().single { it.id == id } }
        assertTrue(reminder.name.startsWith("__QA_RELIABILITY__"))
        fixtures.add(reminder)
        await("BOOT_COMPLETED restored scheduling", 30_000) {
            events(reminder).count { it.eventType == AlarmLogEvent.ALARM_SCHEDULED } >= 2
        }
        await("Restored alarm actually rings after reboot", 240_000) {
            events(reminder).any { it.eventType == "ALARM_RINGING_STARTED" }
        }
        context.getSharedPreferences("qa_alarm_reliability", 0).edit().remove("reboot_fixture").commit()
    }

    private fun fixture(delay: Long = 5_000): MedicationReminderEntity = runBlocking {
        val candidate = MedicationReminderEntity(name = "__QA_RELIABILITY__ ${System.nanoTime()}",
            dosage = "PRUEBA / TEST", startAt = System.currentTimeMillis() + delay, intervalHours = 24,
            fullScreenAlarm = true, soundEnabled = true, vibrationEnabled = true)
        candidate.copy(id = reminders.add(candidate)).also { fixtures.add(it) }
    }
    private fun events(reminder: MedicationReminderEntity) = runBlocking {
        logs.observeAll().first().filter { it.reminderId == reminder.id }
    }
    private fun await(message: String, timeout: Long = 20_000, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(500)
        }
        fail(message)
    }
    private fun shell(command: String) = instrumentation.uiAutomation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().use { reader -> reader.readText() }
    }
}
