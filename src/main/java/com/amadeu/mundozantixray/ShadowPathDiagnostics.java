package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.observation.ObservationPathEvaluationService;
import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.policy.DefaultObservationPolicy;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.BlockPositionMapper;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftObservationPathCollector;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftRuntimeShadowEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

final class ShadowPathDiagnostics {
    static final String ENABLED_PROPERTY =
            "mundoz.antixray.v2-shadow-path-diagnostics";
    static final String DIMENSION_PROPERTY =
            "mundoz.antixray.v2-shadow-path-dimension";
    static final String TARGET_PROPERTY =
            "mundoz.antixray.v2-shadow-path-target";

    private static final Gate GATE = new Gate(resolveConfiguration(
            () -> System.getProperty(ENABLED_PROPERTY),
            () -> System.getProperty(DIMENSION_PROPERTY),
            () -> System.getProperty(TARGET_PROPERTY)
    ));

    private ShadowPathDiagnostics() {}

    static boolean shouldTrace(ServerLevel level, BlockPos target) {
        if (!GATE.isEnabled()) {
            return false;
        }
        try {
            return GATE.matchesAndClaim(level.dimension().identifier(), target);
        } catch (Throwable diagnosticFailure) {
            return false;
        }
    }

    static void report(
            ServerLevel level,
            ServerPlayer player,
            Entity camera,
            BlockPos target,
            BlockState targetState,
            boolean v1Hides,
            MinecraftObservationPathCollector.TraceResult trace,
            MinecraftRuntimeShadowEvaluator.DiagnosticOutcome outcome
    ) {
        try {
            TraceRecord record = reconstruct(
                    level.dimension().identifier(),
                    player,
                    camera,
                    target,
                    targetState,
                    v1Hides,
                    trace,
                    outcome
            );
            LoggerHolder.LOGGER.info(
                    "AntiXray v2 shadow path diagnostic: {}",
                    format(record)
            );
        } catch (Throwable diagnosticFailure) {
            // Detailed diagnostics are never authoritative.
        }
    }

    static Configuration resolveConfiguration(
            Supplier<String> enabled,
            Supplier<String> dimension,
            Supplier<String> target
    ) {
        try {
            if (!Boolean.parseBoolean(enabled.get())) {
                return Configuration.disabled();
            }
            Identifier dimensionId = Identifier.tryParse(dimension.get());
            BlockPos targetPos = parseTarget(target.get());
            if (dimensionId == null || targetPos == null) {
                return Configuration.disabled();
            }
            return new Configuration(true, dimensionId, targetPos);
        } catch (Throwable diagnosticFailure) {
            return Configuration.disabled();
        }
    }

    private static BlockPos parseTarget(String value) {
        if (value == null) {
            return null;
        }
        String[] coordinates = value.split(",", -1);
        if (coordinates.length != 3) {
            return null;
        }
        try {
            return new BlockPos(
                    Integer.parseInt(coordinates[0].trim()),
                    Integer.parseInt(coordinates[1].trim()),
                    Integer.parseInt(coordinates[2].trim())
            );
        } catch (NumberFormatException invalidTarget) {
            return null;
        }
    }

    private static TraceRecord reconstruct(
            Identifier dimension,
            ServerPlayer player,
            Entity camera,
            BlockPos target,
            BlockState targetState,
            boolean v1Hides,
            MinecraftObservationPathCollector.TraceResult trace,
            MinecraftRuntimeShadowEvaluator.DiagnosticOutcome outcome
    ) {
        ObservationEvaluationService singlePath = new ObservationEvaluationService(
                new DefaultObservationPolicy()
        );
        ObservationPathEvaluationService aggregate =
                new ObservationPathEvaluationService(singlePath);
        List<SampleRecord> samples = new ArrayList<>();

        for (int index = 0; index < trace.samples().size(); index++) {
            MinecraftObservationPathCollector.SampleTrace sample =
                    trace.samples().get(index);
            ObservationContext context = trace.contexts().get(index);
            List<VisitedRecord> visited = sample.visited().stream()
                    .map(ShadowPathDiagnostics::visitedRecord)
                    .toList();
            samples.add(new SampleRecord(
                    sample.index(),
                    sampleKind(target, sample.endpoint()),
                    sample.endpoint(),
                    visited,
                    context,
                    singlePath.evaluate(context)
            ));
        }

        return new TraceRecord(
                dimension,
                camera == player,
                BuiltInRegistries.ENTITY_TYPE.getKey(camera.getType()),
                camera.getId(),
                trace.cameraEye(),
                trace.observerBlockPos(),
                target.immutable(),
                blockId(targetState),
                samples,
                aggregate.evaluate(trace.contexts()),
                v1Hides,
                outcome.kind(),
                outcome.comparison()
        );
    }

    private static VisitedRecord visitedRecord(
            MinecraftObservationPathCollector.VisitedBlock visited
    ) {
        Optional<BlockState> state = visited.state();
        return new VisitedRecord(
                visited.position(),
                state.map(ShadowPathDiagnostics::blockId).orElse("UNAVAILABLE"),
                state.map(ShadowPathDiagnostics::waterlogged)
                        .orElse("NOT_APPLICABLE"),
                visited.behavior()
        );
    }

    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static String waterlogged(BlockState state) {
        if (!state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return "NOT_APPLICABLE";
        }
        return state.getValue(BlockStateProperties.WATERLOGGED).toString();
    }

    private static String sampleKind(BlockPos target, Vec3 endpoint) {
        double centerX = target.getX() + 0.5D;
        double centerY = target.getY() + 0.5D;
        double centerZ = target.getZ() + 0.5D;
        if (endpoint.x == centerX && endpoint.y == centerY
                && endpoint.z == centerZ) {
            return "CENTER";
        }
        if (endpoint.x != centerX) {
            return "X_FACE";
        }
        if (endpoint.y != centerY) {
            return "Y_FACE";
        }
        return "Z_FACE";
    }

    static String format(TraceRecord record) {
        StringBuilder output = new StringBuilder();
        output.append("dimension=").append(record.dimension())
                .append(",camera={isPlayerCamera=").append(record.playerCamera())
                .append(",entityType=").append(record.cameraEntityType())
                .append(",entityId=").append(record.cameraEntityId())
                .append(",eye=").append(vec(record.cameraEye()))
                .append(",observerBlockPos=").append(pos(record.observerBlockPos()))
                .append("},target={pos=").append(pos(record.target()))
                .append(",state=").append(record.targetStateId()).append("}")
                .append(",v1Hides=").append(record.v1Hides())
                .append(",samples=[");
        for (int sampleIndex = 0; sampleIndex < record.samples().size(); sampleIndex++) {
            if (sampleIndex > 0) {
                output.append(',');
            }
            SampleRecord sample = record.samples().get(sampleIndex);
            output.append("{index=").append(sample.index())
                    .append(",kind=").append(sample.kind())
                    .append(",endpoint=").append(vec(sample.endpoint()))
                    .append(",firstVisited=")
                    .append(sample.visited().isEmpty()
                            ? "NONE"
                            : pos(sample.visited().getFirst().position()))
                    .append(",visited=[");
            for (int visitedIndex = 0;
                    visitedIndex < sample.visited().size(); visitedIndex++) {
                if (visitedIndex > 0) {
                    output.append(',');
                }
                VisitedRecord visited = sample.visited().get(visitedIndex);
                output.append("{pos=").append(pos(visited.position()))
                        .append(",state=").append(visited.stateId())
                        .append(",waterlogged=").append(visited.waterlogged())
                        .append(",behavior=").append(visited.behavior())
                        .append('}');
            }
            output.append("],context={observer=")
                    .append(pos(BlockPositionMapper.toMinecraft(
                            sample.context().observerPosition())))
                    .append(",target=")
                    .append(pos(BlockPositionMapper.toMinecraft(
                            sample.context().targetPosition())))
                    .append(",path=").append(sample.context().pathBeforeTarget())
                    .append("},decision=").append(sample.decision()).append('}');
        }
        return output.append("],aggregateDecision=")
                .append(record.aggregateDecision())
                .append(",outcomeKind=").append(record.outcomeKind())
                .append(",comparison=").append(record.comparison())
                .toString();
    }

    private static String pos(BlockPos position) {
        return "[" + position.getX() + "," + position.getY()
                + "," + position.getZ() + "]";
    }

    private static String vec(Vec3 vector) {
        return "[" + vector.x + "," + vector.y + "," + vector.z + "]";
    }

    private static final class LoggerHolder {
        private static final Logger LOGGER = LoggerFactory.getLogger(
                MundoZAntiXrayMod.MOD_ID
        );
    }

    record Configuration(
            boolean enabled,
            Identifier dimension,
            BlockPos target
    ) {
        Configuration {
            if (enabled) {
                Objects.requireNonNull(dimension, "dimension");
                target = Objects.requireNonNull(target, "target").immutable();
            }
        }

        static Configuration disabled() {
            return new Configuration(false, null, null);
        }
    }

    static final class Gate {
        private final Configuration configuration;
        private final AtomicBoolean claimed = new AtomicBoolean();

        Gate(Configuration configuration) {
            this.configuration = Objects.requireNonNull(
                    configuration,
                    "configuration"
            );
        }

        boolean isEnabled() {
            return configuration.enabled();
        }

        boolean matchesAndClaim(Identifier dimension, BlockPos target) {
            return configuration.enabled()
                    && configuration.dimension().equals(dimension)
                    && configuration.target().equals(target)
                    && claimed.compareAndSet(false, true);
        }
    }

    record TraceRecord(
            Identifier dimension,
            boolean playerCamera,
            Identifier cameraEntityType,
            int cameraEntityId,
            Vec3 cameraEye,
            BlockPos observerBlockPos,
            BlockPos target,
            String targetStateId,
            List<SampleRecord> samples,
            ObservationDecision aggregateDecision,
            boolean v1Hides,
            MinecraftRuntimeShadowEvaluator.DiagnosticKind outcomeKind,
            RuntimeDecisionComparison comparison
    ) {}

    record SampleRecord(
            int index,
            String kind,
            Vec3 endpoint,
            List<VisitedRecord> visited,
            ObservationContext context,
            ObservationDecision decision
    ) {}

    record VisitedRecord(
            BlockPos position,
            String stateId,
            String waterlogged,
            com.amadeu.mundozantixray.domain.model.ObservationPathBehavior behavior
    ) {}
}
