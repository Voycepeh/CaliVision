package com.inversioncoach.app.motion

data class PhaseThresholds(
    val downStartDeg: Float,
    val bottomDeg: Float,
    val upStartDeg: Float,
    val topDeg: Float,
    val minDwellMs: Long = 100,
)

data class RepTrackingSnapshot(
    val rawRepAttempts: Int,
    val validRepCount: Int,
    val alignmentPassRatio: Float,
)

data class HoldTrackingSnapshot(
    val isCurrentlyAligned: Boolean,
    val totalSessionDurationMs: Long,
    val totalAlignedDurationMs: Long,
    val currentAlignedStreakMs: Long,
    val bestAlignedStreakMs: Long,
) {
    val misalignedDurationMs: Long
        get() = (totalSessionDurationMs - totalAlignedDurationMs).coerceAtLeast(0L)
}

class MovementPhaseDetector(
    private val thresholds: PhaseThresholds,
    private val trackedAngle: String,
) {
    private var phase: MovementPhase = MovementPhase.SETUP
    private var phaseSince = 0L
    private var validRepCount = 0
    private var rawRepAttempts = 0
    private var cycleHasAlignedFrames = false
    private var cycleFrames = 0
    private var cycleAlignedFrames = 0

    fun update(frame: AngleFrame, isAligned: Boolean): MovementState {
        if (phaseSince == 0L) phaseSince = frame.timestampMs
        val angle = frame.anglesDeg[trackedAngle] ?: 0f
        val canSwitch = frame.timestampMs - phaseSince >= thresholds.minDwellMs

        if (phase == MovementPhase.ECCENTRIC || phase == MovementPhase.BOTTOM || phase == MovementPhase.CONCENTRIC) {
            cycleFrames += 1
            if (isAligned) {
                cycleAlignedFrames += 1
                cycleHasAlignedFrames = true
            }
        }

        when (phase) {
            MovementPhase.SETUP -> if (angle <= thresholds.downStartDeg && canSwitch) {
                resetRepCycle()
                moveTo(MovementPhase.ECCENTRIC, frame)
            }
            MovementPhase.ECCENTRIC -> {
                if (angle <= thresholds.bottomDeg && canSwitch) moveTo(MovementPhase.BOTTOM, frame)
                else if (angle >= thresholds.topDeg && canSwitch) moveTo(MovementPhase.SETUP, frame)
            }
            MovementPhase.BOTTOM -> if (angle >= thresholds.upStartDeg && canSwitch) moveTo(MovementPhase.CONCENTRIC, frame)
            MovementPhase.CONCENTRIC -> {
                if (angle >= thresholds.topDeg && canSwitch) {
                    moveTo(MovementPhase.TOP, frame)
                    rawRepAttempts += 1
                    if (cycleHasAlignedFrames && alignmentPassRatio() >= MIN_ALIGNMENT_RATIO_FOR_VALID_REP) {
                        validRepCount += 1
                    }
                }
            }
            MovementPhase.TOP -> if (canSwitch) moveTo(MovementPhase.RESET, frame)
            MovementPhase.RESET -> if (canSwitch) moveTo(MovementPhase.SETUP, frame)
            MovementPhase.HOLD -> Unit
        }

        val progress = when (phase) {
            MovementPhase.SETUP -> 0f
            MovementPhase.ECCENTRIC -> 0.25f
            MovementPhase.BOTTOM -> 0.5f
            MovementPhase.CONCENTRIC -> 0.75f
            MovementPhase.TOP, MovementPhase.RESET -> 1f
            MovementPhase.HOLD -> 1f
        }

        return MovementState(
            currentPhase = phase,
            repProgress = progress,
            confidence = 0.8f,
            startedAt = phaseSince,
            completedRepCount = validRepCount,
        )
    }

    fun snapshot(): RepTrackingSnapshot = RepTrackingSnapshot(
        rawRepAttempts = rawRepAttempts,
        validRepCount = validRepCount,
        alignmentPassRatio = alignmentPassRatio(),
    )

    fun reset() {
        phase = MovementPhase.SETUP
        phaseSince = 0L
        rawRepAttempts = 0
        validRepCount = 0
        resetRepCycle()
    }

    private fun moveTo(target: MovementPhase, frame: AngleFrame) {
        phase = target
        phaseSince = frame.timestampMs
    }

    private fun resetRepCycle() {
        cycleHasAlignedFrames = false
        cycleFrames = 0
        cycleAlignedFrames = 0
    }

    private fun alignmentPassRatio(): Float = if (cycleFrames == 0) 0f else cycleAlignedFrames.toFloat() / cycleFrames.toFloat()

    companion object {
        private const val MIN_ALIGNMENT_RATIO_FOR_VALID_REP = 0.7f
    }
}

class HoldAlignmentTracker(
    private val minUnalignedDurationToBreakMs: Long = 220L,
) {
    private var lastTimestampMs: Long? = null
    private var totalSessionDurationMs: Long = 0L
    private var totalAlignedDurationMs: Long = 0L
    private var currentAlignedStreakMs: Long = 0L
    private var bestAlignedStreakMs: Long = 0L
    private var currentlyAligned = false
    private var pendingUnalignedMs = 0L

    fun update(timestampMs: Long, isAligned: Boolean): HoldTrackingSnapshot {
        val delta = ((lastTimestampMs?.let { timestampMs - it } ?: 0L)).coerceAtLeast(0L)
        lastTimestampMs = timestampMs
        totalSessionDurationMs += delta

        if (isAligned) {
            pendingUnalignedMs = 0L
            currentlyAligned = true
            currentAlignedStreakMs += delta
            totalAlignedDurationMs += delta
            bestAlignedStreakMs = maxOf(bestAlignedStreakMs, currentAlignedStreakMs)
        } else if (currentlyAligned) {
            pendingUnalignedMs += delta
            if (pendingUnalignedMs >= minUnalignedDurationToBreakMs) {
                currentlyAligned = false
                currentAlignedStreakMs = 0L
                pendingUnalignedMs = 0L
            } else {
                currentAlignedStreakMs += delta
                totalAlignedDurationMs += delta
                bestAlignedStreakMs = maxOf(bestAlignedStreakMs, currentAlignedStreakMs)
            }
        }

        return snapshot()
    }

    fun snapshot(): HoldTrackingSnapshot = HoldTrackingSnapshot(
        isCurrentlyAligned = currentlyAligned,
        totalSessionDurationMs = totalSessionDurationMs,
        totalAlignedDurationMs = totalAlignedDurationMs,
        currentAlignedStreakMs = currentAlignedStreakMs,
        bestAlignedStreakMs = bestAlignedStreakMs,
    )

    fun reset() {
        lastTimestampMs = null
        totalSessionDurationMs = 0L
        totalAlignedDurationMs = 0L
        currentAlignedStreakMs = 0L
        bestAlignedStreakMs = 0L
        currentlyAligned = false
        pendingUnalignedMs = 0L
    }
}
