package zone.moddev.mc.mineralogy;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/** Content-only Mineralogy configuration. OreSpawn owns all world-generation settings. */
public final class MineralogyConfig {
    private static final String OPTIONS = "options";

    private static boolean smeltableGravel = true;
    private static boolean dropCobblestone;
    private static boolean patchUpdate = true;
    private static boolean generateReliefs = true;
    private static boolean generateRockStairs = true;
    private static boolean generateRockFurnace = true;
    private static boolean generateRockSlab = true;
    private static boolean generateRockWall = true;
    private static boolean generateBrick = true;
    private static boolean generateBrickFurnace = true;
    private static boolean generateBrickStairs = true;
    private static boolean generateBrickSlab = true;
    private static boolean generateBrickWall = true;
    private static boolean generateSmooth = true;
    private static boolean generateSmoothFurnace = true;
    private static boolean generateSmoothStairs = true;
    private static boolean generateSmoothSlab = true;
    private static boolean generateSmoothWall = true;
    private static boolean generateSmoothBrick = true;
    private static boolean generateSmoothBrickFurnace = true;
    private static boolean generateSmoothBrickStairs = true;
    private static boolean generateSmoothBrickSlab = true;
    private static boolean generateSmoothBrickWall = true;

    private static ContentPolicy contentPolicy = ContentPolicy.defaults();
    private static OreDictionaryPolicy oreDictionaryPolicy = OreDictionaryPolicy.defaults();
    private static CreativeTabPolicy creativeTabPolicy = CreativeTabPolicy.defaults();
    private static Configuration config;

    private MineralogyConfig() {
    }

    public static void preInit(FMLPreInitializationEvent event) {
        load(event.getSuggestedConfigurationFile());
    }

    static void load(File file) {
        boolean existing = file.isFile();
        Configuration loaded = new Configuration(file);
        loaded.load();
        apply(loaded, existing);
    }

    static void apply(Configuration loaded, boolean existing) {
        config = loaded;

        patchUpdate = option("patch_world", patchUpdate,
                "Patch old block aliases so established Mineralogy worlds remain loadable.");
        smeltableGravel = option("SMELTABLE_GRAVEL", smeltableGravel,
                "Allow gravel to be smelted into vanilla stone.");
        dropCobblestone = option("DROP_COBBLESTONE", dropCobblestone,
                "Make raw Mineralogy rocks drop cobblestone instead of themselves.");

        generateReliefs = option("GENERATE_RELIEFS", generateReliefs, "Register rock relief blocks.");
        generateRockStairs = option("GENERATE_ROCKSTAIRS", generateRockStairs, "Register raw rock stairs.");
        generateRockFurnace = option("GENERATE_ROCKFURNACE", generateRockFurnace, "Register raw rock furnaces.");
        generateRockSlab = option("GENERATE_ROCKSLAB", generateRockSlab, "Register raw rock slabs.");
        generateRockWall = option("GENERATE_ROCKWALL", generateRockWall, "Register raw rock walls.");
        generateBrick = option("GENERATE_BRICK", generateBrick, "Register rock brick blocks.");
        generateBrickFurnace = option("GENERATE_BRICKFURNACE", generateBrickFurnace, "Register brick furnaces.");
        generateBrickStairs = option("GENERATE_BRICKSTAIRS", generateBrickStairs, "Register brick stairs.");
        generateBrickSlab = option("GENERATE_BRICKSLAB", generateBrickSlab, "Register brick slabs.");
        generateBrickWall = option("GENERATE_BRICKWALL", generateBrickWall, "Register brick walls.");
        generateSmooth = option("GENERATE_SMOOTH", generateSmooth, "Register polished rock blocks.");
        generateSmoothFurnace = option("GENERATE_SMOOTHFURNACE", generateSmoothFurnace, "Register polished furnaces.");
        generateSmoothStairs = option("GENERATE_SMOOTHSTAIRS", generateSmoothStairs, "Register polished stairs.");
        generateSmoothSlab = option("GENERATE_SMOOTHSLAB", generateSmoothSlab, "Register polished slabs.");
        generateSmoothWall = option("GENERATE_SMOOTHWALL", generateSmoothWall, "Register polished walls.");
        generateSmoothBrick = option("GENERATE_SMOOTHBRICK", generateSmoothBrick, "Register polished brick blocks.");
        generateSmoothBrickFurnace = option("GENERATE_SMOOTHBRICKFURNACE", generateSmoothBrickFurnace,
                "Register polished brick furnaces.");
        generateSmoothBrickStairs = option("GENERATE_SMOOTHBRICKSTAIRS", generateSmoothBrickStairs,
                "Register polished brick stairs.");
        generateSmoothBrickSlab = option("GENERATE_SMOOTHBRICKSLAB", generateSmoothBrickSlab,
                "Register polished brick slabs.");
        generateSmoothBrickWall = option("GENERATE_SMOOTHBRICKWALL", generateSmoothBrickWall,
                "Register polished brick walls.");

        contentPolicy = ContentPolicy.read(config);
        oreDictionaryPolicy = OreDictionaryPolicy.read(config);
        creativeTabPolicy = CreativeTabPolicy.read(config);
        if (!existing) {
            config.save();
        }
    }

    private static boolean option(String key, boolean fallback, String comment) {
        return config.getBoolean(key, OPTIONS, fallback, comment);
    }

    public static boolean smeltableGravel() { return smeltableGravel; }
    public static boolean dropCobblestone() { return dropCobblestone; }
    public static boolean patchUpdate() { return patchUpdate; }
    public static boolean generateReliefs() { return generateReliefs; }
    public static boolean generateRockStairs() { return generateRockStairs; }
    public static boolean generateRockFurnace() { return generateRockFurnace; }
    public static boolean generateRockSlab() { return generateRockSlab; }
    public static boolean generateRockWall() { return generateRockWall; }
    public static boolean generateBrick() { return generateBrick; }
    public static boolean generateBrickFurnace() { return generateBrickFurnace; }
    public static boolean generateBrickStairs() { return generateBrickStairs; }
    public static boolean generateBrickSlab() { return generateBrickSlab; }
    public static boolean generateBrickWall() { return generateBrickWall; }
    public static boolean generateSmooth() { return generateSmooth; }
    public static boolean generateSmoothFurnace() { return generateSmoothFurnace; }
    public static boolean generateSmoothStairs() { return generateSmoothStairs; }
    public static boolean generateSmoothSlab() { return generateSmoothSlab; }
    public static boolean generateSmoothWall() { return generateSmoothWall; }
    public static boolean generateSmoothBrick() { return generateSmoothBrick; }
    public static boolean generateSmoothBrickFurnace() { return generateSmoothBrickFurnace; }
    public static boolean generateSmoothBrickStairs() { return generateSmoothBrickStairs; }
    public static boolean generateSmoothBrickSlab() { return generateSmoothBrickSlab; }
    public static boolean generateSmoothBrickWall() { return generateSmoothBrickWall; }
    public static boolean makeRockCobblestoneEquivilent() { return oreDictionaryPolicy.cobblestoneEquivalentEnabled(); }
    public static boolean groupCreativeTabItemsByType() { return creativeTabPolicy.groupTabsByType(); }
    public static String creativeTabName(Block block) {
        if (!groupCreativeTabItemsByType()) {
            return Mineralogy.MODID;
        }
        String lower = creativeTabPolicy.groupFor(block).name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
    public static String itemCreativeTabName() {
        return groupCreativeTabItemsByType() ? "Item" : Mineralogy.MODID;
    }
    public static boolean isCreativeVisible(String name) {
        if (name.startsWith("drywall_")) {
            return contentPolicy.drywallsEnabled();
        }
        if ("rocksaltlamp".equals(name) || "rocksaltstreetlamp".equals(name)) {
            return contentPolicy.rockSaltLampsEnabled();
        }
        if ("sulfur_dust".equals(name) || "phosphorous_dust".equals(name)
                || "nitrate_dust".equals(name) || "sulfur_block".equals(name)
                || "phosphorous_block".equals(name) || "nitrate_block".equals(name)) {
            return contentPolicy.mineralDustsEnabled();
        }
        return !"mineral_fertilizer".equals(name) || contentPolicy.mineralFertilizerEnabled();
    }
    public static ContentPolicy contentPolicy() { return contentPolicy; }
    static CreativeTabPolicy creativeTabPolicy() { return creativeTabPolicy; }
    public static Configuration config() { return config; }
}
