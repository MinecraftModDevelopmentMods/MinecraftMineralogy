package com.mcmoddev.mineralogy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DON'T FORGET TO UPDATE mcmod.info FILE!!!

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.mineralogy.blocks.Chert;
import com.mcmoddev.mineralogy.blocks.DryWall;
import com.mcmoddev.mineralogy.blocks.Gypsum;
import com.mcmoddev.mineralogy.blocks.Ore;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.data.MaterialType;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.items.MineralFertilizer;
import com.mcmoddev.mineralogy.patching.PatchHandler;
import com.mcmoddev.mineralogy.util.BlockItemPair;
import com.mcmoddev.mineralogy.util.RecipeHelper;
import com.mcmoddev.mineralogy.util.RegistrationHelper;
import com.mcmoddev.mineralogy.worldgen.OreSpawner;
import com.mcmoddev.mineralogy.worldgen.StoneReplacer;

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

	//public static final CreativeTabs mineralogyTab = MineralogyCreativeTab.instance("tabMineralogy");

	protected static BlockItemPair blockChert;
	protected static BlockItemPair blockGypsum;
	public static BlockItemPair blockPumice;

	

	protected static BlockItemPair[] drywalls = new BlockItemPair[16];

	@Mod.EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		logger.warn("Invalid fingerprint detected!");
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new MineralogyEventBusSubscriber());
		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Blocks.class);
		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Items.class);
		
		MineralogyConfig.preInit(event);

		// Blocks, Items, World-gen


		// other blocks
		MineralogyRegistry.sedimentaryStones.add(Blocks.SANDSTONE);

		blockChert = RegistrationHelper.registerBlock(new Chert(mineralogyTab), Constants.CHERT, "blockChert");
		MineralogyRegistry.sedimentaryStones.add(blockChert.PairedBlock);

		blockGypsum = RegistrationHelper.registerBlock(new Gypsum(mineralogyTab), Constants.GYPSUM.toLowerCase(), "blockGypsum");
		MineralogyRegistry.sedimentaryStones.add(blockGypsum.PairedBlock);

		RecipeHelper.addShapedOreRecipe(Constants.GYPSUM.toLowerCase(), new ItemStack(blockGypsum.PairedItem, 1), "xxx", "xxx", "xxx", 'x',
				gypsumPowder);
		RecipeHelper.addShapelessOreRecipe(Constants.GYPSUM.toLowerCase() + "_dust", new ItemStack(gypsumPowder, 9),
				Ingredient.fromStacks(new ItemStack(blockGypsum.PairedItem)));

		blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.GROUND, mineralogyTab), Constants.PUMICE, "blockPumice");
		MineralogyRegistry.igneousStones.add(blockPumice.PairedBlock);

		final String ORES = "ores";

		// register ores
		addOre(Constants.SULFUR, sulphurPowder, 1, 4, 0,
				MineralogyConfig.config().getInt("sulphur_ore.minY", ORES, 16, 1, 255, "Minimum ore spawn height"),
				MineralogyConfig.config().getInt("sulphur_ore.maxY", ORES, 64, 1, 255, "Maximum ore spawn height"),
				MineralogyConfig.config().getFloat("sulphur_ore.frequency", ORES, 1, 0, 63, "Number of ore deposits per chunk"),
				MineralogyConfig.config().getInt("sulphur_ore.quantity", ORES, 16, 0, 63, "Size of ore deposit"));

		addOre(Constants.PHOSPHOROUS, phosphorousPowder, 1, 4, 0,
				MineralogyConfig.config().getInt("phosphorous_ore.minY", ORES, 16, 1, 255, "Minimum ore spawn height"),
				MineralogyConfig.config().getInt("phosphorous_ore.maxY", ORES, 64, 1, 255, "Maximum ore spawn height"),
				MineralogyConfig.config().getFloat("phosphorous_ore.frequency", ORES, 1, 0, 63, "Number of ore deposits per chunk"),
				MineralogyConfig.config().getInt("phosphorous_ore.quantity", ORES, 16, 0, 63, "Size of ore deposit"));

		addOre(Constants.NITRATE, nitratePowder, 1, 4, 0,
				MineralogyConfig.config().getInt("nitrate_ore.minY", ORES, 16, 1, 255, "Minimum ore spawn height"),
				MineralogyConfig.config().getInt("nitrate_ore.maxY", ORES, 64, 1, 255, "Maximum ore spawn height"),
				MineralogyConfig.config().getFloat("nitrate_ore.frequency", ORES, 1, 0, 63, "Number of ore deposits per chunk"),
				MineralogyConfig.config().getInt("nitrate_ore.quantity", ORES, 16, 0, 63, "Size of ore deposit"));

		MineralogyConfig.config().save();

		for (int i = 0; i < 16; i++) {
			drywalls[i] = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
					Constants.DRYWALL + Constants.colorSuffixesTwo[i]);
		}

		RecipeHelper.addShapedOreRecipe(Constants.DRYWALL, new ItemStack(drywalls[15].PairedItem, 3), "pgp", "pgp", "pgp", 'p', Items.PAPER,
				'g', gypsumPowder);

		for (int i = 0; i < 16; i++) {
			RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
					Ingredient.fromStacks(new ItemStack(drywalls[15].PairedItem)),
					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
		}

		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_COAL", new ItemStack(Items.GUNPOWDER, 4),
				Ingredient.fromStacks(new ItemStack(Items.COAL)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(sulphurPowder)));

		// TODO: Fix this recipe
		// addShapelessOreRecipe(GUNPOWDER + "_FROM_CARBON", new
		// ItemStack(Items.GUNPOWDER, 4), Ingredient.fromStacks(new
		// ItemStack(carbonDust)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
		// Ingredient.fromStacks(new ItemStack(sulphurPowder)));

		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_SUGAR", new ItemStack(Items.GUNPOWDER, 4),
				Ingredient.fromStacks(new ItemStack(Items.SUGAR)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(sulphurPowder)));
		RecipeHelper.addShapelessOreRecipe("mineralFertilizer", new ItemStack(mineralFertilizer, 1),
				Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(phosphorousPowder)));

		RecipeHelper.addShapelessOreRecipe(Constants.COBBLESTONE.toUpperCase(), new ItemStack(Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));
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

		for (String name : MineralogyRegistry.MineralogyItemRegistry.keySet()) {
			Item i = MineralogyRegistry.MineralogyItemRegistry.get(name);
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
			MineralogyRegistry.igneousStones.add(b);
		}
		for (String id : MineralogyConfig.metamorphicWhitelist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			MineralogyRegistry.metamorphicStones.add(b);
		}
		for (String id : MineralogyConfig.sedimentaryWhitelist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			MineralogyRegistry.sedimentaryStones.add(b);
		}
		for (String id : MineralogyConfig.igneousBlacklist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			MineralogyRegistry.igneousStones.remove(b);
		}
		for (String id : MineralogyConfig.metamorphicBlacklist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			MineralogyRegistry.metamorphicStones.remove(b);
		}
		for (String id : MineralogyConfig.sedimentaryBlacklist()) {
			Block b = getBlock(id);
			if (b == null)
				continue;
			MineralogyRegistry.sedimentaryStones.remove(b);
		}
	}

	private static Block getBlock(String id) {
		return Block.getBlockFromName(id);
	}

	private static int oreWeightCount = 20;

	private static Block addBlock(String oreDictionaryName, int pickLevel, Item dust) {
		String name = oreDictionaryName.toLowerCase() + "_block";

		BlockItemPair pair = RegistrationHelper.registerBlock(new Rock(false, (float) 1.5, (float) 10, 0, SoundType.STONE, mineralogyTab), name,
				Constants.BLOCK.toLowerCase() + oreDictionaryName);

		RecipeHelper.addShapedOreRecipe(name, new ItemStack(pair.PairedItem), "xxx", "xxx", "xxx", 'x', dust);
		RecipeHelper.addShapelessOreRecipe(oreDictionaryName.toLowerCase() + "_dust", new ItemStack(dust, 9),
				Ingredient.fromStacks(new ItemStack(pair.PairedItem)));

		return pair.PairedBlock;
	}

	private static Block addOre(String oreDictionaryName, Item oreDropItem, int numMin, int numMax, int pickLevel, int minY, int maxY, float spawnFrequency, int spawnQuantity) {
		String oreName = oreDictionaryName.toLowerCase() + "_" + Constants.ORE;

		Block oreBlock = new Ore(oreName, oreDropItem, numMin, numMax, pickLevel, mineralogyTab)
				.setUnlocalizedName(Mineralogy.MODID + "." + oreName);

		RegistrationHelper.registerBlock(oreBlock, oreName, Constants.ORE + oreDictionaryName);

		GameRegistry.registerWorldGenerator(new OreSpawner(oreBlock, minY, maxY, spawnFrequency, spawnQuantity,
				(oreWeightCount * 25214903917L) + 11L), oreWeightCount++);

		addBlock(oreDictionaryName, 0, oreDropItem);

		return oreBlock;
	}
}