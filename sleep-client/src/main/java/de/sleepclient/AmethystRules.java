package de.sleepclient;

/** Exact block evidence, shared by scanning and verification. */
public final class AmethystRules {
    public static int stage(String blockId) {
        return switch (blockId) {
            case "minecraft:small_amethyst_bud" -> 1;
            case "minecraft:medium_amethyst_bud" -> 2;
            case "minecraft:large_amethyst_bud" -> 3;
            case "minecraft:amethyst_cluster" -> 4;
            default -> 0;
        };
    }

    public static boolean matches(int actualBuds, int minimum) {
        return actualBuds >= Math.max(1, minimum);
    }

    public static boolean withinRadius(int x, int z, int centerX, int centerZ, int radius) {
        return Math.abs((long) x - centerX) <= radius && Math.abs((long) z - centerZ) <= radius;
    }

    public static double chunkStart(int coordinate) { return coordinate * 16.0; }

    private AmethystRules() {}
}
