package com.mcmoddev.mineralogy.worldgen;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class StoneReplacer extends Feature<NoFeatureConfig> {
	public static final StoneReplacer FEATURE = new StoneReplacer();
	private static final ConfiguredFeature<?, ?> CONFIGURED_FEATURE =
			FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
					.withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG));

	private final Lock geologyLock = new ReentrantLock();
	private Geology geology = null;
	private GeomeGeology geomeGeology = null;
	private long geologySeed = Long.MIN_VALUE;

	private StoneReplacer() {
		super(NoFeatureConfig.CODEC);
		setRegistryName(Mineralogy.MODID, "stone_replacer");
	}

	public static void registerConfiguredFeature() {
		WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
				new ResourceLocation(Mineralogy.MODID, "stone_replacer"), CONFIGURED_FEATURE);
	}

	public static void onBiomeLoading(BiomeLoadingEvent event) {
		if (isOverworldCategory(event.getCategory())) {
			event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, CONFIGURED_FEATURE);
		}
	}

	@Override
	public boolean generate(ISeedReader world, ChunkGenerator generator,
			Random random, BlockPos pos, NoFeatureConfig config) {
		if (!MineralogyConfig.placeMineralogyRock()
				|| world.getWorld().getDimensionKey() != World.OVERWORLD) {
			return false;
		}

		IChunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (MineralogyConfig.geologyMode() == GeologyMode.LEGACY) {
			getLegacyGeology(world.getSeed()).replaceStoneInChunk(chunk);
		} else {
			getGeomeGeology(world.getSeed()).replaceStoneInChunk(world, chunk);
		}
		return true;
	}

	private static boolean isOverworldCategory(Category category) {
		return category != Category.NETHER && category != Category.THEEND && category != Category.NONE;
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
