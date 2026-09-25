package de.sleepclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class SleepClickGuiScreen extends Screen {
    private static final int TARGET_W = 604;
    private static final int TARGET_H = 498;
    private static final int SIDEBAR_W = 188;

    private static final int BG = 0xFF0B0710;
    private static final int SIDEBAR_BG = 0xFF0C0810;
    private static final int MAIN_BG = 0xFF120A17;
    private static final int CARD_BG = 0xFF1A111D;
    private static final int CARD_BG_ACTIVE = 0xFF1B1020;
    private static final int LINE = 0xFF332238;
    private static final int LINE_SOFT = 0xFF251828;
    private static final int TEXT = 0xFFF4EDF6;
    private static final int MUTED = 0xFF9A8E9E;
    private static final int MUTED_DARK = 0xFF716676;
    private static final int PINK = 0xFFFF4FA3;
    private static final int PINK_DARK = 0xFFCF2F78;
    private static final int OFF_TRACK = 0xFF48515D;
    private static final int OFF_KNOB = 0xFF8993A0;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

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
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int w = Math.min(this.width - 16, TARGET_W);
        int h = Math.min(this.height - 16, TARGET_H);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        // Outer shell
        roundedRect(g, x, y, w, h, 11, BG);
        roundedOutline(g, x, y, w, h, 11, LINE, BG);

        // Sidebar and main body
        roundedRect(g, x + 1, y + 1, SIDEBAR_W - 1, h - 2, 10, SIDEBAR_BG);
        g.fill(x + SIDEBAR_W, y + 1, x + w - 1, y + h - 1, MAIN_BG);
        g.fill(x + SIDEBAR_W, y + 1, x + SIDEBAR_W + 1, y + h - 1, LINE_SOFT);

        renderBrand(g, x, y);
        renderSidebar(g, x, y, mouseX, mouseY);
        renderMain(g, x, y, w, h, mouseX, mouseY);
    }

    private void renderBrand(GuiGraphics g, int x, int y) {
        // Pink app icon
        roundedRect(g, x + 19, y + 20, 33, 33, 8, 0xFFE13D9A);
        g.drawString(this.font, "C", x + 31, y + 32, 0xFFFFFFFF, true);

        g.drawString(this.font, "Sleep Client", x + 59, y + 27, TEXT, true);
        g.drawString(this.font, "v1.0", x + 59, y + 44, MUTED_DARK, false);
    }

    private void renderSidebar(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        g.drawString(this.font, "MODULE", x + 20, y + 91, MUTED_DARK, true);

        int itemY = y + 109;
        for (ModuleCategory category : new ModuleCategory[]{
                ModuleCategory.COMBAT,
                ModuleCategory.MOVEMENT,
                ModuleCategory.DONUT_SMP,
                ModuleCategory.VISUALS,
                ModuleCategory.MISC
        }) {
            renderCategory(g, category, x + 12, itemY, 165, 38, mouseX, mouseY);
            itemY += 42;
        }

        g.drawString(this.font, "ALLGEMEIN", x + 20, y + 345, MUTED_DARK, true);

        renderBottomItem(g, "⚙", "GUI", x + 12, y + 365, 165, 38, selected == ModuleCategory.GUI, mouseX, mouseY);
        renderBottomItem(g, "⌁", "Settings", x + 12, y + 407, 165, 38, false, mouseX, mouseY);
    }

    private void renderCategory(GuiGraphics g, ModuleCategory category, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean active = selected == category;
        boolean hover = inside(mouseX, mouseY, x, y, w, h);

        if (active) {
            roundedRect(g, x, y, w, h, 8, 0xFF28132F);
            roundedOutline(g, x, y, w, h, 8, 0xFF4C224F, 0xFF28132F);
            g.fill(x, y + 7, x + 2, y + h - 7, PINK);
        } else if (hover) {
            roundedRect(g, x, y, w, h, 8, 0xFF151019);
        }

        String icon = switch (category) {
            case COMBAT -> "⚔";
            case MOVEMENT -> "➜";
            case DONUT_SMP -> "◉";
            case VISUALS -> "◇";
            case MISC -> "◇";
            default -> "•";
        };

        int color = active ? TEXT : MUTED;
        g.drawString(this.font, icon, x + 9, y + 15, color, false);
        g.drawString(this.font, category.displayName(), x + 27, y + 15, color, active);
    }

    private void renderBottomItem(GuiGraphics g, String icon, String label, int x, int y, int w, int h,
                                  boolean active, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        if (active) {
            roundedRect(g, x, y, w, h, 8, 0xFF28132F);
        } else if (hover) {
            roundedRect(g, x, y, w, h, 8, 0xFF151019);
        }
        int color = active ? TEXT : MUTED;
        g.drawString(this.font, icon, x + 9, y + 15, color, false);
        g.drawString(this.font, label, x + 27, y + 15, color, active);
    }

    private void renderMain(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        int mainX = x + SIDEBAR_W;

        String title = switch (selected) {
            case COMBAT -> "Kampf";
            case MOVEMENT -> "Movement";
            case DONUT_SMP -> "DonutSMP";
            case VISUALS -> "Visuals";
            case MISC -> "Misc";
            case GUI -> "GUI";
        };

        g.drawString(this.font, title, mainX + 17, y + 29, TEXT, true);
        g.drawString(this.font, LocalTime.now().format(TIME), mainX + 174, y + 29, PINK, true);

        // Search field
        int searchX = x + w - 196;
        int searchY = y + 18;
        roundedRect(g, searchX, searchY, 186, 31, 8, 0xFF1B1220);
        roundedOutline(g, searchX, searchY, 186, 31, 8, LINE, 0xFF1B1220);
        g.drawString(this.font, "Module suchen...", searchX + 11, searchY + 12, MUTED_DARK, false);

        // Header divider
        g.fill(mainX + 17, y + 55, x + w - 10, y + 56, 0xFF3B1C39);

        List<Module> modules = ModuleRegistry.byCategory(selected);

        int left = mainX + 17;
        int top = y + 70 - scrollOffset;
        int gap = 10;
        int cardW = (w - SIDEBAR_W - 44) / 2;
        int cardH = 54;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % 2;
            int row = i / 2;
            int cx = left + col * (cardW + gap);
            int cy = top + row * (cardH + gap);

            if (cy + cardH < y + 58 || cy > y + h - 12) continue;
            renderModuleCard(g, module, cx, cy, cardW, cardH, mouseX, mouseY);
        }
    }

    private void renderModuleCard(GuiGraphics g, Module module, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        int bg = module.enabled() ? CARD_BG_ACTIVE : CARD_BG;
        int border = module.enabled() ? PINK_DARK : (hover ? 0xFF66335F : LINE);

        roundedRect(g, x, y, w, h, 8, bg);
        roundedOutline(g, x, y, w, h, 8, border, bg);

        g.drawString(this.font, module.name(), x + 12, y + 22, TEXT, true);

        int trackX = x + w - 39;
        int trackY = y + 19;
        renderToggle(g, trackX, trackY, module.enabled());
    }

    private void renderToggle(GuiGraphics g, int x, int y, boolean enabled) {
        int track = enabled ? PINK : OFF_TRACK;
        roundedRect(g, x, y, 28, 15, 8, track);

        int knobX = enabled ? x + 15 : x + 3;
        roundedRect(g, knobX, y + 3, 9, 9, 5, enabled ? 0xFFFFFFFF : OFF_KNOB);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();

        int w = Math.min(this.width - 16, TARGET_W);
        int h = Math.min(this.height - 16, TARGET_H);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        int itemY = y + 109;
        for (ModuleCategory category : new ModuleCategory[]{
                ModuleCategory.COMBAT,
                ModuleCategory.MOVEMENT,
                ModuleCategory.DONUT_SMP,
                ModuleCategory.VISUALS,
                ModuleCategory.MISC
        }) {
            if (inside(mouseX, mouseY, x + 12, itemY, 165, 38)) {
                selected = category;
                scrollOffset = 0;
                return true;
            }
            itemY += 42;
        }

        if (inside(mouseX, mouseY, x + 12, y + 365, 165, 38)) {
            selected = ModuleCategory.GUI;
            scrollOffset = 0;
            return true;
        }

        List<Module> modules = ModuleRegistry.byCategory(selected);
        int mainX = x + SIDEBAR_W;
        int left = mainX + 17;
        int top = y + 70 - scrollOffset;
        int gap = 10;
        int cardW = (w - SIDEBAR_W - 44) / 2;
        int cardH = 54;

        for (int i = 0; i < modules.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = left + col * (cardW + gap);
            int cy = top + row * (cardH + gap);
            if (inside(mouseX, mouseY, cx, cy, cardW, cardH)) {
                modules.get(i).toggle();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int rows = (ModuleRegistry.byCategory(selected).size() + 1) / 2;
        int contentHeight = rows * 64;
        int visible = Math.min(this.height - 16, TARGET_H) - 88;
        int max = Math.max(0, contentHeight - visible);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) (verticalAmount * 26)));
        return true;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static void roundedOutline(GuiGraphics g, int x, int y, int w, int h, int r, int border, int fill) {
        roundedRect(g, x, y, w, h, r, border);
        roundedRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), fill);
    }

    private static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(1, Math.min(r, Math.min(w, h) / 2));

        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + w, y + h - r, color);

        for (int i = 0; i < r; i++) {
            int inset = cornerInset(r, i);
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
    }

    private static int cornerInset(int radius, int row) {
        double dy = radius - row - 0.5;
        double dx = Math.sqrt(Math.max(0.0, radius * radius - dy * dy));
        return radius - (int) Math.floor(dx);
    }
}
