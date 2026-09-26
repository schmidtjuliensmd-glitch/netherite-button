package de.sleepclient;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PaymentTracker {
    private static final String ENDPOINT = "https://julienmcoption.vercel.app/api/payments/report";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sleep-payment-tracker.properties");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private static final Pattern PAID_YOU = Pattern.compile(
            "(?i)(?:^|\\s)(\\.?[A-Za-z0-9_]{3,16})\\s+(?:paid\\s+you|sent\\s+you)\\s+\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?\\s*[KMBT]?)\\b"
    );
    private static final Pattern RECEIVED_FROM = Pattern.compile(
            "(?i)you\\s+received\\s+\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?\\s*[KMBT]?)\\s+from\\s+(\\.?[A-Za-z0-9_]{3,16})\\b"
    );

    private static volatile String secret = "";
    private static volatile String lastFingerprint = "";
    private static volatile long lastFingerprintAt;

    private PaymentTracker() {}

    public static void init() {
        loadSecret();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> onServerMessage(message));
    }

    public static boolean configured() {
        return secret != null && !secret.isBlank();
    }

    public static boolean setSecret(String value) {
        secret = value == null ? "" : value.trim();
        Properties properties = new Properties();
        properties.setProperty("adminSecret", secret);
        properties.setProperty("endpoint", ENDPOINT);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                properties.store(writer, "Sleep Client owner payment tracker");
            }
            return true;
        } catch (IOException error) {
            return false;
        }
    }

    private static void loadSecret() {
        if (!Files.exists(CONFIG_PATH)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(reader);
            secret = properties.getProperty("adminSecret", "").trim();
        } catch (IOException ignored) {
            secret = "";
        }
    }

    private static void onServerMessage(Component component) {
        if (!configured() || component == null) return;

        String message = component.getString();
        if (message == null || message.isBlank()) return;

        Payment payment = parse(message);
        if (payment == null || payment.amount() <= 0) return;

        long now = System.currentTimeMillis();
        String fingerprint = payment.payer().toLowerCase(Locale.ROOT) + "|" + payment.amount() + "|" + message;
        if (fingerprint.equals(lastFingerprint) && now - lastFingerprintAt < 2500L) return;
        lastFingerprint = fingerprint;
        lastFingerprintAt = now;

        String receiver = Minecraft.getInstance().getUser().getName();
        report(new PaymentEvent(
                UUID.randomUUID().toString(),
                payment.payer(),
                payment.amount(),
                payment.rawAmount(),
                receiver,
                message,
                now
        ));
    }

    private static Payment parse(String message) {
        Matcher direct = PAID_YOU.matcher(message);
        if (direct.find()) {
            long amount = parseAmount(direct.group(2));
            return amount > 0 ? new Payment(direct.group(1), amount, direct.group(2).trim()) : null;
        }

        Matcher received = RECEIVED_FROM.matcher(message);
        if (received.find()) {
            long amount = parseAmount(received.group(1));
            return amount > 0 ? new Payment(received.group(2), amount, received.group(1).trim()) : null;
        }

        return null;
    }

    private static long parseAmount(String raw) {
        if (raw == null) return 0L;
        String normalized = raw.replace(",", "").replace(" ", "").trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) return 0L;

        BigDecimal multiplier = BigDecimal.ONE;
        char suffix = normalized.charAt(normalized.length() - 1);
        if (Character.isLetter(suffix)) {
            normalized = normalized.substring(0, normalized.length() - 1);
            multiplier = switch (suffix) {
                case 'K' -> BigDecimal.valueOf(1_000L);
                case 'M' -> BigDecimal.valueOf(1_000_000L);
                case 'B' -> BigDecimal.valueOf(1_000_000_000L);
                case 'T' -> BigDecimal.valueOf(1_000_000_000_000L);
                default -> BigDecimal.ZERO;
            };
        }

        try {
            return new BigDecimal(normalized)
                    .multiply(multiplier)
                    .setScale(0, RoundingMode.DOWN)
                    .longValueExact();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static void report(PaymentEvent event) {
        String body = "{"
                + "\"eventId\":\"" + json(event.eventId()) + "\","
                + "\"payer\":\"" + json(event.payer()) + "\","
                + "\"amount\":" + event.amount() + ","
                + "\"amountRaw\":\"" + json(event.amountRaw()) + "\","
                + "\"receiver\":\"" + json(event.receiver()) + "\","
                + "\"message\":\"" + json(event.message()) + "\","
                + "\"receivedAt\":" + event.receivedAt()
                + "}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + secret)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(error -> null);
    }

    private static String json(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private record Payment(String payer, long amount, String rawAmount) {}
    private record PaymentEvent(
            String eventId,
            String payer,
            long amount,
            String amountRaw,
            String receiver,
            String message,
            long receivedAt
    ) {}
}
