package com.inversioncoach.app.calibration

import com.inversioncoach.app.model.PoseFrame

class CalibrationReadinessEvaluator {
    data class ReadinessResult(
        val usable: Boolean,
        val visibleJointCount: Int,
        val missingRequiredJoints: List<String>,
        val status: String,
    )

    fun isFrameUsable(step: CalibrationStep, frame: PoseFrame): Boolean = evaluate(step, frame).usable

    fun evaluate(step: CalibrationStep, frame: PoseFrame): ReadinessResult {
        val visible = frame.joints.filter { it.visibility >= 0.55f }
        val visibleByName = visible.associateBy { it.name }
        val required = requiredJointNames(step)
        val missing = when (step) {
            CalibrationStep.SIDE_NEUTRAL -> {
                val leftChain = listOf("left_shoulder", "left_hip", "left_knee", "left_ankle")
                val rightChain = listOf("right_shoulder", "right_hip", "right_knee", "right_ankle")
                val leftMissing = leftChain.filterNot(visibleByName::containsKey)
                val rightMissing = rightChain.filterNot(visibleByName::containsKey)
                if (leftMissing.size <= 1 || rightMissing.size <= 1) {
                    val core = listOf("nose")
                    core.filterNot(visibleByName::containsKey)
                } else {
                    (leftMissing + rightMissing).distinct()
                }
            }
            else -> required.filterNot(visibleByName::containsKey)
        }

        if (missing.isNotEmpty()) {
            val status = when {
                missing.any { it.contains("ankle") } -> "Feet not visible"
                missing.any { it.contains("wrist") || it.contains("elbow") } && step == CalibrationStep.ARMS_OVERHEAD -> "Raise arms higher"
                else -> "Full body not visible"
            }
            return ReadinessResult(false, visible.size, missing, status)
        }

        val allVisible = visibleByName.values
        val minX = allVisible.minOfOrNull { it.x } ?: 0f
        val maxX = allVisible.maxOfOrNull { it.x } ?: 1f
        val minY = allVisible.minOfOrNull { it.y } ?: 0f
        val maxY = allVisible.maxOfOrNull { it.y } ?: 1f
        val frameShare = (maxX - minX) * (maxY - minY)
        val minShare = when (step) {
            CalibrationStep.SIDE_NEUTRAL -> 0.12f
            CalibrationStep.ARMS_OVERHEAD -> 0.16f
            else -> 0.18f
        }
        val maxShare = when (step) {
            CalibrationStep.ARMS_OVERHEAD -> 0.95f
            else -> 0.92f
        }
        if (frameShare < minShare) return ReadinessResult(false, visible.size, emptyList(), "Move closer")
        if (frameShare > maxShare) return ReadinessResult(false, visible.size, emptyList(), "Move farther back")

        if (!isPoseMatch(step, visibleByName)) {
            val status = when (step) {
                CalibrationStep.SIDE_NEUTRAL -> "Turn to side"
                CalibrationStep.ARMS_OVERHEAD -> "Raise arms higher"
                else -> "Adjust posture"
            }
            return ReadinessResult(false, visible.size, emptyList(), status)
        }

        val confidenceOk = frame.confidence >= 0.45f
        if (!confidenceOk) return ReadinessResult(false, visible.size, emptyList(), "More light needed")

        return ReadinessResult(true, visible.size, emptyList(), "Ready")
    }

    private fun isPoseMatch(step: CalibrationStep, byName: Map<String, com.inversioncoach.app.model.JointPoint>): Boolean {
        return when (step) {
            CalibrationStep.SIDE_NEUTRAL -> {
                val leftShoulder = byName["left_shoulder"]
                val rightShoulder = byName["right_shoulder"]
                val leftHip = byName["left_hip"]
                val rightHip = byName["right_hip"]
                val leftDominant = listOf("left_shoulder", "left_hip", "left_knee", "left_ankle").count(byName::containsKey)
                val rightDominant = listOf("right_shoulder", "right_hip", "right_knee", "right_ankle").count(byName::containsKey)
                val sideDominance = maxOf(leftDominant, rightDominant) >= 3
                if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null) {
                    sideDominance
                } else {
                    val shoulderCompressed = kotlin.math.abs(leftShoulder.x - rightShoulder.x) < 0.17f
                    val hipCompressed = kotlin.math.abs(leftHip.x - rightHip.x) < 0.18f
                    (shoulderCompressed && hipCompressed) || sideDominance
                }
            }
            CalibrationStep.ARMS_OVERHEAD -> {
                val shouldersY = listOfNotNull(byName["left_shoulder"]?.y, byName["right_shoulder"]?.y).average().toFloat()
                val wristsY = listOfNotNull(byName["left_wrist"]?.y, byName["right_wrist"]?.y).average().toFloat()
                wristsY < shouldersY - 0.08f
            }
            else -> true
        }
    }

    fun requiredJointNames(step: CalibrationStep): List<String> = when (step) {
        CalibrationStep.FRONT_NEUTRAL,
        CalibrationStep.ARMS_OVERHEAD,
        -> listOf(
            "nose",
            "left_shoulder",
            "right_shoulder",
            "left_hip",
            "right_hip",
            "left_knee",
            "right_knee",
            "left_ankle",
            "right_ankle",
        ) + if (step == CalibrationStep.ARMS_OVERHEAD) listOf("left_elbow", "right_elbow", "left_wrist", "right_wrist") else emptyList()
        CalibrationStep.SIDE_NEUTRAL -> listOf("nose", "left_shoulder", "left_hip", "left_knee", "left_ankle", "right_shoulder", "right_hip", "right_knee", "right_ankle")
        CalibrationStep.CONTROLLED_HOLD -> listOf("nose", "left_shoulder", "right_shoulder", "left_hip", "right_hip")
    }
}
