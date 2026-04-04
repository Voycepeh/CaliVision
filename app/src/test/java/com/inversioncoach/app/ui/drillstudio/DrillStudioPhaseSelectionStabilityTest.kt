package com.inversioncoach.app.ui.drillstudio

import kotlin.test.Test
import kotlin.test.assertEquals

class DrillStudioPhaseSelectionStabilityTest {

    @Test
    fun `keeps selected phase when it still exists after reorder`() {
        val selected = stabilizeSelectedPhaseSelection(
            previousOrderedPhaseIds = listOf("phase_1", "phase_2", "phase_3"),
            nextOrderedPhaseIds = listOf("phase_3", "phase_1", "phase_2"),
            currentSelectedPhaseId = "phase_2",
        )

        assertEquals("phase_2", selected)
    }

    @Test
    fun `when selected phase is deleted selection falls to adjacent index instead of first`() {
        val selected = stabilizeSelectedPhaseSelection(
            previousOrderedPhaseIds = listOf("phase_1", "phase_2", "phase_3"),
            nextOrderedPhaseIds = listOf("phase_1", "phase_3"),
            currentSelectedPhaseId = "phase_2",
        )

        assertEquals("phase_3", selected)
    }

    @Test
    fun `when selection missing falls back deterministically to first available`() {
        val selected = stabilizeSelectedPhaseSelection(
            previousOrderedPhaseIds = emptyList(),
            nextOrderedPhaseIds = listOf("phase_1", "phase_2"),
            currentSelectedPhaseId = null,
        )

        assertEquals("phase_1", selected)
    }
}
