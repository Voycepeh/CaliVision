package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.JointPoint
import kotlin.math.abs

class CommonMetricsCalculator {

    fun calculate(pose: NormalizedPose, phaseTempo: Map<String, Float>, pathMetrics: Map<String, Float>): DerivedMetrics {
        val side = pose.dominantSide.name.lowercase()
        val j = pose.joints
        val m = pose.midpoints
        val shoulder = m["shoulder_mid"] ?: j["${side}_shoulder"]
        val hip = m["hip_mid"] ?: j["${side}_hip"]
        val knee = m["knee_mid"] ?: j["${side}_knee"]
        val ankle = m["ankle_mid"] ?: j["${side}_ankle"]
        val wrist = m["wrist_mid"] ?: j["${side}_wrist"]
        val elbow = j["${side}_elbow"]
        val ear = j["${side}_ear"] ?: j["nose"]

        val jointAngles = mapOf(
            "elbow_angle" to LandmarkMath.angle(shoulder, elbow, wrist),
            "shoulder_angle_proxy" to LandmarkMath.angle(hip, shoulder, elbow),
            "hip_angle" to LandmarkMath.angle(shoulder, hip, knee),
            "knee_angle" to LandmarkMath.angle(hip, knee, ankle),
        )

        val segmentVertical = mapOf(
            "wrist_to_shoulder" to LandmarkMath.segmentVerticalDeviationDegrees(wrist, shoulder),
            "shoulder_to_hip" to LandmarkMath.segmentVerticalDeviationDegrees(shoulder, hip),
            "hip_to_knee" to LandmarkMath.segmentVerticalDeviationDegrees(hip, knee),
            "knee_to_ankle" to LandmarkMath.segmentVerticalDeviationDegrees(knee, ankle),
        )

        val stackLineX = wrist?.x ?: shoulder?.x ?: 0.5f
        val offsets = mapOf(
            "shoulder_stack_offset" to normalizeX(shoulder, stackLineX, pose.torsoLength),
            "hip_stack_offset" to normalizeX(hip, stackLineX, pose.torsoLength),
            "knee_stack_offset" to normalizeX(knee, stackLineX, pose.torsoLength),
            "ankle_stack_offset" to normalizeX(ankle, stackLineX, pose.torsoLength),
        )

        val bodyLineDeviation = offsets.values.map { abs(it) }.average().toFloat()
        val kneeAngle = jointAngles["knee_angle"] ?: 180f
        val kneeScore = ((kneeAngle - 140f) / 40f * 100f).toInt().coerceIn(0, 100)
        val bananaProxy = bananaProxy(offsets)
        val pelvicProxy = (100 - abs((offsets["hip_stack_offset"] ?: 0f - offsets["shoulder_stack_offset"]!!)).times(220f).toInt())
            .coerceIn(0, 100)
        val shoulderOpen = jointAngles["shoulder_angle_proxy"] ?: 0f
        val shoulderOpenScore = (100 - abs(170f - shoulderOpen) * 1.2f).toInt().coerceIn(0, 100)
        val scapProxy = scapularProxy(ear, shoulder, pose.torsoLength)

        return DerivedMetrics(
            timestampMs = pose.timestampMs,
            jointAngles = jointAngles,
            segmentVerticalDeviation = segmentVertical,
            stackOffsetsNorm = offsets,
            bodyLineDeviationNorm = bodyLineDeviation,
            kneeExtensionScore = kneeScore,
            bananaProxyScore = bananaProxy,
            pelvicControlProxyScore = pelvicProxy,
            shoulderOpennessScore = shoulderOpenScore,
            scapularElevationProxyScore = scapProxy,
            tempoMetrics = phaseTempo,
            pathMetrics = pathMetrics,
            confidenceLevel = pose.confidenceLevel,
            confidence = pose.confidence,
        )
    }

    private fun normalizeX(point: JointPoint?, referenceX: Float, torsoLength: Float): Float {
        if (point == null) return 0f
        return LandmarkMath.normalizeByTorsoLength(LandmarkMath.signedHorizontalOffset(referenceX, point.x), torsoLength)
    }

    private fun bananaProxy(offsets: Map<String, Float>): Int {
        val shoulder = abs(offsets["shoulder_stack_offset"] ?: 0f)
        val hip = abs(offsets["hip_stack_offset"] ?: 0f)
        val ankle = abs(offsets["ankle_stack_offset"] ?: 0f)
        val curve = (hip - ((shoulder + ankle) / 2f)).coerceAtLeast(0f)
        return (curve * 360f).toInt().coerceIn(0, 100)
    }

    private fun scapularProxy(ear: JointPoint?, shoulder: JointPoint?, torso: Float): Int {
        if (ear == null || shoulder == null) return 50
        val d = LandmarkMath.normalizeByTorsoLength(LandmarkMath.verticalDistance(ear, shoulder), torso)
        return (100 - d * 260f).toInt().coerceIn(0, 100)
    }
}
