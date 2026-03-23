# System Overview

Inversion Coach combines realtime coaching and offline imported-video analysis into a shared session model.

## Runtime Subsystems

- **UI/navigation**: Compose screens route users through home, drill selection, live coaching, upload analysis, history, and results.
- **Live orchestration**: `LiveCoachingViewModel` coordinates countdown, frame ingestion, session stop/finalization, and export kickoff.
- **Upload orchestration**: upload flow view model/runner coordinates imported asset processing and persistence.
- **Analysis engines**: pose processing, biomechanics, readiness/fault detection, and summary generation.
- **Recording/export**: timeline capture, normalization, annotated composition, media verification, compression/cleanup.
- **Persistence**: Room + blob storage through `SessionRepository` and `SessionBlobStorage`.

## Cross-Flow Invariants

1. Session metadata is persisted even when annotated export fails.
2. Replay is selected from validated assets, preferring annotated output when ready.
3. Raw capture remains a fallback when annotated output is unavailable/invalid.
4. Export/finalization events are diagnostics-heavy for async troubleshooting.
