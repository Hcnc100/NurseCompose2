package com.nullpointer.nourseCompose.medication

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nullpointer.nourseCompose.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationNavigationInstrumentedTest {
    @Test fun mainNavigationHostLaunches() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario -> scenario.onActivity { assertNotNull(it) } }
    }
}