# ADR 0008 - AntiXray Observation Path Semantics

## Status

Accepted

## Context

ADR 0003 requires visibility to be based on observation rather than physical
contact, but it does not define an executable path policy. The active v1
adjacency rule therefore cannot supply v2 observation decisions.

AntiXray must avoid false hiding behind partial, open, decorative, or
construction-oriented blocks. It must also hide information behind materials
that form a visual wall, including lava.

## Decision

AntiXray observation evaluates the ordered path before a target. The target
itself is excluded from that path.

The first occluding block is observed. Targets behind that occluder are not
observed.

Air, water, glass, crafting tables, brewing stands, doors, trapdoors, fences,
fence gates, iron bars, copper bars, chains, lanterns, buttons, levers, copper
grates, and stairs are conservative pass-through categories.

Lava, ordinary terrain, full building walls, ores, and other blocks not in an
approved pass-through category terminate observation. Pass-through must not be
inferred solely from a generic Minecraft non-solid or non-occluding property.

When any required path fact is unavailable or unknown, observation returns
`OBSERVED`. Uncertainty therefore fails visible.

Minecraft-specific classification belongs to infrastructure. The policy and
its path facts remain pure domain concepts.

This decision does not define observer eye position, target sampling, path
collection, exact voxel-shape ray tracing, packet behavior, or runtime wiring.

## Consequences

Positive:

- first-occluder behavior is explicit and testable;
- approved partial and transparent categories avoid false hiding;
- uncertainty cannot silently hide valid information;
- domain policy remains independent from Minecraft.

Negative:

- a future infrastructure slice must construct ordered path facts;
- conservative pass-through may reveal more information than exact geometry;
- Minecraft category mappings and waxed/weathering implementation differences
  require version-specific tests.
