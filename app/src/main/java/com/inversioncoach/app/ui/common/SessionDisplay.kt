package com.inversioncoach.app.ui.common

import com.inversioncoach.app.model.SessionMode
import com.inversioncoach.app.model.SessionRecord
import com.inversioncoach.app.model.sessionMode
import java.text.DateFormat
import java.util.Date

private const val NOT_TRACKED = "Not tracked"

data class SessionSummaryDisplay(
    val wins: String,
    val issues: String,
    val improvement: String,
)

data class SessionMetrics(
    val trackingMode: String? = null,
    val validReps: Int? = null,
    val rawRepAttempts: Int? = null,
    val alignedDurationMs: Long? = null,
    val bestAlignedStreakMs: Long? = null,
    val sessionTrackedMs: Long? = null,
)

fun formatSessionDateTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return "-"
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestampMs))
}

fun formatSessionDuration(durationMs: Long): String {
    val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000L).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

fun computeSessionDurationMs(startedAtMs: Long, completedAtMs: Long): Long {
    if (startedAtMs <= 0L || completedAtMs <= 0L) return 0L
    return (completedAtMs - startedAtMs).coerceAtLeast(0L)
}

fun parseSessionMetrics(metricsJson: String): SessionMetrics {
    val pairs = metricsJson.split("|")
        .mapNotNull { token ->
            val idx = token.indexOf(':')
            if (idx <= 0) null else token.substring(0, idx) to token.substring(idx + 1)
        }
        .toMap()
    return SessionMetrics(
        trackingMode = pairs["trackingMode"],
        validReps = pairs["validReps"]?.toIntOrNull(),
        rawRepAttempts = pairs["rawRepAttempts"]?.toIntOrNull(),
        alignedDurationMs = pairs["alignedDurationMs"]?.toLongOrNull(),
        bestAlignedStreakMs = pairs["bestAlignedStreakMs"]?.toLongOrNull(),
        sessionTrackedMs = pairs["sessionTrackedMs"]?.toLongOrNull(),
    )
}

fun formatPrimaryPerformance(session: SessionRecord): String {
    val metrics = parseSessionMetrics(session.metricsJson)
    return if (metrics.trackingMode == "HOLD_BASED") {
        val aligned = formatSessionDuration(metrics.alignedDurationMs ?: 0L)
        val best = formatSessionDuration(metrics.bestAlignedStreakMs ?: 0L)
        val total = formatSessionDuration(metrics.sessionTrackedMs ?: computeSessionDurationMs(session.startedAtMs, session.completedAtMs))
        "Hold: $aligned aligned • Best streak: $best • Session: $total"
    } else {
        val valid = metrics.validReps ?: 0
        val raw = metrics.rawRepAttempts ?: valid
        "Reps: $valid valid${if (raw >= valid) " / $raw attempts" else ""}"
    }
}

fun formatLimiterText(session: SessionRecord?): String {
    if (session == null) return "-"
    return when (session.sessionMode()) {
        SessionMode.FREESTYLE -> NOT_TRACKED
        SessionMode.DRILL -> session.limitingFactor
    }
}

fun buildSessionSummaryDisplay(session: SessionRecord?): SessionSummaryDisplay {
    if (session == null) {
        return SessionSummaryDisplay(
            wins = "No wins captured yet",
            issues = "No issues captured",
            improvement = "-",
        )
    }
    return when (session.sessionMode()) {
        SessionMode.FREESTYLE -> SessionSummaryDisplay(
            wins = NOT_TRACKED,
            issues = NOT_TRACKED,
            improvement = NOT_TRACKED,
        )

        SessionMode.DRILL -> SessionSummaryDisplay(
            wins = session.wins.ifBlank { "No wins captured yet" },
            issues = session.issues.ifBlank { "No issues captured" },
            improvement = session.topImprovementFocus.ifBlank { "-" },
        )
    }
}
