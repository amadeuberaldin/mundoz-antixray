package com.amadeu.mundozantixray;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.concurrent.atomic.AtomicReference;

public final class AntiXrayObfuscator {

    private static final int REVEAL_DISTANCE = 24;
    private static final int REVEAL_DISTANCE_SQUARED = REVEAL_DISTANCE * REVEAL_DISTANCE;

    private AntiXrayObfuscator() {
    }

    public static void writeSection(LevelChunkSection section, FriendlyByteBuf buf) {
        AntiXrayContext.Context context = AntiXrayContext.get();

        if (context == null) {
            section.write(buf);
            return;
        }

        if (!section.maybeHas(AntiXrayBlocks::isHiddenOre)) {
            section.write(buf);
            return;
        }

        BlockState replacement = findSafeReplacement(section);

        if (replacement == null) {
            section.write(buf);
            return;
        }

        LevelChunk chunk = context.chunk();
        LevelChunkSection fake = section.copy();

        int sectionIndex = findSectionIndex(chunk, section);

        if (sectionIndex < 0) {
            section.write(buf);
            return;
        }

        int sectionBaseY = chunk.getMinY() + (sectionIndex * 16);
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        AntiXrayShadowRuntime.SectionEvaluation shadowEvaluation =
                AntiXrayShadowRuntime.beginSection(section);

        boolean changed = false;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);

                    if (!AntiXrayBlocks.isHiddenOre(state)) {
                        continue;
                    }

                    int worldX = chunkMinX + x;
                    int worldY = sectionBaseY + y;
                    int worldZ = chunkMinZ + z;

                    if (AntiXrayBlocks.isOreLike(state)) {
                        if (isExposed(chunk, worldX, worldY, worldZ)) {
                            shadowEvaluation.compare(
                                    context.level(),
                                    context.player(),
                                    worldX,
                                    worldY,
                                    worldZ,
                                    state,
                                    false
                            );
                            continue;
                        }

                        shadowEvaluation.compare(
                                context.level(),
                                context.player(),
                                worldX,
                                worldY,
                                worldZ,
                                state,
                                true
                        );
                        fake.setBlockState(x, y, z, replacement, false);
                        changed = true;
                        continue;
                    }

                    if (AntiXrayBlocks.isStructureLike(state)) {
                        if (!isNearPlayer(context.player(), worldX, worldY, worldZ)) {
                            fake.setBlockState(x, y, z, replacement, false);
                            changed = true;
                            continue;
                        }

                        if (isExposed(chunk, worldX, worldY, worldZ)) {
                            continue;
                        }

                        fake.setBlockState(x, y, z, replacement, false);
                        changed = true;
                    }

                    fake.setBlockState(x, y, z, replacement, false);
                    changed = true;
                }
            }
        }

        if (!changed) {
            section.write(buf);
            return;
        }

        fake.recalcBlockCounts();
        fake.write(buf);
    }

    private static boolean isNearPlayer(ServerPlayer player, int x, int y, int z) {
        double dx = player.getX() - (x + 0.5D);
        double dy = player.getY() - (y + 0.5D);
        double dz = player.getZ() - (z + 0.5D);

        return (dx * dx + dy * dy + dz * dz) <= REVEAL_DISTANCE_SQUARED;
    }

    private static int findSectionIndex(LevelChunk chunk, LevelChunkSection target) {
        LevelChunkSection[] sections = chunk.getSections();

        for (int i = 0; i < sections.length; i++) {
            if (sections[i] == target) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isExposed(LevelChunk chunk, int x, int y, int z) {
        return isTransparentForXray(chunk, x + 1, y, z)
                || isTransparentForXray(chunk, x - 1, y, z)
                || isTransparentForXray(chunk, x, y + 1, z)
                || isTransparentForXray(chunk, x, y - 1, z)
                || isTransparentForXray(chunk, x, y, z + 1)
                || isTransparentForXray(chunk, x, y, z - 1);
    }

    private static boolean isTransparentForXray(LevelChunk chunk, int x, int y, int z) {
        int minY = chunk.getMinY();
        int maxY = minY + chunk.getHeight();

        if (y < minY || y >= maxY) {
            return false;
        }

        BlockState state = chunk.getLevel().getBlockState(new BlockPos(x, y, z));

        return state.isAir()
                || !state.canOcclude()
                || !state.getFluidState().isEmpty();
    }

    private static BlockState findSafeReplacement(LevelChunkSection section) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        BlockState endStone = Blocks.END_STONE.defaultBlockState();
        BlockState tuff = Blocks.TUFF.defaultBlockState();

        if (section.maybeHas(stone::equals))
            return stone;
        if (section.maybeHas(deepslate::equals))
            return deepslate;
        if (section.maybeHas(netherrack::equals))
            return netherrack;
        if (section.maybeHas(endStone::equals))
            return endStone;
        if (section.maybeHas(tuff::equals))
            return tuff;

        AtomicReference<BlockState> best = new AtomicReference<>();

        section.getStates().getAll(state -> {
            if (best.get() != null)
                return;
            if (state == null)
                return;
            if (state.isAir())
                return;
            if (!state.canOcclude())
                return;
            if (AntiXrayBlocks.isHiddenOre(state))
                return;

            if (state.is(Blocks.BEDROCK))
                return;
            if (state.is(Blocks.BARRIER))
                return;
            if (state.is(Blocks.OBSIDIAN))
                return;
            if (state.is(Blocks.CRYING_OBSIDIAN))
                return;

            best.set(state);
        });

        return best.get();
    }
}
