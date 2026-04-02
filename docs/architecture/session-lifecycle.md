# Session Lifecycle

This document describes the drill-centric live session lifecycle from drill selection through final persistence.

## End-to-end lifecycle

1. **Drill selection** from Home / Drill Hub.
2. **Effective/resolved view** is computed (drill options + runtime context).
3. **Countdown gating** runs and confirms ready-to-start boundaries.
4. **Session starts** and recorder/analysis loop activates.
5. **Overlay + cue loop** processes frames during active coaching.
6. **Stop requested** by user.
7. **Finalize callback accepted** (deduped/ownership-checked).
8. **Raw media persisted + verified**.
9. **Annotated export launched** with resolved timeline metadata.
10. **Replay source resolved** from verified candidates.
11. **Session persisted as final outcome** and shown in Results / Session History.

## State boundaries to protect

- **Pre-start**: drill context, permissions, camera readiness, resolved options.
- **Countdown**: visible warm-up without marking the session active early.
- **Active live loop**: frame ingestion, scoring/cues, overlay timeline capture.
- **Finalize boundary**: stop → callback handling → raw persistence.
- **Post-finalize async**: export, verification, resolver, repository update.

## Failure behavior

- Ignore duplicate/invalid finalize callbacks.
- If raw persistence fails, keep explicit failure status and diagnostics.
- If annotated export fails, keep session truth and raw fallback path.
- If media verification fails for one asset, resolver must select the next valid candidate.
