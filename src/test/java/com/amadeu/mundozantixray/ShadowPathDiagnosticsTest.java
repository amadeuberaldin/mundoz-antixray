package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftRuntimeShadowEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowPathDiagnosticsTest {
    @Test
    void completePropertiesEnableExactTargetConfiguration() {
        var configuration = ShadowPathDiagnostics.resolveConfiguration(
                () -> "true",
                () -> "minecraft:the_end",
                () -> "12,64,-9"
        );

        assertTrue(configuration.enabled());
        assertTrue(configuration.dimension().equals(
                Identifier.parse("minecraft:the_end")));
        assertTrue(configuration.target().equals(new BlockPos(12, 64, -9)));
    }

    @Test
    void incompleteMalformedOrUnavailablePropertiesFailDisabled() {
        assertFalse(resolve("false", "minecraft:the_end", "12,64,-9").enabled());
        assertFalse(resolve("true", null, "12,64,-9").enabled());
        assertFalse(resolve("true", "minecraft:the_end", null).enabled());
        assertFalse(resolve("true", "minecraft:the_end", "12,64").enabled());
        assertFalse(resolve("true", "minecraft:the_end", "x,64,-9").enabled());
        assertFalse(ShadowPathDiagnostics.resolveConfiguration(
                () -> { throw new SecurityException("unavailable"); },
                () -> "minecraft:the_end",
                () -> "12,64,-9"
        ).enabled());
    }

    @Test
    void gateRequiresExactDimensionAndPositionAndClaimsOnlyOnce() {
        var configuration = resolve("true", "minecraft:the_end", "12,64,-9");
        var gate = new ShadowPathDiagnostics.Gate(configuration);

        assertFalse(gate.matchesAndClaim(
                Identifier.parse("minecraft:overworld"),
                new BlockPos(12, 64, -9)
        ));
        assertFalse(gate.matchesAndClaim(
                Identifier.parse("minecraft:the_end"),
                new BlockPos(13, 64, -9)
        ));
        assertTrue(gate.matchesAndClaim(
                Identifier.parse("minecraft:the_end"),
                new BlockPos(12, 64, -9)
        ));
        assertFalse(gate.matchesAndClaim(
                Identifier.parse("minecraft:the_end"),
                new BlockPos(12, 64, -9)
        ));
    }

    @Test
    void recordMakesObserverOriginChainAndCompleteDecisionVisible() {
        ObservationContext context = new ObservationContext(
                new BlockPosition(4, 70, -2),
                new BlockPosition(4, 67, -2),
                List.of(ObservationPathBehavior.PASS_THROUGH)
        );
        var visited = new ShadowPathDiagnostics.VisitedRecord(
                new BlockPos(4, 70, -2),
                "minecraft:copper_grate",
                "true",
                ObservationPathBehavior.PASS_THROUGH
        );
        var sample = new ShadowPathDiagnostics.SampleRecord(
                0,
                "CENTER",
                new Vec3(4.5D, 67.5D, -1.5D),
                List.of(visited),
                context,
                ObservationDecision.OBSERVED
        );
        var record = new ShadowPathDiagnostics.TraceRecord(
                Identifier.parse("minecraft:the_end"),
                true,
                Identifier.parse("minecraft:player"),
                42,
                new Vec3(4.5D, 70.62D, -1.5D),
                new BlockPos(4, 70, -2),
                new BlockPos(4, 67, -2),
                "minecraft:diamond_ore",
                List.of(sample),
                ObservationDecision.OBSERVED,
                true,
                MinecraftRuntimeShadowEvaluator.DiagnosticKind.COMPARABLE,
                RuntimeDecisionComparison.V1_HIDES_V2_REVEALS
        );

        String output = ShadowPathDiagnostics.format(record);
        assertTrue(output.contains("eye=[4.5,70.62,-1.5]"));
        assertTrue(output.contains("observerBlockPos=[4,70,-2]"));
        assertTrue(output.contains("firstVisited=[4,70,-2]"));
        assertTrue(output.contains("context={observer=[4,70,-2]"));
        assertTrue(output.contains("state=minecraft:copper_grate"));
        assertTrue(output.contains("waterlogged=true"));
        assertTrue(output.contains("decision=OBSERVED"));
        assertTrue(output.contains("aggregateDecision=OBSERVED"));
        assertTrue(output.contains("v1Hides=true"));
        assertTrue(output.contains("comparison=V1_HIDES_V2_REVEALS"));
    }

    private static ShadowPathDiagnostics.Configuration resolve(
            String enabled,
            String dimension,
            String target
    ) {
        return ShadowPathDiagnostics.resolveConfiguration(
                () -> enabled,
                () -> dimension,
                () -> target
        );
    }
}
