Copper Grate Runtime Observation Fix
Mission

Investigate and correct the confirmed runtime observation defect affecting the complete Minecraft copper grate family.

This mission is intentionally narrow.

Do not redesign observation semantics.

Do not modify unrelated pass-through behavior.

Do not activate v2 authority.

The existing v1 runtime remains authoritative.

Confirmed runtime evidence

Manual controlled tests were performed in the End using isolated diamond ore candidates.

The following observation behavior was confirmed:

air -> OBSERVED
glass -> OBSERVED
water -> OBSERVED
lever -> OBSERVED
brewing stand -> OBSERVED
solid-block occlusion -> NOT_OBSERVED
lava -> NOT_OBSERVED

These results match the approved observation semantics.

Two runtime failures were reproduced:

copper grate -> NOT_OBSERVED
waterlogged copper grate -> NOT_OBSERVED

Both should be OBSERVED.

For both failures:

unavailable = 0
missingReplacement = 0
failure = 0

Therefore the result was a real NOT_OBSERVED decision rather than UNKNOWN, unavailable-world handling, missing replacement, or exception isolation.

Important copper grate family requirement

Do not treat copper grate as a single block variant.

Investigate the complete Minecraft 26.2 copper grate family.

The relevant dimensions include:

oxidation level;
waxed versus unwaxed;
dry versus waterlogged.

At minimum, investigate the Minecraft 26.2 equivalents of:

copper grate;
exposed copper grate;
weathered copper grate;
oxidized copper grate;
waxed copper grate;
waxed exposed copper grate;
waxed weathered copper grate;
waxed oxidized copper grate.

Every applicable variant must also be considered in both:

dry state;
waterlogged state.

Do not assume these exact Minecraft constant names or implementation relationships.

Inspect the actual Minecraft 26.2 mappings and runtime block/state representation first.

Required semantics

Every copper grate variant that preserves the grate's see-through geometry is PASS_THROUGH for AntiXray observation.

Oxidation must not make the grate OCCLUDING.

Waxing must not make the grate OCCLUDING.

Waterlogging must not make the grate OCCLUDING.

Water itself remains PASS_THROUGH.

The copper grate family must therefore remain observable through all supported oxidation/wax/waterlogging combinations.

Investigation requirement

Before implementing a correction, reproduce the confirmed defect with an automated test.

Determine which layer actually causes the incorrect NOT_OBSERVED result.

Investigate as necessary:

MinecraftObservationPathClassifier;
Minecraft block identity/state representation;
copper grate family matching;
waterlogged state handling;
MinecraftObservationPathCollector;
BlockGetter.traverseBlocks integration;
target sampling;
runtime observation coordination.

Do not assume the classifier is necessarily the defect merely because the runtime symptom involves a pass-through block.

Follow the actual execution path until the cause is demonstrated.

Regression matrix

Automated coverage must include the complete applicable copper grate family across:

all oxidation levels;
waxed and unwaxed variants;
dry states;
waterlogged states.

For every applicable combination, verify that the copper grate contributes PASS_THROUGH behavior.

Where practical, include integration coverage proving that:

observer -> copper grate -> protected target

evaluates OBSERVED when no other occluding block exists in the observation path.

The tests must be capable of failing against the defective behavior before the correction.

Control cases

Preserve existing behavior for representative controls.

PASS_THROUGH controls:

air;
glass;
water;
lever;
brewing stand.

OCCLUDING controls:

ordinary full solid block;
lava.

Do not broaden or alter these semantics as part of this mission.

Safety boundaries

Do not change:

v1 runtime authority;
AntiXrayObfuscator behavior unless investigation proves a correction is strictly required there and human approval is obtained first;
shadow activation semantics;
validation activation semantics;
diagnostic lifecycle;
candidate bounds;
missing-replacement semantics;
UNKNOWN/failure isolation;
replacement semantics;
reveal semantics;
protection semantics;
mixins;
packet structure;
palette behavior;
serialization.

Do not make v2 authoritative.

Do not add configuration.

Do not add commands.

Do not add telemetry.

Do not add caching.

Do not add asynchronous execution.

Do not solve the defect by special-casing only the base copper grate variant.

Prefer a Minecraft-aware family classification that accurately represents the complete copper grate family without accidentally matching unrelated copper blocks.

Compatibility requirement

The correction must remain compatible with Minecraft 26.2 and the project's existing architecture.

Minecraft-specific knowledge belongs in the infrastructure/Minecraft boundary rather than the domain model when possible.

The domain observation policy must remain independent of Minecraft block identities.

Documentation

Document:

root cause;
affected copper grate variants;
chosen family-classification strategy;
waterlogged behavior;
regression boundary;
any Minecraft-version-sensitive assumptions.

Update the Minecraft version upgrade documentation if the solution introduces a mapping or block-family assumption that should be revalidated during future Minecraft upgrades.

Validation

Run the validation required by AGENTS.md.

At minimum run:

git diff --check

./gradlew test

./gradlew shadowEnabledTest

./gradlew shadowValidationEnabledTest

./gradlew shadowValidationExplicitlyDisabledTest

./gradlew shadowValidationOnlyEnabledTest

./gradlew clean build

If AGENTS.md requires additional validation, run it as well.

Commit discipline

Follow:

One concept -> one commit -> one responsibility.

Do not squash, amend, rebase, or rewrite existing history.

Keep investigation/reproduction, correction, regression coverage, and documentation in logically reviewable commits where appropriate.

Stop conditions

Stop for human review before implementing if investigation shows that fixing this defect requires changing:

approved observation semantics;
BlockGetter.traverseBlocks strategy;
target sampling semantics;
v1 behavior;
runtime authority;
packet mutation;
serialization;
mixins;
replacement semantics;
reveal semantics;
protection semantics.

Also stop if the complete copper grate family cannot be classified reliably without introducing a broader architectural decision.

Final review

Before completion, perform an aggregate maintainer-style review of the complete mission branch.

Report findings as:

BLOCKING
IMPORTANT
MINOR

Do not silently fix unrelated findings.

Deliverable

Push only the assigned mission branch.

Provide a final report containing:

root cause;
files changed;
copper grate variants covered;
tests added;
behavior before correction;
behavior after correction;
validation results;
aggregate review findings;
commit list;
final branch SHA.

Do not merge.
