package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExtraHudRuntime {
    public static void render(GuiGraphics g) {
        if (!LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        renderPotionHud(g, client);
        renderKeystrokes(g, client);
        renderPearlCooldown(g, client);
    }

    private static void renderPotionHud(GuiGraphics g, Minecraft client) {
        if (!enabled("Potion HUD")) return;

        List<MobEffectInstance> effects = new ArrayList<>(client.player.getActiveEffects());
        if (effects.isEmpty()) return;

        effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration).reversed());

        int visible = Math.min(effects.size(), 8);
        int w = 180;
        int h = 28 + visible * 17;
        int x = 14;
        int y = 350;

        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, "Potion HUD", x + 10, y + 8, 8.6f, 0xFFF2EDF4, true);

        for (int i = 0; i < visible; i++) {
            MobEffectInstance effect = effects.get(i);
            String name = effect.getEffect().value().getDisplayName().getString();
            int sec = Math.max(0, effect.getDuration() / 20);
            String time = String.format("%d:%02d", sec / 60, sec % 60);

            int rowY = y + 27 + i * 17;
            SmoothTextRenderer.draw(g, name, x + 10, rowY, 7.6f, 0xFFD8CFDC, false);
            int tw = SmoothTextRenderer.width(time, 7.4f, true);
            SmoothTextRenderer.draw(g, time, x + w - 10 - tw, rowY, 7.4f, ThemeConfig.accent, true);
        }
    }

    private static void renderKeystrokes(GuiGraphics g, Minecraft client) {
        if (!enabled("Keystrokes")) return;

        int x = g.guiWidth() / 2 - 37;
        int y = g.guiHeight() - 105;
        int key = 23;
        int gap = 3;

        drawKey(g, x + key + gap, y, key, key, "W", client.options.keyUp.isDown());
        drawKey(g, x, y + key + gap, key, key, "A", client.options.keyLeft.isDown());
        drawKey(g, x + key + gap, y + key + gap, key, key, "S", client.options.keyDown.isDown());
        drawKey(g, x + (key + gap) * 2, y + key + gap, key, key, "D", client.options.keyRight.isDown());

        if (ConfigManager.boolOption("Keystrokes","mouse",true)) {
            drawKey(g, x, y + (key + gap) * 2, key * 2 + gap, 20, "LMB", client.options.keyAttack.isDown());
            drawKey(g, x + key * 2 + gap * 2, y + (key + gap) * 2, key * 2 + gap, 20, "RMB", client.options.keyUse.isDown());
        }
    }

    private static void drawKey(GuiGraphics g, int x, int y, int w, int h, String text, boolean down) {
        int bg = down ? ThemeConfig.accent : 0xD8120C16;
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 6, bg);
        int tw = SmoothTextRenderer.width(text, 7.6f, true);
        SmoothTextRenderer.draw(g, text, x + (w - tw) / 2, y + (h - 8) / 2, 7.6f, 0xFFFFFFFF, true);
    }

    private static void renderPearlCooldown(GuiGraphics g, Minecraft client) {
        if (!enabled("Pearl Cooldown")) return;

        ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
        float pct = client.player.getCooldowns().getCooldownPercent(pearl, 0.0f);
        if (pct <= 0.0f) return;

        String text = ConfigManager.boolOption("Pearl Cooldown","seconds",true)
                ? "Pearl Cooldown  " + Math.max(1, Math.round(pct * 20.0f) / 2.0f) + "s"
                : "Pearl Cooldown";

        int tw = SmoothTextRenderer.width(text, 8.4f, true);
        int x = (g.guiWidth() - tw - 22) / 2;
        int y = g.guiHeight() - 64;

        SmoothShapeRenderer.roundedRect(g, x, y, tw + 22, 29, 8, 0xE80D0912);
        SmoothTextRenderer.draw(g, text, x + 11, y + 8, 8.4f, 0xFFD9D0DF, true);

        int barW = tw + 2;
        g.fill(x + 10, y + 22, x + 10 + barW, y + 25, 0xFF2A2230);
        g.fill(x + 10, y + 22, x + 10 + Math.round(barW * pct), y + 25, ThemeConfig.accent);
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private ExtraHudRuntime() {}
}
