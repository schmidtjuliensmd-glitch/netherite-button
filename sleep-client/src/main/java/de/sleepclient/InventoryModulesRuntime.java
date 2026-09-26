package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class InventoryModulesRuntime {
    private static int actionCooldown;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player == null || client.gameMode == null) return;
        if (actionCooldown > 0) actionCooldown--;

        tickAutoTotem(client);
        tickAutoInventoryTotem(client);
        tickAutoRefill(client);
        tickAutoWeapon(client);
        tickAutoArmor(client);
    }

    private static void tickAutoTotem(Minecraft client) {
        if (!enabled("Auto Totem") || actionCooldown > 0) return;

        boolean always = ConfigManager.boolOption("Auto Totem","always",true);
        float health = ConfigManager.floatOption("Auto Totem","health",8.0f);
        if (!always && client.player.getHealth() > health) return;
        if (client.player.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.TOTEM_OF_UNDYING)) return;

        int slot = findInventory(client, Items.TOTEM_OF_UNDYING, 0, 35);
        if (slot < 0) return;

        moveInventorySlotToMenuSlot(client, slot, 45);
        actionCooldown = 4;
    }

    private static void tickAutoInventoryTotem(Minecraft client) {
        if (!enabled("Auto Inventory Totem") || actionCooldown > 0) return;
        if (client.player.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.TOTEM_OF_UNDYING)) return;

        int reserve = ConfigManager.intOption("Auto Inventory Totem","reserve",2);
        if (countItem(client, Items.TOTEM_OF_UNDYING) < reserve) return;

        int slot = findInventory(client, Items.TOTEM_OF_UNDYING, 0, 35);
        if (slot < 0) return;

        moveInventorySlotToMenuSlot(client, slot, 45);
        actionCooldown = 4;
    }

    private static void tickAutoRefill(Minecraft client) {
        if (!enabled("Auto Refill Hotbar") || actionCooldown > 0) return;

        int threshold = ConfigManager.intOption("Auto Refill Hotbar","threshold",16);
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            ItemStack target = client.player.getInventory().getItem(hotbar);
            if (target.isEmpty() || target.getCount() > threshold || target.getMaxStackSize() <= 1) continue;

            Item item = target.getItem();
            int source = findInventory(client, item, 9, 35);
            if (source < 0) continue;

            int sourceMenu = inventoryToMenu(source);
            client.gameMode.handleInventoryMouseClick(
                    client.player.inventoryMenu.containerId,
                    sourceMenu,
                    0,
                    ClickType.QUICK_MOVE,
                    client.player
            );
            actionCooldown = Math.max(1, ConfigManager.intOption("Auto Refill Hotbar","delay",4));
            return;
        }
    }

    private static void tickAutoWeapon(Minecraft client) {
        if (!enabled("Auto Weapon")) return;
        if (!client.options.keyAttack.isDown()) return;

        String preferred = ConfigManager.stringOption("Auto Weapon","weapon","Best Damage");
        int best = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            int score = weaponScore(path, preferred);
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }

        if (best >= 0 && bestScore > 0) client.player.getInventory().setSelectedSlot(best);
    }

    private static void tickAutoArmor(Minecraft client) {
        if (!enabled("Auto Armor") || actionCooldown > 0) return;

        int bestSlot = -1;
        EquipmentSlot equipmentSlot = null;
        int bestScore = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            EquipmentSlot target = armorSlot(path);
            if (target == null) continue;

            int score = armorScore(path);
            ItemStack equipped = client.player.getItemBySlot(target);
            int equippedScore = equipped.isEmpty()
                    ? -1
                    : armorScore(BuiltInRegistries.ITEM.getKey(equipped.getItem()).getPath());

            if (score > equippedScore && score > bestScore) {
                bestScore = score;
                bestSlot = i;
                equipmentSlot = target;
            }
        }

        if (bestSlot < 0 || equipmentSlot == null) return;

        int menuSlot = switch (equipmentSlot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> -1;
        };
        if (menuSlot < 0) return;

        moveInventorySlotToMenuSlot(client, bestSlot, menuSlot);
        actionCooldown = Math.max(1, ConfigManager.intOption("Auto Armor","delay",2));
    }

    private static void moveInventorySlotToMenuSlot(Minecraft client, int inventorySlot, int targetMenuSlot) {
        int sourceMenu = inventoryToMenu(inventorySlot);
        int container = client.player.inventoryMenu.containerId;

        client.gameMode.handleInventoryMouseClick(container, sourceMenu, 0, ClickType.PICKUP, client.player);
        client.gameMode.handleInventoryMouseClick(container, targetMenuSlot, 0, ClickType.PICKUP, client.player);
        client.gameMode.handleInventoryMouseClick(container, sourceMenu, 0, ClickType.PICKUP, client.player);
    }

    private static int inventoryToMenu(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot <= 8) return 36 + inventorySlot;
        return inventorySlot;
    }

    private static int findInventory(Minecraft client, Item item, int from, int to) {
        for (int i = from; i <= to; i++) {
            if (client.player.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }

    private static int countItem(Minecraft client, Item item) {
        int count = 0;
        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        if (client.player.getItemBySlot(EquipmentSlot.OFFHAND).is(item)) count++;
        return count;
    }

    private static int weaponScore(String path, String preferred) {
        boolean mace = path.endsWith("mace");
        boolean sword = path.endsWith("_sword");
        boolean axe = path.endsWith("_axe");

        if ("Mace".equals(preferred)) return mace ? 100 : 0;
        if ("Sword".equals(preferred)) return sword ? 100 + tier(path) : 0;
        if ("Axe".equals(preferred)) return axe ? 100 + tier(path) : 0;

        if (mace) return 120;
        if (axe) return 100 + tier(path);
        if (sword) return 95 + tier(path);
        return 0;
    }

    private static EquipmentSlot armorSlot(String path) {
        if (path.endsWith("_helmet")) return EquipmentSlot.HEAD;
        if (path.endsWith("_chestplate")) return EquipmentSlot.CHEST;
        if (path.endsWith("_leggings")) return EquipmentSlot.LEGS;
        if (path.endsWith("_boots")) return EquipmentSlot.FEET;
        return null;
    }

    private static int armorScore(String path) {
        return tier(path) * 100 + (path.contains("netherite") ? 20 : 0);
    }

    private static int tier(String path) {
        if (path.startsWith("netherite_")) return 6;
        if (path.startsWith("diamond_")) return 5;
        if (path.startsWith("iron_")) return 4;
        if (path.startsWith("chainmail_")) return 3;
        if (path.startsWith("golden_")) return 2;
        if (path.startsWith("stone_")) return 2;
        if (path.startsWith("leather_")) return 1;
        if (path.startsWith("wooden_")) return 1;
        return 0;
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private InventoryModulesRuntime() {}
}
