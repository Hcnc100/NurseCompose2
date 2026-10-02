package com.nullpointer.nourseCompose.navigation

import com.nullpointer.nourseCompose.ui.screens.NavGraphs
import com.nullpointer.nourseCompose.ui.screens.destinations.HomeScreenDestination
import com.nullpointer.nourseCompose.ui.screens.destinations.MedicationReminderEditorScreenDestination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderEditorNavigationTest {
    @Test
    fun `reminder editor is a root sibling of Home and not a nested Home destination`() {
        val rootDestinations = NavGraphs.root.destinationsByRoute.values
        assertTrue(rootDestinations.contains(HomeScreenDestination))
        assertTrue(rootDestinations.contains(MedicationReminderEditorScreenDestination))
        assertFalse(NavGraphs.homeGraph.destinationsByRoute.values.contains(MedicationReminderEditorScreenDestination))
    }
}
