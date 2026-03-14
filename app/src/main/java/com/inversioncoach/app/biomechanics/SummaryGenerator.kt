package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.DrillType

class SummaryGenerator {
    fun generate(
        drill: DrillType,
        score: DrillScoreBreakdown,
        issues: List<IssueInstance>,
        bestRepOrWindow: String,
        worstRepOrWindow: String,
        trendDelta: Int?,
        recommendation: DrillRecommendation,
    ): SessionNarrative {
        val strongest = score.strongestArea.replace('_', ' ')
        val limiter = score.mainLimiter.replace('_', ' ')
        val trend = when {
            trendDelta == null -> "Trend data not available yet."
            trendDelta > 3 -> "Consistency is improving versus recent sessions."
            trendDelta < -3 -> "Consistency dipped versus recent sessions."
            else -> "Consistency is stable versus recent sessions."
        }
        val topIssue = issues.groupBy { it.type }.maxByOrNull { it.value.size }?.key
        val breakdownText = topIssue?.name?.lowercase()?.replace('_', ' ') ?: limiter
        return SessionNarrative(
            whatWentWell = "Best quality was in $strongest. $bestRepOrWindow.",
            whatBrokeDown = "Main breakdown was $breakdownText.",
            whereItBrokeDown = "$worstRepOrWindow.",
            focusNext = "Focus next on $limiter with one cue at a time. $trend",
            nextDrillSuggestion = recommendation.drillName,
        )
    }
}
