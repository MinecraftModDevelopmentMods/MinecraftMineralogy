package com.mcmoddev.mineralogy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class OreSpawnProviderTest {
	private static final Path PROVIDER = Paths.get("src", "main", "resources", "data",
			"mineralogy", "orespawn", "provider.json");

	@Test
	void packagedProviderIsACompleteSchemaTwoDeclaration() throws Exception {
		JsonObject root;
		try (Reader reader = Files.newBufferedReader(PROVIDER, StandardCharsets.UTF_8)) {
			root = JsonParser.parseReader(reader).getAsJsonObject();
		}

		assertEquals(2, root.get("schema_version").getAsInt());
		assertEquals("mineralogy", root.get("provider_modid").getAsString());
		assertEquals(32, root.getAsJsonObject("rocks").size());
		assertEquals(14, root.getAsJsonObject("ores").size());
		assertTrue(root.getAsJsonObject("profile_defaults").get("place_crude_oil").getAsBoolean());
		assertEquals("mineralogy:crude_oil", root.getAsJsonObject("profile_defaults")
				.getAsJsonObject("oil").get("block").getAsString());
		assertEquals("minecraft:basalt", root.getAsJsonObject("profile_defaults")
				.getAsJsonObject("worldgen_aliases").get("mineralogy:basalt").getAsString());
		assertFalse(root.getAsJsonObject("rocks").has("mineralogy:basalt"));
	}
}
