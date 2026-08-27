package zone.moddev.mc.mineralogy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ResourceContractTest {
    private static final File ROOT = new File("src/main/resources");

    @Test
    public void providerUsesOreSpawnFourAndOwnsNoVanillaGeneration() throws Exception {
        JsonObject provider = json(new File(ROOT, "data/mineralogy/orespawn/provider.json"));
        assertEquals(4, provider.get("schema_version").getAsInt());
        assertEquals(3, provider.get("provider_revision").getAsInt());
        assertEquals("mineralogy", provider.get("provider_modid").getAsString());
        assertEquals(32, provider.getAsJsonObject("rocks").size());
        assertEquals(3, provider.getAsJsonObject("ores").size());
        assertEquals(1, provider.getAsJsonObject("fluid_deposits").size());
        assertFalse(provider.getAsJsonObject("profile_defaults").get("manage_vanilla_ores").getAsBoolean());
        JsonObject dimensions = provider.getAsJsonObject("profile_defaults").getAsJsonObject("terrain_dimensions");
        assertEquals(1, dimensions.size());
        assertTrue(dimensions.has("minecraft:overworld"));
        String text = new String(Files.readAllBytes(new File(ROOT,
                "data/mineralogy/orespawn/provider.json").toPath()), StandardCharsets.UTF_8);
        assertTrue(text.contains("\"minecraft:andesite\""));
        assertTrue(text.contains("\"minecraft:diorite\""));
        assertTrue(text.contains("\"minecraft:granite\""));
        JsonObject defaults = provider.getAsJsonObject("profile_defaults");
        assertEquals("minecraft:basalt", defaults.getAsJsonObject("worldgen_aliases")
                .get("mineralogy:basalt").getAsString());
        assertEquals("minecraft:basalt", provider.getAsJsonObject("rocks")
                .getAsJsonObject("mineralogy:rock/minecraft/basalt").get("block").getAsString());
        assertEquals("mineralogy:tuff", provider.getAsJsonObject("rocks")
                .getAsJsonObject("mineralogy:rock/minecraft/tuff").get("block").getAsString());
        assertFalse(text.contains("metadata"));
        assertFalse(text.contains("deepslate"));
    }

    @Test
    public void everyRecipeHasAProgressiveAdvancement() throws Exception {
        File recipeDir = new File(ROOT, "data/mineralogy/recipes");
        File advancementDir = new File(ROOT, "data/mineralogy/advancements/recipes");
        File[] recipes = recipeDir.listFiles((dir, name) -> name.endsWith(".json"));
        File[] advancements = advancementDir.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertNotNull(advancements);
        assertEquals(1415, recipes.length);
        assertEquals(1415, advancements.length);

        Set<String> advancementNames = new HashSet<String>();
        for (File file : advancements) advancementNames.add(file.getName());
        for (File recipeFile : recipes) {
            JsonObject recipe = json(recipeFile);
            assertFalse(recipeFile.getName(), recipe.get("type").getAsString().startsWith("forge:ore_"));
            assertTrue(recipeFile.getName(), advancementNames.contains(recipeFile.getName()));
            JsonObject advancement = json(new File(advancementDir, recipeFile.getName()));
            assertEquals("mineralogy:" + stripJson(recipeFile.getName()),
                    advancement.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
            assertTrue(advancement.getAsJsonObject("criteria").has("has_the_recipe"));
            assertTrue(advancement.getAsJsonObject("criteria").has("has_rock"));
            if (recipe.has("conditions")) {
                assertTrue(recipeFile.getName(), recipe.get("conditions").isJsonArray());
                assertTrue(recipeFile.getName(), advancement.has("conditions"));
                assertEquals(recipeFile.getName(), recipe.get("conditions"), advancement.get("conditions"));
            } else {
                assertFalse(recipeFile.getName(), advancement.has("conditions"));
            }
        }

        JsonObject smooth = json(new File(advancementDir, "basalt_smooth.json"));
        assertEquals("mineralogy:basalt", criterionItem(smooth, "has_rock"));
        assertTrue(smooth.getAsJsonObject("criteria").has("has_sand"));
        JsonObject stairs = json(new File(advancementDir, "basalt_smooth_stairs.json"));
        assertEquals("mineralogy:basalt_smooth", criterionItem(stairs, "has_rock"));
        JsonObject blank = json(new File(advancementDir, "basalt_relief_blank.json"));
        assertEquals("mineralogy:basalt_smooth", criterionItem(blank, "has_rock"));
        JsonObject marked = json(new File(advancementDir, "basalt_relief_pickaxe.json"));
        assertEquals("mineralogy:basalt_relief_blank", criterionItem(marked, "has_rock"));
        JsonObject right = json(new File(advancementDir, "basalt_relief_right.json"));
        assertEquals("mineralogy:basalt_relief_left", criterionItem(right, "has_rock"));
    }

    @Test
    public void rockFurnacesUnlockFromTheirMatchingSlabs() throws Exception {
        File advancementDir = new File(ROOT, "data/mineralogy/advancements/recipes");
        File[] furnaces = advancementDir.listFiles((dir, name) -> name.endsWith("_furnace.json"));
        assertNotNull(furnaces);
        assertEquals(108, furnaces.length);

        for (File file : furnaces) {
            JsonObject advancement = json(file);
            JsonObject criteria = advancement.getAsJsonObject("criteria");
            assertTrue(file.getName(), criteria.has("has_rock"));
            assertFalse(file.getName(), criteria.has("has_furnace"));
            String expectedSlab = "mineralogy:"
                    + stripJson(file.getName()).replaceFirst("_furnace$", "_slab");
            assertEquals(file.getName(), expectedSlab, criterionItem(advancement, "has_rock"));

            JsonArray requirements = advancement.getAsJsonArray("requirements");
            assertEquals(file.getName(), 1, requirements.size());
            JsonArray unlockAlternatives = requirements.get(0).getAsJsonArray();
            assertEquals(file.getName(), 2, unlockAlternatives.size());
            assertEquals(file.getName(), "has_the_recipe", unlockAlternatives.get(0).getAsString());
            assertEquals(file.getName(), "has_rock", unlockAlternatives.get(1).getAsString());
        }
    }

    @Test
    public void acceptedRecipeDetailsArePresent() throws Exception {
        File recipes = new File(ROOT, "data/mineralogy/recipes");
        assertEquals(4, json(new File(recipes, "gypsum_dust.json"))
                .getAsJsonObject("result").get("count").getAsInt());
        assertEquals(2, json(new File(recipes, "gypsum.json")).getAsJsonArray("pattern").size());
        assertEquals("minecraft:charcoal", json(new File(recipes, "gunpowder_from_charcoal.json"))
                .getAsJsonArray("ingredients").get(0).getAsJsonObject().get("item").getAsString());
        assertFalse(new File(recipes, "gunpowder_from_coal.json").exists());
        assertGunpowderRecipe(recipes, "gunpowder_from_sugar", null);
        assertGunpowderRecipe(recipes, "gunpowder_from_charcoal", null);
        assertGunpowderRecipe(recipes, "gunpowder_from_carbon_dust", "forge:dusts/carbon");
        assertGunpowderRecipe(recipes, "gunpowder_from_coal_dust", "forge:dusts/coal");
        assertEquals("mineralogy:basalt", json(new File(recipes, "basalt_slab.json"))
                .getAsJsonObject("key").getAsJsonObject("x").get("item").getAsString());
        assertEquals("mineralogy:basalt", json(new File(recipes, "basalt_raw_slab_recombination.json"))
                .getAsJsonObject("result").get("item").getAsString());
        assertEquals("forge:sand", json(new File(recipes, "basalt_brick_block_polishing.json"))
                .getAsJsonArray("ingredients").get(1).getAsJsonObject().get("tag").getAsString());
        JsonObject furnace = json(new File(recipes, "basalt_furnace.json"));
        assertEquals("mineralogy:slabs/basalt", furnace.getAsJsonObject("key")
                .getAsJsonObject("x").get("tag").getAsString());
        assertEquals("minecraft:furnace", furnace.getAsJsonObject("key")
                .getAsJsonObject("y").get("item").getAsString());
        assertFalse(new File(recipes, "basalt_furnace_from_rock.json").exists());
        JsonObject vanillaFurnace = json(new File(ROOT, "data/minecraft/recipes/furnace.json"));
        assertEquals("forge:cobblestone", vanillaFurnace.getAsJsonObject("key")
                .getAsJsonObject("#").get("tag").getAsString());
        assertFalse(new File(ROOT, "data/minecraft/recipes/stone_pickaxe.json").exists());
        assertEquals("#forge:cobblestone", json(new File(ROOT,
                "data/minecraft/tags/items/stone_crafting_materials.json"))
                .getAsJsonArray("values").get(0).getAsString());
        assertEquals("#forge:cobblestone", json(new File(ROOT,
                "data/minecraft/tags/items/stone_tool_materials.json"))
                .getAsJsonArray("values").get(0).getAsString());
        assertFalse(new File(ROOT, "data/mineralogy/tags/items/vanilla_furnace_materials.json").exists());
        assertEquals("minecraft:red_dye", json(new File(recipes, "drywall_red.json"))
                .getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString());
        assertEquals("minecraft:green_dye", json(new File(recipes, "drywall_green.json"))
                .getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString());
        assertEquals("minecraft:yellow_dye", json(new File(recipes, "drywall_yellow.json"))
                .getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString());
    }

    @Test
    public void allSeventeenLocalesHaveOrdered938KeyParity() throws Exception {
        File directory = new File(ROOT, "assets/mineralogy/lang");
        Set<String> expectedFiles = new HashSet<String>(Arrays.asList(
                "de_au.json", "de_de.json", "en_ca.json", "en_en.json", "en_gb.json",
                "en_pt.json", "en_us.json", "es_es.json", "es_mx.json", "fr_ca.json",
                "fr_fr.json", "ja_jp.json", "ko_kr.json", "pt_br.json", "pt_pt.json",
                "ru_ru.json", "zh_cn.json"));
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);
        Set<String> actual = new HashSet<String>();
        for (File file : files) actual.add(file.getName());
        assertEquals(expectedFiles, actual);

        List<String> keys = keys(json(new File(directory, "en_us.json")));
        assertEquals(938, keys.size());
        for (File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            assertFalse(file.getName(), bytes.length >= 3 && bytes[0] == (byte) 0xef
                    && bytes[1] == (byte) 0xbb && bytes[2] == (byte) 0xbf);
            String text = new String(bytes, StandardCharsets.UTF_8);
            assertFalse(file.getName(), text.contains("\ufffd"));
            assertTrue(file.getName(), text.endsWith("\n"));
            assertEquals(file.getName(), keys, keys(json(file)));
        }
        assertPair(directory, "de_au.json", "de_de.json");
        assertPair(directory, "es_es.json", "es_mx.json");
        assertPair(directory, "fr_ca.json", "fr_fr.json");
        assertPair(directory, "pt_br.json", "pt_pt.json");
        JsonObject japanese = json(new File(directory, "ja_jp.json"));
        assertEquals("原油入りバケツ", japanese.get("item.mineralogy.crude_oil_bucket").getAsString());
        JsonObject russian = json(new File(directory, "ru_ru.json"));
        assertEquals("Ведро сырой нефти", russian.get("item.mineralogy.crude_oil_bucket").getAsString());
    }

    @Test
    public void forge36ResourcesRetainNativeWallsTagsAndPackFormat() throws Exception {
        JsonObject pack = json(new File(ROOT, "pack.mcmeta"));
        assertEquals(6, pack.getAsJsonObject("pack").get("pack_format").getAsInt());

        File blockstates = new File(ROOT, "assets/mineralogy/blockstates");
        File[] wallStates = blockstates.listFiles((dir, name) -> name.endsWith("_wall.json"));
        assertNotNull(wallStates);
        assertEquals(108, wallStates.length);
        for (File state : wallStates) {
            assertTrue(state.getName(), new String(Files.readAllBytes(state.toPath()), StandardCharsets.UTF_8)
                    .contains("_wall_side_tall"));
        }
        File models = new File(ROOT, "assets/mineralogy/models/block");
        File[] tallSides = models.listFiles((dir, name) -> name.endsWith("_wall_side_tall.json"));
        assertNotNull(tallSides);
        assertEquals(108, tallSides.length);

        assertTrue(new File(ROOT, "data/minecraft/tags/blocks/walls.json").isFile());
        assertTrue(new File(ROOT, "data/minecraft/tags/items/walls.json").isFile());
        File minecraftRecipes = new File(ROOT, "data/minecraft/recipes");
        File[] overrides = minecraftRecipes.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(overrides);
        assertEquals(1, overrides.length);
        assertEquals("furnace.json", overrides[0].getName());
        File[] stonecutting = new File(ROOT, "data/mineralogy/recipes")
                .listFiles((dir, name) -> name.contains("stonecutting"));
        assertNotNull(stonecutting);
        assertEquals(0, stonecutting.length);
    }

    @Test
    public void oilAndBuildMetadataUseStableTargetIdentities() throws Exception {
        String properties = new String(Files.readAllBytes(new File("gradle.properties").toPath()), StandardCharsets.UTF_8);
        assertTrue(properties.contains("mod_version=6.0.1.116051"));
        assertTrue(properties.contains("orespawn_curse_file_id=8742102"));
        String build = new String(Files.readAllBytes(new File("build.gradle").toPath()), StandardCharsets.UTF_8);
        assertTrue(build.contains("runtimeOnly renamer.dependency(\"curse.maven:mmd-orespawn-"));
        assertTrue(build.contains("orespawnRelease"));
        String metadata = new String(Files.readAllBytes(new File(ROOT, "META-INF/mods.toml").toPath()), StandardCharsets.UTF_8);
        assertTrue(metadata.contains("loaderVersion=\"[36,)\""));
        assertTrue(metadata.contains("versionRange=\"[36.2.34,37)\""));
        assertTrue(metadata.contains("versionRange=\"[4.0.6,5.0.0)\""));
        assertTrue(metadata.contains("ordering=\"AFTER\""));
        assertTrue(new File(ROOT, "assets/mineralogy/textures/items/crude_oil_bucket.png").isFile());
        assertTrue(new File(ROOT, "assets/mineralogy/textures/blocks/crude_oil_still.png").isFile());
        String fluidSource = new String(Files.readAllBytes(new File(
                "src/main/java/zone/moddev/mc/mineralogy/fluids/MineralogyFluids.java").toPath()), StandardCharsets.UTF_8);
        assertTrue(fluidSource.contains("\"flowing_crude_oil\""));
        assertTrue(fluidSource.contains("\"crude_oil_bucket\""));
        assertFalse(fluidSource.contains("new ResourceLocation(\"crude_oil\")"));
    }

    @Test
    public void internalWorldgenAndRetiredPackagesAreAbsent() throws Exception {
        assertFalse(containsJava(new File("src/main/java/com/mcmoddev/mineralogy")));
        assertFalse(containsJava(new File("src/main/java/zone/moddev/mc/mineralogy/worldgen")));
        String main = new String(Files.readAllBytes(new File(
                "src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java").toPath()), StandardCharsets.UTF_8);
        assertFalse(main.contains("registerWorldGenerator"));
        assertFalse(main.contains("StoneReplacer"));
        assertFalse(main.contains("OreSpawner"));
    }

    private static String criterionItem(JsonObject advancement, String criterion) {
        return advancement.getAsJsonObject("criteria").getAsJsonObject(criterion)
                .getAsJsonObject("conditions").getAsJsonArray("items").get(0)
                .getAsJsonObject().get("item").getAsString();
    }

    private static JsonObject json(File file) throws Exception {
        try (java.io.Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private static void assertGunpowderRecipe(File recipes, String name, String requiredTag) throws Exception {
        JsonObject recipe = json(new File(recipes, name + ".json"));
        assertEquals(name, 3, recipe.getAsJsonArray("ingredients").size());
        assertGunpowderConditions(name, requiredTag, recipe.getAsJsonArray("conditions"));
        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancements/recipes/" + name + ".json"));
        assertGunpowderConditions(name + " advancement", requiredTag,
                advancement.getAsJsonArray("conditions"));
    }

    private static void assertGunpowderConditions(String name, String requiredTag, JsonArray conditions) {
        assertEquals(name, requiredTag == null ? 1 : 2, conditions.size());
        assertEquals(name, "mineralogy:config",
                conditions.get(0).getAsJsonObject().get("type").getAsString());
        if (requiredTag != null) {
            JsonObject tagCondition = conditions.get(1).getAsJsonObject();
            assertEquals(name, "forge:not", tagCondition.get("type").getAsString());
            JsonObject value = tagCondition.getAsJsonObject("value");
            assertEquals(name, "forge:tag_empty", value.get("type").getAsString());
            assertEquals(name, requiredTag, value.get("tag").getAsString());
        }
    }

    private static List<String> keys(JsonObject object) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) result.add(entry.getKey());
        return result;
    }

    private static void assertPair(File directory, String first, String second) throws Exception {
        assertArrayEquals(Files.readAllBytes(new File(directory, first).toPath()),
                Files.readAllBytes(new File(directory, second).toPath()));
    }

    private static String stripJson(String name) {
        return name.substring(0, name.length() - ".json".length());
    }

    private static boolean containsJava(File directory) {
        if (!directory.isDirectory()) return false;
        File[] children = directory.listFiles();
        if (children == null) return false;
        for (File child : children) {
            if (child.isFile() && child.getName().endsWith(".java")) return true;
            if (containsJava(child)) return true;
        }
        return false;
    }
}
