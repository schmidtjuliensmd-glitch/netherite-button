package de.sleepclient;

import net.minecraft.client.gui.GuiGraphics;
import java.util.List;
import java.util.Locale;

/** Module-specific controls embedded in the reference GUI, opened only by right click. */
final class ModuleSettingsPanel {
    private static final int X = 205, WIDTH = 393, TOP = 166, BOTTOM = 482, ROW = 76;
    private final Module module;
    private final List<ModuleSettingsRegistry.SettingSpec> specs;
    private int scroll;
    private int dragging = -1;
    private boolean dirty;

    ModuleSettingsPanel(Module module) {
        this.module = module;
        this.specs = ModuleSettingsRegistry.settingsFor(module);
    }

    void render(GuiGraphics g, GuiLayout layout, double mx, double my) {
        boolean backHover = GuiLayout.inside(mx, my, X, 17, 32, 32);
        SmoothShapeRenderer.roundedRect(g, X, 17, 32, 32, 9, backHover ? 0xFF302035 : 0xFF211527);
        SmoothTextRenderer.draw(g, "‹", X + 10, 19, 22f, 0xFFF6F1F8, false);
        SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(module.name(), 13f, true, 340),
                X + 43, 18, 13f, 0xFFF6F1F8, true);
        SmoothTextRenderer.draw(g, "Moduleinstellungen", X + 43, 37, 9.5f, 0xFF9F909C, false);
        g.fill(X, 54, 598, 55, SleepClickGuiScreen.alpha(ThemeConfig.accent, 46));

        shell(g, 69, 54);
        SmoothTextRenderer.draw(g, "Modul aktiv", X + 12, 86, 12f, 0xFFCDBFCA, true);
        SleepClickGuiScreen.drawToggle(g, X + WIDTH - 40, 88, module.enabled() ? 1f : 0f);
        SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(module.description(), 10f, false, WIDTH - 24),
                X + 12, 134, 10f, 0xFF9F909C, false);

        SleepClickGuiScreen.clip(g, layout, X, TOP, X + WIDTH, BOTTOM);
        for (int i = 0; i < specs.size(); i++) {
            int y = TOP + i * ROW - scroll;
            if (y + 68 <= TOP || y >= BOTTOM) continue;
            renderSetting(g, specs.get(i), y);
        }
        if (specs.isEmpty()) {
            SmoothTextRenderer.draw(g, "Für dieses Modul gibt es keine weiteren Einstellungen.",
                    X + 12, TOP + 18, 10.5f, 0xFF9F909C, false);
        }
        g.disableScissor();
        int maximum = maxScroll();
        if (maximum > 0) {
            int height = BOTTOM - TOP;
            int thumb = Math.max(24, Math.round(height * (float) height / (maximum + height)));
            int y = TOP + Math.round((height - thumb) * scroll / (float) maximum);
            SmoothShapeRenderer.roundedRect(g, 600, y, 2, thumb, 1, SleepClickGuiScreen.alpha(ThemeConfig.accent, 130));
        }
    }

    private void renderSetting(GuiGraphics g, ModuleSettingsRegistry.SettingSpec spec, int y) {
        shell(g, y, 68);
        String raw = ConfigManager.rawOption(module.name(), spec);
        boolean numeric = spec.type() == ModuleSettingsRegistry.Type.INTEGER || spec.type() == ModuleSettingsRegistry.Type.FLOAT;
        int labelWidth = numeric ? 220 : WIDTH - 72;
        SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(spec.label(), 11.5f, true, labelWidth),
                X + 12, y + 9, 11.5f, 0xFFCDBFCA, true);
        if (spec.type() == ModuleSettingsRegistry.Type.BOOLEAN) {
            SleepClickGuiScreen.drawToggle(g, X + WIDTH - 40, y + 11, Boolean.parseBoolean(raw) ? 1f : 0f);
            SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(spec.description(), 9.5f, false, WIDTH - 24),
                    X + 12, y + 37, 9.5f, 0xFF9F909C, false);
        } else if (spec.type() == ModuleSettingsRegistry.Type.CHOICE) {
            SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(spec.description(), 9f, false, WIDTH - 24),
                    X + 12, y + 26, 9f, 0xFF9F909C, false);
            SmoothShapeRenderer.roundedRect(g, X + 12, y + 42, WIDTH - 24, 20, 6, 0xFF25172C);
            SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(raw, 10.5f, true, WIDTH - 94),
                    X + 20, y + 44, 10.5f, ThemeConfig.accent, true);
            smallButton(g, X + WIDTH - 60, y + 40, "‹");
            smallButton(g, X + WIDTH - 32, y + 40, "›");
        } else {
            String value = spec.type() == ModuleSettingsRegistry.Type.INTEGER
                    ? Integer.toString((int) number(spec, raw)) : String.format(Locale.ROOT, "%.2f", number(spec, raw));
            int valueWidth = SmoothTextRenderer.width(value, 10.5f, true);
            SmoothTextRenderer.draw(g, value, X + WIDTH - 74 - valueWidth, y + 10, 10.5f, ThemeConfig.accent, true);
            smallButton(g, X + WIDTH - 60, y + 5, "−");
            smallButton(g, X + WIDTH - 32, y + 5, "+");
            SmoothTextRenderer.draw(g, SmoothTextRenderer.fit(spec.description(), 9f, false, WIDTH - 24),
                    X + 12, y + 30, 9f, 0xFF9F909C, false);
            double span = spec.max() - spec.min();
            float amount = span <= 0 ? 0 : (float) ((number(spec, raw) - spec.min()) / span);
            int trackWidth = WIDTH - 30;
            int filled = Math.round(trackWidth * Math.max(0, Math.min(1, amount)));
            SmoothShapeRenderer.roundedRect(g, X + 15, y + 53, trackWidth, 4, 2, 0xFF35293D);
            if (filled > 0) SmoothShapeRenderer.roundedRect(g, X + 15, y + 53, filled, 4, 2, ThemeConfig.accent);
            SmoothShapeRenderer.roundedRect(g, X + 10 + filled, y + 50, 10, 10, 5, 0xFFF8F4FA);
        }
    }

    private void shell(GuiGraphics g, int y, int height) {
        SmoothShapeRenderer.roundedRect(g, X, y, WIDTH, height, 11, 0xFF1B121E);
        SmoothShapeRenderer.roundedOutline(g, X, y, WIDTH, height, 11, 0xFF35293A);
    }

    private void smallButton(GuiGraphics g, int x, int y, String label) {
        SmoothShapeRenderer.roundedRect(g, x, y, 24, 23, 6, 0xFF302035);
        int width = SmoothTextRenderer.width(label, 12f, true);
        SmoothTextRenderer.draw(g, label, x + (24 - width) / 2f, y + 3, 12f, 0xFFF7F3F8, true);
    }

    boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;
        release();
        if (GuiLayout.inside(mx, my, X, 69, WIDTH, 54)) {
            module.toggle();
            ConfigManager.save();
            return true;
        }
        if (!GuiLayout.inside(mx, my, X, TOP, WIDTH, BOTTOM - TOP)) return false;
        int index = (int) ((my - TOP + scroll) / ROW);
        if (index < 0 || index >= specs.size()) return false;
        int y = TOP + index * ROW - scroll;
        if (!GuiLayout.inside(mx, my, X, y, WIDTH, 68)) return false;
        ModuleSettingsRegistry.SettingSpec spec = specs.get(index);
        if (spec.type() == ModuleSettingsRegistry.Type.BOOLEAN) {
            ConfigManager.adjust(module.name(), spec, 1);
        } else if (spec.type() == ModuleSettingsRegistry.Type.CHOICE) {
            if (GuiLayout.inside(mx, my, X + WIDTH - 60, y + 40, 24, 23)) {
                ConfigManager.adjust(module.name(), spec, -1);
            } else if (GuiLayout.inside(mx, my, X + 12, y + 40, WIDTH - 24, 23)) {
                ConfigManager.adjust(module.name(), spec, 1);
            }
        } else if (GuiLayout.inside(mx, my, X + WIDTH - 60, y + 5, 24, 23)) {
            ConfigManager.adjust(module.name(), spec, -1);
        } else if (GuiLayout.inside(mx, my, X + WIDTH - 32, y + 5, 24, 23)) {
            ConfigManager.adjust(module.name(), spec, 1);
        } else if (GuiLayout.inside(mx, my, X + 10, y + 45, WIDTH - 20, 19)) {
            dragging = index;
            setSlider(mx);
        }
        return true;
    }

    boolean mouseDragged(double mx, double my) {
        if (dragging < 0) return false;
        setSlider(mx);
        return true;
    }

    private void setSlider(double mx) {
        ModuleSettingsRegistry.SettingSpec spec = specs.get(dragging);
        String value = sliderValue(spec, (mx - X - 15) / (WIDTH - 30));
        if (!value.equals(ConfigManager.rawOption(module.name(), spec))) {
            ConfigManager.previewRawOption(module.name(), spec.key(), value);
            dirty = true;
        }
    }

    static String sliderValue(ModuleSettingsRegistry.SettingSpec spec, double fraction) {
        double value = spec.min() + Math.max(0, Math.min(1, fraction)) * (spec.max() - spec.min());
        if (spec.step() > 0) value = spec.min() + Math.round((value - spec.min()) / spec.step()) * spec.step();
        value = Math.max(spec.min(), Math.min(spec.max(), value));
        return spec.type() == ModuleSettingsRegistry.Type.INTEGER
                ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.3f", value);
    }

    private static double number(ModuleSettingsRegistry.SettingSpec spec, String raw) {
        try {
            double value = Double.parseDouble(raw);
            if (Double.isFinite(value)) return Math.max(spec.min(), Math.min(spec.max(), value));
        } catch (NumberFormatException ignored) { }
        return Double.parseDouble(spec.defaultValue());
    }

    boolean mouseScrolled(double amount) {
        release();
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) (amount * 30)));
        return true;
    }

    private int maxScroll() { return Math.max(0, specs.size() * ROW - 8 - (BOTTOM - TOP)); }

    void release() {
        dragging = -1;
        if (dirty) ConfigManager.save();
        dirty = false;
    }

    void close() { release(); }
}
