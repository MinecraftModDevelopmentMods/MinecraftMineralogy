package com.mcmoddev.mineralogy.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.OreGenerationSettings;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureSpreadConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.template.IRuleTestType;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyOreGeneration {
	private static final IRuleTestType<MineralogyOreRuleTest> MINERALOGY_ORE_TARGET_TYPE =
			IRuleTestType.register(Mineralogy.MODID + ":ore_targets",
					Codec.unit(MineralogyOreRuleTest::new));
	private static final RuleTest MINERALOGY_ORE_TARGETS = new MineralogyOreRuleTest();
	private static final List<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = new ArrayList<ConfiguredFeature<?, ?>>();

	private MineralogyOreGeneration() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static void registerConfiguredFeatures() {
		CONFIGURED_FEATURES.clear();
		addOre("sulfur_ore", MineralogyConfig.sulfurOre());
		addOre("phosphorous_ore", MineralogyConfig.phosphorousOre());
		addOre("nitrate_ore", MineralogyConfig.nitrateOre());
	}

	public static void onBiomeLoading(BiomeLoadingEvent event) {
		if (event.getCategory() == Category.NETHER || event.getCategory() == Category.THEEND
				|| event.getCategory() == Category.NONE) {
			return;
		}

		for (ConfiguredFeature<?, ?> feature : CONFIGURED_FEATURES) {
			event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
		}
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

		if (wholeCount > 0) {
			addConfiguredOreFeature(oreName, "count", oreConfig,
					baseOreFeature(oreConfig, settings).count(wholeCount));
		}

		if (fractionalChance > 0.0F) {
			int chance = Math.max(1, (int) Math.ceil(1.0F / fractionalChance));
			addConfiguredOreFeature(oreName, "chance", oreConfig,
					baseOreFeature(oreConfig, settings).chance(chance));
		}
	}

	private static ConfiguredFeature<?, ?> baseOreFeature(OreFeatureConfig oreConfig, OreGenerationSettings settings) {
		return Feature.ORE.withConfiguration(oreConfig)
				.withPlacement(Placement.RANGE.configure(new TopSolidRangeConfig(settings.minY(),
						settings.minY(), settings.maxY())))
				.square();
	}

	private static void addConfiguredOreFeature(String oreName, String suffix, OreFeatureConfig oreConfig,
			ConfiguredFeature<?, ?> configuredFeature) {
		WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
				new ResourceLocation(Mineralogy.MODID, oreName + "_" + suffix), configuredFeature);
		CONFIGURED_FEATURES.add(configuredFeature);
	}

	private static final class MineralogyOreRuleTest extends RuleTest {
		@Override
		public boolean test(BlockState state, Random random) {
			if (OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD.test(state, random)) {
				return true;
			}

			Block block = state.getBlock();
			return block instanceof Rock && ((Rock) block).isStoneEquivalent;
		}

		@Override
		protected IRuleTestType<?> getType() {
			return MINERALOGY_ORE_TARGET_TYPE;
		}
	}
}
