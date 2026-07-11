# MundoZ AntiXray Domain Model

## Purpose

The domain model describes the concepts required to decide
which world information should be hidden or revealed.

The domain is independent from Minecraft implementation details.

---

# Main Concepts

## Protected Block

A block that requires visibility evaluation.

Examples:

- Diamond Ore
- Ancient Debris
- Gold Ore

A protected block does not automatically mean hidden.

Visibility rules decide the final result.

---

## Visibility Context

Represents the situation where a block is observed.

Contains concepts such as:

- player position;
- block position;
- surrounding blocks;
- dimension rules.

The domain does not know how this information is obtained.

---

## Visibility Decision

The result of evaluating a block.

Possible outcomes:

- Visible
- Hidden
- Replace with safe block

---

## Replacement Strategy

Defines which block representation should replace a hidden block.

Examples:

Overworld:

- Stone
- Deepslate

Nether:

- Netherrack

End:

- End Stone

---

## Exposure Rule

Determines if a protected block is naturally visible.

Examples:

- adjacent air;
- transparent block;
- fluid exposure.

---

## Reveal Event

Represents a situation where previously hidden information may become visible.

Example:

A player breaks a block and exposes nearby ore.

---

# Domain Rules

## Rule 1

The real world state is never changed.

## Rule 2

A hidden block is only a representation decision.

## Rule 3

A protected block can be visible.

## Rule 4

A replacement block must be safe and believable.

## Rule 5

Failure to evaluate safely must preserve the original information.

---

# Non Domain Concepts

The following are infrastructure details:

- LevelChunk
- LevelChunkSection
- FriendlyByteBuf
- Clientbound packets
- Fabric events
- Mixins

They belong to the Minecraft adapter layer.
