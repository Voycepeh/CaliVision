package com.inversioncoach.app.ui.reference

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrillWorkspacePrimaryActionsTest {
    @Test
    fun primaryActionsFocusOnDrillPracticeFlow() {
        assertTrue(DrillWorkspacePrimaryActions.primary.contains("Start Live Coaching"))
        assertTrue(DrillWorkspacePrimaryActions.primary.contains("Upload Attempt"))
        assertTrue(DrillWorkspacePrimaryActions.primary.contains("Compare Attempts"))
        assertTrue(DrillWorkspacePrimaryActions.primary.contains("View Past Sessions"))
    }

    @Test
    fun legacyReferenceManagementActionsAreHiddenFromPrimaryFlow() {
        assertFalse(DrillWorkspacePrimaryActions.primary.contains("Upload New Reference"))
        assertFalse(DrillWorkspacePrimaryActions.primary.contains("Use Past Session as Reference"))
        assertFalse(DrillWorkspacePrimaryActions.primary.contains("Reference Template"))
        assertFalse(DrillWorkspacePrimaryActions.primary.contains("Edit Drill"))
        assertTrue(DrillWorkspacePrimaryActions.hiddenLegacy.contains("Upload New Reference"))
    }
}
