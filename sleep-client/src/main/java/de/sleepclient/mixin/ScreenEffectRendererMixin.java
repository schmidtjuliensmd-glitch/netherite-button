package de.sleepclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.sleepclient.Module;
import de.sleepclient.ModuleRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void sleepclient$noFlame(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            TextureAtlasSprite fireSprite,
            CallbackInfo ci
    ) {
        Module module = ModuleRegistry.find("No Flame");
        if (module != null && module.enabled()) ci.cancel();
    }
}
