# Mission: Observation Path Collection

## Mission Objective

Investigate and implement the minimum safe infrastructure required to
collect observation-path facts from the Minecraft world for the existing
AntiXray Observation model.

This mission begins at the boundary between Minecraft world state and the
already accepted Observation semantics.

The result must remain inactive and must not change the active v1 runtime.

## Existing Observation Contract

The existing Observation model already defines how an ordered path is
interpreted.

Conceptually:

Minecraft world
    ->
observation path collection
    ->
Minecraft observation-path classification
    ->
ObservationContext
    ->
ObservationPolicy
    ->
OBSERVED / NOT_OBSERVED

This mission owns only the observation-path collection boundary and the
minimum infrastructure necessary to feed the existing model.

Do not redefine the accepted Observation semantics.

## Core Invariant

The real Minecraft world must never be modified by AntiXray.

Observation-path collection is read-only.

It may inspect Minecraft world state but must never mutate:

- blocks;
- chunks;
- entities;
- player state;
- palettes;
- packets;
- authoritative world data.

## Observation Origin

The conceptual observation origin is the player's visual viewpoint.

The preferred starting point is therefore the player's eye/camera
position rather than feet or block position.

However, before implementing this assumption, inspect:

- the Minecraft 26.2 APIs and Mojang mappings available to the project;
- the stable v1 implementation;
- existing repository architecture;
- accepted ADRs.

If Minecraft exposes multiple materially different notions of eye,
camera, or view origin that could change AntiXray semantics, stop for
human review and present the alternatives.

Do not silently choose between materially different origins.

## Observation Target

The target is a block position whose observability is being evaluated.

This mission does NOT pre-authorize using only the center of the target
block as the definitive visibility rule.

A target block may be partially visible even when a line to its center
is obstructed.

Before implementing target sampling, investigate the available Minecraft
26.2 primitives and existing v1 behavior.

Potential approaches may include, but are not limited to:

- target block center;
- face-oriented sampling;
- multiple representative points;
- existing Minecraft clipping/raycast primitives;
- conservative block traversal without exact shape tracing.

Do not select a target-sampling semantic merely because it is easiest to
implement.

If choosing between alternatives would change what a player is considered
able to observe, stop for human review.

## Path Semantics

The collected path must preserve traversal order from observation origin
toward the target.

The target itself must not incorrectly act as an occluder against itself.

Example:

PLAYER -> AIR -> AIR -> STONE

When STONE is the target:

STONE = OBSERVED

For:

PLAYER -> AIR -> AIR -> STONE -> DIAMOND

when DIAMOND is the target:

STONE = path occluder
DIAMOND = NOT_OBSERVED

The collector must therefore distinguish the path leading to the target
from the target being evaluated.

## World Sampling

World sampling must be read-only.

The collector should produce only the facts required by the existing
Observation model.

Do not move Minecraft concepts into the domain merely to simplify
sampling.

Minecraft-specific concepts such as:

- ServerLevel;
- ServerPlayer;
- BlockPos;
- BlockState;
- Vec3;
- ClipContext;
- voxel shapes;
- chunk access;

must remain outside the pure domain.

## Classification Boundary

Reuse the existing Minecraft observation-path classifier.

Do not duplicate pass-through/occluder classification inside the path
collector.

Conceptually:

Minecraft path positions
    ->
BlockState lookup
    ->
MinecraftObservationPathClassifier
    ->
ObservationPathBehavior
    ->
ObservationContext

The collector determines which world positions belong to the path.

The classifier determines the observation behavior of the block state at
those positions.

These are separate responsibilities.

## Unknown and Unavailable Information

Observation remains fail-visible.

If required world information cannot be obtained safely, the result must
not cause protected information to be hidden incorrectly.

Do not silently convert:

- unavailable chunks;
- invalid positions;
- unsupported states;
- incomplete traversal;
- uncertain sampling;

into OCCLUDING.

Determine whether existing Observation UNKNOWN semantics can represent
these cases safely.

If a new semantic distinction would be required, stop for human review
rather than expanding the domain silently.

## Minecraft Investigation

Before implementation, inspect Minecraft 26.2 and the current project to
determine:

1. Which API represents the player's visual/eye origin.

2. Which Minecraft primitives are available for traversing or clipping a
   line through block space.

3. Whether those primitives depend on collision shapes, visual shapes,
   interaction shapes, or other geometry with semantics different from
   AntiXray observation.

4. How unloaded or unavailable world data behaves.

5. Whether traversal can produce the ordered block positions before the
   target without modifying world state.

6. What the stable v1 implementation currently does for proximity and
   visibility.

7. Whether an existing repository concept already owns any part of this
   responsibility.

Document these findings before making a semantic choice that is not
already authorized.

## Abstraction Discipline

Prefer completing existing concepts.

Before introducing any new:

- collector;
- sampler;
- path;
- ray;
- context;
- source;
- adapter;
- service;

search the current source tree for an existing responsibility that can be
completed or reused.

Do not build a generic ray-tracing framework.

Do not build a general-purpose Observation Engine.

Any new abstraction must directly support the AntiXray observation use
case implemented by this mission.

## Mission Scope

Included:

- audit existing Observation and Minecraft adapter code;
- inspect stable v1 behavior relevant to observation;
- investigate Minecraft 26.2 eye/view and path traversal APIs;
- identify the smallest safe collection boundary;
- implement read-only path collection where semantics are already
  authorized;
- reuse MinecraftObservationPathClassifier;
- produce ordered observation facts;
- represent unavailable information conservatively using existing
  fail-visible semantics where possible;
- add focused automated tests;
- add integration-style tests that do not activate the production runtime;
- update architecture/maintenance documentation where appropriate;
- document unresolved semantic decisions and runtime integration gaps.

## Out of Scope

Do not:

- modify active v1 runtime behavior;
- modify AntiXrayObfuscator;
- modify AntiXrayRevealer;
- modify mixins;
- modify packet serialization;
- modify chunk packet palettes;
- connect the new collector to packet generation;
- replace the active runtime path;
- remove legacy code;
- change protection targets;
- change replacement policy;
- change reveal radius;
- change accepted pass-through/occluder categories;
- track player block placement;
- introduce caching or performance optimization unless required for test
  correctness;
- implement a general-purpose rendering system;
- implement distant-horizon observation;
- introduce exact voxel-shape ray tracing without human approval.

## Required Scenarios

Tests and/or investigation must explicitly cover the semantics necessary
to distinguish at least:

PLAYER -> AIR -> AIR -> TARGET

PLAYER -> AIR -> STONE -> TARGET

PLAYER -> WATER -> WATER -> TARGET

PLAYER -> GLASS -> GLASS -> TARGET

PLAYER -> PASS_THROUGH_OBJECT -> TARGET

PLAYER -> LAVA -> TARGET

The target must not be included as an occluder against itself.

Where target sampling has not yet been human-approved, tests must not
silently encode a disputed center-only visibility rule.

## Human Decision Points

Stop for human review before proceeding if implementation requires a
decision about:

- eye position versus another materially different observation origin;
- center-only target sampling;
- face sampling;
- multi-point target sampling;
- voxel-shape ray tracing;
- collision-shape versus visual-shape semantics;
- treatment of unavailable chunks beyond existing UNKNOWN/fail-visible
  semantics;
- path traversal behavior that changes what is considered observable;
- active runtime integration;
- packet or palette behavior;
- gameplay semantics;
- contradiction with accepted ADRs.

When stopping, provide:

- the concrete Minecraft APIs involved;
- current v1 behavior;
- alternatives;
- advantages and disadvantages;
- likely false-positive and false-negative consequences;
- recommended option.

Do not implement the disputed semantic until human review.

## Mission Planning

Before editing:

1. Read AGENTS.md.
2. Read this mission.
3. Complete the repository reading required by AGENTS.md.
4. Inspect existing Observation implementation.
5. Inspect relevant v1 implementation.
6. Inspect Minecraft 26.2 APIs available through the project's mapped
   dependencies.
7. Identify existing concepts that can be reused.
8. Produce an implementation plan.
9. Estimate the commits.
10. Identify likely human decision points.

The plan must clearly separate:

- investigation;
- already-authorized implementation;
- semantic decisions requiring human approval.

Wait for plan approval before editing.

## Commit Discipline

Follow:

One concept -> one commit -> one responsibility.

Likely slices may include:

- Minecraft observation-path collection infrastructure;
- application coordination, only if actually required;
- tests/characterization;
- architecture or maintenance documentation.

The exact commit structure must come from repository inspection rather
than being forced by this mission.

## Validation

Before every commit:

1. run `git diff --check`;
2. run relevant focused tests;
3. run `./gradlew test`;
4. run `./gradlew clean build`;
5. verify changed-file scope.

Before mission completion, perform a maintainer-style review of the entire
branch.

## Completion Criteria

The mission is complete, or correctly stopped at a human decision point,
when:

- Minecraft 26.2 observation/path APIs have been investigated;
- relevant v1 behavior has been characterized;
- existing Observation concepts have been reused where possible;
- collection and classification responsibilities remain separate;
- world access remains read-only;
- target self-occlusion is prevented;
- uncertainty remains fail-visible;
- domain purity is preserved;
- no active runtime behavior changed;
- tests cover all semantics implemented by the mission;
- all validations pass;
- commits follow repository discipline;
- the branch is pushed only to its dedicated codex branch.

If a required geometric semantic remains unresolved, a documented human
decision point is a valid mission outcome.

Do not invent the missing semantic merely to declare the mission complete.

## Expected Deliverables

- implementation plan;
- Minecraft 26.2 API investigation findings;
- v1 behavior characterization;
- implementation of safely authorized path collection infrastructure;
- focused tests;
- documentation;
- maintainer-style branch review;
- commits created;
- exact files changed;
- validations executed;
- unresolved semantic decisions;
- runtime integration gaps;
- recommended next step.

## Target Branch

`codex/observation-path-collection`
