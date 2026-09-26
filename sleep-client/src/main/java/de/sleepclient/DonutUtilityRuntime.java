package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class DonutUtilityRuntime {
    private static Vec3 lastPosition;
    private static int rtpScanTicks;
    private static int rtpTickCounter;
    private static String rtpStatus = "";
    private static int rtpStorageCount;
    private static int rtpNearbyPlayers;

    private static boolean spawnerDanger;
    private static int spawnerCooldown;

    private static Long cachedSeed;
    private static boolean seedCopied;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player == null || client.level == null) {
            lastPosition = null;
            rtpScanTicks = 0;
            spawnerDanger = false;
            return;
        }

        tickRtp(client);
        tickSpawnerProtect(client);
        tickSeedAnalyzer(client);
    }

    private static void tickRtp(Minecraft client) {
        Module module = ModuleRegistry.find("RTP Base Alert");
        if (module == null || !module.enabled()) {
            lastPosition = client.player.position();
            rtpScanTicks = 0;
            rtpStatus = "";
            return;
        }

        Vec3 now = client.player.position();
        int trigger = ConfigManager.intOption("RTP Base Alert", "teleportDistance", 256);

        if (lastPosition != null && now.distanceTo(lastPosition) >= trigger) {
            int seconds = ConfigManager.intOption("RTP Base Alert", "scanSeconds", 30);
            rtpScanTicks = seconds * 20;
            rtpStatus = "RTP erkannt · Umgebung wird geprüft";
            rtpStorageCount = 0;
            rtpNearbyPlayers = 0;
        }
        lastPosition = now;

        if (rtpScanTicks <= 0) return;
        rtpScanTicks--;

        if (++rtpTickCounter < 20) return;
        rtpTickCounter = 0;

        int storage = 0;
        int radiusChunks = 4;
        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;

        for (int cx = pcx - radiusChunks; cx <= pcx + radiusChunks; cx++) {
            for (int cz = pcz - radiusChunks; cz <= pcz + radiusChunks; cz++) {
                if (!client.level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = client.level.getChunk(cx, cz);

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockEntityType<?> type = be.getType();
                    if (type == BlockEntityType.CHEST
                            || type == BlockEntityType.TRAPPED_CHEST
                            || type == BlockEntityType.BARREL
                            || type == BlockEntityType.SHULKER_BOX
                            || type == BlockEntityType.HOPPER
                            || type == BlockEntityType.ENDER_CHEST) {
                        storage++;
                    }
                }
            }
        }

        int players = 0;
        for (AbstractClientPlayer other : client.level.players()) {
            if (other == client.player || other.isRemoved()) continue;
            if (client.player.distanceTo(other) <= 96.0f) players++;
        }

        rtpStorageCount = storage;
        rtpNearbyPlayers = players;

        int threshold = ConfigManager.intOption("RTP Base Alert", "storageThreshold", 4);
        boolean playerAlert = ConfigManager.boolOption("RTP Base Alert", "playerAlert", true);

        if (storage >= threshold || (playerAlert && players > 0)) {
            rtpStatus = "Basis-Hinweis · " + storage + " Storage · " + players + " Spieler";
        } else {
            int secondsLeft = Math.max(0, rtpScanTicks / 20);
            rtpStatus = "RTP Scan · " + secondsLeft + "s";
        }
    }

    private static void tickSpawnerProtect(Minecraft client) {
        Module module = ModuleRegistry.find("SpawnerProtect");
        if (module == null || !module.enabled()) {
            spawnerDanger = false;
            spawnerCooldown = 0;
            return;
        }

        if (spawnerCooldown > 0) spawnerCooldown--;

        int spawnerRange = ConfigManager.intOption("SpawnerProtect", "spawnerRange", 20);
        int playerRange = ConfigManager.intOption("SpawnerProtect", "playerRange", 48);

        BlockPos playerPos = client.player.blockPosition();
        boolean nearSpawner = false;

        int radiusChunks = Math.max(1, (spawnerRange + 15) / 16);
        int pcx = playerPos.getX() >> 4;
        int pcz = playerPos.getZ() >> 4;

        outer:
        for (int cx = pcx - radiusChunks; cx <= pcx + radiusChunks; cx++) {
            for (int cz = pcz - radiusChunks; cz <= pcz + radiusChunks; cz++) {
                if (!client.level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = client.level.getChunk(cx, cz);

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.getType() != BlockEntityType.MOB_SPAWNER) continue;
                    BlockPos p = be.getBlockPos();
                    if (client.player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)
                            <= spawnerRange * (double)spawnerRange) {
                        nearSpawner = true;
                        break outer;
                    }
                }
            }
        }

        boolean playerClose = false;
        for (AbstractClientPlayer other : client.level.players()) {
            if (other == client.player || other.isRemoved()) continue;
            if (client.player.distanceToSqr(other) <= playerRange * (double)playerRange) {
                playerClose = true;
                break;
            }
        }

        spawnerDanger = nearSpawner && playerClose;

        if (spawnerDanger
                && ConfigManager.boolOption("SpawnerProtect", "disconnect", false)
                && spawnerCooldown <= 0
                && client.getConnection() != null) {
            spawnerCooldown = 200;
            client.getConnection().getConnection().disconnect(
                    Component.literal("Sleep Client SpawnerProtect: Spieler in der Nähe.")
            );
        }
    }

    private static void tickSeedAnalyzer(Minecraft client) {
        Module module = ModuleRegistry.find("SeedAnalyzer");
        if (module == null || !module.enabled()) {
            cachedSeed = null;
            seedCopied = false;
            return;
        }

        MinecraftServer server = client.getSingleplayerServer();
        if (server == null) {
            cachedSeed = null;
            return;
        }

        long seed = server.overworld().getSeed();
        cachedSeed = seed;

        if (ConfigManager.boolOption("SeedAnalyzer", "copyClipboard", false) && !seedCopied) {
            client.keyboardHandler.setClipboard(Long.toString(seed));
            seedCopied = true;
        }
    }

    public static void renderWorld(WorldRenderContext context) {
        renderDeepPlayers(context);
        renderElytra(context);
    }

    private static void renderDeepPlayers(WorldRenderContext context) {
        Module module = ModuleRegistry.find("Deep Player Finder");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        int range = ConfigManager.intOption("Deep Player Finder", "range", 128);
        int maxY = ConfigManager.intOption("Deep Player Finder", "maxY", 32);

        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player || player.isRemoved()) continue;
            if (player.getY() > maxY) continue;
            if (client.player.distanceToSqr(player) > range * (double)range) continue;

            WorldBoxRenderer.box(
                    context,
                    player.getBoundingBox().inflate(0.08),
                    0xFFFF5D9E,
                    0.98f,
                    2.6f
            );
        }
    }

    private static void renderElytra(WorldRenderContext context) {
        Module module = ModuleRegistry.find("Auto Elytra Finder");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        int range = ConfigManager.intOption("Auto Elytra Finder", "searchRadius", 512);
        double maxSq = range * (double)range;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof ItemFrame frame)) continue;
            if (client.player.distanceToSqr(frame) > maxSq) continue;
            if (!frame.getItem().is(Items.ELYTRA)) continue;

            WorldBoxRenderer.box(
                    context,
                    frame.getBoundingBox().inflate(0.18),
                    0xFF55E6B1,
                    1.0f,
                    3.0f
            );
        }
    }

    public static void renderHud(GuiGraphics g) {
        renderStaffList(g);
        renderRtpAlert(g);
        renderRegionMap(g);
        renderSeed(g);
        renderSpawnerProtect(g);
        renderFakeStats(g);
        renderDeepPlayerHud(g);
    }

    private static void renderStaffList(GuiGraphics g) {
        Module module = ModuleRegistry.find("Staff List");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        List<String> staff = new ArrayList<>();
        boolean heuristic = ConfigManager.boolOption("Staff List", "rankHeuristic", true);

        for (AbstractClientPlayer player : client.level.players()) {
            String visible = player.getDisplayName().getString();
            String lower = visible.toLowerCase(Locale.ROOT);

            if (!heuristic
                    || lower.contains("admin")
                    || lower.contains("mod")
                    || lower.contains("staff")
                    || lower.contains("helper")
                    || lower.contains("owner")
                    || lower.contains("developer")) {
                if (heuristic) staff.add(visible);
            }
        }

        if (staff.isEmpty() && ConfigManager.boolOption("Staff List", "hideEmpty", true)) return;

        int maxRows = ConfigManager.intOption("Staff List", "maxRows", 8);
        int visible = Math.min(staff.size(), maxRows);
        int w = 174;
        int h = 32 + Math.max(1, visible) * 16;
        int x = g.guiWidth() - w - 14;
        int y = 95;

        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 10, 0xE90D0912);
        SmoothTextRenderer.draw(g, "Staff List", x + 10, y + 8, 9.0f, 0xFFF4EFF7, true);

        if (staff.isEmpty()) {
            SmoothTextRenderer.draw(g, "Kein sichtbarer Staff erkannt", x + 10, y + 25, 7.5f, 0xFF827789, false);
            return;
        }

        for (int i = 0; i < visible; i++) {
            SmoothTextRenderer.draw(g, staff.get(i), x + 10, y + 27 + i * 16, 7.8f, 0xFFFF8FC3, false);
        }
    }

    private static void renderRtpAlert(GuiGraphics g) {
        Module module = ModuleRegistry.find("RTP Base Alert");
        if (module == null || !module.enabled() || !LicenseManager.verified() || rtpStatus.isEmpty()) return;

        int w = 238;
        int x = (g.guiWidth() - w) / 2;
        int y = 14;

        SmoothShapeRenderer.glow(g, x, y, w, 34, 10, 0x55FF4F9F, 4);
        SmoothShapeRenderer.roundedRect(g, x, y, w, 34, 10, 0xED110B16);
        SmoothTextRenderer.draw(g, rtpStatus, x + 12, y + 11, 8.4f, 0xFFF6F0F8, true);
    }

    private static void renderRegionMap(GuiGraphics g) {
        Module module = ModuleRegistry.find("RegionMap");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int scale = ConfigManager.intOption("RegionMap", "scale", 3);
        int cell = 4 + scale;
        int radius = 4;
        int size = (radius * 2 + 1) * cell;
        int x = 14;
        int y = 118;

        SmoothShapeRenderer.roundedRect(g, x - 7, y - 22, size + 14, size + 31, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, "RegionMap", x, y - 15, 8.4f, 0xFFF2EDF4, true);

        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;

        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                int px = x + (dx + radius) * cell;
                int py = y + (dz + radius) * cell;

                int color = client.level.hasChunk(cx, cz) ? 0xFF2B1B31 : 0xFF171019;
                if (dx == 0 && dz == 0) color = ThemeConfig.accent;

                g.fill(px, py, px + cell - 1, py + cell - 1, color);
            }
        }
    }

    private static void renderSeed(GuiGraphics g) {
        Module module = ModuleRegistry.find("SeedAnalyzer");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        String text = cachedSeed == null
                ? "Seed: auf SMP clientseitig nicht verfügbar"
                : "Seed: " + cachedSeed;

        int tw = SmoothTextRenderer.width(text, 7.9f, true);
        int x = (g.guiWidth() - tw) / 2;
        int y = g.guiHeight() - 26;

        SmoothTextRenderer.draw(g, text, x, y, 7.9f, cachedSeed == null ? 0xFF8C818F : 0xFF55E6B1, true);
    }

    private static void renderSpawnerProtect(GuiGraphics g) {
        Module module = ModuleRegistry.find("SpawnerProtect");
        if (module == null || !module.enabled() || !LicenseManager.verified()
                || !spawnerDanger
                || !ConfigManager.boolOption("SpawnerProtect", "hud", true)) return;

        String text = "SpawnerProtect · Spieler in der Nähe";
        int tw = SmoothTextRenderer.width(text, 8.6f, true);
        int x = (g.guiWidth() - tw - 24) / 2;
        int y = 54;

        SmoothShapeRenderer.glow(g, x, y, tw + 24, 32, 9, 0x66FF5B6E, 4);
        SmoothShapeRenderer.roundedRect(g, x, y, tw + 24, 32, 9, 0xED170B10);
        SmoothTextRenderer.draw(g, text, x + 12, y + 10, 8.6f, 0xFFFF8793, true);
    }

    private static void renderFakeStats(GuiGraphics g) {
        Module module = ModuleRegistry.find("FakeStats");
        if (module == null || !module.enabled() || !LicenseManager.verified()
                || !ConfigManager.boolOption("FakeStats", "hud", true)) return;

        int money = ConfigManager.intOption("FakeStats", "money", 1_000_000);
        int kills = ConfigManager.intOption("FakeStats", "kills", 100);
        String text = "Local Preview · $" + money + " · " + kills + " Kills";

        int tw = SmoothTextRenderer.width(text, 7.8f, true);
        int x = 14;
        int y = g.guiHeight() - 82;

        SmoothShapeRenderer.roundedRect(g, x, y, tw + 20, 28, 8, 0xE80D0912);
        SmoothTextRenderer.draw(g, text, x + 10, y + 8, 7.8f, 0xFFBDA8D0, true);
    }

    private static void renderDeepPlayerHud(GuiGraphics g) {
        Module module = ModuleRegistry.find("Deep Player Finder");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int range = ConfigManager.intOption("Deep Player Finder", "range", 128);
        int maxY = ConfigManager.intOption("Deep Player Finder", "maxY", 32);

        List<AbstractClientPlayer> found = client.level.players().stream()
                .filter(p -> p != client.player && !p.isRemoved())
                .filter(p -> p.getY() <= maxY)
                .filter(p -> client.player.distanceToSqr(p) <= range * (double)range)
                .sorted(Comparator.comparingDouble(client.player::distanceToSqr))
                .limit(5)
                .toList();

        if (found.isEmpty()) return;

        int w = 190;
        int h = 28 + found.size() * 16;
        int x = g.guiWidth() - w - 14;
        int y = 190;

        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, "Deep Player Finder", x + 10, y + 8, 8.6f, 0xFFF1ECF4, true);

        for (int i = 0; i < found.size(); i++) {
            AbstractClientPlayer player = found.get(i);
            String left = player.getName().getString();
            String right = (int)Math.round(client.player.distanceTo(player)) + "m · Y" + (int)Math.floor(player.getY());
            int rw = SmoothTextRenderer.width(right, 7.3f, true);

            SmoothTextRenderer.draw(g, left, x + 10, y + 26 + i * 16, 7.5f, 0xFFD8CFDD, false);
            SmoothTextRenderer.draw(g, right, x + w - 10 - rw, y + 26 + i * 16, 7.3f, 0xFFFF6AAF, true);
        }
    }

    private DonutUtilityRuntime() {}
}
