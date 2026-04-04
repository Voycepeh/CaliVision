package com.inversioncoach.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.inversioncoach.app.drills.catalog.SkeletonTemplate
import com.inversioncoach.app.drills.catalog.StickFigureAnimator

data class SeededSkeletonPreviewPolicy(
    val renderPolicy: SkeletonRenderPolicy,
)

object SeededSkeletonPreviewDefaults {
    const val PORTRAIT_ASPECT_RATIO: Float = OverlaySkeletonPreviewDefaults.PORTRAIT_ASPECT_RATIO

    val DefaultPolicy = SeededSkeletonPreviewPolicy(
        renderPolicy = SkeletonRenderContract.SharedPolicy,
    )

    fun normalizeJointNames(joints: Map<String, com.inversioncoach.app.drills.catalog.JointPoint>) =
        OverlaySkeletonPreviewDefaults.normalizeJointNames(joints)
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
        StickFigureAnimator.poseAt(template, progress)
    }
    OverlaySkeletonPreview(
        joints = pose,
        modifier = modifier,
        style = OverlaySkeletonPreviewStyle(
            aspectRatio = policy.renderPolicy.aspectRatio,
            policy = policy.renderPolicy,
            styleScaleMultiplier = policy.renderPolicy.styleScaleMultiplier,
        ),
    )
}
