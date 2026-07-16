package com.mcmoddev.mineralogy.worldgen;

import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Algorithm;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Preset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WorldGeologyProfile {
	public static final int SCHEMA_VERSION = 1;

	private static final Logger LOGGER = LogManager.getLogger();

	private final GeologyMode geologyMode;
	private final Algorithm algorithm;
	private final Preset horizontalSize;
	private final Preset verticalThickness;
	private final Preset waviness;
	private final Preset edgeIrregularity;
	private final Preset formationContinuity;
	private final double customStratumWavelength;
	private final double customFamilyRegionWavelength;
	private final double customWavinessWavelength;
	private final double customEdgeWavelength;
	private final int customVerticalThickness;
	private final double customWavinessAmplitude;
	private final double customEdgeAmplitude;
	private final int customEdgeOctaves;
	private final double customContinuity;
	private final boolean placeCrudeOil;

	private WorldGeologyProfile(GeologyMode geologyMode, Algorithm algorithm,
			Preset horizontalSize, Preset verticalThickness, Preset waviness,
			Preset edgeIrregularity, Preset formationContinuity,
			double customStratumWavelength, double customFamilyRegionWavelength,
			double customWavinessWavelength, double customEdgeWavelength,
			int customVerticalThickness, double customWavinessAmplitude,
			double customEdgeAmplitude, int customEdgeOctaves,
			double customContinuity, boolean placeCrudeOil) {
		this.geologyMode = geologyMode;
		this.algorithm = algorithm;
		this.horizontalSize = horizontalSize;
		this.verticalThickness = verticalThickness;
		this.waviness = waviness;
		this.edgeIrregularity = edgeIrregularity;
		this.formationContinuity = formationContinuity;
		this.customStratumWavelength = customStratumWavelength;
		this.customFamilyRegionWavelength = customFamilyRegionWavelength;
		this.customWavinessWavelength = customWavinessWavelength;
		this.customEdgeWavelength = customEdgeWavelength;
		this.customVerticalThickness = customVerticalThickness;
		this.customWavinessAmplitude = customWavinessAmplitude;
		this.customEdgeAmplitude = customEdgeAmplitude;
		this.customEdgeOctaves = customEdgeOctaves;
		this.customContinuity = customContinuity;
		this.placeCrudeOil = placeCrudeOil;
	}

	public static WorldGeologyProfile recommended(boolean placeCrudeOil) {
		return new WorldGeologyProfile(GeologyMode.GEOME, Algorithm.STABLE_LAYERS,
				Preset.AVERAGE, Preset.AVERAGE, Preset.AVERAGE, Preset.AVERAGE, Preset.AVERAGE,
				256.0D, 100.0D, 256.0D, 64.0D,
				8, 48.0D, 12.0D, 2, 0.85D, placeCrudeOil);
	}

	public static WorldGeologyProfile fromGlobalConfig(JsonObject root,
			GeologyMode geologyMode, boolean placeCrudeOil) {
		WorldGeologyProfile fallback = recommended(placeCrudeOil);
		JsonObject formations = object(root, "formations", fallback.toFormationJson());
		return fromFormationConfig(formations, geologyMode, placeCrudeOil, fallback);
	}

	public static WorldGeologyProfile fromJson(JsonObject root, WorldGeologyProfile fallback) {
		GeologyMode geologyMode = enumValue(root, "geology_mode", GeologyMode.class, fallback.geologyMode);
		boolean placeCrudeOil = booleanValue(root, "place_crude_oil", fallback.placeCrudeOil);
		JsonObject formations = object(root, "formations", fallback.toFormationJson());
		return fromFormationConfig(formations, geologyMode, placeCrudeOil, fallback);
	}

	private static WorldGeologyProfile fromFormationConfig(JsonObject formations,
			GeologyMode geologyMode, boolean placeCrudeOil, WorldGeologyProfile fallback) {
		Algorithm algorithm = namedValue(formations, "algorithm",
				fallback.algorithm, Algorithm::fromConfigName);
		Preset horizontalSize = namedValue(formations, "horizontal_size",
				fallback.horizontalSize, Preset::fromConfigName);
		Preset verticalThickness = namedValue(formations, "vertical_thickness",
				fallback.verticalThickness, Preset::fromConfigName);
		Preset waviness = namedValue(formations, "waviness",
				fallback.waviness, Preset::fromConfigName);
		Preset edgeIrregularity = namedValue(formations, "edge_irregularity",
				fallback.edgeIrregularity, Preset::fromConfigName);
		Preset formationContinuity = namedValue(formations, "formation_continuity",
				fallback.formationContinuity, Preset::fromConfigName);
		JsonObject custom = object(formations, "custom", fallback.toFormationJson().getAsJsonObject("custom"));

		return new WorldGeologyProfile(geologyMode, algorithm,
				horizontalSize, verticalThickness, waviness, edgeIrregularity, formationContinuity,
				doubleValue(custom, "stratum_wavelength", fallback.customStratumWavelength, 16.0D, 8192.0D),
				doubleValue(custom, "family_region_wavelength", fallback.customFamilyRegionWavelength, 16.0D, 8192.0D),
				doubleValue(custom, "waviness_wavelength", fallback.customWavinessWavelength, 32.0D, 2048.0D),
				doubleValue(custom, "edge_wavelength", fallback.customEdgeWavelength, 8.0D, 512.0D),
				intValue(custom, "vertical_thickness", fallback.customVerticalThickness, 1, 64),
				doubleValue(custom, "waviness_amplitude", fallback.customWavinessAmplitude, 0.0D, 512.0D),
				doubleValue(custom, "edge_amplitude", fallback.customEdgeAmplitude, 0.0D, 256.0D),
				intValue(custom, "edge_octaves", fallback.customEdgeOctaves, 1, 8),
				doubleValue(custom, "continuity", fallback.customContinuity, 0.0D, 1.0D),
				placeCrudeOil);
	}

	public WorldGeologyProfile withSelection(GeologyMode geologyMode,
			Preset horizontalSize, Preset verticalThickness, Preset waviness,
			Preset edgeIrregularity, Preset formationContinuity, boolean placeCrudeOil) {
		return new WorldGeologyProfile(geologyMode, Algorithm.STABLE_LAYERS,
				horizontalSize, verticalThickness, waviness, edgeIrregularity, formationContinuity,
				customStratumWavelength, customFamilyRegionWavelength,
				customWavinessWavelength, customEdgeWavelength, customVerticalThickness,
				customWavinessAmplitude, customEdgeAmplitude, customEdgeOctaves,
				customContinuity, placeCrudeOil);
	}

	public JsonObject toJson() {
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", SCHEMA_VERSION);
		root.addProperty("geology_mode", geologyMode.name().toLowerCase(Locale.ROOT));
		root.addProperty("place_crude_oil", placeCrudeOil);
		root.add("formations", toFormationJson());
		return root;
	}

	public JsonObject toFormationJson() {
		JsonObject formations = new JsonObject();
		formations.addProperty("algorithm", algorithm.configName());
		formations.addProperty("horizontal_size", horizontalSize.configName());
		formations.addProperty("vertical_thickness", verticalThickness.configName());
		formations.addProperty("waviness", waviness.configName());
		formations.addProperty("edge_irregularity", edgeIrregularity.configName());
		formations.addProperty("formation_continuity", formationContinuity.configName());

		JsonObject custom = new JsonObject();
		custom.addProperty("stratum_wavelength", customStratumWavelength);
		custom.addProperty("family_region_wavelength", customFamilyRegionWavelength);
		custom.addProperty("vertical_thickness", customVerticalThickness);
		custom.addProperty("waviness_wavelength", customWavinessWavelength);
		custom.addProperty("waviness_amplitude", customWavinessAmplitude);
		custom.addProperty("edge_wavelength", customEdgeWavelength);
		custom.addProperty("edge_amplitude", customEdgeAmplitude);
		custom.addProperty("edge_octaves", customEdgeOctaves);
		custom.addProperty("continuity", customContinuity);
		formations.add("custom", custom);
		return formations;
	}

	public GeologyMode geologyMode() {
		return geologyMode;
	}

	public Algorithm algorithm() {
		return algorithm;
	}

	public Preset horizontalSize() {
		return horizontalSize;
	}

	public Preset verticalThickness() {
		return verticalThickness;
	}

	public Preset waviness() {
		return waviness;
	}

	public Preset edgeIrregularity() {
		return edgeIrregularity;
	}

	public Preset formationContinuity() {
		return formationContinuity;
	}

	public boolean placeCrudeOil() {
		return placeCrudeOil;
	}

	private static JsonObject object(JsonObject root, String key, JsonObject fallback) {
		JsonElement element = root.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : fallback.deepCopy();
	}

	private static boolean booleanValue(JsonObject root, String key, boolean fallback) {
		try {
			return root.has(key) ? root.get(key).getAsBoolean() : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	private static int intValue(JsonObject root, String key, int fallback, int min, int max) {
		try {
			return root.has(key) ? Math.max(min, Math.min(max, root.get(key).getAsInt())) : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	private static double doubleValue(JsonObject root, String key, double fallback, double min, double max) {
		try {
			return root.has(key) ? Math.max(min, Math.min(max, root.get(key).getAsDouble())) : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	private static <T extends Enum<T>> T enumValue(JsonObject root, String key, Class<T> type, T fallback) {
		try {
			return root.has(key) ? Enum.valueOf(type, root.get(key).getAsString().toUpperCase(Locale.ROOT)) : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	private static <T extends Enum<T>> T namedValue(JsonObject root, String key,
			T fallback, NamedValueParser<T> parser) {
		try {
			return root.has(key) ? parser.parse(root.get(key).getAsString()) : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	@FunctionalInterface
	private interface NamedValueParser<T> {
		T parse(String value);
	}
}
