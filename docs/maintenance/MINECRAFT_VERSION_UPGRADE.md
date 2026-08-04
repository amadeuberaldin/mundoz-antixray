# Minecraft Version Upgrade Notes

## 26.1.x → 26.2

Validated components:

- LevelChunkSection.write
- LevelChunkSection.copy
- PalettedContainer
- ClientboundLevelChunkPacketData

Known risk areas:

- chunk serialization;
- palette handling;
- packet structure.

Investigation result:

The public API signatures remained compatible.

No direct mapping incompatibility was found during migration analysis.

## Plain JUnit Minecraft Bootstrap

Tests that directly reference vanilla registry objects such as `Blocks`
do not run through the normal Minecraft launcher lifecycle.

Initialize Minecraft before any static field references `Blocks.*`:

```java
SharedConstants.tryDetectVersion();
Bootstrap.bootStrap();
```

The order is required.

`SharedConstants.tryDetectVersion()` establishes the current game version.
Registry bootstrap may initialize data fixers, which require that version.
Calling `Bootstrap.bootStrap()` first can fail with:

```text
IllegalStateException: Game version not set
```

`Bootstrap.bootStrap()` then initializes the built-in registries and their
dependent vanilla classes. Referencing `Blocks` before bootstrap can fail with:

```text
IllegalArgumentException: Not bootstrapped
```

Place this setup before static mappings or fixtures that access `Blocks.*`.
The sequence is an internal Minecraft lifecycle requirement and must be
revalidated during version upgrades.
