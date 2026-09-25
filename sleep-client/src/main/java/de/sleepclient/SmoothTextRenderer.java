package de.sleepclient;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SmoothTextRenderer {
    private static final int OVERSAMPLE = 1;
    private static final int MAX_CACHE = 320;

    private static final String FAMILY = chooseFamily();
    private static final Map<String, Entry> CACHE = new LinkedHashMap<>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() <= MAX_CACHE) return false;
            Minecraft.getInstance().getTextureManager().release(eldest.getValue().id());
            return true;
        }
    };

    public static int draw(GuiGraphics g, String text, float x, float y, float size, int color, boolean bold) {
        if (text == null || text.isEmpty()) return 0;

        Entry entry = get(text, size, color, bold);
        g.blit(
                RenderPipelines.GUI_TEXTURED,
                entry.id(),
                Math.round(x),
                Math.round(y),
                0.0f,
                0.0f,
                entry.logicalWidth(),
                entry.logicalHeight(),
                entry.pixelWidth(),
                entry.pixelHeight(),
                entry.pixelWidth(),
                entry.pixelHeight()
        );
        return entry.logicalWidth();
    }

    public static int width(String text, float size, boolean bold) {
        return get(text, size, 0xFFFFFFFF, bold).logicalWidth();
    }

    private static synchronized Entry get(String text, float size, int color, boolean bold) {
        int sizeKey = Math.max(7, Math.round(size * 10.0f));
        String key = text + "|" + sizeKey + "|" + color + "|" + bold;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;

        float logicalSize = sizeKey / 10.0f;
        int fontSize = Math.max(8, Math.round(logicalSize * OVERSAMPLE));
        Font awtFont = new Font(FAMILY, bold ? Font.BOLD : Font.PLAIN, fontSize);

        BufferedImage probe = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        applyQuality(pg);
        pg.setFont(awtFont);
        FontMetrics fm = pg.getFontMetrics();
        int pad = 3 * OVERSAMPLE;
        int pixelW = Math.max(2, fm.stringWidth(text) + pad * 2);
        int pixelH = Math.max(2, fm.getAscent() + fm.getDescent() + pad * 2);
        pg.dispose();

        BufferedImage buffered = new BufferedImage(pixelW, pixelH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = buffered.createGraphics();
        applyQuality(gg);
        gg.setFont(awtFont);
        gg.setColor(new Color(color, true));
        gg.drawString(text, pad, pad + fm.getAscent());
        gg.dispose();

        NativeImage image = new NativeImage(pixelW, pixelH, false);
        for (int py = 0; py < pixelH; py++) {
            for (int px = 0; px < pixelW; px++) {
                image.setPixel(px, py, buffered.getRGB(px, py));
            }
        }

        String hash = Integer.toUnsignedString(key.hashCode(), 16);
        Identifier id = Identifier.fromNamespaceAndPath("sleepclient", "smooth_text/" + hash);
        DynamicTexture texture = new DynamicTexture(() -> "Sleep Client smooth text", image);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        texture.upload();

        Entry entry = new Entry(
                id,
                pixelW,
                pixelH,
                Math.max(1, Math.round(pixelW / (float) OVERSAMPLE)),
                Math.max(1, Math.round(pixelH / (float) OVERSAMPLE))
        );
        CACHE.put(key, entry);
        return entry;
    }

    private static void applyQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static String chooseFamily() {
        String[] preferred = {
                "Segoe UI Variable Text",
                "Segoe UI",
                "Inter",
                "Arial",
                "Noto Sans",
                Font.SANS_SERIF
        };
        try {
            String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String want : preferred) {
                for (String have : installed) {
                    if (have.equalsIgnoreCase(want)) return have;
                }
            }
        } catch (Throwable ignored) {
        }
        return Font.SANS_SERIF;
    }

    private record Entry(
            Identifier id,
            int pixelWidth,
            int pixelHeight,
            int logicalWidth,
            int logicalHeight
    ) {}

    private SmoothTextRenderer() {}
}
