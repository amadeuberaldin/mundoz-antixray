package com.amadeu.mundozantixray;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

public final class AntiXrayContext {

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private AntiXrayContext() {}

    public static void set(ServerPlayer player, ServerLevel level, LevelChunk chunk) {
        CURRENT.set(new Context(player, level, chunk));
    }

    public static Context get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record Context(ServerPlayer player, ServerLevel level, LevelChunk chunk) {}
}
