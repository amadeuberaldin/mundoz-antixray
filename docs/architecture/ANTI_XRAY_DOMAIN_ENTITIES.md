# MundoZ AntiXray Domain Entities

## Block Identity

Represents the identity of a block relevant to AntiXray rules.

The domain does not know Minecraft BlockState.

Examples:

- DIAMOND_ORE
- ANCIENT_DEBRIS
- GOLD_ORE

---

## Protected Block

Represents a block identity that requires visibility evaluation.

A protected block may be:

- hidden;
- visible;
- replaced temporarily.

Protection does not mean automatic hiding.

---

## Visibility Decision

Represents the result of evaluating visibility.

Possible decisions:

- VISIBLE
- HIDDEN
- REPLACE

---

## Replacement Block

Represents the safe block representation used when information is hidden.

Examples:

- STONE
- DEEPSLATE
- NETHERRACK

---

## Exposure Context

Represents the information required to decide visibility.

Contains concepts such as:

- block position;
- nearby blocks;
- player observation context.

The domain does not know how this information is collected.

---

## Block Position

Represents the location of a block in the world.

The domain does not know Minecraft BlockPos.

It only knows spatial information required by visibility rules.

---

## Visibility Rule

Represents a rule used to decide whether a protected block
should be visible.

Examples:

- exposed to air;
- exposed to fluids;
- close to player;
- hidden inside terrain.
