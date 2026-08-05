# MundoZ AntiXray Runtime Integration Readiness

## Purpose

This document audits the active v1 runtime and maps the inactive v2 slices to
their eventual runtime boundaries. It defines readiness work only. The active
runtime, packets, palettes, chunks, mixins, and gameplay remain unchanged.

## System Boundaries

The authoritative world is the Minecraft server state. AntiXray may read that
state, but it must never modify it.

Observation analysis derives facts about an observer and a target. Observation
policy turns those facts into an `ObservationDecision`. AntiXray policies then
decide whether protected information should be replaced or revealed.

Player representation is temporary, observer-specific information. Minecraft
packet and chunk encoding serializes that representation. Encoding is an
infrastructure responsibility and must not leak into application or domain
code.

## Active v1 Obfuscation Flow

1. `PlayerChunkSenderMixin` injects at the head of `sendChunk` and stores the
   player, level, and chunk in `AntiXrayContext`.
2. Minecraft constructs `ClientboundLevelChunkPacketData` and calls
   `LevelChunkSection.write` from `extractChunkData`.
3. `ClientboundLevelChunkPacketDataMixin` redirects that write to
   `AntiXrayObfuscator.writeSection`.
4. The obfuscator writes the original section immediately when transmission
   context is absent or the section contains no v1 protected block.
5. `findSafeReplacement` checks the current section for stone, deepslate,
   netherrack, end stone, and tuff in that priority order. It then permits an
   arbitrary first solid fallback after excluding protected blocks, air,
   bedrock, barrier, obsidian, and crying obsidian.
6. The obfuscator copies the section. It rejects an unassociated section and
   writes the original section when its index cannot be found in the active
   chunk.
7. For each protected block, v1 evaluates exposure through the six adjacent
   world positions. Air, non-occluding blocks, and fluids count as transparent.
8. v1 also applies a player-distance branch to structure-like blocks.
9. Hidden blocks are changed only in the copied section. The authoritative
   chunk is not changed.
10. If no representation changed, the original section is written. Otherwise,
    copied-section counts are recalculated and the copy is serialized.
11. `PlayerChunkSenderMixin` clears the thread-local context on normal method
    return.

The current context cleanup is injected at `RETURN`, not a Java `finally`
boundary. Exceptional exits therefore require explicit consideration before a
future runtime migration, but this readiness mission does not alter the mixin.

## Active v1 Reveal Flow

1. `MundoZAntiXrayMod` registers `AntiXrayRevealer` during initialization.
2. `PlayerBlockBreakEvents.AFTER` triggers after a block break.
3. Non-server worlds and non-server players are ignored.
4. The revealer scans the inclusive radius-four cube around the changed block,
   reading 729 authoritative block states.
5. `AntiXrayBlocks` identifies v1 protected candidates.
6. Exposure is evaluated through the same six-neighbor transparency rule used
   by obfuscation.
7. For an exposed candidate, a `ClientboundBlockUpdatePacket` containing the
   authoritative state and position is sent through that player's connection.

The update changes one player's client representation. It does not modify the
world. The active reveal trigger, radius, adjacency rule, and packet delivery
are gameplay and runtime behavior outside this mission.

## Existing v2 Slices

### Protection

`BlockIdentityMapper`, `ProtectionEvaluationService`, and the protection domain
form a tested input and decision boundary. The v2 protected set intentionally
excludes geode blocks and `TUFF_SLAB`, while the active v1 path still includes
them. Replacing the classifier would therefore change behavior and requires
human approval.

### Replacement

`MinecraftReplacementService` connects Minecraft input mapping, protection,
replacement selection, and Minecraft output mapping. It is inactive and
fail-visible. Its missing runtime input is the list of accepted terrain states
available in the section being represented.

The v2 policy accepts only the five documented terrain representations. It does
not preserve v1's arbitrary solid fallback. This is safer because absence of an
accepted representation keeps the original block visible.

### Reveal

`MinecraftRevealCandidateScanner` preserves the radius-four read boundary and
maps supported candidates without modifying the world. `DefaultRevealPolicy`
combines an already-produced protection decision and observation decision.

Missing pieces are application coordination, a real observation-decision
source, and a player-representation output adapter. These cannot be connected
to the runtime until observation semantics and failure behavior are approved.

### Observation

`ObservationContext`, `ObservationDecision`, and `ObservationPolicy` describe
the intended boundary. The current `ObservationEvaluationService` always
returns `OBSERVED`, does not consume `ObservationPolicy`, and does not analyze
facts. It is incomplete scaffolding and must not be used as a runtime source.

Accepted ADR 0003 explicitly rejects treating physical contact as equivalent
to observation. Consequently, the v1 adjacency check cannot silently become
the v2 observation implementation.

## Exact Future Integration Points

For chunk obfuscation, the existing redirect around
`LevelChunkSection.write` is the current representation-encoding boundary. A
future v2 section representation builder could be called before encoding, but
changing that redirect or the written section requires packet and palette
review.

For reveal, the existing post-break event is the current candidate trigger.
Future infrastructure may reuse that event only after an application use case
can obtain an approved observation decision and emit a player-specific update.

`AntiXrayContext` is the current bridge from player/chunk transmission to
section serialization. A future infrastructure context must preserve nesting,
cleanup, player association, and fail-visible behavior before it can replace
the legacy context.

## Required Inputs and Missing Components

### Replacement terrain candidates

The first compatible source is the section currently being represented. It may
read whether stone, deepslate, netherrack, end stone, or tuff exists in that
section and pass only those states to `MinecraftReplacementService`.

This source is read-only. It must not inspect or modify encoded palette data,
copy the section, sample neighboring chunks, invent arbitrary fallbacks, or
change replacement priority.

### Reveal observation decisions

No approved implementation exists. The eventual source must calculate facts
from authoritative state and observer context, then delegate to an observation
policy. It must not equate adjacency with visibility merely to reproduce v1.

Required decisions include the minimum facts, handling of unloaded or
unavailable world information, observer position semantics, occlusion, fluids,
transparent blocks, and the fail-visible reveal outcome. These are human
decision points.

### Representation output

Chunk representation construction and block-update delivery are distinct
Minecraft infrastructure boundaries. The application layer should return
decisions or representation changes without knowing `FriendlyByteBuf`, chunk
palettes, or packet classes. Concrete output ports or adapters should be added
only when a reviewed runtime caller demonstrates their exact contract.

## Safety Contracts

- Missing transmission context writes the original section.
- Unsupported Minecraft input preserves the original state.
- `NOT_PROTECTED` preserves the original state.
- Missing accepted replacement candidates preserve the original state.
- Invalid or unavailable observation information must not cause obfuscation.
- Domain and application code never depend on Minecraft serialization.
- Candidate collection is read-only.
- Representation construction never modifies authoritative world state.
- Section copy or encoding failure must fall back to a complete original
  section, never a partially transformed representation.
- Reveal observation failure behavior remains unresolved; choosing whether to
  reveal or retain a hidden client state affects gameplay and requires review.

## Readiness Gaps

- approved observation facts and policy implementation;
- application coordination for reveal;
- player-specific reveal output contract;
- temporary v2 section representation builder;
- byte-level characterization of original versus transformed section encoding;
- comparative v1/v2 behavior fixtures;
- reviewed lifecycle replacement for `AntiXrayContext`;
- reviewed bootstrap wiring for v2 services;
- explicit approval for the v1-to-v2 protected-set behavior change;
- explicit approval for any mixin or runtime-path modification.
