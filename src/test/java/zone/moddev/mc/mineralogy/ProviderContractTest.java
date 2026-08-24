package zone.moddev.mc.mineralogy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ProviderContractTest {
    private JsonObject provider() {
        return new JsonParser().parse(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("assets/mineralogy/orespawn/provider.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    public void providerUsesTargetNativeSchemaAndStableIds() {
        JsonObject provider = provider();
        assertEquals(4, provider.get("schema_version").getAsInt());
        assertEquals("mineralogy", provider.get("provider_modid").getAsString());
        assertEquals(3, provider.get("provider_revision").getAsInt());
        assertEquals(32, provider.getAsJsonObject("rocks").entrySet().size());
        assertEquals(3, provider.getAsJsonObject("ores").entrySet().size());
        assertTrue(provider.getAsJsonObject("fluid_deposits")
                .has("mineralogy:fluid_deposit/crude_oil"));
    }

    @Test
    public void providerUses112HeightsMetadataAndHosts() {
        JsonObject provider = provider();
        JsonObject rocks = provider.getAsJsonObject("rocks");
        for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : rocks.entrySet()) {
            String id = entry.getKey();
            JsonObject rock = rocks.getAsJsonObject(id);
            assertEquals(id, 0, rock.get("min_y").getAsInt());
            assertEquals(id, 255, rock.get("max_y").getAsInt());
            assertFalse(id, rock.get("block").getAsString().contains("deepslate"));
        }
        assertMetadata(rocks, "mineralogy:rock/minecraft/granite", 1);
        assertMetadata(rocks, "mineralogy:rock/minecraft/diorite", 3);
        assertMetadata(rocks, "mineralogy:rock/minecraft/andesite", 5);
        assertEquals("mineralogy:basalt", rocks.getAsJsonObject("mineralogy:rock/minecraft/basalt").get("block").getAsString());
        assertEquals("mineralogy:tuff", rocks.getAsJsonObject("mineralogy:rock/minecraft/tuff").get("block").getAsString());

        Set<String> expectedFamilies = new HashSet<String>(Arrays.asList(
                "sedimentary", "metamorphic", "igneous_intrusive", "igneous_volcanic"));
        for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : provider.getAsJsonObject("ores").entrySet()) {
            String id = entry.getKey();
            JsonObject dimension = provider.getAsJsonObject("ores").getAsJsonObject(id)
                    .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
            Set<String> families = new HashSet<String>();
            dimension.getAsJsonArray("host_families").forEach(value -> families.add(value.getAsString()));
            assertEquals(expectedFamilies, families);
            assertEquals("forge:stone", dimension.getAsJsonArray("host_tags").get(0).getAsString());
            assertFalse(dimension.toString().contains("deepslate"));
        }
    }

    @Test
    public void providerRecommendsM6GeomesAndCoveredOceanOil() {
        JsonObject provider = provider();
        JsonObject defaults = provider.getAsJsonObject("profile_defaults");
        assertEquals("geome", defaults.get("geology_mode").getAsString());
        assertEquals("stable_layers", defaults.getAsJsonObject("formations").get("algorithm").getAsString());
        assertTrue(defaults.get("place_fluid_deposits").getAsBoolean());
        assertFalse(defaults.get("manage_vanilla_ores").getAsBoolean());
        assertEquals(1, defaults.getAsJsonObject("terrain_dimensions").entrySet().size());
        assertTrue(defaults.getAsJsonObject("terrain_dimensions").has("minecraft:overworld"));
        assertFalse(defaults.toString().contains("minecraft:the_nether"));
        assertFalse(defaults.toString().contains("minecraft:the_end"));
        assertEquals(100, defaults.getAsJsonObject("cyano").get("geome_size").getAsInt());
        assertEquals(32.0D, defaults.getAsJsonObject("cyano").get("rock_layer_noise").getAsDouble(), 0.0D);
        assertEquals(8, defaults.getAsJsonObject("cyano").get("rock_layer_thickness").getAsInt());
        assertFalse(defaults.has("worldgen_aliases"));

        JsonObject oil = provider.getAsJsonObject("fluid_deposits")
                .getAsJsonObject("mineralogy:fluid_deposit/crude_oil");
        assertEquals("mineralogy:crude_oil", oil.get("block").getAsString());
        JsonObject dimension = oil.getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
        assertEquals(0, dimension.get("min_y").getAsInt());
        assertEquals(48, dimension.get("max_y").getAsInt());
        assertEquals(0.08D, dimension.get("frequency").getAsDouble(), 0.0D);
        assertEquals(2, dimension.get("min_solid_cover").getAsInt());
        assertEquals("sedimentary", dimension.getAsJsonArray("host_families").get(0).getAsString());
        assertEquals("OCEAN", dimension.getAsJsonArray("biome_dictionary").get(0).getAsString());
    }

    private static void assertMetadata(JsonObject rocks, String id, int metadata) {
        JsonObject rock = rocks.getAsJsonObject(id);
        assertEquals("minecraft:stone", rock.get("block").getAsString());
        assertEquals(metadata, rock.get("metadata").getAsInt());
    }
}
