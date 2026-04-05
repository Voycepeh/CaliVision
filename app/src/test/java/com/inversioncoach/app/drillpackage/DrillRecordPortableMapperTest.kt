package com.inversioncoach.app.drillpackage

import com.inversioncoach.app.drillpackage.mapping.DrillRecordPortableMapper
import com.inversioncoach.app.drills.DrillCameraView
import com.inversioncoach.app.drills.DrillMovementMode
import com.inversioncoach.app.drills.DrillSourceType
import com.inversioncoach.app.drills.DrillStatus
import com.inversioncoach.app.model.DrillDefinitionRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class DrillRecordPortableMapperTest {
    @Test
    fun mapsRuntimeRecordToPortableAndBackWithoutSupportedFieldLoss() {
        val now = 1234L
        val input = DrillDefinitionRecord(
            id = "seed_push_up",
            name = "Push Up",
            description = "desc",
            movementMode = DrillMovementMode.REP,
            cameraView = DrillCameraView.LEFT,
            phaseSchemaJson = "setup|eccentric|concentric",
            keyJointsJson = "left_shoulder|right_shoulder|left_hip",
            normalizationBasisJson = "HIPS",
            cueConfigJson = "seedKey:seed_push_up",
            sourceType = DrillSourceType.SEEDED,
            status = DrillStatus.READY,
            version = 3,
            createdAtMs = now,
            updatedAtMs = now,
        )

        val portable = DrillRecordPortableMapper.toPortableDrill(input)
        val restored = DrillRecordPortableMapper.toDrillDefinitionRecord(portable = portable, nowMs = now, existing = input)

        assertEquals(input.id, restored.id)
        assertEquals(input.name, restored.name)
        assertEquals(input.phaseSchemaJson, restored.phaseSchemaJson)
        assertEquals(input.keyJointsJson, restored.keyJointsJson)
        assertEquals(input.cueConfigJson, restored.cueConfigJson)
        assertEquals(input.status, restored.status)
        assertEquals(input.sourceType, restored.sourceType)
    }
}
