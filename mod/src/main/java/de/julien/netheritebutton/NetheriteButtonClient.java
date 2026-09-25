package de.julien.netheritebutton;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.lwjgl.glfw.GLFW;

public final class NetheriteButtonClient implements ClientModInitializer {
    public static final String MOD_ID = "netheritebutton";
    public static NetheriteButtonConfig CONFIG;

    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\s*[0-9][0-9.,]*");
    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        CONFIG = NetheriteButtonConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main")
        );

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.netheritebutton.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                client.setScreen(new NetheriteButtonConfigScreen(client.screen));
            }
        });

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
            if (!CONFIG.enabled || !(stack.is(Items.SPRUCE_BUTTON) || stack.is(Items.NETHERITE_INGOT))) {
                return;
            }

            for (int i = 0; i < lines.size(); i++) {
                String plain = lines.get(i).getString();
                Matcher matcher = PRICE_PATTERN.matcher(plain);
                if (matcher.find()) {
                    String prefix = plain.substring(0, matcher.start());
                    String suffix = plain.substring(matcher.end());
                    lines.set(i,
                            Component.literal(prefix)
                                    .append(Component.literal(CONFIG.replacementText).withStyle(CONFIG.formatting()))
                                    .append(Component.literal(suffix))
                    );
                    break;
                }
            }
        });
    }
}
