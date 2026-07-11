# ADR 0002 - Selective AntiXray Protection

## Status

Accepted

## Context

Earlier versions attempted to hide structure-related blocks,
geode blocks, and natural environment blocks.

This included:

- structure blocks;
- amethyst blocks;
- calcite;
- smooth basalt.

However, these blocks are part of normal exploration
and player construction.

Hiding them created incorrect client representations
and reduced gameplay quality.

---

## Decision

AntiXray must focus only on information that creates
an unfair advantage.

The protected targets are:

- ores;
- ancient debris;
- underground lava visibility.

Natural environment blocks and building blocks must remain visible.

---

## Consequences

Positive:

- normal exploration remains natural;
- player constructions remain correct;
- fewer visual inconsistencies.

Negative:

- some structure and environment information remains visible.

## Related Rules

- Player representation only.
- Preserve normal survival gameplay.

---

## Protected Categories

The AntiXray system protects categories of information:

### Resources

Examples:

- ores;
- ancient debris.

Reason:

They directly affect progression and economy.

---

### Underground visibility

Example:

- lava.

Reason:

It can reveal large underground areas and reduce exploration risk.

---

## Design Principle

False positives are worse than false negatives.

Showing a block that could be hidden is preferable to
breaking normal gameplay visuals.
