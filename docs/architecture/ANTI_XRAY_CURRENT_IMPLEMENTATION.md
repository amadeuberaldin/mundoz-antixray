# MundoZ AntiXray Current Implementation

## Overview

This document describes the current v1 implementation.

The purpose is to preserve knowledge before the v2 migration.

---

# Current Flow

## Chunk Transmission

Flow:

Player joins chunk
        |
PlayerChunkSender
        |
AntiXrayContext
        |
ClientboundLevelChunkPacketData
        |
AntiXrayObfuscator
        |
Modified chunk data sent to client

---

# Current Responsibilities

## AntiXrayBlocks

Responsible for:

- protected block detection;
- hidden resource classification;
- replacement validation.

---

## AntiXrayObfuscator

Responsible for:

- reading chunk sections;
- creating temporary copies;
- replacing hidden blocks;
- serializing modified sections.

---

## AntiXrayRevealer

Responsible for:

- detecting newly exposed resources;
- sending block updates.

---

## Current Limitations

Known limitations:

- structure-like blocks previously caused visual issues;
- logic is coupled with Minecraft classes;
- difficult to test without a running server;
- Minecraft version updates require manual validation.

---

# Known Production Behavior

The current implementation provides:

- xray protection;
- reduced client chunk processing;
- no world modification.
