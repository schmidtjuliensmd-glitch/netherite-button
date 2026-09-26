package de.sleepclient.mixin;

import de.sleepclient.ConfigManager;
import de.sleepclient.Module;
import de.sleepclient.ModuleRegistry;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMovementMixin {
    @Inject(method = "isSlowDueToUsingItem", at = @At("HEAD"), cancellable = true)
    private void sleepclient$noSlow(CallbackInfoReturnable<Boolean> cir) {
        Module module = ModuleRegistry.find("No Slow");
        if (module != null && module.enabled()
                && ConfigManager.floatOption("No Slow","itemSlow",1.0f) >= 0.99f) {
            cir.setReturnValue(false);
        }
    }
}
