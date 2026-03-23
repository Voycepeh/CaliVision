# App Modules

## UI Packages

- `ui/home`, `ui/startdrill`, `ui/drilldetail`, `ui/live`, `ui/upload`, `ui/results`, `ui/history`, `ui/settings`
- `ui/components`, `ui/common`, and `ui/navigation` provide shared UI primitives and routes.

## Orchestration and Flow Control

- `ui/live/LiveCoachingViewModel.kt` owns live session state transitions and finalization sequencing.
- Upload flow classes under `ui/upload` coordinate imported-video analysis lifecycle.

## Media and Recording

- `camera/CameraSessionManager.kt` handles camera/recording session control.
- `recording/*` includes annotated export pipeline, normalization, overlay timeline, and media verification.
- `overlay/*` handles overlay render geometry and frame rendering support.

## Analysis and Domain Logic

- `pose/*` frame validation, smoothing, coordinate mapping.
- `motion/*` movement, phase, and quality analysis.
- `biomechanics/*` drill-specific scoring and issue classification.
- `movementprofile/*` imported-video pose sourcing and profile compatibility.
- `calibration/*` user calibration session and body profile logic.

## Data and Storage

- `storage/repository/SessionRepository.kt` is the main persistence boundary.
- `storage/db/*` provides Room entities, converters, and DAOs.
- `storage/SessionBlobStorage.kt` persists media artifacts.
