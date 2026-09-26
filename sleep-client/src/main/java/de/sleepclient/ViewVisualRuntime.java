package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public final class ViewVisualRuntime {
    private static Integer originalFov;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified()) {
            restoreFov(client);
            return;
        }

        if (enabled("Zoom")) {
            if (originalFov == null) originalFov = client.options.fov().get();
            float factor = ConfigManager.floatOption("Zoom","factor",4.0f);
            int target = Math.max(10, Math.round(originalFov / Math.max(1.0f, factor)));
            client.options.fov().set(target);
            return;
        }

        if (enabled("FOV Changer")) {
            if (originalFov == null) originalFov = client.options.fov().get();
            int target = ConfigManager.intOption("FOV Changer","fov",90);
            client.options.fov().set(Math.max(30, Math.min(140, target)));
            return;
        }

        restoreFov(client);
    }

    public static void renderWorld(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (!LicenseManager.verified() || client.level == null || client.player == null) return;
        if (!(client.hitResult instanceof BlockHitResult hit)) return;

        boolean overlay = enabled("Block Overlay");
        boolean highlight = enabled("Block Highlight");
        if (!overlay && !highlight) return;

        var p = hit.getBlockPos();
        int color = highlight ? ThemeConfig.accentSecondary : ThemeConfig.accent;
        float width = ConfigManager.floatOption(highlight ? "Block Highlight" : "Block Overlay","lineWidth",2.0f);
        float opacity = ConfigManager.floatOption(highlight ? "Block Highlight" : "Block Overlay","opacity",0.7f);

        WorldBoxRenderer.box(
                context,
                new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1).inflate(0.002),
                color,
                opacity,
                width
        );
    }

    private static void restoreFov(Minecraft client) {
        if (originalFov != null) {
            client.options.fov().set(originalFov);
            originalFov = null;
        }
    }

    private static boolean enabled(String name) {
        Module m = ModuleRegistry.find(name);
        return m != null && m.enabled();
    }

    private ViewVisualRuntime() {}
}
