package com.inversioncoach.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class DrillWorkspaceRouteTest {
    @Test
    fun drillPickerRouteIsStable() {
        assertEquals("drill-picker", Route.DrillPicker.value)
    }

    @Test
    fun drillWorkspaceRouteIsStable() {
        assertEquals("drill-workspace/{drillId}", Route.DrillWorkspace.value)
    }

    @Test
    fun drillWorkspaceCreateEncodesDrillId() {
        assertEquals("drill-workspace/wall_handstand", Route.DrillWorkspace.create("wall_handstand"))
        assertEquals("drill-workspace/free%20handstand", Route.DrillWorkspace.create("free handstand"))
    }

    @Test
    fun drillScopedDestinationsRemainCanonical() {
        val drillId = "wall_handstand"
        assertEquals(
            "upload-video?drillId=wall_handstand&referenceTemplateId=&isReference=false&createNewDrillFromReference=false",
            Route.UploadVideoForDrill.create(drillId, null, false),
        )
        assertEquals("history?drillId=wall_handstand&mode=history", Route.History.create(drillId))
        assertEquals("history?drillId=wall_handstand&mode=compare", Route.History.create(drillId, mode = "compare"))
    }
}
