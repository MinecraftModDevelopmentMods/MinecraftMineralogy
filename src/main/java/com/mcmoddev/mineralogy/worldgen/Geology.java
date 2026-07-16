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
import net.minecraft.world.level.block.state.BlockState;

public class Geology {
	private final PerlinNoise2D geomeNoiseLayer;
	private final PerlinNoise2D rockNoiseLayer;
	private final short[] whiteNoiseArray;
	private final BlockState[] igneousStones;
	private final BlockState[] metamorphicStones;
	private final BlockState[] sedimentaryStones;

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

		igneousStones = aliasList(MineralogyRegistry.igneousStones);
		metamorphicStones = aliasList(MineralogyRegistry.metamorphicStones);
		sedimentaryStones = aliasList(MineralogyRegistry.sedimentaryStones);
	}

	public Block getStoneAt(int x, int y, int z) {
		float geome = geomeNoiseLayer.valueAt(x, z) + y;
		int rockValue = (int) rockNoiseLayer.valueAt(x, z) + y;
		if (geome < -64) {
			return pickStateFromList(rockValue, igneousStones).getBlock();
		} else if (geome < 64) {
			return pickStateFromList(rockValue, metamorphicStones).getBlock();
		}

		return pickStateFromList(rockValue, sedimentaryStones).getBlock();
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

				for (; y >= chunk.getMinBuildHeight(); y--) {
					cursor.set(x, y, z);
					if (isReplaceableBaseStone(chunk.getBlockState(cursor))) {
						chunk.setBlockState(cursor, pickReplacement(baseRockVal, geomeBase, y), false);
						changed = true;
					}
				}
			}
		}

		if (changed) {
			chunk.setUnsaved(true);
		}
	}

	private BlockState pickReplacement(int baseRockVal, int geomeBase, int y) {
		int geome = geomeBase + y;
		if (geome < -32) {
			return pickStateFromList(baseRockVal + y, igneousStones);
		} else if (geome < 32) {
			return pickStateFromList(baseRockVal + y, metamorphicStones);
		}

		return pickStateFromList(baseRockVal + y, sedimentaryStones);
	}

	public Block[] getStoneColumn(int x, int z, int height) {
		Block[] column = new Block[height];
		int baseRockVal = (int) rockNoiseLayer.valueAt(x, z);
		double geomeBase = geomeNoiseLayer.valueAt(x, z);
		for (int y = 0; y < column.length; y++) {
			double geome = geomeBase + y;
			if (geome < -32) {
				column[y] = pickStateFromList(baseRockVal + y, igneousStones).getBlock();
			} else if (geome < 32) {
				column[y] = pickStateFromList(baseRockVal + y + 3, metamorphicStones).getBlock();
			} else {
				column[y] = pickStateFromList(baseRockVal + y + 5, sedimentaryStones).getBlock();
			}
		}
		return column;
	}

	private static boolean isReplaceableBaseStone(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.STONE || block == Blocks.DEEPSLATE;
	}

	private BlockState pickStateFromList(int value, BlockState[] list) {
		if (list.length == 0) {
			return Blocks.STONE.defaultBlockState();
		}

		return list[whiteNoiseArray[(value / MineralogyConfig.geomLayerThickness()) & 0xFF] % list.length];
	}

	private static BlockState[] aliasList(List<Block> blocks) {
		BlockState[] states = new BlockState[blocks.size()];
		for (int i = 0; i < states.length; i++) {
			states[i] = GeologyBlockAliases.aliasState(blocks.get(i).defaultBlockState());
		}
		return states;
	}
}
