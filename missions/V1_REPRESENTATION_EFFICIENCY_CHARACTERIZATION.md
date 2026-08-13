# Mission — V1 Representation Efficiency Characterization

## Status

Planned

## Context

MundoZ AntiXray v1 has shown an important production side effect in addition
to its anti-xray behavior.

During normal server use, especially while players travel quickly and load many
chunks, the v1 representation appears to reduce client-side lag compared with
receiving the normal underground representation.

This behavior is valuable and should not be lost during the v2 migration.

The exact cause of this improvement has not yet been demonstrated.

Possible contributing factors include:

- fewer distinct block states in transmitted chunk sections;
- simpler section palettes;
- replacement of many protected underground resources with common terrain;
- smaller serialized section representations;
- better network compression;
- reduced client-side work when processing received chunks;
- reduced information exposed to the client;
- or a combination of these effects.

These possibilities are hypotheses only.

The repository must not encode an explanation as architectural fact until the
behavior is measured or demonstrated.

The current v2 architecture improves observation semantics and is expected to
hide information more accurately than v1.

However, semantic correctness and representation efficiency are separate
concerns.

The purpose of this mission is to understand what v1 actually does to the
player-specific representation and determine which measurable properties, if
any, should become requirements for future v2 representation construction.

---

# Objective

Characterize the representation produced by the active v1 AntiXray path and
determine which measurable changes could explain the observed reduction in
client-side lag and/or transmitted information.

Establish a quantitative baseline that can later be used to compare v1 and v2.

This mission is investigative.

It must not optimize v2.

It must not change player-visible behavior.

It must not make v2 authoritative.

---

# Primary Questions

The investigation must answer, as far as the existing runtime boundaries allow:

1. How does v1 change a chunk section before serialization?

2. How many protected states are replaced in representative sections?

3. Does v1 reduce the number of distinct block states in those sections?

4. Does v1 reduce palette cardinality or otherwise simplify palette contents?

5. Does the v1-transformed section serialize to fewer bytes than the original
   section?

6. If serialized size changes, under which terrain and ore distributions does
   that happen?

7. Does the transformed representation compress better when subjected to an
   appropriate controlled compression comparison?

8. Is the observed benefit primarily explainable by:
   - representation size;
   - palette simplification;
   - compressibility;
   - reduced information diversity;
   - or another measurable property?

9. Which of those properties should eventually be preserved when v2 becomes
   responsible for player representation?

10. Which observations cannot be established from server-side measurements
    alone and therefore require separate client/runtime profiling?

---

# Important Distinction

Do not assume that fewer visible ores automatically means fewer network bytes.

Do not assume that a smaller palette automatically means less client work.

Do not assume that improved client FPS proves reduced network traffic.

Do not assume that network compression is the source of the production
improvement.

The mission must distinguish between:

- authoritative world data;
- temporary AntiXray representation;
- uncompressed section serialization;
- compressed network representation where measurable;
- server CPU cost;
- and client-side processing.

Claims must be limited to what the measurements actually demonstrate.

---

# Existing V1 Runtime Boundary

The active v1 implementation intercepts section serialization.

The authoritative `LevelChunkSection` is never modified.

For participating sections, v1:

1. identifies protected blocks;
2. selects a safe replacement;
3. creates a temporary section copy;
4. replaces hidden protected states in that copy;
5. recalculates the copied section state/count information as required;
6. serializes either the transformed copy or the original section.

This mission must characterize that boundary without changing its behavior.

---

# Required Measurements

For every controlled section fixture where technically possible, capture both
the original representation and the representation produced by v1.

At minimum measure:

## Section Contents

- total block cells;
- number of protected candidates;
- number of protected blocks actually replaced;
- number of unchanged protected blocks;
- number of distinct block states before transformation;
- number of distinct block states after transformation;
- selected replacement state;
- relevant palette characteristics available through the supported Minecraft
  API.

## Serialization

Measure:

- original serialized section byte count;
- transformed serialized section byte count;
- absolute byte difference;
- percentage difference.

The measurement must use the same Minecraft serialization mechanism for both
representations.

Do not approximate serialized size from block counts.

## Controlled Compression

If a reliable isolated representation can be obtained without changing the
network protocol, compare compression of the original and transformed byte
sequences using the same compression algorithm and settings.

Record:

- original uncompressed bytes;
- transformed uncompressed bytes;
- original compressed bytes;
- transformed compressed bytes;
- compression ratio for each representation.

This is a characterization experiment.

It must not be presented as actual Minecraft packet bandwidth unless the
measurement occurs at the real packet/network compression boundary.

---

# Representative Fixtures

Tests should cover representative cases rather than only synthetic maximum
differences.

At minimum investigate:

## Homogeneous Terrain

A section dominated by one terrain state with no protected resources.

Expected purpose:

Establish control overhead and verify that AntiXray does not claim a benefit
when it makes no representation change.

## Sparse Protected Resources

A mostly homogeneous terrain section containing a small number of protected
ores.

Expected purpose:

Represent common underground terrain.

## Multiple Ore Types

A section containing several protected ore identities distributed through
terrain.

Expected purpose:

Observe whether replacing multiple resource identities with terrain reduces
state diversity or palette complexity.

## Dense Synthetic Protected Resources

A deliberately unrealistic section containing many protected resources.

Expected purpose:

Establish an upper-bound characterization.

This fixture must be identified as synthetic and must not be used alone to
claim production benefit.

## Mixed Terrain

A section containing multiple accepted terrain/replacement states and protected
resources.

Expected purpose:

Characterize sections whose palette is already non-trivial.

## Nether

Representative netherrack terrain containing protected Nether resources and,
where applicable, ancient debris.

## End Control

Representative End terrain with no naturally occurring protected ore set.

Expected purpose:

Provide a control demonstrating behavior when the AntiXray resource
transformation has little or nothing to replace.

---

# Palette Investigation

Minecraft palette behavior is version-specific infrastructure.

The investigation may inspect palette characteristics needed to explain the
serialized representation, but must not make the domain depend on Minecraft
palette implementation.

Determine, using Minecraft 26.2 behavior:

- whether replacement reduces distinct states;
- whether that reduction changes palette representation;
- whether any palette representation threshold is crossed;
- whether serialized size changes discontinuously at such thresholds.

If internal palette details are required, keep the investigation isolated to
Minecraft infrastructure/tests and document the version dependency.

Do not modify palette encoding.

---

# Runtime Cost

Representation reduction is useful only if its cost is understood.

Where measurements can be made without introducing permanent production
instrumentation, characterize the cost of:

- scanning a section;
- finding replacement candidates;
- copying a section;
- replacing states;
- recalculating section data;
- serializing the original representation;
- serializing the transformed representation.

Timing measurements must be treated as diagnostic indicators, not formal
benchmarks, unless a dedicated benchmark methodology is introduced and
reviewed.

JVM warmup, JIT, GC, machine load, and Minecraft runtime state must be
acknowledged when interpreting timing data.

---

# Client-Side Boundary

Server-side characterization cannot by itself prove why players experienced
higher FPS or smoother chunk loading.

If the server measurements demonstrate a meaningful representation difference,
document what remains unknown about the client.

Possible later client-side investigation may examine:

- chunk decode cost;
- palette/container construction;
- chunk rebuild work;
- rendering-related processing;
- memory allocation;
- frame-time behavior during rapid chunk loading.

That client investigation is not automatically part of this mission.

Do not infer client internals from server-side byte measurements.

---

# Production Observation

The reported production behavior is valuable evidence motivating the
investigation:

v1 AntiXray appeared to reduce lag while players rapidly loaded terrain,
including travel where many chunks entered the client representation.

Treat this as an observed symptom, not a proven mechanism.

The mission should attempt to produce measurable server-side properties that
could later be correlated with that observation.

---

# V1 and V2 Relationship

V1 and v2 have different visibility semantics.

V1 primarily uses local exposure/adjacency behavior.

V2 models observation paths and player-specific observability.

Therefore a direct count of hidden blocks is not sufficient to compare their
efficiency.

A future comparison must distinguish:

- correctness of the observation decision;
- number of protected blocks hidden;
- representation complexity;
- serialization size;
- compression behavior;
- evaluation cost.

The desired future property is not:

"v2 must hide exactly the same blocks as v1."

The desired property is:

"v2 should preserve beneficial representation-efficiency characteristics when
doing so is compatible with correct observation semantics."

---

# Architectural Constraints

The investigation must preserve the existing architecture rules.

## World Integrity

Never modify the authoritative world to perform characterization.

## Player Representation

AntiXray changes only temporary player-specific representation.

## Domain Independence

Domain and application code must not depend on:

- `LevelChunkSection`;
- palettes;
- packet classes;
- `FriendlyByteBuf`;
- Fabric events;
- Minecraft compression internals.

Minecraft-specific measurements belong to infrastructure or test code.

## Runtime Authority

V1 remains authoritative.

V2 remains observational/shadow-only.

No characterization result may change which representation is sent to the
player.

## Failure Behavior

Instrumentation or measurement failure must not affect:

- v1 obfuscation;
- chunk serialization;
- packet output;
- world state;
- server stability.

---

# Instrumentation Constraints

Prefer deterministic tests and isolated characterization fixtures over
permanent production instrumentation.

Any temporary diagnostic instrumentation must:

- be explicitly opt-in;
- be disabled by default;
- retain no player identity;
- retain no coordinates unless a narrowly targeted development diagnostic
  explicitly requires them;
- avoid persistent telemetry;
- avoid background threads;
- avoid additional chunk loading;
- avoid world mutation;
- have bounded runtime cost;
- be removable after the investigation.

Temporary diagnostics must not silently become production architecture.

---

# Required Investigation Sequence

## Phase 1 — Read Existing Runtime

Inspect the current v1 implementation and document exactly:

- where the original section enters the AntiXray boundary;
- where the copy is created;
- how protected states are selected;
- how replacements are chosen;
- where counts/state information are recalculated;
- where serialization occurs.

Do not modify code during this phase.

## Phase 2 — Define Measurement Model

Before implementing instrumentation, define the exact measurements and what
each one can and cannot prove.

Prefer a small immutable measurement/result model suitable for tests if code
is required.

Do not add production telemetry merely for convenience.

## Phase 3 — Build Controlled Fixtures

Create representative Minecraft 26.2 section fixtures covering the scenarios
listed in this mission.

Verify the authoritative fixture remains unchanged after v1 transformation.

## Phase 4 — Compare Representations

For each fixture compare:

- block-state diversity;
- replacement count;
- palette characteristics;
- serialized bytes.

## Phase 5 — Compression Experiment

If technically sound and isolated, compare compression of equivalent original
and transformed serialized data.

Clearly distinguish this experiment from actual network bandwidth.

## Phase 6 — Interpret Results

Document which hypotheses are supported, rejected, or remain unresolved.

Do not generalize beyond the evidence.

## Phase 7 — Architecture Recommendation

Only after measurements exist, recommend which representation-efficiency
properties should become explicit requirements for future v2 work.

Do not implement those recommendations in this mission.

---

# Expected Deliverables

The mission should produce:

1. characterization tests or isolated measurement fixtures;
2. documented measurements for representative section types;
3. explanation of how v1 changes representation complexity;
4. explanation of whether serialized size changes;
5. controlled compression results if technically valid;
6. explicit separation between measured facts and hypotheses;
7. documented limitations of server-side measurements;
8. recommendation for future v2 efficiency requirements;
9. no change to active player-visible behavior.

A dedicated architecture or investigation document should preserve the final
findings.

Suggested document:

`docs/architecture/ANTI_XRAY_V1_REPRESENTATION_EFFICIENCY.md`

---

# Explicitly Out of Scope

Do not:

- make v2 authoritative;
- replace v1;
- redesign dynamic observation reevaluation;
- change observation semantics;
- change copper-grate classification;
- change protected block policy;
- change replacement priority;
- change packet format;
- change palette encoding;
- change Minecraft compression configuration;
- add caching;
- add asynchronous processing;
- add persistent telemetry;
- optimize code based only on assumptions;
- remove legacy components;
- modify the authoritative world;
- perform unrelated refactoring.

---

# Success Criteria

The mission is complete when:

- the v1 transformation boundary is characterized;
- representative original and transformed sections can be compared
  deterministically;
- state/palette differences are measured;
- serialized byte differences are measured;
- compression differences are measured where technically valid;
- unchanged control cases are included;
- Nether, Overworld, and End/control behavior are represented;
- authoritative sections remain unchanged;
- active runtime behavior remains unchanged;
- measurements distinguish facts from hypotheses;
- client-side conclusions are not made from server-only evidence;
- the repository documents which v1 efficiency properties are worth preserving
  in future v2 work;
- all focused tests pass;
- the complete test suite passes;
- `./gradlew clean build` passes;
- `git diff --check` passes;
- maintainer review finds no unintended runtime or architectural change.

---

# Final Question

At completion, the investigation must be able to answer:

**What measurable property of the representation produced by MundoZ AntiXray
v1 could account for its observed performance benefit, and which of those
properties should the future v2 representation preserve without sacrificing
the v2 observation model?**

If the available evidence cannot answer part of that question, document the
remaining uncertainty rather than guessing.
