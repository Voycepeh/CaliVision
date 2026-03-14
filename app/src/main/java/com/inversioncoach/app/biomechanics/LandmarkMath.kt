package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.JointPoint
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object LandmarkMath {
    fun distance(p1: JointPoint, p2: JointPoint): Float =
        sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))

    fun midpoint(p1: JointPoint, p2: JointPoint, name: String = "mid"): JointPoint =
        JointPoint(name, (p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f, (p1.z + p2.z) / 2f, minOf(p1.visibility, p2.visibility))

    fun angle(a: JointPoint?, b: JointPoint?, c: JointPoint?): Float {
        if (a == null || b == null || c == null) return 0f
        val abx = a.x - b.x
        val aby = a.y - b.y
        val cbx = c.x - b.x
        val cby = c.y - b.y
        val dot = abx * cbx + aby * cby
        val magAb = sqrt(abx * abx + aby * aby)
        val magCb = sqrt(cbx * cbx + cby * cby)
        if (magAb <= 1e-6 || magCb <= 1e-6) return 0f
        val cosine = (dot / (magAb * magCb)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosine).toDouble()).toFloat()
    }

    fun signedHorizontalOffset(referenceX: Float, targetX: Float): Float = targetX - referenceX

    fun verticalDistance(p1: JointPoint, p2: JointPoint): Float = abs(p1.y - p2.y)

    fun lowPassFilter(previous: Float, current: Float, alpha: Float): Float =
        previous + alpha * (current - previous)

    fun clamp01(value: Float): Float = value.coerceIn(0f, 1f)

    fun normalizeByTorsoLength(value: Float, torsoLength: Float): Float =
        if (torsoLength <= 1e-6f) 0f else value / torsoLength

    fun projectPointToVerticalReferenceLine(point: JointPoint, referenceX: Float): JointPoint =
        point.copy(x = referenceX)

    fun segmentVerticalDeviationDegrees(p1: JointPoint?, p2: JointPoint?): Float {
        if (p1 == null || p2 == null) return 90f
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val angleToVertical = Math.toDegrees(kotlin.math.atan2(abs(dx).toDouble(), abs(dy).toDouble())).toFloat()
        return angleToVertical
    }
}
