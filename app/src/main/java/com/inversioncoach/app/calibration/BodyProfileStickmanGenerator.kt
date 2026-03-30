package com.inversioncoach.app.calibration

import com.inversioncoach.app.model.JointPoint
import com.inversioncoach.app.model.SmoothedPoseFrame

class BodyProfileStickmanGenerator {
    private data class PoseGeometry(
        val torso: Float,
        val shoulderHalf: Float,
        val hipHalf: Float,
        val upperArm: Float,
        val forearm: Float,
        val thigh: Float,
        val shin: Float,
    )

    private fun geometry(profile: UserBodyProfile): PoseGeometry {
        val torso = 0.32f
        val shoulderHalf = (profile.segmentRatios.shoulderToTorso * torso) / 2f
        val hipHalf = (profile.segmentRatios.hipToShoulder * profile.segmentRatios.shoulderToTorso * torso) / 2f
        val upperArm = profile.segmentRatios.upperArmToTorso * torso
        val forearm = profile.segmentRatios.forearmToUpperArm * upperArm
        val thigh = profile.segmentRatios.thighToTorso * torso
        val shin = profile.segmentRatios.shinToThigh * thigh
        return PoseGeometry(torso, shoulderHalf, hipHalf, upperArm, forearm, thigh, shin)
    }

    fun generateFront(profile: UserBodyProfile, timestampMs: Long = 0L): SmoothedPoseFrame {
        val g = geometry(profile)

        val cx = 0.5f
        val shoulderY = 0.26f
        val hipY = shoulderY + g.torso
        val kneeY = hipY + g.thigh
        val ankleY = kneeY + g.shin

        return SmoothedPoseFrame(
            timestampMs = timestampMs,
            confidence = 0.99f,
            analysisWidth = 1000,
            analysisHeight = 1000,
            joints = listOf(
                jp("nose", cx, shoulderY - 0.14f),
                jp("left_shoulder", cx - g.shoulderHalf, shoulderY),
                jp("right_shoulder", cx + g.shoulderHalf, shoulderY),
                jp("left_elbow", cx - g.shoulderHalf - g.upperArm * 0.55f, shoulderY + g.upperArm * 0.45f),
                jp("right_elbow", cx + g.shoulderHalf + g.upperArm * 0.55f, shoulderY + g.upperArm * 0.45f),
                jp("left_wrist", cx - g.shoulderHalf - g.upperArm * 0.55f, shoulderY + g.upperArm * 0.45f + g.forearm),
                jp("right_wrist", cx + g.shoulderHalf + g.upperArm * 0.55f, shoulderY + g.upperArm * 0.45f + g.forearm),
                jp("left_hip", cx - g.hipHalf, hipY),
                jp("right_hip", cx + g.hipHalf, hipY),
                jp("left_knee", cx - g.hipHalf, kneeY),
                jp("right_knee", cx + g.hipHalf, kneeY),
                jp("left_ankle", cx - g.hipHalf, ankleY),
                jp("right_ankle", cx + g.hipHalf, ankleY),
            ),
        )
    }

    fun generateSide(profile: UserBodyProfile, timestampMs: Long = 0L): SmoothedPoseFrame {
        val g = geometry(profile)
        val centerX = 0.5f
        val shoulderY = 0.27f
        val hipY = shoulderY + g.torso
        val kneeY = hipY + g.thigh
        val ankleY = kneeY + g.shin
        val armForward = g.upperArm * 0.42f
        val forearmDrop = g.forearm * 0.86f
        return SmoothedPoseFrame(
            timestampMs = timestampMs,
            confidence = 0.99f,
            analysisWidth = 1000,
            analysisHeight = 1000,
            joints = listOf(
                jp("nose", centerX + 0.01f, shoulderY - 0.13f),
                jp("left_shoulder", centerX - 0.012f, shoulderY),
                jp("right_shoulder", centerX + 0.012f, shoulderY),
                jp("left_elbow", centerX + armForward * 0.55f, shoulderY + g.upperArm * 0.36f),
                jp("right_elbow", centerX + armForward * 0.42f, shoulderY + g.upperArm * 0.44f),
                jp("left_wrist", centerX + armForward, shoulderY + g.upperArm * 0.36f + forearmDrop),
                jp("right_wrist", centerX + armForward * 0.84f, shoulderY + g.upperArm * 0.44f + forearmDrop),
                jp("left_hip", centerX - 0.01f, hipY),
                jp("right_hip", centerX + 0.01f, hipY),
                jp("left_knee", centerX - 0.008f, kneeY),
                jp("right_knee", centerX + 0.008f, kneeY),
                jp("left_ankle", centerX - 0.008f, ankleY),
                jp("right_ankle", centerX + 0.008f, ankleY),
            ),
        )
    }

    fun generateOverhead(profile: UserBodyProfile, timestampMs: Long = 0L): SmoothedPoseFrame {
        val g = geometry(profile)
        val cx = 0.5f
        val shoulderY = 0.31f
        val hipY = shoulderY + g.torso
        val kneeY = hipY + g.thigh
        val ankleY = kneeY + g.shin
        val elbowRaise = (g.upperArm * 0.72f).coerceAtLeast(0.1f)
        val wristRaise = (g.upperArm + g.forearm * 0.75f).coerceAtLeast(0.2f)
        return SmoothedPoseFrame(
            timestampMs = timestampMs,
            confidence = 0.99f,
            analysisWidth = 1000,
            analysisHeight = 1000,
            joints = listOf(
                jp("nose", cx, shoulderY - 0.13f),
                jp("left_shoulder", cx - g.shoulderHalf, shoulderY),
                jp("right_shoulder", cx + g.shoulderHalf, shoulderY),
                jp("left_elbow", cx - g.shoulderHalf, shoulderY - elbowRaise),
                jp("right_elbow", cx + g.shoulderHalf, shoulderY - elbowRaise),
                jp("left_wrist", cx - g.shoulderHalf, shoulderY - wristRaise),
                jp("right_wrist", cx + g.shoulderHalf, shoulderY - wristRaise),
                jp("left_hip", cx - g.hipHalf, hipY),
                jp("right_hip", cx + g.hipHalf, hipY),
                jp("left_knee", cx - g.hipHalf, kneeY),
                jp("right_knee", cx + g.hipHalf, kneeY),
                jp("left_ankle", cx - g.hipHalf, ankleY),
                jp("right_ankle", cx + g.hipHalf, ankleY),
            ),
        )
    }

    private fun jp(name: String, x: Float, y: Float): JointPoint = JointPoint(name, x.coerceIn(0.05f, 0.95f), y.coerceIn(0.05f, 0.95f), 0f, 0.98f)
}
