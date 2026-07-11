# ADR 0005 - Player Centered Observation Model

## Context

Minecraft already uses player position as a reference
for deciding world relevance.

Examples:

- entity activation range;
- entity despawn rules;
- chunk loading;
- simulation distance;
- mob spawning.

The player is the center of a dynamic area where
world processing has higher importance.

---

## Decision

Future MundoZ observation systems should consider
the player as the center of a dynamic observation area.

The observation area is not only based on distance.

It may consider:

- distance;
- line of sight;
- terrain obstruction;
- player orientation;
- world interaction rules.

---

## Consequences

Positive:

- aligns with existing Minecraft mechanics;
- allows performance optimizations;
- provides a foundation for information filtering.

Negative:

- observation area calculation is complex;
- requires compatibility with vanilla behavior.

---

# Interaction Area vs Observation Area

Minecraft already has multiple player-centered ranges.

## Interaction Area

Used for:

- entity activation;
- simulation;
- despawn rules;
- gameplay mechanics.

## Observation Area

Represents information that can belong to the player's world view.

It may be larger or smaller than interaction distance.

The two concepts should not be considered identical.
