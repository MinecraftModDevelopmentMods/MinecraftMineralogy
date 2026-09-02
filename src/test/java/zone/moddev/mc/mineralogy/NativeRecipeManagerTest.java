package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Checks generated recipe branches before the loader-side recipe-manager smoke.
 * Forge 52's component and condition registries are deliberately unavailable to
 * a plain JUnit VM, so real codec loading and matching are exercised in Forge.
 */
public class NativeRecipeManagerTest {
    private static final File RECIPE_ROOT = new File("src/main/resources/data/minecraft/recipe");
    private static final File MINERALOGY_RECIPE_ROOT = new File(
            "src/main/resources/data/mineralogy/recipe");

    @Test
    public void enabledBranchesUseExpandedMaterialsForEveryCoveredVanillaRecipe()
            throws Exception {
        for (String name : recipeNames()) {
            JsonObject recipe = selectedRecipe(json(new File(RECIPE_ROOT, name + ".json")), true);
            assertNotNull(name, recipe);
            assertTrue(name, containsIngredient(recipe, enabledMaterial(name)));
            assertEquals(name, expectedOutput(name), recipe.getAsJsonObject("result")
                    .get("id").getAsString());
            assertEquals(name, isTrimTemplateRecipe(name) ? 2 : expectedCount(name),
                    resultCount(recipe));
        }
    }

    @Test
    public void disabledBranchesRetainExactTargetNativeMaterials() throws Exception {
        for (String name : recipeNames()) {
            JsonObject recipe = selectedRecipe(json(new File(RECIPE_ROOT, name + ".json")), false);
            assertNotNull(name, recipe);
            assertTrue(name, containsIngredient(recipe, disabledMaterial(name)));
            assertFalse(name, containsIngredient(recipe, enabledMaterial(name)));
            assertEquals(name, expectedOutput(name), recipe.getAsJsonObject("result")
                    .get("id").getAsString());
        }

        assertCompositeTag("cobblestone_equivalents", "#c:cobblestones");
        assertCompositeTag("stone_crafting_materials", "#minecraft:stone_crafting_materials");
        assertCompositeTag("stone_tool_materials", "#minecraft:stone_tool_materials");
        assertTagContains("data/c/tags/item/cobblestones.json",
                "mineralogy:chert", "mineralogy:pumice");
    }

    @Test
    public void nativeAndMineralogySlabsConvertExactlyOneForOneInBothDirections()
            throws Exception {
        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            assertSlabConversion(family + "_slab_to_vanilla",
                    "mineralogy:" + family + "_slab", "minecraft:" + family + "_slab");
            assertSlabConversion(family + "_slab_from_vanilla",
                    "minecraft:" + family + "_slab", "mineralogy:" + family + "_slab");
            assertSlabConversion(family + "_smooth_slab_to_vanilla",
                    "mineralogy:" + family + "_smooth_slab",
                    "minecraft:polished_" + family + "_slab");
            assertSlabConversion(family + "_smooth_slab_from_vanilla",
                    "minecraft:polished_" + family + "_slab",
                    "mineralogy:" + family + "_smooth_slab");
        }
        assertSlabConversion("tuff_slab_to_vanilla",
                "mineralogy:tuff_slab", "minecraft:tuff_slab");
        assertSlabConversion("tuff_slab_from_vanilla",
                "minecraft:tuff_slab", "mineralogy:tuff_slab");
        assertSlabConversion("tuff_smooth_slab_to_vanilla",
                "mineralogy:tuff_smooth_slab", "minecraft:polished_tuff_slab");
        assertSlabConversion("tuff_smooth_slab_from_vanilla",
                "minecraft:polished_tuff_slab", "mineralogy:tuff_smooth_slab");
        assertSlabConversion("tuff_brick_slab_to_vanilla",
                "mineralogy:tuff_brick_slab", "minecraft:tuff_brick_slab");
        assertSlabConversion("tuff_brick_slab_from_vanilla",
                "minecraft:tuff_brick_slab", "mineralogy:tuff_brick_slab");
    }

    private static void assertSlabConversion(String name, String source, String result)
            throws Exception {
        JsonObject recipe = json(new File(MINERALOGY_RECIPE_ROOT, name + ".json"));
        assertEquals(name, "minecraft:crafting_shapeless", recipe.get("type").getAsString());
        assertEquals(name, 1, recipe.getAsJsonArray("ingredients").size());
        assertEquals(name, source, recipe.getAsJsonArray("ingredients").get(0)
                .getAsJsonObject().get("item").getAsString());
        assertEquals(name, result, recipe.getAsJsonObject("result").get("id").getAsString());
        assertEquals(name, 1, resultCount(recipe));
        JsonObject condition = recipe.getAsJsonObject("forge:condition");
        assertEquals(name, "forge:and", condition.get("type").getAsString());
        assertEquals(name, 2, condition.getAsJsonArray("values").size());
    }

    private static JsonObject selectedRecipe(JsonObject wrapper, boolean enabled) {
        for (JsonElement element : wrapper.getAsJsonArray("recipes")) {
            JsonObject branch = element.getAsJsonObject();
            if (conditionMatches(branch.getAsJsonObject("forge:condition"), enabled)) {
                return branch.getAsJsonObject("recipe");
            }
        }
        return null;
    }

    private static boolean conditionMatches(JsonObject condition, boolean enabled) {
        String type = condition.get("type").getAsString();
        if ("mineralogy:config".equals(type)) {
            assertEquals("COBBLESTONE_EQUIVILENT", condition.get("flag").getAsString());
            return enabled;
        }
        if ("forge:not".equals(type)) {
            return !conditionMatches(condition.getAsJsonObject("value"), enabled);
        }
        throw new AssertionError("Unexpected recipe condition " + type);
    }

    private static String enabledMaterial(String name) {
        if ("furnace".equals(name) || "brewing_stand".equals(name)) {
            return "#mineralogy:stone_crafting_materials";
        }
        if (name.startsWith("stone_") || "stone_sword".equals(name)) {
            return "#mineralogy:stone_tool_materials";
        }
        return "#mineralogy:cobblestone_equivalents";
    }

    private static String disabledMaterial(String name) {
        if ("furnace".equals(name) || "brewing_stand".equals(name)) {
            return "#minecraft:stone_crafting_materials";
        }
        if (name.startsWith("stone_") || "stone_sword".equals(name)) {
            return "#minecraft:stone_tool_materials";
        }
        if (isTrimTemplateRecipe(name)) return "minecraft:cobblestone";
        return "#c:cobblestones";
    }

    private static boolean containsIngredient(JsonElement element, String wanted) {
        if (element == null || element.isJsonNull()) return false;
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (containsIngredient(child, wanted)) return true;
            }
            return false;
        }
        if (!element.isJsonObject()) return false;
        JsonObject object = element.getAsJsonObject();
        if (wanted.startsWith("#") && object.has("tag")
                && wanted.substring(1).equals(object.get("tag").getAsString())) return true;
        if (!wanted.startsWith("#") && object.has("item")
                && wanted.equals(object.get("item").getAsString())) return true;
        for (java.util.Map.Entry<String, JsonElement> child : object.entrySet()) {
            if (containsIngredient(child.getValue(), wanted)) return true;
        }
        return false;
    }

    private static int resultCount(JsonObject recipe) {
        JsonObject result = recipe.getAsJsonObject("result");
        return result.has("count") ? result.get("count").getAsInt() : 1;
    }

    private static int expectedCount(String name) {
        if ("andesite".equals(name) || "diorite".equals(name)) return 2;
        return 1;
    }

    private static String expectedOutput(String name) {
        return name.startsWith("mossy_cobblestone_from_")
                ? "minecraft:mossy_cobblestone" : "minecraft:" + name;
    }

    private static void assertCompositeTag(String name, String base) throws Exception {
        JsonArray values = json(new File("src/main/resources/data/mineralogy/tags/item/"
                + name + ".json")).getAsJsonArray("values");
        assertEquals(name, 28, values.size());
        assertEquals(name, base, values.get(0).getAsString());
        assertEquals(name, new LinkedHashSet<String>(rockFamilies()), familyReferences(values));
    }

    private static Set<String> familyReferences(JsonArray values) {
        Set<String> result = new LinkedHashSet<String>();
        for (int index = 1; index < values.size(); index++) {
            result.add(values.get(index).getAsString().replace("#mineralogy:stones/", ""));
        }
        return result;
    }

    private static void assertTagContains(String relative, String... entries) throws Exception {
        JsonArray values = json(new File("src/main/resources", relative)).getAsJsonArray("values");
        for (String entry : entries) assertTrue(relative + " " + entry, values.toString().contains(entry));
    }

    private static java.util.List<String> rockFamilies() {
        return Arrays.asList("andesite", "basalt", "diorite", "granite", "rhyolite",
                "pegmatite", "diabase", "gabbro", "peridotite", "basaltic_glass",
                "scoria", "tuff", "shale", "conglomerate", "dolomite", "limestone",
                "siltstone", "marble", "slate", "schist", "gneiss", "phyllite",
                "amphibolite", "hornfels", "quartzite", "novaculite", "rock_salt");
    }

    private static String[] recipeNames() {
        return new String[] { "furnace", "brewing_stand", "lever", "piston", "dispenser",
                "dropper", "observer", "mossy_cobblestone_from_vine",
                "mossy_cobblestone_from_moss_block", "andesite", "diorite",
                "stone_axe", "stone_hoe", "stone_pickaxe", "stone_shovel", "stone_sword",
                "coast_armor_trim_smithing_template", "sentry_armor_trim_smithing_template",
                "vex_armor_trim_smithing_template" };
    }

    private static boolean isTrimTemplateRecipe(String name) {
        return name.endsWith("_armor_trim_smithing_template");
    }

    private static JsonObject json(File file) throws Exception {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
