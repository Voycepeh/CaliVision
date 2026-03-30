package com.inversioncoach.app.calibration

import com.inversioncoach.app.model.JointPoint
import com.inversioncoach.app.model.PoseFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibrationReadinessEvaluatorTest {
    private val evaluator = CalibrationReadinessEvaluator()

    @Test
    fun overheadNeedsWristsAboveShoulders() {
        val lowArms = frame(leftWristY = 0.5f, rightWristY = 0.5f)
        val result = evaluator.evaluate(CalibrationStep.ARMS_OVERHEAD, lowArms)
        assertFalse(result.usable)
        assertEquals("Raise arms higher", result.status)
    }

    @Test
    fun frontNeutralReadyWhenFullBodyVisible() {
        val result = evaluator.evaluate(CalibrationStep.FRONT_NEUTRAL, frame())
        assertTrue(result.usable)
        assertEquals("Ready", result.status)
    }

    @Test
    fun sideNeutralAcceptsSingleDominantChain() {
        val sideish = frame().copy(
            joints = frame().joints.filterNot { it.name.startsWith("right_") && it.name.contains("ankle") },
        )
        val result = evaluator.evaluate(CalibrationStep.SIDE_NEUTRAL, sideish)
        assertTrue(result.usable || result.status == "Ready")
    }

    private fun frame(leftWristY: Float = 0.1f, rightWristY: Float = 0.1f): PoseFrame = PoseFrame(
        timestampMs = 0,
        confidence = 0.95f,
        joints = listOf(
            JointPoint("nose", 0.5f, 0.1f, 0f, 0.95f),
            JointPoint("left_shoulder", 0.45f, 0.25f, 0f, 0.95f),
            JointPoint("right_shoulder", 0.55f, 0.25f, 0f, 0.95f),
            JointPoint("left_elbow", 0.43f, 0.2f, 0f, 0.95f),
            JointPoint("right_elbow", 0.57f, 0.2f, 0f, 0.95f),
            JointPoint("left_wrist", 0.42f, leftWristY, 0f, 0.95f),
            JointPoint("right_wrist", 0.58f, rightWristY, 0f, 0.95f),
            JointPoint("left_hip", 0.46f, 0.5f, 0f, 0.95f),
            JointPoint("right_hip", 0.54f, 0.5f, 0f, 0.95f),
            JointPoint("left_knee", 0.46f, 0.7f, 0f, 0.95f),
            JointPoint("right_knee", 0.54f, 0.7f, 0f, 0.95f),
            JointPoint("left_ankle", 0.46f, 0.9f, 0f, 0.95f),
            JointPoint("right_ankle", 0.54f, 0.9f, 0f, 0.95f),
        ),
    )
}
