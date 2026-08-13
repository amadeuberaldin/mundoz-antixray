# Mission — Client Representation Cost Characterization

## Status

Planned

## Context

The MundoZ AntiXray v2 migration is preserving and characterizing useful
runtime properties of the existing v1 implementation before v2 becomes
authoritative.

Two previous characterization missions established important facts about the
v1 representation.

### V1 representation efficiency characterization

The v1 obfuscator replaces protected hidden information inside a temporary
chunk-section representation without modifying authoritative world state.

This replacement can reduce block-state diversity in the representation sent
to the client.

The effect is structural: protected states are replaced by terrain states that
may already be common in the section.

### V1 protocol compression characterization

Protocol-level characterization established that this reduced representation
diversity can improve Minecraft's compressed network representation.

However, the measured compressed-byte savings were small in the tested
fixtures and are not sufficient evidence to explain the larger reduction in
client-side lag observed historically on MundoZ while rapidly loading terrain.

### Historical production observation

During use of AntiXray v1 on MundoZ, players observed substantially smoother
client behavior while rapidly loading chunks, especially during high-speed
Elytra travel.

This was an empirical production observation.

Its mechanism has not been established.

It must not be documented as proven network optimization, rendering
optimization, or client-performance optimization without additional evidence.

A plausible remaining hypothesis is that the simplified representation sent by
v1 reduces some form of Minecraft client-side processing cost after network
delivery.

Possible boundaries include:

- chunk packet decoding;
- palette/container processing;
- client chunk state installation;
- render-section rebuild preparation;
- mesh generation;
- block-model processing;
- rendering-related memory allocation;
- garbage collection pressure;
- chunk upload work;
- frame-time spikes during rapid chunk arrival.

These are hypotheses only.

This mission exists to determine what can actually be demonstrated.

---

# Objective

Characterize whether the representation produced by AntiXray v1 can reduce
Minecraft client-side processing cost compared with the equivalent original
representation.

The mission must determine:

1. which client-side boundaries are plausibly affected by block-state
   representation simplification;
2. which boundaries can be measured reproducibly;
3. whether v1-style replacement produces a measurable difference;
4. whether any difference is large enough to plausibly contribute to the
   historical reduction in lag;
5. which property, if any, should later become an explicit v2 representation
   requirement.

The mission must not optimize v2.

It must characterize behavior first.

---

# Primary Research Question

Does the simplified player representation produced by AntiXray v1 reduce
client-side processing cost even when network-byte savings are small?

---

# Secondary Questions

The investigation should answer, where technically possible:

- Does lower block-state diversity reduce client chunk decoding cost?
- Does it reduce palette/container processing cost?
- Does it reduce render-section rebuild cost?
- Does it reduce mesh generation work?
- Does it reduce allocations during chunk processing?
- Does it reduce garbage collection pressure?
- Does it reduce render-thread or worker-thread work?
- Does it reduce frame-time spikes while chunks arrive?
- Does the effect depend on the number or type of replaced protected blocks?
- Does the effect scale with many sections/chunks?
- Is any observed improvement actually caused by something other than
  representation simplification?

---

# Scope

This mission may:

- inspect Minecraft 26.2 client code and mappings;
- inspect the current v1 representation behavior;
- inspect existing characterization fixtures;
- identify client processing boundaries reached after chunk delivery;
- add characterization tests;
- add development-only measurement harnesses when required;
- create synthetic original and v1-style representations;
- compare equivalent representations at selected client boundaries;
- measure deterministic structural properties;
- measure timing only when the measurement boundary is sufficiently controlled;
- document findings;
- identify unanswered questions requiring a real-client experiment.

---

# Out Of Scope

This mission must not:

- change AntiXray production behavior;
- make v2 authoritative;
- change observation semantics;
- change protection semantics;
- change replacement semantics;
- change reveal semantics;
- change packet formats;
- change Minecraft compression configuration;
- change mixin injection points;
- add production telemetry;
- add permanent profiling infrastructure;
- add caching;
- add asynchronous processing;
- modify authoritative world state;
- claim performance improvements without evidence;
- redesign the v2 representation architecture.

Any optimization discovered during this mission belongs to a later,
separately reviewed mission.

---

# Required Baseline

Before investigating client cost, preserve the conclusions from the previous
missions.

The investigation must distinguish at least these representations:

## Original

The representation Minecraft would send without AntiXray replacement.

## V1-style transformed

The same logical section after the current v1 replacement behavior has
transformed hidden protected information into its selected safe terrain
representation.

Where useful, additional synthetic controls may be introduced, but they must
not replace the original-vs-v1 comparison.

---

# Representation Equivalence Rule

Comparisons must isolate representation structure as much as possible.

Original and transformed fixtures should preserve:

- section dimensions;
- block positions;
- non-protected block states;
- biome data where relevant;
- lighting inputs where relevant;
- chunk location where relevant;
- surrounding fixture conditions.

The intended variable is the player-visible block-state representation.

If another variable necessarily changes, document it explicitly.

---

# Investigation Phase 1 — Map The Client Pipeline

Identify the Minecraft 26.2 client-side path from receiving chunk information
to rendering the resulting terrain.

At minimum investigate boundaries related to:

```text
network packet
    |
    v
packet decode / deserialization
    |
    v
client chunk installation
    |
    v
section / palette / container state
    |
    v
render section scheduling
    |
    v
mesh / geometry generation
    |
    v
GPU upload / rendered terrain
```

The exact Minecraft classes and methods must be discovered from the current
26.2 mappings.

Do not assume names from previous Minecraft versions.

Document:

- relevant classes;
- relevant methods;
- thread boundaries;
- data structures involved;
- which stages depend directly on block-state diversity;
- which stages depend only on geometry or visible surfaces;
- which stages are unlikely to be affected.

---

# Investigation Phase 2 — Identify Candidate Cost Mechanisms

For every plausible client-side benefit, define the mechanism before measuring
it.

Examples:

## Palette complexity

Question:

Does replacing rare protected states with common terrain states reduce palette
complexity in a form that survives client decoding and affects later work?

Required evidence:

- actual client-side data structure;
- actual difference between fixtures;
- relationship to processing cost.

## Model or render-state diversity

Question:

Does replacing ore states with surrounding terrain reduce the number or
complexity of block models processed during rebuild?

Required evidence:

- render/rebuild code path;
- actual model/state work performed;
- measurable structural or runtime difference.

## Mesh complexity

Question:

Does the transformed representation produce fewer rendered quads, vertices, or
other mesh data?

Do not assume that replacing an ore with stone reduces geometry.

Both may render as ordinary full cubes.

Measure before concluding.

## Allocation pressure

Question:

Does transformed representation cause fewer temporary allocations during
decode, installation, or rebuild?

Use a controlled measurement if practical.

Do not infer allocation reduction from execution time alone.

## Scheduling/rebuild behavior

Question:

Does representation simplification change how much rebuild work is scheduled?

Verify whether block-state identity affects scheduling or only the work inside
an already-required rebuild.

---

# Investigation Phase 3 — Controlled Characterization

Prefer deterministic measurements before wall-clock benchmarks.

Possible structural measurements include:

- unique block-state count;
- palette size;
- palette representation type;
- serialized palette characteristics;
- number of model/state lookups;
- number of rendered quads;
- number of generated vertices;
- mesh-buffer size;
- number of render layers touched;
- number of temporary objects where instrumentation is safe;
- number of rebuild tasks;
- number of state-dependent operations.

Only measure properties that correspond to actual Minecraft 26.2 behavior.

Do not invent proxy metrics without explaining their relationship to the
client pipeline.

---

# Investigation Phase 4 — Timing Characterization

Timing measurements may be added only after a meaningful boundary is
identified.

If timing is measured:

- warm up the relevant code;
- compare identical fixture shapes;
- run repeated samples;
- avoid comparing first-run class loading against warmed execution;
- use monotonic timing;
- report counts, totals, minima/maxima or distributions where practical;
- distinguish exploratory timing from benchmark-quality evidence;
- document JVM, Minecraft, and environment limitations.

A small timing difference in a synthetic fixture must not automatically be
translated into a gameplay-performance claim.

---

# Investigation Phase 5 — Multi-Section Scaling

The historical observation occurred while rapidly loading terrain.

Single-section results therefore cannot alone explain the production effect.

If a measurable per-section difference exists, investigate how it behaves
across repeated section/chunk processing.

Questions include:

- Does the cost difference scale approximately linearly?
- Does it disappear after warmup?
- Does it accumulate enough to affect frame time?
- Does repeated processing introduce allocation or GC effects?
- Does the benefit become more or less significant with realistic section
  composition?

Do not extrapolate synthetic results beyond what the evidence supports.

---

# Real Client Boundary

Some hypotheses may not be testable reliably in plain JUnit.

If meaningful client behavior requires a running Minecraft client, explicitly
document that boundary.

A real-client experiment may later measure:

- frame time;
- frame-time percentiles;
- render-thread utilization;
- chunk rebuild worker activity;
- allocation rate;
- garbage collection;
- chunk upload behavior;
- high-speed terrain-loading stutter.

Such an experiment must compare controlled scenarios.

Do not add a permanent production client profiler during this mission.

---

# Elytra Scenario

The historical observation was especially noticeable during rapid terrain
loading.

If a real-client experiment becomes necessary, define a reproducible
high-speed traversal scenario.

The experiment should attempt to control:

- world/seed;
- route;
- render distance;
- simulation distance;
- graphics settings;
- Sodium or vanilla renderer status;
- shaders;
- client mods;
- JVM arguments;
- allocated memory;
- server state;
- network conditions;
- starting chunk cache state.

Cold-cache and warm-cache behavior must not be mixed without documentation.

---

# Vanilla And Modded Client Separation

MundoZ clients may use optimization mods.

Do not attribute a result to Minecraft generally if it is specific to Sodium,
Iris, or another renderer.

Where practical, distinguish:

1. vanilla Minecraft client behavior;
2. Sodium client behavior;
3. other relevant client modifications.

A mod-specific result is still useful, but it must be labeled correctly.

---

# Important Alternative Explanations

The investigation must actively attempt to disprove the representation-cost
hypothesis.

Possible alternative explanations include:

- network compression;
- packet scheduling;
- network throughput;
- server tick behavior;
- chunk generation timing;
- client chunk cache behavior;
- measurement noise;
- JVM warmup;
- garbage collection;
- Sodium-specific behavior;
- unrelated changes present during historical testing.

The previous protocol characterization already demonstrated that compressed
network size can change.

Client-side experiments must therefore avoid silently attributing all observed
improvement to rendering or decode cost.

---

# Required Characterization Fixtures

Reuse existing v1 characterization fixtures where technically appropriate.

Add fixtures representing different diversity and replacement patterns.

At minimum consider:

## Terrain-dominant section

Mostly one terrain state with a small number of protected resources.

## Resource-diverse section

Multiple protected resource states distributed through terrain.

## Highly diverse control section

Many unrelated block states, useful for determining whether the measured
effect tracks general palette/state diversity rather than AntiXray
specifically.

## No-replacement control

A section where v1 produces no representation change.

This control is important.

If original and transformed processing differs when the representations are
identical, the measurement harness is invalid or measuring unrelated work.

---

# Required Safety Invariants

All characterization must preserve:

- authoritative world state is unchanged;
- v1 remains the active authority where runtime behavior is involved;
- v2 remains non-authoritative;
- no test changes packet semantics;
- no test changes observation semantics;
- no test changes reveal behavior;
- no test enables production telemetry;
- no test introduces runtime work when characterization is not explicitly
  running.

---

# Required Documentation

Create:

```text
docs/architecture/ANTI_XRAY_CLIENT_REPRESENTATION_COST.md
```

The document must contain:

1. motivation;
2. previous established findings;
3. Minecraft 26.2 client pipeline;
4. candidate mechanisms investigated;
5. fixtures;
6. measurement method;
7. results;
8. rejected hypotheses;
9. limitations;
10. relationship to the historical Elytra observation;
11. implications for v2;
12. remaining unknowns.

---

# Evidence Classification

Every conclusion must be classified as one of:

## Proven structurally

Directly demonstrated by deterministic inspection or tests.

Example:

> The transformed fixture contains fewer unique block states at this client
> boundary.

## Measured experimentally

Observed through controlled runtime measurement.

Example:

> Under this harness, transformed rebuilds used X% less measured CPU time.

The measurement limitations must accompany the claim.

## Plausible but unproven

Supported by architecture or indirect evidence but not demonstrated.

## Rejected

The investigation produced evidence against the hypothesis.

## Unknown

The available test boundary cannot answer the question.

This classification should prevent historical observations from becoming
architectural facts without evidence.

---

# V2 Decision Boundary

This mission must not automatically convert any v1 behavior into a v2
requirement.

At completion, explicitly answer:

> Is there a demonstrated client-side efficiency property worth preserving in
> the v2 representation?

Possible outcomes:

### Yes

Document the exact property.

Examples:

- minimize representation state diversity;
- prefer an already-common terrain state when multiple safe replacements are
  semantically equivalent;
- preserve a measurable representation characteristic.

Do not implement it during this mission.

### No

Document that no meaningful client-side efficiency mechanism was demonstrated.

### Inconclusive

Document the missing experiment required to decide.

---

# Acceptance Criteria

The mission is complete when:

- the relevant Minecraft 26.2 client pipeline has been mapped;
- plausible client-cost mechanisms have been identified;
- at least the original and v1-style representations have been compared;
- a no-replacement control exists;
- deterministic metrics are preferred where available;
- timing claims are clearly separated from structural facts;
- alternative explanations are considered;
- historical observations are not presented as proof;
- production code behavior is unchanged;
- v2 authority is unchanged;
- findings are documented;
- remaining unknowns are explicit;
- the mission states whether a later v2 optimization mission is justified.

---

# Validation

Run all relevant focused tests introduced by this mission.

Then run:

```bash
git diff --check
./gradlew test
./gradlew clean build
git status
```

The worktree must contain only intentional mission changes.

---

# Commit Discipline

Prefer small commits representing distinct discoveries.

Possible sequence:

```text
Map Minecraft client representation pipeline
Characterize client representation structure
Measure client representation processing cost
Document client representation cost findings
```

The actual commits should follow the evidence discovered during the mission.

Do not force this sequence when the investigation disproves a hypothesis
earlier.

---

# Stop Conditions

Stop and document the boundary instead of implementing speculative behavior if:

- the relevant client path cannot be exercised reliably;
- a Minecraft client runtime is required but no controlled harness exists;
- measurements are dominated by noise;
- the experiment would require production behavior changes;
- the experiment would require permanent instrumentation;
- the experiment would require changing packet semantics;
- the evidence cannot distinguish representation cost from network effects.

A negative or inconclusive result is a valid mission outcome.

---

# Final Deliverable

The final report must answer:

1. What exactly does v1 simplify in the representation?
2. Which of those differences survive into the Minecraft client?
3. Which client processing stages are affected?
4. What deterministic differences were measured?
5. What runtime differences were measured?
6. Are those differences large enough to plausibly explain the historical
   improvement?
7. Which hypotheses were rejected?
8. What remains unknown?
9. Is there a specific efficiency property that v2 should preserve?
10. What should the next mission be?

No production optimization should be implemented until these questions have
been reviewed.
