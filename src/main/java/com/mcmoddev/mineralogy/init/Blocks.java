package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.data.*;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.RockType;
//import com.mcmoddev.mineralogy.data.Material;
//import com.mcmoddev.mineralogy.init.MineralogyRegistry;
//import com.mcmoddev.mineralogy.ioc.MinIoC;
//import com.mcmoddev.mineralogy.lib.exceptions.TabNotFoundException;
//import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;
//import com.mcmoddev.mineralogy.worldgen.StoneReplacer;
import com.mcmoddev.mineralogy.util.BlockItemPair;

//import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.block.model.ModelResourceLocation;
//import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.Mod.EventHandler;
//import net.minecraftforge.fml.common.Mod.Instance;
//import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
//import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
//import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.stream.Collectors;

//
//import com.mcmoddev.mineralogy.Constants;
//import com.mcmoddev.mineralogy.Mineralogy;
//import com.mcmoddev.mineralogy.MineralogyConfig;
//import com.mcmoddev.mineralogy.blocks.Chert;
//import com.mcmoddev.mineralogy.blocks.DryWall;
//import com.mcmoddev.mineralogy.blocks.Gypsum;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockStairs;
//import com.mcmoddev.mineralogy.blocks.RockSlab;
//import com.mcmoddev.mineralogy.blocks.RockStairs;
//import com.mcmoddev.mineralogy.blocks.RockWall;
//import com.mcmoddev.mineralogy.data.Material;
//import com.mcmoddev.mineralogy.data.MaterialData;
//import com.mcmoddev.mineralogy.ioc.MinIoC;
//import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;
//import com.mcmoddev.mineralogy.util.BlockItemPair;
//import com.mcmoddev.mineralogy.util.RecipeHelper;
//import com.mcmoddev.mineralogy.util.RegistrationHelper;
//
//import net.minecraft.block.SoundType;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.crafting.Ingredient;
//import net.minecraftforge.fml.common.registry.GameRegistry;
//
import com.mcmoddev.mineralogy.blocks.RockWall;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class Blocks {

    public static final Rock andesite = null;
    public static final Rock basalt = null;
    public static final Rock diorite = null;
    public static final Rock granite = null;
    public static final Rock rhyolite = null;
    public static final Rock pegmatite = null;
    public static final Rock shale = null;
    public static final Rock conglomerate = null;
    public static final Rock dolomite = null;
    public static final Rock limestone = null;
    public static final Rock marble = null;
    public static final Rock slate = null;
    public static final Rock schist = null;
    public static final Rock gneiss = null;
    public static final Rock phyllite = null;
    public static final Rock amphibolite = null;
    
    public static final Rock andesite_smooth = null;
    public static final Rock basalt_smooth = null;
    public static final Rock diorite_smooth = null;
    public static final Rock granite_smooth = null;
    public static final Rock rhyolite_smooth = null;
    public static final Rock pegmatite_smooth = null;
    public static final Rock shale_smooth = null;
    public static final Rock conglomerate_smooth = null;
    public static final Rock dolomite_smooth = null;
    public static final Rock limestone_smooth = null;
    public static final Rock marble_smooth = null;
    public static final Rock slate_smooth = null;
    public static final Rock schist_smooth = null;
    public static final Rock gneiss_smooth = null;
    public static final Rock phyllite_smooth = null;
    public static final Rock amphibolite_smooth = null;
    
    public static final RockStairs andesite_stairs = null;
    public static final RockStairs basalt_stairs = null;
    public static final RockStairs diorite_stairs = null;
    public static final RockStairs granite_stairs = null;
    public static final RockStairs rhyolite_stairs = null;
    public static final RockStairs pegmatite_stairs = null;
    public static final RockStairs shale_stairs = null;
    public static final RockStairs conglomerate_stairs = null;
    public static final RockStairs dolomite_stairs = null;
    public static final RockStairs limestone_stairs = null;
    public static final RockStairs marble_stairs = null;
    public static final RockStairs slate_stairs = null;
    public static final RockStairs schist_stairs = null;
    public static final RockStairs gneiss_stairs = null;
    public static final RockStairs phyllite_stairs = null;
    public static final RockStairs amphibolite_stairs = null;
    
    public static final RockStairs andesite_smooth_stairs = null;
    public static final RockStairs basalt_smooth_stairs = null;
    public static final RockStairs diorite_smooth_stairs = null;
    public static final RockStairs granite_smooth_stairs = null;
    public static final RockStairs rhyolite_smooth_stairs = null;
    public static final RockStairs pegmatite_smooth_stairs = null;
    public static final RockStairs shale_smooth_stairs = null;
    public static final RockStairs conglomerate_smooth_stairs = null;
    public static final RockStairs dolomite_smooth_stairs = null;
    public static final RockStairs limestone_smooth_stairs = null;
    public static final RockStairs marble_smooth_stairs = null;
    public static final RockStairs slate_smooth_stairs = null;
    public static final RockStairs schist_smooth_stairs = null;
    public static final RockStairs gneiss_smooth_stairs = null;
    public static final RockStairs phyllite_smooth_stairs = null;
    public static final RockStairs amphibolite_smooth_stairs = null;
    
    public static final Rock andesite_brick = null;
    public static final Rock basalt_brick = null;
    public static final Rock diorite_brick = null;
    public static final Rock granite_brick = null;
    public static final Rock rhyolite_brick = null;
    public static final Rock pegmatite_brick = null;
    public static final Rock shale_brick = null;
    public static final Rock conglomerate_brick = null;
    public static final Rock dolomite_brick = null;
    public static final Rock limestone_brick = null;
    public static final Rock marble_brick = null;
    public static final Rock slate_brick = null;
    public static final Rock schist_brick = null;
    public static final Rock gneiss_brick = null;
    public static final Rock phyllite_brick = null;
    public static final Rock amphibolite_brick = null;
    
    public static final Rock andesite_smooth_brick = null;
    public static final Rock basalt_smooth_brick = null;
    public static final Rock diorite_smooth_brick = null;
    public static final Rock granite_smooth_brick = null;
    public static final Rock rhyolite_smooth_brick = null;
    public static final Rock pegmatite_smooth_brick = null;
    public static final Rock shale_smooth_brick = null;
    public static final Rock conglomerate_smooth_brick = null;
    public static final Rock dolomite_smooth_brick = null;
    public static final Rock limestone_smooth_brick = null;
    public static final Rock marble_smooth_brick = null;
    public static final Rock slate_smooth_brick = null;
    public static final Rock schist_smooth_brick = null;
    public static final Rock gneiss_smooth_brick = null;
    public static final Rock phyllite_smooth_brick = null;
    public static final Rock amphibolite_smooth_brick = null;
    
    public static final RockStairs andesite_brick_stairs = null;
    public static final RockStairs basalt_brick_stairs = null;
    public static final RockStairs diorite_brick_stairs = null;
    public static final RockStairs granite_brick_stairs = null;
    public static final RockStairs rhyolite_brick_stairs = null;
    public static final RockStairs pegmatite_brick_stairs = null;
    public static final RockStairs shale_brick_stairs = null;
    public static final RockStairs conglomerate_brick_stairs = null;
    public static final RockStairs dolomite_brick_stairs = null;
    public static final RockStairs limestone_brick_stairs = null;
    public static final RockStairs marble_brick_stairs = null;
    public static final RockStairs slate_brick_stairs = null;
    public static final RockStairs schist_brick_stairs = null;
    public static final RockStairs gneiss_brick_stairs = null;
    public static final RockStairs phyllite_brick_stairs = null;
    public static final RockStairs amphibolite_brick_stairs = null;
    
    public static final RockStairs andesite_smooth_brick_stairs = null;
    public static final RockStairs basalt_smooth_brick_stairs = null;
    public static final RockStairs diorite_smooth_brick_stairs = null;
    public static final RockStairs granite_smooth_brick_stairs = null;
    public static final RockStairs rhyolite_smooth_brick_stairs = null;
    public static final RockStairs pegmatite_smooth_brick_stairs = null;
    public static final RockStairs shale_smooth_brick_stairs = null;
    public static final RockStairs conglomerate_smooth_brick_stairs = null;
    public static final RockStairs dolomite_smooth_brick_stairs = null;
    public static final RockStairs limestone_smooth_brick_stairs = null;
    public static final RockStairs marble_smooth_brick_stairs = null;
    public static final RockStairs slate_smooth_brick_stairs = null;
    public static final RockStairs schist_smooth_brick_stairs = null;
    public static final RockStairs gneiss_smooth_brick_stairs = null;
    public static final RockStairs phyllite_smooth_brick_stairs = null;
    public static final RockStairs amphibolite_smooth_brick_stairs = null;
    
//    public static final Rock andesite_wall = null;
    public static final RockWall basalt_wall = null;
//    public static final Rock diorite_wall = null;
//    public static final Rock granite_wall = null;
//    public static final Rock rhyolite_wall = null;
//    public static final Rock pegmatite_wall = null;
//    public static final Rock shale_wall = null;
//    public static final Rock conglomerate_wall = null;
//    public static final Rock dolomite_wall = null;
//    public static final Rock limestone_wall = null;
//    public static final Rock marble_wall = null;
//    public static final Rock slate_wall = null;
//    public static final Rock schist_wall = null;
//    public static final Rock gneiss_wall = null;
//    public static final Rock phyllite_wall = null;
//    public static final Rock amphibolite_wall = null;
//	
//	public static final Rock andesite_smooth_wall = null;
//    public static final Rock basalt_smooth_wall = null;
//    public static final Rock diorite_smooth_wall = null;
//    public static final Rock granite_smooth_wall = null;
//    public static final Rock rhyolite_smooth_wall = null;
//    public static final Rock pegmatite_smooth_wall = null;
//    public static final Rock shale_smooth_wall = null;
//    public static final Rock conglomerate_smooth_wall = null;
//    public static final Rock dolomite_smooth_wall = null;
//    public static final Rock limestone_smooth_wall = null;
//    public static final Rock marble_smooth_wall = null;
//    public static final Rock slate_smooth_wall = null;
//    public static final Rock schist_smooth_wall = null;
//    public static final Rock gneiss_smooth_wall = null;
//    public static final Rock phyllite_smooth_wall = null;
//    public static final Rock amphibolite_smooth_wall = null;
//	
//	public static final Rock andesite_brick_wall = null;
//    public static final Rock basalt_brick_wall = null;
//    public static final Rock diorite_brick_wall = null;
//    public static final Rock granite_brick_wall = null;
//    public static final Rock rhyolite_brick_wall = null;
//    public static final Rock pegmatite_brick_wall = null;
//    public static final Rock shale_brick_wall = null;
//    public static final Rock conglomerate_brick_wall = null;
//    public static final Rock dolomite_brick_wall = null;
//    public static final Rock limestone_brick_wall = null;
//    public static final Rock marble_brick_wall = null;
//    public static final Rock slate_brick_wall = null;
//    public static final Rock schist_brick_wall = null;
//    public static final Rock gneiss_brick_wall = null;
//    public static final Rock phyllite_brick_wall = null;
//    public static final Rock amphibolite_brick_wall = null;
//	
//	public static final Rock andesite_smooth_brick_wall = null;
//    public static final Rock basalt_smooth_brick_wall = null;
//    public static final Rock diorite_smooth_brick_wall = null;
//    public static final Rock granite_smooth_brick_wall = null;
//    public static final Rock rhyolite_smooth_brick_wall = null;
//    public static final Rock pegmatite_smooth_brick_wall = null;
//    public static final Rock shale_smooth_brick_wall = null;
//    public static final Rock conglomerate_smooth_brick_wall = null;
//    public static final Rock dolomite_smooth_brick_wall = null;
//    public static final Rock limestone_smooth_brick_wall = null;
//    public static final Rock marble_smooth_brick_wall = null;
//    public static final Rock slate_smooth_brick_wall = null;
//    public static final Rock schist_smooth_brick_wall = null;
//    public static final Rock gneiss_smooth_brick_wall = null;
//    public static final Rock phyllite_smooth_brick_wall = null;
//    public static final Rock amphibolite_smooth_brick_wall = null;
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
    	for (com.mcmoddev.mineralogy.data.Material material : MaterialData.toArray()) {
			event.getRegistry().register(material.toRock(false, false));
			event.getRegistry().register(material.toRock(true, false));
			event.getRegistry().register(material.toRock(false, true));
			event.getRegistry().register(material.toRock(true, true));
			event.getRegistry().register(material.toRockStairs(false, false));
			event.getRegistry().register(material.toRockStairs(true, false));
			event.getRegistry().register(material.toRockStairs(false, true));
			event.getRegistry().register(material.toRockStairs(true, true));
			//event.getRegistry().register(material.toRockWall(false, false));
//			event.getRegistry().register(material.toRockWall(true, false));
//			event.getRegistry().register(material.toRockWall(false, true));
//			event.getRegistry().register(material.toRockWall(true, true));
		}
    	
    	event.getRegistry().register(MaterialData.BASALT.toRockWall(false, false));
    }
}

//public class Blocks {
//	private static boolean initDone = false;
//	
//	protected Blocks() {
//		throw new IllegalAccessError("Not a instantiable class");
//	}
//
//	/**
//	 *
//	 */
//	public static void init() {
//		if (initDone) {
//			return;
//		}
//
////		MinIoC IoC =  MinIoC.getInstance();
////		
////		BlockItemPair blockChert;
////		BlockItemPair blockGypsum;
////		BlockItemPair blockPumice;
////		BlockItemPair[] drywalls = new BlockItemPair[16];
////		
////		MaterialData.toArray().forEach(material -> addStoneType(material));		
////		
////		MineralogyRegistry.sedimentaryStones.add(net.minecraft.init.Blocks.SANDSTONE);
////
////		blockChert = RegistrationHelper.registerBlock(new Chert(), Constants.CHERT, Constants.BLOCK_CHERT);
////		MineralogyRegistry.sedimentaryStones.add(blockChert.PairedBlock);
////		MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, blockChert.PairedBlock);
////		
////		blockGypsum = RegistrationHelper.registerBlock(new Gypsum(), Constants.GYPSUM.toLowerCase(), Constants.BLOCK_GYPSUM);
////		MineralogyRegistry.sedimentaryStones.add(blockGypsum.PairedBlock);
////		
////		IoC.register(BlockItemPair.class, blockGypsum, Constants.BLOCK_GYPSUM, Mineralogy.MODID);
////		
////		blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.STONE), Constants.PUMICE, Constants.BLOCK_PUMICE);
////		MineralogyRegistry.igneousStones.add(blockPumice.PairedBlock);
////		MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, blockPumice.PairedBlock);
////		
////		IoC.register(BlockItemPair.class, blockPumice, Constants.BLOCK_PUMICE, Mineralogy.MODID);
////		
////		IDynamicTabProvider tabProvider = IoC.resolve(IDynamicTabProvider.class);
////		
////		for (int i = 0; i < 16; i++) {
////			if (MineralogyConfig.groupCreativeTabItemsByType())
////				tabProvider.setTabItemMapping("Item", Constants.DRYWALL + "_" + Constants.colorSuffixes[i]);
////			
////			drywalls[i] = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
////					Constants.DRYWALL + Constants.colorSuffixesTwo[i]);
////			
////			IoC.register(BlockItemPair.class, drywalls[i], Constants.DRYWALL + Constants.colorSuffixesTwo[i], Mineralogy.MODID);
////			
////			RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
////					Constants.DRYWALL_WHITE,
////					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
////		}
//		
//		initDone = true;
//	}
//
//	protected static void addStoneType(Material materialType) {
//		String name = materialType.materialName.toLowerCase();
//
//		final BlockItemPair rockPair;
//		final BlockItemPair rockStairPair;
//		final BlockItemPair rockSlabPair;
//		final BlockItemPair rockWallPair;
//		final BlockItemPair brickPair;
//		final BlockItemPair brickStairPair;
//		final BlockItemPair brickSlabPair;
//		final BlockItemPair brickWallPair;
//		final BlockItemPair smoothPair;
//		final BlockItemPair smoothStairPair;
//		final BlockItemPair smoothSlabPair;
//		final BlockItemPair smoothWallPair;
//		final BlockItemPair smoothBrickPair;
//		final BlockItemPair smoothBrickStairPair;
//		final BlockItemPair smoothBrickSlabPair;
//		final BlockItemPair smoothBrickWallPair;
//
//		rockPair = RegistrationHelper.registerBlock(new Rock(true, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name, "stone" + materialType.materialName);
//
//		if (materialType.cobbleEquivilent)
//			MineralogyRegistry.BlocksToRegister.put(Constants.COBBLESTONE, rockPair.PairedBlock);
//		
//		RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.COBBLESTONE.toUpperCase(), new ItemStack(net.minecraft.init.Blocks.COBBLESTONE, 4),
//				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
//				Ingredient.fromStacks(new ItemStack(rockPair.PairedItem)),
//				Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)),
//				Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)));
//
//		switch (materialType.rockType) {
//			case IGNEOUS:
//				MineralogyRegistry.igneousStones.add(rockPair.PairedBlock);
//				break;
//			case METAMORPHIC:
//				MineralogyRegistry.metamorphicStones.add(rockPair.PairedBlock);
//				break;
//			case SEDIMENTARY:
//				MineralogyRegistry.sedimentaryStones.add(rockPair.PairedBlock);
//				break;
//			case ANY:
//				MineralogyRegistry.sedimentaryStones.add(rockPair.PairedBlock);
//				MineralogyRegistry.metamorphicStones.add(rockPair.PairedBlock);
//				MineralogyRegistry.igneousStones.add(rockPair.PairedBlock);
//				break;
//		}
//
//		GameRegistry.addSmelting(rockPair.PairedBlock, new ItemStack(net.minecraft.init.Blocks.STONE), 0.1F);
//
//		// no point in ore dicting these recipes I think
//		if (MineralogyConfig.generateRockStairs()) {
//			rockStairPair = RegistrationHelper.registerBlock(new RockStairs(rockPair.PairedBlock, (float) materialType.hardness,
//					(float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name + "_" + Constants.STAIRS,
//					Constants.STAIRS + materialType.materialName);
//			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.STAIRS, new ItemStack(rockStairPair.PairedItem, 4), "x  ", "xx ", "xxx",
//					'x', rockPair.PairedItem);
//		}
//
//		if (MineralogyConfig.generateRockSlab()) {
//			rockSlabPair = RegistrationHelper.registerBlock(
//					new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//					name + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName);
//			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SLAB, new ItemStack(rockSlabPair.PairedItem, 6), "xxx", 'x',
//					rockPair.PairedItem);
//		}
//		
//		if (MineralogyConfig.generateRockWall()) {
//			rockWallPair = RegistrationHelper.registerBlock(
//					new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//					name + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
//			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.WALL, new ItemStack(rockWallPair.PairedItem, 6), "xxx", "xxx", 'x',
//					rockPair.PairedItem);
//		}
//
//		if (MineralogyConfig.generateBrick()) {
//			brickPair = RegistrationHelper.registerBlock(
//					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//					name + "_" + Constants.BRICK, "stone" + materialType.materialName + "Brick");
//			RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK, new ItemStack(brickPair.PairedItem, 4), "xx", "xx", 'x',
//					rockPair.PairedItem);
//
//			if (MineralogyConfig.generateBrickStairs()) {
//				brickStairPair = RegistrationHelper.registerBlock(
//						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
//								materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Brick");
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.STAIRS, new ItemStack(brickStairPair.PairedItem, 4),
//						"x  ", "xx ", "xxx", 'x', brickPair.PairedItem);
//			}
//
//			if (MineralogyConfig.generateBrickSlab()) {
//				brickSlabPair = RegistrationHelper.registerBlock(
//						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Brick");
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.SLAB, new ItemStack(brickSlabPair.PairedItem, 6), "xxx",
//						'x', brickPair.PairedItem);
//			}
//			
//			if (MineralogyConfig.generateBrickWall()) {
//				brickWallPair = RegistrationHelper.registerBlock(
//						new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.WALL, new ItemStack(brickWallPair.PairedItem, 6), "xxx", "xxx", 'x',
//						brickPair.PairedItem);
//			}
//		}
//
//		if (MineralogyConfig.generateSmooth()) {
//			smoothPair = RegistrationHelper.registerBlock(
//					new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//					name + "_" + Constants.SMOOTH, "stone" + materialType.materialName + "Smooth");
//			RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.SMOOTH, new ItemStack(smoothPair.PairedItem, 1),
//					Ingredient.fromStacks(new ItemStack(rockPair.PairedItem, 1)),
//					Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.SAND, 1)));
//
//			if (MineralogyConfig.generateSmoothStairs()) {
//				smoothStairPair = RegistrationHelper.registerBlock(
//						new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
//								materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Smooth");
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, new ItemStack(smoothStairPair.PairedItem, 4),
//						"x  ", "xx ", "xxx", 'x', smoothPair.PairedItem);
//			}
//
//			if (MineralogyConfig.generateSmoothSlab()) {
//				smoothSlabPair = RegistrationHelper.registerBlock(
//						new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Smooth");
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, new ItemStack(smoothSlabPair.PairedItem, 6), "xxx",
//						'x', smoothPair.PairedItem);
//			}
//
//			if (MineralogyConfig.generateSmoothWall()) {
//				smoothWallPair = RegistrationHelper.registerBlock(
//						new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.SMOOTH + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.WALL, new ItemStack(smoothWallPair.PairedItem, 6), "xxx", "xxx", 'x',
//						smoothPair.PairedItem);
//			}
//			
//			if (MineralogyConfig.generateSmoothBrick()) {
//				smoothBrickPair = RegistrationHelper.registerBlock(
//						new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//						name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, "stone" + materialType.materialName + "SmoothBrick");
//				RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, new ItemStack(smoothBrickPair.PairedItem, 4),
//						"xx", "xx", 'x', smoothPair.PairedItem);
//
//				if (MineralogyConfig.generateSmoothBrickStairs()) {
//					smoothBrickStairPair = RegistrationHelper.registerBlock(
//							new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
//									materialType.toolHardnessLevel, SoundType.STONE),
//							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "SmoothBrick");
//					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS,
//							new ItemStack(smoothBrickStairPair.PairedItem, 4), "x  ", "xx ", "xxx", 'x',
//							smoothBrickPair.PairedItem);
//				}
//
//				if (MineralogyConfig.generateSmoothBrickSlab()) {
//					smoothBrickSlabPair = RegistrationHelper.registerBlock(
//							new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "SmoothBrick");
//					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB,
//							new ItemStack(smoothBrickSlabPair.PairedItem, 6), "xxx", 'x', smoothBrickPair.PairedItem);
//				}
//				
//				if (MineralogyConfig.generateSmoothBrickWall()) {
//					smoothBrickWallPair = RegistrationHelper.registerBlock(
//							new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
//							name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
//					RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, new ItemStack(smoothBrickWallPair.PairedItem, 6), "xxx", "xxx", 'x',
//							smoothBrickPair.PairedItem);
//				}
//			}
//		}
//	}
//}
