package com.mcmoddev.mineralogy.worldgen;

import java.util.Random;

import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.worldgen.math.PerlinNoise2D;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;

public final class GeomeGeology {
	private final BakedGeomeConfig config;
	private final PerlinNoise2D regionalNoise;
	private final PerlinNoise2D boundaryNoise;
	private final PerlinNoise2D familyShapeNoise;
	private final PerlinNoise2D stratumNoise;
	private final short[] whiteNoiseArray;
	private final int layerThickness;

	public GeomeGeology(long seed, BakedGeomeConfig config) {
		int rockLayerUndertones = 4;
		int undertoneMultiplier = 1 << (rockLayerUndertones - 1);
		this.config = config;
		layerThickness = Math.max(1, MineralogyConfig.geomLayerThickness());
		regionalNoise = new PerlinNoise2D(seed ^ 0x47E04E4DL, 96.0F, (float) config.geomeScale, 2);
		boundaryNoise = new PerlinNoise2D(~seed ^ 0x1BADC0DEL, 48.0F, (float) (config.geomeScale * 0.45D), 2);
		familyShapeNoise = new PerlinNoise2D(~seed, 128.0F, (float) MineralogyConfig.geomeSize(), 2);
		stratumNoise = new PerlinNoise2D(seed, (float) (4 * undertoneMultiplier),
				(float) (MineralogyConfig.rockLayerNoise() * undertoneMultiplier), rockLayerUndertones);

		Random random = new Random(seed ^ 0x5EEDBEEFL);
		whiteNoiseArray = new short[256];
		for (int i = 0; i < whiteNoiseArray.length; i++) {
			whiteNoiseArray[i] = (short) random.nextInt(0x7FFF);
		}
	}

	public void replaceStoneInChunk(IWorld world, IChunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int xOffset = chunkPos.getXStart();
		int zOffset = chunkPos.getZStart();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		double[] regionalValues = new double[config.geomeCount()];
		boolean changed = false;

		for (int dx = 0; dx < 16; dx++) {
			int x = xOffset + dx;
			for (int dz = 0; dz < 16; dz++) {
				int z = zOffset + dz;
				int surfaceY = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, dx, dz);
				cursor.setPos(x, surfaceY, z);
				Biome biome = world.getBiome(cursor);
				int geomeIndex = classifyColumn(biome, x, z, regionalValues);
				int baseRockValue = (int) stratumNoise.valueAt(x, z);

				for (int y = surfaceY; y > 0; y--) {
					cursor.setPos(x, y, z);
					if (chunk.getBlockState(cursor).getBlock() == Blocks.STONE) {
						chunk.setBlockState(cursor, pickReplacement(geomeIndex, baseRockValue, x, y, z), false);
						changed = true;
					}
				}
			}
		}

		if (changed && chunk instanceof Chunk) {
			((Chunk) chunk).setModified(true);
		}
	}

	public Block getStoneAt(Biome biome, int x, int y, int z, int surfaceY) {
		double[] regionalValues = new double[config.geomeCount()];
		int geomeIndex = classifyColumn(biome, x, z, regionalValues);
		return pickReplacement(geomeIndex, (int) stratumNoise.valueAt(x, z), x, y, z).getBlock();
	}

	public String getGeomeName(Biome biome, int x, int z) {
		double[] regionalValues = new double[config.geomeCount()];
		return config.geomeName(classifyColumn(biome, x, z, regionalValues));
	}

	private int classifyColumn(Biome biome, int x, int z, double[] regionalValues) {
		for (int i = 0; i < regionalValues.length; i++) {
			regionalValues[i] = regionalNoise.valueAt(x + config.noiseOffsetX[i], z + config.noiseOffsetZ[i]);
		}

		double boundary = boundaryNoise.valueAt(x, z);
		return config.pickGeome(biome, regionalValues, boundary);
	}

	private net.minecraft.block.BlockState pickReplacement(int geomeIndex, int baseRockValue, int x, int y,
			int z) {
		int stratum = baseRockValue + y;
		int layerIndex = Math.floorDiv(stratum, layerThickness);
		int layerY = y + (layerThickness / 2) - Math.floorMod(stratum, layerThickness);
		int familyHash = whiteNoiseArray[(layerIndex + (geomeIndex * 37)) & 0xFF];
		RockFamily family = pickShapedFamily(geomeIndex, x, y, z, layerY, familyHash);
		int rockHash = whiteNoiseArray[((layerIndex * 31) + (family.ordinal() * 53) + (geomeIndex * 79)) & 0xFF];
		return config.pickRock(geomeIndex, family, layerY, rockHash);
	}

	private RockFamily pickShapedFamily(int geomeIndex, int x, int y, int z, int layerY, int familyHash) {
		double shapedFamily = familyShapeNoise.valueAt(x, z) + y;
		boolean nearBoundary = Math.abs(shapedFamily + 32.0D) < 12.0D || Math.abs(shapedFamily - 32.0D) < 12.0D;
		if (nearBoundary && (familyHash & 0x03) == 0) {
			return config.pickFamily(geomeIndex, layerY, familyHash);
		}

		if (shapedFamily < -32.0D) {
			return y > 48 ? RockFamily.IGNEOUS_VOLCANIC : RockFamily.IGNEOUS_INTRUSIVE;
		} else if (shapedFamily < 32.0D) {
			return RockFamily.METAMORPHIC;
		}

		return RockFamily.SEDIMENTARY;
	}
}
