package de.julien.netheritebutton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NetheriteButtonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("netherite-button.json");

    public boolean enabled = true;
    public String replacementText = "Custom";
    public String color = "GRAY";

    public static NetheriteButtonConfig load() {
        if (Files.exists(FILE)) {
            try {
                NetheriteButtonConfig config = GSON.fromJson(Files.readString(FILE), NetheriteButtonConfig.class);
                if (config != null) {
                    if (config.replacementText == null || config.replacementText.isBlank()) config.replacementText = "Custom";
                    if (config.color == null) config.color = "GRAY";
                    return config;
                }
            } catch (Exception ignored) {
            }
        }

        NetheriteButtonConfig config = new NetheriteButtonConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }

    public ChatFormatting formatting() {
        try {
            return ChatFormatting.valueOf(color);
        } catch (IllegalArgumentException ex) {
            return ChatFormatting.GRAY;
        }
    }

    public void reset() {
        enabled = true;
        replacementText = "Custom";
        color = "GRAY";
    }
}
