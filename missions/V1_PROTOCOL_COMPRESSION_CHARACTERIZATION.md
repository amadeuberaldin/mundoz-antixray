# Mission — V1 Protocol Compression Characterization

## Status

Planned.

## Context

The previous V1 representation-efficiency characterization established an
important distinction between logical representation simplification and actual
serialized section size.

Controlled Minecraft 26.2 fixtures showed that V1 can substantially reduce
the number of actual block states represented in a section while leaving:

- palette width unchanged;
- raw serialized section size unchanged.

However, the resulting representation became more repetitive.

When the isolated serialized section bytes were compressed with controlled
zlib compression, changed fixtures showed compression reductions of
approximately 11.5% to 28.0%.

Those results establish that V1 representation simplification can improve
compressibility.

They do NOT establish that Minecraft actually transmits fewer bytes over the
network.

They also do not establish that compression is responsible for the production
observation that V1 appeared to reduce client-side lag during rapid chunk
loading and Elytra travel.

Minecraft protocol framing, compression thresholds, packet composition,
compression scope, networking implementation, and client processing may all
affect the real result.

Before preserving any supposed V1 optimization in V2, the actual protocol
boundary must be characterized.

---

# Objective

Determine whether V1 representation transformation produces measurably smaller
compressed Minecraft protocol output under Minecraft 26.2 protocol behavior.

The mission must distinguish:

1. raw section serialization size;
2. raw chunk-packet payload size;
3. Minecraft protocol compression eligibility;
4. compressed protocol payload size;
5. framing overhead;
6. representation state diversity.

The investigation must determine whether V1's observed increase in
compressibility survives at the real Minecraft protocol compression boundary.

---

# Primary Question

For equivalent world information, does the representation produced by V1 cause
Minecraft 26.2 to transmit fewer compressed bytes than the original
representation?

---

# Secondary Questions

The investigation should answer, where technically possible:

- What compression implementation is used by the Minecraft 26.2 server path?
- At what boundary is compression applied?
- What data is included in one compressed unit?
- What compression threshold is relevant?
- Are chunk packets normally above that threshold?
- Does simplifying one or more chunk sections materially affect the compressed
  packet?
- Does the gain remain when section data is embedded in its real packet
  context?
- Does the gain become larger when multiple transformed sections exist in the
  same chunk?
- Are there cases where V1 transformation has effectively no compression
  benefit?
- Are there cases where compression becomes slightly worse?
- Is the result dependent on terrain composition or dimension?
- Can protocol compression savings plausibly explain part of the previously
  observed production improvement?

---

# Required Investigation

## 1. Inspect the Minecraft 26.2 networking path

Identify the actual server-side path from chunk packet creation to compressed
network output.

Document the relevant Minecraft/Netty classes and methods.

Determine:

- where packet serialization occurs;
- where protocol framing occurs;
- where the compression threshold is checked;
- where compression occurs;
- which compression algorithm/implementation is used;
- whether compression operates on the complete encoded packet payload or a
  smaller/larger unit.

Do not assume that isolated `Deflater` or zlib compression exactly reproduces
the Minecraft protocol path.

The actual implementation must be inspected.

---

## 2. Preserve the previous characterization baseline

Reuse the conceptual fixture families from
`V1RepresentationEfficiencyCharacterizationTest` where practical.

At minimum include:

- uniform stone control;
- sparse Overworld ores;
- multiple ore types;
- dense synthetic protected blocks;
- mixed terrain;
- Nether protected resources;
- End/control terrain.

Additional fixtures may be added when necessary to exercise protocol behavior.

Do not change the previous characterization merely to make this investigation
easier.

---

## 3. Compare original and V1 representations

For each meaningful fixture, produce equivalent original and V1-transformed
representations.

Record separately:

- protected candidate count;
- replaced count;
- actual block-state diversity before;
- actual block-state diversity after;
- palette width before;
- palette width after;
- raw section bytes before;
- raw section bytes after.

These measurements preserve continuity with the previous characterization.

---

## 4. Characterize real packet context

Where practical, place the representation inside the same Minecraft packet
structure used for normal chunk transmission.

Measure:

- uncompressed packet payload bytes;
- compression eligibility;
- compressed payload bytes;
- protocol framing overhead;
- resulting encoded bytes attributable to the complete protocol unit being
  studied.

Clearly document what each number includes.

Do not label a measurement "network bytes" unless it actually represents the
corresponding encoded protocol output.

If the test cannot reproduce the complete network boundary, state the exact
boundary that was reproduced.

---

## 5. Compression threshold behavior

Explicitly characterize behavior around the configured protocol compression
threshold.

Include cases:

- below threshold;
- near threshold;
- above threshold;

where technically practical.

Determine whether representation simplification can cause meaningful threshold
effects.

Do not modify production server compression configuration as part of this
mission.

Controlled tests may instantiate equivalent compression behavior with explicit
test configuration.

---

## 6. Multi-section chunk behavior

A single section may understate the real benefit because Minecraft chunk
packets can contain multiple sections.

Where practical, characterize representative chunks containing:

- one changed section;
- several changed sections;
- mostly unchanged sections;
- many ore-bearing/transformed sections.

Determine whether repetitive terrain replacement across sections improves
packet-level compression more than isolated section compression suggested.

---

## 7. Dimension-sensitive cases

At minimum characterize:

### Overworld

Dominant terrain replacements such as:

- stone;
- deepslate;

where applicable.

### Nether

Dominant replacement:

- netherrack;

where applicable.

### End

Use as a control unless a supported protected target produces a meaningful
test case.

Do not invent protected targets solely to create a favorable compression
result.

---

# Measurement Rules

All reported measurements must clearly identify their boundary.

Examples:

- actual block-state count;
- palette bits per entry;
- serialized section bytes;
- complete packet payload bytes;
- compressed protocol payload bytes;
- framed protocol bytes.

Never compare numbers from different boundaries as though they represented the
same quantity.

For every percentage reduction, retain the corresponding absolute byte counts.

Example:

original: 12,450 bytes
v1:       10,210 bytes
reduction: 2,240 bytes (17.99%)

Do not report percentage alone.

Important Distinction

This mission investigates protocol/network representation efficiency.

It does NOT establish client performance.

Even if V1 substantially reduces transmitted bytes, the mission must not claim
that this explains:

FPS improvement;
frame-time improvement;
chunk rebuild cost;
rendering cost;
GPU workload;
client memory allocation;
client decode cost.

Those require a separate client-side characterization mission.

Likewise, failure to find substantial protocol savings does not disprove the
previous production observation.

It would instead increase the importance of investigating client-side effects.

Runtime Safety

The active Minecraft runtime must remain unchanged.

V1 remains authoritative.

V2 remains shadow-only.

This mission must not:

make V2 authoritative;
change V1 obfuscation semantics;
change observation semantics;
change protected targets;
change replacement priority;
change reveal behavior;
modify packet output in production;
modify palette behavior in production;
change server compression settings;
add persistent telemetry;
add production packet logging;
retain player information;
retain coordinates;
retain packet contents;
retain world data.

Prefer deterministic tests and isolated characterization over runtime
instrumentation.

If runtime instrumentation appears necessary, stop and document why rather
than adding it automatically.

Architecture Boundaries

The investigation may inspect Minecraft networking internals.

Production domain and application code must not gain dependencies on:

Netty;
packet compression;
packet framing;
FriendlyByteBuf;
protocol codecs.

Protocol compression remains an infrastructure concern.

This characterization must not move networking concepts into the AntiXray
domain model.

Allowed Changes

The mission may add:

focused characterization tests;
test-only helpers;
architecture/characterization documentation.

Production source changes should not be necessary.

If production code appears necessary, stop and document the requirement before
changing it.

Forbidden Changes

Do not modify:

active AntiXray runtime behavior;
V1 output decisions;
V2 output authority;
observation policy;
protection policy;
replacement policy;
reveal policy;
mixin injection points;
packet serialization behavior;
palette implementation;
server compression configuration;
world state.

Do not add:

production telemetry;
packet dumps;
player tracking;
persistent measurements;
background threads;
asynchronous profiling infrastructure.
Required Tests

Add focused characterization tests capable of reproducing the relevant
Minecraft 26.2 compression behavior as closely as practical.

Tests must be deterministic.

The tests must not require:

an external Minecraft client;
Internet access;
a production server;
persistent world files.

Where Minecraft bootstrap is required, follow the repository's documented
plain-JUnit bootstrap sequence.

Required Documentation

Create:

docs/architecture/ANTI_XRAY_V1_PROTOCOL_COMPRESSION.md

Document:

the Minecraft 26.2 packet/compression path inspected;
the exact measurement boundary reproduced;
compression threshold behavior;
fixture methodology;
original versus V1 measurements;
absolute byte differences;
percentage differences;
supported hypotheses;
rejected hypotheses;
remaining uncertainties;
implications for future V2 representation design.

Explicitly separate measured facts from inference.

Required Result Classification

At mission completion classify the protocol result as one of:

A — Material protocol reduction

V1 representation simplification produces consistent and meaningful reduction
at the reproduced Minecraft protocol compression boundary.

B — Small protocol reduction

V1 produces measurable savings, but their magnitude alone is unlikely to
explain the previously observed production improvement.

C — No meaningful protocol reduction

The isolated zlib benefit does not materially survive the reproduced Minecraft
protocol context.

D — Inconclusive

The repository/test environment cannot reproduce enough of the actual
Minecraft protocol boundary to draw a reliable conclusion.

Do not force classification A, B, or C if the evidence supports D.

V2 Preservation Analysis

If protocol savings are demonstrated, identify the properties responsible for
them.

Potential properties to investigate include:

replacing hidden protected states with dominant terrain;
reducing actual state diversity;
increasing repeated state patterns;
producing longer repeated sequences;
simplifying representation across multiple sections.

Do not conclude that V2 must reproduce V1's exact hidden-block set.

V2 observation semantics remain authoritative for deciding what information
may be hidden in the future.

The goal is to determine whether V2 should preserve a representation-efficiency
property after observation has decided that hiding is valid.

Decision Boundary

This mission does not authorize implementation of any optimization in V2.

At completion, report evidence and preservation candidates only.

A separate reviewed mission will decide whether and how any demonstrated
representation-efficiency property belongs in authoritative V2.

Validation

Before completion run:

git diff --check

./gradlew test

./gradlew clean build

Run any new focused characterization task/test separately as well.

All validation must pass.

Commit Discipline

Use small commits with one responsibility each.

Suggested progression:

inspect and characterize Minecraft protocol compression boundary;
add deterministic packet/compression characterization fixtures;
compare original and V1 representations;
document findings;
strengthen or correct measurements discovered during self-review.

Do not squash during the mission.

Do not merge the branch.

Do not push unless explicitly instructed.

Completion Report

At completion report:

commits created;
files changed;
Minecraft compression path identified;
exact reproduced measurement boundary;
measurements obtained;
compression-threshold findings;
single-section findings;
multi-section findings;
dimension-sensitive findings;
supported hypotheses;
rejected hypotheses;
remaining uncertainties;
A/B/C/D result classification;
suggested V2 preservation properties;
validation executed;
confirmation that runtime/player-visible behavior did not change.

Do not claim that protocol savings explain client performance unless separately
measured.
