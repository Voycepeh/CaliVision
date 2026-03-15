package com.inversioncoach.app.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionDisplayTest {

    @Test
    fun parseSessionMetricsReadsDelimitedPairs() {
        val metrics = parseSessionMetrics(
            "status:valid|trackingMode:HOLD_BASED|validReps:3|rawRepAttempts:4|" +
                "alignmentRate:0.625|avgAlignment:74|avgStability:68|repFailureReason:depth",
        )

        assertEquals("HOLD_BASED", metrics.trackingMode)
        assertEquals(3, metrics.validReps)
        assertEquals(4, metrics.rawRepAttempts)
        assertEquals(0.625f, metrics.alignmentRate)
        assertEquals(74, metrics.avgAlignment)
        assertEquals(68, metrics.avgStability)
        assertEquals("depth", metrics.repFailureReason)
    }

    @Test
    fun parseSessionMetricsIgnoresMalformedSegments() {
        val metrics = parseSessionMetrics("trackingMode:HOLD_BASED|badToken|acceptedReps:2|invalidNumber:abc")

        assertEquals("HOLD_BASED", metrics.trackingMode)
        assertEquals(2, metrics.acceptedReps)
        assertNull(metrics.avgRepScore)
    }
}
