package com.nullpointer.nourseCompose.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicationReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: MedicationReminderRepository
    @Inject lateinit var scheduler: MedicationReminderScheduler
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == MedicationReminderScheduler.ACTION_SNOOZE) {
            scheduler.snooze(intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)); return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == MedicationReminderScheduler.ACTION_REMINDER) {
                    repository.observeAll().first().firstOrNull { it.id == intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1) }?.let { reminder -> scheduler.showNotification(reminder); scheduler.schedule(reminder) }
                } else repository.observeActive().first().forEach(scheduler::schedule)
            } finally { pendingResult.finish() }
        }
    }
}
