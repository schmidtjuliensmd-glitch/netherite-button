package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PlayerEsp {
    public static void renderWorld(WorldRenderContext context) {
        Module module = ModuleRegistry.find("PlayerESP");
        if (module == null || !module.enabled()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        int range = ConfigManager.intOption("PlayerESP", "range", 96);
        float lineWidth = ConfigManager.floatOption("PlayerESP", "lineWidth", 2.2f);
        double maxSq = range * (double)range;

        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player || player.isRemoved()) continue;
            if (client.player.distanceToSqr(player) > maxSq) continue;

            AABB box = player.getBoundingBox().inflate(0.06);
            WorldBoxRenderer.box(context, box, ThemeConfig.accent, 0.95f, lineWidth);
        }
    }

    public static void renderHud(GuiGraphics g) {
        Module module = ModuleRegistry.find("PlayerESP");
        if (module == null || !module.enabled()
                || !ConfigManager.boolOption("PlayerESP", "distanceHud", true)) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        List<AbstractClientPlayer> nearby = new ArrayList<>();
        int range = ConfigManager.intOption("PlayerESP", "range", 96);
        double maxSq = range * (double)range;

        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player || player.isRemoved()) continue;
            if (client.player.distanceToSqr(player) <= maxSq) nearby.add(player);
        }

        nearby.sort(Comparator.comparingDouble(client.player::distanceToSqr));
        int visible = Math.min(nearby.size(), 5);
        if (visible == 0) return;

        int w = 188;
        int h = 28 + visible * 17;
        int x = g.guiWidth() - w - 14;
        int y = 14;

        SmoothShapeRenderer.glow(g, x, y, w, h, 10, (0x58 << 24) | (ThemeConfig.accent & 0x00FFFFFF), 4);
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 10, 0xE80D0912);

        SmoothTextRenderer.draw(g, "PlayerESP", x + 10, y + 8, 9.8f, 0xFFF6F1F7, true);

        for (int i = 0; i < visible; i++) {
            AbstractClientPlayer player = nearby.get(i);
            int rowY = y + 27 + i * 17;
            int distance = (int)Math.round(client.player.distanceTo(player));

            SmoothTextRenderer.draw(g, player.getName().getString(), x + 10, rowY, 8.4f, 0xFFD9D0DD, false);
            String dist = distance + "m";
            int tw = SmoothTextRenderer.width(dist, 8.2f, true);
            SmoothTextRenderer.draw(g, dist, x + w - 10 - tw, rowY, 8.2f, ThemeConfig.accent, true);
        }
    }

    private PlayerEsp() {}
}
