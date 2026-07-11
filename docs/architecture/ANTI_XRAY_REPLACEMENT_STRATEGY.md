# MundoZ AntiXray Replacement Strategy

## Purpose

Define how hidden blocks are represented to players.

The replacement must preserve visual consistency.

---

# Principles

## Preserve terrain appearance

The replacement should match the surrounding environment.

Examples:

Overworld stone layer:

Hidden:
- DIAMOND_ORE

Replacement:
- STONE


Nether terrain:

Hidden:
- ANCIENT_DEBRIS

Replacement:
- NETHERRACK

---

## Avoid artificial patterns

The system must avoid creating visible differences
between natural terrain and hidden representations.

---

# Future Strategy

Replacement selection should consider:

- nearby blocks;
- dimension;
- terrain context.

The domain decides the replacement concept.

Minecraft adapters provide the real block data.

---

# Replacement Context

Replacement selection requires environmental information.

Examples:

- surrounding blocks;
- dimension;
- terrain composition.

This information is provided by the Minecraft adapter.

The domain decides the strategy,
but does not access Minecraft world data directly.

---

# Fallback Behavior

If a contextual replacement cannot be safely selected:

The system must prefer a valid visible representation.

Invalid or inconsistent client data must never be generated.
