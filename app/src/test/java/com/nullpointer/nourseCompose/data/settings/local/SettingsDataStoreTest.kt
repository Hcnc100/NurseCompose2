package com.nullpointer.nourseCompose.data.settings.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nullpointer.nourseCompose.models.data.SettingsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsDataStoreTest {
    @get:Rule val folder = TemporaryFolder()
    private val key = stringPreferencesKey("key")
    private class MemoryPreferences(initial: Preferences) : DataStore<Preferences> {
        override val data = MutableStateFlow(initial)
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(data.value).also { data.value = it }
    }
    private fun restoredSettings() = MemoryPreferences(preferencesOf(
        key to Json.encodeToString(SettingsData(numberMeasureGraph = 17, onboardingCompleted = true)),
    ))

    @Test fun restoredLegacyCompletionDoesNotSkipIntroduction() = runBlocking {
        val store = SettingsDataStore(restoredSettings(), File(folder.root, "onboarding"))
        val settings = store.getMeasureSettingsData().first()!!
        assertEquals(17, settings.numberMeasureGraph)
        assertFalse(settings.onboardingCompleted)
    }

    @Test fun completionSurvivesRestartWithoutBeingBackedUp() = runBlocking {
        val preferences = restoredSettings()
        val marker = File(folder.root, "onboarding")
        val store = SettingsDataStore(preferences, marker)
        store.saveNewMeasureSettingsData(SettingsData(numberMeasureGraph = 17, onboardingCompleted = true))
        assertTrue(marker.isFile)
        assertTrue(store.getMeasureSettingsData().first()!!.onboardingCompleted)
        assertTrue(SettingsDataStore(preferences, marker).getMeasureSettingsData().first()!!.onboardingCompleted)
        val backedUp = Json.decodeFromString<SettingsData>(preferences.data.value[key]!!)
        assertEquals(17, backedUp.numberMeasureGraph)
        assertFalse(backedUp.onboardingCompleted)
    }

    @Test fun reinstallRestoresDisplaySettingsButNotCompletion() = runBlocking {
        val preferences = restoredSettings()
        SettingsDataStore(preferences, File(folder.root, "old-installation"))
            .saveNewMeasureSettingsData(SettingsData(numberMeasureGraph = 17, onboardingCompleted = true))
        val reinstalled = SettingsDataStore(preferences, File(folder.root, "new-installation"))
            .getMeasureSettingsData().first()!!
        assertEquals(17, reinstalled.numberMeasureGraph)
        assertFalse(reinstalled.onboardingCompleted)
    }

    @Test fun changingDisplaySettingsDoesNotResetInstallationCompletion() = runBlocking {
        val preferences = restoredSettings()
        val store = SettingsDataStore(preferences, File(folder.root, "onboarding"))
        store.saveNewMeasureSettingsData(SettingsData(onboardingCompleted = true))
        store.saveNewMeasureSettingsData(SettingsData(numberMeasureGraph = 25))
        val result = store.getMeasureSettingsData().first()!!
        assertEquals(25, result.numberMeasureGraph)
        assertTrue(result.onboardingCompleted)
    }
}
