package com.tteumsae.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun previous_destinations_match_current_behavior() {
        assertEquals(AppDestination.HOME, previousDestination(AppDestination.SAVED))
        assertEquals(AppDestination.HOME, previousDestination(AppDestination.SETTINGS))
        assertEquals(AppDestination.HOME, previousDestination(AppDestination.LOCATION))
        assertEquals(AppDestination.LOCATION, previousDestination(AppDestination.CONDITIONS))
        assertEquals(AppDestination.CONDITIONS, previousDestination(AppDestination.LOADING))
        assertEquals(AppDestination.CONDITIONS, previousDestination(AppDestination.RESULTS))
        assertEquals(AppDestination.RESULTS, previousDestination(AppDestination.DETAIL))
        assertEquals(AppDestination.HOME, previousDestination(AppDestination.HOME))
    }
}
