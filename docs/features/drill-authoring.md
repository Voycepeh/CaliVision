# Feature: Drill Authoring (Drill Studio)

Drill Studio is the authoring workspace for creating and refining drill definitions used across live and upload workflows.

## What Drill Studio is for

- Create new drill definitions.
- Edit existing drills with predictable persistence.
- Maintain drill metadata needed by coaching, scoring, and reference workflows.

## Persisted editing behavior

When users reopen an existing drill, the editor should:

- Reload persisted drill data reliably.
- Avoid silently resetting prior selections.
- Surface validation errors clearly.
- Save through one obvious path that writes current truth.

## Simplification direction

Drill authoring should prioritize clarity over branching:

- Avoid multiple save variants that imply ambiguous persistence outcomes.
- Avoid duplicate actions with equivalent results.
- Keep destructive actions explicit and confirmation-backed.
- Return users to Manage Drills with clear post-save state.

For repo-level rationale, see [ADR-004](../decisions/adr-004-product-workflow-simplification.md).

## Phase 1 image-seeded phase authoring

Drill Studio now supports **single-image phase seeding** directly inside phase editing:

- Attach one reference image per phase.
- Run on-device pose detection on that image.
- View skeleton overlay directly on top of the image canvas.
- Apply constrained per-joint correction offsets (drag and reset flows).
- Save canonical normalized phase joints plus authoring metadata into drill persistence.

### Boundary and environment guides

The phase authoring canvas includes lightweight optional visual guides:

- frame/corner guides
- floor line
- wall line
- bar/reference line

These are stored as authoring settings and are intended to be extendable in future phases.

### Persistence and compatibility

- Authored phase image metadata is persisted with the drill in DB-backed drill records (via Drill Studio payload in drill cue config).
- Existing seeded drill loading is preserved.
- New authored data coexists with seeded catalog drills.

### Drill package export/import

Manage Drills now supports package sharing for authored drills:

- Export includes drill metadata + cue payload (including phase poses, manual offsets, guide settings, and image-source authoring references).
- Import rehydrates the drill record so the drill is immediately editable and previewable.
