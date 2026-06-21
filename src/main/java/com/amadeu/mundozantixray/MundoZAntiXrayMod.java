package com.amadeu.mundozantixray;

import net.fabricmc.api.ModInitializer;

public class MundoZAntiXrayMod implements ModInitializer {

    public static final String MOD_ID = "mundoz_antixray";

    @Override
    public void onInitialize() {
        AntiXrayRevealer.register();
        System.out.println("[MundoZ AntiXray] Inicializado.");
    }
}
