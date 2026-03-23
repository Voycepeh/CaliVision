# ADR-001: Annotated Export Strategy

## Context

Live and imported flows need replay assets with coaching overlays, but export can fail or complete later than recording.

## Decision

Track raw and annotated artifacts separately. Run annotated export asynchronously after core session data is persisted.

## Consequences

- Users can still access replay from raw capture when export is unavailable.
- Pipeline status must be persisted and surfaced explicitly.
- Diagnostics are required for async export troubleshooting.
