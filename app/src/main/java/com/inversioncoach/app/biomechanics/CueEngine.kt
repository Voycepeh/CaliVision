package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.CueStyle

class CueEngine {
    private val issuePersistCounts = mutableMapOf<IssueType, Int>()
    private val lastCueByTextAt = mutableMapOf<String, Long>()
    private val lastCueByCategoryAt = mutableMapOf<String, Long>()
    private var lastEncouragementAt = 0L

    fun registerObservedIssues(issues: List<IssueType>) {
        IssueType.values().forEach { issue ->
            issuePersistCounts[issue] = if (issue in issues) (issuePersistCounts[issue] ?: 0) + 1 else 0
        }
    }

    fun persisted(): Map<IssueType, Int> = issuePersistCounts

    fun decide(
        profile: DrillThresholdProfile,
        confidenceLevel: ConfidenceLevel,
        issues: List<IssueInstance>,
        style: CueStyle,
        nowMs: Long,
    ): CueDecision? {
        if (confidenceLevel == ConfidenceLevel.LOW) {
            return withCooldown("setup", "Turn side-on and keep full body in frame", nowMs, profile.sameCueCooldownMs, style, null)
        }
        val major = issues.firstOrNull { it.severity == IssueSeverity.MAJOR }
        val chosen = major ?: issues.firstOrNull()
        if (chosen != null && (persisted()[chosen.type] ?: 0) >= profile.spokenPersistFrames) {
            val cue = issueCue(chosen.type, style)
            return withCategoryCooldown(cue.first, cue.second, nowMs, profile.sameCueCooldownMs, profile.sameIssueFamilyCooldownMs, style, chosen.type)
        }
        if (issues.isEmpty() && nowMs - lastEncouragementAt >= profile.encouragementCooldownMs) {
            lastEncouragementAt = nowMs
            return CueDecision("encouragement", style, styleText(style, "Nice line, hold that"), true, null)
        }
        return null
    }

    private fun issueCue(issue: IssueType, style: CueStyle): Pair<String, String> = when (issue) {
        IssueType.PASSIVE_SHOULDERS, IssueType.SHOULDER_COLLAPSE -> "shoulders" to styleText(style, "Push taller through shoulders")
        IssueType.BANANA_ARCH -> "hips_core" to styleText(style, "Tuck ribs")
        IssueType.HIPS_OFF_STACK, IssueType.HIPS_FOLDING, IssueType.HIPS_DRIFTING -> "hips_core" to styleText(style, "Keep hips stacked")
        IssueType.SOFT_KNEES -> "legs" to styleText(style, "Straighten knees")
        IssueType.WALL_RELIANCE -> "line" to styleText(style, "Use less wall pressure")
        IssueType.HIPS_TOO_LOW -> "hips_core" to styleText(style, "Lift hips higher")
        IssueType.HEAD_PATH_FORWARD -> "path" to styleText(style, "Head between hands")
        IssueType.RUSHED_DESCENT -> "tempo" to styleText(style, "Slow the descent")
        IssueType.INSUFFICIENT_DEPTH -> "path" to styleText(style, "Go lower with control")
        IssueType.INCOMPLETE_LOCKOUT -> "path" to styleText(style, "Finish the press")
        else -> "line" to styleText(style, "Bring hips over hands")
    }

    private fun withCooldown(category: String, text: String, nowMs: Long, cueCooldown: Long, style: CueStyle, issue: IssueType?): CueDecision? {
        val last = lastCueByTextAt[text] ?: 0L
        if (nowMs - last < cueCooldown) return null
        lastCueByTextAt[text] = nowMs
        lastCueByCategoryAt[category] = nowMs
        return CueDecision(category, style, text, false, issue)
    }

    private fun withCategoryCooldown(
        category: String,
        text: String,
        nowMs: Long,
        cueCooldown: Long,
        categoryCooldown: Long,
        style: CueStyle,
        issue: IssueType,
    ): CueDecision? {
        val cueLast = lastCueByTextAt[text] ?: 0L
        val categoryLast = lastCueByCategoryAt[category] ?: 0L
        if (nowMs - cueLast < cueCooldown || nowMs - categoryLast < categoryCooldown) return null
        lastCueByTextAt[text] = nowMs
        lastCueByCategoryAt[category] = nowMs
        return CueDecision(category, style, text, false, issue)
    }

    private fun styleText(style: CueStyle, concise: String): String = when (style) {
        CueStyle.CONCISE -> concise
        CueStyle.TECHNICAL -> when (concise) {
            "Push taller through shoulders" -> "Increase shoulder elevation"
            "Tuck ribs" -> "Reduce rib flare and bring hips in line"
            "Keep hips stacked" -> "Bring hips back over the wrist line"
            "Slow the descent" -> "Control the eccentric"
            else -> concise
        }
        CueStyle.ENCOURAGING -> when (concise) {
            "Push taller through shoulders" -> "Better, keep pushing tall"
            "Tuck ribs" -> "Nice, now tuck the ribs"
            "Slow the descent" -> "Good effort, control the lowering"
            else -> concise
        }
    }
}
