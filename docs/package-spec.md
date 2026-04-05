# Studio Package Spec (Local IO Phase)

This document captures the **portable drill package contract boundary** used by CaliVision Studio (web) and Android runtime consumers.

## Contract goals

- Keep Studio import/export JSON aligned to Android-compatible semantics.
- Treat package JSON as the interoperability boundary.
- Keep this phase local-only (no backend persistence).

## Required fields (v1)

- `schemaVersion` (string)
- `manifest` with `packageId` and `title`
- `drills[]` non-empty
- each drill requires: `drillId`, `name`, `viewType` (`FRONT|SIDE|BACK`), `phases[]`
- each phase requires: `phaseId`, `name`, unique `order`, positive `durationMs`, `pose.joints`
- pose joints must use canonical names and normalized `x`,`y` values in `[0,1]`

## Validation philosophy

Validation returns structured issues:

- `severity`: `error | warning`
- `code`: stable machine-friendly issue key
- `message`: user-facing text
- `path`: optional JSON path for UI targeting

`error` issues block package load. `warning` issues remain loadable but visible in UI.

## Local workflow in this PR

1. Load bundled valid sample package.
2. Optional import from local JSON file picker.
3. Validate and map to Studio view models.
4. Render package/drill/phase preview panels.
5. Export current package as downloadable JSON.

## Out of scope (intentionally deferred)

- Pose editing
- MediaPipe integration
- Cloud publishing/storage/auth
- Multi-user package registry
