package com.mcmoddev.mineralogy.worldgen;

import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Algorithm;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Preset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A complete, self-contained snapshot of the geology settings for one world. */
public final class WorldGeologyProfile {
	public static final int SCHEMA_VERSION = 2;

	private static final Logger LOGGER = LogManager.getLogger();

	private final JsonObject root;
	private final GeologyMode geologyMode;
	private final Algorithm algorithm;
	private final Preset horizontalSize;
	private final Preset verticalThickness;
	private final Preset waviness;
	private final Preset edgeIrregularity;
	private final Preset formationContinuity;
	private final boolean placeCrudeOil;

	private WorldGeologyProfile(JsonObject root, GeologyMode fallbackMode, boolean fallbackOil) {
		this.root = root.deepCopy();
		this.root.addProperty("schema_version", SCHEMA_VERSION);
		geologyMode = enumValue(this.root, "geology_mode", GeologyMode.class, fallbackMode);
		placeCrudeOil = booleanValue(this.root, "place_crude_oil", fallbackOil);
		this.root.addProperty("geology_mode", geologyMode.name().toLowerCase(Locale.ROOT));
		this.root.addProperty("place_crude_oil", placeCrudeOil);

		JsonObject formations = object(this.root, "formations", recommendedFormationJson());
		algorithm = namedValue(formations, "algorithm", Algorithm.STABLE_LAYERS, Algorithm::fromConfigName);
		horizontalSize = namedValue(formations, "horizontal_size", Preset.AVERAGE, Preset::fromConfigName);
		verticalThickness = namedValue(formations, "vertical_thickness", Preset.AVERAGE, Preset::fromConfigName);
		waviness = namedValue(formations, "waviness", Preset.AVERAGE, Preset::fromConfigName);
		edgeIrregularity = namedValue(formations, "edge_irregularity", Preset.AVERAGE, Preset::fromConfigName);
		formationContinuity = namedValue(formations, "formation_continuity", Preset.AVERAGE,
				Preset::fromConfigName);
		this.root.add("formations", normalizedFormationJson(formations));
	}

	public static WorldGeologyProfile recommended(boolean placeCrudeOil) {
		JsonObject root = new JsonObject();
		root.addProperty("geology_mode", GeologyMode.GEOME.name().toLowerCase(Locale.ROOT));
		root.addProperty("place_crude_oil", placeCrudeOil);
		root.add("formations", recommendedFormationJson());
		return new WorldGeologyProfile(root, GeologyMode.GEOME, placeCrudeOil);
	}

	public static WorldGeologyProfile fromGlobalConfig(JsonObject globalRoot,
			GeologyMode geologyMode, boolean placeCrudeOil) {
		JsonObject root = globalRoot.deepCopy();
		if (!root.has("geology_mode")) {
			root.addProperty("geology_mode", geologyMode.name().toLowerCase(Locale.ROOT));
		}
		if (!root.has("place_crude_oil")) {
			root.addProperty("place_crude_oil", placeCrudeOil);
		}
		return new WorldGeologyProfile(root, geologyMode, placeCrudeOil);
	}

	public static WorldGeologyProfile fromJson(JsonObject json, WorldGeologyProfile fallback) {
		int schema = intValue(json, "schema_version", 1);
		if (schema >= SCHEMA_VERSION) {
			return new WorldGeologyProfile(json, fallback.geologyMode, fallback.placeCrudeOil);
		}

		// Schema 1 contained only mode, oil and formations. Overlay those fields on
		// the currently effective pack profile so the resulting profile is complete.
		JsonObject migrated = fallback.rootCopy();
		copyIfPresent(json, migrated, "geology_mode");
		copyIfPresent(json, migrated, "place_crude_oil");
		copyIfPresent(json, migrated, "formations");
		return new WorldGeologyProfile(migrated, fallback.geologyMode, fallback.placeCrudeOil);
	}

	public WorldGeologyProfile withSelection(GeologyMode mode,
			Preset horizontal, Preset thickness, Preset wave,
			Preset irregularity, Preset continuity, boolean oil) {
		JsonObject edited = rootCopy();
		edited.addProperty("geology_mode", mode.name().toLowerCase(Locale.ROOT));
		edited.addProperty("place_crude_oil", oil);
		JsonObject formations = toFormationJson();
		formations.addProperty("algorithm", Algorithm.STABLE_LAYERS.configName());
		formations.addProperty("horizontal_size", horizontal.configName());
		formations.addProperty("vertical_thickness", thickness.configName());
		formations.addProperty("waviness", wave.configName());
		formations.addProperty("edge_irregularity", irregularity.configName());
		formations.addProperty("formation_continuity", continuity.configName());
		edited.add("formations", formations);
		return new WorldGeologyProfile(edited, mode, oil);
	}

	public WorldGeologyProfile withRoot(JsonObject editedRoot) {
		return new WorldGeologyProfile(editedRoot, geologyMode, placeCrudeOil);
	}

	public WorldGeologyProfile copy() {
		return new WorldGeologyProfile(root, geologyMode, placeCrudeOil);
	}

	public JsonObject toJson() {
		return rootCopy();
	}

	public JsonObject rootCopy() {
		JsonObject copy = root.deepCopy();
		copy.addProperty("schema_version", SCHEMA_VERSION);
		return copy;
	}

	public JsonObject toGeomeConfigJson() {
		JsonObject copy = rootCopy();
		copy.addProperty("schema_version", GeomeConfig.SCHEMA_VERSION);
		return copy;
	}

	public JsonObject toFormationJson() {
		return root.getAsJsonObject("formations").deepCopy();
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

	public int cyanoGeomeSize() {
		return nestedInt("cyano", "geome_size", 256, 4, Short.MAX_VALUE);
	}

	public double cyanoRockLayerNoise() {
		return nestedDouble("cyano", "rock_layer_noise", 32.0D, 1.0D, Short.MAX_VALUE);
	}

	public int cyanoLayerThickness() {
		return nestedInt("cyano", "rock_layer_thickness", 8, 1, 255);
	}

	private static JsonObject recommendedFormationJson() {
		JsonObject formations = new JsonObject();
		formations.addProperty("algorithm", Algorithm.STABLE_LAYERS.configName());
		formations.addProperty("horizontal_size", Preset.AVERAGE.configName());
		formations.addProperty("vertical_thickness", Preset.AVERAGE.configName());
		formations.addProperty("waviness", Preset.AVERAGE.configName());
		formations.addProperty("edge_irregularity", Preset.AVERAGE.configName());
		formations.addProperty("formation_continuity", Preset.AVERAGE.configName());
		JsonObject custom = new JsonObject();
		custom.addProperty("stratum_wavelength", 256.0D);
		custom.addProperty("family_region_wavelength", 100.0D);
		custom.addProperty("vertical_thickness", 8);
		custom.addProperty("waviness_wavelength", 256.0D);
		custom.addProperty("waviness_amplitude", 48.0D);
		custom.addProperty("edge_wavelength", 64.0D);
		custom.addProperty("edge_amplitude", 12.0D);
		custom.addProperty("edge_octaves", 2);
		custom.addProperty("continuity", 0.85D);
		formations.add("custom", custom);
		return formations;
	}

	private static JsonObject normalizedFormationJson(JsonObject source) {
		JsonObject fallback = recommendedFormationJson();
		JsonObject result = source.deepCopy();
		JsonObject custom = object(result, "custom", fallback.getAsJsonObject("custom"));
		for (String key : new String[] { "stratum_wavelength", "family_region_wavelength",
				"vertical_thickness", "waviness_wavelength", "waviness_amplitude",
				"edge_wavelength", "edge_amplitude", "edge_octaves", "continuity" }) {
			if (!custom.has(key)) {
				custom.add(key, fallback.getAsJsonObject("custom").get(key).deepCopy());
			}
		}
		result.add("custom", custom);
		return result;
	}

	private static void copyIfPresent(JsonObject source, JsonObject target, String key) {
		if (source.has(key)) {
			target.add(key, source.get(key).deepCopy());
		}
	}

	private int nestedInt(String section, String key, int fallback, int min, int max) {
		try {
			JsonObject object = root.has(section) && root.get(section).isJsonObject()
					? root.getAsJsonObject(section) : null;
			int value = object != null && object.has(key) ? object.get(key).getAsInt() : fallback;
			return Math.max(min, Math.min(max, value));
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private double nestedDouble(String section, String key, double fallback, double min, double max) {
		try {
			JsonObject object = root.has(section) && root.get(section).isJsonObject()
					? root.getAsJsonObject(section) : null;
			double value = object != null && object.has(key) ? object.get(key).getAsDouble() : fallback;
			return Math.max(min, Math.min(max, value));
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static JsonObject object(JsonObject source, String key, JsonObject fallback) {
		JsonElement element = source.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : fallback.deepCopy();
	}

	private static boolean booleanValue(JsonObject source, String key, boolean fallback) {
		try {
			return source.has(key) ? source.get(key).getAsBoolean() : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	private static int intValue(JsonObject source, String key, int fallback) {
		try {
			return source.has(key) ? source.get(key).getAsInt() : fallback;
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static <T extends Enum<T>> T enumValue(JsonObject source, String key, Class<T> type, T fallback) {
		try {
			return source.has(key)
					? Enum.valueOf(type, source.get(key).getAsString().toUpperCase(Locale.ROOT)) : fallback;
		} catch (RuntimeException e) {
			LOGGER.warn("Invalid Mineralogy world geology value for '{}'; using {}", key, fallback);
			return fallback;
		}
	}

	private static <T> T namedValue(JsonObject source, String key, T fallback, NamedValueParser<T> parser) {
		try {
			return source.has(key) ? parser.parse(source.get(key).getAsString()) : fallback;
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
