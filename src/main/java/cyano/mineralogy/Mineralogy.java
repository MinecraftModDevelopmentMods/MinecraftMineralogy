package cyano.mineralogy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DON'T FORGET TO UPDATE mcmod.info FILE!!!

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cyano.mineralogy.blocks.Chert;
import cyano.mineralogy.blocks.DryWall;
import cyano.mineralogy.blocks.Gypsum;
import cyano.mineralogy.blocks.Ore;
import cyano.mineralogy.blocks.Rock;
import cyano.mineralogy.blocks.RockSlab;
import cyano.mineralogy.blocks.RockStairs;
import cyano.mineralogy.items.MineralFertilizer;
import cyano.mineralogy.patching.PatchHandler;
import cyano.mineralogy.util.BlockItemPair;
import cyano.mineralogy.worldgen.OreSpawner;
import cyano.mineralogy.worldgen.StoneReplacer;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(
		modid = Mineralogy.MODID,
		name = Mineralogy.NAME,
		version = Mineralogy.VERSION,
		acceptedMinecraftVersions = "[1.12,)",
		certificateFingerprint = "@FINGERPRINT@")
public class Mineralogy {

	@Instance
	public static Mineralogy instance;

	/** ID of this Mod */
	public static final String MODID = "mineralogy";

	/** Display name of this Mod */
	public static final String NAME = "Mineralogy";

	/**
	 * Version number, in Major.Minor.Patch format. The minor number is
	 * increased whenever a change is made that has the potential to break
	 * compatibility with other mods that depend on this one.
	 */
	public static final String VERSION = "3.3.0";

	public static final Logger logger = LogManager.getFormatterLogger(Mineralogy.MODID);

	public static final CreativeTabs mineralogyTab = new MineralogyCreativeTab("tabMineralogy");

	public static final List<Block> sedimentaryStones = new ArrayList<>(); // stone block replacements that are Sedimentary
	public static final List<Block> metamorphicStones = new ArrayList<>(); // stone block replacements that are Metamorphic
	public static final List<Block> igneousStones = new ArrayList<>(); // stone block replacements that are Igneous
	public static final Map<String, BlockItemPair> MineralogyBlockRegistry = new HashMap<>(); // all blocks used in this mod (blockID, BlockItemPair)
	protected static final Map<String, Item> MineralogyItemRegistry = new HashMap<>(); // all items used in this mod (itemID, item)
	protected static final Map<String, IRecipe> MineralogyRecipeRegistry = new HashMap<>(); // all recipes used in this mod (recipeID, IRecipe)

	protected static final Map<String, Block> BlocksToRegister = new HashMap<>(); // all blocks used in this mod (blockID, BlockItemPair)
	protected static final Map<String, Item> ItemsToRegister = new HashMap<>(); // all items used in this mod (itemID, item)

	protected static BlockItemPair blockChert;
	protected static BlockItemPair blockGypsum;
	public static BlockItemPair blockPumice;

	public static Item gypsumPowder;
	protected static Item sulphurPowder;
	protected static Item phosphorousPowder;
	protected static Item nitratePowder; // aka "saltpeter"
	protected static Item mineralFertilizer;

	protected static BlockItemPair[] drywalls = new BlockItemPair[16];

	// add other blocks and recipes
	private static final String[] colorSuffixes = { 
													"black",
													"red",
													"green",
													"brown",
													"blue",
													"purple",
													"cyan",
													"silver",
													"gray",
													"pink",
													"lime",
													"yellow",
													"light_blue",
													"magenta",
													"orange",
													"white"
													};
	private static final String[] colorSuffixesTwo = {
													"Black",
													"Red",
													"Green",
													"Brown",
													"Blue",
													"Purple",
													"Cyan",
													"LightGray",
													"Gray",
													"Pink",
													"Lime",
													"Yellow",
													"LightBlue",
													"Magenta",
													"Orange",
													"White"
													};

	private static final String GYPSUM = "Gypsum";
	private static final String PHOSPHOROUS = "Phosphorous";
	private static final String NITRATE = "Nitrate";
	private static final String SULFUR = "Sulfur";
	private static final String STAIRS = "stairs";
	private static final String SLAB = "slab";
	private static final String ORE = "ore";
	private static final String BLOCK = "Block";
	private static final String DUST = "dust";
	private static final String GUNPOWDER = "GUNPOWDER";
	private static final String DRYWALL = "drywall";
	private static final String PUMICE = "pumice";
	private static final String CHERT = "chert";
	private static final String SMOOTH = "smooth";
	private static final String BRICK = "brick";

	private static final String COBBLESTONE = "cobblestone";
	private static final String FERTILIZER = "fertilizer";

	@Mod.EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		logger.warn("Invalid fingerprint detected!");
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new MineralogyEventBusSubscriber());

		MineralogyConfig.preInit(event);

		// Blocks, Items, World-gen

		// Rocks
		addStoneType(RockType.IGNEOUS, "Andesite", 1.5, 10, 0);
		addStoneType(RockType.IGNEOUS, "Basalt", 5, 100, 2);
		addStoneType(RockType.IGNEOUS, "Diorite", 1.5, 10, 0);
		addStoneType(RockType.IGNEOUS, "Granite", 3, 15, 1);
		addStoneType(RockType.IGNEOUS, "Rhyolite", 1.5, 10, 0);
		addStoneType(RockType.IGNEOUS, "Pegmatite", 1.5, 10, 0);

		addStoneType(RockType.SEDIMENTARY, "Shale", 1.5, 10, 0);
		addStoneType(RockType.SEDIMENTARY, "Conglomerate", 1.5, 10, 0);
		addStoneType(RockType.SEDIMENTARY, "Dolomite", 3, 15, 1);
		addStoneType(RockType.SEDIMENTARY, "Limestone", 1.5, 10, 0);

		addStoneType(RockType.METAMORPHIC, "Slate", 1.5, 10, 0);
		addStoneType(RockType.METAMORPHIC, "Schist", 3, 15, 1);
		addStoneType(RockType.METAMORPHIC, "Gneiss", 3, 15, 1);
		addStoneType(RockType.SEDIMENTARY, "Marble", 1.5, 10, 0);
		addStoneType(RockType.METAMORPHIC, "Phyllite", 1.5, 10, 0);
		addStoneType(RockType.METAMORPHIC, "Amphibolite", 3, 15, 1);

		// add items
		gypsumPowder = addDust(GYPSUM);
		sulphurPowder = addDust(SULFUR);
		phosphorousPowder = addDust(PHOSPHOROUS);
		nitratePowder = addDust(NITRATE);

		mineralFertilizer = registerItem(new MineralFertilizer(), "mineral_fertilizer")
				.setUnlocalizedName(Mineralogy.MODID + "." + "mineral_fertilizer");

		ItemsToRegister.put(FERTILIZER, mineralFertilizer);

		// other blocks
		sedimentaryStones.add(Blocks.SANDSTONE);

		blockChert = registerBlock(new Chert(), CHERT, "blockChert");
		sedimentaryStones.add(blockChert.PairedBlock);

		blockGypsum = registerBlock(new Gypsum(), GYPSUM.toLowerCase(), "blockGypsum");
		sedimentaryStones.add(blockGypsum.PairedBlock);

		addShapedOreRecipe(GYPSUM.toLowerCase(), new ItemStack(blockGypsum.PairedItem, 1), "xxx", "xxx", "xxx", 'x',
				gypsumPowder);
		addShapelessOreRecipe(GYPSUM.toLowerCase() + "_dust", new ItemStack(gypsumPowder, 9),
				Ingredient.fromStacks(new ItemStack(blockGypsum.PairedItem)));

		blockPumice = registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.GROUND), PUMICE, "blockPumice");
		igneousStones.add(blockPumice.PairedBlock);

		final String ORES = "ores";

		// register ores
		addOre(SULFUR, sulphurPowder, 1, 4, 0,
				MineralogyConfig.config().getInt("sulphur_ore.minY", ORES, 16, 1, 255, "Minimum ore spawn height"),
				MineralogyConfig.config().getInt("sulphur_ore.maxY", ORES, 64, 1, 255, "Maximum ore spawn height"),
				MineralogyConfig.config().getFloat("sulphur_ore.frequency", ORES, 1, 0, 63, "Number of ore deposits per chunk"),
				MineralogyConfig.config().getInt("sulphur_ore.quantity", ORES, 16, 0, 63, "Size of ore deposit"));

		addOre(PHOSPHOROUS, phosphorousPowder, 1, 4, 0,
				MineralogyConfig.config().getInt("phosphorous_ore.minY", ORES, 16, 1, 255, "Minimum ore spawn height"),
				MineralogyConfig.config().getInt("phosphorous_ore.maxY", ORES, 64, 1, 255, "Maximum ore spawn height"),
				MineralogyConfig.config().getFloat("phosphorous_ore.frequency", ORES, 1, 0, 63, "Number of ore deposits per chunk"),
				MineralogyConfig.config().getInt("phosphorous_ore.quantity", ORES, 16, 0, 63, "Size of ore deposit"));

		addOre(NITRATE, nitratePowder, 1, 4, 0,
				MineralogyConfig.config().getInt("nitrate_ore.minY", ORES, 16, 1, 255, "Minimum ore spawn height"),
				MineralogyConfig.config().getInt("nitrate_ore.maxY", ORES, 64, 1, 255, "Maximum ore spawn height"),
				MineralogyConfig.config().getFloat("nitrate_ore.frequency", ORES, 1, 0, 63, "Number of ore deposits per chunk"),
				MineralogyConfig.config().getInt("nitrate_ore.quantity", ORES, 16, 0, 63, "Size of ore deposit"));

		MineralogyConfig.config().save();

		for (int i = 0; i < 16; i++) {
			drywalls[i] = registerBlock(new DryWall(colorSuffixes[i]), DRYWALL + "_" + colorSuffixes[i],
					DRYWALL + colorSuffixesTwo[i]);
		}

		addShapedOreRecipe(DRYWALL, new ItemStack(drywalls[15].PairedItem, 3), "pgp", "pgp", "pgp", 'p', Items.PAPER,
				'g', gypsumPowder);

		for (int i = 0; i < 16; i++) {
			addShapelessOreRecipe(DRYWALL + "_" + colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
					Ingredient.fromStacks(new ItemStack(drywalls[15].PairedItem)),
					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
		}

		addShapelessOreRecipe(GUNPOWDER + "_FROM_COAL", new ItemStack(Items.GUNPOWDER, 4),
				Ingredient.fromStacks(new ItemStack(Items.COAL)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(sulphurPowder)));

		// TODO: Fix this recipe
		// addShapelessOreRecipe(GUNPOWDER + "_FROM_CARBON", new
		// ItemStack(Items.GUNPOWDER, 4), Ingredient.fromStacks(new
		// ItemStack(carbonDust)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
		// Ingredient.fromStacks(new ItemStack(sulphurPowder)));

		addShapelessOreRecipe(GUNPOWDER + "_FROM_SUGAR", new ItemStack(Items.GUNPOWDER, 4),
				Ingredient.fromStacks(new ItemStack(Items.SUGAR)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(sulphurPowder)));
		addShapelessOreRecipe("mineralFertilizer", new ItemStack(mineralFertilizer, 1),
				Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(phosphorousPowder)));

		addShapelessOreRecipe(COBBLESTONE.toUpperCase(), new ItemStack(Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));
	}

	private static ShapedOreRecipe addShapedOreRecipe(String name, ItemStack output, Object... args) {
		return addShapedOreRecipe(MODID, name, output, args);
	}

	private static ShapedOreRecipe addShapedOreRecipe(String domain, String name, ItemStack output, Object... args) {
		ShapedOreRecipe newRecipe = new ShapedOreRecipe(new ResourceLocation(domain, name), output, args);
		newRecipe.setRegistryName(name);
		MineralogyRecipeRegistry.put(name, newRecipe);
		return newRecipe;
	}

	private static ShapelessOreRecipe addShapelessOreRecipe(String name, ItemStack output, Object... args) {
		return addShapelessOreRecipe(MODID, name, output, args);
	}

	private static ShapelessOreRecipe addShapelessOreRecipe(String domain, String name, ItemStack output, Object... args) {
		ShapelessOreRecipe newRecipe = new ShapelessOreRecipe(new ResourceLocation(domain, name), output, args);
		newRecipe.setRegistryName(name);
		MineralogyRecipeRegistry.put(name, newRecipe);
		return newRecipe;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (MineralogyConfig.smeltableGravel())
			GameRegistry.addSmelting(Blocks.GRAVEL, new ItemStack(Blocks.STONE), 0.1F);

		PatchHandler.getInstance().init(MineralogyConfig.patchUpdate()); // initialize legacy updater

		GameRegistry.registerWorldGenerator(new StoneReplacer(), 10); // register custom chunk generation

		// register renderers
		if (event.getSide().isClient()) {
			registerItemRenders();
		}
	}

	private void registerItemRenders() {

		for (String name : MineralogyItemRegistry.keySet()) {
			Item i = Mineralogy.MineralogyItemRegistry.get(name);
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(i, 0,
					new ModelResourceLocation(Mineralogy.MODID + ":" + name, "inventory"));
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// addons to other mods

		// process black-lists and white-lists
		for (String id : MineralogyConfig.igneousWhitelist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			igneousStones.add(b);
		}
		for (String id : MineralogyConfig.metamorphicWhitelist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			metamorphicStones.add(b);
		}
		for (String id : MineralogyConfig.sedimentaryWhitelist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			sedimentaryStones.add(b);
		}
		for (String id : MineralogyConfig.igneousBlacklist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			igneousStones.remove(b);
		}
		for (String id : MineralogyConfig.metamorphicBlacklist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			metamorphicStones.remove(b);
		}
		for (String id : MineralogyConfig.sedimentaryBlacklist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			sedimentaryStones.remove(b);
		}
	}

	private static Block getBlock(String id) {
		return Block.getBlockFromName(id);
	}

	private static int oreWeightCount = 20;

	private static Item addDust(String oreDictionaryName) {
		String dustName = oreDictionaryName.toLowerCase() + "_" + DUST;

		Item item = registerItem(new Item(), dustName).setUnlocalizedName(Mineralogy.MODID + "." + dustName)
				.setCreativeTab(mineralogyTab);

		ItemsToRegister.put(DUST + oreDictionaryName, item);

		NonNullList<ItemStack> blocks = OreDictionary.getOres(BLOCK.toLowerCase() + oreDictionaryName);

		if (!blocks.isEmpty()) {
			addShapelessOreRecipe(BLOCK.toLowerCase() + oreDictionaryName, new ItemStack(item, 9),
					Ingredient.fromStacks(blocks.get(0)));
		}

		return item;
	}

	// TODO: Recipes
	private static Block addBlock(String oreDictionaryName, int pickLevel, Item dust) {
		String name = oreDictionaryName.toLowerCase() + "_block";

		BlockItemPair pair = registerBlock(new Rock(false, (float) 1.5, (float) 10, 0, SoundType.STONE), name,
				BLOCK.toLowerCase() + oreDictionaryName);
		// BlockItemPair pair = registerBlock(new
		// Block(Material.ROCK).setUnlocalizedName(Mineralogy.MODID + "." + name), name,
		// BLOCK.toLowerCase() + oreDictionaryName);

		addShapedOreRecipe(name, new ItemStack(pair.PairedItem), "xxx", "xxx", "xxx", 'x', dust);
		addShapelessOreRecipe(oreDictionaryName.toLowerCase() + "_dust", new ItemStack(dust, 9),
				Ingredient.fromStacks(new ItemStack(pair.PairedItem)));

		return pair.PairedBlock;
	}

	private static Block addOre(String oreDictionaryName, Item oreDropItem, int numMin, int numMax, int pickLevel, int minY, int maxY, float spawnFrequency, int spawnQuantity) {
		String oreName = oreDictionaryName.toLowerCase() + "_" + ORE;

		Block oreBlock = new Ore(oreName, oreDropItem, numMin, numMax, pickLevel)
				.setUnlocalizedName(Mineralogy.MODID + "." + oreName);

		registerBlock(oreBlock, oreName, ORE + oreDictionaryName);

		GameRegistry.registerWorldGenerator(new OreSpawner(oreBlock, minY, maxY, spawnFrequency, spawnQuantity,
				(oreWeightCount * 25214903917L) + 11L), oreWeightCount++);

		addBlock(oreDictionaryName, 0, oreDropItem);

		return oreBlock;
	}

	protected static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName) {
		block.setUnlocalizedName(MODID + "." + name);
		block.setRegistryName(name);

		Item item = registerItem(new ItemBlock(block), name);
		BlockItemPair pair = new BlockItemPair(block, item);

		BlocksToRegister.put(oreDictionaryName, block);
		MineralogyBlockRegistry.put(name, pair);

		return pair;
	}

	protected static Item registerItem(Item item, String name) {
		String itemName = MODID + "." + name;

		item.setUnlocalizedName(itemName);
		item.setRegistryName(name);
		item.setCreativeTab(mineralogyTab);

		MineralogyItemRegistry.put(name, item);
		return item;
	}

	/**
	 * 
	 * @param type
	 *            Igneous, sedimentary, or metamorphic
	 * @param name
	 *            id-name of the block
	 * @param hardness
	 *            How hard (time duration) the block is to pick. For reference, dirt
	 *            is 0.5, stone is 1.5, ores are 3, and obsidian is 50
	 * @param blastResistance
	 *            how resistant the block is to explosions. For reference, dirt is
	 *            0, stone is 10, and blast-proof materials are 2000
	 * @param toolHardnessLevel
	 *            0 for wood tools, 1 for stone, 2 for iron, 3 for diamond
	 */
	protected static void addStoneType(RockType type, String oreDictName, double hardness, double blastResistance, int toolHardnessLevel) {
		String name = oreDictName.toLowerCase();

		final BlockItemPair rockPair;
		final BlockItemPair rockStairPair;
		final BlockItemPair rockSlabPair;
		final BlockItemPair brickPair;
		final BlockItemPair brickStairPair;
		final BlockItemPair brickSlabPair;
		final BlockItemPair smoothPair;
		final BlockItemPair smoothStairPair;
		final BlockItemPair smoothSlabPair;
		final BlockItemPair smoothBrickPair;
		final BlockItemPair smoothBrickStairPair;
		final BlockItemPair smoothBrickSlabPair;

		rockPair = registerBlock(new Rock(true, (float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE), name, name);

		// TODO: see why this is necessary, the ore dictionary should make this unnecessary?
		addShapedOreRecipe(name + "_STONE_AXE", new ItemStack(Items.STONE_AXE), "xx", "xy", " y", 'x', rockPair.PairedItem, 'y', Items.STICK);
		addShapedOreRecipe(name + "_STONE_HOE", new ItemStack(Items.STONE_HOE), "xx", " y", " y", 'x', rockPair.PairedItem, 'y', Items.STICK);
		addShapedOreRecipe(name + "_STONE_PICKAXE", new ItemStack(Items.STONE_PICKAXE), "xxx", " y ", " y ", 'x', rockPair.PairedItem, 'y', Items.STICK);
		addShapedOreRecipe(name + "_STONE_SHOVEL", new ItemStack(Items.STONE_SHOVEL), "x", "y", "y", 'x', rockPair.PairedItem, 'y', Items.STICK);
		addShapedOreRecipe(name + "_STONE_SWORD", new ItemStack(Items.STONE_SWORD), "x", "x", "y", 'x', rockPair.PairedItem, 'y', Items.STICK);
		addShapedOreRecipe(name + "_FURNACE", new ItemStack(Blocks.FURNACE), "xxx", "x x", "xxx", 'x', rockPair.PairedItem);
		addShapelessOreRecipe(name + "_" + COBBLESTONE.toUpperCase(), new ItemStack(Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));

		BlocksToRegister.put(COBBLESTONE, rockPair.PairedBlock); // register so it can be used in cobblestone recipes

		switch (type) {
			case IGNEOUS:
				igneousStones.add(rockPair.PairedBlock);
				break;
			case METAMORPHIC:
				metamorphicStones.add(rockPair.PairedBlock);
				break;
			case SEDIMENTARY:
				sedimentaryStones.add(rockPair.PairedBlock);
				break;
			case ANY:
				sedimentaryStones.add(rockPair.PairedBlock);
				metamorphicStones.add(rockPair.PairedBlock);
				igneousStones.add(rockPair.PairedBlock);
				break;
		}

		// TODO: See why this doesn't work (recipes still wont work with 'stone')
		// OreDictionary.registerOre("stone", rock);
		GameRegistry.addSmelting(rockPair.PairedBlock, new ItemStack(Blocks.STONE), 0.1F);

		if (MineralogyConfig.generateRockStairs()) {
			rockStairPair = registerBlock(new RockStairs(rockPair.PairedBlock, (float) hardness,
					(float) blastResistance, toolHardnessLevel, SoundType.STONE), name + "_" + STAIRS,
					STAIRS + oreDictName);
			addShapedOreRecipe(name + "_" + STAIRS, new ItemStack(rockStairPair.PairedItem, 4), "x  ", "xx ", "xxx",
					'x', rockPair.PairedItem);
		}

		if (MineralogyConfig.generateRockSlab()) {
			rockSlabPair = registerBlock(
					new RockSlab((float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
					name + "_" + SLAB, SLAB + oreDictName);
			addShapedOreRecipe(name + "_" + SLAB, new ItemStack(rockSlabPair.PairedItem, 6), "xxx", 'x',
					rockPair.PairedItem);
		}

		if (MineralogyConfig.generateBrick()) {
			brickPair = registerBlock(
					new Rock(false, (float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
					name + "_" + BRICK, BRICK + oreDictName);
			addShapedOreRecipe(name + "_" + BRICK, new ItemStack(brickPair.PairedItem, 4), "xx", "xx", 'x',
					rockPair.PairedItem);

			if (MineralogyConfig.generateBrickStairs()) {
				brickStairPair = registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) hardness, (float) blastResistance,
								toolHardnessLevel, SoundType.STONE),
						name + "_" + BRICK + "_" + STAIRS, STAIRS + oreDictName + "Brick");
				addShapedOreRecipe(name + "_" + BRICK + "_" + STAIRS, new ItemStack(brickStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', brickPair.PairedItem);
			}

			if (MineralogyConfig.generateBrickSlab()) {
				brickSlabPair = registerBlock(
						new RockSlab((float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
						name + "_" + BRICK + "_" + SLAB, SLAB + oreDictName + "Brick");
				addShapedOreRecipe(name + "_" + BRICK + "_" + SLAB, new ItemStack(brickSlabPair.PairedItem, 6), "xxx",
						'x', brickPair.PairedItem);
			}
		}

		if (MineralogyConfig.generateSmooth()) {
			smoothPair = registerBlock(
					new Rock(false, (float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
					name + "_" + SMOOTH, SMOOTH + oreDictName);
			addShapelessOreRecipe(name + "_" + SMOOTH, new ItemStack(smoothPair.PairedItem, 1),
					Ingredient.fromStacks(new ItemStack(rockPair.PairedItem, 1)),
					Ingredient.fromStacks(new ItemStack(Blocks.SAND, 1)));

			if (MineralogyConfig.generateSmoothStairs()) {
				smoothStairPair = registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) hardness, (float) blastResistance,
								toolHardnessLevel, SoundType.STONE),
						name + "_" + SMOOTH + "_" + STAIRS, STAIRS + oreDictName + "Smooth");
				addShapedOreRecipe(name + "_" + SMOOTH + "_" + STAIRS, new ItemStack(smoothStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', smoothPair.PairedItem);
			}

			if (MineralogyConfig.generateSmoothSlab()) {
				smoothSlabPair = registerBlock(
						new RockSlab((float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
						name + "_" + SMOOTH + "_" + SLAB, SLAB + oreDictName + "Smooth");
				addShapedOreRecipe(name + "_" + SMOOTH + "_" + SLAB, new ItemStack(smoothSlabPair.PairedItem, 6), "xxx",
						'x', smoothPair.PairedItem);
			}

			if (MineralogyConfig.generateSmoothBrick()) {
				smoothBrickPair = registerBlock(
						new Rock(false, (float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
						name + "_" + SMOOTH + "_" + BRICK, BRICK + oreDictName + "Smooth");
				addShapedOreRecipe(name + "_" + SMOOTH + "_" + BRICK, new ItemStack(smoothBrickPair.PairedItem, 4),
						"xx", "xx", 'x', smoothPair.PairedItem);

				if (MineralogyConfig.generateSmoothBrickStairs()) {
					smoothBrickStairPair = registerBlock(
							new RockStairs(rockPair.PairedBlock, (float) hardness, (float) blastResistance,
									toolHardnessLevel, SoundType.STONE),
							name + "_" + SMOOTH + "_" + BRICK + "_" + STAIRS, STAIRS + oreDictName + "SmoothBrick");
					addShapedOreRecipe(name + "_" + SMOOTH + "_" + BRICK + "_" + STAIRS,
							new ItemStack(smoothBrickStairPair.PairedItem, 4), "x  ", "xx ", "xxx", 'x',
							smoothBrickPair.PairedItem);
				}

				if (MineralogyConfig.generateSmoothBrickSlab()) {
					smoothBrickSlabPair = registerBlock(
							new RockSlab((float) hardness, (float) blastResistance, toolHardnessLevel, SoundType.STONE),
							name + "_" + SMOOTH + "_" + BRICK + "_" + SLAB, SLAB + oreDictName + "SmoothBrick");
					addShapedOreRecipe(name + "_" + SMOOTH + "_" + BRICK + "_" + SLAB,
							new ItemStack(smoothBrickSlabPair.PairedItem, 6), "xxx", 'x', smoothBrickPair.PairedItem);
				}
			}
		}
	}
}
