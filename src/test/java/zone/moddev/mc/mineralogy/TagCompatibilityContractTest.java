package zone.moddev.mc.mineralogy;

import com.google.gson.*;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

/** The focused, shipping contract is separate from the local full tag index. */
public class TagCompatibilityContractTest {
    @Test public void isolatedProbePackUsesTheTargetFormat() throws Exception {
        assertEquals(6, read(Paths.get("src/terrainCompatibilityTest/resources/pack.mcmeta"))
                .getAsJsonObject("pack").get("pack_format").getAsInt());
    }
    private static Set<String> keys(JsonObject value) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : value.entrySet()) result.add(entry.getKey());
        return result;
    }
    private static final Path DATA = Paths.get("src/main/resources/data");
    private static JsonObject read(Path file) throws Exception {
        return (new JsonParser()).parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8)).getAsJsonObject();
    }
    private static Set<String> ids(JsonObject tag) {
        Set<String> result = new TreeSet<>();
        assertFalse("Unsupported optional array", tag.has("optional"));
        for (JsonElement value : tag.getAsJsonArray("values")) {
            String id;
            if (value.isJsonObject()) {
                JsonObject object = value.getAsJsonObject();
                assertEquals(new HashSet<>(Arrays.asList("id", "required")), keys(object));
                assertFalse(object.get("required").getAsBoolean());
                id = object.get("id").getAsString();
            } else { id = value.getAsString(); }
            assertTrue(id, id.matches("#?[a-z0-9_.-]+:[a-z0-9_/.-]+"));
            if (id.matches("mineralogy:(lit_)?(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)"))
                assertTrue("Required config-dependent construction entry " + id, value.isJsonObject());
            assertTrue("Duplicate " + id, result.add(id));
        }
        return result;
    }
    @Test public void generatedContractsMatchAndEveryTagHasValidMembers() throws Exception {
        JsonObject contracts = read(Paths.get("src/test/resources/tag-compatibility-contract.json"));
        for (Map.Entry<String, JsonElement> contract : contracts.getAsJsonObject("tags").entrySet()) {
            JsonObject actual = read(DATA.resolve(contract.getKey()));
            assertEquals(contract.getKey(), contract.getValue(), actual.get("values"));
            assertFalse(actual.get("replace").getAsBoolean());
            for (String id : ids(actual)) if (id.startsWith("mineralogy:")) {
                String asset = contract.getKey().contains("/tags/blocks/") ? "blockstates/" : "models/item/";
                assertTrue("Nonexistent registry form " + contract.getKey() + " " + id,
                        Files.exists(Paths.get("src/main/resources/assets/mineralogy/" + asset + id.substring(11) + ".json")));
            }
        }
        try (java.util.stream.Stream<Path> paths = Files.walk(DATA)) {
            for (Path path : (Iterable<Path>)paths.filter(p -> p.toString().contains("tags") && p.toString().endsWith(".json"))::iterator)
                ids(read(path));
        }
    }
    @Test public void shapesExcludeDoubleFullBlocksAndRemainOptional() throws Exception {
        for (String kind : Arrays.asList("blocks", "items")) for (String shape : Arrays.asList("slabs", "stairs", "walls")) {
            JsonObject tag = read(DATA.resolve("minecraft/tags/" + kind + "/" + shape + ".json"));
            Set<String> members = ids(tag);
            assertEquals(108, members.size());
            assertTrue(members.stream().noneMatch(id -> id.contains("double_slab")));
            for (JsonElement member : tag.getAsJsonArray("values")) assertTrue(member.isJsonObject());
        }
    }
    @Test public void naturalRawAndHornPoliciesExcludeSoftAndCraftedForms() throws Exception {
        Set<String> raw = ids(read(DATA.resolve("mineralogy/tags/blocks/raw_stones.json")));
        assertEquals(35, raw.size());
        assertFalse(raw.contains("minecraft:sandstone"));
        assertFalse(raw.stream().anyMatch(id -> id.endsWith("_smooth") || id.endsWith("_brick")));
        assertFalse(Files.exists(DATA.resolve("mineralogy/tags/blocks/horn_breaking_rocks.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/snaps_goat_horn.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/base_stone_overworld.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/azalea_grows_on.json")));
        // Moss, dripstone and azalea systems do not exist in Minecraft 1.16.
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/azalea_root_replaceable.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/height_specific_ore_replaceables.json")));
    }
    @Test public void unavailableWorkstationAndCatTagsAreNotInvented() throws Exception {
        assertFalse(Files.exists(DATA.resolve("c/tags/blocks/player_workstations/furnaces.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/cats_can_sit_on.json")));
    }
    @Test public void commonAggregatesRetainTheirDedicatedChildTags() throws Exception {
        for (String kind : Arrays.asList("blocks", "items")) {
            assertEquals(new TreeSet<>(Arrays.asList("#forge:ores/nitrate", "#forge:ores/phosphorous", "#forge:ores/sulfur")),
                    ids(read(DATA.resolve("forge/tags/" + kind + "/ores.json"))));
            assertEquals(new TreeSet<>(Arrays.asList("#forge:storage_blocks/chalk", "#forge:storage_blocks/gypsum",
                    "#forge:storage_blocks/nitrate", "#forge:storage_blocks/phosphorous", "#forge:storage_blocks/rock_salt", "#forge:storage_blocks/sulfur")),
                    ids(read(DATA.resolve("forge/tags/" + kind + "/storage_blocks.json"))));
            assertFalse(Files.exists(DATA.resolve("forge/tags/" + kind + "/dyed.json")));
        }
    }
    @Test public void miningTotalsAndNormalCobblestoneRemainExact() throws Exception {
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/mineable/pickaxe.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/needs_iron_tool.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/needs_stone_tool.json")));
        for (String kind : Arrays.asList("blocks", "items"))
            assertEquals(new TreeSet<>(Arrays.asList("minecraft:cobblestone", "mineralogy:chert", "mineralogy:pumice")),
                    ids(read(DATA.resolve("forge/tags/" + kind + "/cobblestone/normal.json"))));
    }
    @Test public void bytecodeHooksAreScopedAndRejectTargetDrift() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get("src/main/resources/coremods/mineralogy_terrain_compatibility.js")), StandardCharsets.UTF_8);
        assertTrue(source.contains("if (hooks !== 1) throw new Error"));
        assertFalse(source.contains("DripstoneClusterFeature"));
        assertFalse(source.contains("LargeDripstoneFeature"));
        assertTrue(source.contains("net.minecraftforge.common.ForgeHooks"));
        assertTrue(source.contains("method.name !== 'loadLootTable'"));
        assertFalse(source.contains("WorldCarver"));
        assertFalse(source.contains("BlockTags.BASE_STONE_OVERWORLD ="));
    }
    @Test public void disabledConstructionDoesNotLeaveRequiredLootItemReferences() throws Exception {
        int guarded = 0;
        try (java.util.stream.Stream<Path> paths = Files.list(DATA.resolve("mineralogy/loot_tables/blocks"))) {
            for (Path path : (Iterable<Path>)paths::iterator) {
                JsonObject loot = read(path);
                for (JsonElement pool : loot.getAsJsonArray("pools")) {
                    for (JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
                        JsonObject value = entry.getAsJsonObject();
                        if (!"minecraft:item".equals(value.get("type").getAsString())) continue;
                        String id = value.get("name").getAsString();
                        if (!id.matches("mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)")) continue;
                        assertFalse(path.toString(), loot.has("forge:condition"));
                        assertFalse(path.toString(), pool.getAsJsonObject().has("forge:condition"));
                        assertEquals(id, loot.get("mineralogy:construction_item").getAsString());
                        guarded++;
                    }
                }
            }
        }
        assertEquals(864, guarded);
    }
}
