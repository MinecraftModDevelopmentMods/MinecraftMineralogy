package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

/** Release-critical target-native resource contracts retained by the 1.12 port. */
public class ResourceContractTest {
    @Test
    public void rockSaltLampItemModelsRetainTheAcceptedGuiTransforms() throws Exception {
        JsonObject lamp = json("src/main/resources/assets/mineralogy/models/item/rocksaltlamp.json");
        assertEquals("mineralogy:block/rocksaltlamp", lamp.get("parent").getAsString());
        assertVector(lamp, "rotation", 30.0D, 45.0D, 0.0D);
        assertVector(lamp, "translation", 0.0D, 4.0D, 0.0D);
        assertVector(lamp, "scale", 1.35D, 1.35D, 1.35D);

        JsonObject street = json(
                "src/main/resources/assets/mineralogy/models/item/rocksaltstreetlamp.json");
        assertEquals("mineralogy:block/rocksaltstreetlamp_inventory",
                street.get("parent").getAsString());
        assertVector(street, "rotation", 20.0D, 35.0D, 0.0D);
        assertVector(street, "translation", 0.0D, 1.0D, 0.0D);
        assertVector(street, "scale", 0.82D, 0.82D, 0.82D);
    }

    @Test
    public void crudeOilResourcesUseMineralogysIsolatedIdentity() throws Exception {
        JsonObject blockstate = json(
                "src/main/resources/assets/mineralogy/blockstates/crude_oil.json");
        assertTrue(blockstate.has("forge_marker"));
        JsonObject bucket = json(
                "src/main/resources/assets/mineralogy/models/item/crude_oil_bucket.json");
        assertEquals("forge:bucket", bucket.get("loader").getAsString());
        assertEquals("mineralogy_crude_oil", bucket.get("fluid").getAsString());
    }

    @Test
    public void onlyTheOreSpawn4ProviderDiscoverySurfaceIsPackaged() {
        assertTrue(new File(
                "src/main/resources/assets/mineralogy/orespawn/provider.json").isFile());
        assertFalse(new File(
                "src/main/resources/assets/mineralogy/orespawn/_replacements.json").exists());
    }

    private static JsonObject json(String path) throws Exception {
        try (FileReader reader = new FileReader(path)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private static void assertVector(JsonObject model, String name,
            double first, double second, double third) {
        JsonArray vector = model.getAsJsonObject("display").getAsJsonObject("gui")
                .getAsJsonArray(name);
        assertEquals(3, vector.size());
        assertEquals(first, vector.get(0).getAsDouble(), 0.0D);
        assertEquals(second, vector.get(1).getAsDouble(), 0.0D);
        assertEquals(third, vector.get(2).getAsDouble(), 0.0D);
    }
}
