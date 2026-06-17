package com.mcmoddev.mineralogy.worldgen;

import java.util.function.Predicate;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.OreGenerationSettings;
import com.mcmoddev.mineralogy.blocks.Rock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MinableConfig;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyOreGeneration {
	private static final Predicate<IBlockState> MINERALOGY_ORE_TARGETS = state -> {
		if (MinableConfig.IS_ROCK.test(state)) {
			return true;
		}

		Block block = state.getBlock();
		return block instanceof Rock && ((Rock) block).isStoneEquivalent;
	};

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

		MinableConfig minableConfig = new MinableConfig(MINERALOGY_ORE_TARGETS, ore.getDefaultState(),
				settings.quantity());
		int wholeCount = (int) settings.frequency();
		float fractionalChance = (float) (settings.frequency() - wholeCount);

		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			if (wholeCount > 0) {
				biome.addFeature(
						GenerationStage.Decoration.UNDERGROUND_ORES,
						Biome.createCompositeFeature(
								Feature.MINABLE,
								minableConfig,
								Biome.COUNT_RANGE,
								new CountRangeConfig(wholeCount, settings.minY(), settings.minY(),
										settings.maxY())));
			}

			if (fractionalChance > 0.0F) {
				biome.addFeature(
						GenerationStage.Decoration.UNDERGROUND_ORES,
						Biome.createCompositeFeature(
								Feature.MINABLE,
								minableConfig,
								Biome.CHANCE_RANGE,
								new ChanceRangeConfig(fractionalChance, settings.minY(), settings.minY(),
										settings.maxY())));
			}
		}
	}
}
