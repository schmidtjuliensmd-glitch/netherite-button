package de.sleepclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public final class UpdateManager {
    private static final String VERSION_URL = "https://julienmcoption.vercel.app/downloads/version.json";
    private static final Gson GSON = new Gson();

    private static volatile String latestVersion = currentVersion();
    private static volatile boolean updateAvailable;
    private static volatile boolean checked;
    private static volatile boolean announced;

    public static CompletableFuture<Void> checkForUpdates() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VERSION_URL))
                .GET()
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) return;
                    try {
                        JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                        if (json != null && json.has("latestVersion")) {
                            latestVersion = json.get("latestVersion").getAsString();
                            updateAvailable = compareVersions(latestVersion, currentVersion()) > 0;
                            checked = true;
                        }
                    } catch (Exception ignored) {
                    }
                })
                .exceptionally(error -> null);
    }

    public static void tick(Minecraft client) {
        if (!checked || !updateAvailable || announced || !LicenseManager.verified() || client.player == null) return;
        announced = true;
        client.player.displayClientMessage(Component.literal(
                "Sleep Client: New version " + latestVersion + " available. Use /sleepupdate to get it."
        ), false);
    }

    public static void openLatestDownload() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create("https://julienmcoption.vercel.app/clients.html"));
            }
        } catch (Exception ignored) {
        }
    }

    public static String currentVersion() {
        return FabricLoader.getInstance()
                .getModContainer("sleepclient")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");
    }

    public static String latestVersion() { return latestVersion; }
    public static boolean updateAvailable() { return updateAvailable; }

    private static int compareVersions(String a, String b) {
        String[] aa = a.replaceAll("[^0-9.]", "").split("\\.");
        String[] bb = b.replaceAll("[^0-9.]", "").split("\\.");
        int max = Math.max(aa.length, bb.length);
        for (int i = 0; i < max; i++) {
            int av = i < aa.length && !aa[i].isBlank() ? Integer.parseInt(aa[i]) : 0;
            int bv = i < bb.length && !bb[i].isBlank() ? Integer.parseInt(bb[i]) : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private UpdateManager() {}
}
