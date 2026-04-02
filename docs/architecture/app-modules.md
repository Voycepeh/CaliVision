# App Modules

This page gives a scan-friendly map of key modules and why they exist.

## 1) UI and navigation

- `ui/home`, `ui/navigation`: entry points and route wiring.
- `ui/drills`, `ui/drilldetail`, `ui/startdrill`: drill hub, drill management, and launch paths.
- `ui/live`: live coaching screens and state.
- `ui/upload`: upload/reference training flows.
- `ui/results`, `ui/history`: outcomes, replay, and prior sessions.
- `ui/settings`: profile and calibration entry points.

## 2) Workflow orchestrators

- Live session orchestration (`ui/live/*`) controls countdown gating, start/stop, finalization, and post-session transitions.
- Upload orchestration (`ui/upload/*`) controls import analysis, optional reference creation, and persistence.
- Drill authoring orchestration (`ui/drills/*`) controls editing, validation, and save behavior in Drill Studio.

## 3) Drill + reference domain

- `drills/*`: drill definitions, registry/catalog, drill metadata, authoring support.
- `movementprofile/*`: extracted movement/reference data from uploads or prior sessions.
- Drill-linked reference/template logic spans drill domain + movement profile + persistence.

## 4) Calibration + analysis

- `calibration/*`: user profiles, calibration history, active profile resolution.
- `pose/*`: frame validity, smoothing, coordinate mapping.
- `motion/*`: phase/movement analysis.
- `biomechanics/*`: drill-specific scoring and issue classification.

## 5) Media, overlay, and export

- `camera/*`: capture lifecycle and recorder interaction.
- `overlay/*`: live overlay geometry/render support.
- `recording/*`: timeline capture, normalization, annotated render, verification.

## 6) Persistence

- `storage/db/*`: Room schema + DAOs.
- `storage/repository/*`: session/drill/profile persistence boundaries.
- `storage/SessionBlobStorage.kt`: media artifact persistence.

## Practical rule of thumb

If a change affects user workflow semantics (start gating, save behavior, replay selection, reference creation), update the matching architecture + feature docs and corresponding diagrams in the same PR.
