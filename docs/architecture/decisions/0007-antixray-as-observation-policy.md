# ADR 0006 - AntiXray As Observation Policy

## Context

AntiXray started as a block filtering system.

Further analysis showed that visibility,
representation, and security are different concepts.

## Decision

AntiXray will be implemented as a policy
inside the MundoZ Observation system.

The observation domain determines what belongs
to a player's perception.

AntiXray determines whether protected information
should be represented.

The observation domain is independent from AntiXray.

AntiXray is one possible policy built on top of the observation domain.

Future systems may reuse the same observation infrastructure
without depending on AntiXray.

## Consequences

Positive:

- AntiXray becomes reusable;
- future visibility systems share infrastructure;
- domain responsibilities become clearer.
- Observation becomes a reusable platform concept.

Negative:

- architecture becomes larger;
- migration requires additional abstractions.
