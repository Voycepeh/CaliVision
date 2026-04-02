# Contributing

## Setup

1. Install JDK 17 and Android SDK 34.
2. Ensure `gradle` is available locally (wrapper is not checked in).
3. Run:

   ```bash
   gradle testDebugUnitTest
   gradle :app:assembleDebug
   ```

## Branch and PR requirements

- Create feature branches from `main`.
- Target PRs to `main`.
- Keep each PR focused on one primary concern.

## Workflow and documentation expectations

- If you change workflow/UI behavior, update related docs and Mermaid diagrams in the same PR.
- Avoid stale terminology; use current workflow names consistently:
  - Home / Drill Hub
  - Manage Drills
  - Drill Studio
  - Live Session
  - Upload / Reference Training
  - Results / Session History
  - Calibration / Profiles
- Prefer simplicity and deterministic UX over feature sprawl.
- Avoid adding multiple controls that produce the same outcome.
- Ensure drill editing reliably reloads persisted data.

## Docs to update when relevant

- `README.md`
- `ARCHITECTURE.md`
- `docs/architecture/*`
- `docs/features/*`
- `docs/diagrams/*`
- `docs/decisions/*` (when behavior direction changes)

## Testing guidance

- Run targeted tests for touched modules first.
- Run full unit tests before merge when possible.
- Call out environment limitations or skipped checks clearly in the PR.
