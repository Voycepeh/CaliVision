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
