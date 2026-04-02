# Video Pipeline

The media pipeline is shared by live-session finalization and upload/reference analysis outcomes.

## Inputs

- Raw media callback URI (live flow) or imported media URI (upload flow).
- Overlay timeline frames (live captured or import-synthesized).
- Resolved metadata (orientation, duration, drill context, timestamps).

## Pipeline stages

1. **Source acceptance**: validate callback/import ownership and URI readiness.
2. **Raw persistence**: copy/retain source media and persist status.
3. **Timeline resolution**: freeze/serialize overlay timeline against session truth.
4. **Normalization**: resolve duration/orientation/render constraints.
5. **Annotated render**: produce export output when possible.
6. **Verification**: inspect playable/readable media.
7. **Replay resolution**: choose best verified replay asset and persist.

## Annotated-first with raw fallback

- Annotated output is preferred when export + verification succeed.
- Raw media remains the safety path when annotated output is missing/invalid.
- Session truth must not depend on annotated export success.

## Why this matters

This design keeps coaching workflows resilient across devices/codecs while preserving a practical user outcome: a replayable session whenever capture/import succeeded.
