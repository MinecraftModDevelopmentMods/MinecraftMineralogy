package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyCreativeTab;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.data.MaterialType;
import com.mcmoddev.mineralogy.data.MaterialTypes;
import com.mcmoddev.mineralogy.util.BlockItemPair;
import com.mcmoddev.mineralogy.util.RecipeHelper;

import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Blocks {

	private static boolean initDone = false;
	private static MineralogyCreativeTab mineralogyTab = MineralogyCreativeTab.instance("tabMineralogy");
	
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
		
		MaterialTypes.toArray().forEach(material -> addStoneType(material));
		
//		com.mcmoddev.basemetals.util.Config.init();
//		com.mcmoddev.lib.init.Blocks.init();
//		Materials.init();
//		ItemGroups.init();
//
//		registerVanilla();

//		String[] simpleFullBlocks = new String[] { MaterialTypes.ADAMANTINE, MaterialTypes.ANTIMONY, MaterialTypes.BISMUTH,
//				MaterialTypes.COLDIRON, MaterialTypes.COPPER, MaterialTypes.LEAD, MaterialTypes.NICKEL, MaterialTypes.PLATINUM,
//				MaterialTypes.SILVER, MaterialTypes.TIN, MaterialTypes.ZINC };
//		
//		Arrays.asList(simpleFullBlocks).stream()
//		.filter( name -> Options.isMaterialEnabled(name))
//		.forEach( name -> createBlocksFull(name, myTabs) );
//		
//		Arrays.asList(alloyFullBlocks).stream()
//		.filter( name -> Options.isMaterialEnabled(name))
//		.forEach( name -> createBlocksFullOreless(name, myTabs) );
//
//		createStarSteel();
//		createMercury();
//		
//
//		humanDetector = addBlock(new BlockHumanDetector(), "human_detector", myTabs.blocksTab);
//		initDone = true;
	}

	protected static void addStoneType(MaterialType materialType) {
		String name = materialType.materialName.toLowerCase();

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

		rockPair = registerBlock(new Rock(true, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab), name, name);

		// TODO: see why this is necessary, the ore dictionary should make this unnecessary?
		 
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_STONE_AXE", RecipeHelper.addShapedOreRecipe(name + "_STONE_AXE", new ItemStack(Items.STONE_AXE), "xx", "xy", " y", 'x', rockPair.PairedItem, 'y', Items.STICK));
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_STONE_HOE", RecipeHelper.addShapedOreRecipe(name + "_STONE_HOE", new ItemStack(Items.STONE_HOE), "xx", " y", " y", 'x', rockPair.PairedItem, 'y', Items.STICK));
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_STONE_PICKAXE", RecipeHelper.addShapedOreRecipe(name + "_STONE_PICKAXE", new ItemStack(Items.STONE_PICKAXE), "xxx", " y ", " y ", 'x', rockPair.PairedItem, 'y', Items.STICK));
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_STONE_SHOVEL", RecipeHelper.addShapedOreRecipe(name + "_STONE_SHOVEL", new ItemStack(Items.STONE_SHOVEL), "x", "y", "y", 'x', rockPair.PairedItem, 'y', Items.STICK));
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_STONE_SWORD", RecipeHelper.addShapedOreRecipe(name + "_STONE_SWORD", new ItemStack(Items.STONE_SWORD), "x", "x", "y", 'x', rockPair.PairedItem, 'y', Items.STICK));
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_STONE_FURNACE", RecipeHelper.addShapedOreRecipe(name + "_FURNACE", new ItemStack(net.minecraft.init.Blocks.FURNACE), "xxx", "x x", "xxx", 'x', rockPair.PairedItem));
		MineralogyRegistry.MineralogyRecipeRegistry.put(name + "_" + Constants.COBBLESTONE.toUpperCase(), RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.COBBLESTONE.toUpperCase(), new ItemStack(net.minecraft.init.Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
				Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL))));

		MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, rockPair.PairedBlock); // register so it can be used in cobblestone recipes

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

		// TODO: See why this doesn't work (recipes still wont work with 'stone')
		// OreDictionary.registerOre("stone", rock);
		GameRegistry.addSmelting(rockPair.PairedBlock, new ItemStack(Blocks.STONE), 0.1F);

		if (MineralogyConfig.generateRockStairs()) {
			rockStairPair = registerBlock(new RockStairs(rockPair.PairedBlock, (float) materialType.hardness,
					(float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab), name + "_" + STAIRS,
					STAIRS + materialType.materialName);
			addShapedOreRecipe(name + "_" + STAIRS, new ItemStack(rockStairPair.PairedItem, 4), "x  ", "xx ", "xxx",
					'x', rockPair.PairedItem);
		}

		if (MineralogyConfig.generateRockSlab()) {
			rockSlabPair = registerBlock(
					new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
					name + "_" + SLAB, SLAB + materialType.materialName);
			addShapedOreRecipe(name + "_" + SLAB, new ItemStack(rockSlabPair.PairedItem, 6), "xxx", 'x',
					rockPair.PairedItem);
		}

		if (MineralogyConfig.generateBrick()) {
			brickPair = registerBlock(
					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
					name + "_" + BRICK, BRICK + materialType.materialName);
			addShapedOreRecipe(name + "_" + BRICK, new ItemStack(brickPair.PairedItem, 4), "xx", "xx", 'x',
					rockPair.PairedItem);

			if (MineralogyConfig.generateBrickStairs()) {
				brickStairPair = registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
								materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + BRICK + "_" + STAIRS, STAIRS + materialType.materialName + "Brick");
				addShapedOreRecipe(name + "_" + BRICK + "_" + STAIRS, new ItemStack(brickStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', brickPair.PairedItem);
			}

			if (MineralogyConfig.generateBrickSlab()) {
				brickSlabPair = registerBlock(
						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + BRICK + "_" + SLAB, SLAB + materialType.materialName + "Brick");
				addShapedOreRecipe(name + "_" + BRICK + "_" + SLAB, new ItemStack(brickSlabPair.PairedItem, 6), "xxx",
						'x', brickPair.PairedItem);
			}
		}

		if (MineralogyConfig.generateSmooth()) {
			smoothPair = registerBlock(
					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
					name + "_" + SMOOTH, SMOOTH + materialType.materialName);
			addShapelessOreRecipe(name + "_" + SMOOTH, new ItemStack(smoothPair.PairedItem, 1),
					Ingredient.fromStacks(new ItemStack(rockPair.PairedItem, 1)),
					Ingredient.fromStacks(new ItemStack(Blocks.SAND, 1)));

			if (MineralogyConfig.generateSmoothStairs()) {
				smoothStairPair = registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
								materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + SMOOTH + "_" + STAIRS, STAIRS + materialType.materialName + "Smooth");
				addShapedOreRecipe(name + "_" + SMOOTH + "_" + STAIRS, new ItemStack(smoothStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', smoothPair.PairedItem);
			}

			if (MineralogyConfig.generateSmoothSlab()) {
				smoothSlabPair = registerBlock(
						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + SMOOTH + "_" + SLAB, SLAB + materialType.materialName + "Smooth");
				addShapedOreRecipe(name + "_" + SMOOTH + "_" + SLAB, new ItemStack(smoothSlabPair.PairedItem, 6), "xxx",
						'x', smoothPair.PairedItem);
			}

			if (MineralogyConfig.generateSmoothBrick()) {
				smoothBrickPair = registerBlock(
						new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + SMOOTH + "_" + BRICK, BRICK + materialType.materialName + "Smooth");
				addShapedOreRecipe(name + "_" + SMOOTH + "_" + BRICK, new ItemStack(smoothBrickPair.PairedItem, 4),
						"xx", "xx", 'x', smoothPair.PairedItem);

				if (MineralogyConfig.generateSmoothBrickStairs()) {
					smoothBrickStairPair = registerBlock(
							new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
									materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
							name + "_" + SMOOTH + "_" + BRICK + "_" + STAIRS, STAIRS + materialType.materialName + "SmoothBrick");
					addShapedOreRecipe(name + "_" + SMOOTH + "_" + BRICK + "_" + STAIRS,
							new ItemStack(smoothBrickStairPair.PairedItem, 4), "x  ", "xx ", "xxx", 'x',
							smoothBrickPair.PairedItem);
				}

				if (MineralogyConfig.generateSmoothBrickSlab()) {
					smoothBrickSlabPair = registerBlock(
							new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
							name + "_" + SMOOTH + "_" + BRICK + "_" + SLAB, SLAB + materialType.materialName + "SmoothBrick");
					addShapedOreRecipe(name + "_" + SMOOTH + "_" + BRICK + "_" + SLAB,
							new ItemStack(smoothBrickSlabPair.PairedItem, 6), "xxx", 'x', smoothBrickPair.PairedItem);
				}
			}
		}
	}
}
