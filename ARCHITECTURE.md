# Architecture Guide

This is the top-level architecture map for the current product direction on `main`: a **drill-centric coaching app** with connected workflows for live practice, drill authoring, upload/reference training, replay/history, and calibration profiles.

## Current implemented shape

### Product workflow anchors

- **Home / Drill Hub** is the primary navigation anchor.
- **Manage Drills** and **Drill Studio** own drill catalog and authoring behavior.
- **Live Session** owns countdown gating, live coaching loop, and stop/finalize transitions.
- **Upload / Reference Training** owns imported analysis and optional reference-template creation.
- **Results / Session History** owns persisted outcomes and replay access.
- **Calibration / Profiles** provides cross-workflow context for analysis behavior.

### Technical layers

1. **UI/navigation** (`ui/*`)
2. **Workflow orchestration** (`ui/live`, `ui/upload`, drill flow view models)
3. **Domain** (`drills/*`, `movementprofile/*`, `calibration/*`)
4. **Analysis** (`pose/*`, `motion/*`, `biomechanics/*`)
5. **Media/recording/export** (`recording/*`, `camera/*`, `overlay/*`)
6. **Persistence** (`storage/*`)

### Ownership boundaries

- **Drill authoring ownership**: drill studio view models + drill persistence mapping.
- **Reference/comparison ownership**: movement profile/template creation and drill association flows.
- **Calibration/profile ownership**: active profile resolution and calibration data history.
- **Live lifecycle ownership**: session orchestration, countdown gating, finalize transitions.
- **Replay/export ownership**: export pipeline, validation, media resolver, and session repository updates.

## Future direction (intentional)

- Keep drill workflows simple and deterministic.
- Prefer one obvious save path in Drill Studio.
- Avoid duplicate UX actions that produce the same outcome.
- Keep replay decisions resilient: annotated-first when verified, raw fallback when needed.
- Keep calibration/profile context available to all coaching workflows, not isolated settings pages.

See [ADR-004](docs/decisions/adr-004-product-workflow-simplification.md).

## Architecture index

- [System overview](docs/architecture/system-overview.md)
- [App modules](docs/architecture/app-modules.md)
- [Session lifecycle](docs/architecture/session-lifecycle.md)
- [Video pipeline](docs/architecture/video-pipeline.md)
- [Replay and fallback](docs/architecture/replay-and-fallback.md)
- [Calibration and scoring](docs/architecture/calibration-and-scoring.md)
- [Overlay rendering](docs/architecture/overlay-rendering.md)

## Diagram index

- [UI flow](docs/diagrams/ui-flow.md)
- [Class diagram](docs/diagrams/class-diagram.md)
- [Sequence: live session](docs/diagrams/sequence-live-session.md)
- [Sequence: import analysis](docs/diagrams/sequence-import-analysis.md)
- [Sequence: export finalization](docs/diagrams/sequence-export-finalization.md)
