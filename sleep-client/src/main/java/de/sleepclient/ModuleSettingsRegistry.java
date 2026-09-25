package de.sleepclient;

import java.util.List;

public final class ModuleSettingsRegistry {
    public enum Type { BOOLEAN, INTEGER, FLOAT, CHOICE }

    public record SettingSpec(
            String key,
            String label,
            String description,
            Type type,
            double min,
            double max,
            double step,
            List<String> choices,
            String defaultValue
    ) {
        public static SettingSpec bool(String key, String label, String description, boolean value) {
            return new SettingSpec(key, label, description, Type.BOOLEAN, 0, 1, 1, List.of(), Boolean.toString(value));
        }
        public static SettingSpec integer(String key, String label, String description, int min, int max, int step, int value) {
            return new SettingSpec(key, label, description, Type.INTEGER, min, max, step, List.of(), Integer.toString(value));
        }
        public static SettingSpec decimal(String key, String label, String description, double min, double max, double step, double value) {
            return new SettingSpec(key, label, description, Type.FLOAT, min, max, step, List.of(), Double.toString(value));
        }
        public static SettingSpec choice(String key, String label, String description, String value, String... choices) {
            return new SettingSpec(key, label, description, Type.CHOICE, 0, 0, 0, List.of(choices), value);
        }
    }

    public static List<SettingSpec> settingsFor(Module module) {
        String n = module.name();

        return switch (n) {
            case "PlayerESP" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Maximale ESP-Reichweite in Blöcken.", 16, 256, 16, 96),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Spieler-Boxen.", 1.0, 5.0, 0.2, 2.2),
                    SettingSpec.bool("distanceHud", "Distanz HUD", "Zeigt Namen und Entfernung rechts oben.", true),
                    SettingSpec.bool("throughWalls", "Durch Wände", "ESP bleibt auch hinter Blöcken sichtbar.", true)
            );
            case "StorageESP" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Wie weit Speicher gesucht werden.", 16, 192, 16, 96),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Storage-Boxen.", 1.0, 5.0, 0.2, 2.0),
                    SettingSpec.bool("hoppers", "Hopper", "Hopper ebenfalls markieren.", true),
                    SettingSpec.bool("furnaces", "Öfen", "Öfen und ähnliche Blöcke markieren.", false),
                    SettingSpec.bool("hud", "Zähler HUD", "Anzahl gefundener Speicher anzeigen.", true)
            );
            case "BlockESP" -> List.of(
                    SettingSpec.choice("target", "Block", "Welcher Block hervorgehoben wird.", "Amethyst Cluster",
                            "Amethyst Cluster", "Ancient Debris", "Diamond Ore", "Spawner"),
                    SettingSpec.integer("range", "Reichweite", "Suchradius für BlockESP.", 16, 64, 8, 32),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Block-Markierung.", 1.0, 5.0, 0.2, 2.0),
                    SettingSpec.integer("maxResults", "Max. Treffer", "Begrenzt gleichzeitig angezeigte Treffer.", 32, 512, 32, 192)
            );
            case "Chunk Finder" -> List.of(
                    SettingSpec.integer("radius", "Scan Radius", "Radius bereits geladener Chunks.", 2, 12, 1, 7),
                    SettingSpec.integer("minClusters", "Min. Cluster", "Mindestzahl Amethyst-Cluster für einen Treffer.", 1, 16, 1, 1),
                    SettingSpec.integer("scanDelay", "Scan Tempo", "Ticks zwischen zwei Chunk-Scans.", 1, 20, 1, 4),
                    SettingSpec.bool("hud", "HUD", "Gefundene Amethyst-Chunks anzeigen.", true)
            );
            case "Sus Chunk Finder" -> List.of(
                    SettingSpec.integer("radius", "Scan Radius", "Radius der analysierten geladenen Chunks.", 2, 12, 1, 7),
                    SettingSpec.integer("minScore", "Mindest Score", "Ab welchem Score ein Chunk verdächtig ist.", 1, 40, 1, 8),
                    SettingSpec.bool("hud", "HUD", "Gefundene Chunks oben links anzeigen.", true)
            );
            case "ChunkFinderV2" -> List.of(
                    SettingSpec.integer("radius", "Scan Radius", "Radius für die erweiterte Chunk-Analyse.", 2, 12, 1, 8),
                    SettingSpec.integer("confidence", "Confidence", "Mindestwert für einen Treffer.", 10, 100, 5, 65),
                    SettingSpec.bool("mapOverlay", "Map Overlay", "Treffer auf einer Karten-Ansicht darstellen.", true)
            );
            case "SusChunkFinderV2" -> List.of(
                    SettingSpec.integer("radius", "Scan Radius", "Radius der erweiterten Analyse.", 2, 12, 1, 8),
                    SettingSpec.integer("confidence", "Confidence", "Mindestwahrscheinlichkeit für einen Treffer.", 10, 100, 5, 70),
                    SettingSpec.bool("mapOverlay", "Top Down Map", "Zusätzliche Draufsicht aktivieren.", true)
            );
            case "Netherite Finder" -> List.of(
                    SettingSpec.integer("radius", "Scan Radius", "Geladene Chunks nach Ancient Debris scannen.", 2, 10, 1, 5),
                    SettingSpec.integer("maxResults", "Max. Treffer", "Maximal angezeigte Ancient-Debris-Blöcke.", 32, 512, 32, 192),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Ancient-Debris-Markierung.", 1.0, 5.0, 0.2, 2.2)
            );
            case "TraderFinder" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Suchreichweite für Wandering Trader.", 32, 256, 16, 128),
                    SettingSpec.bool("llamas", "Trader Llamas", "Trader-Lamas ebenfalls markieren.", true),
                    SettingSpec.bool("hud", "Distanz HUD", "Nächsten Trader mit Entfernung anzeigen.", true)
            );
            case "LootESP" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Reichweite für gedroppte Items.", 16, 192, 16, 96),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Item-Boxen.", 1.0, 5.0, 0.2, 1.8),
                    SettingSpec.integer("maxItems", "Max. Items", "Maximale Zahl markierter Item-Entities.", 16, 256, 16, 96)
            );
            case "Totem Counter" -> List.of(
                    SettingSpec.choice("position", "Position", "Position des Totem-Zählers.", "Bottom Right",
                            "Top Left", "Top Right", "Bottom Left", "Bottom Right"),
                    SettingSpec.bool("background", "Hintergrund", "Dunklen Hintergrund anzeigen.", true),
                    SettingSpec.bool("glow", "Glow", "Akzent-Glow am Zähler anzeigen.", true)
            );
            case "Fullbright" -> List.of(
                    SettingSpec.decimal("brightness", "Helligkeit", "Zielhelligkeit des Clients.", 0.5, 1.0, 0.05, 1.0)
            );
            case "No Flame" -> List.of(
                    SettingSpec.decimal("opacity", "Flame Opacity", "Deckkraft des Feuer-Overlays.", 0.0, 1.0, 0.1, 0.0)
            );
            case "No Explosions" -> List.of(
                    SettingSpec.bool("particles", "Partikel ausblenden", "Explosionspartikel reduzieren.", true),
                    SettingSpec.bool("shake", "Kameraeffekt", "Explosionsbedingte Kameraeffekte reduzieren.", true)
            );
            case "Hole ESP" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Suchreichweite für Löcher.", 8, 64, 8, 32),
                    SettingSpec.bool("obsidian", "Obsidian Holes", "Obsidian-Löcher hervorheben.", true),
                    SettingSpec.bool("bedrock", "Bedrock Holes", "Bedrock-Löcher hervorheben.", true)
            );
            case "Block Overlay", "Block Highlight" -> List.of(
                    SettingSpec.decimal("opacity", "Deckkraft", "Deckkraft der Block-Markierung.", 0.1, 1.0, 0.1, 0.7),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Außenlinie.", 1.0, 5.0, 0.2, 2.0),
                    SettingSpec.bool("glow", "Glow", "Leuchten um den markierten Block.", true)
            );
            case "Name Tags" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Maximale Nametag-Reichweite.", 16, 256, 16, 128),
                    SettingSpec.bool("health", "Leben", "Lebenspunkte anzeigen.", true),
                    SettingSpec.bool("armor", "Rüstung", "Rüstung anzeigen.", true),
                    SettingSpec.bool("items", "Items", "Gehaltene Items anzeigen.", true)
            );
            case "Pearl Prediction" -> List.of(
                    SettingSpec.integer("steps", "Pfad Schritte", "Anzahl Simulationsschritte.", 20, 200, 10, 100),
                    SettingSpec.decimal("lineWidth", "Linienstärke", "Stärke der Flugbahn.", 1.0, 5.0, 0.2, 1.8),
                    SettingSpec.bool("landingMarker", "Landepunkt", "Voraussichtlichen Landepunkt markieren.", true)
            );
            case "Pearl Catch" -> List.of(
                    SettingSpec.integer("warningRange", "Warnradius", "Warnung bei nahen Pearl-Landungen.", 4, 32, 2, 12),
                    SettingSpec.bool("sound", "Ton", "Warnton bei eingehender Pearl.", true)
            );
            case "Heat Color" -> List.of(
                    SettingSpec.bool("players", "Spieler", "Spieler nach Leben einfärben.", true),
                    SettingSpec.bool("mobs", "Mobs", "Mobs nach Leben einfärben.", true),
                    SettingSpec.decimal("opacity", "Deckkraft", "Deckkraft der Health-Farbe.", 0.2, 1.0, 0.1, 0.8)
            );
            case "Custom Flame" -> List.of(
                    SettingSpec.decimal("height", "Höhe", "Höhe des Feuer-Overlays.", 0.1, 1.0, 0.1, 0.45),
                    SettingSpec.decimal("opacity", "Deckkraft", "Deckkraft des Overlays.", 0.1, 1.0, 0.1, 0.65)
            );
            case "CustomWorld" -> List.of(
                    SettingSpec.choice("time", "Zeit", "Clientseitige Tageszeit.", "Server",
                            "Server", "Day", "Sunset", "Night"),
                    SettingSpec.bool("weather", "Clear Weather", "Regen optisch ausblenden.", false),
                    SettingSpec.bool("fog", "Custom Fog", "Eigene Nebel-Darstellung verwenden.", false)
            );
            case "FireFly" -> List.of(
                    SettingSpec.integer("count", "Anzahl", "Anzahl kosmetischer Fireflies.", 5, 80, 5, 25),
                    SettingSpec.decimal("size", "Größe", "Größe der Partikel.", 0.2, 2.0, 0.1, 0.8)
            );
            case "Target ESP" -> List.of(
                    SettingSpec.choice("style", "Stil", "Darstellung des aktuellen Ziels.", "Ring",
                            "Ring", "Box", "Marker"),
                    SettingSpec.decimal("size", "Größe", "Größe der Zielmarkierung.", 0.5, 2.0, 0.1, 1.0)
            );
            case "Waypoints" -> List.of(
                    SettingSpec.integer("maxDistance", "Sichtweite", "Maximale Sichtweite für Waypoints.", 128, 4096, 128, 2048),
                    SettingSpec.bool("beam", "Beam", "Vertikalen Beam anzeigen.", true),
                    SettingSpec.bool("distance", "Distanz", "Entfernung am Waypoint anzeigen.", true)
            );
            case "ShaderFog" -> List.of(
                    SettingSpec.decimal("density", "Dichte", "Dichte des Custom Fogs.", 0.0, 1.0, 0.05, 0.35),
                    SettingSpec.integer("distance", "Distanz", "Nebelentfernung.", 16, 256, 16, 96)
            );
            case "HandShader" -> List.of(
                    SettingSpec.decimal("glow", "Glow", "Leuchtstärke am gehaltenen Item.", 0.0, 2.0, 0.1, 0.8),
                    SettingSpec.decimal("lineWidth", "Outline", "Stärke der Outline.", 0.5, 5.0, 0.25, 1.5)
            );
            case "Swing Animation" -> List.of(
                    SettingSpec.choice("style", "Animation", "Stil der Handbewegung.", "Smooth",
                            "Vanilla", "Smooth", "Slide", "Spin"),
                    SettingSpec.decimal("speed", "Geschwindigkeit", "Geschwindigkeit der Animation.", 0.25, 2.0, 0.05, 1.0)
            );
            case "Sword Inspect" -> List.of(
                    SettingSpec.choice("style", "Inspect Stil", "Inspect-Animation.", "Classic",
                            "Classic", "Flip", "Spin"),
                    SettingSpec.decimal("speed", "Geschwindigkeit", "Inspect-Geschwindigkeit.", 0.25, 2.0, 0.05, 1.0)
            );
            case "Freelook" -> List.of(
                    SettingSpec.bool("hold", "Nur halten", "Freelook nur solange die Taste gehalten wird.", true),
                    SettingSpec.decimal("sensitivity", "Empfindlichkeit", "Empfindlichkeit der freien Kamera.", 0.2, 2.0, 0.1, 1.0)
            );
            case "Particles" -> List.of(
                    SettingSpec.integer("count", "Anzahl", "Partikel pro Effekt.", 1, 50, 1, 12),
                    SettingSpec.decimal("size", "Größe", "Größe der Partikel.", 0.2, 2.0, 0.1, 0.8)
            );
            case "Jump Circles" -> List.of(
                    SettingSpec.decimal("size", "Größe", "Radius des Jump-Circles.", 0.5, 4.0, 0.1, 1.6),
                    SettingSpec.decimal("duration", "Dauer", "Animationsdauer in Sekunden.", 0.2, 3.0, 0.1, 1.0)
            );
            case "Auto Builder", "Auto Schematic Builder" -> List.of(
                    SettingSpec.choice("mode", "Modus", "Bauweise des Schematic Builders.", "Legit",
                            "Legit", "Fast"),
                    SettingSpec.integer("placeDelay", "Place Delay", "Ticks zwischen Platzierungen.", 1, 20, 1, 4),
                    SettingSpec.bool("rotate", "Rotation", "Zum Zielblock drehen.", true),
                    SettingSpec.bool("pauseOnMissing", "Pause bei fehlenden Blöcken", "Stoppt wenn Material fehlt.", true)
            );
            case "Auto Area Miner" -> List.of(
                    SettingSpec.integer("breakDelay", "Break Delay", "Ticks zwischen Blöcken.", 1, 20, 1, 3),
                    SettingSpec.bool("autoTool", "Auto Tool", "Passendes Werkzeug wählen.", true),
                    SettingSpec.bool("return", "Auto Return", "Nach Abschluss zurückkehren.", true)
            );
            case "AutoMine" -> List.of(
                    SettingSpec.integer("length", "Tunnel Länge", "Ziel-Länge des Tunnels.", 16, 512, 16, 128),
                    SettingSpec.bool("lavaAvoid", "Lava Avoidance", "Bei Lava stoppen oder ausweichen.", true),
                    SettingSpec.bool("autoTool", "Auto Tool", "Passendes Werkzeug wählen.", true)
            );
            case "BoneDropper" -> List.of(
                    SettingSpec.integer("delay", "Drop Delay", "Ticks zwischen Aktionen.", 1, 40, 1, 8),
                    SettingSpec.bool("collect", "Auto Collect", "Drops automatisch einsammeln.", true)
            );
            case "Auto Villager Trade" -> List.of(
                    SettingSpec.integer("maxTrades", "Max Trades", "Maximale Trades pro Durchlauf.", 1, 128, 1, 32),
                    SettingSpec.bool("restockWait", "Restock warten", "Auf Villager-Restock warten.", true)
            );
            case "Auto Elytra Finder" -> List.of(
                    SettingSpec.integer("searchRadius", "Suchradius", "Suchradius im End.", 64, 2048, 64, 512),
                    SettingSpec.bool("autoFly", "Auto Fly", "Automatische Flugnavigation.", false)
            );
            case "Deep Player Finder" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Analyse-Reichweite für geladene Spieler.", 32, 256, 16, 128),
                    SettingSpec.integer("maxY", "Max Y", "Nur Spieler unterhalb dieser Höhe.", -64, 128, 8, 32)
            );
            case "RegionMap" -> List.of(
                    SettingSpec.integer("scale", "Map Scale", "Größe der RegionMap.", 1, 8, 1, 3),
                    SettingSpec.bool("chunkGrid", "Chunk Grid", "Chunk-Gitter anzeigen.", true),
                    SettingSpec.bool("coords", "Koordinaten", "Koordinaten auf der Karte anzeigen.", true)
            );
            case "SeedAnalyzer" -> List.of(
                    SettingSpec.bool("copyClipboard", "In Zwischenablage", "Seed nach Erkennung kopieren.", false),
                    SettingSpec.bool("notify", "Benachrichtigung", "Seed-Status im Chat anzeigen.", true)
            );
            case "Auto Crystal" -> List.of(
                    SettingSpec.decimal("range", "Reichweite", "Crystal-Reichweite.", 2.0, 6.0, 0.1, 4.5),
                    SettingSpec.integer("delay", "Delay", "Ticks zwischen Aktionen.", 0, 20, 1, 1),
                    SettingSpec.bool("damageCalc", "Damage Calc", "Schaden in die Zielwahl einbeziehen.", true),
                    SettingSpec.bool("faceCheck", "Facecheck", "Facecheck-Regeln verwenden.", true)
            );
            case "Anchor Macro" -> List.of(
                    SettingSpec.integer("delay", "Delay", "Ticks zwischen Charge und Detonation.", 0, 20, 1, 2),
                    SettingSpec.bool("rightClick", "Hold Right Click", "Bei gehaltenem Rechtsklick arbeiten.", true)
            );
            case "Safe Anchor" -> List.of(
                    SettingSpec.decimal("minHealth", "Min. Leben", "Unter diesem Leben nicht detonieren.", 1.0, 20.0, 0.5, 8.0),
                    SettingSpec.bool("shieldCheck", "Shield Check", "Sicherheitsprüfung gegen Schild.", true)
            );
            case "Auto Hit Crystal" -> List.of(
                    SettingSpec.decimal("range", "Reichweite", "Reichweite für Crystal-Hits.", 2.0, 6.0, 0.1, 4.5),
                    SettingSpec.integer("delay", "Hit Delay", "Ticks zwischen Hits.", 0, 20, 1, 1)
            );
            case "Crystal Optimizer" -> List.of(
                    SettingSpec.integer("removeDelay", "Remove Delay", "Clientseitiges Removal-Timing.", 0, 10, 1, 0)
            );
            case "Obsidian Optimizer" -> List.of(
                    SettingSpec.integer("placeDelay", "Place Delay", "Ticks zwischen Platzierungen.", 0, 20, 1, 1),
                    SettingSpec.bool("switchBack", "Zurückwechseln", "Nach Platzierung auf vorherigen Slot wechseln.", true)
            );
            case "AutoTrap" -> List.of(
                    SettingSpec.decimal("range", "Reichweite", "Zielreichweite.", 2.0, 6.0, 0.1, 4.0),
                    SettingSpec.integer("blocksPerTick", "Blöcke/Tick", "Maximale Platzierungen pro Tick.", 1, 8, 1, 2)
            );
            case "Aim Assist" -> List.of(
                    SettingSpec.decimal("range", "Reichweite", "Zielreichweite.", 2.0, 8.0, 0.1, 5.0),
                    SettingSpec.decimal("smoothing", "Smoothing", "Stärke der weichen Zielbewegung.", 0.05, 1.0, 0.05, 0.35),
                    SettingSpec.integer("fov", "FOV", "Ziel-FOV in Grad.", 10, 180, 5, 90),
                    SettingSpec.bool("prediction", "Prediction", "Bewegung des Ziels berücksichtigen.", true)
            );
            case "Silent Aim" -> List.of(
                    SettingSpec.decimal("range", "Reichweite", "Zielreichweite.", 2.0, 8.0, 0.1, 5.0),
                    SettingSpec.integer("fov", "FOV", "Ziel-FOV.", 10, 180, 5, 90)
            );
            case "Trigger Bot" -> List.of(
                    SettingSpec.integer("cps", "CPS", "Angriffe pro Sekunde.", 1, 20, 1, 10),
                    SettingSpec.bool("playersOnly", "Nur Spieler", "Nur Spieler als Ziele.", true)
            );
            case "AutoClicker" -> List.of(
                    SettingSpec.integer("minCps", "Min CPS", "Minimale Klickrate.", 1, 20, 1, 8),
                    SettingSpec.integer("maxCps", "Max CPS", "Maximale Klickrate.", 1, 20, 1, 12),
                    SettingSpec.bool("randomize", "Randomize", "Klickabstände leicht variieren.", true)
            );
            case "Criticals" -> List.of(
                    SettingSpec.bool("playersOnly", "Nur Spieler", "Nur bei Spielern auslösen.", true),
                    SettingSpec.integer("delay", "Delay", "Ticks zwischen Critical-Versuchen.", 0, 20, 1, 2)
            );
            case "Reach" -> List.of(
                    SettingSpec.decimal("range", "Reach", "Zusätzliche Angriffsreichweite.", 3.0, 6.0, 0.1, 3.3),
                    SettingSpec.bool("randomize", "Randomize", "Reichweite leicht variieren.", true)
            );
            case "Hit Boxes" -> List.of(
                    SettingSpec.decimal("scale", "Größe", "Skalierung der Ziel-Hitbox.", 1.0, 2.0, 0.05, 1.15)
            );
            case "Auto Shield Disabler" -> List.of(
                    SettingSpec.integer("delay", "Delay", "Ticks vor dem Axe-Hit.", 0, 20, 1, 1),
                    SettingSpec.bool("switchBack", "Zurückwechseln", "Nach dem Hit zurückwechseln.", true)
            );
            case "Auto Mace" -> List.of(
                    SettingSpec.decimal("minFall", "Min. Fallhöhe", "Mindest-Fallhöhe für Mace-Timing.", 1.0, 20.0, 0.5, 4.0),
                    SettingSpec.bool("fullyCharged", "Fully Charged", "Nur voll geladene Hits.", true)
            );
            case "Mace Swap" -> List.of(
                    SettingSpec.integer("slot", "Mace Slot", "Hotbar-Slot der Mace.", 1, 9, 1, 1),
                    SettingSpec.bool("swapBack", "Swap Back", "Nach dem Treffer zurückwechseln.", true)
            );
            case "Perfect Wind Charge" -> List.of(
                    SettingSpec.decimal("height", "Zielhöhe", "Gewünschte Launch-Höhe.", 2.0, 30.0, 0.5, 10.0),
                    SettingSpec.bool("verticalOnly", "Nur vertikal", "Nur gerade nach oben ausrichten.", true)
            );
            case "Auto Totem" -> List.of(
                    SettingSpec.decimal("health", "Health Trigger", "Ab diesem Leben Totem priorisieren.", 1.0, 20.0, 0.5, 8.0),
                    SettingSpec.bool("always", "Immer Totem", "Totem permanent in Offhand halten.", true)
            );
            case "Auto Inventory Totem" -> List.of(
                    SettingSpec.integer("reserve", "Reserve", "Mindestanzahl Totems im Inventar.", 1, 20, 1, 2)
            );
            case "Auto Refill Hotbar" -> List.of(
                    SettingSpec.integer("threshold", "Schwelle", "Ab welcher Stackgröße nachgefüllt wird.", 1, 63, 1, 16),
                    SettingSpec.integer("delay", "Delay", "Ticks zwischen Refills.", 1, 20, 1, 4)
            );
            case "Auto XP" -> List.of(
                    SettingSpec.integer("delay", "Throw Delay", "Ticks zwischen XP-Flaschen.", 1, 20, 1, 2),
                    SettingSpec.decimal("stopHealth", "Stop bei Leben", "Bei wenig Leben stoppen.", 1.0, 20.0, 0.5, 6.0)
            );
            case "Key Pearl" -> List.of(
                    SettingSpec.integer("slot", "Pearl Slot", "Hotbar-Slot der Pearls.", 1, 9, 1, 2),
                    SettingSpec.bool("swapBack", "Swap Back", "Nach dem Wurf zurückwechseln.", true)
            );
            case "Auto Aggro Pearl" -> List.of(
                    SettingSpec.integer("range", "Reichweite", "Reichweite für Pearl-Reaktionen.", 8, 64, 4, 32),
                    SettingSpec.bool("followEnemy", "Enemy Pearl folgen", "Ziel-Pearl als Reaktionsziel verwenden.", true)
            );
            case "Sprint" -> List.of(
                    SettingSpec.bool("omni", "Omni Sprint", "Auch bei seitlicher Bewegung sprinten.", false)
            );
            case "No Slow" -> List.of(
                    SettingSpec.decimal("itemSlow", "Item Slow", "Bewegungsmultiplikator beim Benutzen von Items.", 0.2, 1.0, 0.05, 1.0)
            );
            case "Step" -> List.of(
                    SettingSpec.decimal("height", "Step Höhe", "Maximale Step-Höhe.", 0.5, 2.5, 0.1, 1.0)
            );
            case "Velocity" -> List.of(
                    SettingSpec.decimal("horizontal", "Horizontal", "Horizontaler Knockback-Multiplikator.", 0.0, 1.0, 0.05, 0.0),
                    SettingSpec.decimal("vertical", "Vertikal", "Vertikaler Knockback-Multiplikator.", 0.0, 1.0, 0.05, 0.0)
            );
            case "Show HUD" -> List.of(
                    SettingSpec.bool("moduleList", "Module List", "Aktive Module im HUD anzeigen.", true),
                    SettingSpec.bool("coords", "Koordinaten", "Spielerkoordinaten anzeigen.", true),
                    SettingSpec.bool("fps", "FPS", "FPS anzeigen.", true)
            );
            case "Spotify HUD" -> List.of(
                    SettingSpec.choice("position", "Position", "Position der Musik-Karte.", "Top Right",
                            "Top Left", "Top Right", "Bottom Left", "Bottom Right"),
                    SettingSpec.bool("artwork", "Cover", "Album-Cover anzeigen.", true)
            );
            case "Keybind Manager" -> List.of(
                    SettingSpec.bool("showConflicts", "Konflikte anzeigen", "Doppelt belegte Tasten hervorheben.", true)
            );
            case "Profiles" -> List.of(
                    SettingSpec.bool("autoSave", "Auto Save", "Profiländerungen automatisch speichern.", true)
            );
            case "Update Center" -> List.of(
                    SettingSpec.bool("startupCheck", "Beim Start prüfen", "Beim Start nach Updates suchen.", true)
            );
            case "Notifications" -> List.of(
                    SettingSpec.bool("moduleToggle", "Module Toggle", "Benachrichtigung beim Umschalten.", true),
                    SettingSpec.bool("updates", "Updates", "Update-Hinweise anzeigen.", true)
            );
            case "Theme Editor" -> List.of(
                    SettingSpec.choice("preset", "Preset", "Farb-Preset des Clients.", "Pink",
                            "Pink", "Purple", "Blue", "Green"),
                    SettingSpec.decimal("glow", "Glow Stärke", "Stärke des GUI-Glows.", 0.0, 2.0, 0.1, 1.0)
            );
            case "HUD Editor" -> List.of(
                    SettingSpec.decimal("scale", "HUD Scale", "Skalierung der HUD-Elemente.", 0.5, 2.0, 0.05, 1.0),
                    SettingSpec.bool("snap", "Snap", "HUD-Elemente an Kanten einrasten.", true)
            );
            case "Animation Settings" -> List.of(
                    SettingSpec.decimal("speed", "Animation Speed", "Geschwindigkeit der GUI-Animationen.", 0.35, 2.0, 0.05, 1.0)
            );
            case "GUI Scale" -> List.of(
                    SettingSpec.decimal("scale", "GUI Scale", "Skalierung des ClickGUI.", 0.75, 1.5, 0.05, 1.0)
            );
            default -> List.of(
                    SettingSpec.bool("enabledExtra", "Zusatzoption", "Modulspezifische Zusatzfunktion.", true)
            );
        };
    }

    private ModuleSettingsRegistry() {}
}
