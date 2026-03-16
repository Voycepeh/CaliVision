package com.inversioncoach.app.recording

import android.util.Log

private const val TAG = "OverlayTimelineRecorder"

class OverlayTimelineRecorder(
    private val startedAtMs: Long,
    private val sampleIntervalMs: Long = DEFAULT_SAMPLE_INTERVAL_MS,
) {
    private val frames = mutableListOf<OverlayTimelineFrame>()
    private var lastRecordedAtMs: Long = Long.MIN_VALUE

    fun record(frame: OverlayTimelineFrame) {
        if (frame.timestampMs < startedAtMs) return
        if (lastRecordedAtMs != Long.MIN_VALUE && frame.timestampMs - lastRecordedAtMs < sampleIntervalMs) return
        frames += frame
        lastRecordedAtMs = frame.timestampMs
        if (frames.size % LOG_SAMPLE_INTERVAL == 0) {
            Log.d(TAG, "overlay_sample_recorded samples=${frames.size} timestampMs=${frame.timestampMs}")
        }
    }

    fun snapshot(): OverlayTimeline = OverlayTimeline(
        startedAtMs = startedAtMs,
        sampleIntervalMs = sampleIntervalMs,
        frames = frames.toList(),
    )

    companion object {
        const val DEFAULT_SAMPLE_INTERVAL_MS = 80L
        private const val LOG_SAMPLE_INTERVAL = 20
    }
}
