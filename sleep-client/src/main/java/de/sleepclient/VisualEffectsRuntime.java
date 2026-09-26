package de.sleepclient;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class VisualEffectsRuntime {
    private static final List<Vec3> TRAIL = new ArrayList<>();
    private static final List<Ring> RINGS = new ArrayList<>();

    private static boolean wasOnGround = true;
    private static boolean lastAttack;
    private static int sampleTicks;
    private static int particleTicks;
    private static int pearlWarningTicks;
    private static String pearlWarning = "";

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            TRAIL.clear();
            RINGS.clear();
            pearlWarning = "";
            return;
        }

        tickTrail(client);
        tickJumpCircles(client);
        tickParticles(client);
        tickFireFly(client);
        tickPearlWarning(client);

        if (pearlWarningTicks > 0) pearlWarningTicks--;
        else pearlWarning = "";
    }

    private static void tickTrail(Minecraft client) {
        if (!enabled("Breadcrumbs") && !enabled("Trail")) {
            TRAIL.clear();
            sampleTicks = 0;
            return;
        }

        if (++sampleTicks < 3) return;
        sampleTicks = 0;

        Vec3 pos = client.player.position().add(0.0, 0.08, 0.0);
        if (TRAIL.isEmpty() || TRAIL.get(TRAIL.size()-1).distanceToSqr(pos) > 0.04) TRAIL.add(pos);

        int max = Math.max(
                ConfigManager.intOption("Breadcrumbs","length",100),
                ConfigManager.intOption("Trail","length",100)
        );
        while (TRAIL.size() > max) TRAIL.remove(0);
    }

    private static void tickJumpCircles(Minecraft client) {
        boolean onGround = client.player.onGround();

        if (enabled("Jump Circles") && wasOnGround && !onGround && client.player.getDeltaMovement().y > 0.05) {
            RINGS.add(new Ring(client.player.position(), System.currentTimeMillis()));
        }
        wasOnGround = onGround;

        double seconds = ConfigManager.floatOption("Jump Circles","duration",1.0f);
        long maxAge = Math.max(200L, (long)(seconds * 1000.0));
        long now = System.currentTimeMillis();
        RINGS.removeIf(r -> now - r.createdAt() > maxAge);
    }

    private static void tickParticles(Minecraft client) {
        if (!enabled("Particles")) {
            lastAttack = client.options.keyAttack.isDown();
            return;
        }

        boolean attack = client.options.keyAttack.isDown();
        if (attack && !lastAttack && client.hitResult instanceof EntityHitResult hit) {
            int count = ConfigManager.intOption("Particles","count",12);
            Entity e = hit.getEntity();
            for (int i=0;i<count;i++) {
                double ox=(Math.random()-0.5)*e.getBbWidth();
                double oy=Math.random()*e.getBbHeight();
                double oz=(Math.random()-0.5)*e.getBbWidth();
                client.level.addParticle(
                        ParticleTypes.CRIT,
                        e.getX()+ox,
                        e.getY()+oy,
                        e.getZ()+oz,
                        (Math.random()-0.5)*0.15,
                        Math.random()*0.12,
                        (Math.random()-0.5)*0.15
                );
            }
        }
        lastAttack = attack;
    }

    private static void tickFireFly(Minecraft client) {
        if (!enabled("FireFly")) return;
        if (++particleTicks < 8) return;
        particleTicks = 0;

        int count = Math.min(8, Math.max(1, ConfigManager.intOption("FireFly","count",25) / 5));
        double t = System.currentTimeMillis() / 850.0;

        for (int i=0;i<count;i++) {
            double a=t+i*2.399;
            double radius=2.0+(i%3)*0.65;
            double x=client.player.getX()+Math.cos(a)*radius;
            double z=client.player.getZ()+Math.sin(a)*radius;
            double y=client.player.getY()+1.1+Math.sin(a*1.6)*0.8;
            client.level.addParticle(ParticleTypes.END_ROD,x,y,z,0.0,0.006,0.0);
        }
    }

    private static void tickPearlWarning(Minecraft client) {
        if (!enabled("Pearl Catch")) return;

        int warningRange = ConfigManager.intOption("Pearl Catch","warningRange",12);
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity.getType() != EntityType.ENDER_PEARL) continue;
            if (entity.distanceToSqr(client.player) > 160.0 * 160.0) continue;

            Vec3 pos=entity.position();
            Vec3 vel=entity.getDeltaMovement();

            for (int i=0;i<80;i++) {
                pos=pos.add(vel);
                vel=vel.scale(0.99).add(0.0,-0.03,0.0);

                if (pos.distanceToSqr(client.player.position()) <= warningRange*(double)warningRange) {
                    pearlWarning="Incoming pearl · "+Math.round(Math.sqrt(pos.distanceToSqr(client.player.position())))+"m";
                    if (pearlWarningTicks <= 0 && ConfigManager.boolOption("Pearl Catch","sound",true)) {
                        client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP,0.7f,1.65f);
                    }
                    pearlWarningTicks=30;
                    return;
                }
            }
        }
    }

    public static void renderWorld(WorldRenderContext context) {
        

        renderTrail(context);
        renderJumpCircles(context);
        renderPearls(context);
        renderEspGlow(context);
    }

    private static void renderTrail(WorldRenderContext context) {
        if ((!enabled("Breadcrumbs") && !enabled("Trail")) || TRAIL.size() < 2) return;

        String module=enabled("Trail")?"Trail":"Breadcrumbs";
        float width=ConfigManager.floatOption(module,"width",1.5f);

        for (int i=1;i<TRAIL.size();i++) {
            float alpha=ConfigManager.boolOption(module,"fade",true)
                    ? Math.max(0.12f,i/(float)TRAIL.size())
                    : 0.85f;
            WorldBoxRenderer.line(context,TRAIL.get(i-1),TRAIL.get(i),ThemeConfig.accent,alpha,width);
        }
    }

    private static void renderJumpCircles(WorldRenderContext context) {
        if (!enabled("Jump Circles")) return;

        double size=ConfigManager.floatOption("Jump Circles","size",1.6f);
        double duration=ConfigManager.floatOption("Jump Circles","duration",1.0f);
        long now=System.currentTimeMillis();

        for (Ring ring:RINGS) {
            double age=(now-ring.createdAt())/1000.0;
            float alpha=(float)Math.max(0.0,1.0-age/Math.max(0.2,duration));
            double radius=size*(0.55+0.45*Math.min(1.0,age/Math.max(0.2,duration)));
            Vec3 previous=null;

            for (int i=0;i<=28;i++) {
                double a=Math.PI*2*i/28.0;
                Vec3 p=ring.center().add(Math.cos(a)*radius,0.04,Math.sin(a)*radius);
                if (previous!=null) WorldBoxRenderer.line(context,previous,p,ThemeConfig.accent,alpha,2.0f);
                previous=p;
            }
        }
    }

    private static void renderPearls(WorldRenderContext context) {
        if (!enabled("Pearl Prediction")) return;

        Minecraft client=Minecraft.getInstance();
        if (client.level==null) return;

        int steps=ConfigManager.intOption("Pearl Prediction","steps",100);
        float width=ConfigManager.floatOption("Pearl Prediction","lineWidth",1.8f);

        for (Entity entity:client.level.entitiesForRendering()) {
            if (entity.getType()!=EntityType.ENDER_PEARL) continue;

            Vec3 pos=entity.position();
            Vec3 vel=entity.getDeltaMovement();

            for (int i=0;i<steps;i++) {
                Vec3 next=pos.add(vel);
                WorldBoxRenderer.line(context,pos,next,0xFF55B8FF,0.86f,width);
                pos=next;
                vel=vel.scale(0.99).add(0.0,-0.03,0.0);
                if (pos.y < client.level.getMinY()) break;
            }

            if (ConfigManager.boolOption("Pearl Prediction","landingMarker",true)) {
                WorldBoxRenderer.box(context,
                        new net.minecraft.world.phys.AABB(pos.x-0.2,pos.y-0.05,pos.z-0.2,pos.x+0.2,pos.y+0.35,pos.z+0.2),
                        0xFF55B8FF,0.9f,2.0f);
            }
        }
    }

    private static void renderEspGlow(WorldRenderContext context) {
        if (!enabled("ESP Glow")) return;

        Minecraft client=Minecraft.getInstance();
        if (client.level==null || client.player==null) return;

        int range=ConfigManager.intOption("ESP Glow","range",96);
        float strength=ConfigManager.floatOption("ESP Glow","strength",0.8f);
        float outer=Math.max(2.2f,2.0f+strength*2.0f);

        for (Player player:client.level.players()) {
            if (player==client.player || player.isRemoved()) continue;
            if (client.player.distanceToSqr(player)>range*(double)range) continue;

            WorldBoxRenderer.box(context,player.getBoundingBox().inflate(0.12),ThemeConfig.accentSecondary,0.30f,outer);
            WorldBoxRenderer.box(context,player.getBoundingBox().inflate(0.06),ThemeConfig.accent,0.95f,1.8f);
        }
    }

    public static void renderHud(GuiGraphics g) {
        if (pearlWarning.isEmpty()) return;

        int tw=SmoothTextRenderer.width(pearlWarning,8.8f,true);
        int x=(g.guiWidth()-tw-28)/2;
        int y=126;

        SmoothShapeRenderer.glow(g,x,y,tw+28,32,9,0x5555B8FF,4);
        SmoothShapeRenderer.roundedRect(g,x,y,tw+28,32,9,0xEB0D0B15);
        SmoothTextRenderer.draw(g,pearlWarning,x+14,y+10,8.8f,0xFF80CCFF,true);
    }

    private static boolean enabled(String name) {
        Module m=ModuleRegistry.find(name);
        return m!=null && m.enabled();
    }

    private record Ring(Vec3 center,long createdAt) {}

    private VisualEffectsRuntime() {}
}
