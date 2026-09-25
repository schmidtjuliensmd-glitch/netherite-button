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
import java.util.List;

public final class StorageEsp {
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static int ticks;

    public static void tick(Minecraft client) {
        Module module = ModuleRegistry.find("StorageESP");
        if (module == null || !module.enabled() || !LicenseManager.verified() || client.level == null || client.player == null) {
            ENTRIES.clear();
            return;
        }

        if (++ticks < 10) return;
        ticks = 0;

        int range = ConfigManager.intOption("StorageESP", "range", 96);
        int radiusChunks = Math.max(1, (range + 15) / 16);
        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;
        boolean hoppers = ConfigManager.boolOption("StorageESP", "hoppers", true);
        boolean furnaces = ConfigManager.boolOption("StorageESP", "furnaces", false);

        List<Entry> found = new ArrayList<>();

        for (int cx = pcx - radiusChunks; cx <= pcx + radiusChunks; cx++) {
            for (int cz = pcz - radiusChunks; cz <= pcz + radiusChunks; cz++) {
                if (!client.level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = client.level.getChunk(cx, cz);

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockEntityType<?> type = be.getType();
                    String label = null;
                    int color = ThemeConfig.accent;

                    if (type == BlockEntityType.CHEST || type == BlockEntityType.TRAPPED_CHEST) {
                        label = "Chest";
                    } else if (type == BlockEntityType.BARREL) {
                        label = "Barrel";
                    } else if (type == BlockEntityType.SHULKER_BOX) {
                        label = "Shulker";
                        color = ThemeConfig.accentSecondary;
                    } else if (type == BlockEntityType.ENDER_CHEST) {
                        label = "Ender Chest";
                        color = 0xFFA16CFF;
                    } else if (type == BlockEntityType.SPAWNER) {
                        label = "Spawner";
                        color = 0xFFFFC857;
                    } else if (hoppers && type == BlockEntityType.HOPPER) {
                        label = "Hopper";
                        color = 0xFF5BC0FF;
                    } else if (furnaces && (type == BlockEntityType.FURNACE
                            || type == BlockEntityType.BLAST_FURNACE
                            || type == BlockEntityType.SMOKER)) {
                        label = "Furnace";
                        color = 0xFFFF8A5B;
                    }

                    if (label == null) continue;

                    BlockPos pos = be.getBlockPos();
                    if (client.player.distanceToSqr(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) > range * (double)range) continue;

                    found.add(new Entry(pos.immutable(), label, color));
                }
            }
        }

        ENTRIES.clear();
        ENTRIES.addAll(found);
    }

    public static void renderWorld(WorldRenderContext context) {
        Module module = ModuleRegistry.find("StorageESP");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        float width = ConfigManager.floatOption("StorageESP", "lineWidth", 2.0f);
        for (Entry entry : ENTRIES) {
            BlockPos p = entry.pos();
            WorldBoxRenderer.box(context,
                    new AABB(p.getX(), p.getY(), p.getZ(), p.getX()+1, p.getY()+1, p.getZ()+1),
                    entry.color(), 0.95f, width);
        }
    }

    public static void renderHud(GuiGraphics g) {
        Module module = ModuleRegistry.find("StorageESP");
        if (module == null || !module.enabled() || !LicenseManager.verified()
                || !ConfigManager.boolOption("StorageESP", "hud", true)) return;

        if (ENTRIES.isEmpty()) return;

        int x = 14;
        int y = g.guiHeight() - 48;
        SmoothShapeRenderer.roundedRect(g, x, y, 150, 30, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, "StorageESP", x + 10, y + 8, 8.8f, 0xFFF2EDF4, true);
        String count = ENTRIES.size() + " Treffer";
        int tw = SmoothTextRenderer.width(count, 8.0f, true);
        SmoothTextRenderer.draw(g, count, x + 140 - tw, y + 9, 8.0f, ThemeConfig.accent, true);
    }

    private record Entry(BlockPos pos, String label, int color) {}

    private StorageEsp() {}
}
