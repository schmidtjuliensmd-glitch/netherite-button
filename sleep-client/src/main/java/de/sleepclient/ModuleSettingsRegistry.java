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
            case "ChunkFinder" -> List.of(
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
            case "KillAura" -> List.of(
                    SettingSpec.decimal("range","Reichweite","Maximale Zielreichweite.",2.0,8.0,0.1,4.5),
                    SettingSpec.integer("cps","CPS","Angriffe pro Sekunde.",1,20,1,10),
                    SettingSpec.integer("fov","FOV","Ziel-FOV.",10,360,10,180),
                    SettingSpec.choice("priority","Priorität","Zielauswahl.","Distance","Distance","Health","Armor"),
                    SettingSpec.bool("playersOnly","Nur Spieler","Nur Spieler angreifen.",true)
            );
            case "Crystal Aura" -> List.of(
                    SettingSpec.decimal("placeRange","Place Range","Reichweite fürs Platzieren.",2.0,6.0,0.1,4.5),
                    SettingSpec.decimal("breakRange","Break Range","Reichweite fürs Zerstören.",2.0,6.0,0.1,4.5),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Aktionen.",0,20,1,1),
                    SettingSpec.bool("damageCalc","Damage Calc","Schaden bei der Auswahl berücksichtigen.",true)
            );
            case "Anchor Aura" -> List.of(
                    SettingSpec.decimal("range","Reichweite","Anchor-Reichweite.",2.0,6.0,0.1,4.0),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Aktionen.",0,20,1,2),
                    SettingSpec.bool("safety","Safety","Eigenen Schaden begrenzen.",true)
            );
            case "Bow Aim", "Auto Bow Release" -> List.of(
                    SettingSpec.decimal("range","Reichweite","Maximale Zielreichweite.",8.0,128.0,4.0,48.0),
                    SettingSpec.decimal("smoothing","Smoothing","Stärke der Zielbewegung.",0.05,1.0,0.05,0.35),
                    SettingSpec.bool("prediction","Prediction","Bewegung des Ziels vorhersagen.",true)
            );
            case "Bow Spam" -> List.of(
                    SettingSpec.integer("chargeTicks","Charge Ticks","Ticks bis zum Schuss.",1,20,1,5),
                    SettingSpec.bool("onlyBow","Nur Bow","Nur mit einem Bogen aktiv.",true)
            );
            case "Auto Armor" -> List.of(
                    SettingSpec.choice("priority","Priorität","Bevorzugte Rüstungsbewertung.","Protection","Protection","Durability","Netherite"),
                    SettingSpec.bool("elytraKeep","Elytra behalten","Elytra nicht automatisch ersetzen.",true),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Ausrüstungsaktionen.",0,20,1,2)
            );
            case "Auto Pot" -> List.of(
                    SettingSpec.decimal("health","Health Trigger","Unter diesem Leben Heiltrank nutzen.",1.0,20.0,0.5,8.0),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Würfen.",1,20,1,3),
                    SettingSpec.bool("splashOnly","Nur Splash","Nur Wurftränke verwenden.",true)
            );
            case "Auto Gap" -> List.of(
                    SettingSpec.decimal("health","Health Trigger","Unter diesem Leben Goldapfel nutzen.",1.0,20.0,0.5,10.0),
                    SettingSpec.bool("enchantedFirst","Enchanted zuerst","Verzauberte Äpfel bevorzugen.",false)
            );
            case "Auto Weapon" -> List.of(
                    SettingSpec.choice("weapon","Waffe","Bevorzugte Waffenklasse.","Best Damage","Best Damage","Sword","Axe","Mace"),
                    SettingSpec.bool("switchBack","Zurückwechseln","Nach Aktion zurückwechseln.",true)
            );
            case "Target Selector" -> List.of(
                    SettingSpec.choice("priority","Priorität","Zielsortierung.","Distance","Distance","Health","Armor","Angle"),
                    SettingSpec.integer("fov","FOV","Maximales Ziel-FOV.",10,360,10,180),
                    SettingSpec.decimal("range","Reichweite","Maximale Auswahlreichweite.",2.0,16.0,0.5,6.0)
            );
            case "Anti Bot" -> List.of(
                    SettingSpec.bool("tabCheck","Tab Check","Spieler ohne Tab-Eintrag ignorieren.",true),
                    SettingSpec.bool("invisibleCheck","Invisible Check","Unsichtbare Verdachts-Entities filtern.",false)
            );
            case "Teams" -> List.of(
                    SettingSpec.bool("scoreboard","Scoreboard Teams","Minecraft-Teamzugehörigkeit beachten.",true),
                    SettingSpec.bool("nameColor","Namensfarbe","Namensfarben als Team-Hinweis verwenden.",false)
            );
            case "Friends" -> List.of(
                    SettingSpec.bool("protectCombat","Combat Schutz","Freunde nicht als Combat-Ziele wählen.",true),
                    SettingSpec.bool("highlight","Highlight","Freunde visuell hervorheben.",true)
            );
            case "Backtrack" -> List.of(
                    SettingSpec.integer("historyMs","History","Positionshistorie in Millisekunden.",50,500,25,150),
                    SettingSpec.bool("playersOnly","Nur Spieler","Nur Spielerhistorie speichern.",true)
            );
            case "Shield Breaker" -> List.of(
                    SettingSpec.integer("delay","Delay","Ticks vor dem Axe-Hit.",0,20,1,1),
                    SettingSpec.bool("switchBack","Zurückwechseln","Danach auf vorherigen Slot wechseln.",true)
            );
            case "Hit Select" -> List.of(
                    SettingSpec.integer("cooldown","Cooldown","Ticks zwischen ausgewählten Hits.",0,20,1,2),
                    SettingSpec.bool("onlyAirborne","Nur airborne","Nur bei bestimmten Bewegungszuständen auslösen.",false)
            );
            case "WTap", "STap" -> List.of(
                    SettingSpec.integer("delay","Reset Delay","Ticks zwischen Sprint-Reset-Aktionen.",0,10,1,1),
                    SettingSpec.bool("playersOnly","Nur Spieler","Nur im Kampf gegen Spieler.",true)
            );
            case "Auto Rod" -> List.of(
                    SettingSpec.decimal("range","Reichweite","Maximale Rod-Reichweite.",3.0,16.0,0.5,8.0),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Rod-Aktionen.",1,20,1,4)
            );

            case "BaseFinder" -> List.of(
                    SettingSpec.integer("radius","Scan Radius","Geladene Chunks analysieren.",2,12,1,8),
                    SettingSpec.integer("storageWeight","Storage Gewicht","Gewichtung von Storage-Blöcken.",1,10,1,5),
                    SettingSpec.bool("hud","HUD","Trefferliste anzeigen.",true)
            );
            case "StashFinder" -> List.of(
                    SettingSpec.integer("radius","Scan Radius","Geladene Chunks nach Storage-Dichte scannen.",2,12,1,8),
                    SettingSpec.integer("minStorage","Min. Storage","Mindestanzahl für einen Treffer.",1,64,1,8),
                    SettingSpec.bool("shulkers","Shulker priorisieren","Shulker stärker gewichten.",true)
            );
            case "SpawnerFinder", "PortalFinder", "Bed Finder", "Beacon Finder", "Shulker Finder", "Minecart Finder" -> List.of(
                    SettingSpec.integer("radius","Scan Radius","Radius geladener Chunks.",2,12,1,6),
                    SettingSpec.integer("maxResults","Max. Treffer","Maximal gleichzeitig angezeigte Treffer.",16,256,16,96),
                    SettingSpec.decimal("lineWidth","Linienstärke","Stärke der Markierung.",1.0,5.0,0.2,2.0)
            );
            case "EndCityFinder", "BastionFinder", "FortressFinder", "TrialChamberFinder", "AncientCityFinder" -> List.of(
                    SettingSpec.integer("radius","Suchradius","Radius der lokalen Strukturhinweise.",2,16,1,8),
                    SettingSpec.bool("hud","HUD","Strukturhinweise anzeigen.",true),
                    SettingSpec.bool("waypoint","Waypoint","Treffer als Waypoint markieren.",true)
            );
            case "OreScanner" -> List.of(
                    SettingSpec.choice("ore","Erz","Gesuchtes Erz.","Diamond","Diamond","Ancient Debris","Emerald","Gold","Iron"),
                    SettingSpec.integer("range","Reichweite","Block-Suchradius.",16,64,8,32),
                    SettingSpec.integer("maxResults","Max. Treffer","Maximale Zahl markierter Blöcke.",32,512,32,192)
            );
            case "ChestCounter" -> List.of(
                    SettingSpec.integer("radius","Radius","Geladene Chunks zählen.",2,12,1,6),
                    SettingSpec.bool("barrels","Barrels","Barrels mitzählen.",true),
                    SettingSpec.bool("shulkers","Shulker","Shulkerboxen mitzählen.",true)
            );
            case "ChunkActivity" -> List.of(
                    SettingSpec.integer("history","Historie","Dauer der lokalen Aktivitätshistorie.",5,120,5,30),
                    SettingSpec.bool("hud","HUD","Aktivitätsanzeige einblenden.",true)
            );
            case "PortalTrace" -> List.of(
                    SettingSpec.integer("maxEntries","Max. Einträge","Gespeicherte Portalpositionen.",10,200,10,50),
                    SettingSpec.bool("waypoints","Waypoints","Portalpositionen markieren.",true)
            );
            case "Coordinate Logger", "Death Logger" -> List.of(
                    SettingSpec.integer("maxEntries","Max. Einträge","Maximal gespeicherte Positionen.",10,500,10,100),
                    SettingSpec.bool("chatNotify","Chat Hinweis","Neue Einträge im Chat melden.",true)
            );

            case "EntityESP", "MobESP", "ProjectileESP", "SpawnerESP", "PortalESP", "BedESP", "BeaconESP" -> List.of(
                    SettingSpec.integer("range","Reichweite","Maximale ESP-Reichweite.",16,256,16,96),
                    SettingSpec.decimal("lineWidth","Linienstärke","Stärke der ESP-Boxen.",1.0,5.0,0.2,2.0),
                    SettingSpec.bool("throughWalls","Durch Wände","ESP durch Blöcke sichtbar lassen.",true)
            );
            case "Tracers" -> List.of(
                    SettingSpec.choice("target","Ziele","Welche Ziele Linien bekommen.","Players","Players","Mobs","Items","All"),
                    SettingSpec.integer("range","Reichweite","Maximale Tracer-Reichweite.",16,256,16,128),
                    SettingSpec.decimal("lineWidth","Linienstärke","Stärke der Linien.",0.5,5.0,0.25,1.5)
            );
            case "Chams" -> List.of(
                    SettingSpec.choice("target","Ziele","Welche Entities eingefärbt werden.","Players","Players","Mobs","All"),
                    SettingSpec.decimal("opacity","Deckkraft","Deckkraft der Chams.",0.1,1.0,0.1,0.7),
                    SettingSpec.bool("throughWalls","Durch Wände","Auch hinter Blöcken anzeigen.",true)
            );
            case "SkeletonESP" -> List.of(
                    SettingSpec.integer("range","Reichweite","Maximale SkeletonESP-Reichweite.",16,256,16,96),
                    SettingSpec.decimal("lineWidth","Linienstärke","Stärke des Skeletts.",0.5,4.0,0.25,1.5)
            );
            case "Health Bars" -> List.of(
                    SettingSpec.integer("range","Reichweite","Maximale Sichtweite.",16,256,16,96),
                    SettingSpec.bool("numbers","Zahlen","HP-Zahlen zusätzlich anzeigen.",true),
                    SettingSpec.bool("players","Spieler","Spieler anzeigen.",true),
                    SettingSpec.bool("mobs","Mobs","Mobs anzeigen.",true)
            );
            case "Armor HUD" -> List.of(
                    SettingSpec.choice("position","Position","Position des Armor HUD.","Bottom","Top","Bottom","Left","Right"),
                    SettingSpec.bool("durability","Haltbarkeit","Haltbarkeit anzeigen.",true),
                    SettingSpec.bool("percent","Prozent","Haltbarkeit in Prozent anzeigen.",true)
            );
            case "Potion HUD" -> List.of(
                    SettingSpec.choice("position","Position","Position des Potion HUD.","Top Right","Top Left","Top Right","Bottom Left","Bottom Right"),
                    SettingSpec.bool("duration","Dauer","Restdauer anzeigen.",true)
            );
            case "Keystrokes" -> List.of(
                    SettingSpec.choice("position","Position","Position des Keystrokes HUD.","Bottom Left","Top Left","Top Right","Bottom Left","Bottom Right"),
                    SettingSpec.bool("mouse","Maus","Maustasten anzeigen.",true),
                    SettingSpec.bool("cps","CPS","CPS mit anzeigen.",true)
            );
            case "CPS Counter", "FPS Counter", "Coordinates HUD", "Direction HUD" -> List.of(
                    SettingSpec.choice("position","Position","HUD-Position.","Top Left","Top Left","Top Right","Bottom Left","Bottom Right"),
                    SettingSpec.bool("background","Hintergrund","Dunklen Hintergrund anzeigen.",true)
            );
            case "Radar" -> List.of(
                    SettingSpec.integer("range","Reichweite","Radar-Reichweite.",16,256,16,96),
                    SettingSpec.integer("size","Größe","Radar-Größe.",80,300,10,150),
                    SettingSpec.bool("rotate","Rotieren","Radar mit Blickrichtung drehen.",true)
            );
            case "MiniMap" -> List.of(
                    SettingSpec.integer("size","Größe","Minimap-Größe.",80,300,10,160),
                    SettingSpec.integer("zoom","Zoom","Minimap-Zoomstufe.",1,8,1,3),
                    SettingSpec.bool("entities","Entities","Entities auf der Karte zeigen.",true)
            );
            case "Crosshair" -> List.of(
                    SettingSpec.integer("size","Größe","Crosshair-Größe.",2,20,1,6),
                    SettingSpec.integer("gap","Abstand","Abstand zur Mitte.",0,15,1,3),
                    SettingSpec.decimal("thickness","Stärke","Linienstärke.",1.0,5.0,0.5,1.5)
            );
            case "Zoom" -> List.of(
                    SettingSpec.decimal("factor","Zoom Faktor","Zoom-Stärke.",1.5,10.0,0.5,4.0),
                    SettingSpec.bool("smooth","Smooth","Weich ein- und auszoomen.",true)
            );
            case "Freecam" -> List.of(
                    SettingSpec.decimal("speed","Geschwindigkeit","Kamera-Geschwindigkeit.",0.1,5.0,0.1,1.0),
                    SettingSpec.bool("lockPlayer","Spieler fixieren","Spielerposition lokal unverändert lassen.",true)
            );
            case "Breadcrumbs", "Trail" -> List.of(
                    SettingSpec.integer("length","Länge","Länge des Trails.",10,500,10,100),
                    SettingSpec.decimal("width","Stärke","Stärke der Linie.",0.5,5.0,0.25,1.5),
                    SettingSpec.bool("fade","Fade","Trail langsam ausblenden.",true)
            );
            case "Damage Numbers" -> List.of(
                    SettingSpec.decimal("duration","Dauer","Anzeigedauer.",0.5,5.0,0.25,1.5),
                    SettingSpec.decimal("scale","Größe","Textgröße.",0.5,2.0,0.1,1.0)
            );
            case "Hit Color" -> List.of(
                    SettingSpec.decimal("opacity","Deckkraft","Deckkraft des Hit-Overlays.",0.1,1.0,0.1,0.8),
                    SettingSpec.decimal("duration","Dauer","Dauer des Hit-Overlays.",0.1,2.0,0.1,0.4)
            );
            case "Motion Blur" -> List.of(
                    SettingSpec.decimal("strength","Stärke","Motion-Blur-Stärke.",0.0,1.0,0.05,0.25)
            );
            case "Camera Clip" -> List.of(
                    SettingSpec.decimal("distance","Distanz","Dritte-Person-Kameradistanz.",2.0,12.0,0.5,4.0),
                    SettingSpec.bool("ignoreBlocks","Block Clip","Kamerakollision ignorieren.",true)
            );
            case "No Hurt Cam", "No Weather", "No Fog" -> List.of(
                    SettingSpec.bool("enabledVisual","Effekt ausblenden","Zugehörigen visuellen Effekt ausblenden.",true)
            );
            case "Time Changer" -> List.of(
                    SettingSpec.choice("time","Zeit","Clientseitige Tageszeit.","Day","Day","Sunset","Night","Midnight"),
                    SettingSpec.bool("freeze","Fixieren","Zeit lokal fixieren.",true)
            );
            case "FOV Changer" -> List.of(
                    SettingSpec.integer("fov","FOV","Client-FOV.",30,140,5,90),
                    SettingSpec.bool("dynamic","Dynamic FOV","Vanilla-Dynamik beibehalten.",false)
            );
            case "Item Physics" -> List.of(
                    SettingSpec.decimal("spin","Rotation","Rotationsgeschwindigkeit.",0.0,3.0,0.1,1.0),
                    SettingSpec.bool("flat","Flach","Items flach auf dem Boden darstellen.",true)
            );
            case "China Hat" -> List.of(
                    SettingSpec.decimal("radius","Radius","Hut-Radius.",0.3,1.5,0.1,0.7),
                    SettingSpec.decimal("opacity","Deckkraft","Deckkraft.",0.1,1.0,0.1,0.7)
            );
            case "Cape" -> List.of(
                    SettingSpec.choice("style","Stil","Cape-Stil.","Sleep","Sleep","Dark","Gradient"),
                    SettingSpec.bool("physics","Physik","Cape-Bewegung aktivieren.",true)
            );
            case "ESP Glow" -> List.of(
                    SettingSpec.integer("range","Reichweite","Maximale Reichweite.",16,256,16,96),
                    SettingSpec.decimal("strength","Glow","Glow-Stärke.",0.1,2.0,0.1,0.8)
            );

            case "Speed" -> List.of(
                    SettingSpec.choice("mode","Modus","Speed-Modus.","Vanilla","Vanilla","Strafe","LowHop"),
                    SettingSpec.decimal("speed","Geschwindigkeit","Bewegungsfaktor.",1.0,5.0,0.1,1.5)
            );
            case "Flight" -> List.of(
                    SettingSpec.decimal("speed","Geschwindigkeit","Fluggeschwindigkeit.",0.1,5.0,0.1,1.0),
                    SettingSpec.bool("vertical","Vertikal","Vertikale Steuerung aktivieren.",true)
            );
            case "Elytra Fly" -> List.of(
                    SettingSpec.decimal("speed","Geschwindigkeit","Elytra-Fluggeschwindigkeit.",0.5,5.0,0.1,1.5),
                    SettingSpec.bool("pitchControl","Pitch Control","Steigung über Blickrichtung steuern.",true)
            );
            case "Elytra Boost" -> List.of(
                    SettingSpec.decimal("boost","Boost","Boost-Stärke.",0.1,3.0,0.1,0.8),
                    SettingSpec.bool("fireworkAssist","Firework Assist","Feuerwerksnutzung unterstützen.",true)
            );
            case "Long Jump" -> List.of(
                    SettingSpec.decimal("power","Power","Horizontale Sprungstärke.",1.0,5.0,0.1,2.0),
                    SettingSpec.decimal("height","Höhe","Vertikale Sprungstärke.",0.1,2.0,0.1,0.6)
            );
            case "High Jump" -> List.of(
                    SettingSpec.decimal("height","Höhe","Sprunghöhe.",1.0,5.0,0.1,2.0)
            );
            case "Fast Fall" -> List.of(
                    SettingSpec.decimal("speed","Geschwindigkeit","Fallgeschwindigkeit.",1.0,5.0,0.1,2.0)
            );
            case "No Fall" -> List.of(
                    SettingSpec.choice("mode","Modus","NoFall-Modus.","Safe","Safe","Packet","Motion")
            );
            case "Safe Walk" -> List.of(
                    SettingSpec.bool("blocksOnly","Nur Blöcke","Nur an Blockkanten greifen.",true)
            );
            case "Parkour" -> List.of(
                    SettingSpec.bool("edgeJump","Edge Jump","Automatisch an Kanten springen.",true),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Sprüngen.",0,10,1,0)
            );
            case "Spider" -> List.of(
                    SettingSpec.decimal("speed","Geschwindigkeit","Klettergeschwindigkeit.",0.1,2.0,0.1,0.5)
            );
            case "Jesus" -> List.of(
                    SettingSpec.choice("mode","Modus","Wasserbewegungsmodus.","Solid","Solid","Bounce","Dolphin")
            );
            case "Inventory Move" -> List.of(
                    SettingSpec.bool("jump","Springen","Springen in Inventar erlauben.",true),
                    SettingSpec.bool("sprint","Sprint","Sprinten in Inventar erlauben.",true)
            );
            case "Sneak" -> List.of(
                    SettingSpec.choice("mode","Modus","Sneak-Modus.","Always","Always","Edge","Toggle")
            );
            case "Anti Void" -> List.of(
                    SettingSpec.integer("fallDistance","Fall Distanz","Ab welcher Distanz reagieren.",2,30,1,8),
                    SettingSpec.choice("action","Aktion","Reaktion auf Void.","Stop","Stop","Jump","Disconnect")
            );
            case "Auto Walk" -> List.of(
                    SettingSpec.bool("sprint","Sprint","Beim AutoWalk sprinten.",true)
            );
            case "Bunny Hop" -> List.of(
                    SettingSpec.decimal("speed","Speed","BHop-Geschwindigkeit.",1.0,4.0,0.1,1.4),
                    SettingSpec.bool("autoJump","Auto Jump","Automatisch springen.",true)
            );
            case "Strafe" -> List.of(
                    SettingSpec.decimal("airControl","Air Control","Kontrolle in der Luft.",0.0,1.0,0.05,0.7)
            );
            case "Timer" -> List.of(
                    SettingSpec.decimal("multiplier","Multiplier","Client-Timing-Multiplikator.",0.5,2.0,0.05,1.0)
            );

            case "Auto Fish" -> List.of(
                    SettingSpec.integer("recastDelay","Recast Delay","Ticks bis zum erneuten Auswerfen.",1,40,1,8),
                    SettingSpec.bool("soundDetect","Sound Detect","Biss über Sound erkennen.",true)
            );
            case "Auto Eat" -> List.of(
                    SettingSpec.integer("hunger","Hunger Trigger","Ab diesem Hungerwert essen.",1,19,1,14),
                    SettingSpec.bool("goldenApples","Goldäpfel","Goldäpfel automatisch verwenden.",false)
            );
            case "Auto Tool" -> List.of(
                    SettingSpec.bool("silkTouch","Silk Touch","Silk Touch bevorzugen wenn passend.",false),
                    SettingSpec.bool("fortune","Fortune","Fortune bevorzugen wenn passend.",true)
            );
            case "Auto Respawn" -> List.of(
                    SettingSpec.integer("delay","Delay","Ticks bis zum Respawn.",0,100,5,10)
            );
            case "Auto Reconnect" -> List.of(
                    SettingSpec.integer("delay","Delay","Sekunden bis zum Reconnect.",1,60,1,5),
                    SettingSpec.integer("maxAttempts","Max. Versuche","Maximale Reconnect-Versuche.",1,20,1,5)
            );
            case "Anti AFK" -> List.of(
                    SettingSpec.integer("interval","Intervall","Sekunden zwischen Aktionen.",5,300,5,45),
                    SettingSpec.choice("action","Aktion","AFK-Aktion.","Jump","Jump","Turn","Walk")
            );
            case "Inventory Cleaner" -> List.of(
                    SettingSpec.integer("delay","Delay","Ticks zwischen Aufräumaktionen.",1,20,1,4),
                    SettingSpec.bool("keepTools","Tools behalten","Werkzeuge nicht entfernen.",true),
                    SettingSpec.bool("keepFood","Essen behalten","Nahrung nicht entfernen.",true)
            );
            case "Chest Stealer" -> List.of(
                    SettingSpec.integer("delay","Delay","Ticks zwischen Transfers.",0,20,1,2),
                    SettingSpec.bool("smart","Smart","Unnötige Items überspringen.",true)
            );
            case "Fast Place" -> List.of(
                    SettingSpec.integer("delay","Place Delay","Placement-Delay.",0,4,1,0)
            );
            case "Fast Use" -> List.of(
                    SettingSpec.integer("delay","Use Delay","Item-Use-Delay.",0,20,1,2)
            );
            case "Auto Drop" -> List.of(
                    SettingSpec.choice("mode","Modus","Drop-Regel.","Whitelist","Whitelist","Blacklist"),
                    SettingSpec.integer("delay","Delay","Ticks zwischen Drops.",1,20,1,4)
            );
            case "Auto Craft" -> List.of(
                    SettingSpec.integer("delay","Craft Delay","Ticks zwischen Craft-Aktionen.",1,20,1,3),
                    SettingSpec.bool("repeat","Wiederholen","Rezept wiederholt craften.",true)
            );
            case "Middle Click Friend" -> List.of(
                    SettingSpec.bool("notify","Hinweis","Änderung im Chat anzeigen.",true)
            );
            case "Chat Timestamp" -> List.of(
                    SettingSpec.choice("format","Format","Zeitformat.","24h","24h","12h"),
                    SettingSpec.bool("seconds","Sekunden","Sekunden anzeigen.",false)
            );
            case "Chat Filter" -> List.of(
                    SettingSpec.bool("caseSensitive","Groß/Klein","Groß-/Kleinschreibung beachten.",false),
                    SettingSpec.bool("hideMatched","Ausblenden","Treffer komplett ausblenden.",true)
            );
            case "Auto Reply" -> List.of(
                    SettingSpec.integer("cooldown","Cooldown","Sekunden zwischen Antworten.",1,120,1,10),
                    SettingSpec.bool("privateOnly","Nur privat","Nur auf private Nachrichten reagieren.",true)
            );
            case "Announcer" -> List.of(
                    SettingSpec.bool("kills","Kills","Kill-Events melden.",false),
                    SettingSpec.bool("distance","Distanz","Gelaufene Distanz melden.",false)
            );
            case "Screenshot Helper" -> List.of(
                    SettingSpec.bool("timestamp","Zeitstempel","Zeitstempel im Dateinamen.",true),
                    SettingSpec.bool("notify","Hinweis","Speicherpfad anzeigen.",true)
            );
            case "Session Info" -> List.of(
                    SettingSpec.bool("playtime","Spielzeit","Session-Zeit anzeigen.",true),
                    SettingSpec.bool("kills","Kills","Kill-Zähler anzeigen.",true),
                    SettingSpec.bool("deaths","Tode","Death-Zähler anzeigen.",true)
            );
            case "Auto GG" -> List.of(
                    SettingSpec.integer("cooldown","Cooldown","Sekunden zwischen Nachrichten.",1,120,1,10),
                    SettingSpec.choice("style","Text","Nachrichtenstil.","GG","GG","gg","Good game")
            );
            case "Durability Alert" -> List.of(
                    SettingSpec.integer("percent","Warnschwelle","Warnung unter dieser Haltbarkeit.",1,100,5,20),
                    SettingSpec.bool("sound","Ton","Warnton abspielen.",true)
            );
            case "Low Health Alert" -> List.of(
                    SettingSpec.decimal("health","Warnschwelle","Warnung unter diesem Leben.",1.0,20.0,0.5,6.0),
                    SettingSpec.bool("sound","Ton","Warnton abspielen.",true)
            );
            case "Pearl Cooldown" -> List.of(
                    SettingSpec.choice("position","Position","HUD-Position.","Bottom Center","Top Left","Top Right","Bottom Center"),
                    SettingSpec.bool("seconds","Sekunden","Restzeit numerisch anzeigen.",true)
            );
            case "Totem Pop Counter" -> List.of(
                    SettingSpec.bool("chat","Chat","Pops im Chat melden.",false),
                    SettingSpec.bool("hud","HUD","Pop-Zähler im HUD anzeigen.",true)
            );
            case "Auto Leave" -> List.of(
                    SettingSpec.decimal("health","Health Trigger","Unter diesem Leben verlassen.",1.0,20.0,0.5,4.0),
                    SettingSpec.bool("totems","Keine Totems","Verlassen wenn keine Totems mehr da sind.",false)
            );

            case "Watermark" -> List.of(
                    SettingSpec.choice("position","Position","Watermark-Position.","Top Left","Top Left","Top Right","Bottom Left","Bottom Right"),
                    SettingSpec.bool("version","Version","Client-Version anzeigen.",true)
            );
            case "Module List" -> List.of(
                    SettingSpec.choice("position","Position","Position der aktiven Module.","Top Right","Top Left","Top Right","Bottom Left","Bottom Right"),
                    SettingSpec.bool("sortLength","Nach Länge","Module nach Textlänge sortieren.",true),
                    SettingSpec.bool("background","Hintergrund","Hintergrund anzeigen.",false)
            );
            case "Font Settings" -> List.of(
                    SettingSpec.decimal("scale","Schriftgröße","Globale Schrift-Skalierung.",0.75,1.5,0.05,1.0),
                    SettingSpec.bool("bold","Bold","Fette Überschriften verwenden.",true)
            );
            case "Blur Settings" -> List.of(
                    SettingSpec.decimal("strength","Blur Stärke","Stärke des Hintergrund-Blurs.",0.0,2.0,0.1,0.8)
            );
            case "Background Effects" -> List.of(
                    SettingSpec.decimal("glow","Glow","Hintergrund-Glow.",0.0,2.0,0.1,1.0),
                    SettingSpec.bool("particles","Partikel","Hintergrundpartikel anzeigen.",false)
            );
            case "Color Manager" -> List.of(
                    SettingSpec.choice("preset","Preset","Globales Farb-Preset.","Pink","Pink","Purple","Blue","Green"),
                    SettingSpec.bool("syncModules","Module synchronisieren","Modulfarben ans Theme koppeln.",true)
            );
            case "Notification Style" -> List.of(
                    SettingSpec.choice("position","Position","Position der Hinweise.","Top Right","Top Left","Top Right","Bottom Left","Bottom Right"),
                    SettingSpec.decimal("duration","Dauer","Anzeigedauer.",0.5,6.0,0.5,2.5)
            );
            case "Search Settings" -> List.of(
                    SettingSpec.bool("fuzzy","Fuzzy Search","Ähnliche Modulnamen finden.",true),
                    SettingSpec.bool("descriptions","Beschreibungen","Auch Beschreibungen durchsuchen.",true)
            );
            case "Compact Mode" -> List.of(
                    SettingSpec.bool("enabledLayout","Compact Layout","Kompakteres GUI-Layout verwenden.",true),
                    SettingSpec.integer("columns","Spalten","Anzahl der Modulspalten.",1,3,1,2)
            );

            case "Staff List" -> List.of(
                    SettingSpec.integer("maxRows","Max. Einträge","Maximal angezeigte Staff-Einträge.",1,20,1,8),
                    SettingSpec.bool("hideEmpty","Leer ausblenden","Panel ausblenden wenn niemand erkannt wird.",true),
                    SettingSpec.bool("rankHeuristic","Rang-Erkennung","Admin/Mod/Staff/Helper-Tags erkennen.",true)
            );
            case "RTP Base Alert" -> List.of(
                    SettingSpec.integer("teleportDistance","Teleport Distanz","Mindestdistanz für RTP-Erkennung.",64,5000,64,256),
                    SettingSpec.integer("scanSeconds","Scan Dauer","Sekunden nach RTP nach Basis-Hinweisen suchen.",5,120,5,30),
                    SettingSpec.integer("storageThreshold","Storage Schwelle","Ab dieser Storage-Anzahl alarmieren.",1,32,1,4),
                    SettingSpec.bool("playerAlert","Spieler Warnung","Nahe Spieler zusätzlich melden.",true)
            );
            case "AutoAuctionFlipper" -> List.of(
                    SettingSpec.integer("minMargin","Min. Marge %","Mindestmarge für lokale Preis-Hinweise.",1,200,5,20),
                    SettingSpec.integer("maxPrice","Max. Preis","Lokales Preislimit für beobachtete Angebote.",1000,100000000,1000,1000000),
                    SettingSpec.bool("notifyOnly","Nur Hinweis","Keine automatischen Käufe oder Verkäufe.",true)
            );
            case "AutoSell" -> List.of(
                    SettingSpec.integer("delay","Delay","Ticks zwischen vorbereiteten Sell-Aktionen.",1,40,1,8),
                    SettingSpec.bool("confirm","Bestätigen","Vor einer Sell-Aktion Bestätigung verlangen.",true)
            );
            case "GamblingRigger" -> List.of(
                    SettingSpec.bool("localPreview","Nur lokal","Nur lokale Anzeige ohne Manipulation.",true)
            );
            case "FakePay" -> List.of(
                    SettingSpec.bool("localOnly","Nur lokal","Keine Nachricht an den Server senden.",true),
                    SettingSpec.integer("amount","Preview Betrag","Lokaler Vorschau-Betrag.",1,1000000,100,10000)
            );
            case "FakeStats" -> List.of(
                    SettingSpec.integer("money","Money Preview","Lokaler Money-Vorschauwert.",0,100000000,1000,1000000),
                    SettingSpec.integer("kills","Kills Preview","Lokaler Kills-Vorschauwert.",0,100000,10,100),
                    SettingSpec.bool("hud","HUD","Lokale Vorschau als HUD anzeigen.",true)
            );
            case "SpawnerProtect" -> List.of(
                    SettingSpec.integer("spawnerRange","Spawner Reichweite","Radius um geladene Spawner.",4,64,4,20),
                    SettingSpec.integer("playerRange","Spieler Reichweite","Abstand anderer Spieler für Warnung.",8,128,8,48),
                    SettingSpec.bool("disconnect","Disconnect","Bei Gefahr automatisch trennen.",false),
                    SettingSpec.bool("hud","HUD Warnung","Warnung im HUD anzeigen.",true)
            );

            default -> List.of(
                    SettingSpec.bool("enabledExtra", "Zusatzoption", "Modulspezifische Zusatzfunktion.", true)
            );
        };
    }

    private ModuleSettingsRegistry() {}
}
