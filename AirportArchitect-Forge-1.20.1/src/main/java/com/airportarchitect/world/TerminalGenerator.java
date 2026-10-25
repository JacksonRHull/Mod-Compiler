package com.airportarchitect.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Deterministic Phase 1 terminal generator. Coordinates are relative to the northwest floor corner. */
public final class TerminalGenerator {
    private TerminalGenerator() { }

    public static void generate(ServerLevel level, BlockPos origin, int width, int length, int height) {
        // Defensive limits keep packet-driven generation bounded.
        width = Math.max(12, Math.min(40, width));
        length = Math.max(16, Math.min(54, length));
        height = Math.max(7, Math.min(12, height));
        BlockState floor = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState frame = Blocks.QUARTZ_PILLAR.defaultBlockState();
        BlockState wall = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState roof = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState accent = Blocks.SMOOTH_STONE.defaultBlockState();

        // Main floor slab.
        fill(level, origin, 0, 0, 0, width - 1, 0, length - 1, floor);
        // Exterior side walls: solid lower band, glass above it.
        for (int z = 0; z < length; z++) {
            set(level, origin, 0, 1, z, wall);
            set(level, origin, width - 1, 1, z, wall);
            for (int y = 2; y < height - 1; y++) {
                set(level, origin, 0, y, z, glass);
                set(level, origin, width - 1, y, z, glass);
            }
        }
        for (int x = 0; x < width; x++) {
            set(level, origin, x, 1, 0, wall);
            set(level, origin, x, 1, length - 1, wall);
            for (int y = 2; y < height - 1; y++) {
                set(level, origin, x, y, 0, glass);
                set(level, origin, x, y, length - 1, glass);
            }
        }
        // Structural columns at regular intervals.
        for (int x = 0; x < width; x += 6) {
            for (int z = 0; z < length; z += 6) {
                fill(level, origin, x, 1, z, x, height - 1, z, frame);
            }
        }
        // Flat modern roof with a raised central spine and skylight strip.
        fill(level, origin, 0, height, 0, width - 1, height, length - 1, roof);
        int centerX = width / 2;
        fill(level, origin, Math.max(1, centerX - 1), height + 1, 1,
                Math.min(width - 2, centerX + 1), height + 1, length - 2, accent);
        for (int z = 2; z < length - 2; z++) {
            set(level, origin, centerX, height + 1, z, glass);
        }
        // Check-in counters along the landside end and repeated gate lounge islands.
        int counterZ = 4;
        for (int x = 3; x < width - 3; x += 4) {
            fill(level, origin, x, 1, counterZ, Math.min(x + 2, width - 3), 2, counterZ + 1, accent);
        }
        int gateStart = Math.max(5, length - 13);
        for (int x = 3; x < width - 3; x += 6) {
            fill(level, origin, x, 1, gateStart, Math.min(x + 2, width - 3), 1, gateStart + 2, Blocks.IRON_BLOCK.defaultBlockState());
            // Gate-number-style marker block above each lounge island.
            set(level, origin, x + 1, 2, gateStart + 1, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Entry opening on the short front wall, with a simple canopy.
        int entryX = width / 2;
        for (int y = 1; y < Math.min(4, height); y++) {
            for (int dx = -1; dx <= 1; dx++) set(level, origin, entryX + dx, y, 0, Blocks.AIR.defaultBlockState());
        }
        fill(level, origin, entryX - 2, 4, 0, entryX + 2, 4, 3, roof);
    }

    private static void fill(ServerLevel level, BlockPos origin, int x1, int y1, int z1,
                             int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) set(level, origin, x, y, z, state);
    }

    private static void set(ServerLevel level, BlockPos origin, int x, int y, int z, BlockState state) {
        BlockPos target = origin.offset(x, y, z);
        if (level.isInWorldBounds(target) && level.hasChunkAt(target)) {
            level.setBlock(target, state, 3);
        }
    }
}
