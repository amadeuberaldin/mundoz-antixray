# Mission: Observation Semantics

## Mission Objective

Define and implement the minimum AntiXray Observation semantics required
to decide whether protected information belongs to a player's visible
world representation.

This mission must complete the existing Observation concepts already
present in the repository instead of creating a parallel observation model.

The result must remain inactive and must not change the active v1 runtime.

## Core Observation Rule

Observation is based on whether the player has a valid visual path to
the target.

The first block or material that actually terminates the visual path is
itself observable.

Information hidden behind that occluder is not observed.

Example:

PLAYER -> AIR -> AIR -> STONE -> DIAMOND

STONE   = OBSERVED
DIAMOND = NOT_OBSERVED

Observation behavior is determined by block type and visual behavior,
not by whether the block was placed by a player or generated naturally.

## Observation Pass-Through

The following categories must not terminate AntiXray observation:

air;
water;
glass;
crafting tables;
brewing stands;
doors;
trapdoors;
fences;
fence gates;
iron bars;
chains;
lanterns;
buttons;
levers;
copper grates;
stairs.

These block types are intentionally treated conservatively as
observation pass-through.

The purpose is to avoid false hiding behind blocks with openings,
partial geometry, decorative use, or construction-oriented behavior.

A player-built wall is not automatically pass-through.

For example:

PLAYER -> SMOOTH_BASALT_WALL -> DIAMOND

The wall terminates observation.

But:

PLAYER -> GLASS -> DIAMOND

observation may continue through the glass.

Observation Occluders

Blocks that effectively form a visual wall terminate the observation path.

This includes ordinary full terrain and building blocks such as:

stone;
deepslate;
dirt;
granite;
andesite;
smooth basalt;
netherrack;
end stone;
ores;
other full visual occluders.

The exact list should not be duplicated unnecessarily if Minecraft
block properties or existing project concepts can safely express the
same responsibility.

Do not infer pass-through solely from whether Minecraft considers a block
non-solid.

The AntiXray observation policy is intentionally conservative.

## Lava

Lava is observable and terminates the observation path.

Example:

PLAYER -> AIR -> LAVA -> DIAMOND

LAVA    = OBSERVED
DIAMOND = NOT_OBSERVED

## Water and Glass

Water and glass do not terminate AntiXray observation.

Example:

PLAYER -> WATER -> WATER -> GLASS -> STONE -> DIAMOND

STONE   = OBSERVED
DIAMOND = NOT_OBSERVED

## Partial Geometry

For AntiXray purposes, the pass-through categories listed in this mission
must not be treated as full occluders.

Do not introduce exact voxel-shape ray tracing unless repository analysis
demonstrates that it is required to satisfy the approved semantics.

Prefer the smallest conservative solution that avoids false hiding.

## Fail-Visible Rule

When observation cannot be determined safely, prefer OBSERVED.

False-positive hiding is worse than safely revealing information.

Observation uncertainty must never produce an invalid or inconsistent
player representation.

## Mission Scope

Included:

audit existing Observation domain concepts;
reuse, complete, or correct existing ObservationContext,
ObservationDecision, ObservationPolicy, and ObservationEvaluationService
where appropriate;
define the minimum observation facts required by the approved semantics;
implement pure observation policy behavior;
add Minecraft infrastructure classification/adapters required to
distinguish pass-through from occluding block behavior;
add automated tests;
update architecture documentation and ADRs where required;
document unresolved runtime integration gaps.

Prefer completing existing concepts over creating new abstractions.

## Out of Scope

Do not:

modify active v1 visibility behavior;
modify AntiXrayObfuscator;
modify AntiXrayRevealer;
modify mixins;
modify packet serialization;
modify chunk palettes or encoding;
register runtime events;
connect Observation to the active runtime;
change protection targets;
change replacement priority;
change reveal radius;
track who placed a block;
create a player block-placement history system;
expand Observation into a general rendering or distant-horizon engine;
remove legacy code.

## Human Decision Points

Stop for human review if implementation requires:

adding new observation semantics not defined by this mission;
deciding behavior for a block category not reasonably covered by the
approved pass-through/occluder rules;
introducing exact voxel-shape ray tracing;
defining packet, palette, chunk, or runtime behavior;
changing active mixin injection points;
contradicting accepted ADRs;
changing gameplay behavior;
expanding the mission beyond AntiXray observation.

## Completion Criteria

The mission is complete when:

existing Observation concepts form one coherent model;
the current placeholder behavior that always returns OBSERVED is no
longer the implemented domain behavior;
pass-through and occluding behavior is represented explicitly;
the approved pass-through categories are covered by tests;
lava is covered as an observation occluder;
ordinary full-block walls are covered as occluders;
observation failure or uncertainty follows fail-visible behavior;
domain code remains free of Minecraft dependencies;
Minecraft-specific block classification remains infrastructure-only;
no active runtime file is changed;
focused tests pass;
git diff --check passes;
./gradlew test passes;
./gradlew clean build passes;
all commits follow one concept -> one commit -> one responsibility;
the branch is pushed only to origin/codex/observation-semantics.

## Expected Deliverables

implementation plan;
observation semantic model;
automated domain tests;
Minecraft classification/adapters where justified;
architecture or ADR updates;
maintainer-style self-review;
completion report;
remaining runtime integration gaps;
pushed autonomous branch.

##Target Branch

codex/observation-semantics
