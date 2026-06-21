# MundoZ AntiXray

MundoZ AntiXray is a server-side Fabric mod that hides non-exposed ores from players by obfuscating chunk data before it is sent to the client.

The goal is to make x-ray resource hunting ineffective while keeping normal survival gameplay functional.

## Features

- Server-side chunk obfuscation
- Hidden ore replacement before chunk packets reach the client
- Exposed ore detection
- Block reveal updates after mining
- Ancient Debris protection
- Diamond and ore protection
- No client-side installation required
- Performance benefits while flying long distances with Elytra

## How it works

When the server sends chunk data to a player, MundoZ AntiXray intercepts the chunk section write process and replaces protected hidden blocks with safe-looking blocks such as stone, deepslate, netherrack, end stone, or tuff.

The original world is not modified.

Only the data sent to the client is changed.

If an ore is exposed to air, fluids, or transparent blocks, it is sent normally. If it is fully hidden inside solid terrain, it is obfuscated.

## Protected blocks

The mod protects ores and underground resource-related blocks, including:

- Coal Ore
- Copper Ore
- Iron Ore
- Gold Ore
- Redstone Ore
- Emerald Ore
- Lapis Ore
- Diamond Ore
- Nether Gold Ore
- Nether Quartz Ore
- Ancient Debris
- Amethyst-related blocks
- Lava

## Reveal system

When a player breaks a block, the mod checks a small area around the broken block.

If a protected block becomes exposed, the server sends a block update packet to reveal the real block to that player.

Current reveal radius: **4 blocks**.

This keeps mining functional while avoiding unnecessary large updates.

## Anti-Xray behavior

The most important protection is against resources that are completely hidden inside terrain.

For example:

- Diamonds hidden behind stone are not visible to x-ray users.
- Ancient Debris is extremely protected because it is usually fully surrounded by netherrack.
- Ancient Debris touching lava is still hard to exploit because lava blocks vision from above.

## Performance side effect

During testing on MundoZ, this approach significantly reduced client-side lag while flying long distances with Elytra.

By replacing large amounts of hidden underground resource information before transmission, the client has less underground detail to process while rapidly loading chunks.

This was not the original purpose of the mod, but it became a useful side benefit.

## Structure obfuscation limitation

Earlier versions attempted to hide more structure-like blocks.

This caused visual issues when players placed certain building blocks. For example, placed copper blocks could temporarily appear as stone to the player.

Because of that, most structure-like blocks were removed from the hidden list.

The current structure-like list is intentionally minimal to avoid breaking normal building gameplay.

## Known limitations

Anti-xray systems based on chunk obfuscation must balance protection, visual correctness, and performance.

Known limitations:

- Some exposed resources must remain visible for normal gameplay.
- Fluids and transparent blocks can expose nearby ores.
- Too many structure-like blocks in the hidden list can cause visual bugs.
- The reveal system must stay small enough to avoid performance problems.
- This mod is designed specifically for the MundoZ server environment.

## Technical details

- Minecraft version: 26.2
- Fabric Loader: 0.19.3+
- Fabric API: 0.149.2+26.2
- Java: 25
- Environment: Server Only
- Uses Mixins

### Main Mixins

- PlayerChunkSenderMixin
- ClientboundLevelChunkPacketDataMixin

## Design Philosophy

MundoZ AntiXray was designed to make underground resource scanning ineffective without modifying the actual world.

The server always keeps the real blocks intact.

Only the information sent to players is modified.

The project prioritizes:

- Fair resource gathering
- Low performance overhead
- Compatibility with normal survival gameplay
- Minimal visual side effects

## Project Status

This mod is part of the MundoZ server ecosystem and is currently developed for private server use.

Future improvements may include:

- Configurable protected block list
- Configurable reveal radius
- Better dimension-specific replacement blocks
- Improved handling for player-placed blocks
- Debug/admin commands
- More detailed performance testing

## License

All Rights Reserved.
