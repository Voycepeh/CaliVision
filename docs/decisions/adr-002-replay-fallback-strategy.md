# ADR-002: Replay Fallback Strategy

## Context

Export/readability can vary across devices and codecs. Replay cannot depend on a single artifact.

## Decision

Use validated annotated replay when ready; otherwise fall back to validated raw replay.

## Consequences

- Replay remains available across more failure modes.
- Session outcome must store both candidate URIs and retained asset type.
- Media verification becomes part of replay selection.
