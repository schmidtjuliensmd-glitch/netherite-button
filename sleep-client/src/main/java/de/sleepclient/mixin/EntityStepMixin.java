package de.sleepclient.mixin;

import de.sleepclient.ConfigManager;
import de.sleepclient.Module;
import de.sleepclient.ModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityStepMixin {
    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void sleepclient$stepHeight(CallbackInfoReturnable<Float> cir) {
        Minecraft client = Minecraft.getInstance();
        if ((Object)this != client.player) return;

        Module module = ModuleRegistry.find("Step");
        if (module == null || !module.enabled()) return;

        float height = ConfigManager.floatOption("Step","height",1.0f);
        cir.setReturnValue(Math.max(0.5f, Math.min(2.5f, height)));
    }
}
