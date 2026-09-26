package de.sleepclient;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SmoothTextRenderer {
    private static final int OVERSAMPLE = 4;
    private static final int PAD = 2;
    private static final int MAX_CACHE = 384;
    private static final FontRenderContext METRICS = new FontRenderContext(null, true, true);
    private static final Font BASE_FONT = new Font(chooseFamily(), Font.PLAIN, 12);
    private static long nextId;

    // White masks are tinted at draw time: hover colors never allocate new textures.
    private static final Map<String, Entry> CACHE = new LinkedHashMap<>(128, .75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() <= MAX_CACHE) return false;
            Minecraft.getInstance().getTextureManager().release(eldest.getValue().id());
            return true;
        }
    };

    private static Font font(float size, boolean bold) {
        return BASE_FONT.deriveFont(bold ? Font.BOLD : Font.PLAIN, Math.max(6f, size));
    }

    /** x/y describe the top left of a font line, excluding transparent texture padding. */
    public static int draw(GuiGraphics g, String text, float x, float y, float size, int color, boolean bold) {
        if (text == null || text.isEmpty()) return 0;
        Entry e = get(text, size, bold);
        g.blit(RenderPipelines.GUI_TEXTURED, e.id(), Math.round(x) - PAD, Math.round(y) - PAD,
                0f, 0f, e.width(), e.height(), e.width() * OVERSAMPLE, e.height() * OVERSAMPLE,
                e.width() * OVERSAMPLE, e.height() * OVERSAMPLE, color);
        return width(text, size, bold);
    }

    public static int width(String text, float size, boolean bold) {
        if (text == null || text.isEmpty()) return 0;
        return (int) Math.ceil(new TextLayout(text, font(size, bold), METRICS).getAdvance());
    }

    public static String fit(String text, float size, boolean bold, int maxWidth) {
        if (text == null || maxWidth <= 0) return "";
        if (width(text, size, bold) <= maxWidth) return text;
        String result = text;
        while (!result.isEmpty() && width(result + "…", size, bold) > maxWidth) {
            result = result.substring(0, result.offsetByCodePoints(result.length(), -1));
        }
        return result.isEmpty() ? "" : result + "…";
    }

    private static synchronized Entry get(String text, float size, boolean bold) {
        int sizeKey = Math.round(Math.max(6f, size) * 10);
        String key = sizeKey + "|" + bold + "|" + text;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;

        Font logicalFont = font(sizeKey / 10f, bold);
        TextLayout layout = new TextLayout(text, logicalFont, METRICS);
        int logicalW = Math.max(1, (int) Math.ceil(layout.getAdvance())) + PAD * 2;
        int logicalH = (int) Math.ceil(layout.getAscent() + layout.getDescent() + layout.getLeading()) + PAD * 2;
        BufferedImage buffered = new BufferedImage(logicalW * OVERSAMPLE, logicalH * OVERSAMPLE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = buffered.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        gg.scale(OVERSAMPLE, OVERSAMPLE);
        gg.setColor(Color.WHITE);
        layout.draw(gg, PAD, PAD + layout.getAscent());
        gg.dispose();

        NativeImage image = new NativeImage(buffered.getWidth(), buffered.getHeight(), false);
        for (int py = 0; py < buffered.getHeight(); py++) {
            for (int px = 0; px < buffered.getWidth(); px++) {
                // Keep white RGB under transparent pixels to prevent dark filtering fringes.
                image.setPixel(px, py, (buffered.getRGB(px, py) & 0xFF000000) | 0x00FFFFFF);
            }
        }
        Identifier id = Identifier.fromNamespaceAndPath("sleepclient", "smooth_text/" + nextId++);
        SmoothTexture texture = new SmoothTexture(image);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        texture.upload();
        Entry entry = new Entry(id, logicalW, logicalH);
        CACHE.put(key, entry);
        return entry;
    }

    private static String chooseFamily() {
        String[] preferred = {"Segoe UI", "Inter", "Arial", "Noto Sans", Font.SANS_SERIF};
        try {
            String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String want : preferred) {
                for (String have : installed) if (have.equalsIgnoreCase(want)) return have;
            }
        } catch (RuntimeException ignored) {
            // Java's logical sans-serif font also works in a headless environment.
        }
        return Font.SANS_SERIF;
    }

    private record Entry(Identifier id, int width, int height) {}
    private SmoothTextRenderer() {}
}
