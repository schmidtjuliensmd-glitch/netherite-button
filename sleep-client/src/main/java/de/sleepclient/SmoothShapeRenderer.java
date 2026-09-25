package de.sleepclient;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SmoothShapeRenderer {
    private static final int MAX_CACHE = 420;

    private static final Map<String, Entry> CACHE = new LinkedHashMap<>(160, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() <= MAX_CACHE) return false;
            Minecraft.getInstance().getTextureManager().release(eldest.getValue().id());
            return true;
        }
    };

    public static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        Entry e = getRounded(w, h, radius, color);
        blit(g, e, x, y);
    }

    public static void glow(GuiGraphics g, int x, int y, int w, int h, int radius, int color, int strength) {
        if (w <= 0 || h <= 0 || strength <= 0) return;
        Entry e = getGlow(w, h, radius, color, strength);
        blit(g, e, x - e.pad(), y - e.pad());
    }

    private static void blit(GuiGraphics g, Entry e, int x, int y) {
        g.blit(
                RenderPipelines.GUI_TEXTURED,
                e.id(),
                x,
                y,
                0.0f,
                0.0f,
                e.width(),
                e.height(),
                e.width(),
                e.height(),
                e.width(),
                e.height()
        );
    }

    private static synchronized Entry getRounded(int w, int h, int radius, int color) {
        int r = Math.max(1, Math.min(radius, Math.min(w, h) / 2));
        String key = "rect|" + w + "|" + h + "|" + r + "|" + color;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = image.createGraphics();
        quality(gg);
        gg.setColor(new Color(color, true));
        gg.fillRoundRect(0, 0, w, h, r * 2, r * 2);
        gg.dispose();

        Entry entry = upload(key, image, 0);
        CACHE.put(key, entry);
        return entry;
    }

    private static synchronized Entry getGlow(int w, int h, int radius, int color, int strength) {
        int pad = Math.max(4, strength * 2);
        String key = "glow|" + w + "|" + h + "|" + radius + "|" + color + "|" + strength;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;

        int tw = w + pad * 2;
        int th = h + pad * 2;
        BufferedImage image = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = image.createGraphics();
        quality(gg);

        int baseAlpha = (color >>> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        for (int i = strength; i >= 1; i--) {
            float t = i / (float)strength;
            int spread = Math.max(1, Math.round(i * 1.7f));
            int alpha = Math.max(1, Math.round(baseAlpha * (0.055f + 0.12f * (1.0f - t))));
            gg.setColor(new Color((alpha << 24) | rgb, true));
            gg.fillRoundRect(
                    pad - spread,
                    pad - spread,
                    w + spread * 2,
                    h + spread * 2,
                    (radius + spread) * 2,
                    (radius + spread) * 2
            );
        }
        gg.dispose();

        Entry entry = upload(key, image, pad);
        CACHE.put(key, entry);
        return entry;
    }

    private static Entry upload(String key, BufferedImage buffered, int pad) {
        int w = buffered.getWidth();
        int h = buffered.getHeight();
        NativeImage image = new NativeImage(w, h, false);

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                image.setPixel(px, py, buffered.getRGB(px, py));
            }
        }

        Identifier id = Identifier.fromNamespaceAndPath(
                "sleepclient",
                "smooth_shape/" + Integer.toUnsignedString(key.hashCode(), 16)
        );
        DynamicTexture texture = new DynamicTexture(() -> "Sleep Client smooth shape", image);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        texture.upload();
        return new Entry(id, w, h, pad);
    }

    private static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    private record Entry(Identifier id, int width, int height, int pad) {}

    private SmoothShapeRenderer() {}
}
