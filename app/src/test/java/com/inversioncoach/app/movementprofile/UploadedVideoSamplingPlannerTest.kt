package com.inversioncoach.app.movementprofile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadedVideoSamplingPlannerTest {
    @Test
    fun stableHoldBecomesSparserThanLegacyFixed() {
        val planner = UploadedVideoSamplingPlanner(
            config = AdaptiveSamplingConfig(enabled = true, legacyFixedFps = 6),
            movementType = MovementType.HOLD,
        )
        val selected = (0L..3000L step 84L).count { ts ->
            planner.decide(signal(ts, visualDiff = 0.01)).sample
        }

        assertTrue(selected < 19) // 6 fps legacy over 3s ~= 19 samples
    }

    @Test
    fun transitionHeavySequenceEntersBurstMode() {
        val planner = UploadedVideoSamplingPlanner(movementType = MovementType.REP)
        planner.decide(signal(0, visualDiff = 0.01))
        val burst = planner.decide(signal(120, visualDiff = 0.30, subject = 0.2, joint = 0.12))

        assertEquals(SamplingMode.BURST, burst.mode)
        assertTrue(burst.reasons.contains(BurstTriggerReason.VISUAL_DIFF))
    }

    @Test
    fun cooldownReturnsToSparseMode() {
        val planner = UploadedVideoSamplingPlanner(movementType = MovementType.REP)
        planner.decide(signal(0, visualDiff = 0.5))
        planner.decide(signal(120, visualDiff = 0.4))
        val stableAfterCooldown = planner.decide(signal(2000, visualDiff = 0.01))

        assertEquals(SamplingMode.SPARSE, stableAfterCooldown.mode)
    }

    @Test
    fun firstAndLastSegmentsAreNotSkipped() {
        val planner = UploadedVideoSamplingPlanner(movementType = MovementType.HOLD)
        val first = planner.decide(signal(0, visualDiff = 0.0, duration = 3000))
        val middle = planner.decide(signal(1200, visualDiff = 0.0, duration = 3000))
        val last = planner.decide(signal(2800, visualDiff = 0.0, duration = 3000))

        assertTrue(first.sample)
        assertTrue(middle.mode == SamplingMode.HOLD_STEADY)
        assertTrue(last.sample)
    }

    @Test
    fun fallbackToLegacyFixedSamplingWorks() {
        val planner = UploadedVideoSamplingPlanner(
            config = AdaptiveSamplingConfig(enabled = true, legacyFixedFps = 6, fallbackToLegacyOnSignalLoss = true),
            movementType = MovementType.REP,
        )
        val a = planner.decide(signal(0, visualDiff = 0.0, reliable = false))
        val b = planner.decide(signal(100, visualDiff = 0.0, reliable = false))
        val c = planner.decide(signal(170, visualDiff = 0.0, reliable = false))

        assertEquals(SamplingMode.LEGACY_FIXED, a.mode)
        assertTrue(a.sample)
        assertTrue(!b.sample)
        assertTrue(c.sample)
    }

    @Test
    fun drillMovementTypeChangesPlannerBehavior() {
        val holdPlanner = UploadedVideoSamplingPlanner(movementType = MovementType.HOLD)
        val repPlanner = UploadedVideoSamplingPlanner(movementType = MovementType.REP)

        val hold = holdPlanner.decide(signal(500, visualDiff = 0.01))
        val rep = repPlanner.decide(signal(500, visualDiff = 0.01))

        assertEquals(SamplingMode.HOLD_STEADY, hold.mode)
        assertEquals(SamplingMode.SPARSE, rep.mode)
        assertTrue(hold.nextIntervalMs > rep.nextIntervalMs)
    }

    private fun signal(
        ts: Long,
        visualDiff: Double,
        subject: Double = 0.0,
        joint: Double = 0.0,
        confidenceDrop: Double = 0.0,
        reliable: Boolean = true,
        duration: Long = 3000,
    ): AdaptiveSamplingSignal = AdaptiveSamplingSignal(
        timestampMs = ts,
        videoDurationMs = duration,
        visualDiff = visualDiff,
        subjectMovement = subject,
        jointMovement = joint,
        confidenceDrop = confidenceDrop,
        signalReliable = reliable,
    )
}
