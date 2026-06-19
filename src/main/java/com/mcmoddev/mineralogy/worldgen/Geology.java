package com.mcmoddev.mineralogy.worldgen;

import java.util.List;
import java.util.Random;

import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.worldgen.math.PerlinNoise2D;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class Geology {
	private final PerlinNoise2D geomeNoiseLayer;
	private final PerlinNoise2D rockNoiseLayer;
	private final short[] whiteNoiseArray;

	public Geology(long seed, double geomeSize, double rockLayerSize) {
		int rockLayerUndertones = 4;
		int undertoneMultiplier = 1 << (rockLayerUndertones - 1);
		geomeNoiseLayer = new PerlinNoise2D(~seed, 128, (float) geomeSize, 2);
		rockNoiseLayer = new PerlinNoise2D(seed, (float) (4 * undertoneMultiplier),
				(float) (rockLayerSize * undertoneMultiplier), rockLayerUndertones);

		Random random = new Random(seed);
		whiteNoiseArray = new short[256];
		for (int i = 0; i < whiteNoiseArray.length; i++) {
			whiteNoiseArray[i] = (short) random.nextInt(0x7FFF);
		}
	}

	public Block getStoneAt(int x, int y, int z) {
		float geome = geomeNoiseLayer.valueAt(x, z) + y;
		int rockValue = (int) rockNoiseLayer.valueAt(x, z) + y;
		if (geome < -64) {
			return pickBlockFromList(rockValue, MineralogyRegistry.igneousStones);
		} else if (geome < 64) {
			return pickBlockFromList(rockValue, MineralogyRegistry.metamorphicStones);
		}

		return pickBlockFromList(rockValue, MineralogyRegistry.sedimentaryStones);
	}

	public void replaceStoneInChunk(ChunkAccess chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int xOffset = chunkPos.getMinBlockX();
		int zOffset = chunkPos.getMinBlockZ();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean changed = false;

		for (int dx = 0; dx < 16; dx++) {
			int x = xOffset + dx;
			for (int dz = 0; dz < 16; dz++) {
				int z = zOffset + dz;
				int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz);
				int baseRockVal = (int) rockNoiseLayer.valueAt(x, z);
				int geomeBase = (int) geomeNoiseLayer.valueAt(x, z);

				for (; y > 0; y--) {
					cursor.set(x, y, z);
					if (chunk.getBlockState(cursor).getBlock() == Blocks.STONE) {
						chunk.setBlockState(cursor, pickReplacement(baseRockVal, geomeBase, y).defaultBlockState(), false);
						changed = true;
					}
				}
			}
		}

		if (changed) {
			chunk.setUnsaved(true);
		}
	}

	private Block pickReplacement(int baseRockVal, int geomeBase, int y) {
		int geome = geomeBase + y;
		if (geome < -32) {
			return pickBlockFromList(baseRockVal + y, MineralogyRegistry.igneousStones);
		} else if (geome < 32) {
			return pickBlockFromList(baseRockVal + y, MineralogyRegistry.metamorphicStones);
		}

		return pickBlockFromList(baseRockVal + y, MineralogyRegistry.sedimentaryStones);
	}

	public Block[] getStoneColumn(int x, int z, int height) {
		Block[] column = new Block[height];
		int baseRockVal = (int) rockNoiseLayer.valueAt(x, z);
		double geomeBase = geomeNoiseLayer.valueAt(x, z);
		for (int y = 0; y < column.length; y++) {
			double geome = geomeBase + y;
			if (geome < -32) {
				column[y] = pickBlockFromList(baseRockVal + y, MineralogyRegistry.igneousStones);
			} else if (geome < 32) {
				column[y] = pickBlockFromList(baseRockVal + y + 3, MineralogyRegistry.metamorphicStones);
			} else {
				column[y] = pickBlockFromList(baseRockVal + y + 5, MineralogyRegistry.sedimentaryStones);
			}
		}
		return column;
	}

	private Block pickBlockFromList(int value, List<Block> list) {
		if (list.isEmpty()) {
			return Blocks.STONE;
		}

		return list.get(whiteNoiseArray[(value / MineralogyConfig.geomLayerThickness()) & 0xFF] % list.size());
	}
}
