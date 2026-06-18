package com.mcmoddev.mineralogy.worldgen;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;

public final class BakedGeomeConfig {
	private static final int MAX_Y = 255;
	private static final int PICKER_SCALE = 1000;

	final GeomeDefinition[] geomes;
	final double geomeScale;
	final double biomeInfluence;
	final double regionalNoiseInfluence;
	final double boundaryNoiseInfluence;
	final int[] noiseOffsetX;
	final int[] noiseOffsetZ;

	private final Map<Biome, double[]> biomeWeights;
	private final int[][][] familyThresholds;
	private final WeightedBlockPicker[][][] rockPickers;
	private final double[] fallbackWeights;

	BakedGeomeConfig(GeomeDefinition[] geomes, double geomeScale, double biomeInfluence,
			double regionalNoiseInfluence, double boundaryNoiseInfluence, Map<Biome, double[]> biomeWeights,
			RockEntry[] rocks) {
		this.geomes = geomes;
		this.geomeScale = geomeScale;
		this.biomeInfluence = biomeInfluence;
		this.regionalNoiseInfluence = regionalNoiseInfluence;
		this.boundaryNoiseInfluence = boundaryNoiseInfluence;
		this.biomeWeights = new IdentityHashMap<>(biomeWeights);
		this.fallbackWeights = defaultWeights(geomes.length);
		this.noiseOffsetX = new int[geomes.length];
		this.noiseOffsetZ = new int[geomes.length];
		for (int i = 0; i < geomes.length; i++) {
			noiseOffsetX[i] = (i + 1) * 9973;
			noiseOffsetZ[i] = -((i + 1) * 6151);
		}

		familyThresholds = new int[geomes.length][MAX_Y + 1][RockFamily.values().length];
		rockPickers = new WeightedBlockPicker[geomes.length][RockFamily.values().length][MAX_Y + 1];
		buildPickers(rocks);
	}

	public int pickGeome(Biome biome, double[] regionalNoise, double boundaryNoise) {
		double[] weights = biomeWeights.get(biome);
		if (weights == null) {
			weights = fallbackWeights;
		}

		double bestScore = Double.NEGATIVE_INFINITY;
		int bestIndex = 0;
		for (int i = 0; i < geomes.length; i++) {
			double boundary = ((i & 1) == 0 ? boundaryNoise : -boundaryNoise) * boundaryNoiseInfluence;
			double score = geomes[i].baseWeight + (weights[i] * biomeInfluence)
					+ (regionalNoise[i] * regionalNoiseInfluence) + boundary;
			if (score > bestScore) {
				bestScore = score;
				bestIndex = i;
			}
		}

		return bestIndex;
	}

	public RockFamily pickFamily(int geomeIndex, int y, int hash) {
		int[] thresholds = familyThresholds[geomeIndex][clampY(y)];
		int total = thresholds[thresholds.length - 1];
		if (total <= 0) {
			return RockFamily.SEDIMENTARY;
		}

		int value = positive(hash) % total;
		for (int i = 0; i < thresholds.length; i++) {
			if (value < thresholds[i]) {
				return RockFamily.values()[i];
			}
		}

		return RockFamily.SEDIMENTARY;
	}

	public BlockState pickRock(int geomeIndex, RockFamily family, int y, int hash) {
		return rockPickers[geomeIndex][family.ordinal()][clampY(y)].pick(hash);
	}

	public String geomeName(int geomeIndex) {
		return geomes[geomeIndex].name;
	}

	int geomeCount() {
		return geomes.length;
	}

	private void buildPickers(RockEntry[] rocks) {
		for (int geome = 0; geome < geomes.length; geome++) {
			for (int y = 0; y <= MAX_Y; y++) {
				int familyTotal = 0;
				for (RockFamily family : RockFamily.values()) {
					WeightedBlockPicker picker = buildRockPicker(rocks, geome, family, y);
					rockPickers[geome][family.ordinal()][y] = picker;

					double familyWeight = geomes[geome].familyWeights[family.ordinal()]
							* familyDepthWeight(family, y);
					if (picker.isEmpty()) {
						familyWeight = 0.0D;
					}

					if (familyWeight > 0.0D) {
						familyTotal += Math.max(1, (int) Math.round(familyWeight * PICKER_SCALE));
					}
					familyThresholds[geome][y][family.ordinal()] = familyTotal;
				}
			}
		}
	}

	private WeightedBlockPicker buildRockPicker(RockEntry[] rocks, int geome, RockFamily family, int y) {
		int count = 0;
		for (RockEntry rock : rocks) {
			if (rock.family == family) {
				count++;
			}
		}

		BlockState[] states = new BlockState[count];
		int[] thresholds = new int[count];
		int total = 0;
		int index = 0;
		for (RockEntry rock : rocks) {
			if (rock.family != family) {
				continue;
			}

			double depthWeight = depthWeight(y, rock.depthPeak, rock.depthSpread);
			double geomeWeight = rock.geomeWeights[geome];
			double rawWeight = rock.weight * geomeWeight * depthWeight;
			if (rawWeight <= 0.0D) {
				continue;
			}

			total += Math.max(1, (int) Math.round(rawWeight * PICKER_SCALE));
			states[index] = rock.state;
			thresholds[index] = total;
			index++;
		}

		if (index != states.length) {
			BlockState[] compactStates = new BlockState[index];
			int[] compactThresholds = new int[index];
			System.arraycopy(states, 0, compactStates, 0, index);
			System.arraycopy(thresholds, 0, compactThresholds, 0, index);
			states = compactStates;
			thresholds = compactThresholds;
		}

		return new WeightedBlockPicker(states, thresholds, total);
	}

	private static double[] defaultWeights(int count) {
		double[] weights = new double[count];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1.0D;
		}
		return weights;
	}

	private static double depthWeight(int y, int peak, int spread) {
		double distance = (y - peak) / (double) Math.max(1, spread);
		return 0.08D + (0.92D / (1.0D + distance * distance));
	}

	private static double familyDepthWeight(RockFamily family, int y) {
		switch (family) {
			case SEDIMENTARY:
				return 0.35D + depthWeight(y, 68, 54);
			case METAMORPHIC:
				return 0.25D + (1.35D * depthWeight(y, 18, 36));
			case IGNEOUS_INTRUSIVE:
				return 0.25D + (1.20D * depthWeight(y, 28, 44));
			case IGNEOUS_VOLCANIC:
				return 0.25D + (1.20D * depthWeight(y, 76, 30));
			default:
				return 1.0D;
		}
	}

	private static int clampY(int y) {
		if (y < 0) {
			return 0;
		}
		if (y > MAX_Y) {
			return MAX_Y;
		}
		return y;
	}

	private static int positive(int value) {
		return value & 0x7FFFFFFF;
	}

	static final class GeomeDefinition {
		final String name;
		final double baseWeight;
		final double[] familyWeights;

		GeomeDefinition(String name, double baseWeight, double[] familyWeights) {
			this.name = name;
			this.baseWeight = baseWeight;
			this.familyWeights = familyWeights;
		}
	}

	static final class RockEntry {
		final BlockState state;
		final RockFamily family;
		final int depthPeak;
		final int depthSpread;
		final double weight;
		final double[] geomeWeights;

		RockEntry(BlockState state, RockFamily family, int depthPeak, int depthSpread, double weight,
				double[] geomeWeights) {
			this.state = state;
			this.family = family;
			this.depthPeak = depthPeak;
			this.depthSpread = depthSpread;
			this.weight = weight;
			this.geomeWeights = geomeWeights;
		}
	}

	private static final class WeightedBlockPicker {
		private static final BlockState FALLBACK = Blocks.STONE.getDefaultState();

		private final BlockState[] states;
		private final int[] thresholds;
		private final int total;

		WeightedBlockPicker(BlockState[] states, int[] thresholds, int total) {
			this.states = states;
			this.thresholds = thresholds;
			this.total = total;
		}

		boolean isEmpty() {
			return total <= 0 || states.length == 0;
		}

		BlockState pick(int hash) {
			if (isEmpty()) {
				return FALLBACK;
			}

			int value = positive(hash) % total;
			for (int i = 0; i < thresholds.length; i++) {
				if (value < thresholds[i]) {
					return states[i];
				}
			}

			return states[states.length - 1];
		}
	}
}
