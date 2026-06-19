package com.mcmoddev.mineralogy.worldgen;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class StoneReplacer extends Feature<NoneFeatureConfiguration> {
	public static final StoneReplacer FEATURE = new StoneReplacer();
	private static final ConfiguredFeature<?, ?> CONFIGURED_FEATURE =
			FEATURE.configured(NoneFeatureConfiguration.INSTANCE)
					.decorated(FeatureDecorator.NOPE.configured(NoneDecoratorConfiguration.INSTANCE));

	private final Lock geologyLock = new ReentrantLock();
	private Geology geology = null;
	private GeomeGeology geomeGeology = null;
	private long geologySeed = Long.MIN_VALUE;

	private StoneReplacer() {
		super(NoneFeatureConfiguration.CODEC);
		setRegistryName(Mineralogy.MODID, "stone_replacer");
	}

	public static void registerConfiguredFeature() {
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
				new ResourceLocation(Mineralogy.MODID, "stone_replacer"), CONFIGURED_FEATURE);
	}

	public static void onBiomeLoading(BiomeLoadingEvent event) {
		if (isOverworldCategory(event.getCategory())) {
			event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CONFIGURED_FEATURE);
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
		if (MineralogyConfig.geologyMode() == GeologyMode.LEGACY) {
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
}
