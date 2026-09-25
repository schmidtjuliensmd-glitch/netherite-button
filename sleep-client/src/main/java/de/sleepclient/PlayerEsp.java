package de.sleepclient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PlayerEsp {
    public static void renderWorld(WorldRenderContext context) {
        Module module = ModuleRegistry.find("PlayerESP");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        Vec3 cam = client.gameRenderer.getMainCamera().position();
        PoseStack matrices = context.matrices();
        VertexConsumer lines = context.consumers().getBuffer(RenderTypes.linesTranslucent());

        int accent = ThemeConfig.accent;
        float red = ((accent >> 16) & 0xFF) / 255.0f;
        float green = ((accent >> 8) & 0xFF) / 255.0f;
        float blue = (accent & 0xFF) / 255.0f;

        double maxSq = ConfigManager.playerEspRange * (double)ConfigManager.playerEspRange;

        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player || player.isRemoved()) continue;
            if (client.player.distanceToSqr(player) > maxSq) continue;

            AABB box = player.getBoundingBox().inflate(0.06);
            double x1 = box.minX - cam.x;
            double y1 = box.minY - cam.y;
            double z1 = box.minZ - cam.z;
            double x2 = box.maxX - cam.x;
            double y2 = box.maxY - cam.y;
            double z2 = box.maxZ - cam.z;

            renderLineBox(
                    matrices.last(),
                    lines,
                    x1, y1, z1,
                    x2, y2, z2,
                    red, green, blue, 0.95f,
                    ConfigManager.playerEspLineWidth
            );
        }
    }

    public static void renderHud(GuiGraphics g) {
        Module module = ModuleRegistry.find("PlayerESP");
        if (module == null || !module.enabled() || !LicenseManager.verified() || !ConfigManager.playerEspDistanceHud) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        List<AbstractClientPlayer> nearby = new ArrayList<>();
        double maxSq = ConfigManager.playerEspRange * (double)ConfigManager.playerEspRange;

        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player || player.isRemoved()) continue;
            if (client.player.distanceToSqr(player) <= maxSq) nearby.add(player);
        }

        nearby.sort(Comparator.comparingDouble(client.player::distanceToSqr));
        int visible = Math.min(nearby.size(), 5);
        if (visible == 0) return;

        int w = 188;
        int h = 28 + visible * 17;
        int x = g.guiWidth() - w - 14;
        int y = 14;

        SmoothShapeRenderer.glow(g, x, y, w, h, 10, (0x58 << 24) | (ThemeConfig.accent & 0x00FFFFFF), 4);
        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 10, 0xE80D0912);

        SmoothTextRenderer.draw(g, "PlayerESP", x + 10, y + 8, 9.8f, 0xFFF6F1F7, true);

        for (int i = 0; i < visible; i++) {
            AbstractClientPlayer player = nearby.get(i);
            int rowY = y + 27 + i * 17;
            int distance = (int)Math.round(client.player.distanceTo(player));

            SmoothTextRenderer.draw(g, player.getName().getString(), x + 10, rowY, 8.4f, 0xFFD9D0DD, false);
            String dist = distance + "m";
            int tw = SmoothTextRenderer.width(dist, 8.2f, true);
            SmoothTextRenderer.draw(g, dist, x + w - 10 - tw, rowY, 8.2f, ThemeConfig.accent, true);
        }
    }

    private static void renderLineBox(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float red, float green, float blue, float alpha,
            float lineWidth
    ) {
        line(pose, consumer, x1, y1, z1, x2, y1, z1, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x2, y1, z1, x2, y1, z2, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x2, y1, z2, x1, y1, z2, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x1, y1, z2, x1, y1, z1, red, green, blue, alpha, lineWidth);

        line(pose, consumer, x1, y2, z1, x2, y2, z1, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x2, y2, z1, x2, y2, z2, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x2, y2, z2, x1, y2, z2, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x1, y2, z2, x1, y2, z1, red, green, blue, alpha, lineWidth);

        line(pose, consumer, x1, y1, z1, x1, y2, z1, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x2, y1, z1, x2, y2, z1, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x2, y1, z2, x2, y2, z2, red, green, blue, alpha, lineWidth);
        line(pose, consumer, x1, y1, z2, x1, y2, z2, red, green, blue, alpha, lineWidth);
    }

    private static void line(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float red, float green, float blue, float alpha,
            float lineWidth
    ) {
        float dx = (float)(x2 - x1);
        float dy = (float)(y2 - y1);
        float dz = (float)(z2 - z1);
        float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len <= 0.0001f) return;

        float nx = dx / len;
        float ny = dy / len;
        float nz = dz / len;

        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(lineWidth);

        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(lineWidth);
    }

    private PlayerEsp() {}
}
