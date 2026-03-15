package com.inversioncoach.app.ui.common

import java.text.DateFormat
import java.util.Date

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

