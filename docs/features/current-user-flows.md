# Feature: Current User Flows

This page describes the intended end-to-end user journeys for the current product direction.

## 1) Home / Drill Hub

Home routes users into Drill Hub as the main practice anchor. From Drill Hub users should be able to:

- Start a live session for a drill.
- Open Manage Drills.
- Open Upload / Reference Training.
- Reach Results / Session History and Calibration / Profiles without losing workflow context.

## 2) Manage Drills

Manage Drills is the operational list for drill maintenance.

Typical flow:

1. Open drill list.
2. Create new drill or select existing drill.
3. Enter Drill Studio for edit.
4. Save and return to managed list state.

## 3) Drill Studio

Drill Studio is where drill definitions are authored and edited.

Expected behavior:

- Existing drills reload persisted values reliably.
- Validation is explicit and user-facing.
- Save path is clear and deterministic.
- Redundant save variants are avoided.

## 4) Start Live Session

Typical flow:

1. Select drill from Drill Hub.
2. Resolve effective session configuration.
3. Complete countdown/start gating.
4. Run live coaching loop with overlays/cues.
5. Stop and finalize session.
6. Review outcome in Results and Session History.

## 5) Upload / Reference Training

Typical flow:

1. Select video clip.
2. Run analysis/scoring.
3. Review results and replay.
4. Optionally create/update a drill-linked reference template.
5. Optionally compare against existing references.

## 6) Results / History

Results show immediate post-session outcomes; History provides ongoing access to prior sessions.

Both surfaces should present:

- Drill-linked context.
- Replay source chosen by resolver policy.
- Persisted scores/issues/metadata.

## 7) Calibration / Profiles

Calibration and profiles manage person-level context used in analysis.

Expected behavior:

- One active profile at runtime.
- Profile changes affect future live/upload analysis.
- Lack of calibration falls back gracefully to default behavior.
