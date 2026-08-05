# Mission: Runtime Integration Readiness

## Mission Objective

Determine and prepare the safest incremental path for integrating the
tested v2 AntiXray slices into the active Minecraft runtime.

The mission must analyze the existing Protection, Replacement, Reveal,
Observation, Minecraft adapters, v1 obfuscation path, v1 reveal path,
mixins, and chunk representation flow as one system.

The result must make the future runtime migration concrete, testable,
incremental, and reviewable without changing active production behavior.

## Mission Scope

Included:

- audit the integrated v2 Protection, Replacement, and Reveal slices;
- inspect the existing Observation concepts and current implementation status;
- inspect the active v1 obfuscation and reveal runtime paths;
- inspect mixins and Minecraft packet/chunk representation boundaries;
- identify exact runtime integration points;
- identify missing application coordination;
- identify missing adapters, ports, safety contracts, and tests;
- define the terrain-candidate source required by replacement;
- define the observation-decision source required by reveal;
- define fail-visible behavior at every integration boundary;
- create safe, inactive preparatory code where justified;
- create test infrastructure that does not alter active runtime behavior;
- document the complete incremental migration sequence;
- produce a commit-by-commit implementation roadmap.

Prefer connecting and validating existing concepts over creating new
future-oriented abstractions.

## Required Analysis

The mission must trace and document the current runtime flows.

### Current v1 obfuscation flow

Identify:

- where chunk data is intercepted;
- where protected blocks are identified;
- where exposure is evaluated;
- where replacement blocks are selected;
- where chunk or palette data is modified;
- where fail-safe behavior occurs.

### Current v1 reveal flow

Identify:

- what triggers reveal processing;
- how reveal candidates are found;
- how exposure is evaluated;
- how block updates are sent;
- which player-specific representation is affected.

### Target v2 flow

Determine how the following existing concepts should eventually participate:


Minecraft world and packet data
        |
        v
Minecraft infrastructure adapters
        |
        v
Protection evaluation
        |
        v
Observation facts and decision
        |
        v
Replacement or reveal policy
        |
        v
Player-specific representation
        |
        v
Minecraft packet or update adapter


The mission must distinguish:

authoritative world state;
observation analysis;
policy decisions;
representation construction;
packet or chunk encoding.

## Allowed Preparatory Work

The agent may implement only inactive and behavior-neutral preparation,
such as:

pure application coordination;
ports or adapters required by demonstrated integration gaps;
test fixtures;
fake or in-memory representation boundaries;
characterization tests for existing v1 behavior;
dependency-boundary tests;
documentation;
migration plans.

Any new class must satisfy the architectural-justification rules in
AGENTS.md.

## Out of Scope

Do not:

change active v1 runtime behavior;
replace AntiXrayObfuscator;
replace AntiXrayRevealer;
modify active mixin behavior;
modify packet serialization;
modify chunk palettes or encoded chunk data;
register new runtime events;
change reveal radius or reveal trigger behavior;
define new observation or visibility semantics;
change protection targets;
change replacement priority;
change gameplay behavior;
remove legacy code;
change Minecraft, Fabric, Loom, Gradle, or Java versions;
merge into v2-domain-architecture.

## Human Decision Points

Stop for human review if progress requires:

deciding visibility or observation semantics;
changing packet or palette behavior;
changing chunk serialization;
changing active mixin injection points;
modifying runtime gameplay behavior;
choosing between conflicting ADRs;
replacing a legacy runtime component;
introducing an abstraction whose responsibility is not demonstrated by
a concrete integration gap;
expanding the mission beyond readiness and preparation.

## Completion Criteria

The mission is complete when:

the complete v1 obfuscation runtime path is documented;
the complete v1 reveal runtime path is documented;
every existing v2 slice is mapped to a future runtime integration point;
missing integration components are identified;
required safety contracts are explicit;
required characterization and integration tests are identified;
safe preparatory tests or inactive infrastructure are implemented where
justified;
a commit-by-commit migration sequence is documented;
the first recommended runtime integration mission is clearly defined;
no active runtime behavior has changed;
no packet, palette, chunk, mixin, or gameplay behavior has changed;
domain and application dependency boundaries remain valid;
git diff --check passes;
focused tests pass;
./gradlew test passes;
./gradlew clean build passes;
all commits follow one concept -> one commit -> one responsibility;
the branch is pushed to
origin/codex/runtime-integration-readiness.

## Expected Deliverables
implementation plan;
runtime-flow audit;
integration-gap analysis;
inactive preparatory code where justified;
characterization or boundary tests where justified;
migration architecture documentation;
commit-by-commit runtime migration roadmap;
definition of the first runtime integration mission;
maintainer-style self-review;
completion report;
pushed autonomous branch.

## Target Branch

codex/runtime-integration-readiness


## Versionar a missão

Confira:

git diff --check
git diff -- missions/RUNTIME_INTEGRATION_READINESS.md
git status
