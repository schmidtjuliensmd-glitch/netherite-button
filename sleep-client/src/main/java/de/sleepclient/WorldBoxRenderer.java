package de.sleepclient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WorldBoxRenderer {
    public static void box(WorldRenderContext context, AABB worldBox, int color, float alpha, float lineWidth) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        Vec3 cam = client.gameRenderer.getMainCamera().position();
        PoseStack.Pose pose = context.matrices().last();
        VertexConsumer lines = context.consumers().getBuffer(RenderTypes.linesTranslucent());

        double x1 = worldBox.minX - cam.x;
        double y1 = worldBox.minY - cam.y;
        double z1 = worldBox.minZ - cam.z;
        double x2 = worldBox.maxX - cam.x;
        double y2 = worldBox.maxY - cam.y;
        double z2 = worldBox.maxZ - cam.z;

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        line(pose, lines, x1,y1,z1, x2,y1,z1, r,g,b,alpha,lineWidth);
        line(pose, lines, x2,y1,z1, x2,y1,z2, r,g,b,alpha,lineWidth);
        line(pose, lines, x2,y1,z2, x1,y1,z2, r,g,b,alpha,lineWidth);
        line(pose, lines, x1,y1,z2, x1,y1,z1, r,g,b,alpha,lineWidth);

        line(pose, lines, x1,y2,z1, x2,y2,z1, r,g,b,alpha,lineWidth);
        line(pose, lines, x2,y2,z1, x2,y2,z2, r,g,b,alpha,lineWidth);
        line(pose, lines, x2,y2,z2, x1,y2,z2, r,g,b,alpha,lineWidth);
        line(pose, lines, x1,y2,z2, x1,y2,z1, r,g,b,alpha,lineWidth);

        line(pose, lines, x1,y1,z1, x1,y2,z1, r,g,b,alpha,lineWidth);
        line(pose, lines, x2,y1,z1, x2,y2,z1, r,g,b,alpha,lineWidth);
        line(pose, lines, x2,y1,z2, x2,y2,z2, r,g,b,alpha,lineWidth);
        line(pose, lines, x1,y1,z2, x1,y2,z2, r,g,b,alpha,lineWidth);
    }

    private static void line(
            PoseStack.Pose pose, VertexConsumer consumer,
            double x1,double y1,double z1,double x2,double y2,double z2,
            float r,float g,float b,float a,float width
    ) {
        float dx=(float)(x2-x1), dy=(float)(y2-y1), dz=(float)(z2-z1);
        float len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
        if (len <= 0.0001f) return;

        float nx=dx/len, ny=dy/len, nz=dz/len;

        consumer.addVertex(pose,(float)x1,(float)y1,(float)z1)
                .setColor(r,g,b,a)
                .setNormal(pose,nx,ny,nz)
                .setLineWidth(width);
        consumer.addVertex(pose,(float)x2,(float)y2,(float)z2)
                .setColor(r,g,b,a)
                .setNormal(pose,nx,ny,nz)
                .setLineWidth(width);
    }

    private WorldBoxRenderer() {}
}
