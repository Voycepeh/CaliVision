package com.inversioncoach.app.movementprofile

import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.motion.DrillCatalog
import com.inversioncoach.app.motion.RepMode

class ExistingDrillToProfileAdapter {
    fun fromDrill(drillType: DrillType): MovementProfile {
        val drill = DrillCatalog.byType(drillType)
        val movementType = if (drill.repMode == RepMode.HOLD_BASED) MovementType.HOLD else MovementType.REP
        val phases = drill.mainPhases.mapIndexed { idx, phase ->
            PhaseDefinition(id = phase.id, displayName = phase.label, sequenceIndex = idx)
        }
        val readinessRule = ReadinessRule(
            minConfidence = 0.28f,
            requiredLandmarks = setOf(
                "left_shoulder", "right_shoulder", "left_hip", "right_hip", "left_ankle", "right_ankle", "left_wrist", "right_wrist"
            ),
            minVisibleLandmarkCount = 4,
            sideViewPrimary = drillType != DrillType.FREESTYLE,
        )
        return MovementProfile(
            id = "legacy-${drill.id.name.lowercase()}",
            displayName = drill.displayName,
            drillType = drillType,
            movementType = movementType,
            allowedViews = setOf(CameraViewConstraint.ANY),
            phaseDefinitions = phases,
            alignmentRules = listOf(
                AlignmentRule("line_stack", "shoulder_hip_stack", target = 0f, tolerance = 0.16f),
            ),
            holdRule = if (movementType == MovementType.HOLD) HoldRule("hold_default", minHoldMs = 2000, maxBreakMs = 220) else null,
            repRule = if (movementType == MovementType.REP) RepRule("rep_default", "elbow_avg", 95f, 160f, 300) else null,
            readinessRule = readinessRule,
            keyJoints = readinessRule.requiredLandmarks,
            defaultThresholds = mapOf("line_deviation_max" to 0.18f, "min_confidence" to readinessRule.minConfidence),
        )
    }
}

class LegacyDrillExecutionBridge(
    private val adapter: ExistingDrillToProfileAdapter = ExistingDrillToProfileAdapter(),
    private val calibrationStore: CalibrationProfileStore = InMemoryCalibrationProfileStore(),
) {
    fun resolveProfile(drillType: DrillType): MovementProfile {
        val base = adapter.fromDrill(drillType)
        val calibration = calibrationStore.latest(base.id)
        if (calibration == null) return base
        return base.copy(
            defaultThresholds = base.defaultThresholds + calibration.calibratedThresholds,
            readinessRule = calibration.readinessOverride ?: base.readinessRule,
        )
    }

    fun saveCalibration(profileId: String, author: String, calibratedThresholds: Map<String, Float>, readinessOverride: ReadinessRule?): CalibrationProfile {
        val version = (calibrationStore.latest(profileId)?.version ?: 0) + 1
        val profile = CalibrationProfile(
            profileId = profileId,
            version = version,
            createdAtMs = System.currentTimeMillis(),
            author = author,
            calibratedThresholds = calibratedThresholds,
            readinessOverride = readinessOverride,
        )
        calibrationStore.save(profile)
        return profile
    }
}

interface CalibrationProfileStore {
    fun save(profile: CalibrationProfile)
    fun latest(profileId: String): CalibrationProfile?
}

class InMemoryCalibrationProfileStore : CalibrationProfileStore {
    private val state = linkedMapOf<String, MutableList<CalibrationProfile>>()
    override fun save(profile: CalibrationProfile) {
        state.getOrPut(profile.profileId) { mutableListOf() }.add(profile)
    }

    override fun latest(profileId: String): CalibrationProfile? = state[profileId]?.maxByOrNull { it.version }
}
