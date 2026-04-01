package com.inversioncoach.app.drills

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class DrillSeederReadyTest {
    @Test
    fun seededDrillsAreReady() {
        val seeded = DrillSeeder.seedDrills(0L)
        assertTrue(seeded.isNotEmpty())
        assertTrue(seeded.all { it.status == DrillStatus.READY })
        assertEquals(
            setOf(
                "Push-Up",
                "Bar Dip",
                "Pike Push Up",
                "Elevated Pike Push Up",
                "Pull-Up",
                "Inverted Row",
                "Bodyweight Squat",
                "Forward Lunge",
                "Pistol Squat",
                "Front Plank",
                "Side Plank",
                "Leg Raise",
                "Hollow Hold",
                "Handstand Hold",
                "Handstand Push Up",
            ),
            seeded.map { it.name }.toSet(),
        )
    }
}
