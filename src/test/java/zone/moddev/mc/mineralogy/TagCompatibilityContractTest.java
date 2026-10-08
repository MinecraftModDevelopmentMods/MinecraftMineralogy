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
                String asset = contract.getKey().contains("/tags/block/") ? "blockstates/" : "models/item/";
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
        for (String kind : Arrays.asList("block", "item")) for (String shape : Arrays.asList("slabs", "stairs", "walls")) {
            JsonObject tag = read(DATA.resolve("minecraft/tags/" + kind + "/" + shape + ".json"));
            Set<String> members = ids(tag);
            assertEquals(108, members.size());
            assertTrue(members.stream().noneMatch(id -> id.contains("double_slab")));
            assertTrue(tag.getAsJsonArray("values").asList().stream().allMatch(JsonElement::isJsonObject));
        }
    }
    @Test public void naturalRawAndHornPoliciesExcludeSoftAndCraftedForms() throws Exception {
        Set<String> raw = ids(read(DATA.resolve("mineralogy/tags/block/raw_stones.json")));
        assertEquals(36, raw.size());
        assertFalse(raw.contains("minecraft:sandstone"));
        assertFalse(raw.stream().anyMatch(id -> id.endsWith("_smooth") || id.endsWith("_brick")));
        Set<String> horns = ids(read(DATA.resolve("mineralogy/tags/block/horn_breaking_rocks.json")));
        assertEquals(30, horns.size());
        assertTrue(horns.contains("mineralogy:chert"));
        for (String excluded : Arrays.asList("rock_salt", "scoria", "siltstone", "chalk", "gypsum", "pumice", "basalt_brick"))
            assertFalse(horns.contains("mineralogy:" + excluded));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/block/base_stone_overworld.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/block/azalea_grows_on.json")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/block/height_specific_ore_replaceables.json")));
    }
    @Test public void furnaceWorkstationsAndCatTargetsUseTheCorrectRegistryForms() throws Exception {
        assertEquals(216, ids(read(DATA.resolve("c/tags/block/player_workstations/furnaces.json"))).size());
        Set<String> items = ids(read(DATA.resolve("c/tags/item/player_workstations/furnaces.json")));
        assertEquals(108, items.size());
        assertTrue(items.stream().noneMatch(id -> id.startsWith("mineralogy:lit_")));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/block/cats_can_sit_on.json")));
    }
    @Test public void miningTotalsAndNormalCobblestoneRemainExact() throws Exception {
        assertEquals(1133, ids(read(DATA.resolve("minecraft/tags/block/mineable/pickaxe.json"))).size());
        assertEquals(123, ids(read(DATA.resolve("minecraft/tags/block/needs_iron_tool.json"))).size());
        assertEquals(329, ids(read(DATA.resolve("minecraft/tags/block/needs_stone_tool.json"))).size());
        for (String kind : Arrays.asList("block", "item"))
            assertEquals(new TreeSet<>(Arrays.asList("mineralogy:chert", "mineralogy:pumice")),
                    ids(read(DATA.resolve("c/tags/" + kind + "/cobblestones/normal.json"))));
    }
    @Test public void dripstoneHooksAreLimitedToTheTwoProvenConsumers() throws Exception {
        JsonArray configured = read(Paths.get("src/main/resources/mineralogy.mixins.json")).getAsJsonArray("mixins");
        for (String name : Arrays.asList("LargeDripstoneBoundaryMixin", "DripstoneWaterPocketMixin")) {
            assertEquals(name, 1, configured.asList().stream().filter(value -> name.equals(value.getAsString())).count());
            String source = Files.readString(Paths.get("src/main/java/zone/moddev/mc/mineralogy/mixin/" + name + ".java"));
            assertTrue(source, source.contains("require = 1, expect = 1, allow = 1"));
            assertTrue(source, source.contains("\"mineralogy\", \"dripstone_base_stones\""));
            assertFalse(source, source.contains("net.minecraftforge"));
        }
        assertEquals(new TreeSet<>(Arrays.asList("#minecraft:base_stone_overworld", "#mineralogy:terrain_rocks")),
                ids(read(DATA.resolve("mineralogy/tags/block/dripstone_base_stones.json"))));
        assertFalse(Files.exists(DATA.resolve("minecraft/tags/block/base_stone_overworld.json")));
    }
    @Test public void disabledConstructionDoesNotLeaveRequiredLootItemReferences() throws Exception {
        int guarded = 0;
        try (java.util.stream.Stream<Path> paths = Files.list(DATA.resolve("mineralogy/loot_table/blocks"))) {
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
        try (java.util.stream.Stream<Path> paths = Files.list(DATA.resolve("mineralogy/loot_table/blocks"))) {
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
            try (java.util.stream.Stream<Path> paths = Files.list(DATA.resolve(namespace + "/recipe"))) {
                for (Path path : (Iterable<Path>)paths::iterator) {
                    JsonObject recipe = read(path);
                    Set<String> required = new TreeSet<>();
                    optionalReferences(recipe, optional, required);
                    Path advancePath = DATA.resolve(namespace + "/advancement/recipes/" + (namespace.equals("minecraft") ? "building_blocks/" : "") + path.getFileName());
                    JsonObject advance = Files.exists(advancePath) ? read(advancePath) : null;
                    if (advance != null) optionalReferences(advance, optional, required);
                    if (required.isEmpty()) continue;
                    Set<String> actual = new TreeSet<>();
                    assertTrue(path.toString(), recipe.has("neoforge:conditions"));
                    for (JsonElement condition : recipe.getAsJsonArray("neoforge:conditions")) {
                        JsonObject value = condition.getAsJsonObject();
                        if (!"neoforge:registered".equals(value.get("type").getAsString())) continue;
                        assertEquals("minecraft:item", value.get("registry").getAsString());
                        assertTrue(path.toString(), actual.add(value.get("value").getAsString()));
                    }
                    assertEquals(path.toString(), required, actual);
                    if (advance != null) assertEquals(path.toString(), recipe.get("neoforge:conditions"), advance.get("neoforge:conditions"));
                    guarded++;
                }
            }
        }
        assertEquals(1365, guarded);
    }
    @Test public void recipeGeneratorKeepsConstructionFamilyMembersOptional() throws Exception {
        String generator = Files.readString(Paths.get("scripts/generate-recipes.ps1"));
        assertTrue(generator.contains("if ($kind -eq 'slabs' -or $finish)"));
        assertTrue(generator.contains("@([ordered]@{ id = $item; required = $false })"));
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
