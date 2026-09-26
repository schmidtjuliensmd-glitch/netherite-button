package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LoadedFinderRuntime {
    private static final List<BlockPos> ORES = new ArrayList<>();
    private static final List<BlockPos> PORTALS = new ArrayList<>();
    private static final List<BlockPos> BEDS = new ArrayList<>();
    private static final List<BlockPos> SPAWNERS = new ArrayList<>();
    private static final List<BlockPos> BEACONS = new ArrayList<>();
    private static final Set<BlockPos> PORTAL_HISTORY = new LinkedHashSet<>();

    private static int tickCounter;
    private static int scanCursor;
    private static int lastCenterX = Integer.MIN_VALUE;
    private static int lastCenterZ = Integer.MIN_VALUE;

    public static void tick(Minecraft client) {
        if (!LicenseManager.verified() || client.player == null || client.level == null) {
            clearCurrent();
            return;
        }

        boolean needed = enabled("OreScanner")
                || enabled("PortalFinder") || enabled("PortalESP") || enabled("PortalTrace")
                || enabled("Bed Finder") || enabled("BedESP")
                || enabled("SpawnerESP") || enabled("BeaconESP");

        if (!needed) {
            clearCurrent();
            return;
        }

        if (++tickCounter < 8) return;
        tickCounter = 0;

        int radius = 4;
        radius = Math.max(radius, ConfigManager.intOption("PortalFinder","radius",6));
        radius = Math.max(radius, ConfigManager.intOption("Bed Finder","radius",6));
        radius = Math.max(radius, ConfigManager.intOption("SpawnerESP","range",96) / 16 + 1);
        radius = Math.min(12, radius);

        int pcx = client.player.blockPosition().getX() >> 4;
        int pcz = client.player.blockPosition().getZ() >> 4;

        if (pcx != lastCenterX || pcz != lastCenterZ) {
            lastCenterX = pcx;
            lastCenterZ = pcz;
            scanCursor = 0;
        }

        int diameter = radius * 2 + 1;
        int total = diameter * diameter;
        int index = Math.floorMod(scanCursor++, total);
        int cx = pcx + (index % diameter - radius);
        int cz = pcz + (index / diameter - radius);

        if (!client.level.hasChunk(cx, cz)) return;

        removeChunk(cx, cz);
        scanChunk(client, cx, cz);
    }

    private static void scanChunk(Minecraft client, int cx, int cz) {
        LevelChunk chunk = client.level.getChunk(cx, cz);

        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be.getType() == BlockEntityType.MOB_SPAWNER) SPAWNERS.add(be.getBlockPos().immutable());
            if (be.getType() == BlockEntityType.BEACON) BEACONS.add(be.getBlockPos().immutable());
        }

        boolean scanOre = enabled("OreScanner");
        boolean scanPortal = enabled("PortalFinder") || enabled("PortalESP") || enabled("PortalTrace");
        boolean scanBeds = enabled("Bed Finder") || enabled("BedESP");

        if (!scanOre && !scanPortal && !scanBeds) return;

        String ore = ConfigManager.stringOption("OreScanner","ore","Diamond");
        int range = ConfigManager.intOption("OreScanner","range",32);
        int maxResults = ConfigManager.intOption("OreScanner","maxResults",192);

        int startX = cx << 4;
        int startZ = cz << 4;
        int minY = client.level.getMinY();
        int maxY = Math.min(client.level.getMaxY(), 320);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int oreCount = ORES.size();

        for (int y=minY; y<maxY; y++) {
            for (int x=startX; x<startX+16; x++) {
                for (int z=startZ; z<startZ+16; z++) {
                    pos.set(x,y,z);
                    Block block = client.level.getBlockState(pos).getBlock();

                    if (scanPortal && block == Blocks.NETHER_PORTAL) {
                        BlockPos found=pos.immutable();
                        PORTALS.add(found);
                        if (PORTAL_HISTORY.size() < 256) PORTAL_HISTORY.add(found);
                    }

                    if (scanBeds && block instanceof BedBlock) BEDS.add(pos.immutable());

                    if (scanOre && oreCount < maxResults && matchesOre(block,ore)) {
                        if (client.player.distanceToSqr(x+0.5,y+0.5,z+0.5) <= range*(double)range) {
                            ORES.add(pos.immutable());
                            oreCount++;
                        }
                    }
                }
            }
        }
    }

    private static boolean matchesOre(Block block,String ore) {
        return switch (ore) {
            case "Ancient Debris" -> block == Blocks.ANCIENT_DEBRIS;
            case "Emerald" -> block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE;
            case "Gold" -> block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE;
            case "Iron" -> block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE;
            default -> block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE;
        };
    }

    private static void removeChunk(int cx,int cz) {
        ORES.removeIf(p -> (p.getX() >> 4)==cx && (p.getZ() >> 4)==cz);
        PORTALS.removeIf(p -> (p.getX() >> 4)==cx && (p.getZ() >> 4)==cz);
        BEDS.removeIf(p -> (p.getX() >> 4)==cx && (p.getZ() >> 4)==cz);
        SPAWNERS.removeIf(p -> (p.getX() >> 4)==cx && (p.getZ() >> 4)==cz);
        BEACONS.removeIf(p -> (p.getX() >> 4)==cx && (p.getZ() >> 4)==cz);
    }

    public static void renderWorld(WorldRenderContext context) {
        if (!LicenseManager.verified()) return;

        if (enabled("OreScanner")) renderBlocks(context,ORES,0xFF55E6B1,
                ConfigManager.floatOption("BlockESP","lineWidth",2.0f),
                ConfigManager.intOption("OreScanner","maxResults",192));

        if (enabled("PortalFinder") || enabled("PortalESP") || enabled("PortalTrace")) {
            renderBlocks(context,PORTALS,0xFFA16CFF,
                    ConfigManager.floatOption("PortalFinder","lineWidth",2.2f),
                    ConfigManager.intOption("PortalFinder","maxResults",96));
        }

        if (enabled("Bed Finder") || enabled("BedESP")) {
            renderBlocks(context,BEDS,0xFFFF6F91,
                    ConfigManager.floatOption("Bed Finder","lineWidth",2.0f),
                    ConfigManager.intOption("Bed Finder","maxResults",96));
        }

        if (enabled("SpawnerESP")) {
            renderBlocks(context,SPAWNERS,0xFFFFC857,
                    ConfigManager.floatOption("SpawnerESP","lineWidth",2.4f),
                    ConfigManager.intOption("SpawnerESP","maxResults",96));
        }

        if (enabled("BeaconESP")) {
            renderBlocks(context,BEACONS,0xFF55E6B1,
                    ConfigManager.floatOption("BeaconESP","lineWidth",2.4f),
                    ConfigManager.intOption("BeaconESP","maxResults",96));
        }

        if (enabled("Minecart Finder")) renderMinecarts(context);
    }

    private static void renderBlocks(WorldRenderContext context,List<BlockPos> positions,int color,float width,int max) {
        int shown=0;
        for (BlockPos p:positions) {
            WorldBoxRenderer.box(context,
                    new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1),
                    color,0.97f,width);
            if (++shown>=max) break;
        }
    }

    private static void renderMinecarts(WorldRenderContext context) {
        Minecraft client=Minecraft.getInstance();
        if (client.level==null || client.player==null) return;

        int range=ConfigManager.intOption("Minecart Finder","radius",6)*16;
        float width=ConfigManager.floatOption("Minecart Finder","lineWidth",2.2f);
        int max=ConfigManager.intOption("Minecart Finder","maxResults",96);
        int shown=0;

        for (Entity entity:client.level.entitiesForRendering()) {
            if (entity.getType()!=EntityType.CHEST_MINECART && entity.getType()!=EntityType.HOPPER_MINECART) continue;
            if (client.player.distanceToSqr(entity)>range*(double)range) continue;

            WorldBoxRenderer.box(context,entity.getBoundingBox().inflate(0.08),0xFF55B8FF,0.98f,width);
            if (++shown>=max) break;
        }
    }

    public static void renderHud(GuiGraphics g) {
        if (!LicenseManager.verified()) return;

        List<String> rows=new ArrayList<>();
        if (enabled("OreScanner")) rows.add("OreScanner  "+ORES.size());
        if (enabled("PortalFinder")) rows.add("Portals  "+PORTALS.size());
        if (enabled("Bed Finder")) rows.add("Beds  "+BEDS.size());
        if (enabled("SpawnerESP")) rows.add("Spawners  "+SPAWNERS.size());
        if (enabled("BeaconESP")) rows.add("Beacons  "+BEACONS.size());
        if (enabled("PortalTrace")) rows.add("Portal history  "+PORTAL_HISTORY.size());

        if (rows.isEmpty()) return;

        int w=170;
        int h=27+rows.size()*16;
        int x=14;
        int y=430;

        SmoothShapeRenderer.roundedRect(g,x,y,w,h,9,0xE80D0912);
        SmoothTextRenderer.draw(g,"Loaded Finders",x+10,y+8,8.5f,0xFFF1ECF4,true);
        for (int i=0;i<rows.size();i++) {
            SmoothTextRenderer.draw(g,rows.get(i),x+10,y+26+i*16,7.5f,0xFFD4CBD8,false);
        }
    }

    public static List<BlockPos> portalHistory() {
        return PORTAL_HISTORY.stream().sorted(Comparator.comparingInt(BlockPos::getY)).toList();
    }

    private static boolean enabled(String name) {
        Module m=ModuleRegistry.find(name);
        return m!=null && m.enabled();
    }

    private static void clearCurrent() {
        ORES.clear();
        PORTALS.clear();
        BEDS.clear();
        SPAWNERS.clear();
        BEACONS.clear();
    }

    private LoadedFinderRuntime() {}
}
