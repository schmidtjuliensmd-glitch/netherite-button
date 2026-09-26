package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import net.minecraft.world.level.block.Block;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shared, evidence-based scan for the four amethyst chunk finders. */
public final class SusChunkFinder {
    private static final String[] MODULES = {
            "ChunkFinder", "Sus Chunk Finder", "ChunkFinderV2", "SusChunkFinderV2"
    };
    private static final int MAX_WITNESSES = 64; // More than the largest configurable minimum (16).
    private static final Map<Block, Integer> BLOCK_STAGES = new IdentityHashMap<>();
    private static final Map<Long, Evidence> HITS = new HashMap<>();
    private static final List<Offset> ORDER = new ArrayList<>();
    private static ClientLevel scannedLevel;
    private static int ticks, cursor, scanRadius = -1;
    private static int previousX = Integer.MIN_VALUE, previousZ = Integer.MIN_VALUE;

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            clear();
            return;
        }
        if (scannedLevel != client.level) {
            clear();
            scannedLevel = client.level;
        }
        List<String> active = activeModules();
        if (active.isEmpty()) { clear(); return; }

        int radius = active.stream().mapToInt(SusChunkFinder::radius).max().orElse(7);
        int delay = active.stream().mapToInt(name -> clamp(
                ConfigManager.intOption(name, "scanDelay", 4), 1, 20)).min().orElse(4);
        int cx = client.player.blockPosition().getX() >> 4;
        int cz = client.player.blockPosition().getZ() >> 4;

        // Revalidate actual block positions, not just an old score or an old chunk coordinate.
        HITS.values().removeIf(hit -> {
            if (!AmethystRules.withinRadius(hit.x, hit.z, cx, cz, radius)
                    || !client.level.hasChunk(hit.x, hit.z)
                    || client.level.getChunk(hit.x, hit.z) != hit.chunk) return true;
            hit.buds.removeIf(pos -> stage(hit.chunk.getBlockState(pos)) == 0);
            return hit.buds.isEmpty();
        });

        if (scanRadius != radius) {
            scanRadius = radius;
            cursor = 0;
            ORDER.clear();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) ORDER.add(new Offset(x, z));
            }
            ORDER.sort(Comparator.comparingInt(p -> p.x * p.x + p.z * p.z));
            ticks = delay;
        }

        if (++ticks < delay) return;
        ticks = 0;
        if (cx != previousX || cz != previousZ) {
            previousX = cx; previousZ = cz;
            if (client.level.hasChunk(cx, cz)) scan(client.level.getChunk(cx, cz), cx, cz);
        }

        // Scan nearby chunks first, skip unloaded chunks, and keep work bounded per tick.
        int scanned = 0;
        long deadline = System.nanoTime() + 2_000_000L;
        for (int attempted = 0; attempted < ORDER.size() && scanned < 8; attempted++) {
            Offset offset = ORDER.get(Math.floorMod(cursor++, ORDER.size()));
            int x = cx + offset.x, z = cz + offset.z;
            if (!client.level.hasChunk(x, z)) continue;
            scan(client.level.getChunk(x, z), x, z);
            scanned++;
            if (System.nanoTime() >= deadline) break;
        }
    }

    private static void scan(LevelChunk chunk, int chunkX, int chunkZ) {
        List<BlockPos> buds = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        outer:
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir() || !section.maybeHas(state -> stage(state) > 0)) continue;
            int baseY = chunk.getMinY() + sectionIndex * 16;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (stage(section.getBlockState(x, y, z)) == 0) continue;
                        buds.add(new BlockPos(chunkX * 16 + x, baseY + y, chunkZ * 16 + z));
                        if (buds.size() >= MAX_WITNESSES) break outer;
                    }
                }
            }
        }
        long key = key(chunkX, chunkZ);
        if (buds.isEmpty()) HITS.remove(key);
        else HITS.put(key, new Evidence(chunkX, chunkZ, chunk, buds));
    }

    private static int stage(BlockState state) {
        return BLOCK_STAGES.computeIfAbsent(state.getBlock(),
                block -> AmethystRules.stage(BuiltInRegistries.BLOCK.getKey(block).toString()));
    }

    private static List<Evidence> hits(String module) {
        Minecraft client = Minecraft.getInstance();
        if (!enabled(module) || client.player == null
                || client.level == null || client.level != scannedLevel) return List.of();
        int cx = client.player.blockPosition().getX() >> 4;
        int cz = client.player.blockPosition().getZ() >> 4;
        int radius = radius(module);
        int minimum = clamp(ConfigManager.intOption(module, "minClusters", 1), 1, 16);
        return HITS.values().stream()
                .filter(hit -> AmethystRules.matches(hit.buds.size(), minimum)
                        && AmethystRules.withinRadius(hit.x, hit.z, cx, cz, radius)
                        && client.level.hasChunk(hit.x, hit.z)
                        && client.level.getChunk(hit.x, hit.z) == hit.chunk)
                .sorted(Comparator.comparingDouble(hit -> Math.hypot(hit.x - cx, hit.z - cz)))
                .toList();
    }

    public static void renderWorld(WorldRenderContext context) {
        Set<Plate> drawn = new HashSet<>();
        for (String module : activeModules()) {
            int height = clamp(ConfigManager.intOption(module, "plateHeight", 64), -64, 320);
            for (Evidence hit : hits(module)) {
                Plate plate = new Plate(hit.x, hit.z, height);
                if (drawn.add(plate)) ChunkPlateRenderer.draw(context, hit.x, hit.z, height);
            }
        }
    }

    public static void renderHud(GuiGraphics g) {
        int y = 14;
        for (String module : activeModules()) {
            if (!ConfigManager.boolOption(module, "hud", true)) continue;
            List<Evidence> hits = hits(module);
            if (hits.isEmpty()) continue; // No alert without a detected amethyst growth block.
            int visible = Math.min(3, hits.size());
            int h = 43 + visible * 16;
            if (y + h > g.guiHeight() - 8) break;
            SmoothShapeRenderer.roundedRect(g, 14, y, 236, h, 10, 0xE80D0912);
            SmoothTextRenderer.draw(g, module, 25, y + 8, 10f, 0xFFF5F1F7, true);
            int height = clamp(ConfigManager.intOption(module, "plateHeight", 64), -64, 320);
            SmoothTextRenderer.draw(g, hits.size() + " Chunk-Treffer · Fläche Y " + height,
                    25, y + 25, 8f, 0xFFFF6666, false);
            for (int i = 0; i < visible; i++) {
                Evidence hit = hits.get(i);
                String amount = hit.buds.size() >= MAX_WITNESSES ? "64+" : Integer.toString(hit.buds.size());
                String row = "Chunk " + hit.x + ", " + hit.z + " · " + amount + " Amethyst";
                SmoothTextRenderer.draw(g, row, 25, y + 42 + i * 16, 8f, 0xFFD7CEDA, false);
            }
            y += h + 7;
        }
    }

    // Keep the existing public accessor available to other client integrations.
    public static List<Result> results() {
        Minecraft client = Minecraft.getInstance();
        return hits("Sus Chunk Finder").stream().map(hit -> new Result(hit.x, hit.z, hit.buds.size(), 0, 0,
                Math.hypot(hit.x - (client.player.blockPosition().getX() >> 4),
                        hit.z - (client.player.blockPosition().getZ() >> 4)))).toList();
    }

    private static List<String> activeModules() {
        List<String> active = new ArrayList<>();
        for (String name : MODULES) if (enabled(name)) active.add(name);
        return active;
    }

    private static int radius(String name) {
        return clamp(ConfigManager.intOption(name, "radius", name.endsWith("V2") ? 8 : 7), 2, 12);
    }

    private static boolean enabled(String name) {
        Module module = ModuleRegistry.find(name);
        return module != null && module.enabled();
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xFFFFFFFFL); }

    private static void clear() {
        HITS.clear(); ORDER.clear(); scannedLevel = null;
        ticks = 0; cursor = 0; scanRadius = -1;
        previousX = Integer.MIN_VALUE; previousZ = Integer.MIN_VALUE;
    }

    private record Offset(int x, int z) {}
    private record Plate(int x, int z, int y) {}
    private record Evidence(int x, int z, LevelChunk chunk, List<BlockPos> buds) {}
    public record Result(int chunkX, int chunkZ, int score, int storageCount, int machineCount, double distance) {}

    private SusChunkFinder() {}
}
