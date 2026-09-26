package de.sleepclient;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class WaypointManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sleep-client-waypoints.properties");
    private static final List<Waypoint> WAYPOINTS = new ArrayList<>();

    public static synchronized void load() {
        WAYPOINTS.clear();
        if (!Files.exists(FILE)) return;

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException ignored) {
            return;
        }

        int count = parseInt(p.getProperty("count"),0);
        for (int i=0;i<count;i++) {
            String base="wp."+i+".";
            String name=p.getProperty(base+"name");
            String dimension=p.getProperty(base+"dimension");
            if (name==null || dimension==null) continue;

            double x=parseDouble(p.getProperty(base+"x"),0);
            double y=parseDouble(p.getProperty(base+"y"),0);
            double z=parseDouble(p.getProperty(base+"z"),0);
            WAYPOINTS.add(new Waypoint(name,dimension,x,y,z));
        }
    }

    public static synchronized void addCurrent(String name) {
        Minecraft client=Minecraft.getInstance();
        if (client.player==null || client.level==null || name==null || name.isBlank()) return;

        String dim=client.level.dimension().identifier().toString();
        WAYPOINTS.removeIf(w -> w.name().equalsIgnoreCase(name));
        WAYPOINTS.add(new Waypoint(name.trim(),dim,client.player.getX(),client.player.getY(),client.player.getZ()));
        save();
    }

    public static synchronized boolean remove(String name) {
        boolean changed=WAYPOINTS.removeIf(w -> w.name().equalsIgnoreCase(name));
        if (changed) save();
        return changed;
    }

    public static synchronized void clear() {
        WAYPOINTS.clear();
        save();
    }

    public static synchronized List<Waypoint> all() {
        return List.copyOf(WAYPOINTS);
    }

    private static synchronized void save() {
        Properties p=new Properties();
        p.setProperty("count",Integer.toString(WAYPOINTS.size()));

        for (int i=0;i<WAYPOINTS.size();i++) {
            Waypoint w=WAYPOINTS.get(i);
            String base="wp."+i+".";
            p.setProperty(base+"name",w.name());
            p.setProperty(base+"dimension",w.dimension());
            p.setProperty(base+"x",Double.toString(w.x()));
            p.setProperty(base+"y",Double.toString(w.y()));
            p.setProperty(base+"z",Double.toString(w.z()));
        }

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out=Files.newOutputStream(FILE)) {
                p.store(out,"Sleep Client waypoints");
            }
        } catch (IOException ignored) {}
    }

    private static int parseInt(String value,int fallback) {
        try { return value==null?fallback:Integer.parseInt(value); }
        catch (Exception ignored) { return fallback; }
    }

    private static double parseDouble(String value,double fallback) {
        try { return value==null?fallback:Double.parseDouble(value); }
        catch (Exception ignored) { return fallback; }
    }

    public record Waypoint(String name,String dimension,double x,double y,double z) {}

    private WaypointManager() {}
}
