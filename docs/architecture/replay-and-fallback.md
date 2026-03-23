# Replay and Fallback

Replay is selected from persisted candidates using readiness + validation rules.

## Candidate Priority

1. Validated annotated replay when export is complete and playable.
2. Persisted raw replay when annotated export is unavailable or invalid.
3. No replay asset (explicit failure state) when neither is usable.

## Why This Exists

- Export can fail independently of recording success.
- Media metadata/readability can differ across devices/codecs.
- Users should still receive a truthful replay whenever raw capture succeeded.

## Operational Signals

- `AnnotatedExportStatus`
- Raw persistence status
- Media verification/replay inspection results
- Retained asset type and best playable URI in finalized session outcome
