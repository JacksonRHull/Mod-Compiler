package com.airportarchitect.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** Builds a terminal from a compact, validated room-grid blueprint. */
public final class TerminalGenerator {
    private static final int CELL_SIZE = 4;
    private static final int WALL_HEIGHT = 5;
    private TerminalGenerator() { }

    public static int generatePlan(ServerLevel level, BlockPos origin, byte[] plan, int cols, int rows) {
        int zones = 0;
        int width = cols * CELL_SIZE;
        int length = rows * CELL_SIZE;
        BlockState floor = modBlock("createdeco:andesite_sheet_block", Blocks.POLISHED_ANDESITE);
        BlockState wall = modBlock("createdeco:industrial_iron_block", Blocks.LIGHT_GRAY_CONCRETE);
        BlockState trim = modBlock("createdeco:industrial_iron_bars", Blocks.IRON_BARS);
        BlockState glass = modBlock("yuushya:clear_glass", Blocks.GLASS);
        BlockState roof = modBlock("createdeco:industrial_iron_block", Blocks.QUARTZ_BLOCK);
        BlockState column = modBlock("createdeco:industrial_iron_pillar", Blocks.QUARTZ_PILLAR);
        BlockState lights = modBlock("minecraft:sea_lantern", Blocks.SEA_LANTERN);
        BlockState counter = modBlock("handcrafted:counter", Blocks.SMOOTH_STONE_SLAB);
        BlockState seat = modBlock("handcrafted:chair", Blocks.GRAY_WOOL);
        BlockState baggage = modBlock("create:depot", Blocks.BARREL);

        // Only build the footprint the player explicitly painted; no surprise giant rectangle.
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) {
            int type = plan[r * cols + c];
            if (type <= 0 || type > 8) continue;
            zones++;
            int x1 = c * CELL_SIZE, z1 = r * CELL_SIZE;
            int x2 = x1 + CELL_SIZE - 1, z2 = z1 + CELL_SIZE - 1;
            fill(level, origin, x1, 0, z1, x2, 0, z2, floor);
            // Per-zone walls only where the neighboring cell is empty or a different room.
            if (r == 0 || plan[(r - 1) * cols + c] != type) wallRun(level, origin, x1, z1, x2, z1, type, r == 0 ? 0 : plan[(r - 1) * cols + c], wall, glass, trim);
            if (r == rows - 1 || plan[(r + 1) * cols + c] != type) wallRun(level, origin, x1, z2, x2, z2, type, r == rows - 1 ? 0 : plan[(r + 1) * cols + c], wall, glass, trim);
            if (c == 0 || plan[r * cols + c - 1] != type) wallRun(level, origin, x1, z1, x1, z2, type, c == 0 ? 0 : plan[r * cols + c - 1], wall, glass, trim);
            if (c == cols - 1 || plan[r * cols + c + 1] != type) wallRun(level, origin, x2, z1, x2, z2, type, c == cols - 1 ? 0 : plan[r * cols + c + 1], wall, glass, trim);
            // Pillars at zone corners and a high ceiling strip for a modern terminal feel.
            for (int y = 1; y <= WALL_HEIGHT; y++) {
                set(level, origin, x1, y, z1, column);
                set(level, origin, x2, y, z1, column);
                set(level, origin, x1, y, z2, column);
                set(level, origin, x2, y, z2, column);
            }
            fill(level, origin, x1, WALL_HEIGHT + 1, z1, x2, WALL_HEIGHT + 1, z2, roof);
            set(level, origin, x1 + 1, WALL_HEIGHT + 1, z1 + 1, lights);
            set(level, origin, x2 - 1, WALL_HEIGHT + 1, z2 - 1, lights);
            furnish(level, origin, type, x1, z1, counter, seat, baggage, lights);
        }
        // Continuous glazed rooflights along the central spine where a zone exists.
        int centerX = width / 2;
        for (int z = 0; z < length; z++) {
            int cell = (z / CELL_SIZE) * cols + Math.min(cols - 1, centerX / CELL_SIZE);
            if (cell >= 0 && cell < plan.length && plan[cell] != 0) {
                set(level, origin, centerX, WALL_HEIGHT + 2, z, glass);
                set(level, origin, centerX + 1, WALL_HEIGHT + 2, z, glass);
            }
        }
        return zones;
    }

    private static void wallRun(ServerLevel level, BlockPos origin, int x1, int z1, int x2, int z2,
                                int type, int neighborType, BlockState wall, BlockState glass, BlockState trim) {
        boolean horizontal = z1 == z2;
        int length = horizontal ? Math.abs(x2 - x1) : Math.abs(z2 - z1);
        int doorAt = length / 2;
        for (int i = 0; i <= length; i++) {
            int x = horizontal ? Math.min(x1, x2) + i : x1;
            int z = horizontal ? z1 : Math.min(z1, z2) + i;
            boolean doorway = neighborType != 0 && (type == 1 || type == 5 || neighborType == 1 || neighborType == 5) && i == doorAt;
            if (doorway) continue;
            set(level, origin, x, 1, z, wall);
            for (int y = 2; y <= WALL_HEIGHT; y++) set(level, origin, x, y, z, y >= 3 ? glass : wall);
            if (i % 2 == 0) set(level, origin, x, WALL_HEIGHT, z, trim);
        }
    }

    private static void furnish(ServerLevel level, BlockPos origin, int type, int x, int z,
                                BlockState counter, BlockState seat, BlockState baggage, BlockState lights) {
        switch (type) {
            case 2 -> { // check-in counters
                fill(level, origin, x + 1, 1, z + 1, x + 2, 1, z + 1, counter);
                set(level, origin, x + 1, 2, z + 1, lights);
            }
            case 3 -> { // security lanes
                set(level, origin, x + 1, 1, z + 1, Blocks.IRON_BARS.defaultBlockState());
                set(level, origin, x + 2, 1, z + 1, Blocks.IRON_BARS.defaultBlockState());
                set(level, origin, x + 1, 1, z + 2, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState());
            }
            case 4 -> { // retail / concessions
                fill(level, origin, x + 1, 1, z + 1, x + 2, 1, z + 2, counter);
                set(level, origin, x + 1, 2, z + 1, lights);
            }
            case 5 -> { // gate lounge seating
                set(level, origin, x + 1, 1, z + 1, seat);
                set(level, origin, x + 2, 1, z + 1, seat);
                set(level, origin, x + 1, 1, z + 2, seat);
            }
            case 6 -> { // baggage claim
                fill(level, origin, x + 1, 1, z + 1, x + 2, 1, z + 2, baggage);
            }
            case 7 -> { // restrooms get sinks / fixtures rather than empty rooms
                set(level, origin, x + 1, 1, z + 1, Blocks.QUARTZ_STAIRS.defaultBlockState());
                set(level, origin, x + 2, 1, z + 1, Blocks.QUARTZ_STAIRS.defaultBlockState());
            }
            case 8 -> set(level, origin, x + 1, 1, z + 1, Blocks.BARREL.defaultBlockState());
            default -> { }
        }
    }

    private static BlockState modBlock(String id, Block fallback) {
        Block found = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
        return found == null || found == Blocks.AIR ? fallback.defaultBlockState() : found.defaultBlockState();
    }
    private static void fill(ServerLevel level, BlockPos origin, int x1, int y1, int z1,
                             int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) set(level, origin, x, y, z, state);
    }
    private static void set(ServerLevel level, BlockPos origin, int x, int y, int z, BlockState state) {
        BlockPos target = origin.offset(x, y, z);
        if (level.isInWorldBounds(target) && level.hasChunkAt(target)) level.setBlock(target, state, 3);
    }
}
