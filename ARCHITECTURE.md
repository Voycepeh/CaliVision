# Architecture Guide

This document is the top-level architecture map for Inversion Coach.

## Core Layers

1. **UI layer** (`ui/*`)
   - Compose screens, route wiring, and user interaction state.
2. **Orchestration layer** (`ui/live`, `ui/upload`)
   - Session lifecycle coordination and async pipeline control.
3. **Domain/analysis layer** (`pose/*`, `motion/*`, `biomechanics/*`, `movementprofile/*`, `calibration/*`)
   - Pose processing, scoring, phase detection, readiness, and calibration.
4. **Recording/export layer** (`recording/*`, `camera/*`, `overlay/*`)
   - Capture, overlay timeline, normalization, rendering, verification, replay source prep.
5. **Data/persistence layer** (`storage/*`)
   - Session metadata storage, blob persistence, and repository APIs.

## Critical Ownership Boundaries

- **Session state owner**: `LiveCoachingViewModel`
- **Export pipeline owner**: `AnnotatedExportPipeline` (+ normalization/verification helpers)
- **Replay source decision owner**: replay helpers used by live/results flows
- **Persistence boundary**: `SessionRepository` / `SessionBlobStorage`

## Most Important Architecture Docs

- [System overview](docs/architecture/system-overview.md)
- [App modules and boundaries](docs/architecture/app-modules.md)
- [Session lifecycle](docs/architecture/session-lifecycle.md)
- [Video pipeline](docs/architecture/video-pipeline.md)
- [Overlay rendering](docs/architecture/overlay-rendering.md)
- [Replay and fallback](docs/architecture/replay-and-fallback.md)
- [Calibration and scoring](docs/architecture/calibration-and-scoring.md)

## Diagrams

- [Class diagram](docs/diagrams/class-diagram.md)
- [Live session sequence](docs/diagrams/sequence-live-session.md)
- [Import analysis sequence](docs/diagrams/sequence-import-analysis.md)
- [Export finalization sequence](docs/diagrams/sequence-export-finalization.md)
- [UI flow](docs/diagrams/ui-flow.md)

## Architectural Decisions

- [ADR-001 annotated export strategy](docs/decisions/adr-001-annotated-export-strategy.md)
- [ADR-002 replay fallback strategy](docs/decisions/adr-002-replay-fallback-strategy.md)
- [ADR-003 session duration source of truth](docs/decisions/adr-003-session-duration-source-of-truth.md)
