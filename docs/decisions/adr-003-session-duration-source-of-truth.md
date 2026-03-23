# ADR-003: Session Duration Source of Truth

## Context

Lifecycle timing, callback timing, and media metadata can diverge during stop/finalization.

## Decision

Resolve duration from lifecycle/session timing with metadata-based validation/normalization during export and verification.

## Consequences

- Duration handling is explicit in normalization paths.
- Validation logic can reject mismatched media outcomes.
- Documentation/tests must cover edge cases near stop/finalize boundaries.
