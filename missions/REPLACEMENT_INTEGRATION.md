# Mission: Replacement Integration Slice

## Mission Objective

Complete the first inactive vertical slice that connects the existing
Minecraft block adapter, protection application service, replacement
application service, and Minecraft replacement representation adapter.

This mission must demonstrate that the existing v2 components can form
a coherent end-to-end flow without changing the active runtime.

## Mission Scope

Included:

- application orchestration between existing protection and replacement concepts;
- Minecraft input translation through `BlockIdentityMapper`;
- protection evaluation through the existing application boundary;
- replacement evaluation through the existing application boundary;
- Minecraft output translation through `ReplacementRepresentationMapper`;
- narrowly required context-building adapters;
- pure and Minecraft-backed automated tests;
- documentation updates where required.

Prefer completing and connecting existing concepts over creating new
abstractions.

## Expected Flow


Minecraft BlockState
        |
        v
BlockIdentityMapper
        |
        v
ProtectionEvaluationService
        |
        v
Replacement context construction
        |
        v
ReplacementEvaluationService
        |
        v
ReplacementResult
        |
        v
ReplacementRepresentationMapper
        |
        v
Minecraft BlockState

Valid, unsupported, or unsafe inputs must preserve the documented
fail-visible behavior.

## Out of Scope

Do not modify or integrate:

active v1 runtime behavior;
AntiXrayObfuscator;
AntiXrayRevealer;
mixins;
packet serialization;
palette manipulation;
chunk encoding;
reveal integration;
observation semantics;
player-specific packet delivery;
gameplay behavior;
Minecraft or build dependency versions.

Do not remove legacy code.

## Completion Criteria

The mission is complete when:

the existing protection and replacement components form one coherent
inactive vertical slice;
the orchestration remains separated from Minecraft packet serialization;
unsupported Minecraft blocks are handled safely;
unprotected blocks are not replaced;
protected blocks receive a valid replacement when one is available;
absence of a safe replacement produces the documented fail-visible result;
domain and application code remain free of Minecraft dependencies;
focused tests cover all meaningful decisions and boundary outcomes;
git diff --check passes;
./gradlew test passes;
./gradlew clean build passes;
no active runtime file is changed;
all commits follow one concept -> one commit -> one responsibility;
the branch is pushed to origin/codex/replacement-integration.


## Human Decision Points

Stop for human review if completion requires:

changing replacement-selection semantics;
defining terrain sampling behavior not already documented;
changing gameplay behavior;
modifying packets, palettes, chunks, or mixins;
integrating with the active runtime;
contradicting accepted ADRs;
expanding the mission into Observation or Reveal.


## Target Branch

codex/replacement-integration

## Expected Deliverables
implementation plan;
small implementation commits;
automated tests;
documentation updates when justified;
maintainer-style self-review;
pushed autonomous branch;
completion report with risks and remaining integration gaps.

## 3. Revise e versione a missão

```bash
git diff --check
git diff -- missions/REPLACEMENT_INTEGRATION.md
git status
