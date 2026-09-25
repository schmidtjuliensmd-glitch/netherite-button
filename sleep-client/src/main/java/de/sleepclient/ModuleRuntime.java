package de.sleepclient;

import net.minecraft.client.Minecraft;

public final class ModuleRuntime {
    private static Double originalGamma;

    public static void tick(Minecraft client) {
        if (client.player == null) {
            restoreGamma(client);
            return;
        }

        Module fullbright = ModuleRegistry.find("Fullbright");
        if (fullbright != null && fullbright.enabled()) {
            if (originalGamma == null) originalGamma = client.options.gamma().get();
            if (client.options.gamma().get() < 1.0D) client.options.gamma().set(1.0D);
        } else {
            restoreGamma(client);
        }

        Module sprint = ModuleRegistry.find("Sprint");
        if (sprint != null && sprint.enabled() && client.options.keyUp.isDown() && !client.options.keyShift.isDown()) {
            client.player.setSprinting(true);
        }
    }

    private static void restoreGamma(Minecraft client) {
        if (originalGamma != null) {
            client.options.gamma().set(originalGamma);
            originalGamma = null;
        }
    }

    private ModuleRuntime() {}
}
