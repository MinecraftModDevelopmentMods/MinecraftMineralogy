package zone.moddev.mc.mineralogy;

import com.google.gson.*;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

/** The focused, shipping contract is separate from the local full tag index. */
public class TagCompatibilityContractTest {
    private static final Path DATA = Paths.get("src/main/resources/data");
    private static JsonObject read(Path file) throws Exception {
        return JsonParser.parseString(new String(Files.readAllBytes(file), StandardCharsets.UTF_8)).getAsJsonObject();
    }
    private static Set<String> ids(JsonObject tag) {
        Set<String> result = new TreeSet<>();
        assertFalse("Unsupported optional array", tag.has("optional"));
        for (JsonElement value : tag.getAsJsonArray("values")) {
            String id;
            if (value.isJsonObject()) {
                JsonObject object = value.getAsJsonObject();
                assertEquals(new HashSet<>(Arrays.asList("id", "required")), object.keySet());
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
            assertTrue(tag.getAsJsonArray("values").asList().stream().allMatch(JsonElement::isJsonObject));
        }
    }
    @Test public void naturalRawAndHornPoliciesExcludeSoftAndCraftedForms() throws Exception {
        Set<String> raw = ids(read(DATA.resolve("mineralogy/tags/blocks/raw_stones.json")));
        assertEquals(36, raw.size());
        assertFalse(raw.contains("minecraft:sandstone"));
        assertFalse(raw.stream().anyMatch(id -> id.endsWith("_smooth") || id.endsWith("_brick")));
        Set<String> horns = ids(read(DATA.resolve("mineralogy/tags/blocks/horn_breaking_rocks.json")));
        assertEquals(30, horns.size());
        assertTrue(horns.contains("mineralogy:chert"));
        for (String excluded : Arrays.asList("rock_salt", "scoria", "siltstone", "chalk", "gypsum", "pumice", "basalt_brick"))
            assertFalse(horns.contains("mineralogy:" + excluded));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/base_stone_overworld.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/azalea_grows_on.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/height_specific_ore_replaceables.json")));
    }
    @Test public void furnaceWorkstationsAndCatTargetsUseTheCorrectRegistryForms() throws Exception {
        assertEquals(216, ids(read(DATA.resolve("c/tags/blocks/player_workstations/furnaces.json"))).size());
        Set<String> items = ids(read(DATA.resolve("c/tags/items/player_workstations/furnaces.json")));
        assertEquals(108, items.size());
        assertTrue(items.stream().noneMatch(id -> id.startsWith("mineralogy:lit_")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/cats_can_sit_on.json")));
    }
    @Test public void miningTotalsAndNormalCobblestoneRemainExact() throws Exception {
        assertEquals(1133, ids(read(DATA.resolve("minecraft/tags/blocks/mineable/pickaxe.json"))).size());
        assertEquals(123, ids(read(DATA.resolve("minecraft/tags/blocks/needs_iron_tool.json"))).size());
        assertEquals(329, ids(read(DATA.resolve("minecraft/tags/blocks/needs_stone_tool.json"))).size());
        for (String kind : Arrays.asList("blocks", "items"))
            assertEquals(new TreeSet<>(Arrays.asList("mineralogy:chert", "mineralogy:pumice")),
                    ids(read(DATA.resolve("c/tags/" + kind + "/cobblestones/normal.json"))));
    }
    @Test public void dripstoneHooksAreLimitedToTheTwoProvenConsumers() throws Exception {
        JsonObject configured = read(Paths.get("src/main/resources/META-INF/coremods.json"));
        assertEquals("coremods/mineralogy_terrain_compatibility.js", configured.get("mineralogy_terrain_compatibility").getAsString());
        String source = Files.readString(Paths.get("src/main/resources/coremods/mineralogy_terrain_compatibility.js"));
        assertTrue(source.contains("if (hooks !== 1) throw new Error"));
        assertTrue(source.contains("DripstoneClusterFeature"));
        assertTrue(source.contains("LargeDripstoneFeature$LargeDripstone"));
        assertTrue(source.contains("net.neoforged.coremod.api.ASMAPI"));
        assertFalse(source.contains("net.minecraftforge"));
        assertEquals(new TreeSet<>(Arrays.asList("#minecraft:base_stone_overworld", "#mineralogy:terrain_rocks")),
                ids(read(DATA.resolve("mineralogy/tags/blocks/dripstone_base_stones.json"))));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/base_stone_overworld.json")));
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
                        assertTrue(path.toString(), loot.has("neoforge:conditions"));
                        List<String> flags = new ArrayList<>();
                        for (JsonElement entryCondition : loot.getAsJsonArray("neoforge:conditions")) {
                            JsonObject condition = entryCondition.getAsJsonObject();
                            assertEquals(new HashSet<>(Arrays.asList("type", "flag")), condition.keySet());
                            assertEquals("mineralogy:config", condition.get("type").getAsString());
                            flags.add(condition.get("flag").getAsString());
                        }
                        assertEquals(id, constructionFlags(id), flags);
                        guarded++;
                    }
                }
            }
        }
        assertEquals(864, guarded);
    }
    @Test public void forestRockHookPreservesNativeChecksWithoutGlobalStoneExpansion() throws Exception {
        assertEquals(Collections.singleton("#mineralogy:terrain_rocks"),
                ids(read(DATA.resolve("mineralogy/tags/blocks/forest_rock_substrates.json"))));
        String transformer = Files.readString(Paths.get("src/main/resources/coremods/mineralogy_terrain_compatibility.js"));
        assertTrue(transformer.contains("BlockBlobFeature"));
        assertTrue(transformer.contains("instruction.name === 'isStone'"));
        assertTrue(transformer.contains("if (hooks !== 1) throw new Error('Mineralogy forest-rock hook drift"));
        String source = Files.readString(Paths.get("src/main/java/zone/moddev/mc/mineralogy/compat/VanillaTerrainHooks.java"));
        assertTrue(source.contains("state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(FOREST_ROCK_SUBSTRATES)"));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/blocks/base_stone_overworld.json")));
        String generator = Files.readString(Paths.get("scripts/generate-recipes.ps1"));
        assertTrue(generator.contains("@([ordered]@{ id = $item; required = $false })"));
    }
    private static List<String> constructionFlags(String id) {
        if (id.contains("_relief_")) return Arrays.asList("GENERATE_SMOOTH", "GENERATE_RELIEFS");
        String prefix = id.contains("_smooth_brick") ? "SMOOTHBRICK"
                : id.contains("_smooth") ? "SMOOTH" : id.contains("_brick") ? "BRICK" : "ROCK";
        List<String> flags = new ArrayList<>();
        if (prefix.startsWith("SMOOTH")) flags.add("GENERATE_SMOOTH");
        if (prefix.equals("SMOOTHBRICK")) flags.add("GENERATE_SMOOTHBRICK");
        if (prefix.equals("BRICK")) flags.add("GENERATE_BRICK");
        if (id.endsWith("_furnace")) { flags.add("GENERATE_" + prefix + "SLAB"); flags.add("GENERATE_" + prefix + "FURNACE"); }
        else if (id.endsWith("_slab")) flags.add("GENERATE_" + prefix + "SLAB");
        else if (id.endsWith("_stairs")) flags.add("GENERATE_" + prefix + "STAIRS");
        else if (id.endsWith("_wall")) flags.add("GENERATE_" + prefix + "WALL");
        assertFalse(id, flags.isEmpty());
        return flags;
    }
    @Test public void disabledConstructionGuardsRecipesAndTheirAdvancements() throws Exception {
        Set<String> optional = new TreeSet<>();
        try (java.util.stream.Stream<Path> paths = Files.list(DATA.resolve("mineralogy/loot_tables/blocks"))) {
            for (Path path : (Iterable<Path>)paths::iterator) {
                for (JsonElement pool : read(path).getAsJsonArray("pools")) {
                    for (JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
                        String id = entry.getAsJsonObject().get("name").getAsString();
                        if (id.matches("mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)")) optional.add(id);
                    }
                }
            }
        }
        assertEquals(864, optional.size());
        int guarded = 0;
        for (String namespace : Arrays.asList("mineralogy", "minecraft")) {
            try (java.util.stream.Stream<Path> paths = Files.list(DATA.resolve(namespace + "/recipes"))) {
                for (Path path : (Iterable<Path>)paths::iterator) {
                    JsonObject recipe = read(path);
                    Set<String> required = new TreeSet<>();
                    optionalReferences(recipe, optional, required);
                    Path advancePath = DATA.resolve(namespace + "/advancements/recipes/" + (namespace.equals("minecraft") ? "building_blocks/" : "") + path.getFileName());
                    JsonObject advance = Files.exists(advancePath) ? read(advancePath) : null;
                    if (advance != null) optionalReferences(advance, optional, required);
                    if (required.isEmpty()) continue;
                    Set<String> actual = new TreeSet<>();
                    assertTrue(path.toString(), recipe.has("neoforge:conditions"));
                    for (JsonElement condition : recipe.getAsJsonArray("neoforge:conditions")) {
                        JsonObject value = condition.getAsJsonObject();
                        if (!"neoforge:item_exists".equals(value.get("type").getAsString())) continue;
                        assertEquals(new HashSet<>(Arrays.asList("type", "item")), value.keySet());
                        assertTrue(path.toString(), actual.add(value.get("item").getAsString()));
                    }
                    assertEquals(path.toString(), required, actual);
                    if (advance != null) assertEquals(path.toString(), recipe.get("neoforge:conditions"), advance.get("neoforge:conditions"));
                    guarded++;
                }
            }
        }
        assertEquals(1350, guarded);
    }
    private static void optionalReferences(JsonElement node, Set<String> optional, Set<String> found) {
        if (node.isJsonPrimitive() && node.getAsJsonPrimitive().isString()) {
            String value = node.getAsString();
            if (optional.contains(value)) found.add(value);
        } else if (node.isJsonArray()) {
            for (JsonElement child : node.getAsJsonArray()) optionalReferences(child, optional, found);
        } else if (node.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet())
                if (!entry.getKey().equals("neoforge:conditions")) optionalReferences(entry.getValue(), optional, found);
        }
    }
}
