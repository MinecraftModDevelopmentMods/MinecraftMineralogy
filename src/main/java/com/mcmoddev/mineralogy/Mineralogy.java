package com.mcmoddev.mineralogy;



// DON'T FORGET TO UPDATE mcmod.info FILE!!!

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.mineralogy.blocks.Chert;
import com.mcmoddev.mineralogy.blocks.DryWall;
import com.mcmoddev.mineralogy.blocks.Gypsum;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.patching.PatchHandler;
import com.mcmoddev.mineralogy.util.BlockItemPair;
import com.mcmoddev.mineralogy.util.RecipeHelper;
import com.mcmoddev.mineralogy.util.RegistrationHelper;
import com.mcmoddev.mineralogy.worldgen.StoneReplacer;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

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

	protected static BlockItemPair blockChert;
	protected static BlockItemPair blockGypsum;
	public static BlockItemPair blockPumice;

	private static CreativeTabs mineralogyTab;

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
		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Ores.class);
		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Recipes.class);
		
		MineralogyConfig.preInit(event);

		com.mcmoddev.mineralogy.init.Blocks.init();
		com.mcmoddev.mineralogy.init.Items.init();
		com.mcmoddev.mineralogy.init.Ores.Init();
		com.mcmoddev.mineralogy.init.Recipes.Init();
		
		mineralogyTab = MinIoC.getInstance().resolve(CreativeTabs.class);
		
		// Blocks, Items, World-gen


		// other blocks
		MineralogyRegistry.sedimentaryStones.add(Blocks.SANDSTONE);

		blockChert = RegistrationHelper.registerBlock(new Chert(mineralogyTab), Constants.CHERT, "blockChert");
		MineralogyRegistry.sedimentaryStones.add(blockChert.PairedBlock);

		blockGypsum = RegistrationHelper.registerBlock(new Gypsum(mineralogyTab), Constants.GYPSUM.toLowerCase(), "blockGypsum");
		MineralogyRegistry.sedimentaryStones.add(blockGypsum.PairedBlock);

		RecipeHelper.addShapedOreRecipe(Constants.GYPSUM.toLowerCase(), new ItemStack(blockGypsum.PairedItem, 1), "xxx", "xxx", "xxx", 'x', "dustGypsum");
		
		blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.GROUND, mineralogyTab), Constants.PUMICE, "blockPumice");
		MineralogyRegistry.igneousStones.add(blockPumice.PairedBlock);

		

		for (int i = 0; i < 16; i++) {
			drywalls[i] = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
					Constants.DRYWALL + Constants.colorSuffixesTwo[i]);
		}

		RecipeHelper.addShapedOreRecipe(Constants.DRYWALL, new ItemStack(drywalls[15].PairedItem, 3), "pgp", "pgp", "pgp", 'p', "paper",
				'g', "dustGypsum");

		for (int i = 0; i < 16; i++) {
			RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
					Ingredient.fromStacks(new ItemStack(drywalls[15].PairedItem)),
					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
		}

		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_COAL", new ItemStack(Items.GUNPOWDER, 4),
				Ingredient.fromStacks(new ItemStack(Items.COAL)), "dustNitrate",
				"dustSulfur");

		// TODO: Fix this recipe
		// addShapelessOreRecipe(GUNPOWDER + "_FROM_CARBON", new
		// ItemStack(Items.GUNPOWDER, 4), Ingredient.fromStacks(new
		// ItemStack(carbonDust)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
		// Ingredient.fromStacks(new ItemStack(sulphurPowder)));

		
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (MineralogyConfig.smeltableGravel())
			GameRegistry.addSmelting(Blocks.GRAVEL, new ItemStack(Blocks.STONE), 0.1F);

		//PatchHandler.getInstance().init(MineralogyConfig.patchUpdate()); // initialize legacy updater

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

	

	
}