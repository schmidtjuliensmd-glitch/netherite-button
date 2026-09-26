package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TotemCounterHud {
    public static void render(GuiGraphics g) {
        Module module = ModuleRegistry.find("Totem Counter");
        if (module == null || !module.enabled()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int count = 0;
        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING)) count += stack.getCount();
        }

        String position = ConfigManager.stringOption("Totem Counter", "position", "Bottom Right");
        boolean background = ConfigManager.boolOption("Totem Counter", "background", true);
        boolean glow = ConfigManager.boolOption("Totem Counter", "glow", true);

        String text = "Totems  " + count;
        int tw = SmoothTextRenderer.width(text, 9.0f, true);
        int w = tw + 20;
        int h = 28;

        int x = position.contains("Right") ? g.guiWidth() - w - 14 : 14;
        int y = position.startsWith("Bottom") ? g.guiHeight() - h - 14 : 14;

        if (glow) SmoothShapeRenderer.glow(g, x, y, w, h, 9, (0x4F << 24) | (ThemeConfig.accent & 0x00FFFFFF), 4);
        if (background) SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xE80D0912);

        SmoothTextRenderer.draw(g, text, x + 10, y + 8, 9.0f, ThemeConfig.accent, true);
    }

    private TotemCounterHud() {}
}
