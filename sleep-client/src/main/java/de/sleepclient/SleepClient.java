package de.sleepclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public final class SleepClient implements ClientModInitializer {
    public static final String NAME = "Sleep Client";
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("sleepclient", "main")
    );
    private static KeyMapping openGui;
    private static boolean licenseWarningShown;
    private static boolean activationPromptShown;

    @Override
    public void onInitializeClient() {
        ModuleRegistry.init();
        ConfigManager.load();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "sus_chunk_hud"),
                (graphics, deltaTracker) -> SusChunkFinder.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "player_esp_hud"),
                (graphics, deltaTracker) -> PlayerEsp.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "block_search_hud"),
                (graphics, deltaTracker) -> BlockSearchRuntime.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "storage_esp_hud"),
                (graphics, deltaTracker) -> StorageEsp.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "trader_finder_hud"),
                (graphics, deltaTracker) -> EntityEspModules.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "totem_counter_hud"),
                (graphics, deltaTracker) -> TotemCounterHud.render(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "donut_utility_hud"),
                (graphics, deltaTracker) -> DonutUtilityRuntime.renderHud(graphics)
        );

        WorldRenderEvents.AFTER_ENTITIES.register(SusChunkFinder::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(PlayerEsp::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(StorageEsp::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(BlockSearchRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(EntityEspModules::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(DonutUtilityRuntime::renderWorld);
        LicenseManager.verifySaved().thenAccept(ok -> {
            if (ok) UpdateManager.checkForUpdates();
        });

        openGui = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.sleepclient.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("sleepkey")
                    .then(ClientCommandManager.argument("key", StringArgumentType.greedyString())
                            .executes(context -> {
                                Minecraft client = Minecraft.getInstance();
                                String mcName = client.getUser().getName();
                                String key = StringArgumentType.getString(context, "key");
                                context.getSource().sendFeedback(Component.literal("Sleep Client: checking license..."));
                                LicenseManager.saveAndVerify(mcName, key).thenAccept(ok ->
                                        client.execute(() -> {
                                            context.getSource().sendFeedback(Component.literal(
                                                    ok ? "Sleep Client: signed in as " + mcName + " (" + LicenseManager.plan() + ")"
                                                       : "Sleep Client: sign in failed (" + LicenseManager.message() + ")"
                                            ));
                                            if (ok) UpdateManager.checkForUpdates();
                                        })
                                );
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("sleepupdate")
                    .executes(context -> {
                        if (!LicenseManager.verified()) {
                            context.getSource().sendFeedback(Component.literal("Sleep Client: sign in first with /sleepkey."));
                            return 1;
                        }
                        UpdateManager.checkForUpdates().thenRun(() -> Minecraft.getInstance().execute(() -> {
                            String text = UpdateManager.updateAvailable()
                                    ? "Sleep Client: version " + UpdateManager.latestVersion() + " is available. Opening download page..."
                                    : "Sleep Client: you already have the latest version (" + UpdateManager.currentVersion() + ").";
                            context.getSource().sendFeedback(Component.literal(text));
                            if (UpdateManager.updateAvailable()) UpdateManager.openLatestDownload();
                        }));
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("sleepaccount")
                    .executes(context -> {
                        String text = LicenseManager.verified()
                                ? "Sleep Client: " + LicenseManager.username() + " · " + LicenseManager.plan()
                                : "Sleep Client: not signed in (" + LicenseManager.message() + ")";
                        context.getSource().sendFeedback(Component.literal(text));
                        return 1;
                    }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LicenseManager.tick();

            if (!LicenseManager.verified()) {
                if (client.player != null && !licenseWarningShown && "license_expired".equals(LicenseManager.message())) {
                    client.player.displayClientMessage(Component.literal(
                            "Sleep Client: your license has expired. Renew your access and enter the new key with /sleepkey."
                    ), false);
                    licenseWarningShown = true;
                }
            } else {
                licenseWarningShown = false;
            }

            if (!LicenseManager.verified()
                    && client.player != null
                    && client.screen == null
                    && !activationPromptShown
                    && !"Checking license...".equals(LicenseManager.message())) {
                activationPromptShown = true;
                client.setScreen(new SleepActivationScreen());
            }

            while (openGui.consumeClick()) {
                if (LicenseManager.verified()) {
                    client.setScreen(new SleepClickGuiScreen());
                } else {
                    activationPromptShown = true;
                    client.setScreen(new SleepActivationScreen());
                }
            }
            ModuleRuntime.tick(client);
            UpdateManager.tick(client);
        });
    }
}
