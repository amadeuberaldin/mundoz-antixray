# MundoZ Observation Flow

The world state is authoritative.

Players never receive the world state directly.

Instead, they receive a player-specific representation.

---

World State
    |
    v
Observation Analysis
    |
    v
Observation Decision
    |
    v
Policy Evaluation
    |
    v
Representation Builder
    |
    v
Minecraft Adapter
    |
    v
Minecraft Packet
    |
    v
Client

---

# Responsibilities

## Observation

Determines which information belongs to the player's observable world.

## Policies

Apply gameplay and security rules.

Examples:

- AntiXray
- Admin View
- Spectator
- Future visibility optimizations

## Representation Builder

Builds a valid player representation.

The original world is never modified.

## Minecraft Adapter

Transforms the representation into Minecraft packet structures.
