package com.inversioncoach.app.ui.live

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStartTimestampResolutionTest {

    @Test
    fun resolveSessionStartedAtMs_prefersActivationBoundaryWhenPresent() {
        val activationMs = 1_000L
        val saveNowMs = 4_250L

        val resolved = resolveSessionStartedAtMs(
            sessionActivatedAtMs = activationMs,
            nowMs = saveNowMs,
        )

        assertEquals(activationMs, resolved)
    }

    @Test
    fun resolveSessionStartedAtMs_fallsBackToSaveTimeWhenActivationMissing() {
        val saveNowMs = 4_250L

        val resolved = resolveSessionStartedAtMs(
            sessionActivatedAtMs = 0L,
            nowMs = saveNowMs,
        )

        assertEquals(saveNowMs, resolved)
    }
}
