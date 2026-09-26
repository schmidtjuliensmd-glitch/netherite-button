import de.sleepclient.AmethystRules;
import de.sleepclient.ModuleCategory;
import de.sleepclient.ModuleSettingsRegistry;

public final class ChunkFinderChecks {
    private static int checks;

    private static void check(boolean value, String description) {
        if (!value) throw new AssertionError(description);
        checks++;
    }

    private static int detected(String... blockIds) {
        int count = 0;
        for (String blockId : blockIds) if (AmethystRules.stage(blockId) > 0) count++;
        return count;
    }

    public static void main(String[] args) {
        String[] stages = {"small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud", "amethyst_cluster"};
        for (String stage : stages) {
            check(AmethystRules.matches(detected("minecraft:" + stage), 1), "Each reference growth stage must trigger: " + stage);
        }
        String[] unrelated = {"air", "stone", "chest", "trapped_chest", "barrel", "shulker_box", "hopper",
                "furnace", "blast_furnace", "smoker", "ender_chest", "brewing_stand", "beacon", "spawner",
                "amethyst_block", "budding_amethyst"};
        for (String block : unrelated) {
            check(!AmethystRules.matches(detected("minecraft:" + block), 1), "Unrelated block must not trigger: " + block);
        }
        check(!AmethystRules.matches(detected("minecraft:chest", "minecraft:hopper", "minecraft:beacon"), 1), "Storage-only chunk must not trigger");
        check(!AmethystRules.matches(0, 0), "A zero threshold cannot create evidence");
        check(!AmethystRules.matches(0, -8), "A corrupt threshold cannot create evidence");
        check(!AmethystRules.matches(3, 4), "Respect configured minimum");
        check(AmethystRules.matches(4, 4), "Trigger exactly at configured minimum");
        check(AmethystRules.stage("other:amethyst_cluster") == 0, "Do not match another namespace by appearance");
        check(AmethystRules.withinRadius(-8, -8, -1, -1, 7), "Negative-coordinate radius edge");
        check(!AmethystRules.withinRadius(-9, -8, -1, -1, 7), "Exclude old chunks outside the radius");
        check(!AmethystRules.withinRadius(Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 0, 12), "Avoid coordinate overflow");
        check(AmethystRules.chunkStart(-1) == -16 && AmethystRules.chunkStart(0) == 0, "Align negative chunk plates");
        check(AmethystRules.chunkStart(27) - AmethystRules.chunkStart(26) == 16, "Plate width must be 16 blocks");
        for (String module : new String[]{"ChunkFinder", "Sus Chunk Finder", "ChunkFinderV2", "SusChunkFinderV2"}) {
            var settings = ModuleSettingsRegistry.settingsFor(new de.sleepclient.Module(module, "", ModuleCategory.DONUT_SMP));
            var height = settings.stream().filter(s -> s.key().equals("plateHeight")).findFirst().orElseThrow();
            check(height.min() == -64 && height.max() == 320 && height.defaultValue().equals("64"), module + " exposes absolute plate Y");
            check(settings.stream().noneMatch(s -> s.key().equals("minScore") || s.key().equals("confidence")), module + " must not offer storage scores as evidence");
        }
        System.out.println(checks + " chunk finder checks passed.");
    }
}
