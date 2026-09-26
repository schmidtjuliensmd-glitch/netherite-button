package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockSearchRuntime {
    private static final Map<Long, List<BlockPos>> NETHERITE = new HashMap<>();
    private static final Map<Long, List<BlockPos>> BLOCK_ESP = new HashMap<>();

    private static int tick;
    private static int cursor;
    private static int lastPlayerChunkX = Integer.MIN_VALUE;
    private static int lastPlayerChunkZ = Integer.MIN_VALUE;

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            clear();
            return;
        }

        boolean netheriteFinder = enabled("Netherite Finder");
        boolean blockEsp = enabled("BlockESP");

        if (!netheriteFinder) NETHERITE.clear();
        if (!blockEsp) BLOCK_ESP.clear();

        if (!netheriteFinder && !blockEsp) return;

        int delay = 4;

        if (++tick < Math.max(1, delay)) return;
        tick = 0;

        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;

        int chunkRadius = 2;
        if (netheriteFinder) chunkRadius = Math.max(chunkRadius, ConfigManager.intOption("Netherite Finder", "radius", 5));
        if (blockEsp) {
            int blockRange = ConfigManager.intOption("BlockESP", "range", 32);
            chunkRadius = Math.max(chunkRadius, Math.max(1, (blockRange + 15) / 16));
        }

        if (pcx != lastPlayerChunkX || pcz != lastPlayerChunkZ) {
            lastPlayerChunkX = pcx;
            lastPlayerChunkZ = pcz;
            cursor = 0;
        }

        int diameter = chunkRadius * 2 + 1;
        int total = diameter * diameter;
        if (total <= 0) return;

        int index = Math.floorMod(cursor++, total);
        int ox = index % diameter - chunkRadius;
        int oz = index / diameter - chunkRadius;
        int cx = pcx + ox;
        int cz = pcz + oz;

        if (!client.level.hasChunk(cx, cz)) return;

        scanChunk(client, cx, cz, netheriteFinder, blockEsp);
    }

    private static void scanChunk(Minecraft client, int cx, int cz, boolean netheriteFinder, boolean blockEsp) {
        long key = key(cx, cz);
        NETHERITE.remove(key);
        BLOCK_ESP.remove(key);

        String targetName = ConfigManager.stringOption("BlockESP", "target", "Amethyst Cluster");
        int maxBlockEsp = ConfigManager.intOption("BlockESP", "maxResults", 192);

        List<BlockPos> netheriteFound = new ArrayList<>();
        List<BlockPos> blockEspFound = new ArrayList<>();


        int minY = -64;
        int maxY = 96;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = cx << 4;
        int startZ = cz << 4;

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = client.level.getBlockState(pos);
                    Block block = state.getBlock();

                    if (netheriteFinder && y <= 40 && block == Blocks.ANCIENT_DEBRIS && netheriteFound.size() < 64) {
                        netheriteFound.add(pos.immutable());
                    }

                    if (blockEsp && blockEspFound.size() < 64 && matchesBlockEsp(block, targetName)) {
                        blockEspFound.add(pos.immutable());
                    }
                }
            }
        }

        if (netheriteFinder && !netheriteFound.isEmpty()) NETHERITE.put(key, netheriteFound);

        if (blockEsp && !blockEspFound.isEmpty()) {
            int current = BLOCK_ESP.values().stream().mapToInt(List::size).sum();
            if (current < maxBlockEsp) BLOCK_ESP.put(key, blockEspFound);
        }
    }

    private static boolean matchesBlockEsp(Block block, String target) {
        return switch (target) {
            case "Ancient Debris" -> block == Blocks.ANCIENT_DEBRIS;
            case "Diamond Ore" -> block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE;
            case "Spawner" -> block == Blocks.SPAWNER;
            default -> block == Blocks.AMETHYST_CLUSTER;
        };
    }

    public static void renderWorld(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (enabled("Netherite Finder")) {
            float width = ConfigManager.floatOption("Netherite Finder", "lineWidth", 2.2f);
            int max = ConfigManager.intOption("Netherite Finder", "maxResults", 192);
            int shown = 0;
            outer:
            for (List<BlockPos> list : NETHERITE.values()) {
                for (BlockPos p : list) {
                    WorldBoxRenderer.box(context, new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1),
                            0xFFFF8A4F, 0.98f, width);
                    if (++shown >= max) break outer;
                }
            }
        }

        if (enabled("BlockESP")) {
            float width = ConfigManager.floatOption("BlockESP", "lineWidth", 2.0f);
            int max = ConfigManager.intOption("BlockESP", "maxResults", 192);
            int shown = 0;
            outer:
            for (List<BlockPos> list : BLOCK_ESP.values()) {
                for (BlockPos p : list) {
                    WorldBoxRenderer.box(context, new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1),
                            ThemeConfig.accent, 0.95f, width);
                    if (++shown >= max) break outer;
                }
            }
        }
    }

    private static boolean enabled(String name) {
        Module module = ModuleRegistry.find(name);
        return module != null && module.enabled();
    }

    private static long key(int x, int z) {
        return ((long)x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static void clear() {
        NETHERITE.clear();
        BLOCK_ESP.clear();
    }


    private BlockSearchRuntime() {}
}
