package com.mcmoddev.mineralogy.worldgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mcmoddev.mineralogy.worldgen.BakedGeomeConfig.GeomeDefinition;
import com.mcmoddev.mineralogy.worldgen.BakedGeomeConfig.RockEntry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class GeomeConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = Paths.get("config", "mineralogy-geomes.json");

	private static BakedGeomeConfig bakedConfig = null;

	private GeomeConfig() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static BakedGeomeConfig bake() {
		JsonObject root = loadConfig();
		bakedConfig = bake(root);
		return bakedConfig;
	}

	public static BakedGeomeConfig baked() {
		if (bakedConfig == null) {
			return bake();
		}
		return bakedConfig;
	}

	private static JsonObject loadConfig() {
		JsonObject defaults = defaultConfig();
		if (!Files.exists(CONFIG_PATH)) {
			writeDefaultConfig(defaults);
			return defaults;
		}

		try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
			JsonElement element = new JsonParser().parse(reader);
			if (!element.isJsonObject()) {
				LOGGER.warn("Mineralogy geome config '{}' is not a JSON object; using defaults", CONFIG_PATH);
				return defaults;
			}
			return element.getAsJsonObject();
		} catch (IOException | JsonSyntaxException | IllegalStateException e) {
			LOGGER.warn("Could not read Mineralogy geome config '{}'; using defaults", CONFIG_PATH, e);
			return defaults;
		}
	}

	private static void writeDefaultConfig(JsonObject defaults) {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(defaults, writer);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not create default Mineralogy geome config '{}'", CONFIG_PATH, e);
		}
	}

	private static BakedGeomeConfig bake(JsonObject root) {
		LinkedHashMap<String, Integer> geomeIndexes = new LinkedHashMap<>();
		GeomeDefinition[] geomes = readGeomes(root, geomeIndexes);
		double geomeScale = getDouble(root, "geome_scale", 384.0D);
		double biomeInfluence = getDouble(root, "biome_influence", 1.15D);
		double regionalNoiseInfluence = getDouble(root, "regional_noise_influence", 0.90D);
		double boundaryNoiseInfluence = getDouble(root, "boundary_noise_influence", 0.45D);

		Map<String, double[]> biomeRules = readWeightRules(root, "biomes", geomeIndexes);
		Map<String, double[]> dictionaryRules = readWeightRules(root, "biome_dictionary", geomeIndexes);
		RockEntry[] rocks = readRocks(root, geomeIndexes);
		Map<Biome, double[]> biomeWeights = bakeBiomeWeights(geomeIndexes, biomeRules, dictionaryRules);

		LOGGER.info("Baked Mineralogy geome config with {} geomes, {} rock entries, and {} registered biome profiles",
				geomes.length, rocks.length, biomeWeights.size());
		return new BakedGeomeConfig(geomes, geomeScale, biomeInfluence, regionalNoiseInfluence,
				boundaryNoiseInfluence, biomeWeights, rocks);
	}

	private static GeomeDefinition[] readGeomes(JsonObject root, LinkedHashMap<String, Integer> geomeIndexes) {
		JsonObject geomeRoot = GsonHelper.getAsJsonObject(root, "geomes", defaultConfig().getAsJsonObject("geomes"));
		List<GeomeDefinition> geomes = new ArrayList<>();
		for (Entry<String, JsonElement> entry : geomeRoot.entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				LOGGER.warn("Ignoring Mineralogy geome '{}' because it is not an object", entry.getKey());
				continue;
			}

			JsonObject json = entry.getValue().getAsJsonObject();
			double[] familyWeights = new double[RockFamily.values().length];
			for (RockFamily family : RockFamily.values()) {
				familyWeights[family.ordinal()] = 1.0D;
			}

			JsonObject families = GsonHelper.getAsJsonObject(json, "families", new JsonObject());
			for (RockFamily family : RockFamily.values()) {
				familyWeights[family.ordinal()] = getDouble(families, family.configName, familyWeights[family.ordinal()]);
			}

			geomeIndexes.put(entry.getKey(), geomes.size());
			geomes.add(new GeomeDefinition(entry.getKey(), getDouble(json, "base", 1.0D), familyWeights));
		}

		if (geomes.isEmpty()) {
			throw new JsonSyntaxException("Mineralogy geome config must define at least one geome");
		}

		return geomes.toArray(new GeomeDefinition[geomes.size()]);
	}

	private static Map<String, double[]> readWeightRules(JsonObject root, String section,
			Map<String, Integer> geomeIndexes) {
		Map<String, double[]> rules = new LinkedHashMap<>();
		if (!root.has(section) || !root.get(section).isJsonObject()) {
			return rules;
		}

		for (Entry<String, JsonElement> entry : root.getAsJsonObject(section).entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				LOGGER.warn("Ignoring Mineralogy geome {} rule '{}' because it is not an object", section,
						entry.getKey());
				continue;
			}
			rules.put(entry.getKey(), readGeomeWeights(entry.getValue().getAsJsonObject(), geomeIndexes, 0.0D));
		}
		return rules;
	}

	private static RockEntry[] readRocks(JsonObject root, Map<String, Integer> geomeIndexes) {
		JsonObject rockRoot = GsonHelper.getAsJsonObject(root, "rocks", defaultConfig().getAsJsonObject("rocks"));
		List<RockEntry> rocks = new ArrayList<>();
		for (Entry<String, JsonElement> entry : rockRoot.entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				LOGGER.warn("Ignoring Mineralogy geome rock '{}' because it is not an object", entry.getKey());
				continue;
			}

			ResourceLocation id;
			try {
				id = new ResourceLocation(entry.getKey());
			} catch (RuntimeException e) {
				LOGGER.warn("Ignoring invalid Mineralogy geome rock id '{}'", entry.getKey());
				continue;
			}

			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block == null || block == Blocks.AIR) {
				LOGGER.warn("Ignoring unknown Mineralogy geome rock block '{}'", id);
				continue;
			}

			JsonObject json = entry.getValue().getAsJsonObject();
			RockFamily family;
			try {
				family = RockFamily.fromConfigName(GsonHelper.getAsString(json, "family"));
			} catch (RuntimeException e) {
				LOGGER.warn("Ignoring Mineralogy geome rock '{}' with invalid family", id);
				continue;
			}

			JsonObject geomeWeightsJson = GsonHelper.getAsJsonObject(json, "geomes", new JsonObject());
			double[] geomeWeights = readGeomeWeights(geomeWeightsJson, geomeIndexes, 1.0D);
			rocks.add(new RockEntry(block.defaultBlockState(), family,
					GsonHelper.getAsInt(json, "depth_peak", 48),
					Math.max(1, GsonHelper.getAsInt(json, "depth_spread", 48)),
					getDouble(json, "weight", 1.0D),
					geomeWeights));
		}

		if (rocks.isEmpty()) {
			LOGGER.warn("Mineralogy geome config produced no valid rock entries; falling back to vanilla stone");
			double[] weights = new double[geomeIndexes.size()];
			for (int i = 0; i < weights.length; i++) {
				weights[i] = 1.0D;
			}
			rocks.add(new RockEntry(Blocks.STONE.defaultBlockState(), RockFamily.SEDIMENTARY, 64, 64, 1.0D, weights));
		}
		return rocks.toArray(new RockEntry[rocks.size()]);
	}

	private static Map<Biome, double[]> bakeBiomeWeights(Map<String, Integer> geomeIndexes,
			Map<String, double[]> biomeRules, Map<String, double[]> dictionaryRules) {
		Map<Biome, double[]> result = new IdentityHashMap<>();
		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			double[] weights = new double[geomeIndexes.size()];
			for (int i = 0; i < weights.length; i++) {
				weights[i] = 1.0D;
			}

			ResourceLocation biomeId = biome.getRegistryName();
			if (biomeId != null) {
				merge(weights, biomeRules.get(biomeId.toString()));
				ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, biomeId);
				for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biomeKey)) {
					merge(weights, dictionaryRules.get(type.getName()));
				}
			}
			applyBiomeHeuristic(weights, geomeIndexes, biomeId, biome);
			result.put(biome, weights);
		}
		return result;
	}

	private static void applyBiomeHeuristic(double[] weights, Map<String, Integer> geomeIndexes,
			ResourceLocation biomeId, Biome biome) {
		String biomeName = biomeId == null ? "" : biomeId.getPath();
		float temperature = biome.getBaseTemperature();
		float downfall = biome.getDownfall();

		if (biomeName.contains("ocean") || biomeName.contains("river") || biomeName.contains("beach")
				|| biomeName.contains("shore")) {
			add(weights, geomeIndexes, "coastal_shelf", 2.25D);
			add(weights, geomeIndexes, "sedimentary_basin", 0.75D);
		}
		if (biomeName.contains("desert") || biomeName.contains("badlands") || biomeName.contains("savanna")
				|| (temperature > 0.95F && downfall < 0.25F)) {
			add(weights, geomeIndexes, "arid_basin", 2.5D);
		}
		if (biomeName.contains("swamp") || biomeName.contains("marsh") || downfall > 0.85F) {
			add(weights, geomeIndexes, "wetland_basin", 2.0D);
		}
		if (biomeName.contains("mountain") || biomeName.contains("hill") || biomeName.contains("peak")
				|| biomeName.contains("slope") || biomeName.contains("windswept") || biomeName.contains("stony")) {
			add(weights, geomeIndexes, "mountain_belt", 2.5D);
		}
		if (biomeName.contains("frozen") || biomeName.contains("snowy") || biomeName.contains("ice")
				|| temperature < 0.15F) {
			add(weights, geomeIndexes, "glacial_highland", 1.75D);
		}
		if (biomeName.contains("plains") || biomeName.contains("forest") || biomeName.contains("taiga")
				|| biomeName.contains("meadow") || biomeName.contains("grove")) {
			add(weights, geomeIndexes, "stable_craton", 1.25D);
		}
	}

	private static double[] readGeomeWeights(JsonObject json, Map<String, Integer> geomeIndexes, double defaultWeight) {
		double[] weights = new double[geomeIndexes.size()];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = defaultWeight;
		}

		for (Entry<String, JsonElement> entry : json.entrySet()) {
			Integer index = geomeIndexes.get(entry.getKey());
			if (index == null) {
				LOGGER.warn("Ignoring unknown Mineralogy geome weight '{}'", entry.getKey());
				continue;
			}
			weights[index] = entry.getValue().getAsDouble();
		}
		return weights;
	}

	private static void merge(double[] target, double[] source) {
		if (source == null) {
			return;
		}
		for (int i = 0; i < target.length && i < source.length; i++) {
			target[i] += source[i];
		}
	}

	private static void add(double[] weights, Map<String, Integer> indexes, String geome, double value) {
		Integer index = indexes.get(geome);
		if (index != null) {
			weights[index] += value;
		}
	}

	private static double getDouble(JsonObject json, String key, double fallback) {
		if (!json.has(key)) {
			return fallback;
		}
		return GsonHelper.getAsFloat(json, key);
	}

	private static JsonObject defaultConfig() {
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", 1);
		root.addProperty("geome_scale", 384.0D);
		root.addProperty("biome_influence", 1.15D);
		root.addProperty("regional_noise_influence", 0.90D);
		root.addProperty("boundary_noise_influence", 0.45D);

		JsonObject geomes = new JsonObject();
		addGeome(geomes, "stable_craton", 1.0D, 0.9D, 1.0D, 1.4D, 0.25D);
		addGeome(geomes, "mountain_belt", 1.0D, 0.55D, 2.8D, 1.35D, 0.55D);
		addGeome(geomes, "volcanic_arc", 0.9D, 0.35D, 0.75D, 1.25D, 3.6D);
		addGeome(geomes, "sedimentary_basin", 1.0D, 3.2D, 0.45D, 0.35D, 0.15D);
		addGeome(geomes, "coastal_shelf", 0.9D, 3.0D, 0.35D, 0.25D, 0.25D);
		addGeome(geomes, "arid_basin", 0.9D, 2.8D, 0.35D, 0.45D, 0.35D);
		addGeome(geomes, "wetland_basin", 0.8D, 2.5D, 0.45D, 0.25D, 0.15D);
		addGeome(geomes, "glacial_highland", 0.8D, 0.75D, 2.0D, 1.25D, 0.35D);
		root.add("geomes", geomes);

		JsonObject biomeRules = new JsonObject();
		addVanillaBiomeDefaults(biomeRules);
		addBiomesOPlentyDefaults(biomeRules);
		root.add("biomes", biomeRules);

		JsonObject dictionaryRules = new JsonObject();
		addWeights(dictionaryRules, "MOUNTAIN", "mountain_belt", 3.0D, "stable_craton", 0.75D);
		addWeights(dictionaryRules, "HILLS", "mountain_belt", 1.5D, "stable_craton", 0.75D);
		addWeights(dictionaryRules, "OCEAN", "coastal_shelf", 3.0D, "sedimentary_basin", 1.0D);
		addWeights(dictionaryRules, "RIVER", "coastal_shelf", 1.8D, "sedimentary_basin", 1.4D);
		addWeights(dictionaryRules, "BEACH", "coastal_shelf", 3.0D);
		addWeights(dictionaryRules, "SANDY", "arid_basin", 2.0D, "sedimentary_basin", 1.0D);
		addWeights(dictionaryRules, "DRY", "arid_basin", 1.6D);
		addWeights(dictionaryRules, "WET", "wetland_basin", 1.8D, "sedimentary_basin", 0.8D);
		addWeights(dictionaryRules, "SWAMP", "wetland_basin", 3.0D);
		addWeights(dictionaryRules, "SNOWY", "glacial_highland", 2.4D, "mountain_belt", 0.6D);
		addWeights(dictionaryRules, "COLD", "glacial_highland", 1.2D);
		addWeights(dictionaryRules, "HOT", "arid_basin", 0.8D, "volcanic_arc", 0.35D);
		addWeights(dictionaryRules, "MESA", "arid_basin", 3.0D, "sedimentary_basin", 2.0D);
		addWeights(dictionaryRules, "FOREST", "stable_craton", 1.2D);
		addWeights(dictionaryRules, "PLAINS", "stable_craton", 1.1D, "sedimentary_basin", 0.6D);
		root.add("biome_dictionary", dictionaryRules);

		JsonObject rocks = new JsonObject();
		addDefaultRocks(rocks);
		root.add("rocks", rocks);
		return root;
	}

	private static void addGeome(JsonObject geomes, String name, double base, double sedimentary,
			double metamorphic, double intrusive, double volcanic) {
		JsonObject geome = new JsonObject();
		geome.addProperty("base", base);
		JsonObject families = new JsonObject();
		families.addProperty(RockFamily.SEDIMENTARY.configName, sedimentary);
		families.addProperty(RockFamily.METAMORPHIC.configName, metamorphic);
		families.addProperty(RockFamily.IGNEOUS_INTRUSIVE.configName, intrusive);
		families.addProperty(RockFamily.IGNEOUS_VOLCANIC.configName, volcanic);
		geome.add("families", families);
		geomes.add(name, geome);
	}

	private static void addVanillaBiomeDefaults(JsonObject biomes) {
		addWeights(biomes, "minecraft:ocean", "coastal_shelf", 4.0D);
		addWeights(biomes, "minecraft:deep_ocean", "coastal_shelf", 4.0D, "sedimentary_basin", 1.0D);
		addWeights(biomes, "minecraft:river", "coastal_shelf", 2.0D, "sedimentary_basin", 2.0D);
		addWeights(biomes, "minecraft:beach", "coastal_shelf", 4.0D);
		addWeights(biomes, "minecraft:stone_shore", "coastal_shelf", 2.5D, "mountain_belt", 1.5D);
		addWeights(biomes, "minecraft:stony_shore", "coastal_shelf", 2.5D, "mountain_belt", 1.5D);
		addWeights(biomes, "minecraft:plains", "stable_craton", 2.0D, "sedimentary_basin", 1.0D);
		addWeights(biomes, "minecraft:forest", "stable_craton", 2.0D);
		addWeights(biomes, "minecraft:birch_forest", "stable_craton", 2.0D);
		addWeights(biomes, "minecraft:dark_forest", "stable_craton", 2.0D, "wetland_basin", 0.5D);
		addWeights(biomes, "minecraft:taiga", "stable_craton", 1.5D, "glacial_highland", 0.75D);
		addWeights(biomes, "minecraft:desert", "arid_basin", 4.0D, "sedimentary_basin", 1.5D);
		addWeights(biomes, "minecraft:desert_hills", "arid_basin", 3.5D, "sedimentary_basin", 1.2D);
		addWeights(biomes, "minecraft:savanna", "arid_basin", 2.5D, "stable_craton", 0.75D);
		addWeights(biomes, "minecraft:savanna_plateau", "arid_basin", 2.5D, "mountain_belt", 1.0D);
		addWeights(biomes, "minecraft:badlands", "arid_basin", 3.5D, "sedimentary_basin", 3.0D);
		addWeights(biomes, "minecraft:badlands_plateau", "arid_basin", 3.0D, "sedimentary_basin", 2.5D,
				"mountain_belt", 0.8D);
		addWeights(biomes, "minecraft:mountains", "mountain_belt", 4.0D, "stable_craton", 1.0D);
		addWeights(biomes, "minecraft:wooded_mountains", "mountain_belt", 3.5D, "stable_craton", 1.2D);
		addWeights(biomes, "minecraft:gravelly_mountains", "mountain_belt", 4.0D, "glacial_highland", 0.8D);
		addWeights(biomes, "minecraft:windswept_hills", "mountain_belt", 4.0D, "stable_craton", 1.0D);
		addWeights(biomes, "minecraft:windswept_forest", "mountain_belt", 3.0D, "stable_craton", 1.3D);
		addWeights(biomes, "minecraft:windswept_gravelly_hills", "mountain_belt", 4.0D,
				"glacial_highland", 0.8D);
		addWeights(biomes, "minecraft:meadow", "stable_craton", 1.7D, "mountain_belt", 1.2D);
		addWeights(biomes, "minecraft:grove", "glacial_highland", 1.8D, "mountain_belt", 1.4D);
		addWeights(biomes, "minecraft:snowy_slopes", "glacial_highland", 3.0D, "mountain_belt", 2.4D);
		addWeights(biomes, "minecraft:jagged_peaks", "mountain_belt", 4.8D, "glacial_highland", 2.0D);
		addWeights(biomes, "minecraft:frozen_peaks", "glacial_highland", 3.6D, "mountain_belt", 3.0D);
		addWeights(biomes, "minecraft:stony_peaks", "mountain_belt", 4.5D, "volcanic_arc", 0.8D);
		addWeights(biomes, "minecraft:dripstone_caves", "sedimentary_basin", 2.8D, "coastal_shelf", 1.0D);
		addWeights(biomes, "minecraft:lush_caves", "wetland_basin", 2.5D, "sedimentary_basin", 1.2D);
		addWeights(biomes, "minecraft:swamp", "wetland_basin", 4.0D, "sedimentary_basin", 1.5D);
		addWeights(biomes, "minecraft:jungle", "wetland_basin", 1.5D, "stable_craton", 1.5D);
		addWeights(biomes, "minecraft:snowy_tundra", "glacial_highland", 3.5D, "sedimentary_basin", 0.7D);
		addWeights(biomes, "minecraft:snowy_mountains", "glacial_highland", 3.5D, "mountain_belt", 2.0D);
		addWeights(biomes, "minecraft:frozen_ocean", "glacial_highland", 1.5D, "coastal_shelf", 3.0D);
		addWeights(biomes, "minecraft:frozen_river", "glacial_highland", 1.5D, "coastal_shelf", 2.0D);
	}

	private static void addBiomesOPlentyDefaults(JsonObject biomes) {
		String[] mountains = { "alps", "alps_foothills", "highland", "highland_crag", "jade_cliffs",
				"rainbow_hills", "redwood_hills" };
		addBOPWeights(biomes, mountains, "mountain_belt", 3.5D, "stable_craton", 0.8D);
		addWeights(biomes, bop("highland_moor"), "mountain_belt", 2.0D, "wetland_basin", 1.0D);
		addWeights(biomes, bop("volcano"), "volcanic_arc", 6.0D, "mountain_belt", 1.0D);
		addWeights(biomes, bop("volcanic_plains"), "volcanic_arc", 4.0D, "arid_basin", 0.8D);

		String[] dry = { "cold_desert", "dry_boneyard", "dryland", "golden_prairie", "grassland",
				"grassland_clover_patch", "lush_desert", "lush_savanna", "prairie", "scrubland", "shrubland",
				"shrubland_hills", "wasteland", "wooded_scrubland" };
		addBOPWeights(biomes, dry, "arid_basin", 3.0D, "sedimentary_basin", 1.0D);

		String[] wet = { "bayou", "bayou_mangrove", "deep_bayou", "dense_marsh", "fungal_field",
				"fungal_jungle", "marsh", "muskeg", "ominous_mire", "rainforest", "rainforest_cliffs",
				"rainforest_floodplain", "shroomy_wetland", "wetland", "wetland_forest" };
		addBOPWeights(biomes, wet, "wetland_basin", 3.0D, "sedimentary_basin", 1.1D);

		String[] cold = { "coniferous_forest", "coniferous_lakes", "fir_clearing",
				"snowy_coniferous_forest", "snowy_fir_clearing", "snowy_maple_forest", "tundra",
				"tundra_basin", "tundra_bog" };
		addBOPWeights(biomes, cold, "glacial_highland", 2.0D, "stable_craton", 1.0D);

		String[] temperate = { "bamboo_blossom_grove", "burnt_forest", "cherry_blossom_grove",
				"dead_forest", "dense_woodland", "flower_meadow", "grove", "grove_clearing", "grove_lakes",
				"lavender_field", "lavender_forest", "meadow", "meadow_forest", "mystic_grove",
				"mystic_plains", "ominous_woods", "orchard", "origin_valley", "redwood_forest",
				"redwood_forest_edge", "seasonal_forest", "seasonal_orchard", "seasonal_pumpkin_patch",
				"tall_dead_forest", "woodland" };
		addBOPWeights(biomes, temperate, "stable_craton", 2.0D, "wetland_basin", 0.5D);

		addWeights(biomes, bop("gravel_beach"), "coastal_shelf", 3.0D, "mountain_belt", 0.8D);
		addWeights(biomes, bop("tropic_beach"), "coastal_shelf", 3.5D, "wetland_basin", 0.8D);
		addWeights(biomes, bop("tropics"), "coastal_shelf", 2.0D, "wetland_basin", 1.2D);
	}

	private static String bop(String path) {
		return "biomesoplenty:" + path;
	}

	private static void addBOPWeights(JsonObject biomes, String[] names, Object... values) {
		for (String name : names) {
			addWeights(biomes, bop(name), values);
		}
	}

	private static void addDefaultRocks(JsonObject rocks) {
		addRock(rocks, "mineralogy:andesite", RockFamily.IGNEOUS_VOLCANIC, 68, 42, 1.0D,
				"volcanic_arc", 3.0D, "mountain_belt", 1.2D);
		addRock(rocks, "mineralogy:basalt", RockFamily.IGNEOUS_VOLCANIC, 72, 36, 1.2D,
				"volcanic_arc", 4.0D, "coastal_shelf", 0.7D);
		addRock(rocks, "mineralogy:rhyolite", RockFamily.IGNEOUS_VOLCANIC, 70, 36, 1.0D,
				"volcanic_arc", 3.5D, "mountain_belt", 0.8D);
		addRock(rocks, "mineralogy:basaltic_glass", RockFamily.IGNEOUS_VOLCANIC, 78, 24, 0.75D,
				"volcanic_arc", 4.5D);
		addRock(rocks, "mineralogy:scoria", RockFamily.IGNEOUS_VOLCANIC, 80, 22, 0.85D,
				"volcanic_arc", 4.0D);
		addRock(rocks, "mineralogy:tuff", RockFamily.IGNEOUS_VOLCANIC, 74, 30, 0.9D,
				"volcanic_arc", 3.5D, "arid_basin", 0.8D);
		addRock(rocks, "mineralogy:pumice", RockFamily.IGNEOUS_VOLCANIC, 82, 20, 0.65D,
				"volcanic_arc", 4.0D);

		addRock(rocks, "mineralogy:diorite", RockFamily.IGNEOUS_INTRUSIVE, 36, 42, 1.0D,
				"stable_craton", 1.3D, "mountain_belt", 1.0D);
		addRock(rocks, "mineralogy:granite", RockFamily.IGNEOUS_INTRUSIVE, 30, 48, 1.1D,
				"stable_craton", 2.0D, "mountain_belt", 1.4D);
		addRock(rocks, "mineralogy:pegmatite", RockFamily.IGNEOUS_INTRUSIVE, 26, 36, 0.8D,
				"stable_craton", 1.6D, "mountain_belt", 1.2D);
		addRock(rocks, "mineralogy:diabase", RockFamily.IGNEOUS_INTRUSIVE, 30, 40, 1.0D,
				"stable_craton", 1.2D, "volcanic_arc", 1.2D);
		addRock(rocks, "mineralogy:gabbro", RockFamily.IGNEOUS_INTRUSIVE, 20, 44, 1.1D,
				"stable_craton", 1.2D, "mountain_belt", 1.2D);
		addRock(rocks, "mineralogy:peridotite", RockFamily.IGNEOUS_INTRUSIVE, 10, 34, 0.85D,
				"mountain_belt", 1.5D, "volcanic_arc", 1.0D);

		addRock(rocks, "mineralogy:shale", RockFamily.SEDIMENTARY, 62, 36, 1.15D,
				"sedimentary_basin", 3.5D, "wetland_basin", 2.0D, "coastal_shelf", 1.8D);
		addRock(rocks, "mineralogy:conglomerate", RockFamily.SEDIMENTARY, 58, 32, 0.9D,
				"sedimentary_basin", 2.2D, "mountain_belt", 0.8D);
		addRock(rocks, "mineralogy:dolomite", RockFamily.SEDIMENTARY, 55, 38, 0.9D,
				"coastal_shelf", 2.2D, "sedimentary_basin", 1.8D);
		addRock(rocks, "mineralogy:limestone", RockFamily.SEDIMENTARY, 60, 40, 1.0D,
				"coastal_shelf", 3.0D, "sedimentary_basin", 1.6D);
		addRock(rocks, "mineralogy:siltstone", RockFamily.SEDIMENTARY, 66, 34, 1.0D,
				"sedimentary_basin", 2.4D, "wetland_basin", 1.8D);
		addRock(rocks, "mineralogy:rock_salt", RockFamily.SEDIMENTARY, 54, 28, 0.6D,
				"arid_basin", 4.0D, "coastal_shelf", 1.2D);
		addRock(rocks, "mineralogy:chert", RockFamily.SEDIMENTARY, 48, 32, 0.65D,
				"coastal_shelf", 2.0D, "sedimentary_basin", 1.4D);
		addRock(rocks, "mineralogy:gypsum", RockFamily.SEDIMENTARY, 52, 26, 0.65D,
				"arid_basin", 3.5D, "coastal_shelf", 1.0D);
		addRock(rocks, "mineralogy:chalk", RockFamily.SEDIMENTARY, 68, 24, 0.65D,
				"coastal_shelf", 3.0D);
		addRock(rocks, "minecraft:sandstone", RockFamily.SEDIMENTARY, 72, 28, 0.55D,
				"arid_basin", 2.5D, "coastal_shelf", 0.8D);

		addRock(rocks, "mineralogy:marble", RockFamily.METAMORPHIC, 32, 36, 0.85D,
				"mountain_belt", 1.8D, "stable_craton", 1.2D);
		addRock(rocks, "mineralogy:slate", RockFamily.METAMORPHIC, 36, 34, 1.0D,
				"mountain_belt", 2.0D, "sedimentary_basin", 0.7D);
		addRock(rocks, "mineralogy:schist", RockFamily.METAMORPHIC, 24, 34, 1.0D,
				"mountain_belt", 2.8D);
		addRock(rocks, "mineralogy:gneiss", RockFamily.METAMORPHIC, 18, 38, 1.0D,
				"mountain_belt", 2.6D, "stable_craton", 1.2D);
		addRock(rocks, "mineralogy:phyllite", RockFamily.METAMORPHIC, 34, 34, 0.9D,
				"mountain_belt", 2.0D);
		addRock(rocks, "mineralogy:amphibolite", RockFamily.METAMORPHIC, 18, 34, 0.9D,
				"mountain_belt", 2.4D, "volcanic_arc", 0.8D);
		addRock(rocks, "mineralogy:hornfels", RockFamily.METAMORPHIC, 24, 28, 0.75D,
				"volcanic_arc", 2.0D, "mountain_belt", 1.4D);
		addRock(rocks, "mineralogy:quartzite", RockFamily.METAMORPHIC, 30, 34, 0.85D,
				"mountain_belt", 1.8D, "stable_craton", 1.0D);
		addRock(rocks, "mineralogy:novaculite", RockFamily.METAMORPHIC, 28, 30, 0.65D,
				"mountain_belt", 1.6D);
	}

	private static void addWeights(JsonObject parent, String key, Object... values) {
		JsonObject weights = new JsonObject();
		for (int i = 0; i + 1 < values.length; i += 2) {
			weights.addProperty((String) values[i], (Double) values[i + 1]);
		}
		parent.add(key, weights);
	}

	private static void addRock(JsonObject rocks, String id, RockFamily family, int peak, int spread,
			double weight, Object... geomeWeights) {
		JsonObject rock = new JsonObject();
		rock.addProperty("family", family.configName);
		rock.addProperty("depth_peak", peak);
		rock.addProperty("depth_spread", spread);
		rock.addProperty("weight", weight);
		JsonObject geomes = new JsonObject();
		for (int i = 0; i + 1 < geomeWeights.length; i += 2) {
			geomes.addProperty((String) geomeWeights[i], (Double) geomeWeights[i + 1]);
		}
		rock.add("geomes", geomes);
		rocks.add(id, rock);
	}
}
