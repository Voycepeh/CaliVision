package com.inversioncoach.app.summary

import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.Recommendation

class RecommendationEngine {
    fun recommend(drill: DrillType, limitingFactor: String): Recommendation {
        val key = limitingFactor.lowercase()
        return when {
            "rib" in key || "arch" in key || "line" in key -> Recommendation(
                title = "Hollow body + chest-to-wall line work",
                reason = "Line-control proxy indicates repeated arching; simpler hollow alignment drills are next.",
                drillFocus = DrillType.WALL_HANDSTAND,
            )
            "shoulder" in key || "scap" in key -> Recommendation(
                title = "Wall shoulder shrugs",
                reason = "Shoulder activity proxy dropped frequently; reinforce active elevation first.",
                drillFocus = DrillType.WALL_HANDSTAND,
            )
            "hip_height" in key || "hips" in key -> Recommendation(
                title = "Pike setup elevation focus",
                reason = "Hip stack/height stayed low; start with stricter setup before harder pressing.",
                drillFocus = DrillType.ELEVATED_PIKE_PUSH_UP,
            )
            "tempo" in key || "descent" in key -> Recommendation(
                title = "Tempo negatives (3-count)",
                reason = "Descent speed is too fast for controlled reps; use counted eccentrics.",
                drillFocus = DrillType.WALL_HANDSTAND_PUSH_UP,
            )
            "elbow" in key || "path" in key -> Recommendation(
                title = "Narrow-grip technical reps",
                reason = "Path and elbow tracking were inconsistent; use lighter path-focused sets.",
                drillFocus = drill,
            )
            else -> Recommendation(
                title = "Repeat with single-cue focus",
                reason = "No single dominant limiter; keep one cue and build consistency.",
                drillFocus = drill,
            )
        }
    }
}
