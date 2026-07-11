# MundoZ AntiXray Domain Mapping

This document maps current implementation concepts to the future domain model.

## Protected Blocks

Current:

AntiXrayBlocks

Future:

domain.protection.ProtectedBlockPolicy

Responsibility:

Determine if a block requires visibility evaluation.

---

## Visibility Decision

Current:

Embedded inside AntiXrayObfuscator

Future:

domain.visibility.VisibilityDecision

Responsibility:

Represent whether information should be visible or hidden.

---

## Replacement Strategy

Current:

findSafeReplacement()

Future:

domain.replacement.ReplacementPolicy

Responsibility:

Choose a safe representation for hidden blocks.

---

## Exposure Rule

Current:

isExposed()

Future:

domain.visibility.ExposureRule

Responsibility:

Determine whether a block is naturally visible.

---

## Reveal Event

Current:

AntiXrayRevealer

Future:

application.reveal.RevealBlocksUseCase

Responsibility:

Process newly exposed information.

---

## Transmission Context

Current:

AntiXrayContext

Future:

infrastructure.minecraft.context.ChunkTransmissionContext

Responsibility:

Store temporary Minecraft transmission information required during chunk serialization.

The context belongs to infrastructure because it depends on:

- ServerPlayer;
- ServerLevel;
- LevelChunk.

---

## Minecraft Integration

Current:

PlayerChunkSenderMixin
ClientboundLevelChunkPacketDataMixin

Future:

infrastructure.minecraft.mixin

Responsibility:

Intercept Minecraft events and delegate processing to application services.

---

# Migration Rule

The migration must preserve behavior.

The V2 implementation should reproduce V1 behavior before introducing improvements.
