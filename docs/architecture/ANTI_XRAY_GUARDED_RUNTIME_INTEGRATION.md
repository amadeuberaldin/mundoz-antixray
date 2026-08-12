# Guarded AntiXray Runtime Integration

## Status

The v2 runtime integration is observational and opt-in. The active v1
obfuscator remains the only authority for the section written to the client.

Shadow evaluation is enabled with the JVM system property:

```text
-Dmundoz.antixray.v2-shadow=true
```

It is disabled by default. The property is read once when the shadow runtime
class is initialized. That startup decision is retained immutably for the
lifetime of the process; changing the system property afterward does not
toggle shadow participation.

## Runtime Boundary

The existing `ClientboundLevelChunkPacketDataMixin` still redirects section
serialization to `AntiXrayObfuscator`. The mixin, packet format, palette
encoding, context lifecycle, and reveal path are unchanged.

After v1 has found its section and replacement boundary,
`AntiXrayShadowRuntime` may create a read-only section evaluation. It reads
the five accepted v2 terrain candidates once for that section.

When the immutable startup decision is disabled, `AntiXrayObfuscator` bypasses
the shadow boundary entirely. It creates no shadow section evaluation or
candidate comparison, performs no observation collection, and constructs no
v2 evaluator or service.

The section evaluation may shadow-evaluate at most one v2-supported protected
candidate. V1-only candidates are mapped as unsupported and do not consume
that budget. Once a supported candidate enters evaluation, the budget is
consumed even if evaluation fails. Every later candidate skips observation
collection and v2 policy evaluation.

For the single selected candidate, the shadow path:

1. collects observation paths from the active camera using loaded chunks only;
2. applies the accepted observation policy;
3. applies protection and replacement policy;
4. compares the discarded v2 hide decision with the v1 decision.

The comparison result is internal only. It is available to focused tests and a
development debugger; there is no persistent state, production telemetry, or
per-block logging.

## Isolation and Failure Behavior

V1 still decides whether the copied section is changed and which section is
serialized. No v2 result is used by `setBlockState`, section writing, packet
construction, reveal updates, or world state.

Unsupported legacy v1 targets, including the geode family and tuff slab, yield
`V2_CANNOT_EVALUATE`. Property access, mapping, collection, policy
coordination, and comparison are all contained by the shadow boundary.
Shadow failures cannot escape into v1 serialization.
Unavailable loaded-chunk reads become `UNKNOWN` facts and therefore preserve
the accepted fail-visible v2 decision.

The shadow path does not request or generate chunks and never writes a block.

## Comparison Model

The bounded comparison model distinguishes:

- both implementations reveal;
- both implementations hide;
- v1 hides while v2 reveals;
- v1 reveals while v2 hides;
- v2 cannot safely evaluate.

Focused fixtures establish all five classifications. Real Minecraft block
states are accepted at the infrastructure boundary, and unsupported or failed
shadow inputs remain non-authoritative.

## Performance Boundary

Shadow work occurs only when the immutable startup decision is enabled and
only after the existing v1 fast exits. Disabled execution remains on the v1
path without per-section shadow allocation or property parsing. A participating
section scan still inspects at most 4096 cells. Accepted replacement terrain
is collected once per participating section.

At most one supported protected target per participating section may produce
one center sample and up to three observer-facing face samples. Each sample
traverses every voxel cell between the current camera eye and the target until
it reaches the target or missing data. The bounded work can repeat whenever
Minecraft transmits that section.

This establishes a potentially material hot-path cost. The mission therefore
does not enable shadow mode by default, add caching, retain cross-section
state, or accept the cost for production. Runtime profiling with representative
Overworld, Nether, and End sections is required before broader activation.

## Deferred Decisions

This integration does not approve:

- using a v2 decision for player-visible output;
- changing protected targets or replacement priority;
- removing the v1 arbitrary solid fallback;
- changing observation or reveal semantics;
- changing packet, palette, or serialization behavior;
- enabling shadow evaluation by default;
- adding caching, asynchronous work, or persistent telemetry;
- replacing legacy runtime components.
