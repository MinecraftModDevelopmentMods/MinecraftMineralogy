package com.mcmoddev.mineralogy.worldgen;

import java.util.function.Predicate;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.OreGenerationSettings;
import com.mcmoddev.mineralogy.blocks.Rock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyOreGeneration {
	private static final Predicate<BlockState> MINERALOGY_ORE_TARGET_PREDICATE = state -> {
		if (OreFeatureConfig.FillerBlockType.NATURAL_STONE.func_214738_b().test(state)) {
			return true;
		}

		Block block = state.getBlock();
		return block instanceof Rock && ((Rock) block).isStoneEquivalent;
	};
	private static final OreFeatureConfig.FillerBlockType MINERALOGY_ORE_TARGETS =
			OreFeatureConfig.FillerBlockType.create("MINERALOGY_ORE_TARGETS", "mineralogy_ore_targets",
					MINERALOGY_ORE_TARGET_PREDICATE);

	private MineralogyOreGeneration() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static void register() {
		addOre("sulfur_ore", MineralogyConfig.sulfurOre());
		addOre("phosphorous_ore", MineralogyConfig.phosphorousOre());
		addOre("nitrate_ore", MineralogyConfig.nitrateOre());
	}

	private static void addOre(String oreName, OreGenerationSettings settings) {
		Block ore = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, oreName));
		if (ore == null || settings.quantity() <= 0 || settings.frequency() <= 0.0D
				|| settings.maxY() <= settings.minY()) {
			return;
		}

		OreFeatureConfig oreConfig = new OreFeatureConfig(MINERALOGY_ORE_TARGETS, ore.getDefaultState(),
				settings.quantity());
		int wholeCount = (int) settings.frequency();
		float fractionalChance = (float) (settings.frequency() - wholeCount);

		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			if (wholeCount > 0) {
				biome.addFeature(
						GenerationStage.Decoration.UNDERGROUND_ORES,
						Biome.createDecoratedFeature(
								Feature.ORE,
								oreConfig,
								Placement.COUNT_RANGE,
								new CountRangeConfig(wholeCount, settings.minY(), settings.minY(),
										settings.maxY())));
			}

			if (fractionalChance > 0.0F) {
				biome.addFeature(
						GenerationStage.Decoration.UNDERGROUND_ORES,
						Biome.createDecoratedFeature(
								Feature.ORE,
								oreConfig,
								Placement.CHANCE_RANGE,
								new ChanceRangeConfig(fractionalChance, settings.minY(), settings.minY(),
										settings.maxY())));
			}
		}
	}
}
