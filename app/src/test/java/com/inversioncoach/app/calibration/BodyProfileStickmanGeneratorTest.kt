package com.inversioncoach.app.calibration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyProfileStickmanGeneratorTest {
    private val generator = BodyProfileStickmanGenerator()

    @Test
    fun generatesExpectedJointSet() {
        val frame = generator.generateFront(sampleProfile(), timestampMs = 123L)
        assertEquals(13, frame.joints.size)
        assertEquals(123L, frame.timestampMs)
        val names = frame.joints.map { it.name }.toSet()
        assertTrue(names.contains("left_shoulder"))
        assertTrue(names.contains("right_ankle"))
    }

    @Test
    fun sidePoseUsesDedicatedAnchors() {
        val side = generator.generateSide(sampleProfile(), timestampMs = 7L)
        val leftShoulderX = side.joints.first { it.name == "left_shoulder" }.x
        val rightShoulderX = side.joints.first { it.name == "right_shoulder" }.x
        val leftWristX = side.joints.first { it.name == "left_wrist" }.x
        assertTrue(kotlin.math.abs(leftShoulderX - rightShoulderX) < 0.04f)
        assertTrue(leftWristX > leftShoulderX)
    }

    @Test
    fun overheadPosePlacesWristsAboveShoulders() {
        val overhead = generator.generateOverhead(sampleProfile(), timestampMs = 9L)
        val shoulderY = overhead.joints.first { it.name == "left_shoulder" }.y
        val wristY = overhead.joints.first { it.name == "left_wrist" }.y
        assertTrue(wristY < shoulderY)
    }

    private fun sampleProfile(): UserBodyProfile = UserBodyProfile(
        segmentRatios = SegmentRatios(
            shoulderToTorso = 0.95f,
            hipToShoulder = 0.85f,
            upperArmToTorso = 0.48f,
            forearmToUpperArm = 0.92f,
            thighToTorso = 0.78f,
            shinToThigh = 0.93f,
            armToTorso = 0.92f,
            legToTorso = 1.55f,
        ),
        symmetryMetrics = SymmetryMetrics(0.95f, 0.96f, 0.02f, 0.03f),
    )
}
