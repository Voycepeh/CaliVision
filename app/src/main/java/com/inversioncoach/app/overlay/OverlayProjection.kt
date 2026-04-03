package com.inversioncoach.app.overlay

import com.inversioncoach.app.pose.PoseProjectionInput

enum class OverlayCoordinateSpace {
    /**
     * Joints are already normalized into upright render space.
     * This is the canonical contract used by live smoothing, uploaded analysis, and export playback.
     */
    UPRIGHT_NORMALIZED,

    /**
     * Joints are normalized in source orientation and still require rotation correction.
     */
    SOURCE_ORIENTED_NORMALIZED,
}

object OverlayProjection {
    fun inputForFrame(
        frame: OverlayDrawingFrame,
        renderWidth: Int,
        renderHeight: Int,
    ): PoseProjectionInput {
        val rotationDegrees = when (frame.coordinateSpace) {
            OverlayCoordinateSpace.UPRIGHT_NORMALIZED -> 0
            OverlayCoordinateSpace.SOURCE_ORIENTED_NORMALIZED -> normalizeRotation(frame.sourceRotationDegrees)
        }
        return PoseProjectionInput(
            sourceWidth = frame.sourceWidth.coerceAtLeast(1),
            sourceHeight = frame.sourceHeight.coerceAtLeast(1),
            previewWidth = renderWidth.toFloat(),
            previewHeight = renderHeight.toFloat(),
            previewContentRect = frame.previewContentRect,
            rotationDegrees = rotationDegrees,
            mirrored = frame.mirrored,
            scaleMode = frame.scaleMode,
        )
    }

    private fun normalizeRotation(rotationDegrees: Int): Int = ((rotationDegrees % 360) + 360) % 360
}
