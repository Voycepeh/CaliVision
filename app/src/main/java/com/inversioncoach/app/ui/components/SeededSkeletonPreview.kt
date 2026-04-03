package com.inversioncoach.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.inversioncoach.app.drills.catalog.JointPoint
import com.inversioncoach.app.drills.catalog.SkeletonTemplate
import com.inversioncoach.app.drills.catalog.StickFigureAnimator
import kotlin.math.min

data class SeededSkeletonPreviewPolicy(
    val aspectRatio: Float,
    val contentPaddingFraction: Float,
    val boneColor: Color,
    val jointColor: Color,
)

object SeededSkeletonPreviewDefaults {
    const val PORTRAIT_ASPECT_RATIO: Float = 3f / 4f
    const val CONTENT_PADDING_FRACTION: Float = 0.12f
    private val PREVIEW_COLOR = Color(0xFF7CF0A9)
    val DefaultPolicy = SeededSkeletonPreviewPolicy(
        aspectRatio = PORTRAIT_ASPECT_RATIO,
        contentPaddingFraction = CONTENT_PADDING_FRACTION,
        boneColor = PREVIEW_COLOR,
        jointColor = PREVIEW_COLOR,
    )

    private val jointAliases = mapOf(
        "head" to "nose",
        "shoulder_left" to "left_shoulder",
        "shoulder_right" to "right_shoulder",
        "elbow_left" to "left_elbow",
        "elbow_right" to "right_elbow",
        "wrist_left" to "left_wrist",
        "wrist_right" to "right_wrist",
        "hip_left" to "left_hip",
        "hip_right" to "right_hip",
        "knee_left" to "left_knee",
        "knee_right" to "right_knee",
        "ankle_left" to "left_ankle",
        "ankle_right" to "right_ankle",
    )

    val canonicalBones: List<Pair<String, String>> =
        StickFigureAnimator.canonicalBones + listOf(
            "left_shoulder" to "left_elbow",
            "left_elbow" to "left_wrist",
            "right_shoulder" to "right_elbow",
            "right_elbow" to "right_wrist",
            "left_hip" to "left_knee",
            "left_knee" to "left_ankle",
            "right_hip" to "right_knee",
            "right_knee" to "right_ankle",
            "left_shoulder" to "right_shoulder",
            "left_hip" to "right_hip",
            "left_shoulder" to "left_hip",
            "right_shoulder" to "right_hip",
        )

    fun normalizeJointNames(joints: Map<String, JointPoint>): Map<String, JointPoint> = joints.entries
        .associate { (name, point) -> (jointAliases[name] ?: name) to point }
}

@Composable
fun rememberSeededSkeletonPreviewProgress(
    template: SkeletonTemplate,
    isPlaying: Boolean = true,
): Float {
    val frameCount = template.keyframes.size.coerceAtLeast(1)
    val fps = template.framesPerSecond.coerceAtLeast(1)
    val durationMillis = ((1000f * frameCount.toFloat()) / fps.toFloat()).toInt().coerceAtLeast(16)
    val transition = rememberInfiniteTransition(label = "seeded_skeleton_preview")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "seeded_skeleton_preview_progress",
    ).value
    return if (isPlaying) progress else 0f
}

@Composable
fun SeededSkeletonPreview(
    template: SkeletonTemplate,
    progress: Float,
    modifier: Modifier = Modifier,
    policy: SeededSkeletonPreviewPolicy = SeededSkeletonPreviewDefaults.DefaultPolicy,
) {
    val pose = remember(template, progress) {
        SeededSkeletonPreviewDefaults.normalizeJointNames(StickFigureAnimator.poseAt(template, progress))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(policy.aspectRatio)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)),
    ) {
        val minDim = min(size.width, size.height)
        val contentPadding = minDim * policy.contentPaddingFraction
        val contentWidth = (size.width - 2f * contentPadding).coerceAtLeast(1f)
        val contentHeight = (size.height - 2f * contentPadding).coerceAtLeast(1f)
        val boneStroke = (minDim * 0.02f).coerceAtLeast(2f)
        val jointRadius = (minDim * 0.028f).coerceAtLeast(3f)
        val jointStroke = (minDim * 0.012f).coerceAtLeast(1.5f)

        fun JointPoint.toContentOffset(): Offset = Offset(
            x = contentPadding + x * contentWidth,
            y = contentPadding + y * contentHeight,
        )

        SeededSkeletonPreviewDefaults.canonicalBones.forEach { (start, end) ->
            val a = pose[start]
            val b = pose[end]
            if (a != null && b != null) {
                drawLine(
                    color = policy.boneColor,
                    start = a.toContentOffset(),
                    end = b.toContentOffset(),
                    strokeWidth = boneStroke,
                    cap = StrokeCap.Round,
                )
            }
        }

        pose.values.forEach { point ->
            drawCircle(
                color = policy.jointColor,
                radius = jointRadius,
                center = point.toContentOffset(),
                style = Stroke(width = jointStroke),
            )
        }
    }
}
