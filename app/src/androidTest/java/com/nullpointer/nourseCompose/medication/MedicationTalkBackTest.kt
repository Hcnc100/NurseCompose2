package com.nullpointer.nourseCompose.medication

import android.app.NotificationManager
import android.app.UiAutomation
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.R
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
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/** Actual TalkBack gestures; never substitute AccessibilityNodeInfo ACTION_CLICK for activation. */
@RunWith(AndroidJUnit4::class)
class MedicationTalkBackTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val automation by lazy {
        instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES or UiAutomation.FLAG_DONT_USE_ACCESSIBILITY)
    }
    private val database by lazy { MeasureDatabaseModule.provideNurseDatabase(context, MeasureDatabaseModule.provideNameDatabase()) }
    private val reminders by lazy { MedicationReminderRepoImpl(MedicationReminderLocalDataSourceImpl(database.getMedicationReminderDao())) }
    private val logs by lazy { AlarmLogRepoImpl(database.getAlarmLogDao()) }
    private val scheduler by lazy { MedicationReminderScheduler(context, logs) }
    private val focused = CopyOnWriteArrayList<String>()
    private var reminder: MedicationReminderEntity? = null

    @Before fun prepare() {
        assumeTrue("Opt-in with actual TalkBack enabled", InstrumentationRegistry.getArguments().getString("talkBackSuite") == "true")
        val manager = context.getSystemService(AccessibilityManager::class.java)
        assertTrue("TalkBack must actually be running", manager.getEnabledAccessibilityServiceList(-1)
            .any { it.id.contains("com.google.android.marvin.talkback") })
        assertTrue("Touch exploration must be enabled", manager.isTouchExplorationEnabled)
        instrumentation.runOnMainSync {
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        SystemClock.sleep(2_000)
        reminder = runBlocking {
            val candidate = MedicationReminderEntity(name = "__QA_TALKBACK__ ${System.nanoTime()}",
                dosage = "PRUEBA / TEST", startAt = System.currentTimeMillis() + 5_000,
                intervalHours = 24, soundEnabled = true, vibrationEnabled = true, fullScreenAlarm = true)
            candidate.copy(id = reminders.add(candidate))
        }
        scheduler.schedule(reminder!!)
        await("Real alarm must open") { eventExists(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Alarm must be ringing during TalkBack navigation") { eventExists("ALARM_RINGING_STARTED") }
        instrumentation.runOnMainSync {
            val activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<MedicationAlarmActivity>().single()
            activity.window.decorView.accessibilityDelegate = object : android.view.View.AccessibilityDelegate() {
                override fun onRequestSendAccessibilityEvent(host: android.view.ViewGroup, child: android.view.View, event: AccessibilityEvent): Boolean {
                    if (event.eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                        val label = event.text.joinToString(" ") + " " + event.contentDescription?.toString().orEmpty()
                        if (label.isNotBlank()) focused.add(label)
                    }
                    return super.onRequestSendAccessibilityEvent(host, child, event)
                }
            }
        }
        val evidence = context.getExternalFilesDir("qa-talkback")!!.apply { mkdirs() }
        File(evidence, "external-ready.txt").writeText(reminder!!.id.toString())
    }

    @After fun cleanUp() {
        if (reminder == null) return
        context.stopService(Intent(context, MedicationAlarmService::class.java))
        scheduler.cancel(reminder!!.id)
        context.getSystemService(NotificationManager::class.java).cancel(reminder!!.id.toInt())
        instrumentation.runOnMainSync {
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            listOf(Stage.RESUMED, Stage.PAUSED, Stage.STOPPED).flatMap { monitor.getActivitiesInStage(it).toList() }
                .distinct().filter { it is MedicationAlarmActivity || it is MainActivity }.forEach { it.finish() }
        }
        runBlocking {
            reminders.delete(reminder!!)
            database.openHelper.writableDatabase.execSQL(
                "DELETE FROM alarm_logs WHERE reminderId = ? AND reminderName = ?",
                arrayOf(reminder!!.id, reminder!!.name),
            )
        }
        database.close()
    }

    @Test fun swipeAndDoubleTapCanMarkTaken() {
        navigateAndDoubleTap(context.getString(R.string.action_taken_alarm), "taken")
        await("TalkBack gesture marks the reminder as taken") { eventExists(AlarmLogEvent.ALARM_DISMISSED) }
    }

    @Test fun swipeAndDoubleTapCanSnooze() {
        navigateAndDoubleTap(context.getString(R.string.action_snooze_alarm), "snooze")
        await("TalkBack gesture snoozes the ringing occurrence") { eventExists("ALARM_RINGING_STOPPED", MedicationAlarmService.SNOOZE) }
    }

    @Test fun keyboardCanMarkTaken() {
        navigateAndDoubleTap(context.getString(R.string.action_taken_alarm), "keyboard-taken", keyboard = true)
        await("TalkBack keyboard activation marks the reminder as taken") { eventExists(AlarmLogEvent.ALARM_DISMISSED) }
    }

    @Test fun keyboardCanSnooze() {
        navigateAndDoubleTap(context.getString(R.string.action_snooze_alarm), "keyboard-snooze", keyboard = true)
        await("TalkBack keyboard activation snoozes the occurrence") { eventExists("ALARM_RINGING_STOPPED", MedicationAlarmService.SNOOZE) }
    }

    @Test fun externalKeyboardCanMarkTaken() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("externalTalkBack") == "true")
        await("External TalkBack input must mark the ringing reminder as taken", timeout = 120_000) {
            eventExists(AlarmLogEvent.ALARM_DISMISSED)
        }
    }

    @Test fun externalKeyboardCanSnooze() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("externalTalkBack") == "true")
        await("External TalkBack input must snooze the ringing reminder", timeout = 120_000) {
            eventExists("ALARM_RINGING_STOPPED", MedicationAlarmService.SNOOZE)
        }
    }

    private fun navigateAndDoubleTap(label: String, evidenceName: String, keyboard: Boolean = false) {
        focused.clear()
        repeat(24) {
            readerInput(if (keyboard) "input keycombination 57 22" else "input swipe 250 1100 800 1100 80")
            SystemClock.sleep(600)
            if (focused.lastOrNull()?.contains(label) == true) {
                val directory = context.getExternalFilesDir("qa-talkback")!!.apply { mkdirs() }
                File(directory, "$evidenceName-focus.txt").writeText(focused.joinToString("\n"))
                automation.takeScreenshot()?.let { bitmap ->
                    File(directory, "$evidenceName.png").outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
                // Double-tap is interpreted by TalkBack, not a direct accessibility click.
                readerInput(if (keyboard) "input keycombination 57 66" else "input tap 500 1200; input tap 500 1200")
                return
            }
        }
        fail("TalkBack focus never reached $label; observed=$focused")
    }

    private fun readerInput(command: String) {
        // Observe the app's outgoing focus events without registering an extra
        // accessibility service that competes with the actual reader for input.
        shell(command)
    }

    private fun eventExists(type: String, detail: String? = null) = runBlocking {
        logs.observeAll().first().any { it.reminderId == reminder!!.id && it.eventType == type &&
            (detail == null || it.details.orEmpty().contains(detail)) }
    }
    private fun await(message: String, timeout: Long = 25_000, condition: () -> Boolean) {
        val end = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < end) {
            if (condition()) return
            SystemClock.sleep(250)
        }
        fail(message)
    }
    private fun shell(command: String) = automation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().use { reader -> reader.readText() }
    }
}
