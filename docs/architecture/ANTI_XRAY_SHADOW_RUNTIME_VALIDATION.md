# Shadow Runtime Validation

## Purpose and authority

Shadow runtime validation adds bounded aggregate diagnostics to the existing
v2 shadow path. It does not enable shadow execution and gives v2 no authority.
V1 remains the sole source of section mutation, replacement selection, packet
and palette serialization, and player-visible output.

Shadow execution and validation diagnostics are separate startup decisions:

```text
-Dmundoz.antixray.v2-shadow=true
-Dmundoz.antixray.v2-shadow-validation=true
```

The validation property defaults to false, is read once during static runtime
initialization, and remains immutable for the process lifetime. If reading the
property fails for any reason, validation resolves safely to disabled. Enabling
validation without enabling shadow execution produces no shadow work.

## Collected aggregates

Diagnostics observe only work already admitted by the existing shadow bound:
at most one v2-supported protected candidate per qualifying section.
Unsupported v1-only candidates do not consume that budget. Diagnostics do not
repeat an evaluation, collect another observation path, scan another section,
or read additional world data.

The process-lifetime aggregate contains only counters and timing totals:

- participating-section initialization count, total nanoseconds, and maximum
  nanoseconds;
- supported-candidate evaluation count, total nanoseconds, and maximum
  nanoseconds;
- counts for each existing comparable `RuntimeDecisionComparison` category,
  plus their agreement and disagreement totals;
- v2 observed and not-observed counts;
- unsupported candidate, unavailable/UNKNOWN observation, missing replacement,
  and shadow failure counts.

Unavailable and missing-replacement outcomes retain their already-computed
comparison for agreement accounting. The diagnostic distinctions do not alter
observation, protection, replacement, reveal, or comparison semantics.

The aggregate never retains individual samples, Minecraft objects, world or
section references, player identity, player or block coordinates, chunk
coordinates, network information, or arbitrary block data. It is not persisted
or transmitted.

## Disabled cost and failure isolation

When validation is disabled, existing shadow execution follows its accepted
path without diagnostic event objects, strings, collections, counter updates,
logging, or diagnostic clock reads. Validation activation does not bypass the
shadow activation guard.

Diagnostic recording and reporting failures are contained. They cannot affect
v1, v2 shadow evaluation, section processing, server shutdown, serialization,
or authoritative world state.

## Reporting boundary

A dedicated server with validation enabled writes one concise SLF4J aggregate
summary during Fabric's dedicated-server stopping lifecycle. There are no
periodic or startup summaries, integrated-server reports, shutdown hooks,
background threads, asynchronous reporting, intermediate summaries, or counter
resets. Abnormal process termination may lose the summary.

The summary fields use `count`, `totalNanos`, and `maxNanos` for section
initialization and candidate evaluation. Agreement categories describe only
the single supported candidate admitted by each participating section.
`unsupported` counts v1 candidates rejected before the supported-candidate
budget is consumed. `unavailable`, `missingReplacement`, and `failure` explain
why an evaluated or attempted shadow result could not be treated as an ordinary
comparable result; none is authoritative.

## Measurement limitations

Timings use monotonic `System.nanoTime()` calls only while validation is
enabled. They are approximate runtime-validation indicators, not benchmark
results. Measurements may include distortion from JVM warmup, JIT compilation,
GC pauses, server contention, Minecraft world access, and the timing calls
themselves. Totals and maxima do not provide percentiles or a latency
distribution, and process-lifetime aggregation can mix warm and steady-state
execution.

For controlled validation, start a dedicated server with both properties set,
exercise representative Overworld, Nether, and End chunk transmission, stop the
server normally, and inspect the single summary. Compare evaluation counts with
section counts to confirm participation, inspect agreement/disagreement and
failure categories, and divide totals by counts only as a rough average. Use an
external profiler in a separate investigation before drawing production
performance conclusions.
