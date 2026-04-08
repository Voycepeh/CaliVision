# Feature: Session History

Session History is the drill-aware detailed history surface (`session-history?...`) and persisted record of drill-linked outcomes from live and upload workflows.

## Scope boundary vs History Overview

- **History Overview (`history`)** is the top-level at-a-glance landing page from Home.
- **Session History (`session-history?...`)** is the deeper detailed/compare page (`HistoryScreen`).
- Drill-filtered history and compare mode belong to Session History, not History Overview.

## What history stores

- Session metadata, scores, and issue summaries.
- Media state for raw/annotated assets.
- Resolver-selected replay source.
- Profile/profile attribution metadata for traceability.

## User-facing behavior

- Show prior **reviewable** sessions in drill-relevant context by default.
- Open replay using resolved best media source.
- Uploaded sessions become Results-navigable only after terminal completion (`ANNOTATED_READY` or raw-only terminal fallback).
- Preserve truthful status when annotated export failed but raw replay exists.
- Hide failed/incomplete upload attempts from the primary list when they have no playable media and no meaningful analysis output.
- Keep optional debug visibility through a simple "Show failed attempts" toggle.
- If no reviewable sessions exist, show a clear user-facing empty state instead of a noisy failed-attempt list.

## Cross-workflow role

History is not a passive archive; it is part of the training loop:

- review prior attempts,
- build references from strong sessions,
- compare current performance to previous outcomes.
