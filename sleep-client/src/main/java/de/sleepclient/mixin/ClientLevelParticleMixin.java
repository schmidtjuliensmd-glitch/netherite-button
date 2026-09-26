package de.sleepclient.mixin;

import de.sleepclient.Module;
import de.sleepclient.ModuleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {
    @Inject(
            method = "doAddParticle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sleepclient$hideExplosionParticles(
            ParticleOptions particle,
            boolean overrideLimiter,
            boolean alwaysShow,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            CallbackInfo ci
    ) {
        Module module = ModuleRegistry.find("No Explosions");
        if (module == null || !module.enabled()) return;

        var type = particle.getType();
        if (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER) {
            ci.cancel();
        }
    }
}
