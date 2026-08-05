# MundoZ AntiXray Replacement Integration

## Purpose

The replacement integration slice connects the existing protection and
replacement decisions to Minecraft block-state translation without activating
the v2 runtime.

## Inactive Flow

```text
Minecraft BlockState
        |
        v
BlockIdentityMapper
        |
        v
ReplacementIntegrationService
        |
        +-- ProtectionEvaluationService
        |
        +-- ReplacementEvaluationService
        |
        v
ReplacementResult
        |
        v
ReplacementRepresentationMapper
        |
        v
Minecraft BlockState
```

`ReplacementIntegrationService` is a small application coordinator. The
existing evaluation services remain focused on their individual policies; the
coordinator sequences them and prevents replacement evaluation for unprotected
information.

`ReplacementContextMapper` translates available Minecraft terrain states
into the existing domain replacement representations. It recognizes only
stone, deepslate, netherrack, end stone, and tuff. It does not read a world,
sample terrain, or select a replacement.

`MinecraftReplacementService` owns the Minecraft boundary. It maps the input,
delegates all decisions to the application layer, and maps a successful
replacement back to a valid default Minecraft block state.

## Failure Boundaries

The original Minecraft state remains visible when:

- the input block has no supported domain identity;
- protection evaluation returns `NOT_PROTECTED`;
- no accepted terrain representation is available;
- replacement evaluation returns `KEEP_VISIBLE`.

Unsupported or unsafe candidate states are not passed into the domain
replacement context. The slice never invents a fallback state.

## Runtime Status

This slice is inactive. It does not modify:

- `AntiXrayObfuscator`;
- `AntiXrayRevealer`;
- mixins;
- chunks or palettes;
- packet serialization;
- the authoritative Minecraft world.

Terrain sampling and active packet integration remain deferred to a later
mission with explicit runtime and serialization review.
