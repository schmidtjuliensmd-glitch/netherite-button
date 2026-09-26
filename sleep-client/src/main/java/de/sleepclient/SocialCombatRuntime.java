package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class SocialCombatRuntime {
    private static boolean middleDown;
    private static boolean attackDown;
    private static int sprintResumeTicks;
    private static int backReleaseTicks;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player==null) return;

        tickMiddleFriend(client);
        tickSprintResets(client);

        if (sprintResumeTicks>0 && --sprintResumeTicks==0) client.player.setSprinting(true);
        if (backReleaseTicks>0 && --backReleaseTicks==0) client.options.keyDown.setDown(false);
    }

    private static void tickMiddleFriend(Minecraft client) {
        if (!enabled("Middle Click Friend")) {
            middleDown=false;
            return;
        }

        boolean down=GLFW.glfwGetMouseButton(client.getWindow().handle(),GLFW.GLFW_MOUSE_BUTTON_MIDDLE)==GLFW.GLFW_PRESS;
        if (down && !middleDown && client.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof AbstractClientPlayer target) {
            String name=target.getName().getString();
            boolean added=FriendManager.toggle(name);

            if (ConfigManager.boolOption("Middle Click Friend","notify",true)) {
                client.player.displayClientMessage(Component.literal(
                        "Sleep Client: "+name+(added?" als Freund hinzugefügt.":" aus Freunden entfernt.")
                ),false);
            }
        }
        middleDown=down;
    }

    private static void tickSprintResets(Minecraft client) {
        boolean attack=client.options.keyAttack.isDown();

        if (attack && !attackDown && client.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof AbstractClientPlayer) {
            if (enabled("WTap")) {
                client.player.setSprinting(false);
                sprintResumeTicks=Math.max(1,ConfigManager.intOption("WTap","delay",1));
            }

            if (enabled("STap")) {
                client.options.keyDown.setDown(true);
                backReleaseTicks=Math.max(1,ConfigManager.intOption("STap","delay",1));
            }
        }

        attackDown=attack;
    }

    private static boolean enabled(String name) {
        Module module=ModuleRegistry.find(name);
        return module!=null && module.enabled();
    }

    private SocialCombatRuntime() {}
}
