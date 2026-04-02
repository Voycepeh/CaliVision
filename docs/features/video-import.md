# Feature: Video Import / Upload Analysis

Video Import supports offline review and reference-training preparation from uploaded clips.

## User-facing flow

1. Open Upload / Reference Training.
2. Select source video clip.
3. Run pose/motion/score analysis.
4. Render or prepare replay/export artifacts.
5. Review outcome and optionally save as drill reference.

## Key behavior

- Upload analysis is parallel in intent to live coaching but uses imported timing/media sources.
- Output persists to session/replay/history surfaces.
- Reference-template creation is optional, drill-linked, and comparison-oriented.

## Integration points

- Shares calibration/profile runtime context.
- Shares replay resolution policy with live sessions.
- Feeds Results / Session History and comparison workflows.
