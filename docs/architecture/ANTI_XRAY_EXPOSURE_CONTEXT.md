# MundoZ AntiXray Exposure Context

## Purpose

Represents the information required to evaluate
whether a protected block is actually visible to a player.

The domain does not access Minecraft world data directly.

---

# Contains

Exposure context may contain:

- block position;
- nearby block information;
- fluid exposure;
- transparency information;
- dimension context.

---

# Responsibility

The context provides information.

It does not decide visibility.

The domain rules decide the final VisibilityDecision.

---

# Dependency Rule

The context must not depend on:

- Minecraft BlockState;
- BlockPos;
- LevelChunk;
- ServerLevel.

Minecraft adapters create the domain context.

---

# Exposure vs Visibility

A block touching air is not necessarily visible.

A block touching lava is not necessarily visible.

Visibility requires considering whether the player has
a valid observation path to the block.

---

# Contains

Exposure context may contain:

- block position;
- nearby block information;
- transparent block information;
- fluid information;
- player observation position;
- line of sight information;
- dimension context.

---

# Visibility Examples

## Hidden

Player

Stone

Air

Diamond


The diamond touches air but cannot be observed.

---

## Visible


Player

Glass

Water

Diamond


The player has a valid visual path.

---

## Lava


Player

Lava

Diamond


Lava contact alone does not make the block visible.

---

# Exposure Is Not Visibility

Exposure describes the physical relationship between blocks.

Visibility describes what the player can actually observe.

Examples:

A diamond touching lava:

- exposed: yes
- visible: not necessarily

A diamond behind glass:

- exposed: yes
- visible: yes

A diamond touching air behind a wall:

- exposed: yes
- visible: no
