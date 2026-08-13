package com.amadeu.mundozantixray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MundoZAntiXrayMod implements ModInitializer {

    public static final String MOD_ID = "mundoz_antixray";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AntiXrayRevealer.register();
        registerShadowValidationReporter();
        System.out.println("[MundoZ AntiXray] Inicializado.");
    }

    private static void registerShadowValidationReporter() {
        if (!ShadowRuntimeDiagnostics.isEnabled()) {
            return;
        }

        try {
            ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
                try {
                    if (!server.isDedicatedServer()) {
                        return;
                    }
                    ValidationReporterHolder.INSTANCE.report(
                            LOGGER,
                            ShadowRuntimeDiagnostics.aggregate().snapshot()
                    );
                } catch (Throwable diagnosticFailure) {
                    // Validation reporting must never affect server shutdown.
                }
            });
        } catch (Throwable diagnosticFailure) {
            // Validation reporting must never affect server initialization.
        }
    }

    private static final class ValidationReporterHolder {
        private static final ShadowRuntimeValidationReporter INSTANCE =
                new ShadowRuntimeValidationReporter();
    }
}
