# Drill Package Contract (Android)

## Purpose

This document defines the versioned portable drill package contract used to prepare Android for future web-based Studio interoperability.

Android remains a **runtime/import consumer** and can export/import package JSON for compatibility validation.

## Contract namespace

Portable contract types live under:

- `app/src/main/java/com/inversioncoach/app/drillpackage/model`
- `app/src/main/java/com/inversioncoach/app/drillpackage/mapping`
- `app/src/main/java/com/inversioncoach/app/drillpackage/validation`
- `app/src/main/java/com/inversioncoach/app/drillpackage/io`

## Core models

- `SchemaVersion` (major/minor)
- `DrillManifest`
- `DrillPackage`
- `PortableDrill`
- `PortablePhase`
- `PortablePose`
- `PortableAssetRef`

## Pose contract guarantees

`PortablePose` enforces portability-oriented rules:

- canonical joint names (snake_case)
- normalized 2D coordinates (`x`,`y` in `[0,1]`)
- explicit `viewType`
- optional `visibility` / `confidence`
- order-independent representation via `Map<String, PortableJoint2D>`

## Validation

`DrillPackageValidator` validates:

- required manifest/drill fields
- schema version presence (`major > 0`)
- unique phase ordering per drill
- normalized coordinates and confidence/visibility ranges
- basic asset ref validity (`id`, `type`, `uri`)

## Mapping boundaries

- Catalog authoring model <-> portable contract:
  - `DrillCatalogPortableMapper`
- Existing runtime drill record <-> portable contract:
  - `DrillRecordPortableMapper`
- Runtime-only shape from persisted records:
  - `RuntimeDrillMapper`

## IO stubs

- `DrillPackageJsonCodec` provides JSON encode/decode.
- `DrillPackageFileIO` provides local file import/export helpers.
- Remote sync is intentionally out-of-scope for this phase.
