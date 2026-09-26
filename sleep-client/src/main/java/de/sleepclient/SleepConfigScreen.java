package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SleepConfigScreen extends Screen {
    private enum Page { MODULES, THEME }

    private static final int W = 620;
    private static final int H = 438;
    private static final int SIDEBAR = 154;

    private static final Set<String> FUNCTIONAL = Set.of(
            "PlayerESP","StorageESP","BlockESP","ChunkFinder","Sus Chunk Finder",
            "Netherite Finder","LootESP","TraderFinder","Totem Counter",
            "Staff List","RTP Base Alert","Deep Player Finder","RegionMap","SeedAnalyzer",
            "Auto Elytra Finder","SpawnerProtect","FakeStats","Fullbright","Sprint",
            "BaseFinder","StashFinder","ChunkFinderV2","SusChunkFinderV2","SpawnerFinder",
            "Beacon Finder","Shulker Finder","ChestCounter","ChunkActivity",
            "Watermark","Module List","FPS Counter","Coordinates HUD","Direction HUD",
            "CPS Counter","Armor HUD","Session Info","Low Health Alert","Durability Alert",
            "Crosshair","EntityESP","MobESP","ProjectileESP","Tracers","Target ESP",
            "Heat Color","Health Bars","Block Overlay","Block Highlight","Zoom","FOV Changer",
            "Auto Walk","Sneak","Bunny Hop","High Jump","Fast Fall","Speed","Flight",
            "Elytra Boost","Long Jump","Anti Void","Aim Assist","Bow Aim","KillAura",
            "Trigger Bot","AutoClicker","Criticals","Anti AFK","Auto Tool","Auto Eat",
            "Auto Gap","Auto XP","Auto Leave","Auto Totem","Auto Inventory Totem","Auto Refill Hotbar","Auto Weapon","Auto Armor","Mace Swap","Key Pearl","Auto Respawn","Auto Reconnect","No Weather","Time Changer","No Hurt Cam","Potion HUD","Keystrokes","Pearl Cooldown","Auto Shield Disabler","Shield Breaker","Auto Rod","Bow Spam","Auto Bow Release","Auto Mace","No Flame","No Explosions","Radar","MiniMap","Waypoints"
    );

    private Page page = Page.MODULES;
    private Module selectedModule;
    private int moduleScroll;
    private int settingsScroll;

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

        renderSidebar(g, x, y, h, mouseX, mouseY);

        int contentX = x + SIDEBAR + 18;
        int contentY = y + 18;
        int contentW = w - SIDEBAR - 36;
        int contentH = h - 36;

        if (page == Page.THEME) {
            renderTheme(g, contentX, contentY, contentW);
        } else if (selectedModule != null) {
            renderModuleSettings(g, contentX, contentY, contentW, contentH);
        } else {
            renderModules(g, contentX, contentY, contentW, contentH, mouseX, mouseY);
        }
    }

    private void renderSidebar(GuiGraphics g, int x, int y, int h, int mouseX, int mouseY) {
        SmoothTextRenderer.draw(g, "Sleep Config", x + 18, y + 20, 12.0f, 0xFFF6F2F7, true);
        SmoothTextRenderer.draw(g, "Module & Einstellungen", x + 18, y + 39, 7.8f, 0xFF756A7B, false);

        drawNav(g, "Modules", x + 12, y + 78, 128, 36, page == Page.MODULES, mouseX, mouseY);
        drawNav(g, "Theme", x + 12, y + 120, 128, 36, page == Page.THEME, mouseX, mouseY);

        SmoothTextRenderer.draw(g, selectedModule != null ? "‹ Module" : "‹ Zurück",
                x + 20, y + h - 30, 9.0f, 0xFFB6A9B8, true);
    }

    private void drawNav(GuiGraphics g, String label, int x, int y, int w, int h,
                         boolean active, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        if (active || hover) {
            SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, active ? 0xFF25132E : 0xFF151019);
            if (active) SmoothShapeRenderer.roundedRect(g, x + 1, y + 9, 2, 18, 2, ThemeConfig.accent);
        }
        SmoothTextRenderer.draw(g, label, x + 12, y + 11, 9.4f, active ? 0xFFFFFFFF : 0xFFAAA0AE, active);
    }

    private void renderModules(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        SmoothTextRenderer.draw(g, "Alle Module", x, y + 1, 12.0f, 0xFFF6F2F7, true);
        SmoothTextRenderer.draw(g, "Klicke ein Modul für seine eigenen Einstellungen.", x, y + 22, 8.0f, 0xFF85798A, false);

        List<Module> modules = ModuleRegistry.all();
        int top = y + 48 - moduleScroll;
        int rowH = 42;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int cy = top + i * rowH;
            if (cy < y + 40 || cy > y + h - 34) continue;

            boolean hover = inside(mouseX, mouseY, x, cy, w, 36);
            int bg = module.enabled() ? 0xFF211329 : (hover ? 0xFF18101D : 0xFF120C16);
            SmoothShapeRenderer.roundedRect(g, x, cy, w, 36, 9, bg);

            SmoothTextRenderer.draw(g, module.name(), x + 11, cy + 7, 8.9f, 0xFFE9E2EC, module.enabled());
            SmoothTextRenderer.draw(g, module.category().displayName(), x + 11, cy + 22, 7.3f, 0xFF746979, false);

            String status = FUNCTIONAL.contains(module.name()) ? "LIVE" : "CONFIG";
            int statusColor = FUNCTIONAL.contains(module.name()) ? 0xFF55E6B1 : 0xFFB46CFF;
            int sw = SmoothTextRenderer.width(status, 6.8f, true);
            SmoothTextRenderer.draw(g, status, x + w - 79 - sw, cy + 14, 6.8f, statusColor, true);

            drawToggle(g, x + w - 39, cy + 11, module.enabled());
        }
    }

    private void renderModuleSettings(GuiGraphics g, int x, int y, int w, int h) {
        Module module = selectedModule;
        if (module == null) return;

        SmoothTextRenderer.draw(g, module.name(), x, y + 1, 12.4f, 0xFFF7F3F8, true);
        SmoothTextRenderer.draw(g, module.description(), x, y + 22, 7.8f, 0xFF85798A, false);

        String status = FUNCTIONAL.contains(module.name())
                ? "Runtime verfügbar"
                : "Konfiguration vorbereitet · Runtime folgt";
        SmoothTextRenderer.draw(g, status, x, y + 39, 7.2f,
                FUNCTIONAL.contains(module.name()) ? 0xFF55E6B1 : 0xFFB8A2CC, true);

        int toggleY = y + 58;
        settingShell(g, x, toggleY, w, 48);
        SmoothTextRenderer.draw(g, "Modul aktiv", x + 12, toggleY + 9, 8.8f, 0xFFE8E0EB, true);
        SmoothTextRenderer.draw(g, module.enabled() ? "Eingeschaltet" : "Ausgeschaltet",
                x + 12, toggleY + 25, 7.5f, 0xFF827689, false);
        drawToggle(g, x + w - 40, toggleY + 17, module.enabled());

        List<ModuleSettingsRegistry.SettingSpec> specs = ModuleSettingsRegistry.settingsFor(module);
        int top = y + 118 - settingsScroll;
        int rowH = 56;

        for (int i = 0; i < specs.size(); i++) {
            ModuleSettingsRegistry.SettingSpec spec = specs.get(i);
            int cy = top + i * rowH;
            if (cy < y + 108 || cy > y + h - 42) continue;
            renderSetting(g, module, spec, x, cy, w);
        }
    }

    private void renderSetting(GuiGraphics g, Module module, ModuleSettingsRegistry.SettingSpec spec,
                               int x, int y, int w) {
        settingShell(g, x, y, w, 49);

        SmoothTextRenderer.draw(g, spec.label(), x + 12, y + 8, 8.6f, 0xFFE7E0EA, true);
        SmoothTextRenderer.draw(g, spec.description(), x + 12, y + 24, 7.2f, 0xFF786D7E, false);

        String raw = ConfigManager.rawOption(module.name(), spec);
        String value = displayValue(spec, raw);

        if (spec.type() == ModuleSettingsRegistry.Type.BOOLEAN) {
            boolean on = Boolean.parseBoolean(raw);
            drawToggle(g, x + w - 40, y + 17, on);
        } else {
            int valueWidth = SmoothTextRenderer.width(value, 7.8f, true);
            int valueX = x + w - 83 - valueWidth;
            SmoothTextRenderer.draw(g, value, valueX, y + 19, 7.8f, ThemeConfig.accent, true);
            smallButton(g, x + w - 61, y + 12, 23, 26, "−");
            smallButton(g, x + w - 31, y + 12, 23, 26, "+");
        }
    }

    private void renderTheme(GuiGraphics g, int x, int y, int w) {
        SmoothTextRenderer.draw(g, "Theme", x, y + 1, 12.4f, 0xFFF7F3F8, true);
        SmoothTextRenderer.draw(g, "Farben und allgemeines Erscheinungsbild.", x, y + 22, 7.8f, 0xFF85798A, false);

        SmoothTextRenderer.draw(g, "Farb-Presets", x, y + 66, 8.6f, 0xFF9E93A3, true);
        themeButton(g, x, y + 88, 80, "Pink", 0xFFFF4F9F);
        themeButton(g, x + 90, y + 88, 80, "Purple", 0xFFB46CFF);
        themeButton(g, x + 180, y + 88, 80, "Blue", 0xFF55B8FF);
        themeButton(g, x + 270, y + 88, 80, "Green", 0xFF55E6B1);

        settingShell(g, x, y + 144, w, 49);
        SmoothTextRenderer.draw(g, "Animationsgeschwindigkeit", x + 12, y + 153, 8.6f, 0xFFE7E0EA, true);
        SmoothTextRenderer.draw(g, String.format(Locale.ROOT, "%.2fx", ThemeConfig.animationSpeed),
                x + 12, y + 170, 7.7f, ThemeConfig.accent, true);
        smallButton(g, x + w - 61, y + 156, 23, 26, "−");
        smallButton(g, x + w - 31, y + 156, 23, 26, "+");
    }

    private void settingShell(GuiGraphics g, int x, int y, int w, int h) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xFF130C17);
    }

    private void themeButton(GuiGraphics g, int x, int y, int w, String label, int color) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, 34, 9, 0xFF160E1B);
        SmoothShapeRenderer.roundedRect(g, x + 7, y + 8, 18, 18, 6, color);
        SmoothTextRenderer.draw(g, label, x + 31, y + 11, 8.0f, 0xFFE4DCE7, true);
    }

    private void smallButton(GuiGraphics g, int x, int y, int w, int h, String text) {
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 7, 0xFF281631);
        int tw = SmoothTextRenderer.width(text, 10.0f, true);
        SmoothTextRenderer.draw(g, text, x + (w - tw) / 2, y + 7, 10.0f, 0xFFFFFFFF, true);
    }

    private void drawToggle(GuiGraphics g, int x, int y, boolean on) {
        SmoothShapeRenderer.roundedRect(g, x, y, 28, 14, 8, on ? ThemeConfig.accent : 0xFF46515D);
        SmoothShapeRenderer.roundedRect(g, x + (on ? 16 : 3), y + 3, 9, 8, 5, on ? 0xFFFFFFFF : 0xFFB0B7BF);
    }

    private String displayValue(ModuleSettingsRegistry.SettingSpec spec, String raw) {
        if (spec.type() == ModuleSettingsRegistry.Type.FLOAT) {
            try {
                double v = Double.parseDouble(raw);
                if (Math.abs(v - Math.rint(v)) < 0.0001) return String.format(Locale.ROOT, "%.1f", v);
                return String.format(Locale.ROOT, "%.2f", v);
            } catch (Exception ignored) {
                return raw;
            }
        }
        return raw;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        int w = Math.min(width - 18, W);
        int h = Math.min(height - 18, H);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        if (inside(mx, my, x + 12, y + 78, 128, 36)) {
            page = Page.MODULES;
            selectedModule = null;
            moduleScroll = 0;
            return true;
        }

        if (inside(mx, my, x + 12, y + 120, 128, 36)) {
            page = Page.THEME;
            selectedModule = null;
            return true;
        }

        if (inside(mx, my, x + 12, y + h - 43, 128, 38)) {
            if (selectedModule != null) {
                selectedModule = null;
                settingsScroll = 0;
            } else {
                Minecraft.getInstance().setScreen(new SleepClickGuiScreen());
            }
            return true;
        }

        int cx = x + SIDEBAR + 18;
        int cy = y + 18;
        int cw = w - SIDEBAR - 36;

        if (page == Page.THEME) {
            if (inside(mx, my, cx, cy + 88, 80, 34)) { ConfigManager.applyThemePreset("pink"); return true; }
            if (inside(mx, my, cx + 90, cy + 88, 80, 34)) { ConfigManager.applyThemePreset("purple"); return true; }
            if (inside(mx, my, cx + 180, cy + 88, 80, 34)) { ConfigManager.applyThemePreset("blue"); return true; }
            if (inside(mx, my, cx + 270, cy + 88, 80, 34)) { ConfigManager.applyThemePreset("green"); return true; }

            if (inside(mx, my, cx + cw - 61, cy + 156, 23, 26)) {
                ThemeConfig.animationSpeed = Math.max(0.35f, ThemeConfig.animationSpeed - 0.1f);
                ConfigManager.save();
                return true;
            }
            if (inside(mx, my, cx + cw - 31, cy + 156, 23, 26)) {
                ThemeConfig.animationSpeed = Math.min(2.0f, ThemeConfig.animationSpeed + 0.1f);
                ConfigManager.save();
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        if (selectedModule == null) {
            List<Module> modules = ModuleRegistry.all();
            int top = cy + 48 - moduleScroll;
            for (int i = 0; i < modules.size(); i++) {
                int ry = top + i * 42;
                if (!inside(mx, my, cx, ry, cw, 36)) continue;

                Module module = modules.get(i);
                if (mx >= cx + cw - 52) {
                    module.toggle();
                    ConfigManager.save();
                } else {
                    selectedModule = module;
                    settingsScroll = 0;
                }
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        int toggleY = cy + 58;
        if (inside(mx, my, cx, toggleY, cw, 48)) {
            selectedModule.toggle();
            ConfigManager.save();
            return true;
        }

        List<ModuleSettingsRegistry.SettingSpec> specs = ModuleSettingsRegistry.settingsFor(selectedModule);
        int top = cy + 118 - settingsScroll;

        for (int i = 0; i < specs.size(); i++) {
            ModuleSettingsRegistry.SettingSpec spec = specs.get(i);
            int ry = top + i * 56;
            if (!inside(mx, my, cx, ry, cw, 49)) continue;

            if (spec.type() == ModuleSettingsRegistry.Type.BOOLEAN) {
                ConfigManager.adjust(selectedModule.name(), spec, 1);
            } else if (mx >= cx + cw - 66 && mx <= cx + cw - 34) {
                ConfigManager.adjust(selectedModule.name(), spec, -1);
            } else if (mx >= cx + cw - 34) {
                ConfigManager.adjust(selectedModule.name(), spec, 1);
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (page != Page.MODULES) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        if (selectedModule == null) {
            int content = ModuleRegistry.all().size() * 42;
            int max = Math.max(0, content - 335);
            moduleScroll = Math.max(0, Math.min(max, moduleScroll - (int)(verticalAmount * 30)));
            return true;
        }

        int content = ModuleSettingsRegistry.settingsFor(selectedModule).size() * 56;
        int max = Math.max(0, content - 250);
        settingsScroll = Math.max(0, Math.min(max, settingsScroll - (int)(verticalAmount * 28)));
        return true;
    }

    private static int alpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
