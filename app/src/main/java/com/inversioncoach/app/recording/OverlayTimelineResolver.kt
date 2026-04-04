package com.inversioncoach.app.recording

import com.inversioncoach.app.model.JointPoint

class OverlayTimelineResolver(
    frames: List<AnnotatedOverlayFrame>,
    private val toleranceMs: Long = DEFAULT_OVERLAY_TOLERANCE_MS,
) {
    private val samples = frames.sortedBy { it.timestampMs }
    private val smoothedByName = samples.map { sample -> sample.smoothedLandmarks.associateBy { it.name } }
    private var lowerIndex = 0

    fun overlayAt(targetTimestampMs: Long): AnnotatedOverlayFrame? {
        if (samples.isEmpty()) return null
        if (targetTimestampMs < samples[lowerIndex].timestampMs) {
            lowerIndex = findFloorIndex(targetTimestampMs).coerceAtLeast(0)
        }
        while (lowerIndex < samples.lastIndex && samples[lowerIndex + 1].timestampMs <= targetTimestampMs) {
            lowerIndex++
        }
        val previous = samples[lowerIndex]
        val next = samples.getOrNull(lowerIndex + 1)
        if (next == null || previous.timestampMs == next.timestampMs) {
            return previous.takeIf { kotlin.math.abs(targetTimestampMs - it.timestampMs) <= toleranceMs }
        }
        if (targetTimestampMs <= previous.timestampMs) {
            return previous.takeIf { kotlin.math.abs(targetTimestampMs - it.timestampMs) <= toleranceMs }
        }
        if (targetTimestampMs >= next.timestampMs) {
            return next.takeIf { kotlin.math.abs(targetTimestampMs - it.timestampMs) <= toleranceMs }
        }
        val span = (next.timestampMs - previous.timestampMs).toFloat().coerceAtLeast(1f)
        val t = ((targetTimestampMs - previous.timestampMs).toFloat() / span).coerceIn(0f, 1f)
        return interpolate(previous, next, lowerIndex, lowerIndex + 1, t)
    }

    private fun findFloorIndex(targetTimestampMs: Long): Int {
        var left = 0
        var right = samples.lastIndex
        var floor = 0
        while (left <= right) {
            val mid = (left + right) ushr 1
            val value = samples[mid].timestampMs
            when {
                value <= targetTimestampMs -> {
                    floor = mid
                    left = mid + 1
                }
                else -> right = mid - 1
            }
        }
        return floor
    }

    private fun interpolate(
        previous: AnnotatedOverlayFrame,
        next: AnnotatedOverlayFrame,
        previousIndex: Int,
        nextIndex: Int,
        t: Float,
    ): AnnotatedOverlayFrame {
        if (t <= 0.01f) return previous
        if (t >= 0.99f) return next
        val prevByName = smoothedByName.getOrNull(previousIndex).orEmpty()
        val nextByName = smoothedByName.getOrNull(nextIndex).orEmpty()
        val names = HashSet<String>(prevByName.size + nextByName.size).apply {
            addAll(prevByName.keys)
            addAll(nextByName.keys)
        }
        val interpolated = ArrayList<JointPoint>(names.size)
        names.forEach { name ->
            val a = prevByName[name]
            val b = nextByName[name]
            when {
                a != null && b != null -> interpolated += JointPoint(
                    name = name,
                    x = lerp(a.x, b.x, t),
                    y = lerp(a.y, b.y, t),
                    z = lerp(a.z, b.z, t),
                    visibility = lerp(a.visibility, b.visibility, t),
                )
                a != null -> interpolated += a
                b != null -> interpolated += b
            }
        }
        return previous.copy(
            timestampMs = lerp(previous.timestampMs.toFloat(), next.timestampMs.toFloat(), t).toLong(),
            landmarks = interpolated,
            smoothedLandmarks = interpolated,
            confidence = lerp(previous.confidence, next.confidence, t),
            unreliableJointNames = previous.unreliableJointNames + next.unreliableJointNames,
            bodyVisible = previous.bodyVisible || next.bodyVisible,
            showSkeleton = previous.showSkeleton || next.showSkeleton,
            showIdealLine = previous.showIdealLine || next.showIdealLine,
        )
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + ((b - a) * t)

    companion object {
        private const val DEFAULT_OVERLAY_TOLERANCE_MS = 120L
    }
}
