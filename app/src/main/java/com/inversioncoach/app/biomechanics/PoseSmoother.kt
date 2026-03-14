package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.JointPoint
import com.inversioncoach.app.model.PoseFrame

class PoseSmoother(
    private val alpha: Float = 0.35f,
) {
    private var previous: PoseFrame? = null

    fun smooth(frame: PoseFrame): PoseFrame {
        val prev = previous
        if (prev == null) {
            previous = frame
            return frame
        }
        val prevMap = prev.joints.associateBy { it.name }
        val smoothed = frame.joints.map { current ->
            val p = prevMap[current.name] ?: return@map current
            JointPoint(
                name = current.name,
                x = LandmarkMath.lowPassFilter(p.x, current.x, alpha),
                y = LandmarkMath.lowPassFilter(p.y, current.y, alpha),
                z = LandmarkMath.lowPassFilter(p.z, current.z, alpha),
                visibility = LandmarkMath.lowPassFilter(p.visibility, current.visibility, alpha),
            )
        }
        return frame.copy(joints = smoothed, confidence = smoothed.map { it.visibility }.average().toFloat()).also { previous = it }
    }
}
