# Inversion Coach / Handstand App

Inversion Coach is an Android app for live inversion coaching, pose-aware recording, imported-video analysis, and replay.

It is built to help users practice handstand and inversion drills with:
- live coaching
- pose overlays
- uploaded video analysis
- session history
- annotated replay and export
- calibration-aware feedback

## Why this project exists

Practicing inversions is hard to judge in real time. This app helps users review posture, stacking, movement quality, and drill performance through camera-based analysis and replay.

The goal is not just to record video, but to turn practice into structured feedback.

## What the app can do

The app currently supports two primary flows:

1. **Live coaching**
   - camera-based session
   - countdown before start
   - drill mode or freestyle mode
   - realtime overlays and cues
   - post-session replay and export

2. **Imported video analysis**
   - analyze an existing video offline
   - generate pose-aware metrics and replay context
   - optionally export annotated output

Both flows persist session data locally and resolve replay from the best available source, preferring annotated output when valid and falling back to raw video when needed.

## Who this repo is for

This repo is useful for:
- users testing the Android app
- contributors improving coaching, overlays, export, or analysis
- AI-assisted builders who need a quick mental model of the system
- anyone trying to understand how the app works before changing code

## How to understand this repo

Start from the user journey, not the package list.

1. A user chooses a drill or freestyle mode
2. The app captures camera frames or imports a video
3. Pose frames are analyzed into alignment and movement signals
4. Overlays, cues, and results are generated
5. Raw and annotated session outputs are saved for replay and history

## Core concepts

### Calibration

Calibration stores body-specific values so analysis can become more consistent across users and drills.

### Drills

Drills define movement expectations, tracking mode, and scoring logic.

### Live coaching

Live coaching handles countdown, recording, overlays, cues, and post-session finalization.

### Imported video analysis

Imported video analysis applies the same general analysis idea to previously recorded footage.

### Replay and export

Sessions can be reviewed using annotated output when available, with raw video fallback when needed.

## Architecture at a glance

The app can be understood in five layers:

1. **UI layer**
   - Compose screens and interaction flow

2. **Orchestration layer**
   - session lifecycle
   - countdowns
   - async processing
   - export coordination

3. **Analysis layer**
   - pose processing
   - smoothing
   - readiness
   - drill and movement evaluation

4. **Overlay and feedback layer**
   - skeleton rendering
   - ideal line rendering
   - cue generation
   - replay formatting

5. **Persistence layer**
   - session metadata
   - blob storage
   - replay source resolution

## Repo guide

### Best place to start

- `README.md`
- `ARCHITECTURE.md`

### Product and system docs

- [Architecture guide](ARCHITECTURE.md)
- [System overview](docs/architecture/system-overview.md)
- [App modules](docs/architecture/app-modules.md)
- [Session lifecycle](docs/architecture/session-lifecycle.md)
- [Video pipeline](docs/architecture/video-pipeline.md)
- [Replay and fallback](docs/architecture/replay-and-fallback.md)
- [Calibration and scoring](docs/architecture/calibration-and-scoring.md)
- [Overlay rendering](docs/architecture/overlay-rendering.md)

### Feature guides

- [Calibration](docs/features/calibration.md)
- [Live coaching](docs/features/live-coaching.md)
- [Session history](docs/features/session-history.md)
- [Video import](docs/features/video-import.md)

### Diagrams

- [UI flow](docs/diagrams/ui-flow.md)
- [Class diagram](docs/diagrams/class-diagram.md)
- [Live session sequence](docs/diagrams/sequence-live-session.md)
- [Import analysis sequence](docs/diagrams/sequence-import-analysis.md)
- [Export finalization sequence](docs/diagrams/sequence-export-finalization.md)

### Architecture decisions

- [ADR-001 annotated export strategy](docs/decisions/adr-001-annotated-export-strategy.md)
- [ADR-002 replay fallback strategy](docs/decisions/adr-002-replay-fallback-strategy.md)
- [ADR-003 session duration source of truth](docs/decisions/adr-003-session-duration-source-of-truth.md)

## Current capabilities

- live coaching sessions with countdown and drill/freestyle modes
- pose smoothing, correction, and issue detection
- overlay timeline capture for replay/export
- raw capture persistence and media verification
- annotated export pipeline with progress and failure state
- replay source resolution with fallback rules
- imported video analysis with stage-based UI
- session history and results review
- calibration/body profile support for analysis tuning

## Known limitations

- annotated export can take longer than raw replay readiness on lower-end devices
- playback correctness depends on metadata normalization and validation
- some fallback and export edge cases are still guarded by defensive diagnostics

## Running the project

### Prerequisites

- JDK 17
- Android SDK (`compileSdk 34`, `targetSdk 34`, `minSdk 28`)
- Gradle 8.14.x

### Quick start

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
java -version
gradle testDebugUnitTest
gradle :app:assembleDebug
```

## Testing

```bash
gradle testDebugUnitTest
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
