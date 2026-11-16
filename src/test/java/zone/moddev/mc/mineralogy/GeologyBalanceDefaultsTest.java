package zone.moddev.mc.mineralogy;

import static org.junit.Assert.*;
import com.google.gson.*;
import java.nio.file.*;
import org.junit.Test;

/** Small resource contracts; world-generation censuses run locally, not in CI. */
public class GeologyBalanceDefaultsTest {
    private JsonObject provider() throws Exception {
        return JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/mineralogy/orespawn/provider.json"))).getAsJsonObject();
    }
    @Test public void evaporitesHaveExplicitProvinceWeights() throws Exception {
        JsonObject p=provider();
        assertEquals(32,p.getAsJsonObject("rocks").size());
        for(String name:new String[]{"rock_salt","gypsum"}) {
            JsonObject rock=p.getAsJsonObject("rocks").getAsJsonObject("mineralogy:rock/mineralogy/"+name);
            assertEquals(8,rock.getAsJsonObject("geomes").size());
            assertTrue(rock.getAsJsonObject("geomes").get("arid_basin").getAsDouble()>rock.getAsJsonObject("geomes").get("wetland_basin").getAsDouble());
        }
        assertEquals(.18,p.getAsJsonObject("rocks").getAsJsonObject("mineralogy:rock/mineralogy/rock_salt").get("weight").getAsDouble(),1e-9);
        assertEquals(.42,p.getAsJsonObject("rocks").getAsJsonObject("mineralogy:rock/mineralogy/gypsum").get("weight").getAsDouble(),1e-9);
    }
    @Test public void newWorldOresUseNaturalHostsWithoutTakingOverVanilla() throws Exception {
        JsonObject p=provider();
        assertFalse(p.getAsJsonObject("profile_defaults").get("manage_vanilla_ores").getAsBoolean());
        assertEquals(3,p.getAsJsonObject("ores").size());
        for(var entry:p.getAsJsonObject("ores").entrySet()) {
            JsonObject rule=entry.getValue().getAsJsonObject().getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
            assertEquals(12,rule.get("quantity").getAsInt());
            assertEquals(0,rule.getAsJsonArray("host_tags").size());
            assertEquals(2,rule.getAsJsonArray("host_blocks").size());
            assertEquals(8,rule.getAsJsonObject("geomes").size());
        }
        JsonObject biomes=p.getAsJsonObject("profile_defaults").getAsJsonObject("biomes");
        assertEquals(5,biomes.size());
        assertEquals(3.65,biomes.getAsJsonObject("minecraft:plains").get("sedimentary_basin").getAsDouble(),1e-9);
        assertEquals(4.3,biomes.getAsJsonObject("minecraft:birch_forest").get("volcanic_arc").getAsDouble(),1e-9);
    }
}
