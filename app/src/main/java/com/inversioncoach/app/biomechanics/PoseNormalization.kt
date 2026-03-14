package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.JointPoint
import com.inversioncoach.app.model.PoseFrame

class PoseNormalization(
    private val visibilityThreshold: Float = 0.45f,
    private val stabilityJitterThreshold: Float = 0.03f,
) {
    private var previousDominantPose: Map<String, JointPoint>? = null

    fun normalize(frame: PoseFrame): NormalizedPose {
        val joints = frame.joints.associateBy { it.name }
        val leftReliability = sideReliability(joints, "left")
        val rightReliability = sideReliability(joints, "right")
        val dominant = if (leftReliability >= rightReliability) BodySide.LEFT else BodySide.RIGHT
        val midpoints = computeMidpoints(joints)
        val shoulder = choose(joints, midpoints, dominant, "shoulder")
        val hip = choose(joints, midpoints, dominant, "hip")
        val torsoLength = if (shoulder != null && hip != null) LandmarkMath.distance(shoulder, hip) else 0.25f
        val stable = stabilityCheck(joints)
        val confidence = frame.confidence
        val confidenceLevel = when {
            confidence >= 0.75f && stable -> ConfidenceLevel.HIGH
            confidence >= 0.5f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
        previousDominantPose = joints
        return NormalizedPose(
            timestampMs = frame.timestampMs,
            dominantSide = dominant,
            joints = joints,
            midpoints = midpoints,
            torsoLength = torsoLength.coerceAtLeast(0.1f),
            confidenceLevel = confidenceLevel,
            confidence = confidence,
        )
    }

    private fun sideReliability(joints: Map<String, JointPoint>, side: String): Float {
        val names = listOf("wrist", "elbow", "shoulder", "hip", "knee", "ankle")
        return names.mapNotNull { joints["${side}_$it"]?.visibility }.average().toFloat()
    }

    private fun computeMidpoints(joints: Map<String, JointPoint>): Map<String, JointPoint> {
        fun mid(name: String): JointPoint? {
            val left = joints["left_$name"]
            val right = joints["right_$name"]
            return if (left != null && right != null && left.visibility > visibilityThreshold && right.visibility > visibilityThreshold) {
                LandmarkMath.midpoint(left, right, "${name}_mid")
            } else null
        }
        return listOf("shoulder", "hip", "knee", "ankle", "wrist").mapNotNull { n ->
            mid(n)?.let { "${n}_mid" to it }
        }.toMap()
    }

    private fun choose(
        joints: Map<String, JointPoint>,
        mids: Map<String, JointPoint>,
        side: BodySide,
        part: String,
    ): JointPoint? {
        val mid = mids["${part}_mid"]
        if (mid != null) return mid
        val key = if (side == BodySide.LEFT) "left_$part" else "right_$part"
        return joints[key]
    }

    private fun stabilityCheck(joints: Map<String, JointPoint>): Boolean {
        val previous = previousDominantPose ?: return true
        val tracked = listOf("left_shoulder", "right_shoulder", "left_hip", "right_hip")
        val deltas = tracked.mapNotNull { key ->
            val a = previous[key]
            val b = joints[key]
            if (a == null || b == null) null else LandmarkMath.distance(a, b)
        }
        if (deltas.isEmpty()) return false
        return deltas.average() <= stabilityJitterThreshold
    }
}
