# ADR 0001 - Player Representation Only

## Status

Accepted

## Context

Anti-Xray needs to prevent unfair resource discovery without changing
the actual Minecraft world.

Changing the real world state would create risks:

- world corruption;
- gameplay inconsistencies;
- loss of server authority.

The server world must always remain the source of truth.

---

## Decision

Anti-Xray will only modify the representation of the world sent to players.

The real world state is never modified.

The system may change:

- chunk data sent to clients;
- block update packets sent to clients.

The system must not change:

- stored blocks;
- chunk data on disk;
- world state.

---

## Consequences

### Positive

- The server remains authoritative.
- Player actions always affect the real world.
- Anti-Xray failures cannot corrupt world data.

### Negative

- The system depends on packet interception.
- Minecraft version updates require validation of network internals.

---

## Related Principles

- Domain logic must not depend on Minecraft networking classes.
- Client representation and world state are different concepts.
