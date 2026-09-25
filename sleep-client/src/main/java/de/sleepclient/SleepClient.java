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

public final class SleepClient implements ClientModInitializer {
    public static final String NAME = "Sleep Client";
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("sleepclient", "main")
    );
    private static KeyMapping openGui;

    @Override
    public void onInitializeClient() {
        ModuleRegistry.init();
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
                                        client.execute(() -> context.getSource().sendFeedback(Component.literal(
                                                ok ? "Sleep Client: signed in as " + mcName + " (" + LicenseManager.plan() + ")"
                                                   : "Sleep Client: sign in failed (" + LicenseManager.message() + ")"
                                        )))
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
            while (openGui.consumeClick()) {
                client.setScreen(new SleepClickGuiScreen());
            }
            ModuleRuntime.tick(client);
            UpdateManager.tick(client);
        });
    }
}
