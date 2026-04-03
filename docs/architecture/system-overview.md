# System Overview

CaliVision is a drill-centric coaching system with connected live, upload, replay/history, and calibration workflows.

## Workflow surfaces

- **Home / Drill Hub**: primary navigation and launch surface.
- **Manage Drills**: drill catalog maintenance.
- **Drill Studio**: create/edit drill definitions.
- **Live Session**: countdown-gated real-time coaching.
- **Upload / Reference Training**: imported clip analysis and optional drill-linked reference creation.
- **Results / Session History**: persisted outcomes and replay access.
- **Calibration / Profiles**: active body profile context for analysis.

## Runtime subsystems

- **Navigation/UI**: `ui/navigation`, feature screens in `ui/**`.
- **Workflow orchestrators**: `LiveCoachingViewModel`, `UploadVideoViewModel`, drill studio view models.
- **Domain**: `drills`, `movementprofile`, `calibration`.
- **Analysis**: `pose`, `motion`, `biomechanics`, coaching cues.
- **Media**: recording, overlay timeline, annotated export, replay resolver.
- **Persistence**: Room + repository + blob storage.

## Operational invariants

1. Drill context remains recoverable across live/upload/results/history flows.
2. Countdown/start gating prevents premature active-session state.
3. Session truth persists even if annotated export fails.
4. Replay prefers verified annotated output, then verified raw fallback.
5. Calibration/profile context is consistently applied to live and upload analysis.
6. Workflow/architecture naming changes require docs and diagrams updates in the same PR.
