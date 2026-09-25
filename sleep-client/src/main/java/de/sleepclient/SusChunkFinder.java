package de.sleepclient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SusChunkFinder {
    private static final List<Result> RESULTS = new ArrayList<>();

    private static int tickCounter;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int SCAN_RADIUS_CHUNKS = 7;
    private static final int MIN_SCORE = 8;

    public static void tick(Minecraft client) {
        Module module = ModuleRegistry.find("Sus Chunk Finder");
        if (module == null || !module.enabled() || !LicenseManager.verified() || client.level == null || client.player == null) {
            RESULTS.clear();
            return;
        }

        tickCounter++;
        if (tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;

        scan(client);
    }

    private static void scan(Minecraft client) {
        int playerChunkX = client.player.blockPosition().getX() >> 4;
        int playerChunkZ = client.player.blockPosition().getZ() >> 4;

        List<Result> found = new ArrayList<>();

        for (int cx = playerChunkX - SCAN_RADIUS_CHUNKS; cx <= playerChunkX + SCAN_RADIUS_CHUNKS; cx++) {
            for (int cz = playerChunkZ - SCAN_RADIUS_CHUNKS; cz <= playerChunkZ + SCAN_RADIUS_CHUNKS; cz++) {
                if (!client.level.hasChunk(cx, cz)) continue;

                LevelChunk chunk = client.level.getChunk(cx, cz);
                int score = 0;
                int storage = 0;
                int machines = 0;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockEntityType<?> type = blockEntity.getType();

                    if (type == BlockEntityType.CHEST || type == BlockEntityType.TRAPPED_CHEST) {
                        score += 3;
                        storage++;
                    } else if (type == BlockEntityType.BARREL) {
                        score += 3;
                        storage++;
                    } else if (type == BlockEntityType.SHULKER_BOX) {
                        score += 5;
                        storage++;
                    } else if (type == BlockEntityType.HOPPER) {
                        score += 4;
                        machines++;
                    } else if (type == BlockEntityType.FURNACE
                            || type == BlockEntityType.BLAST_FURNACE
                            || type == BlockEntityType.SMOKER) {
                        score += 2;
                        machines++;
                    } else if (type == BlockEntityType.ENDER_CHEST) {
                        score += 3;
                        storage++;
                    } else if (type == BlockEntityType.BREWING_STAND) {
                        score += 2;
                        machines++;
                    } else if (type == BlockEntityType.BEACON) {
                        score += 6;
                        machines++;
                    } else {
                        score += 1;
                    }
                }

                if (score >= MIN_SCORE || storage >= 3 || machines >= 4) {
                    int dx = cx - playerChunkX;
                    int dz = cz - playerChunkZ;
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    found.add(new Result(cx, cz, score, storage, machines, distance));
                }
            }
        }

        found.sort(Comparator
                .comparingInt(Result::score).reversed()
                .thenComparingDouble(Result::distance));

        RESULTS.clear();
        RESULTS.addAll(found);
    }

    public static void renderHud(GuiGraphics g) {
        Module module = ModuleRegistry.find("Sus Chunk Finder");
        if (module == null || !module.enabled() || !LicenseManager.verified()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int x = 14;
        int y = 14;
        int visible = Math.min(RESULTS.size(), 5);
        int h = 43 + visible * 18;

        SmoothShapeRenderer.glow(g, x, y, 205, h, 11, 0x66FF4F9F, 5);
        SmoothShapeRenderer.roundedRect(g, x, y, 205, h, 11, 0xDF0D0912);
        SmoothShapeRenderer.roundedRect(g, x + 1, y + 1, 203, h - 2, 10, 0xEF120B18);

        SmoothTextRenderer.draw(g, "Sus Chunk Finder", x + 12, y + 9, 10.8f, 0xFFF5F1F7, true);
        SmoothTextRenderer.draw(
                g,
                RESULTS.isEmpty() ? "Keine auffälligen Chunks" : RESULTS.size() + " verdächtige Chunks",
                x + 12,
                y + 25,
                8.2f,
                RESULTS.isEmpty() ? 0xFF8F8494 : 0xFFFF63B0,
                false
        );

        for (int i = 0; i < visible; i++) {
            Result result = RESULTS.get(i);
            int rowY = y + 43 + i * 18;
            String left = "Chunk " + result.chunkX() + ", " + result.chunkZ();
            String right = "Score " + result.score();

            SmoothTextRenderer.draw(g, left, x + 12, rowY, 8.4f, 0xFFD7CEDA, false);
            int rw = SmoothTextRenderer.width(right, 8.2f, true);
            SmoothTextRenderer.draw(g, right, x + 192 - rw, rowY, 8.2f, 0xFFFF63B0, true);
        }
    }

    public static void renderWorld(WorldRenderContext context) {
        Module module = ModuleRegistry.find("Sus Chunk Finder");
        if (module == null || !module.enabled() || !LicenseManager.verified() || RESULTS.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        Vec3 cam = client.gameRenderer.getMainCamera().position();
        PoseStack matrices = context.matrices();
        VertexConsumer lines = context.consumers().getBuffer(RenderTypes.linesTranslucent());

        double baseY = client.player.getY() - 1.0;

        for (Result result : RESULTS) {
            double x1 = result.chunkX() * 16.0 - cam.x;
            double z1 = result.chunkZ() * 16.0 - cam.z;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;
            double y1 = baseY - cam.y;
            double y2 = y1 + 0.12;

            float intensity = Math.min(1.0f, result.score() / 24.0f);
            float red = 1.0f;
            float green = 0.20f + 0.18f * (1.0f - intensity);
            float blue = 0.62f + 0.20f * intensity;

            ShapeRenderer.renderLineBox(
                    matrices.last(),
                    lines,
                    x1, y1, z1,
                    x2, y2, z2,
                    red, green, blue, 0.95f
            );
        }
    }

    public static List<Result> results() {
        return List.copyOf(RESULTS);
    }

    public record Result(
            int chunkX,
            int chunkZ,
            int score,
            int storageCount,
            int machineCount,
            double distance
    ) {}

    private SusChunkFinder() {}
}
