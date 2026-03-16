package com.inversioncoach.app.overlay

import android.util.Log
import com.inversioncoach.app.model.JointPoint
import kotlin.math.abs

class FreestyleOrientationClassifier {
    private var stableMode: FreestyleViewMode = FreestyleViewMode.UNKNOWN
    private var pendingMode: FreestyleViewMode = FreestyleViewMode.UNKNOWN
    private var pendingCount: Int = 0

    fun classify(joints: List<JointPoint>): FreestyleViewMode {
        val (candidate, confidence) = classifyWithConfidence(joints)
        val stabilized = stabilize(candidate)
        if (stabilized != stableMode) {
            Log.d(TAG, "orientation_transition from=$stableMode to=$stabilized candidate=$candidate confidence=${"%.2f".format(confidence)}")
            stableMode = stabilized
        }
        Log.v(TAG, "orientation_debug candidate=$candidate stable=$stableMode confidence=${"%.2f".format(confidence)}")
        return stabilized
    }

    fun classifyWithConfidence(joints: List<JointPoint>): Pair<FreestyleViewMode, Float> {
        val lookup = joints.associateBy { it.name }
        val shoulderGap = horizontalGap(lookup, "left_shoulder", "right_shoulder")
        val hipGap = horizontalGap(lookup, "left_hip", "right_hip")
        val bilateralSpread = maxOf(shoulderGap, hipGap)
        val symmetric = bilateralSymmetryScore(lookup)
        val faceVisibility = FACE_LANDMARKS.averageVisibility(lookup)
        val leftVisibility = sideVisibilityScore(lookup, "left")
        val rightVisibility = sideVisibilityScore(lookup, "right")

        val bilateralLikely = bilateralSpread >= BILATERAL_SPREAD_THRESHOLD && symmetric >= SYMMETRY_THRESHOLD
        if (bilateralLikely) {
            val backCues = rearBodyScore(lookup)
            val frontConfidence = (faceVisibility * 0.7f + symmetric * 0.3f).coerceIn(0f, 1f)
            val backConfidence = ((1f - faceVisibility) * 0.65f + backCues * 0.35f).coerceIn(0f, 1f)
            return if (frontConfidence >= backConfidence) {
                FreestyleViewMode.FRONT to frontConfidence
            } else {
                FreestyleViewMode.BACK to backConfidence
            }
        }

        val sideDelta = abs(leftVisibility - rightVisibility)
        if (sideDelta < SIDE_DELTA_MIN && bilateralSpread < PROFILE_SPREAD_MAX) {
            val fallback = if (stableMode == FreestyleViewMode.UNKNOWN) FreestyleViewMode.UNKNOWN else stableMode
            return fallback to 0.35f
        }

        return if (leftVisibility >= rightVisibility) {
            FreestyleViewMode.LEFT_PROFILE to (leftVisibility / MAX_SIDE_VISIBILITY).coerceIn(0f, 1f)
        } else {
            FreestyleViewMode.RIGHT_PROFILE to (rightVisibility / MAX_SIDE_VISIBILITY).coerceIn(0f, 1f)
        }
    }

    private fun stabilize(candidate: FreestyleViewMode): FreestyleViewMode {
        if (candidate == stableMode) {
            pendingMode = candidate
            pendingCount = 0
            return stableMode
        }

        if (candidate == pendingMode) {
            pendingCount += 1
        } else {
            pendingMode = candidate
            pendingCount = 1
        }

        if (candidate == FreestyleViewMode.UNKNOWN && stableMode != FreestyleViewMode.UNKNOWN) {
            return stableMode
        }

        return if (pendingCount >= HYSTERESIS_FRAME_COUNT) {
            pendingCount = 0
            candidate
        } else {
            stableMode
        }
    }

    private fun bilateralSymmetryScore(lookup: Map<String, JointPoint>): Float {
        val pairs = listOf(
            "shoulder" to "hip",
            "elbow" to "wrist",
            "knee" to "ankle",
        )
        var sum = 0f
        var count = 0
        pairs.forEach { (upper, lower) ->
            sum += mirroredHeightSymmetry(lookup, "left_$upper", "right_$upper")
            count += 1
            sum += mirroredHeightSymmetry(lookup, "left_$lower", "right_$lower")
            count += 1
        }
        return if (count == 0) 0f else (sum / count).coerceIn(0f, 1f)
    }

    private fun mirroredHeightSymmetry(lookup: Map<String, JointPoint>, left: String, right: String): Float {
        val l = lookup[left] ?: return 0f
        val r = lookup[right] ?: return 0f
        if (l.visibility < MIN_VISIBILITY || r.visibility < MIN_VISIBILITY) return 0f
        return (1f - abs(l.y - r.y) / 0.35f).coerceIn(0f, 1f)
    }

    private fun rearBodyScore(lookup: Map<String, JointPoint>): Float {
        val shoulderYs = listOfNotNull(lookup["left_shoulder"], lookup["right_shoulder"]).map { it.y }
        val hipYs = listOfNotNull(lookup["left_hip"], lookup["right_hip"]).map { it.y }
        if (shoulderYs.isEmpty() || hipYs.isEmpty()) return 0f
        val torsoSpan = (hipYs.average() - shoulderYs.average()).toFloat().coerceAtLeast(0f)
        val noseVis = lookup["nose"]?.visibility ?: 0f
        return ((torsoSpan / 0.45f) * 0.7f + (1f - noseVis.coerceIn(0f, 1f)) * 0.3f).coerceIn(0f, 1f)
    }

    private fun horizontalGap(lookup: Map<String, JointPoint>, left: String, right: String): Float {
        val l = lookup[left] ?: return 0f
        val r = lookup[right] ?: return 0f
        if (l.visibility < MIN_VISIBILITY || r.visibility < MIN_VISIBILITY) return 0f
        return abs(l.x - r.x)
    }

    private fun sideVisibilityScore(lookup: Map<String, JointPoint>, prefix: String): Float =
        listOf("shoulder", "elbow", "wrist", "hip", "knee", "ankle")
            .sumOf { (lookup["${prefix}_$it"]?.visibility ?: 0f).toDouble() }
            .toFloat()

    private fun List<String>.averageVisibility(lookup: Map<String, JointPoint>): Float {
        if (isEmpty()) return 0f
        val total = sumOf { (lookup[it]?.visibility ?: 0f).toDouble() }.toFloat()
        return (total / size).coerceIn(0f, 1f)
    }

    companion object {
        private const val TAG = "FreestyleOrientation"
        private const val MIN_VISIBILITY = 0.35f
        private const val BILATERAL_SPREAD_THRESHOLD = 0.16f
        private const val PROFILE_SPREAD_MAX = 0.14f
        private const val SYMMETRY_THRESHOLD = 0.55f
        private const val SIDE_DELTA_MIN = 0.5f
        private const val HYSTERESIS_FRAME_COUNT = 3
        private const val MAX_SIDE_VISIBILITY = 6f

        private val FACE_LANDMARKS = listOf(
            "nose",
            "left_eye",
            "right_eye",
            "left_ear",
            "right_ear",
            "mouth_left",
            "mouth_right",
        )
    }
}
