package com.inversioncoach.app.summary

import com.inversioncoach.app.model.DrillScore
import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.SessionSummary

class SummaryGenerator(
    private val recommendationEngine: RecommendationEngine,
) {
    fun generate(
        drillType: DrillType,
        score: DrillScore,
        issues: List<String>,
        wins: List<String>,
    ): SessionSummary {
        val recommendation = recommendationEngine.recommend(drillType, score.limitingFactor)
        val topIssue = issues.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        val best = wins.firstOrNull() ?: "Best segment showed stable alignment"
        val worst = issues.firstOrNull() ?: "No major breakdown"
        return SessionSummary(
            headline = "Score ${score.overall}: strongest ${score.strongestArea.replace('_', ' ')}, limiter ${score.limitingFactor.replace('_', ' ')}.",
            whatWentWell = listOf("$best.", "Strongest area: ${score.strongestArea.replace('_', ' ')}."),
            whatBrokeDown = listOf("Primary issue: ${topIssue ?: score.limitingFactor}.", "Most visible in lower-quality segments."),
            whereItBrokeDown = "Worst moment: $worst.",
            nextFocus = "Next focus: ${score.limitingFactor.replace('_', ' ')} with one consistent cue.",
            recommendedDrill = recommendation,
            issueTimeline = issues.take(5).mapIndexed { i, issue -> "Phase ${i + 1}: $issue" },
        )
    }
}
