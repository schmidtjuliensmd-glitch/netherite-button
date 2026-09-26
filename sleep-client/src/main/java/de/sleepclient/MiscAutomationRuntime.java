package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MiscAutomationRuntime {
    private static int afkTicks;
    private static int useCooldown;
    private static int leaveCooldown;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player == null || client.level == null || client.gameMode == null) return;

        if (useCooldown > 0) useCooldown--;
        if (leaveCooldown > 0) leaveCooldown--;

        tickAntiAfk(client);
        tickAutoTool(client);
        tickAutoEat(client);
        tickAutoGap(client);
        tickAutoXp(client);
        tickAutoLeave(client);
    }

    private static void tickAntiAfk(Minecraft client) {
        if (!enabled("Anti AFK")) {
            afkTicks = 0;
            return;
        }

        int seconds = ConfigManager.intOption("Anti AFK","interval",45);
        if (++afkTicks < Math.max(20, seconds * 20)) return;
        afkTicks = 0;

        String action = ConfigManager.stringOption("Anti AFK","action","Jump");
        switch (action) {
            case "Turn" -> client.player.setYRot(client.player.getYRot() + 32.0f);
            case "Walk" -> {
                Vec3 look = client.player.getLookAngle();
                Vec3 m = client.player.getDeltaMovement();
                client.player.setDeltaMovement(m.x + look.x * 0.08, m.y, m.z + look.z * 0.08);
            }
            default -> {
                if (client.player.onGround()) {
                    Vec3 m = client.player.getDeltaMovement();
                    client.player.setDeltaMovement(m.x, 0.42D, m.z);
                }
            }
        }
    }

    private static void tickAutoTool(Minecraft client) {
        if (!enabled("Auto Tool")) return;
        if (!(client.hitResult instanceof BlockHitResult hit)) return;
        if (!client.options.keyAttack.isDown()) return;

        BlockState state = client.level.getBlockState(hit.getBlockPos());
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0) client.player.getInventory().setSelectedSlot(bestSlot);
    }

    private static void tickAutoEat(Minecraft client) {
        if (!enabled("Auto Eat") || useCooldown > 0) return;

        int trigger = ConfigManager.intOption("Auto Eat","hunger",14);
        if (client.player.getFoodData().getFoodLevel() > trigger) return;

        int slot = findFood(client);
        if (slot < 0) return;

        client.player.getInventory().setSelectedSlot(slot);
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        useCooldown = 12;
    }

    private static void tickAutoGap(Minecraft client) {
        if (!enabled("Auto Gap") || useCooldown > 0) return;

        float trigger = ConfigManager.floatOption("Auto Gap","health",10.0f);
        if (client.player.getHealth() > trigger) return;

        boolean enchantedFirst = ConfigManager.boolOption("Auto Gap","enchantedFirst",false);
        int slot = enchantedFirst
                ? findHotbar(client, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)
                : findHotbar(client, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);

        if (slot < 0) return;

        client.player.getInventory().setSelectedSlot(slot);
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        useCooldown = 12;
    }

    private static void tickAutoXp(Minecraft client) {
        if (!enabled("Auto XP") || useCooldown > 0) return;

        int delay = ConfigManager.intOption("Auto XP","delay",2);
        int slot = findHotbar(client, Items.EXPERIENCE_BOTTLE);
        if (slot < 0) return;

        client.player.getInventory().setSelectedSlot(slot);
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        useCooldown = Math.max(1, delay);
    }

    private static void tickAutoLeave(Minecraft client) {
        if (!enabled("Auto Leave") || leaveCooldown > 0) return;

        float health = ConfigManager.floatOption("Auto Leave","health",4.0f);
        boolean noTotems = ConfigManager.boolOption("Auto Leave","totems",false);

        boolean trigger = client.player.getHealth() <= health;
        if (noTotems) trigger |= countTotems(client) == 0;

        if (!trigger || client.getConnection() == null) return;

        leaveCooldown = 200;
        client.getConnection().getConnection().disconnect(
                Component.literal("Sleep Client Auto Leave")
        );
    }

    private static int findFood(Minecraft client) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.has(DataComponents.FOOD)) return i;
        }
        return -1;
    }

    private static int findHotbar(Minecraft client, net.minecraft.world.item.Item... items) {
        for (net.minecraft.world.item.Item item : items) {
            for (int i = 0; i < 9; i++) {
                if (client.player.getInventory().getItem(i).is(item)) return i;
            }
        }
        return -1;
    }

    private static int countTotems(Minecraft client) {
        int count = 0;
        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING)) count += stack.getCount();
        }
        return count;
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private MiscAutomationRuntime() {}
}
