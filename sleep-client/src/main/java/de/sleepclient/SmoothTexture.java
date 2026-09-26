package de.sleepclient;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.renderer.texture.DynamicTexture;

/** Linear sampling prevents pixel blocks when Minecraft scales the interface. */
final class SmoothTexture extends DynamicTexture {
    SmoothTexture(NativeImage image) {
        super(() -> "Sleep Client antialiased GUI", image);
    }

    @Override
    public GpuSampler getSampler() {
        return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    }
}
