package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ReminderSaveSessionTest {
    private val reminder = MedicationReminderEntity(name = "Test", startAt = 1_000, intervalHours = 1)

    @Test fun completedSaveIgnoresRepeatedSubmission() = runBlocking {
        val repository = MemoryRepository()
        var scheduled = 0
        val session = ReminderSaveSession(repository, { scheduled++ }) { _, _, _ -> }
        assertNotNull(session.save(reminder))
        assertNull(session.save(reminder))
        assertEquals(1, repository.addCalls)
        assertEquals(1, scheduled)
    }

    @Test fun schedulingFailureReusesPersistedIdAndAppliesDraftChanges() = runBlocking {
        val repository = MemoryRepository()
        var scheduled = 0
        val session = ReminderSaveSession(repository, { if (++scheduled == 1) error("Scheduler failure") }) { _, _, _ -> }
        assertTrue(runCatching { session.save(reminder) }.isFailure)
        val saved = session.save(reminder.copy(name = "Changed after failure"))!!
        assertEquals(1, repository.addCalls)
        assertEquals(1, repository.updateCalls)
        assertEquals(saved, repository.rows.value.single())
        assertEquals(2, scheduled)
    }

    @Test fun logFailureDoesNotCreateAnotherReminderOnRetry() = runBlocking {
        val repository = MemoryRepository()
        var records = 0
        val session = ReminderSaveSession(repository, {}) { _, created, first ->
            assertTrue(created)
            assertTrue(first)
            if (++records == 1) error("Log failure")
        }
        assertTrue(runCatching { session.save(reminder) }.isFailure)
        assertNotNull(session.save(reminder))
        assertEquals(1, repository.rows.value.size)
        assertEquals(1, repository.addCalls)
    }

    @Test fun failedInsertCanBeRetriedWithoutSchedulingUnsavedData() = runBlocking {
        val repository = MemoryRepository().apply { failAdd = true }
        var scheduled = 0
        val session = ReminderSaveSession(repository, { scheduled++ }) { _, _, _ -> }
        assertTrue(runCatching { session.save(reminder) }.isFailure)
        assertEquals(0, scheduled)
        repository.failAdd = false
        assertNotNull(session.save(reminder))
        assertEquals(1, repository.rows.value.size)
        assertEquals(1, scheduled)
    }

    @Test fun simultaneousSubmissionDoesNotInsertTwice() = runBlocking {
        val repository = MemoryRepository().apply { addGate = CompletableDeferred() }
        val session = ReminderSaveSession(repository, {}) { _, _, _ -> }
        val first = async { session.save(reminder) }
        repository.addEntered.await()
        assertNull(session.save(reminder))
        repository.addGate!!.complete(Unit)
        assertNotNull(first.await())
        assertEquals(1, repository.addCalls)
    }

    @Test fun existingReminderIsUpdatedNotInserted() = runBlocking {
        val existing = reminder.copy(id = 42)
        val repository = MemoryRepository().apply { rows.value = listOf(existing) }
        val session = ReminderSaveSession(repository, {}) { saved, created, _ ->
            assertFalse(created)
            assertEquals(42L, saved.id)
        }
        assertEquals(42L, session.save(existing)!!.id)
        assertEquals(0, repository.addCalls)
        assertEquals(1, repository.updateCalls)
    }

    @Test fun restoredPendingIdPreventsDuplicateAfterSessionRecreation() = runBlocking {
        val repository = MemoryRepository()
        var persistedId: Long? = null
        val firstSession = ReminderSaveSession(repository, { error("Scheduling failed") },
            onPersisted = { id, _ -> persistedId = id }) { _, _, _ -> }
        assertTrue(runCatching { firstSession.save(reminder) }.isFailure)
        assertNotNull(persistedId)
        val restored = ReminderSaveSession(repository, {}, initialPersistedId = persistedId) { _, _, _ -> }
        assertEquals(persistedId, restored.save(reminder)!!.id)
        assertEquals(1, repository.addCalls)
        assertEquals(1, repository.rows.value.size)
    }

    private class MemoryRepository : MedicationReminderRepository {
        val rows = MutableStateFlow<List<MedicationReminderEntity>>(emptyList())
        var addCalls = 0
        var updateCalls = 0
        var failAdd = false
        var addGate: CompletableDeferred<Unit>? = null
        val addEntered = CompletableDeferred<Unit>()
        override fun observeAll() = rows
        override fun observeActive() = rows.map { list -> list.filter { it.isActive } }
        override suspend fun add(reminder: MedicationReminderEntity): Long {
            addCalls++
            addEntered.complete(Unit)
            addGate?.await()
            if (failAdd) error("Database failure")
            val id = (rows.value.maxOfOrNull { it.id } ?: 0) + 1
            rows.value = rows.value + reminder.copy(id = id)
            return id
        }
        override suspend fun update(reminder: MedicationReminderEntity) {
            updateCalls++
            rows.value = rows.value.map { if (it.id == reminder.id) reminder else it }
        }
        override suspend fun delete(reminder: MedicationReminderEntity) {
            rows.value = rows.value.filterNot { it.id == reminder.id }
        }
    }
}
