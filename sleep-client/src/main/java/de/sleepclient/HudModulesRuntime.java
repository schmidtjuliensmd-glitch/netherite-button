package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class HudModulesRuntime {
    private static final long SESSION_START = System.currentTimeMillis();
    private static boolean lastAttackDown;
    private static int clicksThisSecond;
    private static int shownCps;
    private static long cpsWindowStart = System.currentTimeMillis();

    public static void tick(Minecraft client) {
        boolean attack = client.options.keyAttack.isDown();
        if (attack && !lastAttackDown) clicksThisSecond++;
        lastAttackDown = attack;

        long now = System.currentTimeMillis();
        if (now - cpsWindowStart >= 1000L) {
            shownCps = clicksThisSecond;
            clicksThisSecond = 0;
            cpsWindowStart = now;
        }
    }

    public static void render(GuiGraphics g) {
        if (!LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        renderWatermark(g);
        renderModuleList(g);
        renderBasicHud(g, client);
        renderArmorHud(g, client);
        renderSessionInfo(g);
        renderAlerts(g, client);
        renderCrosshair(g);
    }

    private static void renderWatermark(GuiGraphics g) {
        if (!enabled("Watermark")) return;
        String text = "Sleep Client" + (ConfigManager.boolOption("Watermark","version",true) ? "  " + UpdateManager.currentVersion() : "");
        int tw = SmoothTextRenderer.width(text, 8.6f, true);
        int x = 10;
        int y = 8;
        SmoothShapeRenderer.roundedRect(g, x, y, tw + 18, 25, 8, 0xD80D0912);
        SmoothTextRenderer.draw(g, text, x + 9, y + 7, 8.6f, ThemeConfig.accent, true);
    }

    private static void renderModuleList(GuiGraphics g) {
        if (!enabled("Module List")) return;

        List<String> active = ModuleRegistry.all().stream()
                .filter(Module::enabled)
                .map(Module::name)
                .toList();

        if (active.isEmpty()) return;

        int y = 42;
        int max = Math.min(active.size(), 18);
        for (int i = 0; i < max; i++) {
            String name = active.get(i);
            int tw = SmoothTextRenderer.width(name, 7.8f, true);
            int x = g.guiWidth() - tw - 12;
            SmoothTextRenderer.draw(g, name, x, y + i * 14, 7.8f, ThemeConfig.accent, true);
        }
    }

    private static void renderBasicHud(GuiGraphics g, Minecraft client) {
        boolean showHud = enabled("Show HUD");
        boolean fps = enabled("FPS Counter") || (showHud && ConfigManager.boolOption("Show HUD","fps",true));
        boolean coords = enabled("Coordinates HUD") || (showHud && ConfigManager.boolOption("Show HUD","coords",true));
        boolean direction = enabled("Direction HUD");
        boolean cps = enabled("CPS Counter");

        List<String> lines = new ArrayList<>();
        if (fps) lines.add("FPS " + client.getFps());
        if (coords) lines.add("XYZ " + (int)Math.floor(client.player.getX()) + " / " + (int)Math.floor(client.player.getY()) + " / " + (int)Math.floor(client.player.getZ()));
        if (direction) lines.add("Facing " + client.player.getDirection().getName().toUpperCase());
        if (cps) lines.add("CPS " + shownCps);

        if (lines.isEmpty()) return;

        int y = g.guiHeight() - 18 - lines.size() * 14;
        for (String line : lines) {
            SmoothTextRenderer.draw(g, line, 10, y, 7.8f, 0xFFE3DBE6, true);
            y += 14;
        }
    }

    private static void renderArmorHud(GuiGraphics g, Minecraft client) {
        if (!enabled("Armor HUD")) return;

        int x = g.guiWidth() / 2 - 78;
        int y = g.guiHeight() - 52;
        int i = 0;

        for (ItemStack stack : client.player.getArmorSlots()) {
            if (stack.isEmpty()) continue;
            int max = stack.getMaxDamage();
            int left = max <= 0 ? 100 : (int)Math.round((max - stack.getDamageValue()) * 100.0 / max);
            String text = left + "%";
            SmoothShapeRenderer.roundedRect(g, x + i * 42, y, 38, 24, 7, 0xC90D0912);
            SmoothTextRenderer.draw(g, text, x + 6 + i * 42, y + 7, 7.8f,
                    left <= 20 ? 0xFFFF6878 : 0xFFDCD3E0, true);
            i++;
        }
    }

    private static void renderSessionInfo(GuiGraphics g) {
        if (!enabled("Session Info")) return;

        long seconds = Duration.ofMillis(System.currentTimeMillis() - SESSION_START).getSeconds();
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        String text = String.format("Session %02d:%02d:%02d", h, m, s);

        int tw = SmoothTextRenderer.width(text, 7.7f, true);
        int x = g.guiWidth() - tw - 12;
        int y = g.guiHeight() - 18;
        SmoothTextRenderer.draw(g, text, x, y, 7.7f, 0xFFB9ADBE, true);
    }

    private static void renderAlerts(GuiGraphics g, Minecraft client) {
        String warning = null;

        if (enabled("Low Health Alert")) {
            float threshold = ConfigManager.floatOption("Low Health Alert","health",6.0f);
            if (client.player.getHealth() <= threshold) warning = "LOW HEALTH  " + String.format("%.1f", client.player.getHealth());
        }

        if (enabled("Durability Alert")) {
            int threshold = ConfigManager.intOption("Durability Alert","percent",20);
            for (ItemStack stack : client.player.getArmorSlots()) {
                if (stack.isEmpty() || stack.getMaxDamage() <= 0) continue;
                int left = (int)Math.round((stack.getMaxDamage() - stack.getDamageValue()) * 100.0 / stack.getMaxDamage());
                if (left <= threshold) {
                    warning = "LOW DURABILITY  " + left + "%";
                    break;
                }
            }
        }

        if (warning == null) return;

        int tw = SmoothTextRenderer.width(warning, 9.0f, true);
        int x = (g.guiWidth() - tw - 28) / 2;
        int y = 88;
        SmoothShapeRenderer.glow(g, x, y, tw + 28, 32, 9, 0x55FF536B, 4);
        SmoothShapeRenderer.roundedRect(g, x, y, tw + 28, 32, 9, 0xE9140A10);
        SmoothTextRenderer.draw(g, warning, x + 14, y + 10, 9.0f, 0xFFFF7787, true);
    }

    private static void renderCrosshair(GuiGraphics g) {
        if (!enabled("Crosshair")) return;

        int size = ConfigManager.intOption("Crosshair","size",6);
        int gap = ConfigManager.intOption("Crosshair","gap",3);
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        int c = ThemeConfig.accent;

        g.fill(cx - gap - size, cy, cx - gap, cy + 1, c);
        g.fill(cx + gap, cy, cx + gap + size, cy + 1, c);
        g.fill(cx, cy - gap - size, cx + 1, cy - gap, c);
        g.fill(cx, cy + gap, cx + 1, cy + gap + size, c);
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private HudModulesRuntime() {}
}
