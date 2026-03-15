package com.inversioncoach.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DrillTypeDisplayNameTest {
    @Test
    fun canonicalDisplayNamesMatchSelectionLabels() {
        assertEquals("Free Handstand", DrillType.FREESTANDING_HANDSTAND_FUTURE.displayName)
        assertEquals("Wall Handstand", DrillType.CHEST_TO_WALL_HANDSTAND.displayName)
        assertEquals("Pike Push Up", DrillType.PIKE_PUSH_UP.displayName)
        assertEquals("Elevated Pike Push Up", DrillType.ELEVATED_PIKE_PUSH_UP.displayName)
        assertEquals("Handstand Push Up", DrillType.PUSH_UP.displayName)
        assertEquals("Wall Handstand Push Up", DrillType.NEGATIVE_WALL_HANDSTAND_PUSH_UP.displayName)
    }
}
