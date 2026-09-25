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

        add(ModuleCategory.DONUT_SMP,"Sus Chunk Finder","Suspicious chunk analysis.");
        add(ModuleCategory.DONUT_SMP,"Chunk Finder","Chunk analysis tools.");
        add(ModuleCategory.DONUT_SMP,"Auto Schematic Builder","Schematic building workflow.");

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

        add(ModuleCategory.MOVEMENT,"Sprint","Automatic sprint controls.");
        add(ModuleCategory.MOVEMENT,"No Slow","Movement slowdown controls.");
        add(ModuleCategory.MOVEMENT,"Step","Step-height controls.");
        add(ModuleCategory.MOVEMENT,"Velocity","Knockback controls.");

        add(ModuleCategory.MISC,"Keybind Manager","Central module keybind editor.");
        add(ModuleCategory.MISC,"Profiles","Save and switch configurations.");
        add(ModuleCategory.MISC,"Update Center","Sleep Client update information.");
        add(ModuleCategory.MISC,"Notifications","Client notification controls.");

        add(ModuleCategory.GUI,"Theme Editor","Accent and panel color controls.");
        add(ModuleCategory.GUI,"HUD Editor","Move and scale HUD widgets.");
        add(ModuleCategory.GUI,"Animation Settings","GUI animation speed.");
        add(ModuleCategory.GUI,"GUI Scale","ClickGUI scaling.");
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
