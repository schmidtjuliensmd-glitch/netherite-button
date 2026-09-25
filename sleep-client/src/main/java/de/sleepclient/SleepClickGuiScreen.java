package de.sleepclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SleepClickGuiScreen extends Screen {
    private static final int TARGET_W = 604;
    private static final int TARGET_H = 498;
    private static final int SIDEBAR_W = 188;

    private static final int BG = 0xFF09070D;
    private static final int SIDEBAR_BG = 0xFF0B0810;
    private static final int MAIN_BG = 0xFF110A17;
    private static final int CARD_BG = 0xFF18101D;
    private static final int CARD_HOVER = 0xFF1C1222;
    private static final int LINE = 0xFF34223B;
    private static final int LINE_SOFT = 0xFF241729;
    private static final int TEXT = 0xFFF5F1F7;
    private static final int MUTED = 0xFFAAA1AF;
    private static final int MUTED_DARK = 0xFF706777;
    private static final int OFF_TRACK = 0xFF46515D;
    private static final int OFF_KNOB = 0xFFA8B0B9;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final long openedAt = System.nanoTime();
    private final Map<Module, Float> toggleAnim = new HashMap<>();
    private final Map<Module, Float> hoverAnim = new HashMap<>();

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

        float progress = Math.min(1.0f, (System.nanoTime() - openedAt) / 240_000_000.0f);
        float eased = 1.0f - (float)Math.pow(1.0f - progress, 3.0);
        float scale = 0.965f + 0.035f * eased;

        g.pose().pushMatrix();
        g.pose().translate(this.width / 2.0f, this.height / 2.0f);
        g.pose().scale(scale, scale);
        g.pose().translate(-this.width / 2.0f, -this.height / 2.0f);

        renderGlow(g, x, y, w, h, eased);

        roundedRect(g, x, y, w, h, 12, BG);
        roundedOutline(g, x, y, w, h, 12, 0xFF38243E, BG);

        roundedRect(g, x + 1, y + 1, SIDEBAR_W - 1, h - 2, 11, SIDEBAR_BG);
        g.fill(x + SIDEBAR_W, y + 1, x + w - 1, y + h - 1, MAIN_BG);
        g.fill(x + SIDEBAR_W, y + 1, x + SIDEBAR_W + 1, y + h - 1, LINE_SOFT);

        renderBrand(g, x, y);
        renderSidebar(g, x, y, mouseX, mouseY);
        renderMain(g, x, y, w, h, mouseX, mouseY);

        g.pose().popMatrix();
    }

    private void renderGlow(GuiGraphics g, int x, int y, int w, int h, float amount) {
        int accent = accent();
        int glowAlpha = Math.max(1, Math.round(85 * amount));
        SmoothShapeRenderer.glow(g, x, y, w, h, 14, withAlpha(accent, glowAlpha), 12);

        int purple = ThemeConfig.accentSecondary;
        int a = Math.max(1, Math.round(24 * amount));
        SmoothShapeRenderer.glow(g, x + w - 160, y + h - 78, 125, 45, 20, withAlpha(purple, a), 10);
    }

    private void renderBrand(GuiGraphics g, int x, int y) {
        int accent = accent();

        SmoothShapeRenderer.glow(g, x + 19, y + 20, 33, 33, 9, withAlpha(accent, 115), 6);
        roundedRect(g, x + 19, y + 20, 33, 33, 9, accent);
        roundedRect(g, x + 22, y + 23, 27, 27, 8, withAlpha(ThemeConfig.accentSecondary, 62));

        SmoothTextRenderer.draw(g, "☾", x + 28, y + 25, 14.0f, 0xFFFFFFFF, true);
        SmoothTextRenderer.draw(g, "Sleep Client", x + 59, y + 23, 11.7f, TEXT, true);
        SmoothTextRenderer.draw(g, UpdateManager.currentVersion(), x + 59, y + 39, 8.2f, MUTED_DARK, false);
    }

    private void renderSidebar(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        SmoothTextRenderer.draw(g, "MODULE", x + 20, y + 85, 8.0f, MUTED_DARK, true);

        int itemY = y + 108;
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

        SmoothTextRenderer.draw(g, "ALLGEMEIN", x + 20, y + 340, 8.0f, MUTED_DARK, true);

        renderBottomItem(g, "⚙", "GUI", x + 12, y + 363, 165, 38, selected == ModuleCategory.GUI, mouseX, mouseY);
        renderBottomItem(g, "⌁", "Settings", x + 12, y + 405, 165, 38, false, mouseX, mouseY);
    }

    private void renderCategory(GuiGraphics g, ModuleCategory category, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean active = selected == category;
        boolean hover = inside(mouseX, mouseY, x, y, w, h);

        if (active) {
            roundedRect(g, x, y, w, h, 9, 0xFF27142F);
            roundedOutline(g, x, y, w, h, 9, withAlpha(accent(), 105), 0xFF27142F);
            roundedRect(g, x + 1, y + 8, 2, h - 16, 2, accent());
        } else if (hover) {
            roundedRect(g, x, y, w, h, 9, 0xFF151019);
        }

        String icon = switch (category) {
            case COMBAT -> "⚔";
            case MOVEMENT -> "➜";
            case DONUT_SMP -> "◉";
            case VISUALS -> "◇";
            case MISC -> "◆";
            default -> "•";
        };

        int color = active ? TEXT : MUTED;
        SmoothTextRenderer.draw(g, icon, x + 9, y + 10, 10.0f, color, false);
        SmoothTextRenderer.draw(g, category.displayName(), x + 28, y + 10, 10.2f, color, active);
    }

    private void renderBottomItem(GuiGraphics g, String icon, String label, int x, int y, int w, int h,
                                  boolean active, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        if (active) {
            roundedRect(g, x, y, w, h, 9, 0xFF27142F);
            roundedOutline(g, x, y, w, h, 9, withAlpha(accent(), 95), 0xFF27142F);
        } else if (hover) {
            roundedRect(g, x, y, w, h, 9, 0xFF151019);
        }

        int color = active ? TEXT : MUTED;
        SmoothTextRenderer.draw(g, icon, x + 9, y + 10, 10.0f, color, false);
        SmoothTextRenderer.draw(g, label, x + 28, y + 10, 10.2f, color, active);
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

        SmoothTextRenderer.draw(g, title, mainX + 17, y + 20, 11.7f, TEXT, true);
        SmoothTextRenderer.draw(g, LocalTime.now().format(TIME), mainX + 174, y + 20, 10.0f, accent(), true);

        int searchX = x + w - 196;
        int searchY = y + 17;
        roundedRect(g, searchX, searchY, 186, 32, 9, 0xFF1A1120);
        roundedOutline(g, searchX, searchY, 186, 32, 9, LINE, 0xFF1A1120);
        SmoothTextRenderer.draw(g, "Module suchen...", searchX + 11, searchY + 8, 9.0f, MUTED_DARK, false);

        g.fill(mainX + 17, y + 55, x + w - 10, y + 56, withAlpha(accent(), 58));

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

        float hoverNow = hoverAnim.getOrDefault(module, 0.0f);
        hoverNow += ((hover ? 1.0f : 0.0f) - hoverNow) * 0.18f * Math.max(0.3f, ThemeConfig.animationSpeed);
        hoverAnim.put(module, hoverNow);

        float toggleNow = toggleAnim.getOrDefault(module, module.enabled() ? 1.0f : 0.0f);
        toggleNow += ((module.enabled() ? 1.0f : 0.0f) - toggleNow) * 0.22f * Math.max(0.3f, ThemeConfig.animationSpeed);
        toggleAnim.put(module, toggleNow);

        int bg = mix(CARD_BG, CARD_HOVER, hoverNow * 0.72f);
        int border = mix(LINE, accent(), module.enabled() ? 0.76f : hoverNow * 0.30f);

        if (module.enabled()) {
            SmoothShapeRenderer.glow(g, x, y, w, h, 10, withAlpha(accent(), 78), 5);
        }

        roundedRect(g, x, y, w, h, 10, bg);
        roundedOutline(g, x, y, w, h, 10, border, bg);

        SmoothTextRenderer.draw(g, module.name(), x + 12, y + 17, 10.2f, TEXT, true);

        int trackX = x + w - 39;
        int trackY = y + 19;
        renderToggle(g, trackX, trackY, toggleNow);
    }

    private void renderToggle(GuiGraphics g, int x, int y, float enabledAmount) {
        int track = mix(OFF_TRACK, accent(), enabledAmount);
        roundedRect(g, x, y, 28, 15, 8, track);

        float knob = x + 3 + 12.0f * enabledAmount;
        roundedRect(g, Math.round(knob), y + 3, 9, 9, 5, mix(OFF_KNOB, 0xFFFFFFFF, enabledAmount));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();

        int w = Math.min(this.width - 16, TARGET_W);
        int h = Math.min(this.height - 16, TARGET_H);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        int itemY = y + 108;
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

        if (inside(mouseX, mouseY, x + 12, y + 363, 165, 38)) {
            selected = ModuleCategory.GUI;
            scrollOffset = 0;
            return true;
        }

        if (inside(mouseX, mouseY, x + 12, y + 405, 165, 38)) {
            this.minecraft.setScreen(new SleepConfigScreen());
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
                ConfigManager.save();
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
        scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int)(verticalAmount * 26)));
        return true;
    }

    private static int accent() {
        return ThemeConfig.accent;
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    private static int mix(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int aa = (a >>> 24) & 0xFF;
        int ar = (a >>> 16) & 0xFF;
        int ag = (a >>> 8) & 0xFF;
        int ab = a & 0xFF;

        int ba = (b >>> 24) & 0xFF;
        int br = (b >>> 16) & 0xFF;
        int bg = (b >>> 8) & 0xFF;
        int bb = b & 0xFF;

        int oa = Math.round(aa + (ba - aa) * t);
        int or = Math.round(ar + (br - ar) * t);
        int og = Math.round(ag + (bg - ag) * t);
        int ob = Math.round(ab + (bb - ab) * t);

        return (oa << 24) | (or << 16) | (og << 8) | ob;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static void roundedOutline(GuiGraphics g, int x, int y, int w, int h, int r, int border, int fill) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, r, border);
        SmoothShapeRenderer.roundedRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), fill);
    }

    private static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, r, color);
    }
}
