package de.sleepclient;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** High-resolution, linearly sampled icon masks, tinted with the current GUI theme. */
public final class GuiIconRenderer {
    private static final int PIXELS = 96;
    private static final Map<GuiIconArtwork.Icon, Identifier> CACHE = new EnumMap<>(GuiIconArtwork.Icon.class);

    public static void draw(GuiGraphics g, GuiIconArtwork.Icon icon, int x, int y, int size, int color) {
        if (size <= 0) return;
        Identifier texture = CACHE.computeIfAbsent(icon, GuiIconRenderer::upload);
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, size, size,
                PIXELS, PIXELS, PIXELS, PIXELS, color);
    }

    private static Identifier upload(GuiIconArtwork.Icon icon) {
        BufferedImage artwork = GuiIconArtwork.render(icon, PIXELS);
        NativeImage image = new NativeImage(PIXELS, PIXELS, false);
        for (int y = 0; y < PIXELS; y++) {
            for (int x = 0; x < PIXELS; x++) {
                image.setPixel(x, y, (artwork.getRGB(x, y) & 0xFF000000) | 0x00FFFFFF);
            }
        }
        Identifier id = Identifier.fromNamespaceAndPath("sleepclient", "gui_icons/" + icon.name().toLowerCase(Locale.ROOT));
        SmoothTexture texture = new SmoothTexture(image);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        texture.upload();
        return id;
    }

    private GuiIconRenderer() {}
}
