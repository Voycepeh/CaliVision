# Overlay Rendering

Overlay rendering is split between realtime UI overlays and offline annotated export rendering.

## Realtime Overlay

- Pose frames are smoothed/validated.
- UI renders skeleton/guide overlays in-session for coaching feedback.
- Freestyle orientation helpers influence how overlays are interpreted.

## Export Overlay

- Overlay timeline frames are recorded with timestamps.
- Export pipeline resolves timeline against chosen render preset.
- Annotated compositor writes a media file with overlay baked into frames.

## Consistency Goals

- Preserve visual intent between live overlay and exported replay.
- Avoid timeline drift during stop/finalization boundaries.
- Fail safely when timeline/media validation detects mismatches.
