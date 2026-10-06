package zone.moddev.mc.mineralogy;

import com.google.gson.*;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

/** Target-independent resource contract; runtime probes independently test vanilla consumers. */
public class TerrainCompatibilityContractTest {
    private static final Path ROOT = Paths.get("src/main/resources/data");
    private static final String[] FAMILIES = ("andesite basalt diorite granite rhyolite pegmatite diabase gabbro peridotite "
            + "basaltic_glass scoria tuff shale conglomerate dolomite limestone siltstone marble slate schist gneiss phyllite "
            + "amphibolite hornfels quartzite novaculite rock_salt").split(" ");

    private String minecraft() throws Exception {
        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(Paths.get("gradle.properties"))) { properties.load(reader); }
        return properties.getProperty("minecraft_version");
    }
    private String kind(String kind) throws Exception {
        return Arrays.asList("1.19.4", "1.20.1", "1.20.6").contains(minecraft()) ? kind + "s" : kind;
    }
    private JsonObject json(String path) throws Exception {
        return JsonParser.parseString(new String(Files.readAllBytes(ROOT.resolve(path)), StandardCharsets.UTF_8)).getAsJsonObject();
    }
    private Set<String> members(JsonObject tag, boolean optional) {
        assertEquals(new HashSet<>(Arrays.asList("replace", "values")), tag.keySet());
        assertFalse(tag.get("replace").getAsBoolean());
        Set<String> ids = new TreeSet<>();
        for (JsonElement value : tag.getAsJsonArray("values")) {
            String id;
            if (value.isJsonObject()) {
                assertTrue("Unexpected optional entry", optional);
                JsonObject entry = value.getAsJsonObject();
                assertEquals(new HashSet<>(Arrays.asList("id", "required")), entry.keySet());
                assertFalse(entry.get("required").getAsBoolean());
                id = entry.get("id").getAsString();
            } else { assertTrue(value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()); id = value.getAsString(); }
            assertTrue("Malformed member: " + id, id.matches("#?[a-z0-9_.-]+:[a-z0-9_/.-]+"));
            assertTrue("Duplicate: " + id, ids.add(id));
        }
        return ids;
    }
    private Set<String> raw() {
        Set<String> result = new TreeSet<>();
        for (String family : FAMILIES) result.add("mineralogy:" + family);
        for (String special : Arrays.asList("chalk", "chert", "gypsum", "pumice")) result.add("mineralogy:" + special);
        return result;
    }
    @Test public void terrainIsExactlyAllProviderOutputsAndLegacyAliases() throws Exception {
        Set<String> expected = raw();
        for (String nativeRock : Arrays.asList("andesite", "basalt", "diorite", "granite", "sandstone", "tuff")) expected.add("minecraft:" + nativeRock);
        assertEquals(37, expected.size());
        JsonObject provider = json("mineralogy/orespawn/provider.json");
        Set<String> providerMembers = new TreeSet<>();
        for (Map.Entry<String, JsonElement> rule : provider.getAsJsonObject("rocks").entrySet()) providerMembers.add(rule.getValue().getAsJsonObject().get("block").getAsString());
        assertEquals(32, providerMembers.size());
        providerMembers.addAll(provider.getAsJsonObject("profile_defaults").getAsJsonObject("worldgen_aliases").keySet());
        assertEquals(expected, providerMembers);
        assertEquals(expected, members(json("mineralogy/tags/" + kind("block") + "/terrain_rocks.json"), false));
        assertEquals(expected, members(json("mineralogy/tags/" + kind("item") + "/terrain_rocks.json"), false));
    }
    @Test public void batSpawningUsesOnlyTheAdditiveBlockTerrainInterface() throws Exception {
        assertEquals(Collections.singleton("#mineralogy:terrain_rocks"),
                members(json("minecraft/tags/" + kind("block") + "/bats_spawnable_on.json"), false));
        assertFalse(Files.exists(ROOT.resolve("minecraft/tags/" + kind("item") + "/bats_spawnable_on.json")));
        assertFalse(Files.exists(ROOT.resolve("minecraft/tags/blocks/bats_spawnable_on.json")));
        // Do not broaden base_stone_overworld: it has unrelated worldgen consumers.
        assertFalse(Files.exists(ROOT.resolve("minecraft/tags/" + kind("block") + "/base_stone_overworld.json")));
    }
    @Test public void naturalReplacementIsAdditiveAndNotConfigConditional() throws Exception {
        for (String name : Arrays.asList("sculk_replaceable", "dripstone_replaceable_blocks", "moss_replaceable"))
            assertEquals(Collections.singleton("#mineralogy:terrain_rocks"), members(json("minecraft/tags/" + kind("block") + "/" + name + ".json"), false));
        // Vanilla aggregate references, not redundant overrides, are checked by the real-loader probe.
        assertFalse(Files.exists(ROOT.resolve("minecraft/tags/" + kind("block") + "/sculk_replaceable_world_gen.json")));
        assertFalse(Files.exists(ROOT.resolve("minecraft/tags/" + kind("block") + "/lush_ground_replaceable.json")));
    }
    @Test public void sulfurCubeMembershipIsOnlySupportedFullRockItems() throws Exception {
        boolean cubes = Arrays.asList("26.2", "26.3").contains(minecraft());
        Path stones = ROOT.resolve("mineralogy/tags/" + kind("item") + "/sulfur_cube_stones.json");
        Path archetype = ROOT.resolve("minecraft/tags/" + kind("item") + "/sulfur_cube_archetype/slow_bouncy.json");
        if (!cubes) { assertFalse(Files.exists(stones)); assertFalse(Files.exists(archetype)); return; }
        Set<String> expected = raw();
        for (String suffix : Arrays.asList("_smooth", "_brick", "_smooth_brick"))
            for (String family : FAMILIES) expected.add("mineralogy:" + family + suffix);
        JsonObject tag = json("mineralogy/tags/" + kind("item") + "/sulfur_cube_stones.json");
        assertEquals(112, expected.size());
        assertEquals(expected, members(tag, true));
        for (String id : expected) {
            String name = id.substring("mineralogy:".length());
            assertTrue("Nonexistent block identity: " + id, Files.exists(Paths.get("src/main/resources/assets/mineralogy/blockstates/" + name + ".json")));
            assertTrue("Nonexistent item identity: " + id, Files.exists(Paths.get("src/main/resources/assets/mineralogy/models/item/" + name + ".json")));
        }
        int optional = 0;
        for (JsonElement value : tag.getAsJsonArray("values")) {
            if (value.isJsonObject()) { optional++; assertFalse(raw().contains(value.getAsJsonObject().get("id").getAsString())); }
            else assertTrue(raw().contains(value.getAsString()));
        }
        assertEquals(81, optional);
        assertEquals(Collections.singleton("#mineralogy:sulfur_cube_stones"), members(json("minecraft/tags/" + kind("item") + "/sulfur_cube_archetype/slow_bouncy.json"), false));
    }
    @Test public void noWrongEraTagDirectoriesOrSulfurCubeResources() throws Exception {
        String wrongBlock = kind("block").equals("block") ? "blocks" : "block";
        String wrongItem = kind("item").equals("item") ? "items" : "item";
        assertFalse(Files.exists(ROOT.resolve("mineralogy/tags/" + wrongBlock + "/terrain_rocks.json")));
        assertFalse(Files.exists(ROOT.resolve("mineralogy/tags/" + wrongItem + "/terrain_rocks.json")));
        if (!Arrays.asList("26.2", "26.3").contains(minecraft())) {
            try (java.util.stream.Stream<Path> paths = Files.walk(ROOT)) {
                assertFalse(paths.anyMatch(path -> path.toString().contains("sulfur_cube")));
            }
        }
    }
}
