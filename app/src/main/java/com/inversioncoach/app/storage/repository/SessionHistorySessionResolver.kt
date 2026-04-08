package com.inversioncoach.app.storage.repository

import com.inversioncoach.app.model.SessionRecord

internal fun SessionRecord.resolvedDrillId(): String? =
    drillId ?: metricsJson.parseInlineMetric("drillId")

internal fun String.parseInlineMetric(key: String): String? =
    split('|')
        .firstOrNull { token -> token.startsWith("$key:") }
        ?.substringAfter(':')
        ?.takeIf { it.isNotBlank() }

internal fun List<SessionRecord>.filterForDrill(drillId: String?): List<SessionRecord> =
    if (drillId.isNullOrBlank()) this else filter { session -> session.resolvedDrillId() == drillId }

internal fun SessionRecord.isReviewableSession(): Boolean =
    hasPlayableMediaSource() || hasMeaningfulAnalysisOutput() || isExplicitlyPreservedByUser()

private fun SessionRecord.hasPlayableMediaSource(): Boolean =
    !bestPlayableUri.isNullOrBlank() || !annotatedVideoUri.isNullOrBlank() || !rawVideoUri.isNullOrBlank()

private fun SessionRecord.isExplicitlyPreservedByUser(): Boolean =
    !referenceTemplateId.isNullOrBlank() || !notesUri.isNullOrBlank()

private fun SessionRecord.hasMeaningfulAnalysisOutput(): Boolean {
    if (repCountFromMetrics() > 0) return true
    if (holdSecondsFromMetrics() > 0) return true
    if (totalAlignedDurationMsFromMetrics() > 0L) return true
    if (uploadAnalysisProcessedFrames > 0) return true
    if (overlayFrameCount > 0) return true
    if (issues.isNotBlank() || wins.isNotBlank()) return true
    return false
}

private fun SessionRecord.repCountFromMetrics(): Int =
    metricsJson.parseInlineMetric("repCount")?.toIntOrNull()
        ?: metricsJson.parseInlineMetric("acceptedReps")?.toIntOrNull()
        ?: 0

private fun SessionRecord.holdSecondsFromMetrics(): Int =
    metricsJson.parseInlineMetric("holdSeconds")?.toIntOrNull()
        ?: 0

private fun SessionRecord.totalAlignedDurationMsFromMetrics(): Long =
    metricsJson.parseInlineMetric("totalAlignedDurationMs")?.toLongOrNull()
        ?: 0L
