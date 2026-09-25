package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockSearchRuntime {
    private static final Map<Long, ChunkHit> AMETHYST_CHUNKS = new HashMap<>();
    private static final Map<Long, List<BlockPos>> NETHERITE = new HashMap<>();
    private static final Map<Long, List<BlockPos>> BLOCK_ESP = new HashMap<>();

    private static int tick;
    private static int cursor;
    private static int lastPlayerChunkX = Integer.MIN_VALUE;
    private static int lastPlayerChunkZ = Integer.MIN_VALUE;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.level == null || client.player == null) {
            clear();
            return;
        }

        boolean chunkFinder = enabled("Chunk Finder");
        boolean netheriteFinder = enabled("Netherite Finder");
        boolean blockEsp = enabled("BlockESP");

        if (!chunkFinder) AMETHYST_CHUNKS.clear();
        if (!netheriteFinder) NETHERITE.clear();
        if (!blockEsp) BLOCK_ESP.clear();

        if (!chunkFinder && !netheriteFinder && !blockEsp) return;

        int delay = chunkFinder
                ? ConfigManager.intOption("Chunk Finder", "scanDelay", 4)
                : 4;

        if (++tick < Math.max(1, delay)) return;
        tick = 0;

        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;

        int chunkRadius = 2;
        if (chunkFinder) chunkRadius = Math.max(chunkRadius, ConfigManager.intOption("Chunk Finder", "radius", 7));
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

        scanChunk(client, cx, cz, chunkFinder, netheriteFinder, blockEsp);
    }

    private static void scanChunk(Minecraft client, int cx, int cz, boolean chunkFinder, boolean netheriteFinder, boolean blockEsp) {
        long key = key(cx, cz);
        AMETHYST_CHUNKS.remove(key);
        NETHERITE.remove(key);
        BLOCK_ESP.remove(key);

        String targetName = ConfigManager.stringOption("BlockESP", "target", "Amethyst Cluster");
        int maxBlockEsp = ConfigManager.intOption("BlockESP", "maxResults", 192);
        int minClusters = ConfigManager.intOption("Chunk Finder", "minClusters", 1);

        List<BlockPos> netheriteFound = new ArrayList<>();
        List<BlockPos> blockEspFound = new ArrayList<>();

        int clusters = 0;
        BlockPos firstCluster = null;

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

                    if (chunkFinder && block == Blocks.AMETHYST_CLUSTER) {
                        clusters++;
                        if (firstCluster == null) firstCluster = pos.immutable();
                    }

                    if (netheriteFinder && y <= 40 && block == Blocks.ANCIENT_DEBRIS && netheriteFound.size() < 64) {
                        netheriteFound.add(pos.immutable());
                    }

                    if (blockEsp && blockEspFound.size() < 64 && matchesBlockEsp(block, targetName)) {
                        blockEspFound.add(pos.immutable());
                    }
                }
            }
        }

        if (chunkFinder && clusters >= minClusters && firstCluster != null) {
            AMETHYST_CHUNKS.put(key, new ChunkHit(cx, cz, clusters, firstCluster));
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
        if (!LicenseManager.verified() || client.player == null) return;

        if (enabled("Chunk Finder")) {
            double y = client.player.getY() - 0.15;
            for (ChunkHit hit : AMETHYST_CHUNKS.values()) {
                double x = hit.chunkX() * 16.0;
                double z = hit.chunkZ() * 16.0;
                WorldBoxRenderer.box(context, new AABB(x, y, z, x + 16.0, y + 0.12, z + 16.0),
                        0xFFB46CFF, 0.95f, 2.4f);

                BlockPos p = hit.firstCluster();
                WorldBoxRenderer.box(context, new AABB(p.getX(), p.getY(), p.getZ(), p.getX()+1, p.getY()+1, p.getZ()+1),
                        0xFFD18CFF, 1.0f, 2.2f);
            }
        }

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

    public static void renderHud(GuiGraphics g) {
        if (!enabled("Chunk Finder")
                || !LicenseManager.verified()
                || !ConfigManager.boolOption("Chunk Finder", "hud", true)) return;

        List<ChunkHit> hits = AMETHYST_CHUNKS.values().stream()
                .sorted(Comparator.comparingInt(ChunkHit::clusters).reversed())
                .limit(5)
                .toList();

        int h = 42 + hits.size() * 17;
        int x = 14;
        int y = 55;

        SmoothShapeRenderer.glow(g, x, y, 212, h, 10, 0x55B46CFF, 4);
        SmoothShapeRenderer.roundedRect(g, x, y, 212, h, 10, 0xE80D0912);
        SmoothTextRenderer.draw(g, "Chunk Finder · Amethyst", x + 11, y + 8, 9.2f, 0xFFF4EFF7, true);
        SmoothTextRenderer.draw(g, AMETHYST_CHUNKS.size() + " Chunk Treffer", x + 11, y + 24, 7.9f, 0xFFBFA7D7, false);

        for (int i = 0; i < hits.size(); i++) {
            ChunkHit hit = hits.get(i);
            String text = "Chunk " + hit.chunkX() + ", " + hit.chunkZ() + " · " + hit.clusters() + " Cluster";
            SmoothTextRenderer.draw(g, text, x + 11, y + 42 + i * 17, 7.9f, 0xFFDCCFE6, false);
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
        AMETHYST_CHUNKS.clear();
        NETHERITE.clear();
        BLOCK_ESP.clear();
    }

    private record ChunkHit(int chunkX, int chunkZ, int clusters, BlockPos firstCluster) {}

    private BlockSearchRuntime() {}
}
