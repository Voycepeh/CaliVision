# AGENTS Guide for CaliVision

## Project purpose

CaliVision is a drill-centric Android app for calisthenics coaching and analysis. Product direction emphasizes connected workflows across live coaching, upload/reference training, replay/history.

## Toolchain requirements

- JDK 17
- Android SDK 34
- Local `gradle` installation (Gradle wrapper is not checked in)

## Main validation commands

```bash
gradle testDebugUnitTest
gradle :app:assembleDebug
```

## Branch and PR rules

- Branch from `main`.
- Target PRs to `main`.
- Keep PRs focused and scoped to one primary concern.

## Architecture and coding guardrails

- Preserve drill-centric flow integrity.
- Keep terminology aligned with the current UX labels.
- Do not silently break drill metadata/catalog/schema behavior.
- Do not silently break replay/export/upload workflows.
- Avoid duplicate entry points or overlapping controls that create ambiguous outcomes.

## Documentation and diagrams rule (required)

Any PR changing UX flow, navigation, architecture, media pipeline, or terminology **must** update relevant markdown docs and Mermaid diagrams in the same PR.

## Repo cleanup/doc-update behavior

For cleanup and doc-focused tasks, proactively refresh `README.md`, architecture docs, feature docs, and diagrams so they match current code behavior.

## Owner preference

The repo owner prefers PR-ready execution against `main` (implemented changes + PR), not patch-only suggestions.
