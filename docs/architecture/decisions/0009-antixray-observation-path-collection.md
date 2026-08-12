# ADR 0009 - AntiXray Observation Path Collection

## Status

Accepted

## Context

ADR 0008 defines how one ordered path is interpreted but deliberately leaves
Minecraft traversal, observation origin, and target sampling unresolved.
Minecraft clipping APIs use collision, outline, visual, or fluid shapes whose
semantics do not match AntiXray's approved category-level classification.

Center-only target sampling can falsely hide a block whose center is blocked
while an observer-facing portion remains visible.

## Decision

The observation origin is the current eye position of the player's active
camera entity: `player.getCamera().getEyePosition()`. No partial-tick
interpolation is used.

Minecraft `BlockGetter.traverseBlocks` determines the ordered block cells
crossed by each sample path. It provides grid traversal only. It does not
decide occlusion and no `ClipContext` or voxel-shape mode is used.

`MinecraftObservationPathClassifier` remains exclusively responsible for
translating readable `BlockState` values into path behavior.

Every target uses its center and the inset centers of observer-facing faces.
A face is observer-facing only when the origin coordinate is strictly outside
the target block's closed bounds on that face's axis. Samples are ordered:
center, X face, Y face, Z face. Face centers are inset by `0.0001` blocks so
each endpoint remains inside the target cell.

The origin-containing cell is included unless it is also the target. The
target cell is excluded from every path so it cannot occlude itself.

Each sample is evaluated by the existing single-path observation policy. The
multi-sample result is `OBSERVED` when any sample is `OBSERVED`, and
`NOT_OBSERVED` only when every sample is `NOT_OBSERVED`.

World reads use only already-loaded chunks. A position outside build height or
without an already-loaded chunk appends `UNKNOWN` and terminates that sample.
Existing fail-visible policy then makes the sample observed.

## Consequences

Positive:

- traversal mechanics remain separate from AntiXray semantics;
- partially visible observer-facing target areas reduce false hiding;
- missing world data cannot trigger hiding or chunk generation;
- the target cannot occlude itself;
- the authoritative world is read but never modified.

Negative:

- up to four paths are collected per target;
- grid traversal has Minecraft's deterministic cell-boundary tie behavior;
- the inactive collector still requires runtime integration and performance
  review before production use.
