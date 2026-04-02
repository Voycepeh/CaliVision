# Feature: Calibration / Profiles

Calibration and profile management provide person-level context used by coaching analysis.

## What this feature does

- Create and maintain user profiles.
- Store and version body profile/calibration data.
- Resolve one active profile at runtime.
- Provide analysis context to live sessions, upload analysis, and comparisons.

## Runtime expectations

- Active profile resolution should be centralized and consistent.
- If no calibration exists, analysis falls back to default model behavior.
- Persisted session data should retain profile attribution where available.

## Why this matters

Calibration is a cross-workflow dependency. Changes here affect scoring interpretation and feedback quality beyond the settings surface.
