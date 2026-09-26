package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChunkAnalysisRuntime {
    private static final Map<Long, ChunkStats> STATS = new HashMap<>();
    private static final List<BlockPos> SPAWNERS = new ArrayList<>();
    private static final List<BlockPos> BEACONS = new ArrayList<>();
    private static final List<BlockPos> SHULKERS = new ArrayList<>();

    private static int ticks;
    private static int lastLoadedCount;

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            clear();
            return;
        }

        if (++ticks < 20) return;
        ticks = 0;

        int radius = 8;
        radius = Math.max(radius, ConfigManager.intOption("BaseFinder","radius",8));
        radius = Math.max(radius, ConfigManager.intOption("StashFinder","radius",8));
        radius = Math.max(radius, ConfigManager.intOption("SpawnerFinder","radius",6));
        radius = Math.max(radius, ConfigManager.intOption("Beacon Finder","radius",6));
        radius = Math.max(radius, ConfigManager.intOption("Shulker Finder","radius",6));
        radius = Math.max(radius, ConfigManager.intOption("ChestCounter","radius",6));

        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;

        Map<Long, ChunkStats> nextStats = new HashMap<>();
        List<BlockPos> nextSpawners = new ArrayList<>();
        List<BlockPos> nextBeacons = new ArrayList<>();
        List<BlockPos> nextShulkers = new ArrayList<>();
        int loaded = 0;

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                if (!client.level.hasChunk(cx, cz)) continue;
                loaded++;

                LevelChunk chunk = client.level.getChunk(cx, cz);
                int chests = 0;
                int barrels = 0;
                int shulkers = 0;
                int hoppers = 0;
                int furnaces = 0;
                int spawners = 0;
                int beacons = 0;
                int other = 0;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockEntityType<?> type = be.getType();

                    if (type == BlockEntityType.CHEST || type == BlockEntityType.TRAPPED_CHEST || type == BlockEntityType.ENDER_CHEST) {
                        chests++;
                    } else if (type == BlockEntityType.BARREL) {
                        barrels++;
                    } else if (type == BlockEntityType.SHULKER_BOX) {
                        shulkers++;
                        nextShulkers.add(be.getBlockPos().immutable());
                    } else if (type == BlockEntityType.HOPPER) {
                        hoppers++;
                    } else if (type == BlockEntityType.FURNACE || type == BlockEntityType.BLAST_FURNACE || type == BlockEntityType.SMOKER) {
                        furnaces++;
                    } else if (type == BlockEntityType.MOB_SPAWNER) {
                        spawners++;
                        nextSpawners.add(be.getBlockPos().immutable());
                    } else if (type == BlockEntityType.BEACON) {
                        beacons++;
                        nextBeacons.add(be.getBlockPos().immutable());
                    } else {
                        other++;
                    }
                }

                int storage = chests + barrels + shulkers;
                int machines = hoppers + furnaces;
                int score = storage * 4 + shulkers * 4 + hoppers * 3 + furnaces + spawners * 5 + beacons * 8 + Math.min(8, other);

                nextStats.put(key(cx, cz), new ChunkStats(cx, cz, storage, shulkers, machines, spawners, beacons, score));
            }
        }

        STATS.clear();
        STATS.putAll(nextStats);

        SPAWNERS.clear();
        SPAWNERS.addAll(nextSpawners);

        BEACONS.clear();
        BEACONS.addAll(nextBeacons);

        SHULKERS.clear();
        SHULKERS.addAll(nextShulkers);

        lastLoadedCount = loaded;
    }

    public static void renderWorld(WorldRenderContext context) {
        

        renderBlocks(context, "SpawnerFinder", SPAWNERS, 0xFFFFC857);
        renderBlocks(context, "Beacon Finder", BEACONS, 0xFF55E6B1);
        renderBlocks(context, "Shulker Finder", SHULKERS, 0xFFB46CFF);

        if (enabled("BaseFinder")) renderChunkScores(context, "BaseFinder", 20, 0xFFFF5D9E);
        if (enabled("StashFinder")) {
            int min = ConfigManager.intOption("StashFinder","minStorage",8);
            for (ChunkStats stats : STATS.values()) {
                if (stats.storage() < min) continue;
                renderChunk(context, stats, 0xFF55B8FF, 2.4f);
            }
        }

    }

    private static void renderChunkScores(WorldRenderContext context, String module, int minScore, int color) {
        for (ChunkStats stats : STATS.values()) {
            int threshold = ConfigManager.intOption(module,"storageWeight",5) * 3;
            if (stats.score() < Math.max(minScore, threshold)) continue;
            renderChunk(context, stats, color, 2.5f);
        }
    }

    private static void renderChunk(WorldRenderContext context, ChunkStats stats, int color, float width) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        double y = client.player.getY() - 0.18;
        double x = stats.chunkX() * 16.0;
        double z = stats.chunkZ() * 16.0;
        WorldBoxRenderer.box(context, new AABB(x, y, z, x + 16.0, y + 0.12, z + 16.0), color, 0.95f, width);
    }

    private static void renderBlocks(WorldRenderContext context, String module, List<BlockPos> positions, int color) {
        if (!enabled(module)) return;

        float width = ConfigManager.floatOption(module,"lineWidth",2.0f);
        int max = ConfigManager.intOption(module,"maxResults",96);
        int shown = 0;

        for (BlockPos p : positions) {
            WorldBoxRenderer.box(context,
                    new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1),
                    color, 0.98f, width);
            if (++shown >= max) break;
        }
    }

    public static void renderHud(GuiGraphics g) {
        

        renderFinderHud(g);
        renderChestCounter(g);
        renderChunkActivity(g);
    }

    private static void renderFinderHud(GuiGraphics g) {
        String active = null;
        int threshold = 0;

        if (enabled("BaseFinder")) {
            active = "BaseFinder";
            threshold = ConfigManager.intOption("BaseFinder","storageWeight",5) * 3;
        } else if (enabled("StashFinder")) {
            active = "StashFinder";
        }

        if (active == null) return;

        final String activeModule = active;
        final int scoreThreshold = threshold;

        List<ChunkStats> hits = STATS.values().stream()
                .filter(s -> switch (activeModule) {
                    case "StashFinder" -> s.storage() >= ConfigManager.intOption("StashFinder","minStorage",8);
                    default -> s.score() >= scoreThreshold;
                })
                .sorted(Comparator.comparingInt(ChunkStats::score).reversed())
                .limit(5)
                .toList();

        int w = 220;
        int h = 31 + hits.size() * 16;
        int x = 14;
        int y = 302;

        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, activeModule, x + 10, y + 8, 8.8f, 0xFFF3EEF5, true);

        for (int i = 0; i < hits.size(); i++) {
            ChunkStats s = hits.get(i);
            String text = "Chunk " + s.chunkX() + ", " + s.chunkZ() + " · Score " + s.score();
            SmoothTextRenderer.draw(g, text, x + 10, y + 27 + i * 16, 7.5f, 0xFFD5CBD9, false);
        }
    }

    private static void renderChestCounter(GuiGraphics g) {
        if (!enabled("ChestCounter")) return;

        int totalStorage = STATS.values().stream().mapToInt(ChunkStats::storage).sum();
        int totalShulkers = STATS.values().stream().mapToInt(ChunkStats::shulkers).sum();
        String text = "Storage " + totalStorage + " · Shulker " + totalShulkers;

        int tw = SmoothTextRenderer.width(text, 7.8f, true);
        int x = 14;
        int y = g.guiHeight() - 112;

        SmoothShapeRenderer.roundedRect(g, x, y, tw + 20, 28, 8, 0xE80D0912);
        SmoothTextRenderer.draw(g, text, x + 10, y + 8, 7.8f, 0xFFBDA8D0, true);
    }

    private static void renderChunkActivity(GuiGraphics g) {
        if (!enabled("ChunkActivity") || !ConfigManager.boolOption("ChunkActivity","hud",true)) return;

        String text = "Loaded chunks nearby " + lastLoadedCount;
        int tw = SmoothTextRenderer.width(text, 7.8f, true);
        int x = g.guiWidth() - tw - 14;
        int y = g.guiHeight() - 112;

        SmoothTextRenderer.draw(g, text, x, y, 7.8f, 0xFF9A8FA0, true);
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private static long key(int x, int z) {
        return ((long)x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static void clear() {
        STATS.clear();
        SPAWNERS.clear();
        BEACONS.clear();
        SHULKERS.clear();
        lastLoadedCount = 0;
    }

    private record ChunkStats(
            int chunkX,
            int chunkZ,
            int storage,
            int shulkers,
            int machines,
            int spawners,
            int beacons,
            int score
    ) {}

    private ChunkAnalysisRuntime() {}
}
