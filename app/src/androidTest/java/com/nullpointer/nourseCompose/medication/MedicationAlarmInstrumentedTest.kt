package com.nullpointer.nourseCompose.medication

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.datasource.medication.local.MedicationReminderLocalDataSourceImpl
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepoImpl
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
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

/** Real AlarmManager/receiver/service tests. Only uniquely identified QA fixtures are removed. */
@RunWith(AndroidJUnit4::class)
class MedicationAlarmInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val automation get() = instrumentation.uiAutomation
    // Reuse production providers/repositories against the same DB. A test-only Hilt entry
    // point is not part of the installed production SingletonComponent.
    private val database by lazy { MeasureDatabaseModule.provideNurseDatabase(context, MeasureDatabaseModule.provideNameDatabase()) }
    private val reminders by lazy { MedicationReminderRepoImpl(MedicationReminderLocalDataSourceImpl(database.getMedicationReminderDao())) }
    private val logs by lazy { AlarmLogRepoImpl(database.getAlarmLogDao()) }
    private val scheduler by lazy { MedicationReminderScheduler(context, logs) }
    private val notifications get() = context.getSystemService(NotificationManager::class.java)
    private var fixture: MedicationReminderEntity? = null
    private var main: ActivityScenario<MainActivity>? = null

    @Before fun prepare() {
        assumeTrue("Notifications must be enabled for real delivery", notifications.areNotificationsEnabled())
        assumeTrue("Exact-alarm access required for deterministic delivery", context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms())
        main = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun cleanUp() {
        // Always recover from a failed locked-screen test without leaving a ringing QA alarm.
        shell("input keyevent 224")
        shell("wm dismiss-keyguard")
        instrumentation.runOnMainSync {
            // Screen-off and notification tests can leave MainActivity paused/stopped.
            // Finish owned activities in every live stage before closing its scenario.
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            listOf(Stage.CREATED, Stage.STARTED, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED)
                .flatMap { monitor.getActivitiesInStage(it).toList() }.distinct()
                .filter { it is MedicationAlarmActivity || it is MainActivity }
                .forEach { it.finish() }
        }
        fixture?.let { reminder ->
            scheduler.cancel(reminder.id)
            if (reminder.fullScreenAlarm &&
                notifications.activeNotifications.any { it.id == reminder.id.toInt() }) {
                context.startService(Intent(context, MedicationAlarmService::class.java)
                    .setAction(MedicationAlarmService.STOP)
                    .putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, reminder.id))
            }
            SystemClock.sleep(500)
            notifications.cancel(reminder.id.toInt())
            runBlocking {
                reminders.delete(reminder)
                database.openHelper.writableDatabase.execSQL(
                    "DELETE FROM alarm_logs WHERE reminderId = ? AND reminderName = ?",
                    arrayOf(reminder.id, reminder.name),
                )
            }
        }
        main?.close()
        if (fixture != null) database.close()
        // Font scale is configured/restored by the outer runner, before instrumentation.
    }

    @Test fun standardAlarmIsDeliveredByAlarmManager() {
        val reminder = createFixture(fullScreen = false)
        scheduler.schedule(reminder)
        await("AlarmManager delivered the receiver") { hasEvent(AlarmLogEvent.ALARM_LAUNCHED) }
        val posted = notifications.activeNotifications.single { it.id == reminder.id.toInt() }
        assertNull(posted.notification.fullScreenIntent)
        assertEquals(context.getString(R.string.notification_medication_title, reminder.name),
            posted.notification.extras.getCharSequence("android.title").toString())
        shell("cmd statusbar expand-notifications")
        SystemClock.sleep(700)
        capture("standard-notification")
        shell("cmd statusbar collapse")
    }

    @Test fun foregroundFullScreenTakenStopsRinging() {
        createFixture(fullScreen = true)
        scheduler.schedule(fixture!!)
        await("Full screen is opened by the real receiver") { hasEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Alarm service starts audio") { hasEvent("ALARM_RINGING_STARTED", "audioPlaying=true") }
        assertAccessibleActions()
        capture("full-screen")
        click(context.getString(R.string.action_taken_alarm))
        await("Taken action is recorded") { hasEvent(AlarmLogEvent.MEDICATION_TAKEN) }
        await("Ringing stops after Taken") { hasEvent("ALARM_RINGING_STOPPED", MedicationAlarmService.STOP) }
        await("Notification is removed") { notifications.activeNotifications.none { it.id == fixture!!.id.toInt() } }
    }

    @Test fun intakeSaveFailureKeepsAlarmActiveAndAllowsRetry() {
        createFixture(fullScreen = true)
        scheduler.schedule(fixture!!)
        await("Alarm opens") { hasEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Alarm rings") { hasEvent("ALARM_RINGING_STARTED") }
        val allowSave = java.util.concurrent.atomic.AtomicBoolean(false)
        instrumentation.runOnMainSync {
            val activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<MedicationAlarmActivity>().single()
            activity.alarmLogRepository = object : com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository by logs {
                override suspend fun record(log: com.nullpointer.nourseCompose.models.entity.AlarmLogEntity) {
                    if (log.eventType == AlarmLogEvent.MEDICATION_TAKEN && !allowSave.get()) {
                        throw java.io.IOException("QA simulated persistence failure")
                    }
                    logs.record(log)
                }
            }
        }
        click(context.getString(R.string.action_taken_alarm))
        await("Retry error is visible") { find(context.getString(R.string.alarm_decision_save_failed)) != null }
        assertFalse("A failed save is not a confirmed dose", hasEvent(AlarmLogEvent.MEDICATION_TAKEN))
        assertFalse("Failed save must not stop ringing", hasEvent("ALARM_RINGING_STOPPED", MedicationAlarmService.STOP))
        assertTrue("Alarm notification remains", notifications.activeNotifications.any { it.id == fixture!!.id.toInt() })
        allowSave.set(true)
        click(context.getString(R.string.action_taken_alarm))
        await("Retry stores intake") { hasEvent(AlarmLogEvent.MEDICATION_TAKEN) }
        await("Successful retry stops ringing") { hasEvent("ALARM_RINGING_STOPPED", MedicationAlarmService.STOP) }
        assertEquals("One confirmed response", 1, runBlocking {
            logs.observeAll().first().count { it.reminderId == fixture!!.id && it.eventType == AlarmLogEvent.MEDICATION_TAKEN }
        })
    }

    @Test fun notTakenRequiresConfirmationAndDoesNotDisableFutureReminders() {
        createFixture(fullScreen = true)
        scheduler.schedule(fixture!!)
        await("Alarm opens") { hasEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Alarm rings") { hasEvent("ALARM_RINGING_STARTED") }
        assertAccessibleActions()
        click(context.getString(R.string.action_not_taken_alarm))
        await("Not taken confirmation opens") { find(context.getString(R.string.alarm_not_taken_confirm)) != null }
        capture("not-taken-confirmation")
        click(context.getString(R.string.message_cancel_dialog))
        assertFalse("Cancel must not record a decision", hasEvent(AlarmLogEvent.MEDICATION_NOT_TAKEN))
        assertTrue("Cancel keeps notification", notifications.activeNotifications.any { it.id == fixture!!.id.toInt() })
        click(context.getString(R.string.action_not_taken_alarm))
        await("Confirmation opens again") { find(context.getString(R.string.alarm_not_taken_confirm)) != null }
        click(context.getString(R.string.alarm_not_taken_confirm))
        await("Explicit Not taken is stored") { hasEvent(AlarmLogEvent.MEDICATION_NOT_TAKEN) }
        await("Current ringing stops") { hasEvent("ALARM_RINGING_STOPPED", MedicationAlarmService.STOP) }
        await("Current notification closes") { notifications.activeNotifications.none { it.id == fixture!!.id.toInt() } }
        assertFalse("Not taken must not become Taken", hasEvent(AlarmLogEvent.MEDICATION_TAKEN))
        assertTrue("Reminder remains active", runBlocking { reminders.observeActive().first().any { it.id == fixture!!.id } })
        await("Receiver scheduled the next occurrence") {
            runBlocking { logs.observeAll().first().count { it.reminderId == fixture!!.id && it.eventType == AlarmLogEvent.ALARM_SCHEDULED && it.success } >= 2 }
        }
    }

    @Test fun snoozeStopsRingingAndClosesAlarmScreen() {
        createFixture(fullScreen = true)
        scheduler.schedule(fixture!!)
        await("Full screen is opened") { hasEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Ringing started") { hasEvent("ALARM_RINGING_STARTED") }
        assertAccessibleActions()
        capture("before-snooze")
        click(context.getString(R.string.action_snooze_alarm))
        await("Snooze is recorded") { hasEvent(AlarmLogEvent.ALARM_SNOOZED) }
        await("Ringing stops after Snooze") { hasEvent("ALARM_RINGING_STOPPED", MedicationAlarmService.SNOOZE) }
        await("Alarm controls close") { find(context.getString(R.string.action_snooze_alarm)) == null }
        // The requested ten-minute alarm is canceled in cleanup; this test does not wait ten minutes.
    }

    @Test fun fullScreenIsDeliveredWithScreenOff() {
        val reminder = createFixture(fullScreen = true, delayMillis = 8_000)
        scheduler.schedule(reminder)
        shell("input keyevent 3")
        shell("input keyevent 223")
        await("Full-screen intent opens while screen is off", 25_000) { hasEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Alarm service rings") { hasEvent("ALARM_RINGING_STARTED", "audioPlaying=true") }
        assertAccessibleActions()
        capture("screen-off-full-screen")
        click(context.getString(R.string.action_taken_alarm))
        await("Ringing stops") { hasEvent("ALARM_RINGING_STOPPED") }
    }

    @Test fun backDoesNotMarkTakenAndLeavesNotificationControls() {
        createFixture(fullScreen = true)
        scheduler.schedule(fixture!!)
        await("Alarm screen opens") { hasEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED) }
        await("Ringing started") { hasEvent("ALARM_RINGING_STARTED") }
        shell("input keyevent 4")
        await("Alarm screen closes on Back") { find(context.getString(R.string.action_snooze_alarm)) == null }
        assertFalse("Back must not record a taken dose", hasEvent(AlarmLogEvent.MEDICATION_TAKEN))
        assertTrue("Notification controls must remain available", notifications.activeNotifications.any {
            it.id == fixture!!.id.toInt() && it.notification.actions?.size == 2
        })
        val actions = notifications.activeNotifications.single { it.id == fixture!!.id.toInt() }.notification.actions
        assertEquals(context.getString(R.string.action_stop_alarm), actions[0].title.toString())
        assertEquals(context.getString(R.string.action_snooze_alarm), actions[1].title.toString())
        shell("cmd statusbar expand-notifications")
        SystemClock.sleep(700)
        capture("after-back-notification")
        shell("cmd statusbar collapse")
    }

    @Test(timeout = 60_000) fun fullScreenActionsRemainReachableWithLargeTextAndLongName() {
        val reminder = createFixture(fullScreen = true, delayMillis = 86_400_000,
            name = "__QA_ALARM__ Medicamento de prueba con un nombre largo para comprobar accesibilidad")
        assumeTrue("Run this case with system font_scale >= 2.0 before instrumentation starts",
            context.resources.configuration.fontScale >= 2.0f)
        // ActivityScenario's synchronous idle wait can stall on this continuously animated
        // alarm. Start on the main thread and synchronize on observable accessibility state.
        instrumentation.runOnMainSync {
            context.startActivity(Intent(context, MedicationAlarmActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
                .putExtra("reminder_name", reminder.name)
                .putExtra("reminder_dosage", "Texto de prueba — sin dosis médica"))
        }
        SystemClock.sleep(1_000)
        assertAccessibleActions()
        val holdMillis = InstrumentationRegistry.getArguments().getString("visualHoldMillis")
            ?.toLongOrNull()?.coerceIn(0, 20_000) ?: 0
        SystemClock.sleep(holdMillis)
        capture("large-text-long-name")
        click(context.getString(R.string.action_not_taken_alarm))
        await("Confirmation remains reachable with enlarged text") {
            find(context.getString(R.string.alarm_not_taken_confirm))?.isVisibleToUser == true &&
                find(context.getString(R.string.message_cancel_dialog))?.isVisibleToUser == true
        }
        capture("large-text-not-taken-dialog")
        click(context.getString(R.string.message_cancel_dialog))
        assertFalse("Cancellation must not record Not taken", hasEvent(AlarmLogEvent.MEDICATION_NOT_TAKEN))
    }

    private fun createFixture(fullScreen: Boolean, delayMillis: Long = 4_000,
        name: String = "__QA_ALARM__ ${System.currentTimeMillis()}"): MedicationReminderEntity = runBlocking {
        val candidate = MedicationReminderEntity(name = name, dosage = "PRUEBA / TEST",
            startAt = System.currentTimeMillis() + delayMillis, intervalHours = 24,
            fullScreenAlarm = fullScreen, soundEnabled = fullScreen, vibrationEnabled = fullScreen)
        candidate.copy(id = reminders.add(candidate)).also { fixture = it }
    }

    @Test fun recoverOnlyAbandonedQaFixtures() = runBlocking {
        reminders.observeAll().first().filter { it.name.startsWith("__QA_ALARM__") }.forEach { reminder ->
            scheduler.cancel(reminder.id)
            notifications.cancel(reminder.id.toInt())
            reminders.delete(reminder)
            database.openHelper.writableDatabase.execSQL(
                "DELETE FROM alarm_logs WHERE reminderId = ? AND reminderName = ?",
                arrayOf(reminder.id, reminder.name),
            )
        }
        database.close()
    }

    private fun hasEvent(type: String, detail: String? = null): Boolean = runBlocking {
        logs.observeAll().first().any {
            it.reminderId == fixture?.id && it.eventType == type && it.success &&
                (detail == null || it.details.orEmpty().contains(detail))
        }
    }

    private fun await(message: String, timeout: Long = 15_000, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(250)
        }
        capture("failure-${message.replace(Regex("[^a-zA-Z]"), "-")}")
        fail(message)
    }

    private fun nodes(): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo?) {
            if (node == null) return
            result.add(node)
            for (i in 0 until node.childCount) visit(node.getChild(i))
        }
        visit(automation.rootInActiveWindow)
        return result
    }

    private fun find(label: String) = nodes().firstOrNull { it.text?.toString() == label || it.contentDescription?.toString() == label }

    private fun assertAccessibleActions() {
        for (label in listOf(context.getString(R.string.action_taken_alarm), context.getString(R.string.action_snooze_alarm),
            context.getString(R.string.action_not_taken_alarm))) {
            await("Accessible action exists: $label") { find(label) != null }
            var node = find(label)!!
            while (!node.isClickable && node.parent != null) node = node.parent
            assertTrue("Action must be clickable: $label", node.isClickable)
            assertTrue("Action must be visible: $label", node.isVisibleToUser)
            val bounds = Rect().also(node::getBoundsInScreen)
            val minSize = 48 * context.resources.displayMetrics.density
            assertTrue("Minimum touch height: $label $bounds", bounds.height() >= minSize - 1)
        }
    }

    private fun click(label: String) {
        await("Action label visible: $label") { find(label) != null }
        var node = find(label)!!
        while (!node.isClickable && node.parent != null) node = node.parent
        assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }

    private fun shell(command: String): String = automation.executeShellCommand(command).use { descriptor ->
        android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }

    private fun capture(name: String) {
        // Accessibility nodes can appear before the Activity enter transition has finished.
        SystemClock.sleep(700)
        val directory = context.getExternalFilesDir("qa-alarm")!!
        directory.mkdirs()
        // Surface transitions can temporarily return null even when nodes are available.
        val screenshot = checkNotNull((1..3).firstNotNullOfOrNull {
            automation.takeScreenshot().also { if (it == null) SystemClock.sleep(500) }
        }) { "Screenshot unavailable after retries" }
        File(directory, "$name.png").outputStream().use {
            check(screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
        }
        screenshot.recycle()
        File(directory, "$name-nodes.txt").writeText(nodes().joinToString("\n") { node ->
            val bounds = Rect().also(node::getBoundsInScreen)
            "${node.className}; text=${node.text}; description=${node.contentDescription}; clickable=${node.isClickable}; focusable=${node.isFocusable}; visible=${node.isVisibleToUser}; bounds=$bounds"
        })
    }
}
