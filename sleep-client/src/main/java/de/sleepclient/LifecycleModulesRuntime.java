package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class LifecycleModulesRuntime {
    private static ServerData lastServer;
    private static boolean wasConnected;
    private static int reconnectTicks;
    private static int respawnTicks;
    private static int reconnectAttempts;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified()) return;

        ServerData current = client.getCurrentServer();
        if (client.level != null && current != null) {
            lastServer = current;
            wasConnected = true;
            reconnectTicks = 0;
            reconnectAttempts = 0;
        }

        tickRespawn(client);
        tickReconnect(client);
    }

    private static void tickRespawn(Minecraft client) {
        Module module = ModuleRegistry.find("Auto Respawn");
        if (module == null || !module.enabled() || client.player == null) {
            respawnTicks = 0;
            return;
        }

        if (client.player.isAlive()) {
            respawnTicks = 0;
            return;
        }

        int delay = ConfigManager.intOption("Auto Respawn","delay",10);
        if (++respawnTicks < Math.max(0, delay)) return;

        respawnTicks = 0;
        client.player.respawn();
    }

    private static void tickReconnect(Minecraft client) {
        Module module = ModuleRegistry.find("Auto Reconnect");
        if (module == null || !module.enabled()) {
            reconnectTicks = 0;
            reconnectAttempts = 0;
            return;
        }

        if (client.getConnection() != null) {
            wasConnected = true;
            reconnectTicks = 0;
            return;
        }

        if (!wasConnected || lastServer == null || !(client.screen instanceof DisconnectedScreen)) return;

        int delaySeconds = ConfigManager.intOption("Auto Reconnect","delay",5);
        int maxAttempts = ConfigManager.intOption("Auto Reconnect","maxAttempts",5);

        if (++reconnectTicks < Math.max(20, delaySeconds * 20)) return;
        reconnectTicks = 0;

        if (reconnectAttempts >= maxAttempts) {
            wasConnected = false;
            return;
        }

        reconnectAttempts++;
        String ip = lastServer.ip;
        if (ip == null || ip.isBlank()) {
            wasConnected = false;
            return;
        }

        var parent = client.screen != null ? client.screen : new JoinMultiplayerScreen(new TitleScreen());
        net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
                parent,
                client,
                ServerAddress.parseString(ip),
                lastServer,
                false,
                null
        );
    }

    private LifecycleModulesRuntime() {}
}
