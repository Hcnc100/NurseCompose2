package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex

/** One editor session. Retrying a partially completed insert must reuse its ID. */
internal class ReminderSaveSession(
    private val repository: MedicationReminderRepository,
    private val schedule: (MedicationReminderEntity) -> Unit,
    initialPersistedId: Long? = null,
    initialFirstReminder: Boolean = false,
    private val onPersisted: (Long, Boolean) -> Unit = { _, _ -> },
    private val record: suspend (MedicationReminderEntity, Boolean, Boolean) -> Unit,
) {
    private val mutex = Mutex()
    private var persistedId: Long? = initialPersistedId
    private var firstReminder = initialFirstReminder
    private var completed = false

    suspend fun save(reminder: MedicationReminderEntity): MedicationReminderEntity? {
        if (!mutex.tryLock()) return null
        try {
            if (completed) return null
            val created = reminder.id == 0L
            val saved = when {
                !created -> reminder.also { repository.update(it) }
                persistedId != null -> reminder.copy(id = persistedId!!).also { repository.update(it) }
                else -> {
                    firstReminder = repository.observeAll().first().isEmpty()
                    val id = repository.add(reminder)
                    persistedId = id
                    onPersisted(id, firstReminder)
                    reminder.copy(id = id)
                }
            }
            schedule(saved)
            record(saved, created, firstReminder)
            completed = true
            return saved
        } finally {
            mutex.unlock()
        }
    }
}
