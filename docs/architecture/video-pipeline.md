# Video Pipeline

## Inputs

- Live recording raw URI from recorder finalization callback.
- Overlay timeline frames captured during live session or synthesized for import flow.
- Drill/session metadata (orientation, side, timestamps, duration signals).

## Pipeline Stages

1. **Raw source acceptance**: callback URI canonicalization and ownership checks.
2. **Raw persistence**: blob storage copy/retain and status updates.
3. **Timeline resolution**: overlay frames frozen/serialized for export.
4. **Normalization**: orientation, duration alignment, and render constraints.
5. **Annotated render/composition**: pipeline/compositor writes output media.
6. **Validation**: media verification and replay inspection.
7. **Replay selection**: best playable candidate persisted to session outcome.

## Source-of-Truth Rules

- Session duration is resolved from lifecycle timing and validated media metadata where available.
- Replay selection does not assume export success; it validates availability/readability.
- Raw and annotated outputs are both tracked to support resilience and diagnostics.
