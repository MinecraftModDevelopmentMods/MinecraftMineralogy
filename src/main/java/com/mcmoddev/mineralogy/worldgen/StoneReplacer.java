package com.mcmoddev.mineralogy.worldgen;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;

import net.minecraft.world.IWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public class StoneReplacer extends Feature<NoFeatureConfig> {
	private static final StoneReplacer FEATURE = new StoneReplacer();

	private final Lock geologyLock = new ReentrantLock();
	private Geology geology = null;
	private GeomeGeology geomeGeology = null;
	private long geologySeed = Long.MIN_VALUE;

	private StoneReplacer() {
		super(NoFeatureConfig::deserialize);
	}

	public static void register() {
		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			biome.addFeature(
					GenerationStage.Decoration.UNDERGROUND_ORES,
					Biome.createDecoratedFeature(
							FEATURE,
							IFeatureConfig.NO_FEATURE_CONFIG,
							Placement.NOPE,
							IPlacementConfig.NO_PLACEMENT_CONFIG));
		}
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator,
			Random random, BlockPos pos, NoFeatureConfig config) {
		if (!MineralogyConfig.placeMineralogyRock()
				|| world.getDimension().getType() != DimensionType.OVERWORLD) {
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
