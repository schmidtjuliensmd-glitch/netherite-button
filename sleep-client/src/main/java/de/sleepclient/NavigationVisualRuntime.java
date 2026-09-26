package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NavigationVisualRuntime {
    public static void renderHud(GuiGraphics g) {
        if (!LicenseManager.verified()) return;

        Minecraft client=Minecraft.getInstance();
        if (client.player==null || client.level==null) return;

        if (enabled("Radar")) renderRadar(g,client,false);
        if (enabled("MiniMap")) renderRadar(g,client,true);
        if (enabled("Waypoints")) renderWaypointList(g,client);
    }

    public static void renderWorld(WorldRenderContext context) {
        if (!LicenseManager.verified() || !enabled("Waypoints")) return;

        Minecraft client=Minecraft.getInstance();
        if (client.player==null || client.level==null) return;

        String dim=client.level.dimension().identifier().toString();
        int maxDistance=ConfigManager.intOption("Waypoints","maxDistance",2048);
        boolean beam=ConfigManager.boolOption("Waypoints","beam",true);

        for (WaypointManager.Waypoint wp:WaypointManager.all()) {
            if (!wp.dimension().equals(dim)) continue;
            double dx=client.player.getX()-wp.x();
            double dz=client.player.getZ()-wp.z();
            if (dx*dx+dz*dz>maxDistance*(double)maxDistance) continue;

            AABB box=new AABB(wp.x()-0.35,wp.y(),wp.z()-0.35,wp.x()+0.35,wp.y()+1.2,wp.z()+0.35);
            WorldBoxRenderer.box(context,box,ThemeConfig.accent,0.98f,2.4f);

            if (beam) {
                WorldBoxRenderer.line(
                        context,
                        new Vec3(wp.x(),wp.y(),wp.z()),
                        new Vec3(wp.x(),wp.y()+64.0,wp.z()),
                        ThemeConfig.accentSecondary,
                        0.65f,
                        1.8f
                );
            }
        }
    }

    private static void renderRadar(GuiGraphics g,Minecraft client,boolean minimap) {
        String module=minimap?"MiniMap":"Radar";
        int range=ConfigManager.intOption(module,"range",96);
        if (minimap) range=Math.max(32,ConfigManager.intOption("Radar","range",96));

        int size=minimap
                ? ConfigManager.intOption("MiniMap","size",160)
                : ConfigManager.intOption("Radar","size",150);
        size=Math.max(80,Math.min(220,size));

        int x=g.guiWidth()-size-14;
        int y=minimap?190:14;

        SmoothShapeRenderer.roundedRect(g,x,y,size,size,10,0xE60D0912);
        int cx=x+size/2;
        int cy=y+size/2;

        g.fill(cx-1,y+8,cx+1,y+size-8,0x332F2436);
        g.fill(x+8,cy-1,x+size-8,cy+1,0x332F2436);

        if (minimap) {
            int cell=Math.max(8,size/9);
            for (int gx=cx%cell;gx<x+size;gx+=cell) g.fill(gx,y+6,gx+1,y+size-6,0x221C1521);
            for (int gy=cy%cell;gy<y+size;gy+=cell) g.fill(x+6,gy,x+size-6,gy+1,0x221C1521);
        }

        double yaw=Math.toRadians(client.player.getYRot());
        boolean rotate=ConfigManager.boolOption("Radar","rotate",true);

        for (Entity entity:client.level.entitiesForRendering()) {
            if (entity==client.player || entity.isRemoved()) continue;
            boolean allowed=entity instanceof AbstractClientPlayer || entity instanceof Mob;
            if (!allowed) continue;

            double dx=entity.getX()-client.player.getX();
            double dz=entity.getZ()-client.player.getZ();
            if (dx*dx+dz*dz>range*(double)range) continue;

            if (rotate) {
                double rx=dx*Math.cos(-yaw)-dz*Math.sin(-yaw);
                double rz=dx*Math.sin(-yaw)+dz*Math.cos(-yaw);
                dx=rx;
                dz=rz;
            }

            int px=cx+(int)Math.round(dx/range*(size/2.0-10));
            int py=cy+(int)Math.round(dz/range*(size/2.0-10));
            int color=entity instanceof AbstractClientPlayer?0xFFFF5D9E:0xFFFFA65B;
            g.fill(px-2,py-2,px+3,py+3,color);
        }

        g.fill(cx-2,cy-2,cx+3,cy+3,0xFF55E6B1);
        SmoothTextRenderer.draw(g,minimap?"MiniMap":"Radar",x+9,y+8,7.8f,0xFFF3EEF5,true);
    }

    private static void renderWaypointList(GuiGraphics g,Minecraft client) {
        String dim=client.level.dimension().identifier().toString();
        int maxDistance=ConfigManager.intOption("Waypoints","maxDistance",2048);
        boolean showDistance=ConfigManager.boolOption("Waypoints","distance",true);

        List<WaypointDistance> list=new ArrayList<>();
        for (WaypointManager.Waypoint wp:WaypointManager.all()) {
            if (!wp.dimension().equals(dim)) continue;

            double dx=wp.x()-client.player.getX();
            double dy=wp.y()-client.player.getY();
            double dz=wp.z()-client.player.getZ();
            double distance=Math.sqrt(dx*dx+dy*dy+dz*dz);
            if (distance>maxDistance) continue;
            list.add(new WaypointDistance(wp,distance));
        }

        list.sort(Comparator.comparingDouble(WaypointDistance::distance));
        int visible=Math.min(5,list.size());
        if (visible==0) return;

        int w=185;
        int h=29+visible*16;
        int x=g.guiWidth()-w-14;
        int y=g.guiHeight()-h-14;

        SmoothShapeRenderer.roundedRect(g,x,y,w,h,9,0xE80D0912);
        SmoothTextRenderer.draw(g,"Waypoints",x+10,y+8,8.5f,0xFFF3EEF5,true);

        for (int i=0;i<visible;i++) {
            WaypointDistance entry=list.get(i);
            String left=entry.waypoint().name();
            String right=showDistance?Math.round(entry.distance())+"m":"";
            int rowY=y+27+i*16;
            SmoothTextRenderer.draw(g,left,x+10,rowY,7.5f,0xFFD9D0DD,false);
            if (!right.isEmpty()) {
                int rw=SmoothTextRenderer.width(right,7.3f,true);
                SmoothTextRenderer.draw(g,right,x+w-10-rw,rowY,7.3f,ThemeConfig.accent,true);
            }
        }
    }

    private static boolean enabled(String name) {
        Module m=ModuleRegistry.find(name);
        return m!=null && m.enabled();
    }

    private record WaypointDistance(WaypointManager.Waypoint waypoint,double distance) {}

    private NavigationVisualRuntime() {}
}
