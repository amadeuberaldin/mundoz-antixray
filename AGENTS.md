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
