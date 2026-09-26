package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class MovementModulesRuntime {
    private static boolean forcedForward;
    private static boolean forcedSneak;
    private static boolean forcedJump;
    private static Boolean originalMayFly;
    private static Boolean originalFlying;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player == null || client.level == null) {
            restoreKeys(client);
            restoreFlight(client);
            return;
        }

        tickAutoWalk(client);
        tickSneak(client);
        tickBunnyHop(client);
        tickHighJump(client);
        tickFastFall(client);
        tickSpeed(client);
        tickFlight(client);
        tickElytraBoost(client);
        tickLongJump(client);
        tickAntiVoid(client);
    }

    private static void tickAutoWalk(Minecraft client) {
        if (enabled("Auto Walk")) {
            client.options.keyUp.setDown(true);
            forcedForward = true;
        } else if (forcedForward) {
            client.options.keyUp.setDown(false);
            forcedForward = false;
        }
    }

    private static void tickSneak(Minecraft client) {
        if (enabled("Sneak") && "Always".equals(ConfigManager.stringOption("Sneak","mode","Always"))) {
            client.options.keyShift.setDown(true);
            forcedSneak = true;
        } else if (forcedSneak) {
            client.options.keyShift.setDown(false);
            forcedSneak = false;
        }
    }

    private static void tickBunnyHop(Minecraft client) {
        boolean should = enabled("Bunny Hop")
                && client.options.keyUp.isDown()
                && client.player.onGround();

        if (should) {
            client.options.keyJump.setDown(true);
            forcedJump = true;
        } else if (forcedJump && !enabled("High Jump")) {
            client.options.keyJump.setDown(false);
            forcedJump = false;
        }
    }

    private static void tickHighJump(Minecraft client) {
        if (!enabled("High Jump")) return;
        if (!client.player.onGround() || !client.options.keyJump.isDown()) return;

        float height = ConfigManager.floatOption("High Jump","height",2.0f);
        Vec3 motion = client.player.getDeltaMovement();
        double jumpVelocity = 0.42D * Math.max(1.0D, height);
        client.player.setDeltaMovement(motion.x, jumpVelocity, motion.z);
    }

    private static void tickFastFall(Minecraft client) {
        if (!enabled("Fast Fall") || client.player.onGround()) return;

        Vec3 motion = client.player.getDeltaMovement();
        if (motion.y >= -0.01D) return;

        float speed = ConfigManager.floatOption("Fast Fall","speed",2.0f);
        double y = Math.max(-3.5D, motion.y * Math.max(1.0D, speed));
        client.player.setDeltaMovement(motion.x, y, motion.z);
    }

    private static void tickSpeed(Minecraft client) {
        if (!enabled("Speed")) return;
        if (!client.options.keyUp.isDown()
                && !client.options.keyDown.isDown()
                && !client.options.keyLeft.isDown()
                && !client.options.keyRight.isDown()) return;

        float speed = ConfigManager.floatOption("Speed","speed",1.5f);
        Vec3 motion = client.player.getDeltaMovement();

        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal < 0.01D) return;

        double target = Math.min(0.75D, horizontal * Math.max(1.0D, speed));
        double factor = target / horizontal;
        client.player.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
    }

    private static void tickFlight(Minecraft client) {
        if (enabled("Flight")) {
            if (originalMayFly == null) {
                originalMayFly = client.player.getAbilities().mayfly;
                originalFlying = client.player.getAbilities().flying;
            }

            client.player.getAbilities().mayfly = true;
            client.player.getAbilities().flying = true;

            float speed = ConfigManager.floatOption("Flight","speed",1.0f);
            client.player.getAbilities().setFlyingSpeed(Math.max(0.01f, Math.min(0.25f, 0.05f * speed)));
        } else {
            restoreFlight(client);
        }
    }

    private static void tickElytraBoost(Minecraft client) {
        if (!enabled("Elytra Boost") || !client.player.isFallFlying()) return;
        if (!client.options.keyUp.isDown()) return;

        float boost = ConfigManager.floatOption("Elytra Boost","boost",0.8f);
        Vec3 look = client.player.getLookAngle();
        Vec3 motion = client.player.getDeltaMovement();
        double scale = 0.035D * boost;
        client.player.setDeltaMovement(
                motion.x + look.x * scale,
                motion.y + look.y * scale * 0.35D,
                motion.z + look.z * scale
        );
    }

    private static void tickLongJump(Minecraft client) {
        if (!enabled("Long Jump")) return;
        if (!client.player.onGround() || !client.options.keyJump.isDown() || !client.options.keyUp.isDown()) return;

        float power = ConfigManager.floatOption("Long Jump","power",2.0f);
        float height = ConfigManager.floatOption("Long Jump","height",0.6f);
        Vec3 look = client.player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 0.001D) return;

        horizontal = horizontal.normalize().scale(0.32D * power);
        client.player.setDeltaMovement(horizontal.x, 0.42D + height * 0.22D, horizontal.z);
    }

    private static void tickAntiVoid(Minecraft client) {
        if (!enabled("Anti Void")) return;

        int threshold = ConfigManager.intOption("Anti Void","fallDistance",8);
        int minY = client.level.getMinY();
        if (client.player.getY() > minY + threshold) return;
        if (client.player.getDeltaMovement().y >= 0.0D) return;

        String action = ConfigManager.stringOption("Anti Void","action","Stop");
        switch (action) {
            case "Jump" -> {
                Vec3 m = client.player.getDeltaMovement();
                client.player.setDeltaMovement(m.x, 0.6D, m.z);
            }
            case "Disconnect" -> {
                if (client.getConnection() != null) {
                    client.getConnection().getConnection().disconnect(
                            Component.literal("Sleep Client Anti Void")
                    );
                }
            }
            default -> {
                Vec3 m = client.player.getDeltaMovement();
                client.player.setDeltaMovement(0.0D, Math.max(0.0D, m.y), 0.0D);
            }
        }
    }

    private static void restoreKeys(Minecraft client) {
        if (forcedForward) client.options.keyUp.setDown(false);
        if (forcedSneak) client.options.keyShift.setDown(false);
        if (forcedJump) client.options.keyJump.setDown(false);
        forcedForward = false;
        forcedSneak = false;
        forcedJump = false;
    }

    private static void restoreFlight(Minecraft client) {
        if (client.player == null || originalMayFly == null) return;

        client.player.getAbilities().mayfly = originalMayFly;
        client.player.getAbilities().flying = Boolean.TRUE.equals(originalFlying);
        client.player.getAbilities().setFlyingSpeed(0.05f);

        originalMayFly = null;
        originalFlying = null;
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private MovementModulesRuntime() {}
}
