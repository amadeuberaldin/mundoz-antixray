# ADR 0004 - Player Observation Model

## Context

Minecraft clients currently receive world information
that may not be observable by the player.

This creates unnecessary processing and allows information
extraction through unauthorized clients.

## Decision

Future MundoZ systems should consider player observation
as a first-class concept.

Systems should prefer sending only information that belongs
to the player's observable world representation.

## Consequences

Positive:

- reduced client processing;
- improved anti-cheat possibilities;
- reduced unnecessary data transmission.

Negative:

- requires complex visibility calculations;
- requires careful compatibility handling.
