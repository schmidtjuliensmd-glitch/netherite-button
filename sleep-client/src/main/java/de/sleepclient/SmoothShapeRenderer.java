package de.sleepclient;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SmoothShapeRenderer {
    private static final int OVERSAMPLE = 4;
    private static final int MAX_CACHE = 192;
    private static long nextId;
    private static final Map<String, Entry> CACHE = new LinkedHashMap<>(96, .75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() <= MAX_CACHE) return false;
            Minecraft.getInstance().getTextureManager().release(eldest.getValue().id());
            return true;
        }
    };

    public static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        blit(g, getRounded(w, h, radius, false), x, y, color);
    }

    public static void roundedOutline(GuiGraphics g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        blit(g, getRounded(w, h, radius, true), x, y, color);
    }

    public static void roundedGradient(GuiGraphics g, int x, int y, int w, int h, int radius,
                                       int first, int second, boolean horizontal) {
        if (w <= 0 || h <= 0) return;
        blit(g, getGradient(w, h, radius, first, second, horizontal), x, y, 0xFFFFFFFF);
    }

    public static void glow(GuiGraphics g, int x, int y, int w, int h, int radius, int color, int strength) {
        if (w <= 0 || h <= 0 || strength <= 0) return;
        Entry e = getGlow(w, h, radius, strength);
        blit(g, e, x - e.pad(), y - e.pad(), color);
    }

    private static void blit(GuiGraphics g, Entry e, int x, int y, int color) {
        g.blit(RenderPipelines.GUI_TEXTURED, e.id(), x, y, 0f, 0f, e.width(), e.height(),
                e.width() * OVERSAMPLE, e.height() * OVERSAMPLE,
                e.width() * OVERSAMPLE, e.height() * OVERSAMPLE, color);
    }

    private static synchronized Entry getRounded(int w, int h, int radius, boolean outline) {
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        String key = "mask|" + w + "|" + h + "|" + r + "|" + outline;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;
        BufferedImage image = canvas(w, h);
        Graphics2D gg = graphics(image);
        gg.setColor(Color.WHITE);
        if (outline) {
            gg.setStroke(new BasicStroke(1f));
            gg.draw(new RoundRectangle2D.Float(.5f, .5f, w - 1f, h - 1f, r * 2f, r * 2f));
        } else {
            gg.fill(new RoundRectangle2D.Float(0, 0, w, h, r * 2f, r * 2f));
        }
        gg.dispose();
        Entry entry = upload(image, w, h, 0, true);
        CACHE.put(key, entry);
        return entry;
    }

    private static synchronized Entry getGradient(int w, int h, int radius, int first, int second, boolean horizontal) {
        String key = "gradient|" + w + "|" + h + "|" + radius + "|" + first + "|" + second + "|" + horizontal;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;
        BufferedImage image = canvas(w, h);
        Graphics2D gg = graphics(image);
        gg.setPaint(new GradientPaint(0, 0, new Color(first, true),
                horizontal ? w : 0, horizontal ? 0 : h, new Color(second, true)));
        gg.fill(new RoundRectangle2D.Float(0, 0, w, h, radius * 2f, radius * 2f));
        gg.dispose();
        Entry entry = upload(image, w, h, 0, false);
        CACHE.put(key, entry);
        return entry;
    }

    private static synchronized Entry getGlow(int w, int h, int radius, int strength) {
        int pad = Math.max(4, strength * 2);
        String key = "glow|" + w + "|" + h + "|" + radius + "|" + strength;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;
        int tw = w + pad * 2, th = h + pad * 2;
        BufferedImage image = canvas(tw, th);
        Graphics2D gg = graphics(image);
        for (int i = strength; i >= 1; i--) {
            float t = i / (float) strength;
            int spread = Math.max(1, Math.round(i * 1.7f));
            int alpha = Math.max(1, Math.round(255 * (.025f + .065f * (1f - t))));
            gg.setColor(new Color(255, 255, 255, alpha));
            gg.fill(new RoundRectangle2D.Float(pad - spread, pad - spread, w + spread * 2, h + spread * 2,
                    (radius + spread) * 2f, (radius + spread) * 2f));
        }
        gg.dispose();
        Entry entry = upload(image, tw, th, pad, true);
        CACHE.put(key, entry);
        return entry;
    }

    private static BufferedImage canvas(int width, int height) {
        return new BufferedImage(width * OVERSAMPLE, height * OVERSAMPLE, BufferedImage.TYPE_INT_ARGB);
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D gg = image.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gg.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        gg.scale(OVERSAMPLE, OVERSAMPLE);
        return gg;
    }

    private static Entry upload(BufferedImage buffered, int w, int h, int pad, boolean mask) {
        NativeImage image = new NativeImage(buffered.getWidth(), buffered.getHeight(), false);
        for (int py = 0; py < buffered.getHeight(); py++) {
            for (int px = 0; px < buffered.getWidth(); px++) {
                int argb = buffered.getRGB(px, py);
                image.setPixel(px, py, mask ? (argb & 0xFF000000) | 0x00FFFFFF : argb);
            }
        }
        Identifier id = Identifier.fromNamespaceAndPath("sleepclient", "smooth_shape/" + nextId++);
        SmoothTexture texture = new SmoothTexture(image);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        texture.upload();
        return new Entry(id, w, h, pad);
    }

    private record Entry(Identifier id, int width, int height, int pad) {}
    private SmoothShapeRenderer() {}
}
