# MundoZ Observation Facts Model

## Purpose

Observation facts represent calculated information
about the relationship between an observer and a target.

Facts are produced by infrastructure analysis
and consumed by domain rules.

---

# Examples

## Distance Fact

Represents spatial distance between observer and target.

---

## Line Of Sight Fact

Represents whether the observer has a valid visual path.

Possible states:

- CLEAR
- BLOCKED

---

## Occlusion Fact

Represents whether terrain blocks observation.

---

# Responsibility

Facts describe reality.

They do not decide representation.

The domain uses facts to produce ObservationDecision.

## AntiXray Path Behavior

The minimum executable AntiXray observation fact is the ordered behavior of
the blocks or materials on the analyzed path before the target:

- `PASS_THROUGH` does not terminate observation;
- `OCCLUDING` terminates observation;
- `UNKNOWN` means the path cannot be determined safely.

The target is not included in its own preceding path. This makes the first
occluder observable while preventing observation of information behind it.

The observation policy returns `OBSERVED` when every preceding fact is
`PASS_THROUGH`, and `NOT_OBSERVED` when a known preceding fact is
`OCCLUDING`. Any `UNKNOWN` fact makes the decision `OBSERVED` so uncertainty
fails visible.

Minecraft infrastructure classifies concrete block states. It explicitly
treats the approved conservative categories as pass-through and treats other
known blocks, including lava and ordinary full visual walls, as occluding.
It must not infer pass-through solely from Minecraft solidity or occlusion
properties.

## Deferred Path Analysis

This fact model does not define observer eye position, target sample position,
the algorithm used to collect blocks along the path, or exact voxel-shape
intersection. Unavailable world information must produce `UNKNOWN` rather than
an invented path fact.

Those concerns require a separately reviewed Minecraft observation-analysis
slice before runtime integration.
