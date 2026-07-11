# MundoZ AntiXray Architecture

## Vision

MundoZ AntiXray prevents unfair resource discovery by controlling
the information exposed to players.

The system never modifies the real world state.

It only modifies the representation sent to clients.

---

# Core Principle

World state and player representation are different concepts.

The server always keeps the original blocks.

AntiXray only controls what a player receives.

---

# Architecture Goals

The v2 architecture aims to:

- separate Minecraft infrastructure from domain logic;
- make anti-xray rules testable without Minecraft;
- isolate Mojang mapping changes;
- make future Minecraft upgrades safer;
- keep behavior explainable through Git history.

---

# Domain Responsibilities

The domain decides:

- what blocks are hidden;
- when a block should be revealed;
- which replacement block is acceptable;
- visibility rules.

The domain does not know:

- Minecraft packets;
- Mixins;
- Fabric events;
- LevelChunk;
- FriendlyByteBuf.

---

# Infrastructure Responsibilities

The Minecraft layer handles:

- chunk interception;
- packet modification;
- player communication;
- conversion between Minecraft objects and domain objects.

---

# Invariants

## No world modification

AntiXray must never change the actual world.

## Fail safe

If obfuscation cannot be performed safely,
the original chunk data must be sent.

## Client representation only

All transformations affect only transmitted data.

---

# Evolution

Version 1 implemented the functionality directly using Minecraft classes.

Version 2 restructures the same behavior using domain-driven design principles.
