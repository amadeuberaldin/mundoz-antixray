# AntiXray v1 Protocol Compression Characterization

## Result

**B — Small protocol reduction.** V1 produced deterministic savings at the
reproduced Minecraft 26.2 compression and framing boundary, but the absolute
single-section savings were 4 to 14 bytes in the controlled packet context.
That evidence is insufficient to explain the reported client-side improvement.

## Minecraft 26.2 Path Inspected

The server constructs `ClientboundLevelChunkWithLightPacket`. Its stream codec
writes chunk X/Z, `ClientboundLevelChunkPacketData`, and
`ClientboundLightUpdatePacketData`. Chunk data writes heightmaps, a VarInt
length followed by the concatenated `LevelChunkSection.write` bytes, and block
entities. Light data writes four bit sets followed by sky/block update lists.

The outbound `PacketEncoder` writes the packet ID and complete packet codec
payload into one `ByteBuf`. `Connection.setupCompression` installs
`CompressionEncoder` after the length prepender in pipeline order, which means
outbound data is compressed before framing. `CompressionEncoder` examines the
entire encoded packet payload. Below its configured threshold it writes VarInt
zero plus the unchanged payload. At or above the threshold it writes the
uncompressed payload length and compresses the full payload with a reusable
`java.util.zip.Deflater` using default zlib settings. Input is capped at 8 MiB.
`Varint21LengthFieldPrepender` then prefixes the compression-unit length with a
one-to-three-byte VarInt. One compressed unit therefore corresponds to one
complete encoded packet, not an individual section.

## Reproduced Boundary

The test uses real Minecraft 26.2 `LevelChunkSection.write`, the active
`AntiXrayObfuscator.writeSection`, `CompressionEncoder`, and
`Varint21LengthFieldPrepender`. It reproduces the chunk-with-light field layout
with deterministic coordinates, empty heightmaps, no block entities, and empty
light masks/update lists. A fixed one-byte play packet-ID slot precedes that
body. Thus the final measurement includes packet payload, Minecraft compression
marker/compressed bytes, and Minecraft length framing. It does not include TCP,
encryption, a production registry/world packet constructor, nonempty heightmaps,
block entities, or lighting arrays. The 256-byte threshold is an explicit test
configuration; production configuration is not changed.

## Single-Section Measurements

| Fixture | Raw section original -> v1 | Packet payload original -> v1 | Eligible | Compression unit original -> v1 | Frame bytes | Final unit original -> v1 | Difference |
|---|---:|---:|---|---:|---:|---:|---:|
| Stone control | 8 -> 8 | 26 -> 26 | no | 27 -> 27 | 1 -> 1 | 28 -> 28 | 0 (0%) |
| Sparse Overworld | 2059 -> 2059 | 2078 -> 2078 | yes | 42 -> 38 | 1 -> 1 | 43 -> 39 | 4 (9.30%) |
| Multiple Overworld ores | 2070 -> 2070 | 2089 -> 2089 | yes | 59 -> 49 | 1 -> 1 | 60 -> 50 | 10 (16.67%) |
| Dense synthetic | 2064 -> 2064 | 2083 -> 2083 | yes | 57 -> 43 | 1 -> 1 | 58 -> 44 | 14 (24.14%) |
| Mixed terrain | 2079 -> 2079 | 2098 -> 2098 | yes | 85 -> 76 | 1 -> 1 | 86 -> 77 | 9 (10.47%) |
| Nether | 2065 -> 2065 | 2084 -> 2084 | yes | 56 -> 44 | 1 -> 1 | 57 -> 45 | 12 (21.05%) |
| End control | 9 -> 9 | 27 -> 27 | no | 28 -> 28 | 1 -> 1 | 29 -> 29 | 0 (0%) |

Raw section and packet payload sizes remain equal because V1 retains palette
width. Savings appear only after Minecraft protocol compression. Controls are
below threshold and unchanged.

## Threshold Behavior

With threshold 256, complete payloads of 255, 256, and 257 bytes demonstrate
the exact inclusive check. At 255 bytes Minecraft emits a zero marker and 255
raw bytes, producing a 256-byte compression unit plus a two-byte frame prefix.
At 256 and 257 bytes it emits the nonzero uncompressed-length marker and zlib
data. Eligibility depends on complete packet payload size, not section size.
None of the paired V1 fixtures changed raw payload size, so V1 did not move a
packet across the threshold in this investigation.

## Multi-Section Measurements

Repeated sparse Overworld sections produced:

| Sections | Payload bytes | Final original -> v1 | Difference |
|---:|---:|---:|---:|
| 1 | 2078 | 43 -> 39 | 4 (9.30%) |
| 4 | 8255 | 66 -> 60 | 6 (9.09%) |
| 16 | 32964 | 118 -> 111 | 7 (5.93%) |
| 24 | 49436 | 151 -> 145 | 6 (3.97%) |

A 16-section packet with fifteen uniform control sections and one sparse ore
section measured 2198 payload bytes and 48 -> 44 final bytes, a 4-byte (8.33%)
reduction. Repeating identical sections makes both forms highly compressible;
the absolute gain did not scale linearly and the percentage declined.

## Facts, Inference, and Uncertainty

Measured facts:

- V1 changed no raw section or complete payload byte count in these fixtures.
- Every changed Overworld, mixed, dense, and Nether fixture became smaller
  through Minecraft 26.2 `CompressionEncoder`; controls did not.
- Minecraft compression preserves the isolated compressibility direction in a
  deterministic chunk-packet context.
- Multi-section repetition does not imply proportional savings.

Supported inference: replacing hidden states with dominant terrain increases
repeated byte patterns that Minecraft zlib compression can exploit. Rejected:
raw-size reduction, palette compaction, or a threshold crossing explains these
results.

Remaining uncertainties include real heightmaps/light/block entities, actual
production section distributions, negotiated threshold values, encryption and
transport overhead, and packets constructed from a live world. Most
importantly, these server-side bytes do not measure client decode, rebuilding,
rendering, memory, FPS, or frame time. Protocol savings may contribute to the
production symptom, but this experiment cannot establish that they do.

## Future v2 Preservation Candidate

After v2 observation semantics decide that hiding is valid, representation
construction should prefer valid dominant terrain states and preserve repeated
patterns across a packet where safe. Future work must measure complete protocol
units rather than infer efficiency from state counts, and must not require V2
to reproduce V1 hidden-block decisions. No V2 optimization or authority change
is approved here.
