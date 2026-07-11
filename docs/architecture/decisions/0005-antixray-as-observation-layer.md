# ADR 0005 - AntiXray Evolution Into Observation Layer

## Context

AntiXray originally focused on hiding specific blocks.

Production experience showed that the real problem is
controlling world information exposed to players.

## Decision

AntiXray V2 will be designed as the first implementation
of a broader player observation model.

The system should evaluate what information belongs to
a player's world representation.

## Consequences

Positive:

- AntiXray becomes more general;
- future visibility optimization becomes possible;
- security and performance share the same foundation.

Negative:

- architecture complexity increases;
- requires careful Minecraft client compatibility.
