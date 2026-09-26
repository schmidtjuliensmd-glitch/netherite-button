package de.sleepclient;

import net.minecraft.client.Minecraft;

public final class EnvironmentModulesRuntime {
    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null) return;

        if (enabled("No Weather")) {
            client.level.setRainLevel(0.0f);
            client.level.setThunderLevel(0.0f);
        }

        if (enabled("Time Changer")) {
            String time = ConfigManager.stringOption("Time Changer","time","Day");
            long value = switch (time) {
                case "Sunset" -> 12000L;
                case "Night" -> 14000L;
                case "Midnight" -> 18000L;
                default -> 1000L;
            };
            client.level.setTimeFromServer(client.level.getGameTime(), value, false);
        }

        if (enabled("No Hurt Cam")) {
            client.player.hurtTime = 0;
            client.player.hurtDuration = 0;
        }
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private EnvironmentModulesRuntime() {}
}
