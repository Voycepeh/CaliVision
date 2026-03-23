# Inversion Coach / Handstand App

Inversion Coach is an Android app for live inversion coaching, pose-aware recording, imported-video analysis, and replay.

## Overview

The app supports two primary flows that share a common results/history surface:

- **Live coaching sessions** (camera + realtime overlays + post-session export)
- **Imported video analysis** (offline pose analysis + optional annotated export)

Both flows persist session data locally and resolve replay from a best-available source (annotated when valid, raw as fallback).

## Current Features

- Live coaching sessions with countdown and drill/freestyle modes
- Pose smoothing, correction, and issue detection in-session
- Overlay timeline capture for annotated replay/export
- Raw capture persistence and media verification
- Annotated export pipeline with progress/failure state
- Replay source resolution with explicit fallback rules
- Imported video analysis flow with stage-based UI
- Session history and results review
- Calibration/body profile support for analysis tuning

## Architecture at a Glance

- **UI**: Jetpack Compose screens under `app/src/main/java/com/inversioncoach/app/ui`
- **Session orchestration**: `LiveCoachingViewModel` + upload flow view models/coordinators
- **Recording/export**: `recording/*` pipeline (`SessionRecorder`, `AnnotatedExportPipeline`, normalization/verification helpers)
- **Analysis/scoring**: pose, biomechanics, motion, and movement profile packages
- **Persistence**: Room DAOs + `SessionRepository` + `SessionBlobStorage`

For deeper documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Key Documentation

- [System overview](docs/architecture/system-overview.md)
- [App modules](docs/architecture/app-modules.md)
- [Session lifecycle](docs/architecture/session-lifecycle.md)
- [Video pipeline](docs/architecture/video-pipeline.md)
- [Replay and fallback](docs/architecture/replay-and-fallback.md)
- [Calibration and scoring](docs/architecture/calibration-and-scoring.md)
- [UI flow diagram](docs/diagrams/ui-flow.md)

## Known Limitations

- Annotated export latency can exceed raw replay readiness on lower-end devices.
- Final playback correctness depends on metadata normalization and validation.
- Some export/fallback edge cases are still guarded by defensive diagnostics and verification.

## Running the Project

### Prerequisites

- JDK 17
- Android SDK (`compileSdk 34`, `targetSdk 34`, `minSdk 28`)
- Gradle 8.14.x (wrapper script is not checked in)

### Quick Start

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
java -version
gradle testDebugUnitTest
gradle :app:assembleDebug
```

## Testing

Primary unit test command:

```bash
gradle testDebugUnitTest
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
