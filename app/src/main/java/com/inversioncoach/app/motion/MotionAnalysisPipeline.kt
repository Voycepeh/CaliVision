package com.inversioncoach.app.motion

import com.inversioncoach.app.model.AlignmentStrictness
import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.PoseFrame as LegacyPoseFrame

class MotionAnalysisPipeline(
    drillType: DrillType = DrillType.FREESTANDING_HANDSTAND_FUTURE,
) {
    private val smoother = TemporalPoseSmoother()
    private val angleEngine = AngleEngine()
    private val drillDefinition = DrillCatalog.byType(drillType)
    private val phaseDetector = MovementPhaseDetector(
        thresholds = PhaseThresholds(
            downStartDeg = 165f,
            bottomDeg = 95f,
            upStartDeg = 110f,
            topDeg = 168f,
        ),
        trackedAngle = trackedAngleFor(drillDefinition.movementPattern),
    )
    private val holdTracker = HoldAlignmentTracker()
    private val faultEngine = FaultDetectionEngine(
        movementPattern = drillDefinition.movementPattern,
        allowedFaultCodes = drillDefinition.commonFaults,
    )
    private val feedbackEngine = FeedbackEngine()

    data class Output(
        val smoothed: SmoothedPoseFrame,
        val angles: AngleFrame,
        val movement: MovementState,
        val repTracking: RepTrackingSnapshot?,
        val holdTracking: HoldTrackingSnapshot?,
        val isAligned: Boolean,
        val faults: List<FaultEvent>,
        val cue: LiveCue?,
    )

    fun analyze(frame: LegacyPoseFrame, strictness: AlignmentStrictness = AlignmentStrictness.EASY): Output {
        val motionFrame = PoseFrame(
            timestampMs = frame.timestampMs,
            landmarks = frame.joints.mapNotNull { raw ->
                mapJoint(raw.name)?.let { it to Landmark2D(raw.x, raw.y) }
            }.toMap(),
            confidenceByLandmark = frame.joints.mapNotNull { raw -> mapJoint(raw.name)?.let { it to raw.visibility } }.toMap(),
        )

        val smoothed = smoother.smooth(motionFrame)
        val angles = angleEngine.compute(smoothed)
        val isAligned = angles.lineDeviationNorm <= AlignmentPolicy.forStrictness(strictness).lineDeviationNormMax
        val movement = if (drillDefinition.repMode == RepMode.REP_BASED) {
            phaseDetector.update(angles, isAligned)
        } else {
            holdTracker.update(frame.timestampMs, isAligned)
            MovementState(
                currentPhase = MovementPhase.HOLD,
                repProgress = if (isAligned) 1f else 0f,
                confidence = 0.8f,
                startedAt = frame.timestampMs,
                completedRepCount = 0,
            )
        }
        val faults = faultEngine.detect(angles, movement)
        val cue = feedbackEngine.selectCue(faults, frame.timestampMs)

        return Output(
            smoothed = smoothed,
            angles = angles,
            movement = movement,
            repTracking = if (drillDefinition.repMode == RepMode.REP_BASED) phaseDetector.snapshot() else null,
            holdTracking = if (drillDefinition.repMode == RepMode.HOLD_BASED) holdTracker.snapshot() else null,
            isAligned = isAligned,
            faults = faults,
            cue = cue,
        )
    }

    private fun mapJoint(name: String): JointId? = when (name) {
        "nose" -> JointId.NOSE
        "left_shoulder" -> JointId.LEFT_SHOULDER
        "right_shoulder" -> JointId.RIGHT_SHOULDER
        "left_elbow" -> JointId.LEFT_ELBOW
        "right_elbow" -> JointId.RIGHT_ELBOW
        "left_wrist" -> JointId.LEFT_WRIST
        "right_wrist" -> JointId.RIGHT_WRIST
        "left_hip" -> JointId.LEFT_HIP
        "right_hip" -> JointId.RIGHT_HIP
        "left_knee" -> JointId.LEFT_KNEE
        "right_knee" -> JointId.RIGHT_KNEE
        "left_ankle" -> JointId.LEFT_ANKLE
        "right_ankle" -> JointId.RIGHT_ANKLE
        else -> null
    }

    private fun trackedAngleFor(pattern: MovementPattern): String = when (pattern) {
        MovementPattern.VERTICAL_PUSH -> "left_elbow_flexion"
    }
}
