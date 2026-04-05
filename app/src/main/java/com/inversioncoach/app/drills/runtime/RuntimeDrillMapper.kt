package com.inversioncoach.app.drills.runtime

import com.inversioncoach.app.model.DrillDefinitionRecord

object RuntimeDrillMapper {
    fun fromRecord(record: DrillDefinitionRecord): RuntimeDrillDefinition = RuntimeDrillDefinition(
        id = record.id,
        name = record.name,
        movementMode = record.movementMode,
        cameraView = record.cameraView,
        status = record.status,
        phases = record.phaseSchemaJson.split('|').filter { it.isNotBlank() },
        keyJoints = record.keyJointsJson.split('|').filter { it.isNotBlank() }.toSet(),
        normalizationBasis = record.normalizationBasisJson,
    )
}
