# AntiXray v1 Representation Efficiency Characterization

## Scope and Method

This investigation characterizes the active v1 representation boundary on
Minecraft 26.2. It does not change that boundary. `PlayerChunkSenderMixin`
provides the player, level, and authoritative chunk context;
`ClientboundLevelChunkPacketDataMixin` redirects the section write to
`AntiXrayObfuscator.writeSection`. V1 checks for protected states, selects the
first accepted replacement in stone, deepslate, netherrack, end stone, and tuff
priority order (then its legacy solid fallback), copies the participating
`LevelChunkSection`, replaces hidden candidates only in the copy, recalculates
the copy counts, and writes either that copy or the original with Minecraft
`LevelChunkSection.write`. The authoritative section is never mutated.

The permanent characterization fixture invokes that real v1 entry point. It
serializes the original with the same Minecraft method, decodes the v1 bytes
back into an isolated section, and compares all 4096 block cells. Distinct
states are counted from cell contents. Palette storage is reported through
Minecraft 26.2 `PalettedContainer.bitsPerEntry()`. Serialized byte counts are
actual `FriendlyByteBuf` writer output, not estimates.

Compression uses Java `Deflater` at `DEFAULT_COMPRESSION` over each isolated
section byte sequence. This is a controlled zlib experiment. It is not a
Minecraft packet measurement and does not include packet framing, adjacent
sections, protocol compression thresholds, or network transport.

All protected candidates in the changed fixtures were placed in a solid
environment, so they are hidden under v1 adjacency rules. The dense fixture is
explicitly synthetic and represents an upper-bound case, not a production
distribution.

## Minecraft 26.2 Measurements

| Fixture | Protected / replaced / unchanged | Actual states before -> after | Palette bits before -> after | Replacement | Serialized bytes before -> after | zlib bytes before -> after |
|---|---:|---:|---:|---|---:|---:|
| Homogeneous stone control | 0 / 0 / 0 | 1 -> 1 | 0 -> 0 | none | 8 -> 8 | 14 -> 14 |
| Sparse Overworld (8 diamond ore) | 8 / 8 / 0 | 2 -> 1 | 4 -> 4 | stone | 2059 -> 2059 | 37 -> 31 |
| Multiple Overworld ores | 48 / 48 / 0 | 7 -> 1 | 4 -> 4 | stone | 2070 -> 2070 | 52 -> 43 |
| Dense synthetic resources | 2048 / 2048 / 0 | 4 -> 1 | 4 -> 4 | stone | 2064 -> 2064 | 50 -> 36 |
| Mixed Overworld terrain | 128 / 128 / 0 | 12 -> 6 | 4 -> 4 | stone | 2079 -> 2079 | 78 -> 69 |
| Nether resources | 96 / 96 / 0 | 4 -> 1 | 4 -> 4 | netherrack | 2065 -> 2065 | 50 -> 38 |
| End-stone control | 0 / 0 / 0 | 1 -> 1 | 0 -> 0 | none | 9 -> 9 | 15 -> 15 |

The isolated compressed sizes fell by 16.2% for sparse Overworld, 17.3% for
multiple ores, 28.0% for dense synthetic resources, 11.5% for mixed terrain,
and 24.0% for Nether. Controls were unchanged. Ratios are specific to these
small isolated inputs and zlib settings.

A separate threshold fixture begins with stone plus 16 distinct protected
states. V1 reduces actual cell diversity from 17 states to one, but the copied
container remains at five bits per entry and the original and transformed
serialized sizes remain equal. Thus v1 does not compact its copied palette or
cross to a smaller serialized palette representation merely because replacing
blocks removes all uses of palette entries. The four-bit fixtures behave the
same way.

## Measured Facts

- V1 replaces the expected hidden candidates in an isolated temporary copy and
  preserves every non-candidate cell and every authoritative-section cell.
- Replacement substantially reduces actual block-state diversity in all
  changed fixtures.
- The active copy-and-mutate process retains the original palette bit width.
- No changed fixture reduced uncompressed `LevelChunkSection.write` byte size.
- Controlled zlib output was smaller for every changed fixture and identical
  for both controls. More repetitive palette-index patterns therefore improved
  compressibility in this experiment even without reducing palette width.
- The selected state followed active v1 priority: stone for the Overworld and
  mixed fixtures, netherrack for the Nether fixture, and no replacement for
  controls.

## Supported and Rejected Hypotheses

Supported by these fixtures:

- v1 reduces the diversity of actual block information represented to the
  client;
- replacing protected states with dominant terrain creates more repetitive
  serialized data;
- that repetition can improve controlled compression even when raw section
  size is unchanged.

Rejected for the active Minecraft 26.2 copy-and-mutate path represented here:

- fewer distinct cell states necessarily reduce uncompressed section bytes;
- v1 automatically compacts the copied palette after replacement;
- crossing an actual-state cardinality threshold necessarily lowers the copied
  container bit width or serialized size.

Not established:

- actual Minecraft packet or network bandwidth savings;
- whether protocol compression produces the same percentages when sections are
  combined with real packet data;
- whether compressibility, information diversity, rendering work, memory use,
  or another client effect caused the production improvement;
- client decode, chunk rebuild, rendering, allocation, FPS, or frame-time cost;
- representative production ore distributions;
- comparative v1/v2 efficiency, because their visibility semantics differ.

## Runtime-Cost Boundary

This mission did not establish a formal runtime benchmark. The deterministic
fixture executes the real scan, replacement selection, section copy, state
replacement, count recalculation, and serialization path, but JUnit wall time
cannot isolate those stages reliably. JVM warmup, JIT compilation, GC, Mockito
fixtures, host load, and Minecraft bootstrap dominate or perturb small timing
samples. Any ad hoc nanosecond result would be diagnostic only and is not
preserved as an architectural measurement. A future cost comparison should
use a reviewed benchmark harness with warmup, repeated forks, realistic section
distributions, and separately defined stage boundaries.

## Future v2 Recommendation

When v2 eventually becomes authoritative, it should preserve representation
validity first and observation correctness should determine which facts may be
hidden. Within those semantics, representation construction should:

- prefer an already-valid dominant terrain representation for hidden targets;
- measure actual state diversity, palette width, exact serialized bytes, and
  protocol-boundary compression separately;
- avoid assuming that fewer logical states compact a copied Minecraft palette;
- compare representative Overworld, Nether, mixed, dense-bound, and control
  fixtures against this v1 baseline.

This is a preservation requirement for beneficial representation properties,
not a requirement that v2 hide the same blocks as v1. No optimization or v2
authority change is approved by these findings. Client profiling remains
necessary to explain the reported smoother rapid chunk loading.
