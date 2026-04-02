# Replay and Fallback

Replay resolution is deterministic and shared across live results, history, and upload-analysis outcomes.

## Candidate order

1. **Verified annotated media** (preferred).
2. **Verified raw media** (fallback).
3. **Explicit no-replay state** when neither candidate is valid.

## Decision inputs

- Annotated export status.
- Raw/annotated persistence outcomes.
- Media verification/readability checks.
- Resolver policy for preferred but unavailable candidates.

## Annotated-first with truthful fallback

The app should not hide failures by pretending annotated export always succeeds. It should also avoid blocking user review when raw media is available. Resolver output should reflect real persisted state.

## Contributor guidance

If replay behavior changes, update:

- `docs/architecture/video-pipeline.md`
- `docs/architecture/session-lifecycle.md`
- `docs/features/session-history.md`
- relevant sequence diagrams
