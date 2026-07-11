# MundoZ Observation Domain Model

## Purpose

Define concepts related to player-specific world representation.

The observation domain decides what information belongs
to an observer.

---

# Observer

Represents the entity receiving a world representation.

The observer is not limited to a normal player.

Examples:

- player;
- spectator;
- administrator;
- future observation systems.

---

# Observation Context

Represents the relationship between an observer
and world information.

Contains concepts such as:

- observer position;
- target position;
- environmental information;
- calculated observation facts.

The context provides information.

It does not decide observation.

---

# Observation Decision

Represents whether information belongs
to the observer representation.

Possible results:

- OBSERVED
- NOT_OBSERVED

Observation describes perception.

It is not a permission system.

---

# Policy

Policies apply additional rules.

Examples:

- AntiXray;
- Admin visibility;
- Gameplay restrictions.

Policies decide how observed information
should be represented.

---

# Representation

The final data sent to the observer.

The representation must:

- be valid;
- preserve world consistency;
- never modify the real world.
