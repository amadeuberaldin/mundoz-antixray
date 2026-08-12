Guarded Runtime Integration Mission
Mission Goal

Prepare the smallest safe integration boundary between the existing
active AntiXray v1 runtime and the tested v2 architecture.

The purpose of this mission is to determine and implement, where safe,
the infrastructure needed for v2 to participate beside the existing
runtime without replacing active production behavior.

This mission must not switch production AntiXray behavior from v1 to v2.

The result should make a later controlled runtime migration possible.

Starting Point

The repository already contains tested v2 concepts for:

protected block classification;
Minecraft block identity mapping;
replacement policy;
replacement integration;
reveal policy and candidate adapters;
observation path semantics;
Minecraft observation path classification;
deterministic target sampling;
ordered read-only observation path collection;
multi-sample observation evaluation;
section replacement candidate collection.

The active v1 runtime remains the production authority.

The v2 components are intentionally not yet connected to the active
packet/chunk transformation path.

Core Safety Invariants

The mission must preserve all existing project invariants.

In particular:

never modify authoritative Minecraft world state;
never load or generate chunks only for AntiXray evaluation;
never emit invalid chunk, section, palette, or packet data;
prefer visible information over an invalid or uncertain hidden
representation;
preserve active v1 production behavior;
do not silently redefine observation semantics;
do not silently redefine replacement semantics;
do not silently redefine reveal semantics.
Primary Investigation

Before editing code, trace the complete active v1 runtime path from the
Mixin entry point through chunk/section serialization and AntiXray
transformation.

Compare that runtime path against the available v2 components.

Identify:

the exact active runtime entry point;
where player identity/context is available;
where chunk and section data are available;
where the current v1 candidate detection occurs;
where v1 decides whether a block is hidden;
where replacement representation is selected;
where serialized player-specific representation is produced;
where reveal/update behavior is triggered;
which stages mutate only representation;
which stages could safely call v2 without changing emitted data.
Guarded Integration Principle

The first v2 runtime participation must be observational.

V2 may evaluate the same inputs as v1 and produce internal decisions,
but those decisions must not control the representation sent to the
player during this mission.

Conceptually:

active runtime input
|
+----> v1 production path ----> emitted representation
|
+----> v2 shadow evaluation --> internal result only

The v1 result remains authoritative.

V2 shadow evaluation must not alter:

packets;
palettes;
serialized section data;
world state;
reveal behavior;
player-visible output.
Shadow Evaluation

Investigate whether a safe shadow-evaluation boundary can be introduced.

A shadow evaluation should, where practical:

reuse already available runtime information;
avoid additional chunk loads;
avoid authoritative world mutations;
execute the v2 protection/replacement/observation pipeline;
discard the v2 representation decision after evaluation;
leave v1 output completely unchanged.

Do not add shadow evaluation merely for architectural completeness.

It must have a concrete purpose:

prove that v2 can consume real runtime inputs;
expose integration gaps;
enable comparison with v1 behavior;
allow later migration with lower risk.
Comparison Model

Determine whether v1 and v2 decisions can be compared safely without
changing gameplay.

If practical, define a small comparison result representing cases such
as:

both systems reveal;
both systems hide;
v1 hides while v2 reveals;
v1 reveals while v2 hides;
v2 cannot safely evaluate.

Do not introduce a general telemetry framework.

Do not introduce persistent analytics.

Do not log every evaluated block in production.

Any comparison mechanism must remain small, bounded, and appropriate
for development/testing.

Performance Investigation

Observation path collection is potentially more expensive than the v1
six-neighbor visibility rule.

Before connecting it to a hot runtime path, determine:

how often the candidate evaluation would execute;
how many protected candidates may exist in a section;
how many observation samples each candidate may require;
how many traversed cells a sample may inspect;
whether repeated path evaluation can occur during chunk transmission;
whether the current architecture would cause avoidable repeated world
reads or object allocation.

Do not optimize speculatively.

Identify concrete cost boundaries first.

If the current design is clearly unsafe for runtime use because of
unbounded or excessive work, stop and report the problem rather than
inventing a new caching architecture.

Fail-Visible Boundary

Any inability of v2 shadow evaluation to obtain safe information must
remain fail-visible.

Examples include:

unavailable chunk data;
UNKNOWN observation path behavior;
unsupported Minecraft state;
incomplete mapping;
unexpected runtime context.

A v2 shadow failure must never make v1 production output less safe or
less valid.

Runtime Isolation

During this mission, do not:

replace the active v1 decision;
remove AntiXrayObfuscator;
remove AntiXrayRevealer;
remove existing Mixins;
redirect packet serialization to v2 output;
modify palette encoding behavior;
modify packet structure;
modify active reveal/update behavior;
delete legacy runtime code.

If safe integration requires any of those actions, stop for human
review.

Architecture Boundaries

Preserve dependency direction:

infrastructure -> application -> domain;
bootstrap -> application;
bootstrap -> infrastructure.

Domain remains pure Java.

Minecraft runtime types remain in infrastructure.

Packet serialization remains outside domain and application policy.

Do not move Minecraft classes into domain models for convenience.

Testing Requirements

Characterize the existing runtime before introducing any integration
hook.

Tests should cover, where feasible:

existing v1 output remains unchanged;
v2 shadow evaluation cannot modify world state;
v2 shadow evaluation cannot modify emitted representation;
v2 can consume real Minecraft section candidates;
protected and unprotected candidates are handled safely;
observable protected blocks produce the expected v2 decision;
fully occluded protected blocks produce the expected v2 decision;
UNKNOWN remains fail-visible;
missing chunks are not loaded;
failures in shadow evaluation do not corrupt v1 output.

Prefer executable characterization tests over assumptions.

Commit Discipline

Follow:

One concept -> one commit -> one responsibility.

Likely slices may include:

runtime boundary characterization;
guarded/shadow evaluation coordination;
v1/v2 comparison model or development diagnostics, if justified;
integration tests;
architecture/runtime documentation.

This list is guidance, not a requirement.

The Codex must determine the smallest coherent commit structure after
repository investigation.

Human Decision Boundaries

Stop for human review before:

allowing a v2 decision to affect player-visible output;
changing packet or palette serialization;
replacing any active v1 runtime component;
changing observation semantics;
changing protection targets;
changing replacement semantics;
changing reveal semantics;
introducing caching or persistent runtime state;
introducing asynchronous evaluation;
introducing a general telemetry system;
changing gameplay behavior;
accepting a performance tradeoff that could materially affect the
server.
Mission Planning

Before editing:

read AGENTS.md;
read this mission;
read all required architecture and maintenance documentation;
inspect the complete current Java source tree;
inspect Git history from v1.0.0 to HEAD;
trace the active v1 runtime;
map existing v2 components against that runtime;
identify integration and performance risks;
propose the smallest safe vertical slices;
estimate the commit structure;
identify exact expected files/packages;
identify decisions requiring human approval.

Do not edit files during planning.

Wait for human approval of the plan.

After approval, execute autonomously within the accepted scope.

Mission Completion

At completion provide:

branch name;
commits created;
exact files changed;
tests executed;
build results;
active runtime behavior impact;
architecture impact;
v1/v2 comparison findings;
performance findings;
known limitations;
unresolved integration gaps;
decisions still requiring human review;
recommended next migration step;
recommended review order.

Push only to the dedicated codex/* branch.

Do not merge the branch.
