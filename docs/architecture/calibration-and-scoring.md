# Calibration and Scoring

Calibration and scoring are cross-workflow foundations, not isolated settings features.

## Calibration as shared runtime context

- Calibration creates/refines body profile data for the active user profile.
- Runtime body-profile resolution should be consistent in:
  - Live Session
  - Upload / Reference Training
  - Drill-linked comparison flows
- Missing calibration should degrade gracefully to default model behavior, with explicit attribution where persisted.

## Scoring pipeline

- Pose + motion signals feed biomechanics and issue classifiers.
- Drill-specific metrics roll up into session-level feedback, summaries, and outcomes.
- Comparison/reference training can reuse extracted movement signals for drill-linked benchmarking.

## Cross-workflow implications

- Calibration updates can change interpretation of future analyses across multiple workflows.
- Contributors should treat calibration changes as architecture-level behavior changes, not just UI/settings changes.
