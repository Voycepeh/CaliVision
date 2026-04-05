package com.inversioncoach.app.drillpackage.mapping

import com.inversioncoach.app.drillpackage.model.PortableDrill
import com.inversioncoach.app.drillpackage.model.PortablePhase
import com.inversioncoach.app.drillpackage.model.PortableViewType
import com.inversioncoach.app.drills.DrillCameraView
import com.inversioncoach.app.drills.DrillSourceType
import com.inversioncoach.app.model.DrillDefinitionRecord

object DrillRecordPortableMapper {
    fun toPortableDrill(record: DrillDefinitionRecord): PortableDrill {
        val phases = record.phaseSchemaJson.split('|').filter { it.isNotBlank() }
        val keyJoints = record.keyJointsJson.split('|').filter { it.isNotBlank() }
        return PortableDrill(
            id = record.id,
            title = record.name,
            description = record.description,
            family = "runtime",
            movementType = record.movementMode,
            cameraView = record.cameraView.toPortableView(),
            supportedViews = listOf(record.cameraView.toPortableView()),
            comparisonMode = "POSE_TIMELINE",
            normalizationBasis = record.normalizationBasisJson,
            keyJoints = keyJoints.map(PortableJointNames::canonicalize),
            tags = listOf(record.sourceType),
            phases = phases.mapIndexed { index, phase ->
                PortablePhase(
                    id = phase,
                    label = phase.replaceFirstChar { it.uppercase() },
                    order = index,
                )
            },
            poses = emptyList(),
            metricThresholds = emptyMap(),
            extensions = mapOf(
                "sourceType" to record.sourceType,
                "status" to record.status,
                "cueConfig" to record.cueConfigJson,
                "version" to record.version.toString(),
            ),
        )
    }

    fun toDrillDefinitionRecord(
        portable: PortableDrill,
        nowMs: Long,
        existing: DrillDefinitionRecord? = null,
    ): DrillDefinitionRecord {
        val phaseSchemaJson = portable.phases.sortedBy { it.order }.joinToString("|") { it.id }
        val keyJointsJson = portable.keyJoints.joinToString("|")
        return DrillDefinitionRecord(
            id = portable.id,
            name = portable.title,
            description = portable.description,
            movementMode = portable.movementType,
            cameraView = portable.cameraView.toLegacyCameraView(),
            phaseSchemaJson = phaseSchemaJson,
            keyJointsJson = keyJointsJson,
            normalizationBasisJson = portable.normalizationBasis,
            cueConfigJson = portable.extensions["cueConfig"].orEmpty(),
            sourceType = portable.extensions["sourceType"] ?: existing?.sourceType ?: DrillSourceType.USER_CREATED,
            status = portable.extensions["status"] ?: existing?.status ?: "DRAFT",
            version = portable.extensions["version"]?.toIntOrNull() ?: existing?.version ?: 1,
            createdAtMs = existing?.createdAtMs ?: nowMs,
            updatedAtMs = nowMs,
        )
    }

    private fun String.toPortableView(): PortableViewType = when (this) {
        DrillCameraView.FRONT -> PortableViewType.FRONT
        DrillCameraView.LEFT -> PortableViewType.LEFT_PROFILE
        DrillCameraView.RIGHT -> PortableViewType.RIGHT_PROFILE
        DrillCameraView.BACK -> PortableViewType.SIDE
        DrillCameraView.FREESTYLE -> PortableViewType.ANY
        else -> PortableViewType.ANY
    }

    private fun PortableViewType.toLegacyCameraView(): String = when (this) {
        PortableViewType.FRONT -> DrillCameraView.FRONT
        PortableViewType.LEFT_PROFILE -> DrillCameraView.LEFT
        PortableViewType.RIGHT_PROFILE -> DrillCameraView.RIGHT
        PortableViewType.SIDE -> DrillCameraView.LEFT
        PortableViewType.ANY -> DrillCameraView.FREESTYLE
    }
}
