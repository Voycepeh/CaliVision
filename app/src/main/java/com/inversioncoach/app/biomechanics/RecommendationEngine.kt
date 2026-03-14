package com.inversioncoach.app.biomechanics

class RecommendationEngine {
    fun recommend(issueFrequency: Map<IssueType, Int>): DrillRecommendation {
        val issue = issueFrequency.maxByOrNull { it.value }?.key
        return when (issue) {
            IssueType.BANANA_ARCH -> DrillRecommendation("Chest-to-wall line work", "Repeated arch proxy detected", "Tuck ribs")
            IssueType.PASSIVE_SHOULDERS, IssueType.SHOULDER_COLLAPSE -> DrillRecommendation("Wall shoulder shrugs", "Shoulder activity dropped repeatedly", "Push taller")
            IssueType.HIPS_TOO_LOW -> DrillRecommendation("Pike setup elevation holds", "Hip height stayed low", "Lift hips higher")
            IssueType.RUSHED_DESCENT -> DrillRecommendation("Tempo negatives (3-count)", "Eccentric too fast", "Slow the descent")
            IssueType.WALL_RELIANCE -> DrillRecommendation("Heel pull-away practice", "Wall dependence remained high", "Use less wall pressure")
            IssueType.ELBOWS_FLARING -> DrillRecommendation("Narrow-grip path reps", "Elbow flare persisted", "Keep elbows in")
            else -> DrillRecommendation("Repeat drill with single cue focus", "No dominant breakdown", "Maintain current best cue")
        }
    }
}
