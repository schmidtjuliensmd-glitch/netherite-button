package de.sleepclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SleepClickGuiScreen extends Screen {
    private ModuleCategory selected = ModuleCategory.COMBAT;
    private int scrollOffset = 0;

    public SleepClickGuiScreen() {
        super(Component.literal("Sleep Client"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int width = Math.min(this.width - 36, 980);
        int height = Math.min(this.height - 36, 590);
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;
        int sidebar = 190;

        graphics.fill(x, y, x + width, y + height, 0xF0090710);
        graphics.fill(x, y, x + sidebar, y + height, 0xF20D0911);
        graphics.fill(x + sidebar, y, x + sidebar + 1, y + height, 0x22FFFFFF);

        graphics.drawString(this.font, "☾", x + 18, y + 18, ThemeConfig.accent, true);
        graphics.drawString(this.font, "Sleep Client", x + 39, y + 18, 0xFFFFFFFF, true);
        graphics.drawString(this.font, "v0.1", x + 39, y + 31, 0xFF756879, false);

        int cy = y + 72;
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean active = category == selected;
            int bg = active ? 0x332C183A : 0x00000000;
            graphics.fill(x + 10, cy - 5, x + sidebar - 10, cy + 17, bg);
            if (active) graphics.fill(x + 10, cy - 5, x + 13, cy + 17, ThemeConfig.accent);
            graphics.drawString(this.font, category.displayName(), x + 22, cy, active ? 0xFFFFFFFF : 0xFF9A8E9A, false);
            cy += 31;
        }

        graphics.drawString(this.font, selected.displayName(), x + sidebar + 20, y + 18, 0xFFFFFFFF, true);
        graphics.drawString(this.font, "Search modules...", x + width - 150, y + 18, 0xFF786D79, false);
        graphics.fill(x + sidebar + 16, y + 42, x + width - 16, y + 43, 0x224F9FFF);

        List<Module> modules = ModuleRegistry.byCategory(selected);
        int contentX = x + sidebar + 18;
        int contentY = y + 58 - scrollOffset;
        int cardW = (width - sidebar - 54) / 2;
        int cardH = 46;
        int gap = 10;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % 2;
            int row = i / 2;
            int mx = contentX + col * (cardW + gap);
            int my = contentY + row * (cardH + gap);
            if (my + cardH < y + 50 || my > y + height - 12) continue;

            int border = module.enabled() ? ThemeConfig.accent : 0x22FFFFFF;
            graphics.fill(mx - 1, my - 1, mx + cardW + 1, my + cardH + 1, border);
            graphics.fill(mx, my, mx + cardW, my + cardH, module.enabled() ? 0xF21A1020 : 0xF20F0C12);
            graphics.drawString(this.font, module.name(), mx + 10, my + 9, 0xFFFFFFFF, true);
            graphics.drawString(this.font, module.description(), mx + 10, my + 25, 0xFF877C87, false);

            int tx = mx + cardW - 35;
            int ty = my + 14;
            graphics.fill(tx, ty, tx + 24, ty + 12, module.enabled() ? ThemeConfig.accent : 0xFF30343B);
            graphics.fill(module.enabled() ? tx + 14 : tx + 2, ty + 2, module.enabled() ? tx + 22 : tx + 10, ty + 10, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int width = Math.min(this.width - 36, 980);
        int height = Math.min(this.height - 36, 590);
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;
        int sidebar = 190;

        int cy = y + 72;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseX >= x + 10 && mouseX <= x + sidebar - 10 && mouseY >= cy - 5 && mouseY <= cy + 17) {
                selected = category;
                scrollOffset = 0;
                return true;
            }
            cy += 31;
        }

        List<Module> modules = ModuleRegistry.byCategory(selected);
        int contentX = x + sidebar + 18;
        int contentY = y + 58 - scrollOffset;
        int cardW = (width - sidebar - 54) / 2;
        int cardH = 46;
        int gap = 10;

        for (int i = 0; i < modules.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int mx = contentX + col * (cardW + gap);
            int my = contentY + row * (cardH + gap);
            if (mouseX >= mx && mouseX <= mx + cardW && mouseY >= my && mouseY <= my + cardH) {
                modules.get(i).toggle();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int rows = (ModuleRegistry.byCategory(selected).size() + 1) / 2;
        int contentHeight = rows * 56;
        int visible = Math.min(this.height - 36, 590) - 78;
        int max = Math.max(0, contentHeight - visible);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int)(verticalAmount * 24)));
        return true;
    }
}
