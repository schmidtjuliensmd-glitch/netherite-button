package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EntityVisualRuntime {
    public static void renderWorld(WorldRenderContext context) {
        if (!LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        int range = Math.max(
                ConfigManager.intOption("EntityESP", "range", 96),
                Math.max(
                        ConfigManager.intOption("MobESP", "range", 96),
                        ConfigManager.intOption("ProjectileESP", "range", 96)
                )
        );
        double maxSq = range * (double)range;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || entity.isRemoved()) continue;
            if (client.player.distanceToSqr(entity) > maxSq) continue;

            if (enabled("EntityESP")) {
                float width = ConfigManager.floatOption("EntityESP", "lineWidth", 2.0f);
                WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.04), ThemeConfig.accent, 0.90f, width);
            }

            if (enabled("MobESP") && entity instanceof Mob) {
                float width = ConfigManager.floatOption("MobESP", "lineWidth", 2.0f);
                WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.05), 0xFFFF8A5B, 0.95f, width);
            }

            if (enabled("ProjectileESP") && entity instanceof Projectile) {
                float width = ConfigManager.floatOption("ProjectileESP", "lineWidth", 2.0f);
                WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.08), 0xFF55B8FF, 0.95f, width);
            }

            if (enabled("Heat Color") && entity instanceof LivingEntity living) {
                boolean allowed = (entity instanceof Player && ConfigManager.boolOption("Heat Color","players",true))
                        || (!(entity instanceof Player) && ConfigManager.boolOption("Heat Color","mobs",true));
                if (allowed) {
                    float health = Math.max(0.0f, living.getHealth());
                    float maxHealth = Math.max(1.0f, living.getMaxHealth());
                    float pct = Math.max(0.0f, Math.min(1.0f, health / maxHealth));
                    int red = (int)Math.round(255 * (1.0f - pct));
                    int green = (int)Math.round(255 * pct);
                    int color = 0xFF000000 | (red << 16) | (green << 8) | 0x45;
                    float opacity = ConfigManager.floatOption("Heat Color","opacity",0.8f);
                    WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.07), color, opacity, 2.2f);
                }
            }

            if (enabled("Tracers")) {
                String target = ConfigManager.stringOption("Tracers","target","Players");
                boolean match = switch (target) {
                    case "Mobs" -> entity instanceof Mob;
                    case "Items" -> entity instanceof net.minecraft.world.entity.item.ItemEntity;
                    case "All" -> true;
                    default -> entity instanceof Player;
                };
                if (match) {
                    int tracerRange = ConfigManager.intOption("Tracers","range",128);
                    if (client.player.distanceToSqr(entity) <= tracerRange * (double)tracerRange) {
                        float width = ConfigManager.floatOption("Tracers","lineWidth",1.5f);
                        Vec3 from = client.player.getEyePosition();
                        Vec3 to = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
                        WorldBoxRenderer.line(context, from, to, ThemeConfig.accent, 0.85f, width);
                    }
                }
            }
        }

        renderTargetEsp(context, client);
    }

    private static void renderTargetEsp(WorldRenderContext context, Minecraft client) {
        if (!enabled("Target ESP")) return;
        if (!(client.hitResult instanceof EntityHitResult hit)) return;

        Entity entity = hit.getEntity();
        String style = ConfigManager.stringOption("Target ESP","style","Ring");
        float size = ConfigManager.floatOption("Target ESP","size",1.0f);

        if ("Box".equals(style)) {
            WorldBoxRenderer.box(context, entity.getBoundingBox().inflate(0.08 * size), ThemeConfig.accent, 1.0f, 2.8f);
            return;
        }

        Vec3 center = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
        double radius = 0.65 * size;
        int segments = "Marker".equals(style) ? 4 : 24;

        Vec3 previous = null;
        for (int i = 0; i <= segments; i++) {
            double a = Math.PI * 2.0 * i / segments;
            Vec3 point = center.add(Math.cos(a) * radius, 0.05, Math.sin(a) * radius);
            if (previous != null) {
                WorldBoxRenderer.line(context, previous, point, ThemeConfig.accent, 0.98f, 2.2f);
            }
            previous = point;
        }
    }

    public static void renderHud(GuiGraphics g) {
        if (!LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        if (enabled("Health Bars")) renderHealthList(g, client);
    }

    private static void renderHealthList(GuiGraphics g, Minecraft client) {
        int range = ConfigManager.intOption("Health Bars","range",96);
        boolean showPlayers = ConfigManager.boolOption("Health Bars","players",true);
        boolean showMobs = ConfigManager.boolOption("Health Bars","mobs",true);

        List<LivingEntity> list = new ArrayList<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == client.player || entity.isRemoved()) continue;
            boolean match = (entity instanceof Player && showPlayers) || (!(entity instanceof Player) && showMobs);
            if (!match) continue;
            if (client.player.distanceToSqr(entity) > range * (double)range) continue;
            list.add(living);
        }

        list.sort(Comparator.comparingDouble(client.player::distanceToSqr));
        int visible = Math.min(list.size(), 6);
        if (visible == 0) return;

        int w = 192;
        int h = 28 + visible * 18;
        int x = 14;
        int y = 215;

        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xE80D0912);
        SmoothTextRenderer.draw(g, "Health Bars", x + 10, y + 8, 8.8f, 0xFFF4EFF6, true);

        for (int i = 0; i < visible; i++) {
            LivingEntity e = list.get(i);
            float pct = Math.max(0.0f, Math.min(1.0f, e.getHealth() / Math.max(1.0f, e.getMaxHealth())));
            String name = e.getName().getString();
            String hp = String.format("%.1f", e.getHealth());
            int rowY = y + 27 + i * 18;

            SmoothTextRenderer.draw(g, name, x + 10, rowY, 7.6f, 0xFFD9D0DD, false);
            int barX = x + 92;
            int barW = 65;
            g.fill(barX, rowY + 2, barX + barW, rowY + 7, 0xFF2A2230);
            int fill = Math.max(1, Math.round(barW * pct));
            int red = Math.round(255 * (1.0f - pct));
            int green = Math.round(255 * pct);
            int color = 0xFF000000 | (red << 16) | (green << 8) | 0x45;
            g.fill(barX, rowY + 2, barX + fill, rowY + 7, color);

            int hw = SmoothTextRenderer.width(hp, 7.2f, true);
            SmoothTextRenderer.draw(g, hp, x + w - 9 - hw, rowY, 7.2f, 0xFFE7DFEA, true);
        }
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private EntityVisualRuntime() {}
}
