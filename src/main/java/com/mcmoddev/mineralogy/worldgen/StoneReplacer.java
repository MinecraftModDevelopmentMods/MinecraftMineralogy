package com.mcmoddev.mineralogy.worldgen;

import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class StoneReplacer extends Feature<NoneFeatureConfiguration> {
	public static final StoneReplacer FEATURE = new StoneReplacer();
	private static final ResourceLocation[] VANILLA_MATCHING_STONE_FEATURES = new ResourceLocation[] {
			new ResourceLocation("minecraft", "ore_granite_upper"),
			new ResourceLocation("minecraft", "ore_granite_lower"),
			new ResourceLocation("minecraft", "ore_diorite_upper"),
			new ResourceLocation("minecraft", "ore_diorite_lower"),
			new ResourceLocation("minecraft", "ore_andesite_upper"),
			new ResourceLocation("minecraft", "ore_andesite_lower"),
			new ResourceLocation("minecraft", "ore_tuff")
	};
	private static Holder<PlacedFeature> placedFeature;

	private final Lock geologyLock = new ReentrantLock();
	private Geology geology = null;
	private GeomeGeology geomeGeology = null;
	private long geologySeed = Long.MIN_VALUE;

	private StoneReplacer() {
		super(NoneFeatureConfiguration.CODEC);
		setRegistryName(Mineralogy.MODID, "stone_replacer");
	}

	public static void registerConfiguredFeature() {
		ResourceLocation id = new ResourceLocation(Mineralogy.MODID, "stone_replacer");
		Holder<ConfiguredFeature<?, ?>> configured = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
				id, new ConfiguredFeature<NoneFeatureConfiguration, StoneReplacer>(FEATURE,
						NoneFeatureConfiguration.INSTANCE));
		placedFeature = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id,
				new PlacedFeature(configured, Collections.emptyList()));
	}

	public static void onBiomeLoading(BiomeLoadingEvent event) {
		if (!isOverworldCategory(event.getCategory())) {
			return;
		}

		removeVanillaMatchingStoneFeatures(event);
		if (placedFeature != null) {
			event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES)
					.add(placedFeature);
		}
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos pos = context.origin();
		if (!MineralogyConfig.placeMineralogyRock()
				|| world.getLevel().dimension() != Level.OVERWORLD) {
			return false;
		}

		ChunkAccess chunk = world.getChunk(pos);
		if (WorldGeologyProfileManager.geologyMode() == GeologyMode.LEGACY) {
			getLegacyGeology(world.getSeed()).replaceStoneInChunk(chunk);
		} else {
			getGeomeGeology(world.getSeed()).replaceStoneInChunk(world, chunk);
		}
		return true;
	}

	private static boolean isOverworldCategory(BiomeCategory category) {
		return category != BiomeCategory.NETHER && category != BiomeCategory.THEEND
				&& category != BiomeCategory.NONE;
	}

	private static void removeVanillaMatchingStoneFeatures(BiomeLoadingEvent event) {
		event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES)
				.removeIf(StoneReplacer::isVanillaMatchingStoneFeature);
	}

	private static boolean isVanillaMatchingStoneFeature(Holder<PlacedFeature> feature) {
		for (ResourceLocation id : VANILLA_MATCHING_STONE_FEATURES) {
			if (feature.is(id)) {
				return true;
			}
		}
		return false;
	}

	private Geology getLegacyGeology(long seed) {
		if (geology == null || geologySeed != seed) {
			geologyLock.lock();
			try {
				if (geology == null || geologySeed != seed) {
					geology = new Geology(seed, MineralogyConfig.geomeSize(), MineralogyConfig.rockLayerNoise());
					geomeGeology = null;
					geologySeed = seed;
				}
			} finally {
				geologyLock.unlock();
			}
		}

		return geology;
	}

	private GeomeGeology getGeomeGeology(long seed) {
		if (geomeGeology == null || geologySeed != seed) {
			geologyLock.lock();
			try {
				if (geomeGeology == null || geologySeed != seed) {
					geomeGeology = new GeomeGeology(seed, GeomeConfig.baked());
					geology = null;
					geologySeed = seed;
				}
			} finally {
				geologyLock.unlock();
			}
		}

		return geomeGeology;
	}

	public static void refreshWorldConfig() {
		FEATURE.clearCachedGeology();
	}

	private void clearCachedGeology() {
		geologyLock.lock();
		try {
			geology = null;
			geomeGeology = null;
			geologySeed = Long.MIN_VALUE;
		} finally {
			geologyLock.unlock();
		}
	}
}
