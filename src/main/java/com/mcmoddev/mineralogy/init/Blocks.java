package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.blocks.Chalk;
import com.mcmoddev.mineralogy.blocks.Chert;
import com.mcmoddev.mineralogy.blocks.DoubleSlab;
import com.mcmoddev.mineralogy.blocks.DryWall;
import com.mcmoddev.mineralogy.blocks.Gypsum;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockFurnace;
import com.mcmoddev.mineralogy.blocks.RockRelief;
import com.mcmoddev.mineralogy.blocks.RockSalt;
import com.mcmoddev.mineralogy.blocks.RockSaltLamp;
import com.mcmoddev.mineralogy.blocks.RockSaltStreetLamp;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.blocks.RockWall;
import com.mcmoddev.mineralogy.data.Material;
import com.mcmoddev.mineralogy.data.MaterialData;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;
import com.mcmoddev.mineralogy.tileentity.TileEntityRockFurnace;
import com.mcmoddev.mineralogy.util.BlockItemPair;
import com.mcmoddev.mineralogy.util.RecipeHelper;
import com.mcmoddev.mineralogy.util.RegistrationHelper;

import net.minecraft.block.SoundType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Blocks {
	private static boolean initDone = false;
	
	protected Blocks() {
		throw new IllegalAccessError("Not a instantiable class");
	}

	/**
	 *
	 */
	public static void init() {
		if (initDone) {
			return;
		}

		MinIoC IoC =  MinIoC.getInstance();
		
		BlockItemPair blockChert;
		BlockItemPair blockGypsum;
		BlockItemPair blockChalk;
		BlockItemPair blockRocksalt;
		BlockItemPair blockPumice;
		BlockItemPair[] drywalls = new BlockItemPair[16];
	    
		GameRegistry.registerTileEntity(TileEntityRockFurnace.class, "rockfurnace");
		
		MaterialData.toArray().forEach(material -> addStoneType(material));		
		
		MineralogyRegistry.sedimentaryStones.add(net.minecraft.init.Blocks.SANDSTONE);

		blockChert = RegistrationHelper.registerBlock(new Chert(), Constants.CHERT, Constants.BLOCK_CHERT);
		MineralogyRegistry.sedimentaryStones.add(blockChert.PairedBlock);
		MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, blockChert.PairedBlock);
		
		blockGypsum = RegistrationHelper.registerBlock(new Gypsum(), Constants.GYPSUM.toLowerCase(), Constants.BLOCK_GYPSUM);
		MineralogyRegistry.sedimentaryStones.add(blockGypsum.PairedBlock);
		
		blockChalk = RegistrationHelper.registerBlock(new Chalk(), Constants.CHALK.toLowerCase(), Constants.BLOCK_CHALK);
		MineralogyRegistry.sedimentaryStones.add(blockChalk.PairedBlock);
		
		blockRocksalt = RegistrationHelper.registerBlock(new RockSalt(), Constants.ROCKSALT.toLowerCase(), Constants.BLOCK_ROCKSALT);
		MineralogyRegistry.sedimentaryStones.add(blockRocksalt.PairedBlock);
		
		addStoneType(MaterialData.ROCK_SALT, blockRocksalt);
		
		IoC.register(BlockItemPair.class, blockGypsum, Constants.BLOCK_GYPSUM, Mineralogy.MODID);
		IoC.register(BlockItemPair.class, blockChalk, Constants.BLOCK_CHALK, Mineralogy.MODID);
		IoC.register(BlockItemPair.class, blockRocksalt, Constants.BLOCK_ROCKSALT, Mineralogy.MODID);
		
		blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.STONE), Constants.PUMICE, Constants.BLOCK_PUMICE);
		MineralogyRegistry.igneousStones.add(blockPumice.PairedBlock);
		MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, blockPumice.PairedBlock);
		
		IoC.register(BlockItemPair.class, blockPumice, Constants.BLOCK_PUMICE, Mineralogy.MODID);
		
		RegistrationHelper.registerBlock(new RockSaltLamp(), "rocksaltlamp", "lampRocksalt");
		RegistrationHelper.registerBlock(new RockSaltStreetLamp(), "rocksaltstreetlamp", "lampRocksaltStreet", 16);
		
		IDynamicTabProvider tabProvider = IoC.resolve(IDynamicTabProvider.class);
		
		for (int i = 0; i < 16; i++) {
			if (MineralogyConfig.groupCreativeTabItemsByType())
				tabProvider.setTabItemMapping("Item", Constants.DRYWALL + "_" + Constants.colorSuffixes[i]);
			
			drywalls[i] = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
					Constants.DRYWALL + Constants.colorSuffixesTwo[i]);
			
			IoC.register(BlockItemPair.class, drywalls[i], Constants.DRYWALL + Constants.colorSuffixesTwo[i], Mineralogy.MODID);
			
			RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
					Constants.DRYWALL_WHITE,
					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
		}
		
		initDone = true;
	}

	private static void generateReliefs(String materialName, double hardness, double blastResistance,
			int toolHardnessLevel, final BlockItemPair rock) {
		
		String oreDictName = "stone" + materialName.substring(0, 1).toUpperCase() + materialName.substring(1) + "Smooth";
		
		final BlockItemPair  blankRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_blank", Constants.RELIEF + "Blank" + materialName);		
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_blank", new ItemStack(blankRelief.PairedItem, 16), "xxx", "xxx", "xxx", 'x', oreDictName);
		
		final BlockItemPair axeRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_axe", Constants.RELIEF + "Axe" + materialName);
		RecipeHelper.addShapelessOreRecipe(materialName + "_relief_axe", new ItemStack(axeRelief.PairedItem, 1), Constants.RELIEF + "Blank" + materialName, Items.STONE_AXE);
		
		final BlockItemPair crossRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_cross", Constants.RELIEF  + "Cross" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_cross", new ItemStack(crossRelief.PairedItem, 1), "x x", "   ", "x x", 'x', Constants.RELIEF + "Blank" + materialName);
		
		final BlockItemPair hammerRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_hammer", Constants.RELIEF  + "Hammer" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_hammer", new ItemStack(hammerRelief.PairedItem, 1), " x "," y "," z ",'x', oreDictName,'y', Items.STICK,'z', Constants.RELIEF + "Blank" + materialName);
			
		final BlockItemPair hoeRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_hoe", Constants.RELIEF + "Hoe" + materialName);
		RecipeHelper.addShapelessOreRecipe(materialName + "_relief_hoe", new ItemStack(hoeRelief.PairedItem, 1), Constants.RELIEF + "Blank" + materialName, Items.STONE_HOE);
		
		final BlockItemPair horizontalRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_horizontal", Constants.RELIEF  + "Horizontal" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_horizontal", new ItemStack(horizontalRelief.PairedItem, 1), "xxx", 'x', Constants.RELIEF + "Blank" + materialName);
		
		final BlockItemPair leftRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_left", Constants.RELIEF  + "Left" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_left", new ItemStack(leftRelief.PairedItem, 1),"x  "," x ","  x",'x', Constants.RELIEF + "Blank" + materialName);
		
		final BlockItemPair pickaxeRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_pickaxe", Constants.RELIEF + "Pickaxe" + materialName);
		RecipeHelper.addShapelessOreRecipe(materialName + "_relief_pickaxe", new ItemStack(pickaxeRelief.PairedItem, 1), Constants.RELIEF + "Blank" + materialName, Items.STONE_PICKAXE);
		
		final BlockItemPair plusRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_plus", Constants.RELIEF  + "Plus" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_plus", new ItemStack(plusRelief.PairedItem, 1), " x ","xxx"," x ", 'x', Constants.RELIEF + "Blank" + materialName);
		
		final BlockItemPair rightRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_right", Constants.RELIEF + "Right" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_right", new ItemStack(rightRelief.PairedItem, 3),"  x"," x ","x  ",'x', Constants.RELIEF  + "Left" + materialName);
		
		final BlockItemPair swordRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_sword", Constants.RELIEF + "Sword" + materialName);
		RecipeHelper.addShapelessOreRecipe(materialName + "_relief_sword", new ItemStack(swordRelief.PairedItem, 1), Constants.RELIEF + "Blank" + materialName, Items.STONE_SWORD);
		
		final BlockItemPair iRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_i", Constants.RELIEF  + "I" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_i", new ItemStack(iRelief.PairedItem, 1), "xxx"," x ","xxx", 'x', Constants.RELIEF + "Blank" + materialName);
		
		final BlockItemPair verticalRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_vertical", Constants.RELIEF  + "Vertical" + materialName);
		RecipeHelper.addShapedOreRecipe(materialName + "_relief_vertical", new ItemStack(verticalRelief.PairedItem, 1), "x","x","x", 'x', Constants.RELIEF + "Blank" + materialName);
	}
	
	protected static void addStoneType(Material materialType, BlockItemPair rockPair) {

		String name = materialType.materialName.toLowerCase();
		String oreDictName = "stone" + materialType.materialName;
		
		final BlockItemPair rockFurnacePair;
		final BlockItemPair rockStairPair;
		final BlockItemPair rockSlabPair;
		final BlockItemPair rockWallPair;
		final BlockItemPair brickPair;
		final BlockItemPair brickFurnacePair;
		final BlockItemPair brickStairPair;
		final BlockItemPair brickSlabPair;
		final BlockItemPair brickWallPair;
		final BlockItemPair smoothPair;
		final BlockItemPair smoothFurnacePair;
		final BlockItemPair smoothStairPair;
		final BlockItemPair smoothSlabPair;
		final BlockItemPair smoothWallPair;
		final BlockItemPair smoothBrickPair;
		final BlockItemPair smoothBrickFurnacePair;
		final BlockItemPair smoothBrickStairPair;
		final BlockItemPair smoothBrickSlabPair;
		final BlockItemPair smoothBrickWallPair;

		RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.COBBLESTONE.toUpperCase(), new ItemStack(net.minecraft.init.Blocks.COBBLESTONE, 4),
				oreDictName,
				oreDictName,
				Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)));

		switch (materialType.rockType) {
			case IGNEOUS:
				MineralogyRegistry.igneousStones.add(rockPair.PairedBlock);
				break;
			case METAMORPHIC:
				MineralogyRegistry.metamorphicStones.add(rockPair.PairedBlock);
				break;
			case SEDIMENTARY:
				MineralogyRegistry.sedimentaryStones.add(rockPair.PairedBlock);
				break;
			case ANY:
				MineralogyRegistry.sedimentaryStones.add(rockPair.PairedBlock);
				MineralogyRegistry.metamorphicStones.add(rockPair.PairedBlock);
				MineralogyRegistry.igneousStones.add(rockPair.PairedBlock);
				break;
		}

		GameRegistry.addSmelting(rockPair.PairedBlock, new ItemStack(net.minecraft.init.Blocks.STONE), 0.1F);

		// no point in ore dicting these recipes I think
		if (MineralogyConfig.generateRockStairs()) {
			rockStairPair = RegistrationHelper.registerBlock(new RockStairs(rockPair.PairedBlock, (float) materialType.hardness,
					(float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name + "_" + Constants.STAIRS,
					Constants.STAIRS + materialType.materialName);
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.STAIRS, new ItemStack(rockStairPair.PairedItem, 4), "x  ", "xx ", "xxx",
					'x', oreDictName);
		}

		if (MineralogyConfig.generateRockSlab()) {
			rockSlabPair = RegistrationHelper.registerBlock(
					new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_double_" + Constants.SLAB),
					name + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName, true, 64, true);
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SLAB, new ItemStack(rockSlabPair.PairedItem, 6), "xxx", 'x',
					oreDictName);
		
			RegistrationHelper.registerBlock(
					new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, rockSlabPair.PairedBlock),
					name + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName, false, 64, false);
			
			if (MineralogyConfig.generateRockFurnace()) {
				rockFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
						(float) materialType.blastResistance, materialType.toolHardnessLevel, false), name + "_" + Constants.FURNACE,
						Constants.FURNACE + materialType.materialName, true, 1, false);
				RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
						(float) materialType.blastResistance, materialType.toolHardnessLevel, true).setLightLevel(0.875F), "lit_" + name + "_" + Constants.FURNACE,
						Constants.FURNACE + materialType.materialName, false, 1, true);
				
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.FURNACE, new ItemStack(rockFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
						'x', Constants.SLAB + materialType.materialName, 'y', net.minecraft.init.Blocks.FURNACE);
			}
		}
		
		if (MineralogyConfig.generateRockWall()) {
			rockWallPair = RegistrationHelper.registerBlock(
					new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
					name + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.WALL, new ItemStack(rockWallPair.PairedItem, 6), "xxx", "xxx", 'x',
					oreDictName);
		}

		if (MineralogyConfig.generateBrick()) {
			brickPair = RegistrationHelper.registerBlock(
					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
					name + "_" + Constants.BRICK, "stone" + materialType.materialName + "Brick");
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK, new ItemStack(brickPair.PairedItem, 4), "xx", "xx", 'x',
					oreDictName);

			if (MineralogyConfig.generateBrickStairs()) {
				brickStairPair = RegistrationHelper.registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
								materialType.toolHardnessLevel, SoundType.STONE),
						name + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Brick");
				
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.STAIRS, new ItemStack(brickStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', "stone" + materialType.materialName + "Brick");
			}

			if (MineralogyConfig.generateBrickSlab()) {
				brickSlabPair = RegistrationHelper.registerBlock(
						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.BRICK + "_double_" + Constants.SLAB),
						name + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Brick", true, 64, true);
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.SLAB, new ItemStack(brickSlabPair.PairedItem, 6), "xxx",
						'x', "stone" + materialType.materialName + "Brick");
			
				RegistrationHelper.registerBlock(
						new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, brickSlabPair.PairedBlock),
						name + "_" + Constants.BRICK + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "Brick", false, 64, false);
				
				if (MineralogyConfig.generateBrickFurnace()) {
					brickFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
									(float) materialType.blastResistance, materialType.toolHardnessLevel, false), name + "_" + Constants.BRICK + "_" + Constants.FURNACE,
							Constants.FURNACE + materialType.materialName, true, 1, false);
					RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
									(float) materialType.blastResistance, materialType.toolHardnessLevel, true).setLightLevel(0.875F), "lit_" + name + "_" + Constants.BRICK + "_" + Constants.FURNACE,
							Constants.FURNACE + materialType.materialName, false, 1, false);
	
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.FURNACE, new ItemStack(brickFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
							'x', Constants.SLAB + materialType.materialName + "Brick", 'y', net.minecraft.init.Blocks.FURNACE);
				}
			}
			
			if (MineralogyConfig.generateBrickWall()) {
				brickWallPair = RegistrationHelper.registerBlock(
						new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
						name + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.WALL, new ItemStack(brickWallPair.PairedItem, 6), "xxx", "xxx", 'x',
						"stone" + materialType.materialName + "Brick");
			}
		}

		if (MineralogyConfig.generateSmooth()) {
			smoothPair = RegistrationHelper.registerBlock(
					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
					name + "_" + Constants.SMOOTH, "stone" + materialType.materialName + "Smooth");
			RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.SMOOTH, new ItemStack(smoothPair.PairedItem, 1),
					oreDictName,
					Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.SAND, 1)));
	
			if(MineralogyConfig.generateReliefs()) {
				generateReliefs(name, materialType.hardness, materialType.blastResistance, materialType.toolHardnessLevel, smoothPair);
			}
			
			if (MineralogyConfig.generateSmoothStairs()) {
				smoothStairPair = RegistrationHelper.registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
								materialType.toolHardnessLevel, SoundType.STONE),
						name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Smooth");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, new ItemStack(smoothStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', "stone" + materialType.materialName + "Smooth");
			}

			if (MineralogyConfig.generateSmoothSlab()) {
				smoothSlabPair = RegistrationHelper.registerBlock(
						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.SMOOTH + "_double_" + Constants.SLAB),
						name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Smooth", true, 64, true);
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, new ItemStack(smoothSlabPair.PairedItem, 6), "xxx",
						'x', "stone" + materialType.materialName + "Smooth");
				RegistrationHelper.registerBlock(
						new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, smoothSlabPair.PairedBlock),
						name + "_" + Constants.SMOOTH + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "Smooth", false, 64, false);
				
				if (MineralogyConfig.generateSmoothFurnace()) {
					smoothFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
									(float) materialType.blastResistance, materialType.toolHardnessLevel, false), name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE,
							Constants.FURNACE + materialType.materialName, true, 1, false);
					RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
									(float) materialType.blastResistance, materialType.toolHardnessLevel, true).setLightLevel(0.875F), "lit_" + name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE,
							Constants.FURNACE + materialType.materialName, false, 1, false);
	
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE, new ItemStack(smoothFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
							'x', Constants.SLAB + materialType.materialName + "Smooth", 'y', net.minecraft.init.Blocks.FURNACE);
				}
			}

			if (MineralogyConfig.generateSmoothWall()) {
				smoothWallPair = RegistrationHelper.registerBlock(
						new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
						name + "_" + Constants.SMOOTH + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.WALL, new ItemStack(smoothWallPair.PairedItem, 6), "xxx", "xxx", 'x',
						"stone" + materialType.materialName + "Smooth");
			}
			
			if (MineralogyConfig.generateSmoothBrick()) {
				smoothBrickPair = RegistrationHelper.registerBlock(
						new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
						name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, "stone" + materialType.materialName + "SmoothBrick");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, new ItemStack(smoothBrickPair.PairedItem, 4),
						"xx", "xx", 'x', "stone" + materialType.materialName + "Smooth");
			
				if (MineralogyConfig.generateSmoothBrickStairs()) {
					smoothBrickStairPair = RegistrationHelper.registerBlock(
							new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
									materialType.toolHardnessLevel, SoundType.STONE),
							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "SmoothBrick");
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS,
							new ItemStack(smoothBrickStairPair.PairedItem, 4), "x  ", "xx ", "xxx", 'x',
							"stone" + materialType.materialName + "SmoothBrick");
				}

				if (MineralogyConfig.generateSmoothBrickSlab()) {
					smoothBrickSlabPair = RegistrationHelper.registerBlock(
							new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_double_" + Constants.SLAB),
							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "SmoothBrick", true, 64, true);
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB,
							new ItemStack(smoothBrickSlabPair.PairedItem, 6), "xxx", 'x', "stone" + materialType.materialName + "SmoothBrick");
					RegistrationHelper.registerBlock(
							new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, smoothBrickSlabPair.PairedBlock),
							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "SmoothBrick", false, 64, false);
					
					if (MineralogyConfig.generateSmoothBrickFurnace()) {
						smoothBrickFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
										(float) materialType.blastResistance, materialType.toolHardnessLevel, false), name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE,
								Constants.FURNACE + materialType.materialName, true, 1, false);
						RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
										(float) materialType.blastResistance, materialType.toolHardnessLevel, true).setLightLevel(0.875F), "lit_" + name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE,
								Constants.FURNACE + materialType.materialName, false, 1, false);
	
						RecipeHelper.addShapedOreRecipe(name+ "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE, new ItemStack(smoothBrickFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
								'x', Constants.SLAB + materialType.materialName + "SmoothBrick", 'y', net.minecraft.init.Blocks.FURNACE);
					}
				}
				
				if (MineralogyConfig.generateSmoothBrickWall()) {
					smoothBrickWallPair = RegistrationHelper.registerBlock(
							new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, new ItemStack(smoothBrickWallPair.PairedItem, 6), "xxx", "xxx", 'x',
							"stone" + materialType.materialName + "SmoothBrick");
				}
			}
		}
	
	}
	
	protected static void addStoneType(Material materialType) {
		String name = materialType.materialName.toLowerCase();
		final BlockItemPair rockPair = RegistrationHelper.registerBlock(new Rock(true, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name, "stone" + materialType.materialName);

		if (materialType.cobbleEquivilent)
			MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, rockPair.PairedBlock);
		
		addStoneType(materialType, rockPair);
	}
}
