package cyano.mineralogy;

// DON'T FORGET TO UPDATE mcmod.info FILE!!!

import cyano.mineralogy.blocks.*;
import cyano.mineralogy.items.*;
import cyano.mineralogy.patching.PatchHandler;
import cyano.mineralogy.worldgen.OreSpawner;
import cyano.mineralogy.worldgen.StoneReplacer;
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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;

import java.util.*;

@Mod(modid = Mineralogy.MODID, name=Mineralogy.NAME, version = Mineralogy.VERSION, acceptedMinecraftVersions = "[1.12,)")
public class Mineralogy {

	public static final String MODID = "mineralogy";
    public static final String NAME ="Mineralogy";
    public static final String VERSION = "3.3.0";
    
    public static final List<Block> sedimentaryStones = new ArrayList<Block>(); //stone block replacements that are Sedimentary
    public static final List<Block> metamorphicStones = new ArrayList<Block>(); //stone block replacements that are Metamorphic
    public static final List<Block> igneousStones = new ArrayList<Block>(); //stone block replacements that are Igneous
	public static final Map<String,Block> MineralogyBlockRegistry = new HashMap<String, Block>(); //all blocks used in this mod (blockID, block)
	public static final Map<String,Item> MineralogyItemRegistry = new HashMap<String, Item>(); // all items used in this mod (blockID, block)
    
    public static double ROCK_LAYER_NOISE = 32; // size of rock layers 
    public static int GEOME_SIZE = 100; //size of mineral biomes
    public static int GEOM_LAYER_THICKNESS = 8; //thickness of rock layers

	public static boolean SMELTABLE_GRAVEL = true;
	public static boolean DROP_COBBLESTONE = false;
	public static boolean PATCH_UPDATE = true;

    public static Block blockChert;
    public static Block blockGypsum;
    public static Block blockPumice;
   
    public static Item gypsumPowder;   
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

	private List<String> igneousWhitelist = new ArrayList<String>();
	private List<String> igneousBlacklist = new ArrayList<String>();
	private List<String> sedimentaryWhitelist = new ArrayList<String>();
	private List<String> sedimentaryBlacklist = new ArrayList<String>();
	private List<String> metamorphicWhitelist = new ArrayList<String>();
	private List<String> metamorphicBlacklist = new ArrayList<String>();

	private static final String stickWood = "stickWood";
	private static final String cobblestone = "cobblestone";
	private static final String fertilizer = "fertilizer";
	private static final String stone = "stone";
	private static final String dustCarbon = "dustCarbon";
	private static final String blockPhosphorous = "phosphorous_block";
	private static final String dustPhosphorous = "phosphorous_dust";
	private static final String orePhosphorous = "orePhosphorous";
	private static final String blockSulfur = "sulfur_block";
	private static final String dustSulfur = "sulfur_dust";
	private static final String oreSulfur = "oreSulfur";
	private static final String sulfur = "sulfur";
	private static final String dustSulphur = "dustSulphur";
	private static final String oreSulphur = "oreSulphur";
	private static final String sulphur = "sulphur";
	private static final String dustGypsum = "gypsum_dust";
	private static final String blockNitrate = "nitrate_block";
	private static final String dustNitrate = "nitrate_dust";
	private static final String oreNitrate = "oreNitrate";

	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {
	
    	// load config
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    	config.load();

		PATCH_UPDATE = config.getBoolean("patch_world", "options", PATCH_UPDATE,
				"If true, then the world will be patched to fix compatibility-breaking " +
				"changes to this mod by adding-back mock-ups of old obsolete blocks and " +
				"then replacing obsolete blocks with newer blocks.");

    	SMELTABLE_GRAVEL = config.getBoolean("SMELTABLE_GRAVEL", "options", SMELTABLE_GRAVEL, "If true, then gravel can be smelted into generic stone");
   		DROP_COBBLESTONE = config.getBoolean("DROP_COBBLESTONE", "options", DROP_COBBLESTONE, "If true, then rock blocks will drop cobblestone instead of themselves");
    	GEOME_SIZE = config.getInt("GEOME_SIZE", "world-gen", GEOME_SIZE, 4, Short.MAX_VALUE, "Making this value larger increases the size of regions of igneous, sedimentary, and metamorphic rocks");
    	ROCK_LAYER_NOISE = (double)config.getFloat("ROCK_LAYER_NOISE", "world-gen", (float)ROCK_LAYER_NOISE, 1.0f, (float)Short.MAX_VALUE, "Changing this value will change the 'waviness' of the layers.");
    	GEOM_LAYER_THICKNESS = config.getInt("ROCK_LAYER_THICKNESS", "world-gen",GEOM_LAYER_THICKNESS, 1, 255, "Changing this value will change the height of individual layers.");

    	igneousBlacklist.addAll(asList(config.getString("igneous_blacklist", "world-gen", "", "Ban blocks from spawning in rock layers (format is mod:block as a semicolin (;) delimited list)"),";"));
    	sedimentaryBlacklist.addAll(asList(config.getString("sedimentary_blacklist", "world-gen", "", "Ban blocks from spawning in rock layers (format is mod:block as a semicolin (;) delimited list)"),";"));
    	metamorphicBlacklist.addAll(asList(config.getString("metamorphic_blacklist", "world-gen", "", "Ban blocks from spawning in rock layers (format is mod:block as a semicolin (;) delimited list)"),";"));
    	
    	igneousWhitelist.addAll(asList(config.getString("igneous_whitelist", "world-gen", "", "Adds blocks to rock layers (format is mod:block as a semicolin (;) delimited list)"),";"));
    	sedimentaryWhitelist.addAll(asList(config.getString("sedimentary_whitelist", "world-gen", "", "Adds blocks to rock layers (format is mod:block as a semicolin (;) delimited list)"),";"));
    	metamorphicWhitelist.addAll(asList(config.getString("metamorphic_whitelist", "world-gen", "", "Adds blocks to rock layers (format is mod:block as a semicolin (;) delimited list)"),";"));

		// Blocks, Items, World-gen

		// Rocks
		addStoneType(RockType.IGNEOUS, "andesite", 1.5, 10, 0);
		addStoneType(RockType.IGNEOUS, "basalt", 5, 100, 2);
		addStoneType(RockType.IGNEOUS, "diorite", 1.5, 10, 0);
		addStoneType(RockType.IGNEOUS, "granite", 3, 15, 1);
		addStoneType(RockType.IGNEOUS, "rhyolite", 1.5, 10, 0);
		addStoneType(RockType.IGNEOUS, "pegmatite", 1.5, 10, 0);

		addStoneType(RockType.SEDIMENTARY, "shale", 1.5, 10, 0);
		addStoneType(RockType.SEDIMENTARY, "conglomerate" ,1.5, 10, 0);
		addStoneType(RockType.SEDIMENTARY, "dolomite", 3, 15, 1);
		addStoneType(RockType.SEDIMENTARY, "limestone", 1.5, 10, 0);

		addStoneType(RockType.METAMORPHIC,"slate", 1.5, 10, 0);
		addStoneType(RockType.METAMORPHIC,"schist", 3, 15, 1);
		addStoneType(RockType.METAMORPHIC,"gneiss", 3, 15, 1);
		addStoneType(RockType.SEDIMENTARY,"marble", 1.5, 10, 0);
		addStoneType(RockType.METAMORPHIC,"phyllite", 1.5, 10, 0);
		addStoneType(RockType.METAMORPHIC,"amphibolite", 3, 15, 1);

		// add items
		gypsumPowder = addDust(dustGypsum, "Gypsum");
		sulphurPowder = addDust(dustSulfur, "Sulfur");
		phosphorousPowder = addDust(dustPhosphorous, "Phosphorous");
		nitratePowder = addDust(dustNitrate, "Nitrate");
		
		OreDictionary.registerOre(sulfur, sulphurPowder);
		OreDictionary.registerOre(dustSulphur, sulphurPowder);
		OreDictionary.registerOre(sulphur, sulphurPowder);	

		mineralFertilizer = registerItem(new MineralFertilizer(), "mineral_fertilizer").setUnlocalizedName(Mineralogy.MODID + "." + "mineral_fertilizer").setCreativeTab(CreativeTabs.MATERIALS);
		
		OreDictionary.registerOre(fertilizer, mineralFertilizer);

		// other blocks
		sedimentaryStones.add(Blocks.SANDSTONE);
		blockChert = registerBlock(new Chert(), "chert");
		sedimentaryStones.add(blockChert);
		blockGypsum = registerBlock(new Gypsum(), "gypsum");
		sedimentaryStones.add(blockGypsum);
		blockPumice = registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.GROUND), "pumice");
		igneousStones.add(blockPumice);
		
		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:GypsumBlock"), new ResourceLocation("mineralogy"), new ItemStack(blockGypsum), Ingredient.fromStacks(new ItemStack(gypsumPowder, 4))); 
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:GypsumBlock"), new ResourceLocation("mineralogy"), new ItemStack(blockGypsum), "xx", "xx", 'x', dustGypsum);

		// TODO: This should probably go in postinit
		// register sedimentary stones in ore dictionary so that they can be used for stone tools recipes 
		for (int i = 0; i < sedimentaryStones.size(); i++) {
			OreDictionary.registerOre(cobblestone, sedimentaryStones.get(i)); 
		}

		// register ores
		Block sulphurBlock = addOre("sulfur_ore", oreSulfur, sulphurPowder,1, 4, 0,
				config.getInt("sulphur_ore.minY", "ores", 16, 1, 255, "Minimum ore spawn height"),
				config.getInt("sulphur_ore.maxY", "ores", 64, 1, 255, "Maximum ore spawn height"),
				config.getFloat("sulphur_ore.frequency", "ores", 1, 0, 63, "Number of ore deposits per chunk"),
				config.getInt("sulphur_ore.quantity", "ores", 16, 0, 63, "Size of ore deposit"));
		
		OreDictionary.registerOre(oreSulphur, sulphurBlock); // Damn English and its multiple spellings. There better not be people out there spelling is "sulphre"
		
		Block phosphorousBlock = addOre("phosphorous_ore", orePhosphorous, phosphorousPowder, 1, 4, 0,
				config.getInt("phosphorous_ore.minY", "ores", 16, 1, 255, "Minimum ore spawn height"),
				config.getInt("phosphorous_ore.maxY", "ores", 64, 1, 255, "Maximum ore spawn height"),
				config.getFloat("phosphorous_ore.frequency", "ores", 1, 0, 63, "Number of ore deposits per chunk"),
				config.getInt("phosphorous_ore.quantity", "ores", 16, 0, 63, "Size of ore deposit"));
		
		OreDictionary.registerOre(orePhosphorous, phosphorousBlock);
		
		Block nitrateBlock = addOre("nitrate_ore", oreNitrate, nitratePowder, 1, 4, 0,
				config.getInt("nitrate_ore.minY", "ores", 16, 1, 255, "Minimum ore spawn height"),
				config.getInt("nitrate_ore.maxY", "ores", 64, 1, 255, "Maximum ore spawn height"),
				config.getFloat("nitrate_ore.frequency", "ores", 1, 0, 63, "Number of ore deposits per chunk"),
				config.getInt("nitrate_ore.quantity", "ores", 16, 0, 63, "Size of ore deposit"));

		OreDictionary.registerOre(oreNitrate, nitrateBlock);
		
		// TODO: Finish This
		addBlock(blockSulfur, "Sulfur", 0);
		addBlock(blockPhosphorous, "Phosphorous", 0);
		addBlock(blockNitrate, "Nitrate", 0);

		config.save();

		for(int i = 0; i < 16; i++) {
			drywall[i] = registerBlock(new DryWall(colorSuffixes[i]), "drywall_" + colorSuffixes[i]);
			OreDictionary.registerOre("drywall", drywall[i]);
		}

		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:DryWall"), new ResourceLocation("mineralogy"), new ItemStack(drywall[7], 3), "pgp", "pgp", "pgp", 'p', Items.PAPER, 'g', dustGypsum);
    }
    
    private static List<String> asList(String list, String delimiter) {
    	String[] a = list.split(delimiter);
    	return Arrays.asList(a);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	//GameRegistry.addShapelessRecipe(new ResourceLocation(""), new ResourceLocation(""), new ItemStack(blockGypsum), Ingredient.fromStacks(new ItemStack(gypsumPowder, 4)));
    	// recipes
		for(int i = 0; i < 16; i++) {
			GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:DryWall" + i), new ResourceLocation("mineralogy"), new ItemStack(drywall[ i] , 1), Ingredient.fromStacks(new ItemStack(drywall[i])), Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
		}

		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:GUNPOWDER"), new ResourceLocation("mineralogy"), new ItemStack(Items.GUNPOWDER, 4), Ingredient.fromStacks(new ItemStack(Items.COAL,1,1)),Ingredient.fromStacks(new ItemStack(nitratePowder)), Ingredient.fromStacks(new ItemStack(sulphurPowder)));
		
		// TODO: Fix this recipe
		//GameRegistry.addShapelessRecipe(new ResourceLocation(""), new ResourceLocation("mineralogy"), new ItemStack(Items.GUNPOWDER, 4), Ingredient.fromStacks(new ItemStack(carbonPowder)), Ingredient.fromStacks(new ItemStack(nitratePowder)), Ingredient.fromStacks(new ItemStack(sulphurPowder)));
		
		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:GUNPOWDER"), new ResourceLocation("mineralogy"), new ItemStack(Items.GUNPOWDER, 4), Ingredient.fromStacks(new ItemStack(Items.SUGAR)), Ingredient.fromStacks(new ItemStack(nitratePowder)), Ingredient.fromStacks(new ItemStack(sulphurPowder)));
		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:mineralFertilizer"), new ResourceLocation("mineralogy"), new ItemStack(mineralFertilizer, 1), Ingredient.fromStacks(new ItemStack(nitratePowder)), Ingredient.fromStacks(new ItemStack(phosphorousPowder)));

		// recipe modifications
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:STONE_AXE"), new ResourceLocation("mineralogy"), new ItemStack(Items.STONE_AXE), "xx", "xy", " y", 'x', stone, 'y', stickWood);
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:STONE_HOE"), new ResourceLocation("mineralogy"), new ItemStack(Items.STONE_HOE), "xx", " y", " y", 'x', stone, 'y', stickWood);
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:STONE_PICKAXE"), new ResourceLocation("mineralogy"), new ItemStack(Items.STONE_PICKAXE), "xxx", " y "," y ", 'x', stone, 'y',stickWood);
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:STONE_PICKAXE"), new ResourceLocation("mineralogy"), new ItemStack(Items.STONE_SHOVEL), "x", "y", "y", 'x', stone, 'y', stickWood);
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:STONE_PICKAXE"), new ResourceLocation("mineralogy"), new ItemStack(Items.STONE_SWORD), "x", "x", "y", 'x', stone, 'y', stickWood);
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:FURNACE"), new ResourceLocation("mineralogy"), new ItemStack(Blocks.FURNACE), "xxx", "x x", "xxx", 'x', stone);
		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:COBBLESTONE"), new ResourceLocation("mineralogy"), new ItemStack(Blocks.COBBLESTONE, 4), Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)), Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));

		if(SMELTABLE_GRAVEL) GameRegistry.addSmelting(Blocks.GRAVEL, new ItemStack(Blocks.STONE), 0.1F);
		
		// remove default stone slab recipe (interferes with rock slab recipes)
		removeRecipeByName(Arrays.asList(Blocks.STONE_SLAB.getRegistryName().toString()));
		
		// less generic stone slab recipe
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:STONE_SLAB"), new ResourceLocation("mineralogy"), new ItemStack(Blocks.STONE_SLAB, 6, 0), "xxx", 'x', Blocks.STONE);

		// initialize legacy updater
		PatchHandler.getInstance().init(PATCH_UPDATE);

		// event registration, tile entities
    	// (none)

    	// register custom chunk generation
    	GameRegistry.registerWorldGenerator(new StoneReplacer(), 10);

    	// register renderers
    	if(event.getSide().isClient()) {
    		registerItemRenders();
    	}
    }
    
    private void removeRecipeByName(List<String> recipeNames)
    {
    	for(Map.Entry<ResourceLocation, IRecipe> recipe : ForgeRegistries.RECIPES.getEntries()) {
    		for(String recipeName : recipeNames) {
	            if(recipe.getKey().toString().equals(recipeName)) {
	                RegistryManager.ACTIVE.getRegistry(GameData.RECIPES).remove(recipe.getKey());
	            }
    		}
        }
    }
    
    private void registerItemRenders() {

		for(String name : MineralogyItemRegistry.keySet()){
			Item i = Mineralogy.MineralogyItemRegistry.get(name);
    		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
    				.register(i, 0, new ModelResourceLocation(Mineralogy.MODID + ":" + name, "inventory"));
    	}
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
		// addons to other mods
    	
    	// process black-lists and white-lists
    	for(String id : igneousWhitelist) {
    		Block b = getBlock(id);
    		if(b == null) continue;
    		igneousStones.add(b);
    	}
    	for(String id : metamorphicWhitelist) {
    		Block b = getBlock(id);
    		if(b == null) continue;
    		metamorphicStones.add(b);
    	}
    	for(String id : sedimentaryWhitelist) {
    		Block b = getBlock(id);
    		if(b == null) continue;
    		sedimentaryStones.add(b);
    	}
    	for(String id : igneousBlacklist) {
    		Block b = getBlock(id);
    		if(b == null) continue;
    		igneousStones.remove(b);
    	}
    	for(String id : metamorphicBlacklist) {
    		Block b = getBlock(id);
    		if(b == null) continue;
    		metamorphicStones.remove(b);
    	}
    	for(String id : sedimentaryBlacklist) {
    		Block b = getBlock(id);
    		if(b == null) continue;
    		sedimentaryStones.remove(b);
    	}
    }

    private static Block getBlock(String id) {
    	return Block.getBlockFromName(id);
    }
    
    private static int oreWeightCount = 20;

    private static Item addDust(String dustName, String oreDictionaryName) {
    	Item item = registerItem(new Item(), dustName).setUnlocalizedName(Mineralogy.MODID + "." + dustName).setCreativeTab(CreativeTabs.MATERIALS);
		OreDictionary.registerOre("dust" + oreDictionaryName, item);
		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:COBBLESTONE"), new ResourceLocation("mineralogy"), new ItemStack(Blocks.COBBLESTONE, 4), Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)), Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));
		
		NonNullList<ItemStack> blocks = OreDictionary.getOres("block" + oreDictionaryName);
		
		if (blocks.size() > 0) {
				GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:block" + oreDictionaryName), new ResourceLocation("mineralogy"), new ItemStack(item, 9), Ingredient.fromStacks(blocks.get(0)));	
		}
		
		return item;
    }

    // TODO: Recipes
    private static Block addBlock(String name, String oreDictionaryName, int pickLevel) {
    	Block block = new Block(Material.ROCK).setUnlocalizedName(Mineralogy.MODID + "." + name);
    	registerBlock(block, name);
    	OreDictionary.registerOre("block" + oreDictionaryName, block);
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + name + "Block"), new ResourceLocation("mineralogy"), new ItemStack(block), "xxx", "xxx", "xxx", 'x', "dust" + oreDictionaryName);
    	return block;
    }

    private static Block addOre(String oreName, String oreDictionaryName, Item oreDropItem, int numMin, int numMax, int pickLevel,
    		int minY, int maxY, float spawnFrequency, int spawnQuantity) {
    	Block oreBlock = new Ore(oreName, oreDropItem, numMin, numMax, pickLevel).setUnlocalizedName(Mineralogy.MODID + "." + oreName);
    	registerBlock(oreBlock, oreName);
    	OreDictionary.registerOre(oreDictionaryName, oreBlock);
    	GameRegistry.registerWorldGenerator(new OreSpawner(oreBlock, minY, maxY, spawnFrequency, spawnQuantity, (oreWeightCount * 25214903917L)+11L), oreWeightCount++);
    	return oreBlock;
    }

	private static Block registerBlock(Block block, String name) {
		block.setUnlocalizedName(MODID + "." + name);
		registerItem(new ItemBlock(block), name);
		MineralogyBlockRegistry.put(name, block);
		return block;
	}

	private static Item registerItem(Item item, String name) {
		MineralogyItemRegistry.put(name, item);
		item.setUnlocalizedName(MODID + "." + name);
		return item;
	}

	/**
     * 
     * @param type Igneous, sedimentary, or metamorphic
     * @param name id-name of the block
     * @param hardness How hard (time duration) the block is to pick. For reference, dirt is 0.5, stone is 1.5, ores are 3, and obsidian is 50
     * @param blastResistance how resistant the block is to explosions. For reference, dirt is 0, stone is 10, and blast-proof materials are 2000
     * @param toolHardnessLevel 0 for wood tools, 1 for stone, 2 for iron, 3 for diamond
     */
    private static void addStoneType(RockType type, String name, double hardness, double blastResistance, int toolHardnessLevel) {
		final Block rock, rockStairs, rockSlab;
		final Block brick, brickStairs, brickSlab;
		final Block smooth, smoothStairs, smoothSlab;
		final Block smoothBrick, smoothBrickStairs, smoothBrickSlab;
    	rock = registerBlock(new Rock(true, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name);
    	switch(type) {
	    	case IGNEOUS:
	    		igneousStones.add(rock);
	    		break;
	    	case METAMORPHIC:
	    		metamorphicStones.add(rock);
	    		break;
	    	case SEDIMENTARY:
	    		sedimentaryStones.add(rock);
	    		break;
	    	case ANY:
	    		sedimentaryStones.add(rock);
	    		metamorphicStones.add(rock);
	    		igneousStones.add(rock);
	    		break;
    	}
    	
    	// TODO: See if this is needed
    	//OreDictionary.registerOre("stone",rock);
		GameRegistry.addSmelting(rock, new ItemStack(Blocks.STONE), 0.1F);

		rockStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_stairs");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + name + "Stairs"), new ResourceLocation("mineralogy"), new ItemStack(rockStairs, 4), "x  ", "xx ", "xxx", 'x', rock);
		
		rockSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_slab");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + name + "Slab"), new ResourceLocation("mineralogy"), new ItemStack(rockSlab, 6), "xxx", 'x', rock);

		brick = registerBlock(new Rock(false, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_brick");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + name + "Brick"), new ResourceLocation("mineralogy"), new ItemStack(brick, 4), "xx", "xx", 'x', rock);
		
		brickStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance,toolHardnessLevel, SoundType.STONE),name + "_brick_stairs");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + name + "BrickStairs"), new ResourceLocation("mineralogy"), new ItemStack(brickStairs, 4), "x  ", "xx ", "xxx", 'x', brick);
		
		brickSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_brick_slab");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + name + "BrickSlab"), new ResourceLocation("mineralogy"), new ItemStack(brickSlab, 6), "xxx", 'x', brick);

		smooth = registerBlock(new Rock(false, (float)hardness,(float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth");
		GameRegistry.addShapelessRecipe(new ResourceLocation("mineralogy:" + "Polished" + name), new ResourceLocation("mineralogy"), new ItemStack(smooth, 1), Ingredient.fromStacks(new ItemStack(brick, 1), new ItemStack(Blocks.SAND, 1)));
		
		smoothStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE),name + "_smooth_stairs");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + "Polished" + name + "Stairs"), new ResourceLocation("mineralogy"), new ItemStack(smoothStairs, 4), "x  ","xx ", "xxx", 'x', smooth);
		
		smoothSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE),name + "_smooth_slab");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + "Polished" + name + "Slab"), new ResourceLocation("mineralogy"), new ItemStack(smoothSlab, 6), "xxx", 'x', smooth);

		smoothBrick = registerBlock(new Rock(false, (float)hardness,(float)blastResistance,toolHardnessLevel, SoundType.STONE), name + "_smooth_brick");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + "Polished" + name + "Brick"), new ResourceLocation("mineralogy"), new ItemStack(smoothBrick, 4), "xx", "xx", 'x', smooth);
		
		smoothBrickStairs = registerBlock(new RockStairs(rock, (float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth_brick_stairs");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + "Polished" + name + "BrickStair"), new ResourceLocation("mineralogy"), new ItemStack(smoothBrickStairs, 4), "x  ","xx ", "xxx", 'x', smoothBrick);
		
		smoothBrickSlab = registerBlock(new RockSlab((float)hardness, (float)blastResistance, toolHardnessLevel, SoundType.STONE), name + "_smooth_brick_slab");
		GameRegistry.addShapedRecipe(new ResourceLocation("mineralogy:" + "Polished" + name + "BrickSlab"), new ResourceLocation("mineralogy"), new ItemStack(smoothBrickSlab, 6), "xxx", 'x', smoothBrick);
    }
}