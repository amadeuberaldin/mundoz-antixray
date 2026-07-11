# MundoZ AntiXray Runtime Flow

## Chunk Transmission

When a player receives chunk data:

Player enters chunk
        |
        v
Minecraft prepares chunk packet
        |
        v
PlayerChunkSender.sendChunk()
        |
        v
AntiXray context is created
        |
        v
ClientboundLevelChunkPacketData serializes sections
        |
        v
AntiXray evaluates visibility rules
        |
        v
A visibility decision is created
        |
        v
The Minecraft adapter applies the temporary representation
        |
        v
Modified packet is sent to client
        |
        v
Client receives protected representation

The original world data is never modified.

## World State vs Client Representation

The server world is authoritative.

AntiXray creates a temporary player-specific representation.

Example:

World:
Diamond Ore

Player representation:
Stone

The replacement exists only during transmission.

## Block Reveal

When a player breaks a block:

Player breaks block
        |
        v
Block break event
        |
        v
Nearby blocks are evaluated
        |
        v
Newly exposed protected blocks are identified
        |
        v
Block update packets are sent
        |
        v
Player receives updated information

# Failure behavior

If the AntiXray system cannot safely evaluate a chunk section:

The original section must be sent.

The system must prefer visibility over invalid data.
