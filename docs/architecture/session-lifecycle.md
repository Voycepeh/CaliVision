# Session Lifecycle

This document captures the live coaching lifecycle and major async transitions.

## Happy Path

1. User enters live coaching and optionally sees countdown.
2. Recording starts; pose frames stream into analysis/overlay collectors.
3. User stops session.
4. Recorder finalize callback provides raw URI.
5. Raw artifact is persisted/verified.
6. Overlay timeline is frozen and resolved.
7. Annotated export runs asynchronously.
8. Replay source resolver chooses best playable URI (annotated preferred, raw fallback).
9. Session is finalized and appears in history/results.

## Important State Boundaries

- **Startup state**: permission/camera readiness/countdown.
- **Recording state**: live frame processing and cue/scoring updates.
- **Finalization state**: stop pressed -> finalize callback acceptance -> export launch.
- **Replay state**: readiness/validation determines selected replay asset.

## Failure and Fallback

- Empty/duplicate finalize callbacks are ignored/diagnosed.
- Raw persist failures are captured in status and diagnostics.
- Annotated export failures preserve session truth and fallback replay path.
- Verification failures force replay selection away from invalid assets.
