package com.inversioncoach.app.overlay

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.inversioncoach.app.model.AlignmentMetric
import com.inversioncoach.app.model.AngleDebugMetric
import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.SessionMode
import com.inversioncoach.app.model.SmoothedPoseFrame
import com.inversioncoach.app.pose.PoseScaleMode

@Composable
fun OverlayRenderer(
    frame: SmoothedPoseFrame?,
    drillType: DrillType,
    sessionMode: SessionMode,
    modifier: Modifier = Modifier,
    showIdealLine: Boolean,
    showDebugOverlay: Boolean = false,
    debugMetrics: List<AlignmentMetric> = emptyList(),
    debugAngles: List<AngleDebugMetric> = emptyList(),
    currentPhase: String = "setup",
    activeFault: String = "",
    cueText: String = "",
    drillCameraSide: DrillCameraSide = DrillCameraSide.LEFT,
    freestyleViewMode: FreestyleViewMode = FreestyleViewMode.UNKNOWN,
    scaleMode: PoseScaleMode = PoseScaleMode.FILL,
) {
    Canvas(modifier = modifier) {
        val joints = frame?.joints.orEmpty()
        val sourceWidth = frame?.analysisWidth ?: 0
        val sourceHeight = frame?.analysisHeight ?: 0
        val rotation = frame?.analysisRotationDegrees ?: 0
        val previewContentRect = computePreviewContentRect(
            canvasWidth = size.width,
            canvasHeight = size.height,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            rotationDegrees = rotation,
            scaleMode = scaleMode,
        )
        val model = OverlayGeometry.build(drillType, sessionMode, joints, drillCameraSide, freestyleViewMode)
        OverlayFrameRenderer.drawAndroid(
            canvas = drawContext.canvas.nativeCanvas,
            width = size.width.toInt().coerceAtLeast(1),
            height = size.height.toInt().coerceAtLeast(1),
            model = model,
            frame = OverlayDrawingFrame(
                drawSkeleton = joints.isNotEmpty(),
                drawIdealLine = showIdealLine,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                sourceRotationDegrees = rotation,
                mirrored = frame?.mirrored ?: false,
                previewContentRect = previewContentRect,
                scaleMode = scaleMode,
                enableProjectionDiagnostics = showDebugOverlay,
            ),
        )

        if (showDebugOverlay) {
            renderMetricDebug(debugMetrics)
            renderAngleDebug(debugAngles)
            renderFaultAndPhaseDebug(currentPhase, activeFault, cueText)
        }
    }
}

private fun computePreviewContentRect(
    canvasWidth: Float,
    canvasHeight: Float,
    sourceWidth: Int,
    sourceHeight: Int,
    rotationDegrees: Int,
    scaleMode: PoseScaleMode,
): Rect {
    val container = Rect(0f, 0f, canvasWidth, canvasHeight)
    if (sourceWidth <= 0 || sourceHeight <= 0 || canvasWidth <= 0f || canvasHeight <= 0f) {
        return container
    }
    if (scaleMode == PoseScaleMode.FILL) {
        return container
    }
    val normalizedRotation = ((rotationDegrees % 360) + 360) % 360
    val rotated = normalizedRotation == 90 || normalizedRotation == 270
    val effectiveSourceWidth = if (rotated) sourceHeight.toFloat() else sourceWidth.toFloat()
    val effectiveSourceHeight = if (rotated) sourceWidth.toFloat() else sourceHeight.toFloat()
    if (effectiveSourceWidth <= 0f || effectiveSourceHeight <= 0f) {
        return container
    }

    val sourceAspect = effectiveSourceWidth / effectiveSourceHeight
    val canvasAspect = canvasWidth / canvasHeight
    return if (sourceAspect > canvasAspect) {
        val contentHeight = canvasWidth / sourceAspect
        val top = (canvasHeight - contentHeight) / 2f
        Rect(0f, top, canvasWidth, top + contentHeight)
    } else {
        val contentWidth = canvasHeight * sourceAspect
        val left = (canvasWidth - contentWidth) / 2f
        Rect(left, 0f, left + contentWidth, canvasHeight)
    }
}

private fun DrawScope.renderAngleDebug(angles: List<AngleDebugMetric>) {
    var y = size.height * 0.12f
    angles.take(5).forEach {
        drawText("${it.key}: ${it.degrees.toInt()}°", Offset(size.width * 0.04f, y), Color(0xFFB3F5FC))
        y += 34f
    }
}

private fun DrawScope.renderMetricDebug(metrics: List<AlignmentMetric>) {
    var y = size.height * 0.35f
    metrics.take(4).forEach {
        drawText("${it.key}: ${it.score}", Offset(size.width * 0.04f, y), Color(0xFFE2E8F0), 22f)
        y += 28f
    }
}

private fun DrawScope.renderFaultAndPhaseDebug(phase: String, fault: String, cueText: String) {
    drawText("Phase: ${phase.uppercase()}", Offset(size.width * 0.04f, size.height * 0.72f), Color.White)
    drawText(
        "Active Fault: ${fault.ifBlank { "none" }}",
        Offset(size.width * 0.04f, size.height * 0.76f),
        if (fault.isBlank()) Color.LightGray else Color(0xFFFFC857),
    )
    if (cueText.isNotBlank()) {
        drawText("Cue: ${cueText.take(56)}", Offset(size.width * 0.04f, size.height * 0.8f), Color(0xFFC4F1BE), 24f)
    }
}

private fun DrawScope.drawText(text: String, at: Offset, color: Color, textSize: Float = 26f) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        at.x,
        at.y,
        Paint().apply {
            this.color = color.toArgbCompat()
            this.textSize = textSize
            isAntiAlias = true
        },
    )
}
