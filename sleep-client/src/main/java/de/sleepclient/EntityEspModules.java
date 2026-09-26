package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;

public final class EntityEspModules {
    public static void renderWorld(WorldRenderContext context) {
        if (!LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        boolean loot = enabled("LootESP");
        boolean trader = enabled("TraderFinder");
        if (!loot && !trader) return;

        int lootRange = ConfigManager.intOption("LootESP", "range", 96);
        int traderRange = ConfigManager.intOption("TraderFinder", "range", 128);
        float lootWidth = ConfigManager.floatOption("LootESP", "lineWidth", 1.8f);
        boolean llamas = ConfigManager.boolOption("TraderFinder", "llamas", true);
        int maxItems = ConfigManager.intOption("LootESP", "maxItems", 96);

        int items = 0;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || entity.isRemoved()) continue;

            if (loot && entity instanceof ItemEntity) {
                if (client.player.distanceToSqr(entity) <= lootRange * (double)lootRange && items++ < maxItems) {
                    WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.04),
                            0xFF55E6B1, 0.95f, lootWidth);
                }
            }

            if (trader) {
                boolean isTrader = entity.getType() == EntityType.WANDERING_TRADER;
                boolean isLlama = llamas && entity.getType() == EntityType.TRADER_LLAMA;
                if ((isTrader || isLlama) && client.player.distanceToSqr(entity) <= traderRange * (double)traderRange) {
                    WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.08),
                            isTrader ? 0xFFFFC857 : 0xFF55B8FF, 1.0f, 2.4f);
                }
            }
        }
    }

    public static void renderHud(GuiGraphics g) {
        if (!enabled("TraderFinder")
                || !LicenseManager.verified()
                || !ConfigManager.boolOption("TraderFinder", "hud", true)) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        Entity nearest = null;
        double nearestSq = Double.MAX_VALUE;
        int range = ConfigManager.intOption("TraderFinder", "range", 128);

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity.getType() != EntityType.WANDERING_TRADER) continue;
            double d = client.player.distanceToSqr(entity);
            if (d <= range * (double)range && d < nearestSq) {
                nearest = entity;
                nearestSq = d;
            }
        }

        if (nearest == null) return;

        int distance = (int)Math.round(Math.sqrt(nearestSq));
        String text = "Trader " + distance + "m";
        int tw = SmoothTextRenderer.width(text, 8.6f, true);
        int x = g.guiWidth() - tw - 34;
        int y = g.guiHeight() - 48;

        SmoothShapeRenderer.roundedRect(g, x, y, tw + 20, 30, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, text, x + 10, y + 9, 8.6f, 0xFFFFD77A, true);
    }

    private static boolean enabled(String name) {
        Module module = ModuleRegistry.find(name);
        return module != null && module.enabled();
    }

    private EntityEspModules() {}
}
