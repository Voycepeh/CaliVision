package com.inversioncoach.app.recording

import com.inversioncoach.app.model.AnnotatedExportFailureReason
import org.junit.Assert.assertEquals
import org.junit.Test

class AnnotatedVideoComposerTest {
    @Test
    fun composerResultSupportsFailureCodes() {
        val result = ComposerResult(uri = null, failureReason = AnnotatedExportFailureReason.OVERLAY_TIMELINE_EMPTY.name)
        assertEquals(AnnotatedExportFailureReason.OVERLAY_TIMELINE_EMPTY.name, result.failureReason)
    }
}
