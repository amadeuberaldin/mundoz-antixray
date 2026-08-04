# MundoZ AntiXray Reveal Slice

## Purpose

The reveal slice decides when previously hidden information may return to a
player-specific representation.

Reveal does not modify the authoritative Minecraft world.

## Domain Decision

The reveal policy consumes decisions that have already been made by the
protection and observation domains.

A target is revealed only when:

- the target is protected; and
- the target is observed.

All other combinations remain outside the reveal action.

The reveal policy does not calculate exposure, adjacency, transparency, or
line of sight. Those are observation concerns.

## Minecraft Infrastructure

The Minecraft reveal candidate scanner reads the radius-four cube centered on
a changed block position. It translates supported Minecraft block states and
positions into candidate information for later policy evaluation.

The scanner:

- includes both boundaries of the radius-four cube;
- returns immutable candidate information;
- does not decide whether a candidate is observed;
- does not modify block state;
- does not send packets.

The radius preserves the bounded search area learned from the v1 reveal
implementation. It does not preserve the v1 adjacency rule as observation
semantics.

## Current Integration Status

The v2 reveal domain and read-side Minecraft adapters are inactive.

Application orchestration, event registration, observation analysis, and
player representation updates are deferred until a runtime integration mission
has a real caller for the reveal slice.

The legacy `AntiXrayRevealer` remains the active runtime path. This slice does
not alter its behavior.
