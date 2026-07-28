package zone.moddev.mc.mineralogy;

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
	void packagedProviderIsACompleteSchemaThreeDeclaration() throws Exception {
		JsonObject root;
		try (Reader reader = Files.newBufferedReader(PROVIDER, StandardCharsets.UTF_8)) {
			root = JsonParser.parseReader(reader).getAsJsonObject();
		}

		assertEquals(3, root.get("schema_version").getAsInt());
		assertEquals("mineralogy", root.get("provider_modid").getAsString());
		assertEquals(3, root.get("provider_revision").getAsInt());
		assertEquals(32, root.getAsJsonObject("rocks").size());
		assertEquals(3, root.getAsJsonObject("ores").size());
		assertFalse(root.getAsJsonObject("ores").has("mineralogy:ore/minecraft/coal_ore"));
		assertTrue(root.getAsJsonObject("profile_defaults").get("place_fluid_deposits").getAsBoolean());
		assertEquals(1, root.getAsJsonObject("fluid_deposits").size());
		JsonObject oil = root.getAsJsonObject("fluid_deposits")
				.getAsJsonObject("mineralogy:fluid_deposit/crude_oil");
		assertEquals("mineralogy:crude_oil", oil.get("block").getAsString());
		JsonObject overworld = oil.getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
		assertEquals("sedimentary", overworld.getAsJsonArray("host_families").get(0).getAsString());
		assertEquals("OCEAN", overworld.getAsJsonArray("biome_dictionary").get(0).getAsString());
		assertEquals(2, overworld.get("min_solid_cover").getAsInt());
		assertEquals("minecraft:basalt", root.getAsJsonObject("profile_defaults")
				.getAsJsonObject("worldgen_aliases").get("mineralogy:basalt").getAsString());
		assertFalse(root.getAsJsonObject("rocks").has("mineralogy:basalt"));
	}
}
