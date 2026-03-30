package com.inversioncoach.app.calibration

import com.inversioncoach.app.model.PoseFrame
import kotlin.math.abs
import kotlin.math.sqrt

class UserBodyProfileBuilder {

    data class StepQuality(
        val step: CalibrationStep,
        val quality: Float,
        val stability: Float,
        val acceptedFrames: Int,
    )

    fun build(
        frontFrames: List<PoseFrame>,
        sideFrames: List<PoseFrame>,
        overheadFrames: List<PoseFrame>,
        holdFrames: List<PoseFrame>,
    ): UserBodyProfile {
        val now = System.currentTimeMillis()
        val frontStable = stableFrames(frontFrames)
        val sideStable = stableFrames(sideFrames)
        val overheadStable = stableFrames(overheadFrames)
        val all = (frontStable + sideStable + overheadStable + holdFrames).ifEmpty { frontFrames + sideFrames + overheadFrames }

        val shoulderWidth = median(frontStable.mapNotNull { normalizedDistance(it, "left_shoulder", "right_shoulder") })
        val hipWidth = median(frontStable.mapNotNull { normalizedDistance(it, "left_hip", "right_hip") })
        val torsoLength = median(sideStable.mapNotNull { torsoLength(it) }).coerceAtLeast(0.0001f)
        val upperArm = median(
            overheadStable.mapNotNull { segmentLength(it, "left_shoulder", "left_elbow") } +
                overheadStable.mapNotNull { segmentLength(it, "right_shoulder", "right_elbow") },
        )
        val forearm = median(
            overheadStable.mapNotNull { segmentLength(it, "left_elbow", "left_wrist") } +
                overheadStable.mapNotNull { segmentLength(it, "right_elbow", "right_wrist") },
        )
        val femur = median(
            all.mapNotNull { segmentLength(it, "left_hip", "left_knee") } +
                all.mapNotNull { segmentLength(it, "right_hip", "right_knee") },
        )
        val shin = median(
            all.mapNotNull { segmentLength(it, "left_knee", "left_ankle") } +
                all.mapNotNull { segmentLength(it, "right_knee", "right_ankle") },
        )

        val armSymmetry = median(all.mapNotNull { bilateralConsistency(it, "shoulder", "elbow") })
        val legSymmetry = median(all.mapNotNull { bilateralConsistency(it, "hip", "knee") })
        val shoulderLevel = median(frontStable.mapNotNull { levelDelta(it, "left_shoulder", "right_shoulder") })
        val hipLevel = median(frontStable.mapNotNull { levelDelta(it, "left_hip", "right_hip") })

        val segmentRatios = SegmentRatios(
            shoulderToTorso = shoulderWidth / torsoLength,
            hipToShoulder = hipWidth / shoulderWidth.coerceAtLeast(0.0001f),
            upperArmToTorso = upperArm / torsoLength,
            forearmToUpperArm = forearm / upperArm.coerceAtLeast(0.0001f),
            thighToTorso = femur / torsoLength,
            shinToThigh = shin / femur.coerceAtLeast(0.0001f),
            armToTorso = (upperArm + forearm) / torsoLength,
            legToTorso = (femur + shin) / torsoLength,
        )

        val qualities = listOf(
            stepQuality(CalibrationStep.FRONT_NEUTRAL, frontFrames, frontStable),
            stepQuality(CalibrationStep.SIDE_NEUTRAL, sideFrames, sideStable),
            stepQuality(CalibrationStep.ARMS_OVERHEAD, overheadFrames, overheadStable),
        )

        return UserBodyProfile(
            id = "bp_${now}",
            updatedAt = now,
            createdAt = now,
            overallQuality = qualities.map { it.quality }.average().toFloat(),
            frontConfidence = qualities.first { it.step == CalibrationStep.FRONT_NEUTRAL }.quality,
            sideConfidence = qualities.first { it.step == CalibrationStep.SIDE_NEUTRAL }.quality,
            overheadConfidence = qualities.first { it.step == CalibrationStep.ARMS_OVERHEAD }.quality,
            captureVersion = 2,
            segmentRatios = segmentRatios,
            symmetryMetrics = SymmetryMetrics(
                armSymmetry = armSymmetry,
                legSymmetry = legSymmetry,
                shoulderLevelBaseline = shoulderLevel,
                hipLevelBaseline = hipLevel,
            ),
            stepSummaries = qualities.map {
                CaptureStepSummary(
                    stepType = it.step.name,
                    acceptedFrames = it.acceptedFrames,
                    qualityScore = it.quality,
                    stabilityScore = it.stability,
                    notes = if (it.quality > 0.8f) "Good capture quality" else "Fair capture quality",
                )
            },
        )
    }

    private fun stableFrames(frames: List<PoseFrame>): List<PoseFrame> {
        if (frames.size < 3) return frames
        val torso = frames.mapNotNull { torsoLength(it) }
        if (torso.isEmpty()) return frames
        val medianTorso = median(torso)
        return frames.filter { frame ->
            val len = torsoLength(frame) ?: return@filter false
            abs(len - medianTorso) <= medianTorso * 0.2f
        }
    }

    private fun stepQuality(step: CalibrationStep, raw: List<PoseFrame>, stable: List<PoseFrame>): StepQuality {
        val acceptanceRatio = if (raw.isEmpty()) 0f else stable.size.toFloat() / raw.size.toFloat()
        val stability = stable.zipWithNext { a, b ->
            val aNose = joint(a, "nose")
            val bNose = joint(b, "nose")
            if (aNose == null || bNose == null) 0f else 1f - ((abs(aNose.x - bNose.x) + abs(aNose.y - bNose.y)) / 0.1f).coerceIn(0f, 1f)
        }.ifEmpty { listOf(0.6f) }.average().toFloat()
        return StepQuality(step, quality = (acceptanceRatio * 0.6f + stability * 0.4f).coerceIn(0f, 1f), stability = stability, acceptedFrames = stable.size)
    }

    private fun median(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2f else sorted[mid]
    }

    private fun joint(frame: PoseFrame, name: String) = frame.joints.firstOrNull { it.name == name }

    private fun normalizedDistance(frame: PoseFrame, a: String, b: String): Float? {
        val p1 = joint(frame, a) ?: return null
        val p2 = joint(frame, b) ?: return null
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun segmentLength(frame: PoseFrame, a: String, b: String): Float? = normalizedDistance(frame, a, b)

    private fun torsoLength(frame: PoseFrame): Float? {
        val left = normalizedDistance(frame, "left_shoulder", "left_hip")
        val right = normalizedDistance(frame, "right_shoulder", "right_hip")
        return listOfNotNull(left, right).takeIf { it.isNotEmpty() }?.average()?.toFloat()
    }

    private fun bilateralConsistency(frame: PoseFrame, a: String, b: String): Float? {
        val left = segmentLength(frame, "left_$a", "left_$b") ?: return null
        val right = segmentLength(frame, "right_$a", "right_$b") ?: return null
        val denom = maxOf(left, right, 0.0001f)
        return (1f - abs(left - right) / denom).coerceIn(0f, 1f)
    }

    private fun levelDelta(frame: PoseFrame, left: String, right: String): Float? {
        val l = joint(frame, left) ?: return null
        val r = joint(frame, right) ?: return null
        return abs(l.y - r.y)
    }
}
