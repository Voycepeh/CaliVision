package com.inversioncoach.app.movementprofile

import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.overlay.DrillCameraSide
import java.io.Serializable

enum class MovementType { HOLD, REP, TRANSITION, HYBRID }

enum class CameraViewConstraint { SIDE_LEFT, SIDE_RIGHT, FRONT, BACK, ANY }

enum class RuleSeverity { INFO, WARNING, BLOCKING }

data class PhaseDefinition(
    val id: String,
    val displayName: String,
    val minDurationMs: Long = 0L,
    val sequenceIndex: Int,
) : Serializable

data class AlignmentRule(
    val id: String,
    val metricKey: String,
    val target: Float,
    val tolerance: Float,
    val severity: RuleSeverity = RuleSeverity.WARNING,
) : Serializable

data class HoldRule(
    val id: String,
    val minHoldMs: Long,
    val maxBreakMs: Long,
) : Serializable

data class RepRule(
    val id: String,
    val angleKey: String,
    val bottomThresholdDeg: Float,
    val topThresholdDeg: Float,
    val minRepDurationMs: Long,
) : Serializable

data class ReadinessRule(
    val minConfidence: Float,
    val requiredLandmarks: Set<String>,
    val minVisibleLandmarkCount: Int,
    val sideViewPrimary: Boolean,
) : Serializable

data class MovementProfile(
    val id: String,
    val displayName: String,
    val drillType: DrillType?,
    val movementType: MovementType,
    val allowedViews: Set<CameraViewConstraint>,
    val phaseDefinitions: List<PhaseDefinition>,
    val alignmentRules: List<AlignmentRule>,
    val holdRule: HoldRule? = null,
    val repRule: RepRule? = null,
    val readinessRule: ReadinessRule,
    val keyJoints: Set<String>,
    val defaultThresholds: Map<String, Float> = emptyMap(),
) : Serializable {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (id.isBlank()) errors += "profile.id is required"
        if (displayName.isBlank()) errors += "profile.displayName is required"
        if (phaseDefinitions.isEmpty()) errors += "profile.phaseDefinitions must not be empty"
        if (movementType == MovementType.HOLD && holdRule == null) errors += "hold movement requires holdRule"
        if (movementType == MovementType.REP && repRule == null) errors += "rep movement requires repRule"
        if (keyJoints.isEmpty()) errors += "profile.keyJoints must not be empty"
        if (readinessRule.requiredLandmarks.isEmpty()) errors += "readiness.requiredLandmarks must not be empty"
        return errors
    }
}

data class CalibrationProfile(
    val profileId: String,
    val version: Int,
    val createdAtMs: Long,
    val author: String,
    val calibratedThresholds: Map<String, Float>,
    val readinessOverride: ReadinessRule? = null,
    val notes: String? = null,
) : Serializable

data class MovementTemplateCandidate(
    val id: String,
    val sourceSessionId: String,
    val tentativeName: String?,
    val movementTypeGuess: MovementType,
    val detectedView: CameraViewConstraint,
    val keyJoints: Set<String>,
    val candidatePhases: List<PhaseDefinition>,
    val candidateRomMetrics: Map<String, Float>,
    val thresholdSuggestions: Map<String, Float>,
    val confidence: Float,
    val status: CandidateStatus,
) : Serializable

enum class CandidateStatus { DRAFT, REVIEWED, REJECTED }

fun CameraViewConstraint.toDrillCameraSide(preferred: DrillCameraSide): DrillCameraSide = when (this) {
    CameraViewConstraint.SIDE_LEFT -> DrillCameraSide.LEFT
    CameraViewConstraint.SIDE_RIGHT -> DrillCameraSide.RIGHT
    else -> preferred
}
