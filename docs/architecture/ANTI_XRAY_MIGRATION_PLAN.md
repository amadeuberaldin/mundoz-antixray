# MundoZ AntiXray Migration Plan

## Goal

Refactor the current AntiXray implementation into a domain-driven
architecture while preserving existing behavior.

The migration must happen incrementally.

---

# Migration Principles

## Preserve behavior first

The first objective is not to add features.

The first objective is to reproduce V1 behavior inside the new architecture.

---

# Current V1 Components

## AntiXrayBlocks

Current responsibility:

- classify protected blocks;
- identify hidden resources;
- identify replacement candidates.

Future responsibility:

- domain block visibility policies.

---

## AntiXrayObfuscator

Current responsibility:

- inspect chunk sections;
- decide replacements;
- mutate temporary section copy;
- write packet data.

Future responsibility:

Split into:

- domain decision service;
- Minecraft section adapter.

---

## AntiXrayRevealer

Current responsibility:

- detect exposed blocks after mining;
- send block updates.

Future responsibility:

Split into:

- Reveal use case;
- Minecraft packet sender.

---

## AntiXrayContext

Current responsibility:

- store active chunk transmission context.

Future responsibility:

Infrastructure-only context.

---

## Mixins

Current responsibility:

- intercept Minecraft packet creation.

Future responsibility:

Minecraft adapter layer.

---

# Migration Order

1. Create domain models.
2. Extract block classification rules.
3. Extract visibility decision logic.
4. Extract replacement strategy.
5. Create Minecraft adapters.
6. Replace old implementation gradually.
7. Remove legacy classes.

---

# Validation

Each migration step must:

- compile;
- keep server behavior;
- preserve anti-xray protection;
- avoid world modification.
