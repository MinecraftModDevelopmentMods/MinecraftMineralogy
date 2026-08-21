package zone.moddev.mc.mineralogy;

import org.apache.commons.lang3.text.WordUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zone.moddev.mc.mineralogy.blocks.*;
import zone.moddev.mc.mineralogy.documentation.DocumentationExporter;
import zone.moddev.mc.mineralogy.itemblock.BypassItemBlock;
import zone.moddev.mc.mineralogy.items.*;
import zone.moddev.mc.mineralogy.fluids.MineralogyFluids;
import zone.moddev.mc.mineralogy.migration.LegacyOreConfigMigrator;
import zone.moddev.mc.mineralogy.patching.PatchHandler;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.File;
import java.util.*;

@Mod(
        modid = Mineralogy.MODID,
        name=Mineralogy.NAME,
        version = Mineralogy.VERSION,
        acceptedMinecraftVersions = "[1.10.2]",
        dependencies = "required-after:orespawn@[4.0.6,5.0.0)",
        certificateFingerprint = "")
public class Mineralogy {

    public static final String MODID = "mineralogy";
    public static final String NAME ="Mineralogy";
    public static final String VERSION = "6.0.1.110021";

    /**
     * Compatibility-facing construction tab. It is initialized from config in
     * pre-init so grouped mode does not leave an empty legacy main tab behind.
     */
    public static CreativeTabs mineralogyTab;
    /** all blocks used in this mod (blockID, block)*/
    public static final Map<String,Block> mineralogyBlockRegistry = new HashMap<String, Block>();
    /** all items used in this mod (blockID, block)*/
    public static final Map<String,Item> mineralogyItemRegistry = new HashMap<String, Item>();

    public static boolean SMELTABLE_GRAVEL = true;
    public static boolean DROP_COBBLESTONE = false;
    public static boolean PATCH_UPDATE = true;

 //   public static OrePlacer orePlacementGenerator = null;
    public static boolean GENERATE_ROCKSTAIRS = true;
    public static boolean GENERATE_ROCKSLAB = true;
    public static boolean GENERATE_ROCK_WALL = true;
    public static boolean GENERATE_BRICK = true;
    public static boolean GENERATE_BRICKSTAIRS = true;
    public static boolean GENERATE_BRICKSLAB = true;
    public static boolean GENERATE_BRICK_WALL = true;
    public static boolean GENERATE_SMOOTH = true;
    public static boolean GENERATE_SMOOTHSTAIRS = true;
    public static boolean GENERATE_SMOOTHSLAB = true;
    public static boolean GENERATE_SMOOTH_WALL = true;
    public static boolean GENERATE_SMOOTHBRICK = true;
    public static boolean GENERATE_SMOOTHBRICKSTAIRS = true;
    public static boolean GENERATE_SMOOTHBRICKSLAB = true;
    public static boolean GENERATE_SMOOTHBRICK_WALL = true;
    public static boolean GENERATE_ROCKFURNACES = true;
    public static boolean GENERATE_SMOOTHFURNACES = true;
    public static boolean GENERATE_BRICKFURNACES = true;
    public static boolean GENERATE_SMOOTHBRICKFURNACES = true;
    public static boolean GENERATE_RELIEFS = true;

    private static ContentPolicy contentPolicy = ContentPolicy.defaults();
    private static OreDictionaryPolicy oreDictionaryPolicy = OreDictionaryPolicy.defaults();
    private static CreativeTabPolicy creativeTabPolicy = CreativeTabPolicy.defaults();

    public static Block blockChert;
    public static Block blockGypsum;
    public static Block blockChalk;
    public static Block blockSalt;
    public static Block blockPumice;
    public static Block blockRockSaltLamp;
    public static Block blockRockSaltStreetLamp;
    public static Item gypsumPowder;
    public static Item chalkPowder;
    public static Item saltPowder;
    public static Item rockSaltPowder;
    public static Item sulphurPowder;
    public static Item phosphorousPowder;
    public static Item nitratePowder; // aka "saltpeter"
    public static Item mineralFertilizer;

    public static Block[] drywall = new Block[16];

    // add other blocks and recipes
    private static final String[] colorSuffixes = { "black", "red", "green", "brown", "blue", "purple", "cyan",
            "silver", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white" };

    String[] colorSuffixesTwo = { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan",
            "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White" };


    private static final String stickWood = "stickWood";
    private static final String cobblestone = "cobblestone";
    private static final String fertilizer = "fertilizer";
    //private static final String salt = "salt";
    private static final String stone = "stone";
    private static final String dustCarbon = "dustCarbon";
    private static final String dustCoal = "dustCoal";
    //private static final String blockPhosphorous = "blockPhosphorous";
    private static final String dustPhosphorous = "dustPhosphorous";
    private static final String orePhosphorous = "orePhosphorous";
    //private static final String blockSulfur = "blockSulfur";
    private static final String dustSulfur = "dustSulfur";
    private static final String oreSulfur = "oreSulfur";
    private static final String sulfur = "sulfur";
    private static final String dustSulphur = "dustSulphur";
    private static final String oreSulphur = "oreSulphur";
    private static final String sulphur = "sulphur";
    private static final String dustGypsum = "dustGypsum";
    private static final String dustChalk = "dustChalk";
    //private static final String dustSalt = "dustSalt";
    private static final String dustRocksalt = "dustRocksalt";
    //private static final String blockNitrate = "blockNitrate";
    private static final String dustNitrate = "dustNitrate";
    private static final String oreNitrate = "oreNitrate";

    public static final Logger logger = LogManager.getFormatterLogger(Mineralogy.MODID);

    @EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        logger.warn("Invalid fingerprint detected!");
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        final File configFile = event.getSuggestedConfigurationFile();
        final boolean configWasPresent = configFile.isFile();
        Configuration config = new Configuration(configFile);
        config.load();
        if (configWasPresent) {
            LegacyOreConfigMigrator.migrate(configFile, logger);
        }

        contentPolicy = ContentPolicy.read(config);
        oreDictionaryPolicy = OreDictionaryPolicy.read(config);
        creativeTabPolicy = CreativeTabPolicy.read(config);
        MineralogyCreativeTabs.configure(creativeTabPolicy);
        mineralogyTab = MineralogyCreativeTabs.constructionTab();
        DocumentationExporter.exportBundledGuide();

        PATCH_UPDATE = config.getBoolean("patch_world", "options", PATCH_UPDATE,
                "If true, then the world will be patched to fix compatibility-breaking " +
                "changes to this mod by adding-back mock-ups of old obsolete blocks and " +
                "then replacing obsolete blocks with newer blocks.");

        SMELTABLE_GRAVEL = config.getBoolean("SMELTABLE_GRAVEL", "options", SMELTABLE_GRAVEL,
   "If true, then gravel can be smelted into generic stone");
           DROP_COBBLESTONE = config.getBoolean("DROP_COBBLESTONE", "options", DROP_COBBLESTONE,
   "If true, then rock blocks will drop cobblestone instead of themselves");

        GENERATE_ROCKSTAIRS = config.getBoolean("GENERATE_ROCKSTAIRS", "options", GENERATE_ROCKSTAIRS, "If true, then rock stairs will be generated");
        GENERATE_ROCKSLAB = config.getBoolean("GENERATE_ROCKSLAB", "options", GENERATE_ROCKSLAB, "If true, then rock slabs will be generated");
        GENERATE_ROCK_WALL = config.getBoolean("GENERATE_ROCK_WALL", "options", GENERATE_ROCK_WALL, "If true, then rock walls will be generated");
        GENERATE_BRICK = config.getBoolean("GENERATE_BRICK", "options", GENERATE_BRICK, "If true, then rock brick blocks will be generated");
        GENERATE_BRICKSTAIRS = config.getBoolean("GENERATE_BRICKSTAIRS", "options", GENERATE_BRICKSTAIRS, "If true, then brick stairs will be generated");
        GENERATE_BRICKSLAB = config.getBoolean("GENERATE_BRICKSLAB", "options", GENERATE_BRICKSLAB, "If true, then brick slabs will be generated");
        GENERATE_BRICK_WALL = config.getBoolean("GENERATE_BRICK_WALL", "options", GENERATE_BRICK_WALL, "If true, then brick walls will be generated");
        GENERATE_SMOOTH = config.getBoolean("GENERATE_SMOOTH", "options", GENERATE_SMOOTH, "If true, then polished rock will be generated");
        GENERATE_SMOOTHSTAIRS = config.getBoolean("GENERATE_SMOOTHSTAIRS", "options", GENERATE_SMOOTHSTAIRS, "If true, then polished rock stairs will be generated");
        GENERATE_SMOOTHSLAB = config.getBoolean("GENERATE_SMOOTHSLAB", "options", GENERATE_SMOOTHSLAB, "If true, then polished rock slabs will be generated");
        GENERATE_SMOOTH_WALL= config.getBoolean("GENERATE_SMOOTH_WALL", "options", GENERATE_SMOOTH_WALL, "If true, then polished rock walls will be generated");
        GENERATE_SMOOTHBRICK = config.getBoolean("GENERATE_SMOOTHBRICK", "options", GENERATE_SMOOTHBRICK, "If true, then polished brick blocks will be generated");
        GENERATE_SMOOTHBRICKSTAIRS = config.getBoolean("GENERATE_SMOOTHBRICKSTAIRS", "options", GENERATE_SMOOTHBRICKSTAIRS, "If true, then polished brick stairs will be generated");
        GENERATE_SMOOTHBRICKSLAB = config.getBoolean("GENERATE_SMOOTHBRICKSLAB", "options", GENERATE_SMOOTHBRICKSLAB, "If true, then polished brick slabs will be generated");
        GENERATE_SMOOTHBRICK_WALL = config.getBoolean("GENERATE_SMOOTHBRICK_WALL", "options", GENERATE_SMOOTHBRICK_WALL, "If true, then polished brick walls will be generated");

        GENERATE_ROCKFURNACES = config.getBoolean("GENERATE_ROCKFURNACES", "options", GENERATE_ROCKFURNACES, "If true, then rock furnaces will be generated");
        GENERATE_SMOOTHFURNACES = config.getBoolean("GENERATE_SMOOTHFURNACES", "options", GENERATE_SMOOTHFURNACES, "If true, then polished rock furnaces will be generated");
        GENERATE_BRICKFURNACES = config.getBoolean("GENERATE_BRICKFURNACES", "options", GENERATE_BRICKFURNACES, "If true, then brick furnaces will be generated");
        GENERATE_SMOOTHBRICKFURNACES = config.getBoolean("GENERATE_SMOOTHBRICKFURNACES", "options", GENERATE_SMOOTHBRICKFURNACES, "If true, then polished brick furnaces will be generated");
        GENERATE_RELIEFS = config.getBoolean("GENERATE_RELIEFS", "options", GENERATE_RELIEFS, "If true, then reliefs will be generated");

        // Blocks and items

        MineralogyFluids.register();
        if (event.getSide().isClient()) {
            MineralogyFluids.registerClientModels();
        }

        // Rocks
        addStoneType("diabase", 5, 100, 2); // new
        addStoneType("gabbro", 5, 100, 2); // new
        addStoneType("peridotite", 3, 15, 0); // new
        addStoneType("basaltic_glass", 3, 15, 0); // new ?
        addStoneType("scoria", 1, 7, 0);// new
        addStoneType("tuff", 2, 10, 0);// new

        addStoneType("andesite", 1.5, 10, 0);
        addStoneType("basalt", 5, 100, 2);
        addStoneType("diorite", 1.5, 10, 0);
        addStoneType("granite", 3, 15, 1);
        addStoneType("rhyolite", 1.5, 10, 0);
        addStoneType("pegmatite", 1.5, 10, 0);


        addStoneType("siltstone", 1, 10, 0);// new // TODO it should crush to sand and clay

        addStoneType("shale", 1.5, 10, 0);
        addStoneType("conglomerate" ,1.5, 10, 0);
        addStoneType("dolomite", 3, 15, 1);
        addStoneType("limestone", 1.5, 10, 0);

        addStoneType("hornfels", 3, 15, 1);// new
        addStoneType("quartzite", 4, 15, 1);// new
        addStoneType("novaculite", 3, 15, 1);// new // TODO: this can be used like flint

        addStoneType("slate", 1.5, 10, 0);
        addStoneType("schist", 3, 15, 1);
        addStoneType("gneiss", 3, 15, 1);
        addStoneType("marble", 1.5, 10, 0);
        addStoneType("phyllite", 1.5, 10, 0);
        addStoneType("amphibolite", 3, 15, 1);

        // add items
        gypsumPowder = addDust("gypsum_dust", "Gypsum");
        chalkPowder = addDust("chalk_dust", "Chalk");
        saltPowder = addDust("salt_dust", "Salt");
        rockSaltPowder = addDust("rock_salt_dust", "Rocksalt");

        OreDictionary.registerOre("salt", saltPowder);

        sulphurPowder = addDust("sulfur_dust", "Sulfur", contentPolicy.mineralDustsEnabled());

        OreDictionary.registerOre(sulfur, sulphurPowder);
        OreDictionary.registerOre(dustSulphur, sulphurPowder);
        OreDictionary.registerOre(sulphur, sulphurPowder);

        phosphorousPowder = addDust("phosphorous_dust", "Phosphorous", contentPolicy.mineralDustsEnabled());

        nitratePowder = addDust("nitrate_dust", "Nitrate", contentPolicy.mineralDustsEnabled());

        mineralFertilizer = registerItem(new MineralFertilizer(), "mineral_fertilizer")
                .setUnlocalizedName(Mineralogy.MODID + "." + "mineral_fertilizer")
                .setCreativeTab(contentPolicy.mineralFertilizerEnabled()
                        ? MineralogyCreativeTabs.forItem(CreativeTabs.MATERIALS) : null);
        OreDictionary.registerOre(fertilizer, mineralFertilizer);

        // other blocks

        blockChert = registerBlock(new Chert(), "chert");
        OreDictionary.registerOre(cobblestone, blockChert);

        blockGypsum = registerBlock(new Gypsum(), "gypsum");
        OreDictionary.registerOre("blockGypsum", blockGypsum);

        blockChalk = registerBlock(new Chalk(), "chalk");
        OreDictionary.registerOre("blockChalk", blockChalk);

        blockSalt = registerBlock(new RockSalt(), "rock_salt");
        OreDictionary.registerOre("blockRocksalt", blockSalt);

        addStoneType("rock_salt", 1.5, 10, 0, true, blockSalt);// new

        blockRockSaltLamp = registerBlock(new RockSaltLamp(), "rocksaltlamp");
        blockRockSaltStreetLamp = registerBlock(new RockSaltStreetLamp(), "rocksaltstreetlamp", 16);
        OreDictionary.registerOre("lampRocksalt", blockRockSaltLamp);
        OreDictionary.registerOre("lampRocksaltStreet", blockRockSaltStreetLamp);
        applyCreativeVisibility(blockRockSaltLamp, "rocksaltlamp", contentPolicy.rockSaltLampsEnabled());
        applyCreativeVisibility(blockRockSaltStreetLamp, "rocksaltstreetlamp", contentPolicy.rockSaltLampsEnabled());

        blockPumice = registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.STONE), "pumice");
        OreDictionary.registerOre(cobblestone, blockPumice);

        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(gypsumPowder, 4), blockGypsum));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(chalkPowder, 4), blockChalk));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(saltPowder, 4), blockSalt));
        if (contentPolicy.rockSaltLampsEnabled()) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(blockRockSaltLamp, 1), blockSalt, Blocks.TORCH, Items.IRON_INGOT));
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockGypsum), "xx", "xx", 'x', dustGypsum));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockChalk), "xx", "xx", 'x', dustChalk));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockSalt), "xx", "xx", 'x', dustRocksalt));
        if (contentPolicy.rockSaltLampsEnabled()) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockRockSaltStreetLamp), "x", "y", "y", 'x', blockRockSaltLamp, 'y', Items.IRON_INGOT));
        }


        // OreSpawn owns placement; Mineralogy registers only the stable ore content.
        Block s = addOre("sulfur_ore", oreSulfur, sulphurPowder, 1, 4, 0);
        OreDictionary.registerOre(oreSulphur, s);
        addOre("phosphorous_ore", orePhosphorous, phosphorousPowder, 1, 4, 0);
        addOre("nitrate_ore", oreNitrate, nitratePowder, 1, 4, 0);

        // TODO: Finish This
        addBlock("sulfur_block", "Sulfur", 0, contentPolicy.mineralDustsEnabled());
        addBlock("phosphorous_block", "Phosphorous", 0, contentPolicy.mineralDustsEnabled());
        addBlock("nitrate_block", "Nitrate", 0, contentPolicy.mineralDustsEnabled());

        if (!configWasPresent) {
            config.save();
        }

        for(int i = 0; i < 16; i++) {
            drywall[i] = registerBlock(new DryWall(colorSuffixes[i]), "drywall_" + colorSuffixes[i]);
            applyCreativeVisibility(drywall[i], "drywall_" + colorSuffixes[i], contentPolicy.drywallsEnabled());
            OreDictionary.registerOre("drywall", drywall[i]);
        }

        if (contentPolicy.drywallsEnabled()) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(drywall[7], 3), "pgp", "pgp", "pgp", 'p', Items.PAPER, 'g', dustGypsum));
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        // recipes
        if (contentPolicy.drywallsEnabled()) {
            for(int i = 0; i < 16; i++) {
                GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(drywall[i], 1), "drywall", "dye" + colorSuffixesTwo[i]));
            }
        }

        if (contentPolicy.mineralDustsEnabled()) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.GUNPOWDER, 4), new ItemStack(Items.COAL,1,1), dustNitrate, dustSulfur));
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.GUNPOWDER, 4), dustCarbon, dustNitrate, dustSulfur));
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.GUNPOWDER, 4), dustCoal, dustNitrate, dustSulfur));
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.GUNPOWDER, 4), Items.SUGAR, dustNitrate, dustSulfur));
        }
        if (contentPolicy.mineralFertilizerEnabled()) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(mineralFertilizer, 1), dustNitrate, dustPhosphorous));
        }

        // recipe modifications
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.STONE_AXE), "xx", "xy", " y", 'x', stone, 'y', stickWood));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.STONE_HOE), "xx", " y", " y", 'x', stone, 'y', stickWood));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.STONE_PICKAXE), "xxx", " y "," y ", 'x', stone, 'y',stickWood));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.STONE_SHOVEL), "x", "y", "y", 'x', stone, 'y', stickWood));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.STONE_SWORD), "x", "x", "y", 'x', stone, 'y', stickWood));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.FURNACE), "xxx", "x x", "xxx", 'x', stone));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Blocks.COBBLESTONE, 4), stone, stone, Blocks.GRAVEL, Blocks.GRAVEL));

        if(SMELTABLE_GRAVEL) GameRegistry.addSmelting(Blocks.GRAVEL, new ItemStack(Blocks.STONE), 0.1F);

        // initialize legacy updater
        PatchHandler.getInstance().init(PATCH_UPDATE);

        // event registration, tile entities
        // (none)

        // register renderers
        if(event.getSide().isClient()) {
            registerItemRenders();
        }
    }

    private void registerItemRenders() {

        for(String name : mineralogyItemRegistry.keySet()){
            Item i = Mineralogy.mineralogyItemRegistry.get(name);
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
                    .register(i, 0, new ModelResourceLocation(Mineralogy.MODID + ":" + name, "inventory"));
        }
    }

    private static Item addDust(String dustName, String oreDictionaryName) {
        return addDust(dustName, oreDictionaryName, true);
    }

    private static Item addDust(String dustName, String oreDictionaryName, boolean enabled) {
        Item item = registerItem(new Item(), dustName).setUnlocalizedName(Mineralogy.MODID + "." + dustName)
                .setCreativeTab(enabled ? mineralogyTab : null);
        OreDictionary.registerOre("dust" + oreDictionaryName, item);
        if (enabled) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(item, 9), "block" + oreDictionaryName));
        }
        return item;
    }

    // TODO: Recipes
    private static Block addBlock(String name, String oreDictionaryName, int pickLevel) {
        return addBlock(name, oreDictionaryName, pickLevel, true);
    }

    private static Block addBlock(String name, String oreDictionaryName, int pickLevel, boolean enabled) {
        String blockName = Mineralogy.MODID + "." + name;
        Block block = new Block(Material.ROCK).setUnlocalizedName(blockName)
                .setCreativeTab(enabled ? mineralogyTab : null);
        registerBlock(block, name);
        applyCreativeVisibility(block, name, enabled);
        OreDictionary.registerOre("block" + oreDictionaryName, block);
        if (enabled) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(block), "xxx", "xxx", "xxx", 'x', "dust" + oreDictionaryName));
        }

        return block;
    }

    private static void applyCreativeVisibility(Block block, String name, boolean enabled) {
        if (enabled) {
            return;
        }
        block.setCreativeTab(null);
        Item item = mineralogyItemRegistry.get(name);
        if (item != null) {
            item.setCreativeTab(null);
        }
    }

    private static Block addOre(String oreName, String oreDictionaryName, Item oreDropItem, int numMin, int numMax, int pickLevel) {
        String oreBlockName = Mineralogy.MODID + "." + oreName;
        Block oreBlock = new Ore(oreName, oreDropItem, numMin, numMax, pickLevel).setUnlocalizedName(oreBlockName);
        registerBlock(oreBlock, oreName);
        OreDictionary.registerOre(oreDictionaryName, oreBlock);
        return oreBlock;
    }

    private static Block registerBlock(Block b, String name, int maxStackSize, boolean createItem, boolean bypassSneak) {
        GameRegistry.register(b.setRegistryName(MODID, name));
        b.setUnlocalizedName(MODID + "." + name);

        if (createItem) {
            b.setCreativeTab(MineralogyCreativeTabs.forBlock(b));
        }

        ItemBlock itemBlock;

        if (bypassSneak)
            itemBlock = new BypassItemBlock(b);
        else
            itemBlock = new ItemBlock(b);

        itemBlock.setMaxStackSize(maxStackSize);

        if (createItem) {
            itemBlock.setCreativeTab(MineralogyCreativeTabs.forBlock(b));
            registerItem(itemBlock, name);
        }

        mineralogyBlockRegistry.put(name, b);
        return b;
    }

    private static Block registerBlock(Block b, String name, int maxStackSize) {
        return registerBlock(b, name, maxStackSize, true, false);
    }

    private static Block registerBlock(Block b, String name) {
        return registerBlock(b, name, 64);
    }

    private static Item registerItem(Item i, String name) {
        GameRegistry.register(i.setRegistryName(MODID, name));
        mineralogyItemRegistry.put(name, i);
        i.setUnlocalizedName(MODID + "." + name);
        return i;
    }

    private static Block addStoneType(String name, double hardness, double blastResistance, int toolHardnessLevel) {

        return addStoneType(name, hardness, blastResistance, toolHardnessLevel, true, null);
    }

    /**
     *
     * @param name id-name of the block
     * @param hardness How hard (time duration) the block is to pick. For reference, dirt is 0.5, stone is 1.5, ores are 3, and obsidian is 50
     * @param blastResistance how resistant the block is to explosions. For reference, dirt is 0, stone is 10, and blast-proof materials are 2000
     * @param toolHardnessLevel 0 for wood tools, 1 for stone, 2 for iron, 3 for diamond
     */
    private static Block addStoneType(String name, double hardness, double blastResistance, int toolHardnessLevel, Boolean canBePolished, Block rockOverride) { //, ItemStack drops
        final Block rock;
        Block rockStairs = null;
        Block rockSlab = null;
        Block rockWall = null;
        Block rockFurnace = null;
        Block brick = null;
        Block brickStairs = null;
        Block brickSlab = null;
        Block brickWall = null;
        Block brickFurnace = null;
        Block smooth = null;
        Block smoothStairs = null;
        Block smoothSlab = null;
        Block smoothWall = null;
        Block smoothFurnace = null;
        Block smoothBrick = null;
        Block smoothBrickStairs = null;
        Block smoothBrickSlab = null;
        Block smoothBrickWall = null;
        Block smoothBrickFurnace = null;

        float burnModifier = (float) (1 + ((hardness - 3) / 10));

        if (rockOverride != null)
            rock = rockOverride;
        else
            rock = registerBlock(new Rock(true, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name);

        String oreDictName = WordUtils.capitalize(name);

        OreDictionary.registerOre("stone", rock);
        OreDictionary.registerOre("stone" + oreDictName, rock);
        if (oreDictionaryPolicy.cobblestoneEquivalentEnabled()) {
            OreDictionary.registerOre(cobblestone, rock);
        }

        GameRegistry.addSmelting(rock, new ItemStack(Blocks.STONE), 0.1F);

        if (GENERATE_ROCKSTAIRS) {
            rockStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_stairs");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rockStairs, 4), "x  ", "xx ", "xxx", 'x', rock));
        }

        if (GENERATE_ROCKSLAB) {
            rockSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, name + "_double_slab"), name + "_slab", 64, true, true);
            GameRegistry.addShapedRecipe(new ItemStack(rockSlab, 6),
                    "xxx", 'x', new ItemStack(rock, 1, 0));

            registerBlock(
                    new DoubleSlab((float) hardness, (float) blastResistance, toolHardnessLevel,
                            SoundType.STONE, rockSlab, rock),
                    name + "_double_slab",  64, false, true);

            if (GENERATE_ROCKFURNACES) {
                rockFurnace = registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, false, burnModifier), name + "_furnace");
                registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_furnace", 64, false, false);
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rockFurnace, 1), "xxx", "xyx", "xxx", 'x', rockSlab, 'y', Blocks.FURNACE));
                GameRegistry.registerTileEntity(TileEntityRockFurnace.class, name + "_furnace");
            }
        }

        if (GENERATE_ROCK_WALL) {
            rockWall = registerBlock(new RockWall(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_wall");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rockWall, 6), "xxx", "xxx", 'x', rock));
        }

        if (GENERATE_BRICK) {
            brick = registerBlock(new Rock(false, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_brick");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(brick, 4), "xx", "xx", 'x', rock));

            if (GENERATE_BRICKSTAIRS) {
                brickStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance,toolHardnessLevel, SoundType.STONE),name + "_brick_stairs");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(brickStairs, 4), "x  ", "xx ", "xxx", 'x', brick));
            }

            if (GENERATE_BRICKSLAB) {
                brickSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, name + "_brick_double_slab"), name + "_brick_slab", 64, true, true);
                GameRegistry.addShapedRecipe(new ItemStack(brickSlab, 6),
                        "xxx", 'x', new ItemStack(brick, 1, 0));

                registerBlock(
                        new DoubleSlab((float) hardness, (float) blastResistance, toolHardnessLevel,
                                SoundType.STONE, brickSlab, brick),
                        name + "_brick_double_slab",  64, false, true);

                if (GENERATE_BRICKFURNACES) {
                    brickFurnace = registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, false, burnModifier), name + "_brick_furnace");
                    registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_brick_furnace", 64, false, false);
                    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(brickFurnace, 1), "xxx", "xyx", "xxx", 'x', brickSlab, 'y', Blocks.FURNACE));
                    GameRegistry.registerTileEntity(TileEntityRockFurnace.class, name + "_brick_furnace");
                }
            }

            if (GENERATE_BRICK_WALL) {
                brickWall = registerBlock(new RockWall(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_brick_wall");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(brickWall, 6), "xxx", "xxx", 'x', brick));
            }
        }

        if (GENERATE_SMOOTH && canBePolished) {
            smooth = registerBlock(new Rock(false, (float)hardness,(float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth");
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(smooth, 1), rock, "sand"));

            if(GENERATE_RELIEFS) {
                generateReliefs(name, hardness, blastResistance, toolHardnessLevel, smooth);
            }
//			if (hardness >= 4) {
//				smoothAnvil = registerBlock(new RockAnvil(name + "_smooth_anvil", (float)hardness, (float)blastResistance, toolHardnessLevel), name + "_smooth_anvil");
//				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothAnvil, 1), "xxx", "xxx", "xxx", 'x', smooth));
//			}

            if (GENERATE_SMOOTHSTAIRS) {
                smoothStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE),name + "_smooth_stairs");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothStairs, 4), "x  ","xx ", "xxx", 'x', smooth));
            }

            if (GENERATE_SMOOTHSLAB) {
                smoothSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, name + "_smooth_double_slab"),name + "_smooth_slab", 64, true, true);
                GameRegistry.addShapedRecipe(new ItemStack(smoothSlab, 6),
                        "xxx", 'x', new ItemStack(smooth, 1, 0));

                registerBlock(
                        new DoubleSlab((float) hardness, (float) blastResistance, toolHardnessLevel,
                                SoundType.STONE, smoothSlab, smooth),
                        name + "_smooth_double_slab",  64, false, true);

                if (GENERATE_SMOOTHFURNACES) {
                    smoothFurnace = registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, false, burnModifier), name + "_smooth_furnace");
                    registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_smooth_furnace", 64, false, false);
                    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothFurnace, 1), "xxx", "xyx", "xxx", 'x', smoothSlab, 'y', Blocks.FURNACE));
                    GameRegistry.registerTileEntity(TileEntityRockFurnace.class, name + "_smooth_furnace");
                }
            }

            if (GENERATE_SMOOTH_WALL) {
                smoothWall = registerBlock(new RockWall(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth_wall");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothWall, 6), "xxx", "xxx", 'x', smooth));
            }

            if (GENERATE_SMOOTHBRICK) {
                smoothBrick = registerBlock(new Rock(false, (float)hardness,(float)blastResistance,toolHardnessLevel, SoundType.STONE), name + "_smooth_brick");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothBrick, 4), "xx", "xx", 'x', smooth));

                if (GENERATE_SMOOTHBRICKSTAIRS) {
                    smoothBrickStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth_brick_stairs");
                    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothBrickStairs, 4), "x  ","xx ", "xxx", 'x', smoothBrick));
                }

                if (GENERATE_SMOOTHBRICKSLAB) {
                    smoothBrickSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, name + "_smooth_brick_double_slab"), name + "_smooth_brick_slab", 64, true, true);
                    GameRegistry.addShapedRecipe(new ItemStack(smoothBrickSlab, 6),
                            "xxx", 'x', new ItemStack(smoothBrick, 1, 0));

                    registerBlock(
                            new DoubleSlab((float) hardness, (float) blastResistance, toolHardnessLevel,
                                    SoundType.STONE, smoothBrickSlab, smoothBrick),
                            name + "_smooth_brick_double_slab",  64, false, true);

                    if (GENERATE_SMOOTHBRICKFURNACES) {
                        smoothBrickFurnace = registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, false, burnModifier), name + "_smooth_brick_furnace");
                        registerBlock(new RockFurnace((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_smooth_brick_furnace", 64, false, false);
                        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothBrickFurnace, 1), "xxx", "xyx", "xxx", 'x', smoothBrickSlab, 'y', Blocks.FURNACE));
                        GameRegistry.registerTileEntity(TileEntityRockFurnace.class, name + "_smooth_brick_furnace");
                    }
                }

                if (GENERATE_SMOOTHBRICK_WALL) {
                    smoothBrickWall = registerBlock(new RockWall(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth_brick_wall");
                    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(smoothBrickWall, 6), "xxx", "xxx", 'x', smoothBrick));
                }
            }
        }

        ConstructionRecipeHelper.registerConvenienceRecipes(
                new ConstructionRecipeHelper.Forms(rock, rockStairs, rockSlab, rockWall),
                new ConstructionRecipeHelper.Forms(brick, brickStairs, brickSlab, brickWall),
                new ConstructionRecipeHelper.Forms(smooth, smoothStairs, smoothSlab, smoothWall),
                new ConstructionRecipeHelper.Forms(smoothBrick, smoothBrickStairs,
                        smoothBrickSlab, smoothBrickWall));

        return rock;
    }

    private static void generateReliefs(String materialName, double hardness, double blastResistance,
            int toolHardnessLevel, final Block rock) {
        final Block blankRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_blank");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blankRelief, 16), "xxx","xxx","xxx",'x', rock));

        final Block axeRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_axe");
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(axeRelief, 8), blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, Items.STONE_AXE));

        final Block crossRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_cross");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(crossRelief, 4), "x x","   ","x x",'x', blankRelief));

        final Block hammerRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_hammer");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(hammerRelief, 7), "zxz","zyz","zzz",'x', rock,'y', Items.STICK,'z', blankRelief));

        final Block hoeRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_hoe");
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(hoeRelief, 8), blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, Items.STONE_HOE));

        final Block horizontalRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_horizontal");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(horizontalRelief, 3), "xxx",'x', blankRelief));

        final Block leftRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_left");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(leftRelief, 3), "x  "," x ","  x",'x', blankRelief));

        final Block pickaxeRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_pickaxe");
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(pickaxeRelief, 8), blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, Items.STONE_PICKAXE));

        final Block plusRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_plus");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(plusRelief, 5), " x ","xxx"," x ",'x', blankRelief));

        final Block rightRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_right");
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(rightRelief, 2), leftRelief, leftRelief));

        final Block swordRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_sword");
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(swordRelief, 8), blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, blankRelief, Items.STONE_SWORD));

        final Block iRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_i");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(iRelief, 7), "xxx"," x ","xxx",'x', blankRelief));

        final Block verticalRelief = registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_vertical");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(verticalRelief, 3), "x","x","x",'x', blankRelief));
    }
}
