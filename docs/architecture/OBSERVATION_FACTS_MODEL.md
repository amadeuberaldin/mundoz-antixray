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
known blocks, including lava and ordinary full visual walls, as occluding. The
approved categories include both iron and copper bars, and every waxed and
weathering copper-grate variant. Classification uses explicit Minecraft
families and collections where implementation classes differ. It must not
infer pass-through solely from Minecraft solidity or occlusion properties.

## Minecraft Path Collection

Minecraft path collection uses the active camera entity's current eye position
as its origin. It uses ordered voxel-grid traversal, not collision, outline,
visual, or fluid-shape clipping.

Targets are sampled at the center and at slightly inset centers of faces whose
axis places the origin strictly outside the target's closed block bounds. The
stable sample order is center, X face, Y face, then Z face. An origin on a face
plane does not select that face.

The target block is excluded from every preceding path. The origin-containing
block is included unless it is also the target.

Each path remains an independent `ObservationContext`. Application
coordination returns `OBSERVED` when any sample is observed and
`NOT_OBSERVED` only when every sample is not observed. The single-path policy
is unchanged.

Path reads do not load or generate chunks. An unavailable or invalid required
position appends `UNKNOWN` and ends that sample, preserving fail-visible
behavior.

## Deferred Integration

The collector is inactive. It does not define packet, palette, chunk
representation, reveal-event, caching, or runtime performance behavior.
