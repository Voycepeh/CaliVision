package com.inversioncoach.app.ui.upload

import com.inversioncoach.app.drills.DrillStatus
import com.inversioncoach.app.drills.runtime.RuntimeDrillDefinition
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadDrillReadinessGateTest {
    @Test
    fun rejectsDraftCustomDrill() {
        val draft = RuntimeDrillDefinition(
            id = "drill-1",
            name = "Draft Drill",
            movementMode = "HOLD",
            cameraView = "LEFT",
            status = DrillStatus.DRAFT,
            phases = listOf("setup", "hold"),
            keyJoints = setOf("hips"),
            normalizationBasis = "hips",
        )

        val error = validateSelectedDrillForUpload("drill-1", draft)
        assertTrue(error?.contains("not ready", ignoreCase = true) == true)
    }

    @Test
    fun acceptsReadyCustomDrill() {
        val ready = RuntimeDrillDefinition(
            id = "drill-1",
            name = "Ready Drill",
            movementMode = "HOLD",
            cameraView = "LEFT",
            status = DrillStatus.READY,
            phases = listOf("setup", "hold"),
            keyJoints = setOf("hips"),
            normalizationBasis = "hips",
        )

        val error = validateSelectedDrillForUpload("drill-1", ready)
        assertNull(error)
    }
}
