package com.mcmoddev.mineralogy.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonObject;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Algorithm;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Preset;

import org.junit.jupiter.api.Test;

class WorldGeologyProfileTest {
	@Test
	void schemaOneMigrationPreservesShapeAndSnapshotsGlobalGeology() {
		JsonObject global = completeGlobalFixture();
		WorldGeologyProfile fallback = WorldGeologyProfile.fromGlobalConfig(global, GeologyMode.GEOME, true);

		JsonObject legacy = new JsonObject();
		legacy.addProperty("schema_version", 1);
		legacy.addProperty("geology_mode", "legacy");
		legacy.addProperty("place_crude_oil", false);
		JsonObject formations = fallback.toFormationJson();
		formations.addProperty("algorithm", "sky_v1");
		formations.addProperty("horizontal_size", "custom");
		legacy.add("formations", formations);

		WorldGeologyProfile migrated = WorldGeologyProfile.fromJson(legacy, fallback);
		JsonObject result = migrated.toJson();
		assertEquals(WorldGeologyProfile.SCHEMA_VERSION, result.get("schema_version").getAsInt());
		assertEquals(GeologyMode.LEGACY, migrated.geologyMode());
		assertEquals(Algorithm.SKY_V1, migrated.algorithm());
		assertEquals(Preset.CUSTOM, migrated.horizontalSize());
		assertTrue(result.has("geomes"));
		assertTrue(result.has("rocks"));
		assertTrue(result.has("ores"));
		assertTrue(result.has("oil"));
	}

	@Test
	void schemaTwoRoundTripKeepsExternalAndProviderData() {
		JsonObject root = completeGlobalFixture();
		root.addProperty("schema_version", WorldGeologyProfile.SCHEMA_VERSION);
		root.getAsJsonObject("rocks").add("examplemod:slate", rockFixture());
		JsonObject provider = new JsonObject();
		provider.addProperty("provider_revision", 3);
		root.getAsJsonObject("ore_providers").add("basemetals", provider);

		WorldGeologyProfile profile = WorldGeologyProfile.fromJson(root,
				WorldGeologyProfile.recommended(true));
		JsonObject result = profile.toJson();
		assertTrue(result.getAsJsonObject("rocks").has("examplemod:slate"));
		assertEquals(3, result.getAsJsonObject("ore_providers")
				.getAsJsonObject("basemetals").get("provider_revision").getAsInt());
		assertEquals(WorldGeologyProfile.SCHEMA_VERSION, result.get("schema_version").getAsInt());
	}

	@Test
	void aliasDefaultsRepairUsesVanillaKeysWithoutChangingRockOrderOrTraits() {
		JsonObject original = new JsonObject();
		JsonObject rocks = new JsonObject();
		JsonObject andesite = rockFixture();
		andesite.addProperty("weight", 2.25D);
		rocks.add("mineralogy:andesite", andesite);
		rocks.add("mineralogy:basaltic_glass", rockFixture());
		rocks.add("mineralogy:diorite", rockFixture());
		original.add("rocks", rocks);

		JsonObject defaults = new JsonObject();
		JsonObject aliases = new JsonObject();
		aliases.addProperty("mineralogy:andesite", "minecraft:andesite");
		aliases.addProperty("mineralogy:diorite", "minecraft:diorite");
		defaults.add("worldgen_aliases", aliases);

		JsonObject repaired = GeomeConfig.refreshWorldgenAliasDefaults(original, defaults);
		JsonObject repairedRocks = repaired.getAsJsonObject("rocks");
		assertEquals(Arrays.asList("minecraft:andesite", "mineralogy:basaltic_glass", "minecraft:diorite"),
				new ArrayList<>(repairedRocks.keySet()));
		assertEquals(2.25D, repairedRocks.getAsJsonObject("minecraft:andesite").get("weight").getAsDouble());
		assertFalse(repairedRocks.has("mineralogy:andesite"));
		assertTrue(repaired.getAsJsonObject("worldgen_aliases").has("mineralogy:andesite"));
		assertEquals(1, repaired.get("worldgen_alias_defaults_revision").getAsInt());
	}

	private static JsonObject completeGlobalFixture() {
		JsonObject root = WorldGeologyProfile.recommended(true).toJson();
		root.add("geomes", objectWith("stable_craton", new JsonObject()));
		root.add("biomes", new JsonObject());
		root.add("biome_dictionary", new JsonObject());
		root.add("worldgen_aliases", new JsonObject());
		root.add("rocks", objectWith("minecraft:stone", rockFixture()));
		root.add("ores", new JsonObject());
		root.add("oil", new JsonObject());
		root.add("cyano", new JsonObject());
		root.add("ore_providers", new JsonObject());
		return root;
	}

	private static JsonObject rockFixture() {
		JsonObject rock = new JsonObject();
		rock.addProperty("enabled", true);
		rock.addProperty("family", "sedimentary");
		rock.addProperty("weight", 1.0D);
		return rock;
	}

	private static JsonObject objectWith(String key, JsonObject value) {
		JsonObject result = new JsonObject();
		result.add(key, value);
		return result;
	}
}
