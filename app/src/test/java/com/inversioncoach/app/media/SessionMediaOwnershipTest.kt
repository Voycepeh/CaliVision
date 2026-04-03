package com.inversioncoach.app.media

import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.SessionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionMediaOwnershipTest {

    @Test
    fun canonicalUris_preferFinalThenPrimaryThenMaster() {
        val session = baseSession.copy(
            rawVideoUri = "file:///raw_primary.mp4",
            rawMasterUri = "file:///raw_master.mp4",
            rawFinalUri = "file:///raw_final.mp4",
            annotatedVideoUri = "file:///annotated_primary.mp4",
            annotatedMasterUri = "file:///annotated_master.mp4",
            annotatedFinalUri = "file:///annotated_final.mp4",
        )

        assertEquals("file:///raw_final.mp4", SessionMediaOwnership.canonicalRawUri(session))
        assertEquals("file:///annotated_final.mp4", SessionMediaOwnership.canonicalAnnotatedUri(session))
    }

    @Test
    fun rawReplayPlayable_respectsRawInvalidFailures() {
        val session = baseSession.copy(
            rawVideoUri = "file:///raw.mp4",
            rawPersistFailureReason = "RAW_MEDIA_CORRUPT",
        )

        assertFalse(SessionMediaOwnership.rawReplayPlayable(session))
    }

    @Test
    fun rawReplayPlayable_requiresBestPlayableToMatchRawWhenPresent() {
        val session = baseSession.copy(
            rawVideoUri = "file:///raw.mp4",
            bestPlayableUri = "file:///annotated.mp4",
        )

        assertFalse(SessionMediaOwnership.rawReplayPlayable(session))
        assertTrue(SessionMediaOwnership.rawReplayPlayable(session.copy(bestPlayableUri = "file:///raw.mp4")))
    }

    private val baseSession = SessionRecord(
        id = 1L,
        title = "Session",
        drillType = DrillType.FREESTYLE,
        startedAtMs = 1L,
        completedAtMs = 2L,
        overallScore = 0,
        strongestArea = "",
        limitingFactor = "",
        issues = "",
        wins = "",
        metricsJson = "{}",
        annotatedVideoUri = null,
        rawVideoUri = null,
        notesUri = null,
        bestFrameTimestampMs = null,
        worstFrameTimestampMs = null,
        topImprovementFocus = "",
    )
}
