# AntiXray Client Representation Cost Characterization

## 1. Question and outcome

This investigation asks whether the simpler block-state representation emitted by the active V1 AntiXray path demonstrably reduces Minecraft 26.2 client processing cost.

The result is **inconclusive pending a real-client experiment**.

V1 demonstrably reduces the number of distinct block states installed in affected client chunk sections. The controlled fixtures do not demonstrate a corresponding reduction in palette width, section scan work, model lookup count, or model tessellation dispatch count. The repository test environment cannot reliably exercise the resource-backed render compiler, mesh construction, GPU upload, or frame presentation boundary, so it cannot establish a client-side performance improvement.

This outcome neither proves nor disproves the production performance report. It prevents a structural difference from being presented as a measured client-speed benefit.

## 2. Scope and evidence classes

The investigation preserves the active runtime, packet encoding, mixins, observation policy, protection policy, replacement priority, reveal behavior, and authoritative world state. It adds test-only structural characterization and this document.

Statements below use four evidence classes:

- **Proven structural fact**: established by Minecraft 26.2 implementation inspection or deterministic tests using Minecraft classes.
- **Controlled runtime measurement**: elapsed work measured at a meaningful, reproducible client boundary. No such measurement was technically valid in this environment.
- **Plausible but unproven explanation**: consistent with the evidence but not measured at the required boundary.
- **Unknown**: requires a controlled real-client experiment.

Server-side serialized sizes and reproduced protocol sizes remain documented separately in `ANTI_XRAY_V1_REPRESENTATION_EFFICIENCY.md` and `ANTI_XRAY_V1_PROTOCOL_COMPRESSION.md`. They are not client processing measurements.

## 3. Minecraft 26.2 client processing path

The relevant vanilla path is:

1. `ClientboundLevelChunkWithLightPacket` carries the chunk data and light data received by the client protocol stack.
2. `ClientPacketListener.handleLevelChunkWithLight` transfers packet handling to the client thread as required, then passes chunk data to `ClientChunkCache.replaceWithPacketData` and applies the light data.
3. `ClientChunkCache.replaceWithPacketData` creates or replaces the client `LevelChunk` and supplies `ClientboundLevelChunkPacketData.getReadBuffer()` to `LevelChunk.replaceWithPacketData`.
4. Section data is decoded by `LevelChunkSection.read`, including `PalettedContainer.read`, and becomes the client-side installed section representation.
5. Chunk installation and light changes notify client-level and renderer state, including chunk-load handling and dirty render sections.
6. `LevelRenderer` and `SectionRenderDispatcher` schedule render-section compilation.
7. A render worker invokes `SectionCompiler.compile` for a render section. The compiler visits all 4,096 positions, retrieves each `BlockState`, handles opaque-state visibility, block entities and fluids, and dispatches model-shaped blocks through `BlockStateModelSet.get` and `ModelBlockRenderer.tesselateBlock`.
8. Built section meshes are uploaded and later drawn through the render pipeline.

Packet receipt, client chunk installation, render-task scheduling, worker compilation, buffer upload, and presentation therefore span different execution contexts. A server-side JUnit test is not an adequate substitute for this pipeline.

## 4. Characterization method

`ClientRepresentationStructureCharacterizationTest` reuses the V1 fixture approach while targeting the first client boundary that can be reproduced deterministically:

- construct a real `LevelChunkSection`;
- serialize the original with `LevelChunkSection.write`;
- serialize the transformed representation with the active `AntiXrayObfuscator.writeSection` path;
- decode each byte stream into a new section with `LevelChunkSection.read`;
- inspect the representation actually installed after decode;
- count structural inputs to the relevant `SectionCompiler.compile` branches;
- confirm that the authoritative source section was not modified.

The fixture families are:

- terrain control: stone only;
- sparse Overworld ore: stone plus eight diamond-ore blocks;
- resource-diverse Overworld: stone plus 48 protected blocks across six ore types;
- unrelated-diversity control: 16 non-protected full-cube model states;
- repeated processing: one, four, sixteen, and twenty-four equivalent sections.

The earlier representation and protocol characterization tests continue to cover their established Overworld, Nether, mixed-terrain, dense synthetic, control, and multi-section packet fixtures. This investigation does not rewrite those measurements.

## 5. Proven structural facts

### 5.1 Installed palette/container state

The decoded client-side section measurements are:

| Fixture | Original distinct states | V1 distinct states | Original bits per entry | V1 bits per entry |
| --- | ---: | ---: | ---: | ---: |
| Terrain control | 1 | 1 | 0 | 0 |
| Sparse Overworld ore | 2 | 1 | 4 | 4 |
| Resource-diverse Overworld | 7 | 1 | 4 | 4 |
| Unrelated-diversity control | 16 | 16 | 4 | 4 |

V1's diversity reduction survives real Minecraft section serialization and decode. For the tested non-singleton sections it does **not** reduce the installed block-state palette width: both original and transformed containers remain at four bits per entry. The singleton terrain control remains zero bits in both forms.

The unrelated-diversity control is unchanged, showing that the measurement is tied to V1's protected-block replacement decisions rather than to serialization alone.

### 5.2 Render-section scan and branch inputs

For every tested section, original and V1 representations have:

- 4,096 visited positions;
- 4,096 non-air positions;
- 4,096 solid-render positions;
- 4,096 model-render-shape positions;
- zero fluid positions;
- zero block-entity positions.

The ore states and their stone replacements used by these fixtures are all non-air, solid-render, model-shaped states with empty fluid state and no block entity.

Consequently, V1 does not reduce the number of positions scanned by `SectionCompiler.compile`, the number of solid-state visibility inputs, or the number of model lookup/tessellation dispatches for these fixtures. Repetition across one, four, sixteen, and twenty-four sections scales those counts equally for original and transformed representations.

### 5.3 Scheduling and installation

Chunk installation and render-section invalidation operate at chunk/section boundaries. The tested replacement does not remove sections, turn them into all-air sections, or avoid the installed chunk update. No reduction in chunk installation count or render-section rebuild task count follows from the measured diversity reduction.

## 6. Geometry, layers, and allocations

Ore-to-stone replacement must not be assumed to reduce geometry. Both sides enter the full-cube model path for every fixture position, and the deterministic branch counts are equal. This rejects a claim that fewer protected block-state identities automatically means fewer model tessellation calls.

The following remain unknown because they require the fully initialized client resource and render environment:

- baked-model selection cost after `BlockStateModelSet.get`;
- emitted quad and vertex counts;
- face-culling results at unlike and like state boundaries;
- render-layer distribution;
- mesh buffer sizes;
- temporary allocations during compilation;
- render-worker CPU time;
- buffer upload cost;
- cache behavior across repeated sections and chunks.

Stone and ore are structurally similar inputs, but structural similarity is not a measurement of their baked models or generated meshes. No geometry, quad, vertex, layer, or allocation reduction is claimed.

## 7. Timing boundary decision

No timing result was added.

A meaningful render timing requires Minecraft's client resource reload, baked block models, texture atlases, render regions, buffer builders, render-worker scheduling, and upload lifecycle. The repository's server-oriented JUnit bootstrap does not reliably establish those dependencies. Timing container iteration or isolated state lookup would measure a proxy selected for convenience, not the suspected production boundary.

Accordingly, this mission stops at deterministic installed-section and compiler-branch characterization. It records no diagnostic micro-timing and makes no benchmark claim.

## 8. Hypotheses tested

### Supported structural hypothesis

- **V1 lowers installed block-state diversity for affected sections.** Supported. The sparse fixture falls from two states to one and the resource-diverse fixture from seven states to one after real section decode.

This is a representation property, not yet a demonstrated client efficiency property.

### Rejected hypotheses

- **V1 necessarily lowers the installed palette bit width.** Rejected for the measured ore-bearing fixtures: four bits remain four bits.
- **V1 reduces render-section scan iterations.** Rejected: the compiler still visits 4,096 positions per section.
- **V1 reduces model lookup or model tessellation dispatch count merely by replacing ore with stone.** Rejected for the measured fixtures: all 4,096 states enter the model path before and after transformation.
- **V1 necessarily reduces render rebuild task count.** Rejected as an implication of these transformations: the same installed sections are dirtied and require rebuild preparation.
- **Lower state diversity alone proves less generated geometry.** Rejected. No such conclusion follows from palette diversity, and both representations take the same structural model path.

### Plausible but unproven explanations

- Fewer distinct state/model identities may improve locality in model-state lookup or resource caches.
- Repeated stone models may be cheaper to resolve or tessellate than a mixture of ore models despite equal dispatch counts.
- More uniform neighboring states may affect face selection, mesh contents, render layers, temporary allocation patterns, or downstream GPU behavior.
- The previously measured small protocol reduction may combine with client effects in production.

None of these explanations is established by the current measurements.

## 9. Relationship to the reported Elytra improvement

The production improvement observed while moving quickly through terrain remains unexplained by this repository-only investigation. The evidence rules out several simple server-visible explanations, but it does not isolate client render work, client scheduling, GPU upload, network delivery under real latency, modded renderer behavior, or cache effects.

No client performance conclusion is inferred from the earlier raw-section or compressed-protocol byte differences.

## 10. Required controlled client experiment

The next investigation must run a real Minecraft 26.2 client and compare original and V1-transformed representations for identical authoritative terrain and identical movement. It should:

1. begin with the vanilla renderer; evaluate Sodium or another renderer separately;
2. use a fixed world seed, generated chunks, player route, Elytra velocity profile, camera path, render distance, simulation distance, graphics settings, resolution, Java runtime, heap, server configuration, and compression threshold;
3. compare the same fixture families and include no-op and unrelated-diversity controls;
4. separate cold-cache from warmed repeated passes and randomize run order;
5. record chunk receipt/install latency, render tasks queued and completed, section compilation duration, quad/vertex and layer counts, mesh bytes, upload duration, temporary allocations and garbage collection, plus frame-time distributions;
6. identify thread and boundary for every measurement and avoid persistent telemetry;
7. repeat enough independent runs to report variance and confidence rather than a single elapsed time;
8. verify rendered output and player-visible behavior are equivalent for the intended AntiXray representation.

Instrumentation should be confined to a disposable experiment build or profiler setup. Vanilla and modded-renderer results must not be combined because their pipelines and optimizations may differ.

## 11. Implications for V2

No demonstrated client-side efficiency requirement can yet be assigned to V2 from this mission.

V2 should retain the ability to produce valid, behaviorally correct, player-specific representations and should avoid needless block-state diversity where policy outcomes are otherwise equivalent. However, preserving V1's exact diversity profile is not justified as a performance requirement until the controlled client experiment measures a material benefit at a real client boundary.

V2 must not trade correctness, valid representation, observation semantics, or authoritative-world safety for this unproven hypothesis.

## 12. Conclusion

**Result: inconclusive pending a real-client experiment.**

The investigation demonstrates a real structural simplification: V1 collapses protected ore identities to the replacement state in the client-installed section. It also supplies counterevidence against straightforward cost explanations: palette width, section scanning, solid-state processing, and model dispatch counts do not improve in the characterized cases. The remaining potentially meaningful costs live inside a fully initialized client render and upload pipeline and require the controlled experiment described above.
