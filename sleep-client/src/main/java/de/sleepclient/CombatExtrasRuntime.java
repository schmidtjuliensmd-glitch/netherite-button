package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

public final class CombatExtrasRuntime {
    private static boolean previousAttack;
    private static int restoreSlot = -1;
    private static int restoreDelay;
    private static int cooldown;

    public static void tick(Minecraft client) {
        if (client.player == null || client.gameMode == null) return;

        if (cooldown > 0) cooldown--;
        if (restoreDelay > 0 && --restoreDelay == 0 && restoreSlot >= 0) {
            setHeldSlot(client, restoreSlot);
            restoreSlot = -1;
        }

        tickShieldBreaker(client);
        tickAutoRod(client);
        tickBowRelease(client);
        tickAutoMace(client);

        previousAttack = client.options.keyAttack.isDown();
    }

    private static void tickShieldBreaker(Minecraft client) {
        if (!enabled("Auto Shield Disabler") && !enabled("Shield Breaker")) return;
        if (cooldown > 0 || !client.options.keyAttack.isDown()) return;
        if (!(client.hitResult instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof AbstractClientPlayer target)) return;
        if (!target.isBlocking()) return;

        int axeSlot = findHotbarBySuffix(client, "_axe");
        if (axeSlot < 0) return;

        int old = client.player.getInventory().getSelectedSlot();
        setHeldSlot(client, axeSlot);
        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);

        boolean swapBack = ConfigManager.boolOption(
                enabled("Shield Breaker") ? "Shield Breaker" : "Auto Shield Disabler",
                "switchBack",
                true
        );
        if (swapBack) {
            restoreSlot = old;
            restoreDelay = 3;
        }

        cooldown = Math.max(1, ConfigManager.intOption(
                enabled("Shield Breaker") ? "Shield Breaker" : "Auto Shield Disabler",
                "delay",
                1
        ));
    }

    private static void tickAutoRod(Minecraft client) {
        if (!enabled("Auto Rod") || cooldown > 0) return;

        boolean attack = client.options.keyAttack.isDown();
        if (!attack || previousAttack) return;

        int rodSlot = findHotbarItem(client, Items.FISHING_ROD);
        if (rodSlot < 0) return;

        int old = client.player.getInventory().getSelectedSlot();
        setHeldSlot(client, rodSlot);
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);

        restoreSlot = old;
        restoreDelay = 4;
        cooldown = Math.max(1, ConfigManager.intOption("Auto Rod","delay",4));
    }

    private static void tickBowRelease(Minecraft client) {
        if (!client.player.isUsingItem()) return;
        ItemStack use = client.player.getUseItem();
        if (!use.is(Items.BOW)) return;

        int ticks = client.player.getTicksUsingItem();

        if (enabled("Bow Spam")) {
            int charge = ConfigManager.intOption("Bow Spam","chargeTicks",5);
            if (ticks >= charge) {
                client.gameMode.releaseUsingItem(client.player);
                cooldown = 1;
                return;
            }
        }

        if (enabled("Auto Bow Release")) {
            int charge = ConfigManager.intOption("Auto Bow Release","chargeTicks",20);
            if (ticks >= charge) {
                client.gameMode.releaseUsingItem(client.player);
                cooldown = 1;
            }
        }
    }

    private static void tickAutoMace(Minecraft client) {
        if (!enabled("Auto Mace") || cooldown > 0) return;
        if (!(client.hitResult instanceof EntityHitResult hit)) return;
        if (!client.options.keyAttack.isDown()) return;

        float minFall = ConfigManager.floatOption("Auto Mace","minFall",4.0f);
        if (client.player.fallDistance < minFall) return;

        int maceSlot = findHotbarItem(client, Items.MACE);
        if (maceSlot < 0) return;

        int old = client.player.getInventory().getSelectedSlot();
        setHeldSlot(client, maceSlot);
        client.gameMode.attack(client.player, hit.getEntity());
        client.player.swing(InteractionHand.MAIN_HAND);

        restoreSlot = old;
        restoreDelay = 3;
        cooldown = 4;
    }

    private static int findHotbarItem(Minecraft client, net.minecraft.world.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }

    private static int findHotbarBySuffix(Minecraft client, String suffix) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            if (path.endsWith(suffix)) return i;
        }
        return -1;
    }

    private static void setHeldSlot(Minecraft client, int slot) {
        if (slot < 0 || slot > 8) return;
        client.player.getInventory().setSelectedSlot(slot);
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private CombatExtrasRuntime() {}
}
