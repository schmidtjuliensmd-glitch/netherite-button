package de.sleepclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ConfigManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sleep-client.properties");
    private static final Map<String, String> SETTINGS = new HashMap<>();

    public static void load() {
        Properties p = new Properties();

        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }

        ThemeConfig.accent = parseHex(p.getProperty("theme.accent"), ThemeConfig.accent);
        ThemeConfig.accentSecondary = parseHex(p.getProperty("theme.accentSecondary"), ThemeConfig.accentSecondary);
        ThemeConfig.animationSpeed = clamp(parseFloat(p.getProperty("theme.animationSpeed"), ThemeConfig.animationSpeed), 0.35f, 2.0f);
        ThemeConfig.panelOpacity = clamp(parseFloat(p.getProperty("theme.panelOpacity"), ThemeConfig.panelOpacity), 0.55f, 1.0f);
        ThemeConfig.guiScale = clamp(parseFloat(p.getProperty("theme.guiScale"), ThemeConfig.guiScale), 0.75f, 1.5f);

        SETTINGS.clear();
        for (String key : p.stringPropertyNames()) {
            if (key.startsWith("setting.")) SETTINGS.put(key, p.getProperty(key));
        }

        // Import older 0.1.6 keys once so existing user settings remain useful.
        importLegacy(p, "playerEsp.range", "PlayerESP", "range");
        importLegacy(p, "playerEsp.lineWidth", "PlayerESP", "lineWidth");
        importLegacy(p, "playerEsp.distanceHud", "PlayerESP", "distanceHud");
        importLegacy(p, "susChunk.radius", "Sus Chunk Finder", "radius");
        importLegacy(p, "susChunk.minScore", "Sus Chunk Finder", "minScore");
        importLegacy(p, "susChunk.hud", "Sus Chunk Finder", "hud");

        for (Module module : ModuleRegistry.all()) {
            String key = "module." + normalize(module.name());
            if (p.containsKey(key)) module.setEnabled(parseBool(p.getProperty(key), module.enabled()));
        }
    }

    public static synchronized void save() {
        Properties p = new Properties();

        p.setProperty("theme.accent", toHex(ThemeConfig.accent));
        p.setProperty("theme.accentSecondary", toHex(ThemeConfig.accentSecondary));
        p.setProperty("theme.animationSpeed", Float.toString(ThemeConfig.animationSpeed));
        p.setProperty("theme.panelOpacity", Float.toString(ThemeConfig.panelOpacity));
        p.setProperty("theme.guiScale", Float.toString(ThemeConfig.guiScale));

        for (Map.Entry<String, String> entry : SETTINGS.entrySet()) {
            p.setProperty(entry.getKey(), entry.getValue());
        }

        for (Module module : ModuleRegistry.all()) {
            p.setProperty("module." + normalize(module.name()), Boolean.toString(module.enabled()));
        }

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Sleep Client configuration");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean boolOption(String module, String key, boolean fallback) {
        return parseBool(SETTINGS.get(settingKey(module, key)), fallback);
    }

    public static int intOption(String module, String key, int fallback) {
        return parseInt(SETTINGS.get(settingKey(module, key)), fallback);
    }

    public static float floatOption(String module, String key, float fallback) {
        return parseFloat(SETTINGS.get(settingKey(module, key)), fallback);
    }

    public static String stringOption(String module, String key, String fallback) {
        return SETTINGS.getOrDefault(settingKey(module, key), fallback);
    }

    public static String rawOption(String module, ModuleSettingsRegistry.SettingSpec spec) {
        return SETTINGS.getOrDefault(settingKey(module, spec.key()), spec.defaultValue());
    }

    public static void setRawOption(String module, String key, String value) {
        previewRawOption(module, key, value);
        save();
    }

    /** Apply a dragged value immediately; the settings panel saves once on release. */
    public static void previewRawOption(String module, String key, String value) {
        SETTINGS.put(settingKey(module, key), value);
        applySpecial(module, key, value);
    }

    public static void adjust(String module, ModuleSettingsRegistry.SettingSpec spec, int direction) {
        String current = rawOption(module, spec);

        switch (spec.type()) {
            case BOOLEAN -> setRawOption(module, spec.key(), Boolean.toString(!Boolean.parseBoolean(current)));
            case INTEGER -> {
                int value = parseInt(current, (int)Double.parseDouble(spec.defaultValue()));
                int next = clamp(value + (int)Math.round(spec.step()) * direction, (int)spec.min(), (int)spec.max());
                setRawOption(module, spec.key(), Integer.toString(next));
            }
            case FLOAT -> {
                double fallback = Double.parseDouble(spec.defaultValue());
                double value;
                try { value = Double.parseDouble(current); } catch (Exception ignored) { value = fallback; }
                double next = Math.max(spec.min(), Math.min(spec.max(), value + spec.step() * direction));
                setRawOption(module, spec.key(), String.format(java.util.Locale.ROOT, "%.3f", next));
            }
            case CHOICE -> {
                if (spec.choices().isEmpty()) return;
                int index = spec.choices().indexOf(current);
                if (index < 0) index = 0;
                index = Math.floorMod(index + direction, spec.choices().size());
                setRawOption(module, spec.key(), spec.choices().get(index));
            }
        }
    }

    public static void applyThemePreset(String preset) {
        switch (preset.toLowerCase()) {
            case "purple" -> {
                ThemeConfig.accent = 0xFFB46CFF;
                ThemeConfig.accentSecondary = 0xFF6E5CFF;
            }
            case "blue" -> {
                ThemeConfig.accent = 0xFF55B8FF;
                ThemeConfig.accentSecondary = 0xFF7A68FF;
            }
            case "green" -> {
                ThemeConfig.accent = 0xFF55E6B1;
                ThemeConfig.accentSecondary = 0xFF4FB7FF;
            }
            default -> {
                ThemeConfig.accent = 0xFFFF4F9F;
                ThemeConfig.accentSecondary = 0xFF8B5CF6;
            }
        }
        save();
    }

    private static void applySpecial(String module, String key, String value) {
        if ("Animation Settings".equals(module) && "speed".equals(key)) {
            ThemeConfig.animationSpeed = clamp(parseFloat(value, ThemeConfig.animationSpeed), 0.35f, 2.0f);
        }
        if ("GUI Scale".equals(module) && "scale".equals(key)) {
            ThemeConfig.guiScale = clamp(parseFloat(value, ThemeConfig.guiScale), 0.75f, 1.5f);
        }
        if ("Theme Editor".equals(module) && "preset".equals(key)) {
            applyThemePreset(value);
        }
    }

    private static void importLegacy(Properties p, String oldKey, String module, String key) {
        String newKey = settingKey(module, key);
        if (!SETTINGS.containsKey(newKey) && p.containsKey(oldKey)) {
            SETTINGS.put(newKey, p.getProperty(oldKey));
        }
    }

    private static String settingKey(String module, String key) {
        return "setting." + normalize(module) + "." + normalize(key);
    }

    private static String normalize(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private static int parseHex(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return (int)Long.parseLong(value.replace("#", ""), 16);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String toHex(int value) {
        return String.format("%08X", value);
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (Exception ignored) { return fallback; }
    }

    private static float parseFloat(String value, float fallback) {
        try { return value == null ? fallback : Float.parseFloat(value); }
        catch (Exception ignored) { return fallback; }
    }

    private static boolean parseBool(String value, boolean fallback) {
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private ConfigManager() {}
}
