package com.inversioncoach.app.movementprofile

import com.inversioncoach.app.model.JointPoint
import com.inversioncoach.app.model.PoseFrame
import com.inversioncoach.app.ui.live.ReadinessState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessAndDetectorsTest {
    @Test
    fun readinessTransitionsToReady() {
        val engine = ReadinessEngine()
        val rule = ReadinessRule(
            minConfidence = 0.4f,
            requiredLandmarks = setOf("left_shoulder", "right_shoulder", "left_hip", "right_hip"),
            minVisibleLandmarkCount = 3,
            sideViewPrimary = false,
        )
        val frame = PoseFrame(
            timestampMs = 1,
            confidence = 0.8f,
            joints = listOf(
                joint("left_shoulder"), joint("right_shoulder"), joint("left_hip"), joint("right_hip")
            ),
        )
        assertEquals(ReadinessState.READY_FULL, engine.evaluate(frame, rule))
    }

    @Test
    fun repAndHoldDetectionBasics() {
        val rep = RepDetector(RepRule("r", "elbow_avg", bottomThresholdDeg = 95f, topThresholdDeg = 160f, minRepDurationMs = 200))
        rep.update(90f)
        val count = rep.update(165f)
        assertEquals(1, count)

        val hold = HoldDetector(HoldRule("h", minHoldMs = 500, maxBreakMs = 100))
        assertTrue(!hold.update(0, true))
        assertTrue(hold.update(600, true))
    }

    private fun joint(name: String) = JointPoint(name, 0.5f, 0.5f, 0f, 0.9f)
}
