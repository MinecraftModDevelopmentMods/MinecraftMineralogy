package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.blocks.Chert;
import com.mcmoddev.mineralogy.blocks.DryWall;
import com.mcmoddev.mineralogy.blocks.Gypsum;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.data.Material;
import com.mcmoddev.mineralogy.data.MaterialData;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.util.BlockItemPair;
import com.mcmoddev.mineralogy.util.RecipeHelper;
import com.mcmoddev.mineralogy.util.RegistrationHelper;

import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Blocks {
	private static boolean initDone = false;
	private static CreativeTabs mineralogyTab;
	
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
		BlockItemPair blockPumice;
		BlockItemPair[] drywalls = new BlockItemPair[16];
		
		mineralogyTab = MinIoC.getInstance().resolve(CreativeTabs.class);
		MaterialData.toArray().forEach(material -> addStoneType(material));
		
		MineralogyRegistry.sedimentaryStones.add(net.minecraft.init.Blocks.SANDSTONE);

		blockChert = RegistrationHelper.registerBlock(new Chert(mineralogyTab), Constants.CHERT, Constants.BLOCK_CHERT);
		MineralogyRegistry.sedimentaryStones.add(blockChert.PairedBlock);

		blockGypsum = RegistrationHelper.registerBlock(new Gypsum(mineralogyTab), Constants.GYPSUM.toLowerCase(), Constants.BLOCK_GYPSUM);
		MineralogyRegistry.sedimentaryStones.add(blockGypsum.PairedBlock);
		
		IoC.register(BlockItemPair.class, blockGypsum, Constants.BLOCK_GYPSUM, Mineralogy.MODID);
		
		blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.GROUND, mineralogyTab), Constants.PUMICE, Constants.BLOCK_PUMICE);
		MineralogyRegistry.igneousStones.add(blockPumice.PairedBlock);
		
		IoC.register(BlockItemPair.class, blockPumice, Constants.BLOCK_PUMICE, Mineralogy.MODID);
		
		for (int i = 0; i < 16; i++) {
			drywalls[i] = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
					Constants.DRYWALL + Constants.colorSuffixesTwo[i]);
			
			IoC.register(BlockItemPair.class, drywalls[i], Constants.DRYWALL + Constants.colorSuffixesTwo[i], Mineralogy.MODID);
			
			RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
					Constants.DRYWALL_WHITE,
					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
		}
		
		initDone = true;
	}

	protected static void addStoneType(Material materialType) {
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

		rockPair = RegistrationHelper.registerBlock(new Rock(true, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab), name, name);

		RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.COBBLESTONE.toUpperCase(), new ItemStack(net.minecraft.init.Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
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
					(float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab), name + "_" + Constants.STAIRS,
					Constants.STAIRS + materialType.materialName);
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.STAIRS, new ItemStack(rockStairPair.PairedItem, 4), "x  ", "xx ", "xxx",
					'x', rockPair.PairedItem);
		}

		if (MineralogyConfig.generateRockSlab()) {
			rockSlabPair = RegistrationHelper.registerBlock(
					new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
					name + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName);
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SLAB, new ItemStack(rockSlabPair.PairedItem, 6), "xxx", 'x',
					rockPair.PairedItem);
		}

		if (MineralogyConfig.generateBrick()) {
			brickPair = RegistrationHelper.registerBlock(
					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
					name + "_" + Constants.BRICK, Constants.BRICK + materialType.materialName);
			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK, new ItemStack(brickPair.PairedItem, 4), "xx", "xx", 'x',
					rockPair.PairedItem);

			if (MineralogyConfig.generateBrickStairs()) {
				brickStairPair = RegistrationHelper.registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
								materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Brick");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.STAIRS, new ItemStack(brickStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', brickPair.PairedItem);
			}

			if (MineralogyConfig.generateBrickSlab()) {
				brickSlabPair = RegistrationHelper.registerBlock(
						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Brick");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.SLAB, new ItemStack(brickSlabPair.PairedItem, 6), "xxx",
						'x', brickPair.PairedItem);
			}
		}

		if (MineralogyConfig.generateSmooth()) {
			smoothPair = RegistrationHelper.registerBlock(
					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
					name + "_" + Constants.SMOOTH, Constants.SMOOTH + materialType.materialName);
			RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.SMOOTH, new ItemStack(smoothPair.PairedItem, 1),
					Ingredient.fromStacks(new ItemStack(rockPair.PairedItem, 1)),
					Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.SAND, 1)));

			if (MineralogyConfig.generateSmoothStairs()) {
				smoothStairPair = RegistrationHelper.registerBlock(
						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
								materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Smooth");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, new ItemStack(smoothStairPair.PairedItem, 4),
						"x  ", "xx ", "xxx", 'x', smoothPair.PairedItem);
			}

			if (MineralogyConfig.generateSmoothSlab()) {
				smoothSlabPair = RegistrationHelper.registerBlock(
						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Smooth");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, new ItemStack(smoothSlabPair.PairedItem, 6), "xxx",
						'x', smoothPair.PairedItem);
			}

			if (MineralogyConfig.generateSmoothBrick()) {
				smoothBrickPair = RegistrationHelper.registerBlock(
						new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
						name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, Constants.BRICK + materialType.materialName + "Smooth");
				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, new ItemStack(smoothBrickPair.PairedItem, 4),
						"xx", "xx", 'x', smoothPair.PairedItem);

				if (MineralogyConfig.generateSmoothBrickStairs()) {
					smoothBrickStairPair = RegistrationHelper.registerBlock(
							new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
									materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "SmoothBrick");
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS,
							new ItemStack(smoothBrickStairPair.PairedItem, 4), "x  ", "xx ", "xxx", 'x',
							smoothBrickPair.PairedItem);
				}

				if (MineralogyConfig.generateSmoothBrickSlab()) {
					smoothBrickSlabPair = RegistrationHelper.registerBlock(
							new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, mineralogyTab),
							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "SmoothBrick");
					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB,
							new ItemStack(smoothBrickSlabPair.PairedItem, 6), "xxx", 'x', smoothBrickPair.PairedItem);
				}
			}
		}
	}
}
