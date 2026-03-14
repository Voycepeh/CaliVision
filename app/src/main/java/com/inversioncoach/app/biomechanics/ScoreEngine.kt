package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.DrillType
import kotlin.math.abs

class ScoreEngine {
    private val weights = mapOf(
        DrillType.CHEST_TO_WALL_HANDSTAND to mapOf("line_quality" to 30, "shoulder_openness" to 25, "scapular_elevation" to 20, "rib_pelvis_control" to 15, "leg_tension" to 10),
        DrillType.BACK_TO_WALL_HANDSTAND to mapOf("shoulder_push" to 25, "reduced_arch" to 25, "hip_stack" to 20, "wall_reliance" to 20, "leg_tension" to 10),
        DrillType.PIKE_PUSH_UP to mapOf("hip_height" to 25, "shoulder_loading" to 25, "head_path" to 20, "elbow_path" to 15, "tempo_control" to 15),
        DrillType.ELEVATED_PIKE_PUSH_UP to mapOf("loading_angle" to 25, "depth" to 20, "pressing_path" to 20, "lockout" to 15, "tempo_control" to 20),
        DrillType.NEGATIVE_WALL_HANDSTAND_PUSH_UP to mapOf("top_position" to 20, "descent_control" to 30, "path_consistency" to 20, "line_retention" to 20, "bottom_position" to 10),
    )

    fun score(drill: DrillType, subScores: Map<String, Int>): DrillScoreBreakdown {
        val w = weights[drill] ?: emptyMap()
        val overall = if (w.isEmpty()) 0 else (w.entries.sumOf { (k, wt) -> (subScores[k] ?: 50) * wt } / 100).coerceIn(0, 100)
        val strongest = subScores.maxByOrNull { it.value }?.key ?: "consistency"
        val limiter = subScores.minByOrNull { it.value }?.key ?: "consistency"
        return DrillScoreBreakdown(overall, subScores, strongest, limiter)
    }

    fun consistencyScore(values: List<Int>): Int {
        if (values.isEmpty()) return 0
        val avg = values.average().toFloat()
        val mad = values.map { abs(it - avg) }.average().toFloat()
        return (100 - mad * 2).toInt().coerceIn(0, 100)
    }
}
