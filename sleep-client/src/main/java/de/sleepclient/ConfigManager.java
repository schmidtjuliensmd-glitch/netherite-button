package de.sleepclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sleep-client.properties");

    public static int playerEspRange = 96;
    public static float playerEspLineWidth = 2.2f;
    public static boolean playerEspDistanceHud = true;

    public static int susScanRadius = 7;
    public static int susMinScore = 8;
    public static boolean susHudEnabled = true;

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

        playerEspRange = clamp(parseInt(p.getProperty("playerEsp.range"), playerEspRange), 16, 256);
        playerEspLineWidth = clamp(parseFloat(p.getProperty("playerEsp.lineWidth"), playerEspLineWidth), 1.0f, 5.0f);
        playerEspDistanceHud = parseBool(p.getProperty("playerEsp.distanceHud"), playerEspDistanceHud);

        susScanRadius = clamp(parseInt(p.getProperty("susChunk.radius"), susScanRadius), 2, 12);
        susMinScore = clamp(parseInt(p.getProperty("susChunk.minScore"), susMinScore), 1, 40);
        susHudEnabled = parseBool(p.getProperty("susChunk.hud"), susHudEnabled);

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

        p.setProperty("playerEsp.range", Integer.toString(playerEspRange));
        p.setProperty("playerEsp.lineWidth", Float.toString(playerEspLineWidth));
        p.setProperty("playerEsp.distanceHud", Boolean.toString(playerEspDistanceHud));

        p.setProperty("susChunk.radius", Integer.toString(susScanRadius));
        p.setProperty("susChunk.minScore", Integer.toString(susMinScore));
        p.setProperty("susChunk.hud", Boolean.toString(susHudEnabled));

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
