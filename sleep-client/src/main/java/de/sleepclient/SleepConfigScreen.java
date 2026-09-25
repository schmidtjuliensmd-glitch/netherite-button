package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SleepConfigScreen extends Screen {
    private enum Page { MODULES, PLAYER_ESP, SUS_CHUNK, THEME }

    private static final int W = 570;
    private static final int H = 410;
    private static final int SIDEBAR = 150;

    private Page page = Page.MODULES;
    private int scrollOffset = 0;

    public SleepConfigScreen() {
        super(Component.literal("Sleep Client Config"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int w = Math.min(width - 18, W);
        int h = Math.min(height - 18, H);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        SmoothShapeRenderer.glow(g, x, y, w, h, 14, alpha(ThemeConfig.accent, 72), 10);
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 14, 0xFF09070D);
        SmoothShapeRenderer.roundedRect(g, x + 1, y + 1, SIDEBAR - 1, h - 2, 13, 0xFF0C0811);
        g.fill(x + SIDEBAR, y + 1, x + SIDEBAR + 1, y + h - 1, 0xFF25152B);

        SmoothTextRenderer.draw(g, "Sleep Config", x + 18, y + 20, 12.0f, 0xFFF6F2F7, true);
        SmoothTextRenderer.draw(g, "Alle Einstellungen", x + 18, y + 39, 8.2f, 0xFF756A7B, false);

        drawNav(g, Page.MODULES, "Modules", x + 12, y + 76, mouseX, mouseY);
        drawNav(g, Page.PLAYER_ESP, "PlayerESP", x + 12, y + 118, mouseX, mouseY);
        drawNav(g, Page.SUS_CHUNK, "Sus Chunk", x + 12, y + 160, mouseX, mouseY);
        drawNav(g, Page.THEME, "Theme", x + 12, y + 202, mouseX, mouseY);

        SmoothTextRenderer.draw(g, "Zurück", x + 22, y + h - 30, 9.0f, 0xFFB6A9B8, true);

        int contentX = x + SIDEBAR + 18;
        int contentY = y + 18;

        switch (page) {
            case MODULES -> renderModules(g, contentX, contentY, w - SIDEBAR - 36, h - 36, mouseX, mouseY);
            case PLAYER_ESP -> renderPlayerEsp(g, contentX, contentY, w - SIDEBAR - 36);
            case SUS_CHUNK -> renderSusChunk(g, contentX, contentY, w - SIDEBAR - 36);
            case THEME -> renderTheme(g, contentX, contentY, w - SIDEBAR - 36);
        }
    }

    private void drawNav(GuiGraphics g, Page target, String label, int x, int y, int mouseX, int mouseY) {
        boolean active = page == target;
        boolean hover = inside(mouseX, mouseY, x, y, 126, 34);

        if (active || hover) {
            int bg = active ? 0xFF25132E : 0xFF151019;
            SmoothShapeRenderer.roundedRect(g, x, y, 126, 34, 9, bg);
            if (active) SmoothShapeRenderer.roundedRect(g, x + 1, y + 8, 2, 18, 2, ThemeConfig.accent);
        }

        SmoothTextRenderer.draw(g, label, x + 12, y + 10, 9.5f, active ? 0xFFFFFFFF : 0xFFAAA0AE, active);
    }

    private void renderModules(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        SmoothTextRenderer.draw(g, "Alle Module", x, y + 2, 12.0f, 0xFFF6F2F7, true);
        SmoothTextRenderer.draw(g, "Aktivierungen werden automatisch gespeichert.", x, y + 22, 8.2f, 0xFF85798A, false);

        List<Module> modules = ModuleRegistry.all();
        int top = y + 48 - scrollOffset;
        int rowH = 36;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int cy = top + i * rowH;
            if (cy < y + 42 || cy > y + h - 32) continue;

            boolean hover = inside(mouseX, mouseY, x, cy, w, 30);
            int bg = m.enabled() ? 0xFF201229 : (hover ? 0xFF17101C : 0xFF120C16);
            SmoothShapeRenderer.roundedRect(g, x, cy, w, 30, 8, bg);

            SmoothTextRenderer.draw(g, m.name(), x + 10, cy + 9, 8.8f, 0xFFE7E0EA, m.enabled());
            String cat = m.category().displayName();
            int cw = SmoothTextRenderer.width(cat, 7.7f, false);
            SmoothTextRenderer.draw(g, cat, x + w - 54 - cw, cy + 10, 7.7f, 0xFF776C7C, false);
            drawToggle(g, x + w - 40, cy + 8, m.enabled());
        }
    }

    private void renderPlayerEsp(GuiGraphics g, int x, int y, int w) {
        title(g, x, y, "PlayerESP", "Boxen um geladene Spieler und optionale Distanzliste.");

        setting(g, x, y + 58, w, "Reichweite", ConfigManager.playerEspRange + " Blöcke", "esp_range");
        setting(g, x, y + 110, w, "Linienstärke", String.format("%.1f", ConfigManager.playerEspLineWidth), "esp_line");
        toggleSetting(g, x, y + 162, w, "Distanz HUD", "Namen und Entfernung rechts oben", ConfigManager.playerEspDistanceHud);

        Module esp = ModuleRegistry.find("PlayerESP");
        if (esp != null) toggleSetting(g, x, y + 224, w, "PlayerESP aktiv", "Modul direkt ein oder ausschalten", esp.enabled());
    }

    private void renderSusChunk(GuiGraphics g, int x, int y, int w) {
        title(g, x, y, "Sus Chunk Finder", "Scanner für verdächtige bereits geladene Chunks.");

        setting(g, x, y + 58, w, "Scan Radius", ConfigManager.susScanRadius + " Chunks", "sus_radius");
        setting(g, x, y + 110, w, "Mindest Score", Integer.toString(ConfigManager.susMinScore), "sus_score");
        toggleSetting(g, x, y + 162, w, "HUD anzeigen", "Gefundene Chunks oben links anzeigen", ConfigManager.susHudEnabled);

        Module sus = ModuleRegistry.find("Sus Chunk Finder");
        if (sus != null) toggleSetting(g, x, y + 224, w, "Finder aktiv", "Modul direkt ein oder ausschalten", sus.enabled());
    }

    private void renderTheme(GuiGraphics g, int x, int y, int w) {
        title(g, x, y, "Theme", "Farben und Animationsgeschwindigkeit des Clients.");

        setting(g, x, y + 58, w, "Animation", String.format("%.2fx", ThemeConfig.animationSpeed), "animation");

        SmoothTextRenderer.draw(g, "Farb-Presets", x, y + 132, 8.6f, 0xFF9E93A3, true);
        themeButton(g, x, y + 151, 76, "Pink", 0xFFFF4F9F);
        themeButton(g, x + 86, y + 151, 76, "Purple", 0xFFB46CFF);
        themeButton(g, x + 172, y + 151, 76, "Blue", 0xFF55B8FF);
        themeButton(g, x + 258, y + 151, 76, "Green", 0xFF55E6B1);
    }

    private void title(GuiGraphics g, int x, int y, String title, String sub) {
        SmoothTextRenderer.draw(g, title, x, y + 2, 12.0f, 0xFFF6F2F7, true);
        SmoothTextRenderer.draw(g, sub, x, y + 23, 8.2f, 0xFF85798A, false);
    }

    private void setting(GuiGraphics g, int x, int y, int w, String label, String value, String id) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, 42, 9, 0xFF130C17);
        SmoothTextRenderer.draw(g, label, x + 11, y + 8, 8.8f, 0xFFE7E0EA, true);
        SmoothTextRenderer.draw(g, value, x + 11, y + 24, 7.8f, 0xFF8A7E8F, false);

        button(g, x + w - 62, y + 8, 22, 26, "−");
        button(g, x + w - 32, y + 8, 22, 26, "+");
    }

    private void toggleSetting(GuiGraphics g, int x, int y, int w, String label, String sub, boolean on) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, 52, 9, 0xFF130C17);
        SmoothTextRenderer.draw(g, label, x + 11, y + 8, 8.8f, 0xFFE7E0EA, true);
        SmoothTextRenderer.draw(g, sub, x + 11, y + 25, 7.7f, 0xFF817587, false);
        drawToggle(g, x + w - 40, y + 18, on);
    }

    private void themeButton(GuiGraphics g, int x, int y, int w, String label, int color) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, 34, 9, 0xFF160E1B);
        SmoothShapeRenderer.roundedRect(g, x + 7, y + 8, 18, 18, 6, color);
        SmoothTextRenderer.draw(g, label, x + 31, y + 11, 8.2f, 0xFFE4DCE7, true);
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String text) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 7, 0xFF25152D);
        int tw = SmoothTextRenderer.width(text, 10.0f, true);
        SmoothTextRenderer.draw(g, text, x + (w - tw) / 2, y + 7, 10.0f, 0xFFFFFFFF, true);
    }

    private void drawToggle(GuiGraphics g, int x, int y, boolean on) {
        SmoothShapeRenderer.roundedRect(g, x, y, 28, 14, 8, on ? ThemeConfig.accent : 0xFF46515D);
        SmoothShapeRenderer.roundedRect(g, x + (on ? 16 : 3), y + 3, 9, 8, 5, on ? 0xFFFFFFFF : 0xFFB0B7BF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int w = Math.min(width - 18, W);
        int h = Math.min(height - 18, H);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        if (inside(mx, my, x + 12, y + 76, 126, 34)) { page = Page.MODULES; scrollOffset = 0; return true; }
        if (inside(mx, my, x + 12, y + 118, 126, 34)) { page = Page.PLAYER_ESP; return true; }
        if (inside(mx, my, x + 12, y + 160, 126, 34)) { page = Page.SUS_CHUNK; return true; }
        if (inside(mx, my, x + 12, y + 202, 126, 34)) { page = Page.THEME; return true; }

        if (inside(mx, my, x + 12, y + h - 42, 126, 36)) {
            Minecraft.getInstance().setScreen(new SleepClickGuiScreen());
            return true;
        }

        int cx = x + SIDEBAR + 18;
        int cy = y + 18;
        int cw = w - SIDEBAR - 36;

        if (page == Page.MODULES) {
            List<Module> modules = ModuleRegistry.all();
            int top = cy + 48 - scrollOffset;
            for (int i = 0; i < modules.size(); i++) {
                int ry = top + i * 36;
                if (inside(mx, my, cx, ry, cw, 30)) {
                    modules.get(i).toggle();
                    ConfigManager.save();
                    return true;
                }
            }
        }

        if (page == Page.PLAYER_ESP) {
            if (inside(mx, my, cx + cw - 62, cy + 66, 22, 26)) {
                ConfigManager.playerEspRange = Math.max(16, ConfigManager.playerEspRange - 16);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 32, cy + 66, 22, 26)) {
                ConfigManager.playerEspRange = Math.min(256, ConfigManager.playerEspRange + 16);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 62, cy + 118, 22, 26)) {
                ConfigManager.playerEspLineWidth = Math.max(1.0f, ConfigManager.playerEspLineWidth - 0.2f);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 32, cy + 118, 22, 26)) {
                ConfigManager.playerEspLineWidth = Math.min(5.0f, ConfigManager.playerEspLineWidth + 0.2f);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx, cy + 162, cw, 52)) {
                ConfigManager.playerEspDistanceHud = !ConfigManager.playerEspDistanceHud;
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx, cy + 224, cw, 52)) {
                Module m = ModuleRegistry.find("PlayerESP");
                if (m != null) m.toggle();
                ConfigManager.save(); return true;
            }
        }

        if (page == Page.SUS_CHUNK) {
            if (inside(mx, my, cx + cw - 62, cy + 66, 22, 26)) {
                ConfigManager.susScanRadius = Math.max(2, ConfigManager.susScanRadius - 1);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 32, cy + 66, 22, 26)) {
                ConfigManager.susScanRadius = Math.min(12, ConfigManager.susScanRadius + 1);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 62, cy + 118, 22, 26)) {
                ConfigManager.susMinScore = Math.max(1, ConfigManager.susMinScore - 1);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 32, cy + 118, 22, 26)) {
                ConfigManager.susMinScore = Math.min(40, ConfigManager.susMinScore + 1);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx, cy + 162, cw, 52)) {
                ConfigManager.susHudEnabled = !ConfigManager.susHudEnabled;
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx, cy + 224, cw, 52)) {
                Module m = ModuleRegistry.find("Sus Chunk Finder");
                if (m != null) m.toggle();
                ConfigManager.save(); return true;
            }
        }

        if (page == Page.THEME) {
            if (inside(mx, my, cx + cw - 62, cy + 66, 22, 26)) {
                ThemeConfig.animationSpeed = Math.max(0.35f, ThemeConfig.animationSpeed - 0.1f);
                ConfigManager.save(); return true;
            }
            if (inside(mx, my, cx + cw - 32, cy + 66, 22, 26)) {
                ThemeConfig.animationSpeed = Math.min(2.0f, ThemeConfig.animationSpeed + 0.1f);
                ConfigManager.save(); return true;
            }

            if (inside(mx, my, cx, cy + 151, 76, 34)) { ConfigManager.applyThemePreset("pink"); return true; }
            if (inside(mx, my, cx + 86, cy + 151, 76, 34)) { ConfigManager.applyThemePreset("purple"); return true; }
            if (inside(mx, my, cx + 172, cy + 151, 76, 34)) { ConfigManager.applyThemePreset("blue"); return true; }
            if (inside(mx, my, cx + 258, cy + 151, 76, 34)) { ConfigManager.applyThemePreset("green"); return true; }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (page == Page.MODULES) {
            int content = ModuleRegistry.all().size() * 36;
            int max = Math.max(0, content - 315);
            scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int)(verticalAmount * 28)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private static int alpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
