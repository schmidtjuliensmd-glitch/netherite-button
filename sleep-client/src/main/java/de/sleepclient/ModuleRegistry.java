package de.sleepclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleRegistry {
    private static final List<Module> MODULES = new ArrayList<>();

    private static void add(ModuleCategory c, String n, String d) {
        MODULES.add(new Module(n, d, c));
    }

    public static void init() {
        if (!MODULES.isEmpty()) return;

        add(ModuleCategory.COMBAT,"Auto Crystal","Crystal placement and detonation controls.");
        add(ModuleCategory.COMBAT,"Anchor Macro","Respawn-anchor combat macro controls.");
        add(ModuleCategory.COMBAT,"Safe Anchor","Anchor safety checks.");
        add(ModuleCategory.COMBAT,"Auto Hit Crystal","Crystal hit timing controls.");
        add(ModuleCategory.COMBAT,"Crystal Optimizer","Crystal rendering and timing optimizer.");
        add(ModuleCategory.COMBAT,"Obsidian Optimizer","Fast obsidian placement workflow.");
        add(ModuleCategory.COMBAT,"AutoTrap","Obsidian trapping workflow.");
        add(ModuleCategory.COMBAT,"Aim Assist","Configurable target smoothing.");
        add(ModuleCategory.COMBAT,"Silent Aim","Target selection without camera snap.");
        add(ModuleCategory.COMBAT,"Trigger Bot","Crosshair target trigger.");
        add(ModuleCategory.COMBAT,"AutoClicker","Configurable click timing.");
        add(ModuleCategory.COMBAT,"Criticals","Critical hit timing.");
        add(ModuleCategory.COMBAT,"Reach","Attack range settings.");
        add(ModuleCategory.COMBAT,"Hit Boxes","Target hitbox settings.");
        add(ModuleCategory.COMBAT,"Auto Shield Disabler","Shield disable workflow.");
        add(ModuleCategory.COMBAT,"Auto Mace","Mace timing helper.");
        add(ModuleCategory.COMBAT,"Mace Swap","Swap to mace and back.");
        add(ModuleCategory.COMBAT,"Perfect Wind Charge","Wind-charge launch helper.");
        add(ModuleCategory.COMBAT,"Auto Totem","Totem off-hand helper.");
        add(ModuleCategory.COMBAT,"Auto Inventory Totem","Inventory totem refill helper.");
        add(ModuleCategory.COMBAT,"Auto Refill Hotbar","Hotbar refill helper.");
        add(ModuleCategory.COMBAT,"Auto XP","XP bottle helper.");
        add(ModuleCategory.COMBAT,"Key Pearl","One-key pearl workflow.");
        add(ModuleCategory.COMBAT,"Auto Aggro Pearl","Pearl reaction helper.");

        add(ModuleCategory.COMBAT,"KillAura","Configurable automatic target attacking.");
        add(ModuleCategory.COMBAT,"Crystal Aura","Automatic crystal combat workflow.");
        add(ModuleCategory.COMBAT,"Anchor Aura","Automatic respawn-anchor combat workflow.");
        add(ModuleCategory.COMBAT,"Bow Aim","Aim assistance for bows and crossbows.");
        add(ModuleCategory.COMBAT,"Bow Spam","Configurable rapid bow release workflow.");
        add(ModuleCategory.COMBAT,"Auto Armor","Automatically equips preferred armor.");
        add(ModuleCategory.COMBAT,"Auto Pot","Potion selection and throw helper.");
        add(ModuleCategory.COMBAT,"Auto Gap","Golden apple use helper.");
        add(ModuleCategory.COMBAT,"Auto Weapon","Automatically selects a preferred weapon.");
        add(ModuleCategory.COMBAT,"Target Selector","Central target priority settings.");
        add(ModuleCategory.COMBAT,"Anti Bot","Filters likely fake player entities.");
        add(ModuleCategory.COMBAT,"Teams","Avoids targeting configured teammates.");
        add(ModuleCategory.COMBAT,"Friends","Friend protection for combat modules.");
        add(ModuleCategory.COMBAT,"Backtrack","Target position history controls.");
        add(ModuleCategory.COMBAT,"Shield Breaker","Axe-based shield breaker controls.");
        add(ModuleCategory.COMBAT,"Hit Select","Configurable hit-selection timing.");
        add(ModuleCategory.COMBAT,"WTap","Sprint reset timing helper.");
        add(ModuleCategory.COMBAT,"STap","Backward sprint-reset timing helper.");
        add(ModuleCategory.COMBAT,"Auto Rod","Fishing-rod combat helper.");
        add(ModuleCategory.COMBAT,"Auto Bow Release","Automatically releases fully charged bows.");

        add(ModuleCategory.DONUT_SMP,"Auto Builder","Litematica-style schematic building workflow.");
        add(ModuleCategory.DONUT_SMP,"Auto Area Miner","Mine a selected area with configurable automation.");
        add(ModuleCategory.DONUT_SMP,"AutoMine","Straight-line mining workflow with lava avoidance.");
        add(ModuleCategory.DONUT_SMP,"BoneDropper","Bone-farm collection and drop workflow.");
        add(ModuleCategory.DONUT_SMP,"Auto Villager Trade","Configurable villager trading workflow.");
        add(ModuleCategory.DONUT_SMP,"Chunk Finder","Find loaded chunks containing amethyst clusters.");
        add(ModuleCategory.DONUT_SMP,"ChunkFinderV2","Extended loaded-chunk analysis.");
        add(ModuleCategory.DONUT_SMP,"Sus Chunk Finder","Suspicious loaded-chunk analysis.");
        add(ModuleCategory.DONUT_SMP,"SusChunkFinderV2","Extended suspicious-chunk analysis.");
        add(ModuleCategory.DONUT_SMP,"Netherite Finder","Highlights ancient debris in loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"Auto Elytra Finder","End exploration workflow for elytra searches.");
        add(ModuleCategory.DONUT_SMP,"Deep Player Finder","Highlights loaded players below a configured Y level.");
        add(ModuleCategory.DONUT_SMP,"TraderFinder","Highlights loaded wandering traders and trader llamas.");
        add(ModuleCategory.DONUT_SMP,"RegionMap","Region and chunk-grid HUD.");
        add(ModuleCategory.DONUT_SMP,"SeedAnalyzer","World-seed analysis tools.");

        add(ModuleCategory.DONUT_SMP,"Auto Schematic Builder","Alternative schematic building workflow.");
        add(ModuleCategory.DONUT_SMP,"BaseFinder","Loaded-chunk base clue scanner.");
        add(ModuleCategory.DONUT_SMP,"StashFinder","Storage concentration scanner for loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"SpawnerFinder","Highlights mob spawners in loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"PortalFinder","Highlights nether portals in loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"EndCityFinder","End exploration helper for loaded End terrain.");
        add(ModuleCategory.DONUT_SMP,"BastionFinder","Nether structure exploration helper.");
        add(ModuleCategory.DONUT_SMP,"FortressFinder","Nether fortress exploration helper.");
        add(ModuleCategory.DONUT_SMP,"TrialChamberFinder","Trial chamber exploration helper.");
        add(ModuleCategory.DONUT_SMP,"AncientCityFinder","Ancient city exploration helper.");
        add(ModuleCategory.DONUT_SMP,"OreScanner","Configurable loaded-block ore scanner.");
        add(ModuleCategory.DONUT_SMP,"ChestCounter","Counts storage in loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"ChunkActivity","Loaded-chunk activity indicator.");
        add(ModuleCategory.DONUT_SMP,"PortalTrace","Tracks recently observed portal positions.");
        add(ModuleCategory.DONUT_SMP,"Coordinate Logger","Saves useful discovered coordinates.");
        add(ModuleCategory.DONUT_SMP,"Death Logger","Stores recent death positions.");
        add(ModuleCategory.DONUT_SMP,"Bed Finder","Highlights beds in loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"Beacon Finder","Highlights beacons in loaded chunks.");
        add(ModuleCategory.DONUT_SMP,"Shulker Finder","Highlights shulkers and shulker boxes.");
        add(ModuleCategory.DONUT_SMP,"Minecart Finder","Highlights storage minecarts in loaded chunks.");

        add(ModuleCategory.VISUALS,"Show HUD","Draggable HUD widgets.");
        add(ModuleCategory.VISUALS,"Spotify HUD","Now-playing HUD widget.");
        add(ModuleCategory.VISUALS,"PlayerESP","Player highlighting.");
        add(ModuleCategory.VISUALS,"StorageESP","Storage highlighting.");
        add(ModuleCategory.VISUALS,"BlockESP","Selected block highlighting.");
        add(ModuleCategory.VISUALS,"Hole ESP","Hole highlighting.");
        add(ModuleCategory.VISUALS,"Block Overlay","Animated block overlay.");
        add(ModuleCategory.VISUALS,"Name Tags","Enhanced nametags.");
        add(ModuleCategory.VISUALS,"No Explosions","Explosion visual reduction.");
        add(ModuleCategory.VISUALS,"No Flame","First-person fire overlay control.");
        add(ModuleCategory.VISUALS,"LootESP","Dropped-item highlighting.");
        add(ModuleCategory.VISUALS,"Pearl Prediction","Pearl path preview.");
        add(ModuleCategory.VISUALS,"Pearl Catch","Incoming pearl alert.");
        add(ModuleCategory.VISUALS,"Heat Color","Health based entity color.");
        add(ModuleCategory.VISUALS,"Custom Flame","Custom fire overlay.");
        add(ModuleCategory.VISUALS,"CustomWorld","Sky, time, weather and fog controls.");
        add(ModuleCategory.VISUALS,"FireFly","Ambient firefly effect.");
        add(ModuleCategory.VISUALS,"Target ESP","Current target marker.");
        add(ModuleCategory.VISUALS,"Waypoints","Saved world positions.");
        add(ModuleCategory.VISUALS,"Block Highlight","Selected block highlight.");
        add(ModuleCategory.VISUALS,"ShaderFog","Custom fog effect.");
        add(ModuleCategory.VISUALS,"HandShader","Held item glow.");
        add(ModuleCategory.VISUALS,"Swing Animation","Custom swing styles.");
        add(ModuleCategory.VISUALS,"Sword Inspect","Weapon inspect animation.");
        add(ModuleCategory.VISUALS,"Freelook","Free camera look.");
        add(ModuleCategory.VISUALS,"Totem Counter","Totem HUD counter.");
        add(ModuleCategory.VISUALS,"Particles","Custom particles.");
        add(ModuleCategory.VISUALS,"Jump Circles","Jump ring effect.");
        add(ModuleCategory.VISUALS,"Fullbright","Maximum client-side brightness.");

        add(ModuleCategory.VISUALS,"EntityESP","General entity highlighting.");
        add(ModuleCategory.VISUALS,"MobESP","Hostile and passive mob highlighting.");
        add(ModuleCategory.VISUALS,"Tracers","Lines from your view to selected entities.");
        add(ModuleCategory.VISUALS,"Chams","Colored entity rendering controls.");
        add(ModuleCategory.VISUALS,"SkeletonESP","Skeleton-style player overlay.");
        add(ModuleCategory.VISUALS,"Health Bars","Entity health bar overlay.");
        add(ModuleCategory.VISUALS,"Armor HUD","Armor durability HUD.");
        add(ModuleCategory.VISUALS,"Potion HUD","Active potion effect HUD.");
        add(ModuleCategory.VISUALS,"Keystrokes","Movement key HUD.");
        add(ModuleCategory.VISUALS,"CPS Counter","Clicks-per-second HUD.");
        add(ModuleCategory.VISUALS,"FPS Counter","FPS HUD.");
        add(ModuleCategory.VISUALS,"Coordinates HUD","Coordinates HUD.");
        add(ModuleCategory.VISUALS,"Direction HUD","Facing direction HUD.");
        add(ModuleCategory.VISUALS,"Radar","Nearby entity radar.");
        add(ModuleCategory.VISUALS,"MiniMap","Compact local map HUD.");
        add(ModuleCategory.VISUALS,"Crosshair","Custom crosshair editor.");
        add(ModuleCategory.VISUALS,"Zoom","Configurable client-side zoom.");
        add(ModuleCategory.VISUALS,"Freecam","Detached local camera controls.");
        add(ModuleCategory.VISUALS,"Breadcrumbs","Movement trail rendering.");
        add(ModuleCategory.VISUALS,"Damage Numbers","Floating damage number display.");
        add(ModuleCategory.VISUALS,"Hit Color","Entity hit color overlay.");
        add(ModuleCategory.VISUALS,"Motion Blur","Client-side motion blur controls.");
        add(ModuleCategory.VISUALS,"Camera Clip","Third-person camera collision controls.");
        add(ModuleCategory.VISUALS,"No Hurt Cam","Disables hurt camera shake.");
        add(ModuleCategory.VISUALS,"No Weather","Hides weather visuals.");
        add(ModuleCategory.VISUALS,"No Fog","Reduces or disables fog.");
        add(ModuleCategory.VISUALS,"Time Changer","Client-side time override.");
        add(ModuleCategory.VISUALS,"FOV Changer","Client-side FOV controls.");
        add(ModuleCategory.VISUALS,"Item Physics","Dropped-item visual rotation controls.");
        add(ModuleCategory.VISUALS,"China Hat","Cosmetic player hat renderer.");
        add(ModuleCategory.VISUALS,"Cape","Custom local cape renderer.");
        add(ModuleCategory.VISUALS,"Trail","Player trail cosmetic.");
        add(ModuleCategory.VISUALS,"ESP Glow","Glow-style selected entity overlay.");
        add(ModuleCategory.VISUALS,"ProjectileESP","Highlights arrows, pearls and projectiles.");
        add(ModuleCategory.VISUALS,"SpawnerESP","Dedicated mob-spawner highlighting.");
        add(ModuleCategory.VISUALS,"PortalESP","Dedicated portal highlighting.");
        add(ModuleCategory.VISUALS,"BedESP","Dedicated bed highlighting.");
        add(ModuleCategory.VISUALS,"BeaconESP","Dedicated beacon highlighting.");

        add(ModuleCategory.MOVEMENT,"Sprint","Automatic sprint controls.");
        add(ModuleCategory.MOVEMENT,"No Slow","Movement slowdown controls.");
        add(ModuleCategory.MOVEMENT,"Step","Step-height controls.");
        add(ModuleCategory.MOVEMENT,"Velocity","Knockback controls.");

        add(ModuleCategory.MOVEMENT,"Speed","Configurable movement speed helper.");
        add(ModuleCategory.MOVEMENT,"Flight","Client movement flight controls.");
        add(ModuleCategory.MOVEMENT,"Elytra Fly","Elytra flight controls.");
        add(ModuleCategory.MOVEMENT,"Elytra Boost","Elytra boost helper.");
        add(ModuleCategory.MOVEMENT,"Long Jump","Long-jump movement controls.");
        add(ModuleCategory.MOVEMENT,"High Jump","Higher jump controls.");
        add(ModuleCategory.MOVEMENT,"Fast Fall","Faster downward movement.");
        add(ModuleCategory.MOVEMENT,"No Fall","Fall handling controls.");
        add(ModuleCategory.MOVEMENT,"Safe Walk","Prevents walking off block edges.");
        add(ModuleCategory.MOVEMENT,"Parkour","Automatic edge jump helper.");
        add(ModuleCategory.MOVEMENT,"Spider","Wall-climb movement helper.");
        add(ModuleCategory.MOVEMENT,"Jesus","Water-surface movement controls.");
        add(ModuleCategory.MOVEMENT,"Inventory Move","Movement while inventory screens are open.");
        add(ModuleCategory.MOVEMENT,"Sneak","Configurable automatic sneaking.");
        add(ModuleCategory.MOVEMENT,"Anti Void","Void fall protection helper.");
        add(ModuleCategory.MOVEMENT,"Auto Walk","Automatic forward walking.");
        add(ModuleCategory.MOVEMENT,"Bunny Hop","Repeated jump movement helper.");
        add(ModuleCategory.MOVEMENT,"Strafe","Air and ground strafing controls.");
        add(ModuleCategory.MOVEMENT,"Timer","Client movement timing controls.");

        add(ModuleCategory.MISC,"Keybind Manager","Central module keybind editor.");
        add(ModuleCategory.MISC,"Profiles","Save and switch configurations.");
        add(ModuleCategory.MISC,"Update Center","Sleep Client update information.");
        add(ModuleCategory.MISC,"Notifications","Client notification controls.");

        add(ModuleCategory.MISC,"Auto Fish","Automatic fishing helper.");
        add(ModuleCategory.MISC,"Auto Eat","Automatic food use helper.");
        add(ModuleCategory.MISC,"Auto Tool","Selects the preferred tool for a block.");
        add(ModuleCategory.MISC,"Auto Respawn","Automatically respawns after death.");
        add(ModuleCategory.MISC,"Auto Reconnect","Reconnect workflow after disconnect.");
        add(ModuleCategory.MISC,"Anti AFK","Configurable anti-idle movement.");
        add(ModuleCategory.MISC,"Inventory Cleaner","Inventory cleanup rules.");
        add(ModuleCategory.MISC,"Chest Stealer","Configurable container transfer helper.");
        add(ModuleCategory.MISC,"Fast Place","Placement delay controls.");
        add(ModuleCategory.MISC,"Fast Use","Item use timing controls.");
        add(ModuleCategory.MISC,"Auto Drop","Automatic item drop rules.");
        add(ModuleCategory.MISC,"Auto Craft","Recipe crafting helper.");
        add(ModuleCategory.MISC,"Middle Click Friend","Middle-click friend toggling.");
        add(ModuleCategory.MISC,"Chat Timestamp","Adds timestamps to chat.");
        add(ModuleCategory.MISC,"Chat Filter","Filters configured chat phrases.");
        add(ModuleCategory.MISC,"Auto Reply","Configurable chat auto replies.");
        add(ModuleCategory.MISC,"Announcer","Local event announcement settings.");
        add(ModuleCategory.MISC,"Screenshot Helper","Screenshot naming and notification tools.");
        add(ModuleCategory.MISC,"Session Info","Session time and stats HUD data.");
        add(ModuleCategory.MISC,"Auto GG","Configurable post-event chat message.");
        add(ModuleCategory.MISC,"Durability Alert","Warns about low durability.");
        add(ModuleCategory.MISC,"Low Health Alert","Warns when your health is low.");
        add(ModuleCategory.MISC,"Pearl Cooldown","Tracks pearl cooldown visually.");
        add(ModuleCategory.MISC,"Totem Pop Counter","Counts observed totem pops.");
        add(ModuleCategory.MISC,"Auto Leave","Disconnect rules based on configured conditions.");

        add(ModuleCategory.GUI,"Theme Editor","Accent and panel color controls.");
        add(ModuleCategory.GUI,"HUD Editor","Move and scale HUD widgets.");
        add(ModuleCategory.GUI,"Animation Settings","GUI animation speed.");
        add(ModuleCategory.GUI,"GUI Scale","ClickGUI scaling.");
        add(ModuleCategory.GUI,"Watermark","Sleep Client watermark controls.");
        add(ModuleCategory.GUI,"Module List","Active module list styling.");
        add(ModuleCategory.GUI,"Font Settings","GUI font size and weight controls.");
        add(ModuleCategory.GUI,"Blur Settings","Background blur strength controls.");
        add(ModuleCategory.GUI,"Background Effects","Background glow and particle controls.");
        add(ModuleCategory.GUI,"Color Manager","Shared module color presets.");
        add(ModuleCategory.GUI,"Notification Style","Notification position and animation.");
        add(ModuleCategory.GUI,"Search Settings","ClickGUI module search behavior.");
        add(ModuleCategory.GUI,"Compact Mode","Compact ClickGUI layout option.");
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> byCategory(ModuleCategory category) {
        return MODULES.stream().filter(m -> m.category() == category).toList();
    }

    public static Module find(String name) {
        return MODULES.stream()
                .filter(m -> m.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private ModuleRegistry() {}
}
