# Contributing

## Setup

1. Install JDK 17 and Android SDK 34.
2. Ensure `gradle` is available locally (wrapper is not checked in).
3. Run:

```bash
gradle testDebugUnitTest
gradle :app:assembleDebug
```

## Branch and PR Expectations

- Keep changes focused on a single concern.
- Update docs when changing lifecycle, export, replay, or calibration behavior.
- Add/adjust unit tests for behavior changes in pipelines and state derivation.
- Prefer small, reviewable PRs with explicit risk notes.

## Documentation Requirements

When touching any of these systems, update matching docs:

- Session lifecycle: `docs/architecture/session-lifecycle.md`
- Video/export pipeline: `docs/architecture/video-pipeline.md`
- Replay resolution rules: `docs/architecture/replay-and-fallback.md`
- Diagrams for significant flow changes: `docs/diagrams/*`

## Testing Guidance

- Run targeted tests first (package-level if available).
- Run full unit test suite before merge.
- Include failing/flake notes in PR if environment limitations occur.
