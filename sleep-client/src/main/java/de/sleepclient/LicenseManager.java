package de.sleepclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public final class LicenseManager {
    private static final String VERIFY_URL = "https://julienmcoption.vercel.app/api/license/verify";
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sleep-client-license.properties");
    private static final Gson GSON = new Gson();

    private static volatile boolean verified;
    private static volatile String plan = "none";
    private static volatile Long expiresAt;
    private static volatile String username = "";
    private static volatile String message = "Not signed in";

    public static CompletableFuture<Boolean> saveAndVerify(String mcName, String productKey) {
        save(mcName, productKey);
        return verify(mcName, productKey);
    }

    public static CompletableFuture<Boolean> verifySaved() {
        Properties props = load();
        String mcName = props.getProperty("username", "");
        String key = props.getProperty("key", "");
        if (mcName.isBlank() || key.isBlank()) {
            verified = false;
            message = "Not signed in";
            return CompletableFuture.completedFuture(false);
        }
        return verify(mcName, key);
    }

    private static CompletableFuture<Boolean> verify(String mcName, String productKey) {
        username = mcName;
        message = "Checking license...";

        JsonObject payload = new JsonObject();
        payload.addProperty("username", mcName);
        payload.addProperty("key", productKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VERIFY_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload), StandardCharsets.UTF_8))
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                        if (response.statusCode() == 200 && json != null && json.has("ok") && json.get("ok").getAsBoolean()) {
                            verified = true;
                            plan = json.has("plan") ? json.get("plan").getAsString() : "unknown";
                            expiresAt = json.has("expiresAt") && !json.get("expiresAt").isJsonNull()
                                    ? json.get("expiresAt").getAsLong() : null;
                            message = "Signed in";
                            return true;
                        }
                        verified = false;
                        message = json != null && json.has("error") ? json.get("error").getAsString() : "verification_failed";
                        return false;
                    } catch (Exception e) {
                        verified = false;
                        message = "invalid_server_response";
                        return false;
                    }
                })
                .exceptionally(error -> {
                    verified = false;
                    message = "license_server_unreachable";
                    return false;
                });
    }

    private static void save(String mcName, String productKey) {
        Properties props = new Properties();
        props.setProperty("username", mcName);
        props.setProperty("key", productKey);
        try {
            Files.createDirectories(FILE.getParent());
            try (var out = Files.newOutputStream(FILE)) {
                props.store(out, "Sleep Client license");
            }
        } catch (IOException ignored) {
        }
    }

    private static Properties load() {
        Properties props = new Properties();
        if (!Files.exists(FILE)) return props;
        try (var in = Files.newInputStream(FILE)) {
            props.load(in);
        } catch (IOException ignored) {
        }
        return props;
    }

    public static boolean verified() { return verified; }
    public static String plan() { return plan; }
    public static Long expiresAt() { return expiresAt; }
    public static String username() { return username; }
    public static String message() { return message; }

    private LicenseManager() {}
}
