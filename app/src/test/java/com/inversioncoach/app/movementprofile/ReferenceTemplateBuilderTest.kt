package com.inversioncoach.app.movementprofile

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceTemplateBuilderTest {
    @Test
    fun buildFromSingleReference_writesStructuredPhaseAndKeyframeJson() {
        val snapshot = StoredProfileSnapshot(
            phaseDurationsMs = linkedMapOf("setup" to 1200L, "hold" to 3400L),
            featureMeans = mapOf("alignment_score" to 0.85f),
            stabilityJitter = mapOf("trunk_lean" to 0.12f),
        )

        val record = ReferenceTemplateBuilder().buildFromSingleReference(
            drillId = "drill_1",
            displayName = "Template",
            sourceProfileId = "profile_1",
            snapshot = snapshot,
            createdAtMs = 1234L,
        )

        val phases = JSONObject(record.phasePosesJson).getJSONArray("phases")
        val keyframes = JSONObject(record.keyframesJson).getJSONArray("keyframes")
        assertEquals(2, phases.length())
        assertEquals(2, keyframes.length())
        assertEquals("setup", phases.getJSONObject(0).getString("phaseId"))
        assertTrue(phases.getJSONObject(0).has("targetFeatures"))
        assertEquals("hold", keyframes.getJSONObject(1).getString("phaseId"))
    }
}

