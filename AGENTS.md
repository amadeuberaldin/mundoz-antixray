# MundoZ AntiXray Agent Instructions

## Project Goal

Refactor MundoZ AntiXray v1 into the v2 domain-driven architecture
defined by this repository.

The stable v1 implementation is preserved by tag `v1.0.0`.

Development occurs on branch `v2-domain-architecture`.

## Required Reading

Before modifying code, read:

- README.md
- all files under docs/architecture/
- all files under docs/maintenance/
- the current Java source tree
- the Git history from v1.0.0 to HEAD

Architecture documents are living guides.

They are authoritative for system boundaries, responsibilities,
accepted decisions, and migration direction.

## Core Invariant

The real Minecraft world must never be modified by AntiXray.

Only the player-specific representation may be transformed.

## Dependency Direction

Allowed:

- infrastructure -> application -> domain
- bootstrap -> application
- bootstrap -> infrastructure

Forbidden:

- domain -> Minecraft
- domain -> Fabric API
- domain -> Mixins
- domain -> packets
- application -> Mixins
- application -> Minecraft packet serialization

## Domain Purity

The domain must use pure Java concepts.

Minecraft types are infrastructure-only, including:

- BlockState
- BlockPos
- ServerLevel
- ServerPlayer
- LevelChunk
- LevelChunkSection
- FriendlyByteBuf
- Minecraft packet classes

## Migration Rules

- Preserve production knowledge learned from v1.
- Do not blindly preserve every v1 implementation detail.
- Follow accepted ADRs.
- Do not change unrelated behavior.
- Do not mix refactoring and new features in one commit.
- Prefer valid visible information over invalid client representation.
- Never generate inconsistent chunk, palette, or packet data.
- Do not modify the authoritative world state.
- Do not remove legacy code until its replacement is tested and integrated.

## Scope

Complete the AntiXray policy and its safe integration first.

The broader observation model is architectural direction.

Do not expand this migration into a complete general-purpose
Observation Engine, rendering engine, or distant-horizon system
unless explicitly requested.

## Git Discipline

Follow this rule:

One concept -> one commit -> one responsibility.

Every commit must:

- make one coherent change;
- compile;
- pass relevant tests;
- avoid unrelated formatting;
- use a precise English commit message.

Do not create large catch-all refactoring commits.

Do not commit unless explicitly instructed.

Do not push unless explicitly instructed.

## Working Process

For each implementation task:

1. Inspect the relevant v1 implementation.
2. Read the applicable architecture documents and ADRs.
3. Identify the smallest safe change.
4. Explain the proposed change before editing.
5. Modify only the required files.
6. Run `git diff --check`.
7. Run relevant tests.
8. Run `./gradlew clean build`.
9. Show the resulting diff.
10. Explain behavior impact and architectural impact.
11. Stop for review before committing unless explicitly instructed.

## Compatibility

Current target:

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.149.2+26.2
- Fabric Loom 1.14.10
- Gradle 9.2.1
- Java 25

## Behavior Priorities

Priority order:

1. Valid and consistent client representation.
2. Unmodified authoritative world state.
3. Normal gameplay correctness.
4. AntiXray protection.
5. Performance optimization.

False-positive hiding is worse than safely revealing information.

## Existing Architectural Direction

The system distinguishes:

- world state;
- observation;
- policy evaluation;
- player representation;
- Minecraft packet encoding.

AntiXray is a policy built on top of the observation architecture.

Observation describes perception.

Observation is not a permission system.

Representation determines how information is encoded for a client.

## Current Development Discipline

Do not introduce abstractions only because they may be useful later.

Prefer completing small vertical slices that connect:

- Minecraft infrastructure;
- application use cases;
- domain decisions;
- tests.

Review existing incomplete concepts before adding new ones.

## Architecture Authority

Architecture documents and accepted ADRs define the intended
responsibilities, boundaries, and direction of the project.

If documentation and implementation disagree:

- do not silently change either one;
- identify the exact conflict;
- show the relevant documentation and implementation;
- explain the possible interpretations;
- wait for human review before changing behavior or architecture.

Accepted ADRs have priority over older descriptive documents.

Do not automatically rewrite an ADR to match the current code.

Do not automatically rewrite production code to match an ADR
when the behavioral consequences have not been reviewed.

## Abstraction Discipline

Do not introduce a new abstraction when an existing concept
can be completed, corrected, or connected safely.

Before creating a new:

- model;
- interface;
- policy;
- service;
- context;
- decision;
- result;
- adapter;

search the current source tree and architecture documents
for an existing concept with the same or overlapping responsibility.

Prefer completing small vertical slices over adding disconnected scaffolding.

A vertical slice should connect, when applicable:

- Minecraft infrastructure;
- application coordination;
- domain decisions;
- executable tests.

Unused future-oriented concepts must not be integrated into production
only because they already exist.

Observation Engine concepts are architectural direction until they
support a concrete and tested AntiXray use case.

## Human Approval Boundaries

The agent may independently perform:

- repository inspection;
- compilation;
- test execution;
- small implementations explicitly requested by the user;
- test creation for behavior already defined by accepted documentation.

The agent must stop for human review before:

- changing gameplay behavior;
- changing accepted protection targets;
- defining visibility or observation semantics;
- changing packet or palette serialization behavior;
- replacing the active v1 runtime path;
- removing legacy production code;
- expanding the AntiXray migration into a general-purpose Observation Engine;
- creating commits or pushing changes unless explicitly authorized.

When uncertain, report the uncertainty instead of choosing silently.

## Autonomous Agent Branches

The agent may work autonomously only on a dedicated branch whose name
starts with:

- codex/

Examples:

- codex/replacement-slice
- codex/reveal-migration
- codex/chunk-integration

The agent must never perform autonomous work directly on:

- main
- v2-domain-architecture
- release branches
- production branches

On a dedicated `codex/*` branch, the agent may:

- inspect the repository;
- modify files within the approved mission scope;
- create and update tests;
- run validation commands;
- create multiple small commits;
- push commits to the corresponding remote `codex/*` branch.

Each commit must still follow:

One concept -> one commit -> one responsibility.

Before every commit, the agent must:

1. run `git diff --check`;
2. run relevant focused tests;
3. run `./gradlew test`;
4. run `./gradlew clean build`;
5. confirm that all changed files belong to the approved mission.

The agent must not:

- merge into another branch;
- rebase the target branch;
- force-push;
- delete remote branches;
- modify Git history;
- create releases or tags;
- change the approved mission scope silently.

## Autonomous Mission Completion

When assigned an autonomous mission, the agent should continue working
without requesting approval for every small implementation detail.

It must stop when:

- the approved mission is complete;
- tests or build cannot be fixed within the approved scope;
- documentation and implementation conflict;
- a gameplay or architecture decision is required;
- packet, palette, or runtime behavior would change;
- continuing would require expanding the approved scope.

At completion, the agent must provide:

- the branch name;
- the commits created;
- the exact files changed;
- tests and builds executed;
- known limitations;
- unresolved questions;
- recommended review order.

The agent must push only to its dedicated `codex/*` branch and must not
merge the branch.
