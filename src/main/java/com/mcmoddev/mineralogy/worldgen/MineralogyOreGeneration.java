package com.mcmoddev.mineralogy.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.OreGenerationSettings;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyOreGeneration {
	private static final RuleTestType<MineralogyOreRuleTest> MINERALOGY_ORE_TARGET_TYPE =
			RuleTestType.register(Mineralogy.MODID + ":ore_targets",
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
		if (event.getCategory() == BiomeCategory.NETHER || event.getCategory() == BiomeCategory.THEEND
				|| event.getCategory() == BiomeCategory.NONE) {
			return;
		}

		for (ConfiguredFeature<?, ?> feature : CONFIGURED_FEATURES) {
			event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
		}
	}

	private static void addOre(String oreName, OreGenerationSettings settings) {
		Block ore = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, oreName));
		if (ore == null || settings.quantity() <= 0 || settings.frequency() <= 0.0D
				|| settings.maxY() <= settings.minY()) {
			return;
		}

		OreConfiguration oreConfig = new OreConfiguration(MINERALOGY_ORE_TARGETS, ore.defaultBlockState(),
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
					baseOreFeature(oreConfig, settings).rarity(chance));
		}
	}

	private static ConfiguredFeature<?, ?> baseOreFeature(OreConfiguration oreConfig, OreGenerationSettings settings) {
		return Feature.ORE.configured(oreConfig)
				.rangeUniform(VerticalAnchor.absolute(settings.minY()), VerticalAnchor.absolute(settings.maxY()))
				.squared();
	}

	private static void addConfiguredOreFeature(String oreName, String suffix, OreConfiguration oreConfig,
			ConfiguredFeature<?, ?> configuredFeature) {
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
				new ResourceLocation(Mineralogy.MODID, oreName + "_" + suffix), configuredFeature);
		CONFIGURED_FEATURES.add(configuredFeature);
	}

	private static final class MineralogyOreRuleTest extends RuleTest {
		@Override
		public boolean test(BlockState state, Random random) {
			if (OreConfiguration.Predicates.NATURAL_STONE.test(state, random)) {
				return true;
			}

			Block block = state.getBlock();
			return block instanceof Rock && ((Rock) block).isStoneEquivalent;
		}

		@Override
		protected RuleTestType<?> getType() {
			return MINERALOGY_ORE_TARGET_TYPE;
		}
	}
}
