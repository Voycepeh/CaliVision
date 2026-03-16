# Movement Profile Architecture (Foundation)

## Old model vs new model

- **Old**: drill-specific logic and threshold branching lived in multiple live-coaching components.
- **New foundation**: `MovementProfile` becomes the generic movement definition contract, with legacy drills mapped through adapters.

## Core domain additions

- `MovementProfile`, `MovementType`, `CameraViewConstraint`
- `PhaseDefinition`, `AlignmentRule`, `HoldRule`, `RepRule`, `ReadinessRule`
- `CalibrationProfile` for versioned threshold overrides
- `MovementTemplateCandidate` (draft-only artifact generated from uploaded analysis)

## Core services additions

- `PoseFrameNormalizer`
- `LandmarkVisibilityEvaluator`
- `JointAngleEngine`
- `MotionPhaseDetector`
- `ReadinessEngine`
- `HoldDetector`
- `RepDetector`
- `AlignmentScorer`
- `MovementFeedbackEngine`

These are profile-driven and reusable for both live sessions and upload analysis.

## Upload analysis pipeline (offline)

`UploadedVideoAnalysisCoordinator -> UploadedVideoAnalyzer -> UploadedAnalysisRepository`

Output artifact is `UploadedMovementSession` with:
- source video URI
- timing and telemetry
- phase timeline
- overlay-ready timeline points
- derived metrics
- generated draft `MovementTemplateCandidate`

## Compatibility layer

- `ExistingDrillToProfileAdapter` maps existing drill catalog definitions to a `MovementProfile`.
- `LegacyDrillExecutionBridge` applies calibration profile versions to base defaults while preserving drill identity and behavior.

## Migration strategy

1. Use adapters to preserve all current drills.
2. Enable freestyle/generic paths to consume profile-backed readiness + analysis.
3. Add upload analysis storage and draft candidate generation.
4. Iterate calibration persistence from in-memory to Room-backed storage in a follow-up.
