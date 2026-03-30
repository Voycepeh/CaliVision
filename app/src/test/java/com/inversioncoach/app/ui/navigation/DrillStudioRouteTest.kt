package com.inversioncoach.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class DrillStudioRouteTest {
    @Test
    fun drillStudioRouteIsStable() {
        assertEquals("drill-studio?mode={mode}&drillId={drillId}&templateId={templateId}", Route.DrillStudio.value)
    }

    @Test
    fun drillStudioCreateRoutes() {
        assertEquals("drill-studio?mode=create&drillId=&templateId=", Route.DrillStudio.createNew())
        assertEquals("drill-studio?mode=drill&drillId=wall_handstand&templateId=", Route.DrillStudio.createForDrill("wall_handstand"))
        assertEquals(
            "drill-studio?mode=drill&drillId=wall_handstand&templateId=baseline-template",
            Route.DrillStudio.createForTemplate("wall_handstand", "baseline-template"),
        )
    }

    @Test
    fun drillStudioRouteEncodesDrillId() {
        assertEquals(
            "drill-studio?mode=drill&drillId=free%20handstand&templateId=template%201",
            Route.DrillStudio.createForTemplate("free handstand", "template 1"),
        )
    }
}
