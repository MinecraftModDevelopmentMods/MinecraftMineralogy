package com.mcmoddev.mineralogy.worldgen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class GeomeDistributionSampler {
	private GeomeDistributionSampler() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static String sample(long seed, Iterable<Biome> biomes, int columnsPerBiome, int yStep) {
		BakedGeomeConfig config = GeomeConfig.baked();
		GeomeGeology geology = new GeomeGeology(seed, config);
		Map<String, Integer> geomeCounts = new LinkedHashMap<>();
		Map<String, Integer> rockCounts = new LinkedHashMap<>();

		int biomeIndex = 0;
		int samples = 0;
		int step = Math.max(1, yStep);
		for (Biome biome : biomes) {
			for (int column = 0; column < columnsPerBiome; column++) {
				int x = (biomeIndex * 257) + (column * 19);
				int z = (biomeIndex * -193) + (column * 23);
				add(geomeCounts, geology.getGeomeName(biome, x, z));
				for (int y = 8; y <= 96; y += step) {
					Block block = geology.getStoneAt(biome, x, y, z, 96);
					ResourceLocation id = block.getRegistryName();
					add(rockCounts, id == null ? "<unregistered>" : id.toString());
					samples++;
				}
			}
			biomeIndex++;
		}

		StringBuilder report = new StringBuilder();
		report.append("Geome sampler seed=").append(seed)
				.append(" columnsPerBiome=").append(columnsPerBiome)
				.append(" yStep=").append(step)
				.append(" rockSamples=").append(samples)
				.append('\n');
		appendCounts(report, "geomes", geomeCounts);
		appendCounts(report, "rocks", rockCounts);
		return report.toString();
	}

	private static void appendCounts(StringBuilder report, String label, Map<String, Integer> counts) {
		report.append(label).append(':').append('\n');
		for (Entry<String, Integer> entry : counts.entrySet()) {
			report.append("  ").append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
		}
	}

	private static void add(Map<String, Integer> counts, String key) {
		Integer current = counts.get(key);
		counts.put(key, current == null ? 1 : current + 1);
	}
}
