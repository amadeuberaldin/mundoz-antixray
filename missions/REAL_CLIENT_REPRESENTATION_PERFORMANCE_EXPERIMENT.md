Mission — Real Client Representation Performance Experiment
Status

Planned

Context

Previous MundoZ AntiXray characterization missions established the following:

V1 reduces block-state diversity in affected player representations.
V1 does not necessarily reduce raw section or packet payload size.
V1 can improve Minecraft protocol compression, but the measured savings were small.
V1 does not reduce section scan counts or model-dispatch counts in the deterministic client-side structural characterization.
The repository-only test environment cannot reliably measure the complete Minecraft client render, mesh, upload, allocation, or frame-time pipeline.

Historical production use nevertheless suggested that AntiXray v1 could make rapid terrain loading feel substantially smoother, especially during high-speed Elytra travel.

The mechanism remains unproven.

The remaining meaningful investigation requires a real Minecraft 26.2 client.

This mission defines that experiment.

It must measure before recommending any V2 optimization.

Objective

Determine whether the player representation produced by AntiXray v1 produces a measurable client-side performance difference compared with the equivalent original Minecraft representation.

The experiment must isolate client-side effects as far as practical and determine whether any observed difference is:

reproducible;
statistically meaningful;
attributable to representation structure;
large enough to matter in real gameplay.
Primary Research Question

When the same player traverses the same already-generated terrain under controlled conditions, does receiving the V1-transformed representation measurably reduce Minecraft 26.2 client processing cost or frame-time instability compared with receiving the original representation?

Core Comparison

The experiment must compare two representation modes.

Mode A — Original representation

The client receives the normal world representation without V1 AntiXray obfuscation affecting the tested terrain.

Mode B — V1 representation

The client receives the same authoritative world through the active V1 AntiXray representation path.

The authoritative server world, terrain, player route, and gameplay state must otherwise remain equivalent.

The test must not compare different worlds or independently generated terrain.

Important Safety Rule

Do not alter the authoritative world merely to create the comparison.

The AntiXray representation remains client-only.

Any experiment mechanism that would require modifying the actual world instead of the player representation must stop for review.

Experiment Order

The experiment must be performed in two distinct phases.

Phase 1 — Vanilla Minecraft 26.2 Client

Establish the vanilla client baseline first.

Do not use Sodium, Iris, shaders, or unrelated client optimization mods during the vanilla experiment.

Phase 2 — Sodium Client

Repeat the experiment separately using the approved Minecraft 26.2 Sodium setup if available.

Do not combine Vanilla and Sodium results.

Treat them as separate client pipelines and separate evidence.

If Sodium or its matching version is unavailable, complete and document Vanilla first rather than delaying the entire mission.

Environment Control

Record the exact test environment.

At minimum:

Minecraft version;
Fabric Loader version if applicable;
Java version;
client JVM arguments;
allocated client heap;
GPU;
GPU driver;
CPU;
RAM;
operating system;
display resolution;
refresh rate;
VSync state;
fullscreen/windowed state;
render distance;
simulation distance;
graphics preset;
biome blend;
entity distance;
clouds;
particles;
mipmap level;
chunk update settings where applicable;
framerate limit;
Sodium version when used;
other installed client mods.

Do not silently change settings between runs.

Server Control

Record and preserve:

server Minecraft version;
server JVM;
server heap;
server view distance;
server simulation distance;
AntiXray jar;
AntiXray V1/V2 activation properties;
Minecraft protocol compression threshold;
server world;
server seed;
world time where relevant;
weather where relevant;
loaded entities in the test area;
server-side mods that can affect chunk transmission or player movement.

Avoid unrelated server changes during the experiment.

World Preparation

Use already-generated terrain.

Chunk generation must not be mixed with client representation measurement.

The test route must be pre-generated sufficiently beyond the client render distance so that the measured runs primarily exercise:

chunk transmission;
chunk receipt;
client installation;
render preparation;
mesh construction;
GPU upload;
rendering.

If chunk generation occurs during a measurement run, flag the run as contaminated and exclude it from direct comparison.

Route Design

Use a fixed route.

The route must be repeatable.

Prefer one or more of:

fixed Elytra route;
fixed spectator-flight route;
deterministic teleport sequence;
replayed movement path if available without introducing unrelated behavior.

The route should load enough new terrain to expose chunk-processing differences.

Record:

route start;
route end;
approximate distance;
altitude;
direction;
movement speed;
duration.

Use the same route for both representation modes.

Elytra Scenario

Because the historical observation was strongest during rapid terrain loading, include a high-speed traversal scenario.

If practical, use a controlled Elytra flight.

Control:

starting position;
altitude;
heading;
speed profile;
fireworks or propulsion method;
camera direction.

If these cannot be reproduced reliably by manual movement, prefer a more deterministic movement method for the actual benchmark and keep Elytra as a supplementary real-gameplay validation.

Cold And Warm Cache

Cache state must be controlled.

At minimum distinguish:

Cold pass

The client has not recently rendered the route's target chunks in the current controlled cycle.

Warm pass

The route or relevant resources have already been processed by the client.

Do not compare an Original cold pass with a V1 warm pass.

Where possible:

alternate modes;
randomize run order;
restart client between cold-run groups;
clear only caches that can be cleared reproducibly without changing unrelated configuration.

Document the exact procedure.

Run Order

Do not always test Original first and V1 second.

Use alternating or randomized order.

Example:

A
B
B
A
A
B
B
A

where:

A = Original
B = V1

The goal is to reduce bias from:

JVM warmup;
shader/model/resource caches;
OS filesystem cache;
GPU driver cache;
thermal state;
background processes.
Warmup

Before collecting measurements:

start the client;
enter a neutral area;
allow resource loading to settle;
allow JVM compilation/warmup;
allow background chunk work to settle.

Do not include startup/resource initialization as part of the measured route unless specifically studying startup.

Required Metrics

Measure as many of the following as can be obtained reliably.

Frame-time metrics

Preferred:

median frame time;
average frame time;
1% low equivalent;
0.1% low equivalent where statistically meaningful;
95th percentile frame time;
99th percentile frame time;
maximum frame-time spike;
count of frames above selected thresholds such as 16.7 ms, 33.3 ms, and 50 ms.

Prefer frame time over average FPS alone.

Average FPS may be reported but must not be the primary metric.

Chunk processing metrics

Where measurable:

chunk packets received;
chunks installed;
sections dirtied;
render tasks queued;
render tasks completed;
section compilation duration;
number of concurrent render worker tasks;
chunk upload duration.
Geometry metrics

Where measurable:

generated quads;
generated vertices;
mesh buffer bytes;
render-layer counts;
visible faces.
Allocation and GC

Where measurable:

allocation rate;
allocated bytes during route;
garbage collection count;
garbage collection pause duration;
heap high-water mark.
CPU

Where measurable:

render-thread CPU;
chunk worker CPU;
overall process CPU;
server CPU separately.
Network

Retain as contextual data:

bytes received;
packet counts;
chunk packet bytes.

Do not interpret network differences as client-render differences.

Measurement Tools

Prefer existing trusted profiling or measurement tools over building permanent instrumentation.

Possible approaches may include:

Java Flight Recorder;
JVM profiling tools;
Minecraft debug/profiling facilities;
external frame-time capture;
disposable development instrumentation;
graphics profiling tools where practical.

The exact tool must be justified by the metric it measures.

Do not install or commit permanent production telemetry into MundoZ AntiXray.

Any source instrumentation used specifically for the experiment must live on a dedicated experimental branch and be removable afterward.

Java Flight Recorder

JFR is strongly preferred for JVM-level measurements when appropriate.

Potential metrics:

CPU samples;
thread activity;
allocations;
GC;
monitor contention;
method samples.

If JFR is used:

use the same recording configuration for every compared run;
record the start and stop boundaries;
avoid comparing recordings with substantially different durations;
retain raw recordings outside the Git repository unless explicitly approved.

Do not commit large binary recordings.

Frame-Time Capture

Use a tool capable of reporting individual frame times where practical.

Do not rely solely on visually reading the F3 FPS counter.

The measurement must allow comparison of frame-time distribution and stutter.

If an external frame-time tool cannot be used reliably on the current Linux/graphics stack, document the limitation and identify the best available alternative.

Client Instrumentation Boundary

If Minecraft-specific counters are unavailable and source instrumentation is required, instrumentation must be:

development-only;
explicitly enabled;
bounded;
temporary;
player-safe;
free of persistent telemetry;
limited to the exact measured boundary.

Do not modify AntiXray semantics merely to instrument Minecraft.

If instrumentation requires a separate client-side helper mod, that helper must have a clearly isolated purpose and must not become a dependency of the server AntiXray mod.

Representation Toggle

The experiment requires a reliable way to compare Original and V1 representation.

Before implementing any toggle, inspect the current runtime and determine the safest test mechanism.

Preferred options, in order:

controlled server restart with AntiXray jar enabled or disabled;
separate otherwise-identical server instances;
an experiment-only startup property selecting the representation path, but only if it can be added without affecting production and receives explicit review.

Do not add a mutable runtime command toggle merely for convenience.

Do not change representation mode mid-packet or mid-section.

V2 Isolation

V2 must not influence the client-performance comparison.

If shadow V2 is enabled for other development reasons, verify that it does not materially affect the client-side comparison through server CPU contention.

Prefer disabling unrelated V2 shadow-validation instrumentation for the performance experiment unless its presence is itself being measured.

The goal here is Original versus V1 representation cost.

Server Performance Contamination

A slower server can make the client appear less smooth.

Therefore record server health during every run.

At minimum watch for:

Can't keep up warnings;
tick-time spikes;
CPU saturation;
GC pauses;
chunk generation;
disk I/O spikes.

Runs with server overload should be marked contaminated.

The experiment must not attribute server stalls to client representation cost.

Local Network Consideration

Where possible perform tests under a stable low-jitter connection.

If client and server are on the same LAN, record that.

If traffic crosses the Internet/VPS/WireGuard path, network variability must be acknowledged.

For the strongest client-render comparison, a local or otherwise controlled network is preferable if technically practical.

Repetition

One run is not evidence.

Perform enough repetitions to estimate variability.

Initial target:

at least 5 valid runs per mode for exploratory comparison;
preferably 10 or more when variance is high.

The final number should depend on observed noise.

Do not discard inconvenient runs without a predefined contamination reason.

Run Record

For each run record:

unique run ID;
date/time;
client mode;
Vanilla or Sodium;
Original or V1 representation;
cold or warm;
route;
duration;
client settings checksum or equivalent record;
server state;
profiler files;
contamination notes;
measured metrics.

Use a simple structured format such as CSV or JSON.

Do not put personal data or unrelated client information into run records.

Statistical Analysis

Do not base conclusions only on the best run.

For major metrics report:

sample count;
median;
mean where useful;
variability;
relevant percentiles;
original versus V1 delta;
percentage difference.

If results overlap heavily relative to run-to-run variance, classify the result as inconclusive.

Do not present tiny differences below measurement noise as optimization wins.

Interpretation Categories

At completion classify each major hypothesis.

Demonstrated material client benefit

A repeatable difference exists, exceeds measurement noise, and is large enough to plausibly matter during gameplay.

Demonstrated small client benefit

A repeatable difference exists but is too small by itself to explain the historical observation.

No meaningful difference detected

Measurements show no material client difference under the controlled scenario.

Inconclusive

Noise, tooling, or missing boundaries prevent reliable conclusion.

Different metrics may receive different classifications.

Causality Rule

Even if V1 performs better, do not immediately attribute the benefit to lower block-state diversity.

Use measured intermediate metrics to identify the mechanism.

For example:

If frame time improves but:

mesh size is unchanged;
compilation time is unchanged;
allocations are unchanged;

then another explanation is required.

Likewise, if compilation time decreases while network bytes change only slightly, client-side work becomes a stronger explanation.

Vanilla First

The primary evidence must begin with Vanilla.

This establishes a Minecraft baseline without renderer-specific optimizations.

Only after completing the Vanilla experiment should Sodium be tested.

A Sodium-specific result must be labeled:

Sodium-specific

unless the Vanilla experiment shows the same mechanism.

Sodium Phase

If Sodium is available for Minecraft 26.2, repeat the same core route and comparison.

Record:

exact Sodium version;
Sodium settings;
whether chunk builder thread configuration differs;
whether any Sodium-specific metrics are available.

Do not assume Sodium behaves like Vanilla.

Output Correctness

Performance is irrelevant if the player representation becomes incorrect.

For every compared mode verify:

visible terrain is correct for that mode;
AntiXray-protected information behaves as expected;
no missing chunks;
no corrupted sections;
no visual artifacts caused by the experiment harness.

If performance differs because output correctness differs, the comparison is invalid.

Required Documentation

Create:

docs/architecture/ANTI_XRAY_REAL_CLIENT_PERFORMANCE_EXPERIMENT.md

The document must contain:

experiment objective;
environment;
route;
representation-mode mechanism;
Vanilla methodology;
Sodium methodology if completed;
measurement tools;
run table;
frame-time results;
chunk/render results;
allocation/GC results;
network context;
statistical interpretation;
supported hypotheses;
rejected hypotheses;
remaining uncertainties;
V2 implications;
recommendation for the next mission.

Do not fill sections with claims before measurements exist.

Experiment Data

Do not commit large raw profiler recordings to Git.

Small structured summaries may be committed when useful.

Raw:

JFR files;
frame captures;
profiler dumps;
large logs;

should remain outside the repository unless explicitly approved.

Document their local storage location if they are needed for reproducibility.

V2 Decision Boundary

This experiment does not authorize V2 optimization.

At completion, report whether a demonstrated client-side property exists that may justify a future V2 requirement.

Examples might include:

preferring an already-common replacement state;
reducing model/state diversity;
reducing mesh complexity;
reducing render-layer diversity.

These are examples only.

Do not select one before evidence exists.

Observation correctness remains the primary V2 requirement.

Production Safety

Do not deploy experimental profiling code to normal production operation unless explicitly approved.

If testing uses the MundoZ Survival server, use a controlled window.

Protect player experience and server stability.

Do not collect information about unrelated players.

Prefer testing when no unrelated players are online.

Stop Conditions

Stop for review if the experiment requires:

permanent production telemetry;
changes to packet semantics;
changes to AntiXray decisions;
changes to world state;
invasive client modifications that invalidate the comparison;
disabling safety behavior;
collecting unrelated player data;
uncontrolled production testing.
Acceptance Criteria

The mission is complete when:

Vanilla real-client testing has been performed or its blocker precisely documented;
Original and V1 representations were compared under equivalent conditions;
run order and cache state were controlled;
frame-time distribution was measured where technically possible;
server contamination was monitored;
enough repetitions were performed to characterize variance;
results distinguish client cost from network/server effects as far as possible;
conclusions respect measurement noise;
no V2 optimization was implemented;
production/runtime changes are absent or explicitly experimental and reverted;
findings are documented;
the next architectural decision is evidence-based.
Commit Discipline

Repository commits should contain only:

experiment harness code that is appropriate to retain;
small structured result summaries;
documentation.

Do not commit raw profiler recordings.

Prefer separate commits for:

experiment harness;
Vanilla findings;
Sodium findings;
final interpretation.

Do not merge until final maintainer review.

Final Deliverable

The final report must answer:

Was there a reproducible client-side performance difference between Original and V1?
How large was it?
Which metric showed it most clearly?
Was it Vanilla, Sodium, or both?
Which intermediate client cost changed?
Did network savings contribute materially?
Could server stalls explain the result?
Did cold versus warm cache affect the result?
Does the evidence plausibly explain the historical rapid-terrain-loading improvement?
Is there a concrete client-efficiency property V2 should preserve?
What is the next implementation or investigation mission?

A negative or inconclusive result is valid.

Do not optimize V2 until this result is reviewed.
