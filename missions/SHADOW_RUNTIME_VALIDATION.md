# Shadow Runtime Validation Mission

## Mission Goal

Prepare MundoZ AntiXray v2 shadow mode for controlled runtime validation
without giving v2 authority over player-visible representation.

The guarded runtime integration already allows v2 to participate in the
real AntiXray runtime while v1 remains the sole authority.

This mission must add only the minimum bounded observability necessary to
determine whether the v2 shadow path behaves correctly and economically
enough in real server execution to justify a future controlled-authority
mission.

This mission does NOT authorize v2 to change player-visible output.

## Branch

Work autonomously only on:

codex/shadow-runtime-validation

The branch must be created from the current:

v2-domain-architecture

Do not work autonomously on the integration branch.

## Required Reading

Before planning or modifying code, read:

- AGENTS.md;
- README.md;
- all files under docs/architecture/;
- all accepted ADRs;
- all files under docs/maintenance/;
- missions/GUARDED_RUNTIME_INTEGRATION.md;
- the complete current Java source tree;
- relevant tests;
- Git history from v1.0.0 to HEAD.

Pay particular attention to:

- AntiXrayObfuscator;
- AntiXrayShadowRuntime;
- RuntimeDecisionComparison;
- ShadowEvaluationService;
- MinecraftRuntimeShadowEvaluator;
- observation path collection;
- replacement integration;
- guarded runtime tests.

## Existing Runtime Contract

The following behavior is already accepted and must remain unchanged:

- v1 is the sole authority for player-visible representation;
- v2 shadow mode is disabled by default;
- shadow activation is a startup-only immutable JVM decision;
- failure to read the activation property resolves to disabled;
- disabled mode bypasses shadow participation;
- enabled shadow mode evaluates at most one v2-supported protected
  candidate per qualifying section;
- unsupported v1-only candidates do not consume that budget;
- shadow failures cannot affect v1;
- shadow results do not modify authoritative world state;
- shadow results do not modify packet or palette serialization;
- v2 observation, protection, and replacement semantics remain unchanged.

Do not weaken any of these guarantees.

## Questions This Mission Must Enable Us To Answer

After this mission, a human maintainer should be able to run a controlled
server with shadow mode enabled and determine:

1. Is v2 shadow evaluation actually being exercised?

2. How many bounded shadow evaluations occur?

3. How often do v1 and v2 agree or disagree for the evaluated candidate?

4. When they disagree, what high-level decision categories differ?

5. Are shadow evaluations failing, becoming UNKNOWN, or encountering
   unavailable information?

6. Is the bounded shadow path introducing meaningful runtime cost?

The implementation must collect only enough information to answer these
questions.

## Validation Mode

Runtime validation must require explicit startup activation.

Existing shadow activation alone must NOT automatically enable diagnostic
output.

Introduce a separate startup-only JVM property for validation diagnostics.

The validation property must:

- default to false;
- be read once;
- produce an immutable process-lifetime decision;
- fail safely to disabled if property access fails;
- not be mutable at runtime.

Shadow may therefore be enabled while validation diagnostics remain
disabled.

Do not introduce:

- configuration files;
- commands;
- mutable runtime switches;
- reload behavior.

The exact property name must be centralized in one clearly named constant.

## Disabled Validation Cost

When validation diagnostics are disabled:

- no diagnostic event object may be created;
- no diagnostic string may be formatted;
- no diagnostic collection may be allocated;
- no diagnostic counter update may occur;
- no diagnostic logging may occur;
- no additional observation-path collection may occur because of
  diagnostics.

Existing shadow behavior must remain equivalent to the currently accepted
guarded runtime implementation.

## Bounded Observability

Diagnostics must observe only shadow evaluations that already occur under
the existing one-supported-candidate-per-section bound.

Diagnostics must NOT:

- cause another candidate to be evaluated;
- cause another observation sample to be collected;
- repeat an evaluation;
- scan additional blocks;
- load or generate chunks;
- retain Minecraft world objects;
- retain player objects;
- retain chunk or section objects.

Validation observes existing shadow work.

It must not create additional AntiXray work.

## Information Allowed

Prefer aggregated counters over per-block or per-player records.

The minimum useful aggregate categories may include:

- shadow evaluation attempted;
- v1/v2 agreement;
- v1/v2 disagreement;
- v2 OBSERVED;
- v2 NOT_OBSERVED;
- v2 UNKNOWN/fail-visible outcome;
- unsupported candidate;
- unavailable information;
- shadow evaluation failure.

The agent must inspect the existing decision models before deciding the
exact categories.

Do not duplicate concepts already represented by existing domain or
application models.

## Privacy And Data Retention

Diagnostics must not record:

- player names;
- player UUIDs;
- IP addresses;
- chat;
- inventory contents;
- exact player coordinates;
- exact protected-block coordinates;
- chunk coordinates;
- world seed;
- arbitrary block dumps.

Do not create persistent databases or structured telemetry storage.

Do not send diagnostic information over the network.

Do not introduce external telemetry services.

## Reporting

The preferred first implementation is a bounded aggregate runtime summary,
not per-evaluation logging.

Investigate the smallest safe mechanism for exposing the aggregate
validation result to the server operator.

Acceptable architectural directions to investigate include a concise
process-lifetime aggregate summary written through the existing server
logging infrastructure at a bounded frequency or lifecycle boundary.

Do not choose the final reporting frequency or lifecycle trigger silently
if the repository does not already establish an appropriate convention.

If choosing between periodic reporting, shutdown reporting, or another
lifecycle boundary requires a runtime/product decision, stop for human
review during planning.

Never log one line per block evaluation.

Never log one line per observation sample.

## Performance Validation

This mission must investigate how to measure shadow overhead without
introducing a profiling framework.

Prefer simple bounded measurements that can answer whether shadow
evaluation is materially expensive.

Do not add:

- background profiling threads;
- asynchronous pipelines;
- sampling agents;
- external profiling libraries;
- persistent timing histories.

If timing is proposed, use monotonic elapsed-time measurement and aggregate
it without retaining individual samples.

Timing diagnostics must themselves remain optional and bounded.

The agent must identify measurement distortion risks in the mission plan.

## Comparison Semantics

Do not invent new v1 or v2 semantics for the sake of diagnostics.

Diagnostics may classify existing outcomes.

Diagnostics must not change:

- protection decisions;
- observation decisions;
- replacement decisions;
- reveal decisions;
- v1 hide/reveal behavior.

If RuntimeDecisionComparison does not currently contain enough information
to classify a useful disagreement safely, report that limitation instead
of expanding semantics silently.

## Failure Isolation

Diagnostic failure must never affect:

- v1;
- v2 shadow evaluation;
- section processing;
- packet serialization;
- player-visible output.

Any diagnostic exception must be contained.

A failure in diagnostics must behave as though validation diagnostics were
disabled for that operation.

Do not suppress or reinterpret existing v1 behavior.

## Runtime Authority

This mission explicitly does NOT authorize:

- using a v2 decision as the actual replacement decision;
- modifying a section because of v2;
- changing serialized output because of v2;
- replacing v1 protection logic;
- replacing v1 reveal logic;
- removing legacy code;
- changing packet or palette behavior.

V1 remains authoritative after this mission.

## Tests

Tests must prove observable guarantees, not merely implementation details.

At minimum, investigate and plan tests proving:

- validation property absent -> diagnostics disabled;
- validation property false -> diagnostics disabled;
- validation property true -> diagnostics enabled;
- production validation activation is immutable after initialization;
- property-read failure -> diagnostics safely disabled;
- diagnostics disabled -> zero diagnostic participation;
- diagnostics do not cause additional shadow candidate evaluations;
- diagnostics do not cause additional observation-path collection;
- one-supported-candidate-per-section shadow bound remains intact;
- aggregate agreement accounting is correct;
- aggregate disagreement accounting is correct;
- failure/UNKNOWN accounting is correct where existing models expose it;
- diagnostic exceptions cannot affect shadow or v1;
- no diagnostic state influences serialized output;
- authoritative world/section state remains unchanged;
- existing guarded-runtime tests continue to pass.

If enabled startup behavior requires a forked JVM test, follow the existing
guarded-runtime testing pattern rather than introducing mutable production
activation.

## Architecture Discipline

Before introducing any new:

- event;
- counter;
- summary;
- collector;
- reporter;
- service;
- adapter;
- result model;

search the existing source tree for overlapping responsibility.

Prefer the smallest implementation capable of validating the existing
shadow runtime.

Do not build a general-purpose telemetry framework.

Do not build an analytics subsystem.

Do not generalize this work for future unrelated features.

## Documentation

Document:

- validation activation;
- collected aggregate information;
- information deliberately not collected;
- reporting boundary;
- failure isolation;
- expected runtime overhead;
- how a maintainer enables validation;
- how a maintainer interprets the resulting summary;
- limitations of the measurements.

Documentation must clearly distinguish:

- shadow execution;
- shadow validation diagnostics;
- future v2 authority.

## Human Approval Boundaries

Stop for human review before:

- selecting a reporting lifecycle/frequency if no existing convention
  determines it;
- introducing new comparison semantics;
- recording exact coordinates or player identity;
- introducing persistent storage;
- introducing networking;
- adding asynchronous execution;
- changing the existing one-candidate-per-section bound;
- changing shadow activation semantics;
- changing v1 or v2 gameplay behavior;
- modifying packet/palette serialization;
- giving v2 any runtime authority;
- expanding the mission into general telemetry infrastructure.

## Mission Planning

Before editing code:

1. inspect the current implementation;
2. identify what RuntimeDecisionComparison already exposes;
3. identify where a diagnostic observation can occur without causing
   additional shadow work;
4. identify the smallest aggregate state required;
5. determine how disabled diagnostics can have effectively zero hot-path
   participation;
6. investigate an appropriate bounded reporting boundary;
7. identify timing-measurement distortion risks;
8. identify exact files/packages likely to change;
9. propose the smallest vertical slices;
10. estimate the number of commits;
11. identify risks and human decision points.

Do not edit files during planning.

Present the implementation plan and stop for human approval.

## Commit Discipline

After the plan is approved, execute autonomously on the dedicated
codex/* branch.

Follow:

One concept -> one commit -> one responsibility.

Before every commit:

1. run git diff --check;
2. run focused relevant tests;
3. run ./gradlew test;
4. run any startup-enabled/forked-JVM validation tasks affected by the
   change;
5. run ./gradlew clean build;
6. verify changed-file scope.

Do not amend, squash, rebase, or rewrite history.

## Mission Completion

At completion:

- perform an aggregate maintainer-style review;
- compare delivered commits with the accepted plan;
- report deviations;
- report exact files changed;
- report tests/builds executed;
- report known performance limitations;
- report measurement limitations;
- report unresolved questions;
- report the recommended controlled-server validation procedure;
- push only codex/shadow-runtime-validation;
- do not merge.
