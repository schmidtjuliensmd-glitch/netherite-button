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
    private static KeyMapping keyPearl;
    private static boolean licenseWarningShown;
    private static boolean activationPromptShown;

    @Override
    public void onInitializeClient() {
        ModuleRegistry.init();
        ConfigManager.load();
        WaypointManager.load();
        FriendManager.load();
        PaymentTracker.init();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "sus_chunk_hud"),
                (graphics, deltaTracker) -> SusChunkFinder.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "player_esp_hud"),
                (graphics, deltaTracker) -> PlayerEsp.renderHud(graphics)
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
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "expanded_hud"),
                (graphics, deltaTracker) -> {
                    HudModulesRuntime.render(graphics);
                    EntityVisualRuntime.renderHud(graphics);
                    ExtraHudRuntime.render(graphics);
                }
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "chunk_analysis_hud"),
                (graphics, deltaTracker) -> ChunkAnalysisRuntime.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "navigation_hud"),
                (graphics, deltaTracker) -> NavigationVisualRuntime.renderHud(graphics)
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sleepclient", "finder_effects_hud"),
                (graphics, deltaTracker) -> {
                    LoadedFinderRuntime.renderHud(graphics);
                    VisualEffectsRuntime.renderHud(graphics);
                }
        );

        WorldRenderEvents.AFTER_ENTITIES.register(SusChunkFinder::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(PlayerEsp::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(StorageEsp::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(BlockSearchRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(EntityEspModules::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(DonutUtilityRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(EntityVisualRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(ChunkAnalysisRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(ViewVisualRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(NavigationVisualRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(LoadedFinderRuntime::renderWorld);
        WorldRenderEvents.AFTER_ENTITIES.register(VisualEffectsRuntime::renderWorld);
        LicenseManager.verifySaved().thenAccept(ok -> {
            if (ok) UpdateManager.checkForUpdates();
        });

        openGui = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.sleepclient.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
        keyPearl = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.sleepclient.key_pearl",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
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

            dispatcher.register(ClientCommandManager.literal("sleepwaypoint")
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        WaypointManager.addCurrent(name);
                                        context.getSource().sendFeedback(Component.literal("Sleep Client: waypoint added: " + name));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        boolean removed = WaypointManager.remove(name);
                                        context.getSource().sendFeedback(Component.literal(
                                                removed ? "Sleep Client: waypoint removed: " + name
                                                        : "Sleep Client: waypoint not found: " + name
                                        ));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("list")
                            .executes(context -> {
                                String names = WaypointManager.all().isEmpty()
                                        ? "none"
                                        : WaypointManager.all().stream().map(WaypointManager.Waypoint::name).reduce((a,b) -> a + ", " + b).orElse("none");
                                context.getSource().sendFeedback(Component.literal("Sleep Client waypoints: " + names));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("clear")
                            .executes(context -> {
                                WaypointManager.clear();
                                context.getSource().sendFeedback(Component.literal("Sleep Client: all waypoints cleared."));
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("sleepfriend")
                    .then(ClientCommandManager.literal("toggle")
                            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        boolean added = FriendManager.toggle(name);
                                        context.getSource().sendFeedback(Component.literal(
                                                "Sleep Client: " + name + (added ? " als Freund hinzugefügt." : " aus Freunden entfernt.")
                                        ));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("list")
                            .executes(context -> {
                                String names = FriendManager.all().isEmpty()
                                        ? "none"
                                        : String.join(", ", FriendManager.all());
                                context.getSource().sendFeedback(Component.literal("Sleep Client friends: " + names));
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("sleeppaymentsecret")
                    .then(ClientCommandManager.argument("secret", StringArgumentType.greedyString())
                            .executes(context -> {
                                String secret = StringArgumentType.getString(context, "secret").trim();
                                if (secret.isEmpty()) {
                                    context.getSource().sendFeedback(Component.literal("Sleep Client: payment tracker secret cannot be empty."));
                                    return 1;
                                }
                                boolean saved = PaymentTracker.setSecret(secret);
                                context.getSource().sendFeedback(Component.literal(
                                        saved
                                                ? "Sleep Client: payment tracker enabled. Incoming DonutSMP payments will be sent to your admin page."
                                                : "Sleep Client: could not save the payment tracker secret."
                                ));
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("sleeppaymentstatus")
                    .executes(context -> {
                        context.getSource().sendFeedback(Component.literal(
                                PaymentTracker.configured()
                                        ? "Sleep Client: payment tracker is enabled."
                                        : "Sleep Client: payment tracker is disabled. Use /sleeppaymentsecret <your admin secret>."
                        ));
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
            LifecycleModulesRuntime.tick(client);

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

            while (keyPearl.consumeClick()) {
                InventoryModulesRuntime.triggerKeyPearl(client);
            }

            ModuleRuntime.tick(client);
            UpdateManager.tick(client);
        });
    }
}
