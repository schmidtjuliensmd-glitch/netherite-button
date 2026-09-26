package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CombatModulesRuntime {
    private static int auraCooldown;
    private static int clickCooldown;
    private static boolean lastAttackHeld;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player == null || client.level == null || client.gameMode == null) return;

        if (auraCooldown > 0) auraCooldown--;
        if (clickCooldown > 0) clickCooldown--;

        if (enabled("Aim Assist")) tickAimAssist(client, "Aim Assist");
        if (enabled("Bow Aim")) tickAimAssist(client, "Bow Aim");

        if (enabled("KillAura")) tickKillAura(client);
        if (enabled("Trigger Bot")) tickTriggerBot(client);
        if (enabled("AutoClicker")) tickAutoClicker(client);
        if (enabled("Criticals")) tickCriticals(client);

        lastAttackHeld = client.options.keyAttack.isDown();
    }

    private static void tickAimAssist(Minecraft client, String moduleName) {
        float range = ConfigManager.floatOption(moduleName, "range", moduleName.equals("Bow Aim") ? 48.0f : 5.0f);
        int fov = ConfigManager.intOption(moduleName, "fov", 90);
        float smoothing = ConfigManager.floatOption(moduleName, "smoothing", 0.35f);

        AbstractClientPlayer target = findTarget(client, range, fov);
        if (target == null) return;

        Rotation rot = rotationTo(client.player.getEyePosition(), target.getEyePosition());
        float currentYaw = client.player.getYRot();
        float currentPitch = client.player.getXRot();

        float yawDiff = wrapDegrees(rot.yaw() - currentYaw);
        float pitchDiff = rot.pitch() - currentPitch;

        float factor = Math.max(0.05f, Math.min(1.0f, smoothing));
        client.player.setYRot(currentYaw + yawDiff * factor);
        client.player.setXRot(clampPitch(currentPitch + pitchDiff * factor));
    }

    private static void tickKillAura(Minecraft client) {
        if (auraCooldown > 0) return;

        float range = ConfigManager.floatOption("KillAura", "range", 4.5f);
        int fov = ConfigManager.intOption("KillAura", "fov", 180);
        int cps = Math.max(1, ConfigManager.intOption("KillAura", "cps", 10));

        AbstractClientPlayer target = findTarget(client, range, fov);
        if (target == null) return;
        if (client.player.getAttackStrengthScale(0.0f) < 0.9f) return;

        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);

        auraCooldown = Math.max(1, 20 / cps);
    }

    private static void tickTriggerBot(Minecraft client) {
        if (!(client.hitResult instanceof EntityHitResult hit)) return;
        Entity target = hit.getEntity();
        if (!(target instanceof AbstractClientPlayer)) return;
        if (client.player.getAttackStrengthScale(0.0f) < 0.9f) return;

        int cps = Math.max(1, ConfigManager.intOption("Trigger Bot", "cps", 10));
        if (clickCooldown > 0) return;

        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);
        clickCooldown = Math.max(1, 20 / cps);
    }

    private static void tickAutoClicker(Minecraft client) {
        if (!client.options.keyAttack.isDown()) return;
        if (!(client.hitResult instanceof EntityHitResult hit)) return;
        if (clickCooldown > 0) return;

        int min = Math.max(1, ConfigManager.intOption("AutoClicker", "minCps", 8));
        int max = Math.max(min, ConfigManager.intOption("AutoClicker", "maxCps", 12));
        boolean randomize = ConfigManager.boolOption("AutoClicker", "randomize", true);

        int cps = randomize ? ThreadLocalRandom.current().nextInt(min, max + 1) : max;

        client.gameMode.attack(client.player, hit.getEntity());
        client.player.swing(InteractionHand.MAIN_HAND);
        clickCooldown = Math.max(1, Math.round(20.0f / cps));
    }

    private static void tickCriticals(Minecraft client) {
        if (!client.options.keyAttack.isDown() || lastAttackHeld) return;
        if (!client.player.onGround()) return;
        if (!(client.hitResult instanceof EntityHitResult)) return;

        Vec3 motion = client.player.getDeltaMovement();
        client.player.setDeltaMovement(motion.x, 0.20D, motion.z);
    }

    private static AbstractClientPlayer findTarget(Minecraft client, float range, int fov) {
        List<AbstractClientPlayer> players = client.level.players().stream()
                .filter(p -> p != client.player)
                .filter(p -> !p.isRemoved() && p.isAlive())
                .filter(p -> !p.isSpectator())
                .filter(p -> client.player.distanceToSqr(p) <= range * (double)range)
                .filter(p -> yawDistance(client.player.getYRot(),
                        rotationTo(client.player.getEyePosition(), p.getEyePosition()).yaw()) <= fov * 0.5f)
                .toList();

        if (players.isEmpty()) return null;

        String priority = ConfigManager.stringOption("Target Selector", "priority",
                ConfigManager.stringOption("KillAura", "priority", "Distance"));

        Comparator<AbstractClientPlayer> comparator = switch (priority) {
            case "Health" -> Comparator.comparingDouble(AbstractClientPlayer::getHealth);
            case "Angle" -> Comparator.comparingDouble(p ->
                    yawDistance(client.player.getYRot(), rotationTo(client.player.getEyePosition(), p.getEyePosition()).yaw()));
            default -> Comparator.comparingDouble(client.player::distanceToSqr);
        };

        return players.stream().min(comparator).orElse(null);
    }

    private static Rotation rotationTo(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizontal)));
        return new Rotation(yaw, pitch);
    }

    private static float yawDistance(float a, float b) {
        return Math.abs(wrapDegrees(b - a));
    }

    private static float wrapDegrees(float value) {
        value %= 360.0f;
        if (value >= 180.0f) value -= 360.0f;
        if (value < -180.0f) value += 360.0f;
        return value;
    }

    private static float clampPitch(float value) {
        return Math.max(-90.0f, Math.min(90.0f, value));
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private record Rotation(float yaw, float pitch) {}

    private CombatModulesRuntime() {}
}
