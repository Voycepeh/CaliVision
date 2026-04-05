# Studio ↔ Android Compatibility (PR 2 update)

Studio PR 2 establishes local package IO foundations around the existing portable drill package contract.

## Compatibility boundary

- Package JSON remains the core handoff artifact.
- Studio import and export preserve Android-compatible structure and camera `viewType` semantics.
- No incompatible package format is introduced.

## What Studio now does locally

- Loads bundled sample package JSON.
- Imports local `.json` package files from the browser.
- Runs explicit validation and surfaces issue lists.
- Exports currently loaded package back to JSON with stable filename conventions.

## Temporary assumptions

- Local-only state (memory + browser download).
- No remote DB/object storage/auth.
- Validation targets structural safety and schema compliance, not end-to-end media verification.

## Why this supports Android interoperability

- Android and Studio can converge on shared package schema guarantees.
- Contract-level validation in Studio reduces malformed package ingestion risk before Android import.
- Future PRs can add pose canvas/editing while preserving the package boundary.
