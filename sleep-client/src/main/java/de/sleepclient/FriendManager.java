package de.sleepclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class FriendManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sleep-client-friends.properties");
    private static final Set<String> FRIENDS = new LinkedHashSet<>();

    public static synchronized void load() {
        FRIENDS.clear();
        if (!Files.exists(FILE)) return;

        Properties p=new Properties();
        try (InputStream in=Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException ignored) {
            return;
        }

        int count=parseInt(p.getProperty("count"),0);
        for (int i=0;i<count;i++) {
            String name=p.getProperty("friend."+i);
            if (name!=null && !name.isBlank()) FRIENDS.add(normalize(name));
        }
    }

    public static synchronized boolean isFriend(String name) {
        return name!=null && FRIENDS.contains(normalize(name));
    }

    public static synchronized boolean toggle(String name) {
        String n=normalize(name);
        boolean added;
        if (FRIENDS.contains(n)) {
            FRIENDS.remove(n);
            added=false;
        } else {
            FRIENDS.add(n);
            added=true;
        }
        save();
        return added;
    }

    public static synchronized Set<String> all() {
        return Set.copyOf(FRIENDS);
    }

    private static void save() {
        Properties p=new Properties();
        p.setProperty("count",Integer.toString(FRIENDS.size()));
        int i=0;
        for (String name:FRIENDS) p.setProperty("friend."+i++,name);

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out=Files.newOutputStream(FILE)) {
                p.store(out,"Sleep Client friends");
            }
        } catch (IOException ignored) {}
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static int parseInt(String value,int fallback) {
        try { return value==null?fallback:Integer.parseInt(value); }
        catch (Exception ignored) { return fallback; }
    }

    private FriendManager() {}
}
