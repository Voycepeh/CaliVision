package com.inversioncoach.app.drills

import com.inversioncoach.app.model.DrillDefinitionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrillSeedSynchronizerTest {
    @Test
    fun reconcile_cleanInstall_insertsFullCatalog() {
        val now = 123L
        val updates = DrillSeedSynchronizer.reconcile(
            existing = emptyList(),
            seededCatalog = DrillSeeder.seedDrills(now),
            nowMs = now,
        )

        assertEquals(15, updates.size)
        assertTrue(updates.all { it.sourceType == DrillSourceType.SEEDED && it.status == DrillStatus.READY })
    }

    @Test
    fun reconcile_upgradeFromLegacyTwoSeeds_insertsMissingAndRepairsLegacy() {
        val legacyNow = 10L
        val existing = listOf(
            DrillDefinitionRecord(
                id = "seed_free_handstand",
                name = "Free Handstand",
                description = "Legacy free handstand",
                movementMode = DrillMovementMode.HOLD,
                cameraView = DrillCameraView.FREESTYLE,
                phaseSchemaJson = "setup|stack|hold",
                keyJointsJson = "shoulders|hips|ankles",
                normalizationBasisJson = "hips",
                cueConfigJson = "legacyDrillType:FREE_HANDSTAND",
                sourceType = DrillSourceType.SEEDED,
                status = DrillStatus.READY,
                version = 1,
                createdAtMs = legacyNow,
                updatedAtMs = legacyNow,
            ),
            DrillDefinitionRecord(
                id = "seed_wall_handstand",
                name = "Wall Handstand",
                description = "Legacy wall handstand",
                movementMode = DrillMovementMode.HOLD,
                cameraView = DrillCameraView.LEFT,
                phaseSchemaJson = "setup|stack|hold",
                keyJointsJson = "shoulders|hips|ankles",
                normalizationBasisJson = "hips",
                cueConfigJson = "legacyDrillType:WALL_HANDSTAND",
                sourceType = DrillSourceType.SEEDED,
                status = DrillStatus.READY,
                version = 1,
                createdAtMs = legacyNow,
                updatedAtMs = legacyNow,
            ),
        )

        val updates = DrillSeedSynchronizer.reconcile(
            existing = existing,
            seededCatalog = DrillSeeder.seedDrills(999L),
            nowMs = 999L,
        )

        assertEquals(15, updates.size)
        val repairedLegacy = updates.first { it.id == "seed_free_handstand" }
        assertEquals("Handstand Hold", repairedLegacy.name)
        assertEquals(legacyNow, repairedLegacy.createdAtMs)
    }

    @Test
    fun reconcile_idempotentAndDoesNotOverwriteUserAuthored() {
        val now = 777L
        val catalog = DrillSeeder.seedDrills(now)
        val userOverride = catalog.first().copy(
            sourceType = DrillSourceType.USER_CREATED,
            name = "My Push-Up",
            description = "Personal drill",
            version = 99,
        )
        val existing = catalog + userOverride

        val updates = DrillSeedSynchronizer.reconcile(
            existing = existing,
            seededCatalog = catalog,
            nowMs = now + 1,
        )

        assertTrue(updates.none { it.id == userOverride.id })
        assertTrue(updates.isEmpty())
    }
}
