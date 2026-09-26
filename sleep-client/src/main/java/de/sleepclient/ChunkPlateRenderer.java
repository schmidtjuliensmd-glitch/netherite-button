package de.sleepclient;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** A flat, two-sided red chunk overlay. The world Y never follows the player. */
public final class ChunkPlateRenderer {
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("sleepclient", "pipeline/chunk_plate"))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build());
    private static final RenderType TYPE = RenderType.create("sleepclient:chunk_plate",
            RenderSetup.builder(PIPELINE).sortOnUpload().createRenderSetup());

    public static void draw(WorldRenderContext context, int chunkX, int chunkZ, int worldY) {
        if (context.consumers() == null || context.matrices() == null) return;
        Vec3 camera = context.worldState().cameraRenderState.pos;
        PoseStack.Pose pose = context.matrices().last();
        VertexConsumer buffer = context.consumers().getBuffer(TYPE);
        double x = AmethystRules.chunkStart(chunkX) - camera.x;
        double z = AmethystRules.chunkStart(chunkZ) - camera.z;
        double y = worldY - camera.y;
        quad(pose, buffer, x, y, z, x + 16, z + 16, .42f);
        // A narrow border makes adjacent chunk edges readable without drawing a tall box.
        double edge = .055;
        quad(pose, buffer, x, y, z, x + 16, z + edge, .88f);
        quad(pose, buffer, x, y, z + 16 - edge, x + 16, z + 16, .88f);
        quad(pose, buffer, x, y, z + edge, x + edge, z + 16 - edge, .88f);
        quad(pose, buffer, x + 16 - edge, y, z + edge, x + 16, z + 16 - edge, .88f);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer,
                             double x1, double y, double z1, double x2, double z2, float alpha) {
        buffer.addVertex(pose, (float) x1, (float) y, (float) z1).setColor(1f, 0f, 0f, alpha);
        buffer.addVertex(pose, (float) x1, (float) y, (float) z2).setColor(1f, 0f, 0f, alpha);
        buffer.addVertex(pose, (float) x2, (float) y, (float) z2).setColor(1f, 0f, 0f, alpha);
        buffer.addVertex(pose, (float) x2, (float) y, (float) z1).setColor(1f, 0f, 0f, alpha);
    }

    private ChunkPlateRenderer() {}
}
