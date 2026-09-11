package zone.moddev.mc.mineralogy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
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
        assertEquals("minecraft:tuff", provider.getAsJsonObject("rocks")
                .getAsJsonObject("mineralogy:rock/minecraft/tuff").get("block").getAsString());
        assertFalse(text.contains("metadata"));
        assertTrue(text.contains("minecraft:deepslate"));
        assertTrue(text.contains("\"host_blocks\""));
        assertFalse("NeoForge 26.2 common setup must not resolve unbound host tags",
                text.contains("minecraft:stone_ore_replaceables")
                        || text.contains("minecraft:deepslate_ore_replaceables"));
        for (Map.Entry<String, JsonElement> entry : provider.getAsJsonObject("rocks").entrySet()) {
            JsonObject rock = entry.getValue().getAsJsonObject();
            assertEquals(entry.getKey(), -64, rock.get("min_y").getAsInt());
            assertEquals(entry.getKey(), 319, rock.get("max_y").getAsInt());
        }
        JsonObject oil = provider.getAsJsonObject("fluid_deposits")
                .getAsJsonObject("mineralogy:fluid_deposit/crude_oil")
                .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
        assertEquals(-48, oil.get("min_y").getAsInt());
        assertEquals(48, oil.get("max_y").getAsInt());
        assertEquals(new HashSet<String>(Arrays.asList(
                "minecraft:ocean", "minecraft:deep_ocean", "minecraft:warm_ocean",
                "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean",
                "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
                "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean")),
                stringSet(oil.getAsJsonArray("biome_ids")));
        assertEquals(new HashSet<String>(Arrays.asList("OCEAN")),
                stringSet(oil.getAsJsonArray("biome_dictionary")));
    }

    @Test
    public void customAdvancementUsesTheDataComponentItemStackShape() throws Exception {
        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancement/mineralogy/all_the_rocks.json"));
        JsonObject icon = advancement.getAsJsonObject("display").getAsJsonObject("icon");
        assertEquals("mineralogy:basalt", icon.get("id").getAsString());
        assertFalse(icon.has("item"));
    }

    @Test
    public void everyRecipeHasAProgressiveAdvancement() throws Exception {
        File recipeDir = new File(ROOT, "data/mineralogy/recipe");
        File advancementDir = new File(ROOT, "data/mineralogy/advancement/recipes");
        File[] recipes = recipeDir.listFiles((dir, name) -> name.endsWith(".json"));
        File[] advancements = advancementDir.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertNotNull(advancements);
        assertEquals(1433, recipes.length);
        assertEquals(1433, advancements.length);

        Set<String> advancementNames = new HashSet<String>();
        for (File file : advancements) advancementNames.add(file.getName());
        for (File recipeFile : recipes) {
            JsonObject recipe = json(recipeFile);
            assertFalse(recipeFile.getName(), recipe.get("type").getAsString().contains("conditional"));
            assertRecipeBookFields(recipeFile.getName(), recipe);
            assertTrue(recipeFile.getName(), advancementNames.contains(recipeFile.getName()));
            JsonObject advancement = json(new File(advancementDir, recipeFile.getName()));
            assertEquals("mineralogy:" + stripJson(recipeFile.getName()),
                    advancement.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
            assertTrue(advancement.getAsJsonObject("criteria").has("has_the_recipe"));
            assertTrue(advancement.getAsJsonObject("criteria").has("has_rock"));
            if (recipe.has("neoforge:conditions")) {
                assertTrue(recipeFile.getName(), recipe.get("neoforge:conditions").isJsonArray());
                assertTrue(recipeFile.getName(), advancement.has("neoforge:conditions"));
                assertEquals(recipeFile.getName(), recipe.get("neoforge:conditions"),
                        advancement.get("neoforge:conditions"));
            } else {
                assertFalse(recipeFile.getName(), advancement.has("neoforge:conditions"));
            }
        }

        JsonObject smooth = json(new File(advancementDir, "basalt_smooth.json"));
        assertEquals("mineralogy:basalt", criterionItem(smooth, "has_rock"));
        assertTrue(smooth.getAsJsonObject("criteria").has("has_sand"));
        assertAdvancementIngredient("basalt_smooth_stairs", "tag",
                "mineralogy:stones/basalt/smooth");
        assertAdvancementIngredient("basalt_relief_blank", "tag",
                "mineralogy:stones/basalt/smooth");
        JsonObject marked = json(new File(advancementDir, "basalt_relief_pickaxe.json"));
        assertEquals("mineralogy:basalt_relief_blank", criterionItem(marked, "has_rock"));
        JsonObject right = json(new File(advancementDir, "basalt_relief_right.json"));
        assertEquals("mineralogy:basalt_relief_left", criterionItem(right, "has_rock"));
    }

    @Test
    public void advancementsUseMinecraft2612HolderSetPredicateSchema() throws Exception {
        File mineralogy = new File(ROOT, "data/mineralogy/advancement");
        for (File file : jsonFiles(mineralogy)) {
            JsonObject advancement = json(file);
            if (advancement.has("criteria")) {
                assertInventoryPredicates(file.getPath(), advancement);
            }
        }

        File minecraft = new File(ROOT, "data/minecraft/advancement/recipes");
        for (File file : jsonFiles(minecraft)) {
            JsonObject wrapper = json(file);
            if (wrapper.has("forge:conditional")) {
                for (JsonElement branch : wrapper.getAsJsonArray("forge:conditional")) {
                    assertInventoryPredicates(file.getPath(), branch.getAsJsonObject());
                }
            } else {
                assertInventoryPredicates(file.getPath(), wrapper);
            }
        }
    }

    @Test
    public void minecraftOverridesUseNativeRecipesAndRecipeBookFields() throws Exception {
        File recipeDirectory = new File(ROOT, "data/minecraft/recipe");
        File[] recipes = recipeDirectory.listFiles((directory, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertEquals(48, recipes.length);
        for (File file : recipes) {
            JsonObject recipe = json(file);
            assertFalse(file.getName(), recipe.has("recipes"));
            assertFalse(file.getName(), recipe.has("forge:condition"));
            assertFalse(file.getName(), recipe.has("neoforge:conditions"));
            assertRecipeBookFields(file.getName(), recipe);
        }
    }

    @Test
    public void rockFurnacesUnlockFromTheirMatchingSlabs() throws Exception {
        File advancementDir = new File(ROOT, "data/mineralogy/advancement/recipes");
        File[] furnaces = advancementDir.listFiles((dir, name) -> name.endsWith("_furnace.json"));
        assertNotNull(furnaces);
        assertEquals(108, furnaces.length);

        for (File file : furnaces) {
            JsonObject advancement = json(file);
            JsonObject criteria = advancement.getAsJsonObject("criteria");
            assertTrue(file.getName(), criteria.has("has_rock"));
            assertFalse(file.getName(), criteria.has("has_furnace"));
            String expectedSlabTag = expectedSlabTag(stripJson(file.getName()));
            assertAdvancementIngredient(stripJson(file.getName()), "tag", expectedSlabTag);

            JsonArray requirements = advancement.getAsJsonArray("requirements");
            assertEquals(file.getName(), 1, requirements.size());
            JsonArray unlockAlternatives = requirements.get(0).getAsJsonArray();
            assertEquals(file.getName(), 2, unlockAlternatives.size());
            assertEquals(file.getName(), "has_the_recipe", unlockAlternatives.get(0).getAsString());
            assertEquals(file.getName(), "has_rock", unlockAlternatives.get(1).getAsString());
        }
    }

    @Test
    public void nativeRockAliasesDriveRecipesAndAdvancementsWithoutStealingVanillaForms() throws Exception {
        File tags = new File(ROOT, "data/mineralogy/tags/item");
        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            assertTagValues(new File(tags, "stones/" + family + ".json"),
                    "mineralogy:" + family, "minecraft:" + family);
            assertTagValues(new File(tags, "stones/" + family + "/smooth.json"),
                    "mineralogy:" + family + "_smooth", "minecraft:polished_" + family);
        }
        assertTagValues(new File(tags, "stones/basalt.json"),
                "mineralogy:basalt", "minecraft:basalt");
        assertTagValues(new File(tags, "stones/basalt/smooth.json"),
                "mineralogy:basalt_smooth", "minecraft:polished_basalt", "minecraft:smooth_basalt");
        assertTagValues(new File(tags, "stones/tuff.json"),
                "mineralogy:tuff", "minecraft:tuff");
        assertTagValues(new File(tags, "stones/tuff/smooth.json"),
                "mineralogy:tuff_smooth", "minecraft:polished_tuff");
        assertTagValues(new File(tags, "stones/tuff/brick.json"),
                "mineralogy:tuff_brick", "minecraft:tuff_bricks");
        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            assertTagValues(new File(tags, "slabs/" + family + ".json"),
                    "mineralogy:" + family + "_slab", "minecraft:" + family + "_slab");
            assertTagValues(new File(tags, "slabs/" + family + "/smooth.json"),
                    "mineralogy:" + family + "_smooth_slab",
                    "minecraft:polished_" + family + "_slab");
        }
        assertTagValues(new File(tags, "slabs/basalt.json"), "mineralogy:basalt_slab");
        assertTagValues(new File(tags, "slabs/basalt/smooth.json"),
                "mineralogy:basalt_smooth_slab");
        assertTagValues(new File(tags, "slabs/tuff.json"),
                "mineralogy:tuff_slab", "minecraft:tuff_slab");
        assertTagValues(new File(tags, "slabs/tuff/smooth.json"),
                "mineralogy:tuff_smooth_slab", "minecraft:polished_tuff_slab");
        assertTagValues(new File(tags, "slabs/tuff/brick.json"),
                "mineralogy:tuff_brick_slab", "minecraft:tuff_brick_slab");

        assertRecipeIngredient("basalt_slab", "tag", "mineralogy:stones/basalt");
        assertRecipeIngredient("basalt_stairs", "tag", "mineralogy:stones/basalt");
        assertRecipeIngredient("basalt_wall", "tag", "mineralogy:stones/basalt");
        assertRecipeIngredient("basalt_smooth_slab", "tag", "mineralogy:stones/basalt/smooth");
        assertRecipeIngredient("basalt_smooth_stairs", "tag", "mineralogy:stones/basalt/smooth");
        assertRecipeIngredient("basalt_smooth_wall", "tag", "mineralogy:stones/basalt/smooth");
        assertRecipeIngredient("tuff_slab", "item", "mineralogy:tuff");
        assertRecipeIngredient("tuff_stairs", "item", "mineralogy:tuff");
        assertRecipeIngredient("tuff_wall", "item", "mineralogy:tuff");
        assertRecipeIngredient("tuff_smooth", "item", "mineralogy:tuff");
        assertRecipeIngredient("tuff_brick", "tag", "mineralogy:stones/tuff");
        assertRecipeIngredient("tuff_smooth_brick", "item", "mineralogy:tuff_smooth");
        assertRecipeIngredient("tuff_brick_stairs", "item", "mineralogy:tuff_brick");
        assertRecipeIngredient("tuff_smooth_stairs", "item", "mineralogy:tuff_smooth");

        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            assertRecipeIngredient(family + "_slab", "item", "mineralogy:" + family);
            assertRecipeIngredient(family + "_stairs", "item", "mineralogy:" + family);
            assertRecipeIngredient(family + "_wall", "item", "mineralogy:" + family);
            assertRecipeIngredient(family + "_smooth_slab", "item",
                    "mineralogy:" + family + "_smooth");
            assertRecipeIngredient(family + "_smooth_stairs", "item",
                    "mineralogy:" + family + "_smooth");
            assertRecipeIngredient(family + "_smooth_wall", "tag",
                    "mineralogy:stones/" + family + "/smooth");
        }
        for (String family : Arrays.asList("andesite", "basalt", "diorite", "granite", "tuff")) {
            assertRecipeIngredient(family + "_brick", "tag", "mineralogy:stones/" + family);
            assertAdvancementIngredient(family + "_brick", "tag", "mineralogy:stones/" + family);
            assertRecipeIngredient(family + "_smooth", "item", "mineralogy:" + family);
            assertAdvancementIngredient(family + "_smooth", "item", "mineralogy:" + family);
            assertAdvancementIngredient(family + "_relief_blank", "tag",
                    "mineralogy:stones/" + family + "/smooth");

            JsonObject vanillaPolished = json(new File(ROOT,
                    "data/minecraft/recipe/polished_" + family + ".json"));
            assertEquals("minecraft:crafting_shapeless",
                    vanillaPolished.get("type").getAsString());
            assertEquals("minecraft:" + family, vanillaPolished.getAsJsonArray("ingredients")
                    .get(0).getAsString());
            assertEquals("minecraft:sand", vanillaPolished.getAsJsonArray("ingredients")
                    .get(1).getAsString());
            assertEquals("minecraft:polished_" + family, vanillaPolished
                    .getAsJsonObject("result").get("id").getAsString());
            assertEquals(1, vanillaPolished.getAsJsonObject("result").get("count").getAsInt());

            JsonObject nativeAdvancement = json(new File(ROOT,
                    "data/minecraft/advancement/recipes/building_blocks/polished_"
                            + family + ".json"));
            assertEquals("minecraft:" + family, nativeAdvancement.getAsJsonObject("criteria")
                    .getAsJsonObject("has_rock").getAsJsonObject("conditions")
                    .getAsJsonArray("items").get(0).getAsJsonObject()
                    .get("items").getAsString());
            assertEquals("minecraft:sand", nativeAdvancement.getAsJsonObject("criteria")
                    .getAsJsonObject("has_sand").getAsJsonObject("conditions")
                    .getAsJsonArray("items").get(0).getAsJsonObject()
                    .get("items").getAsString());
            assertEquals(2, nativeAdvancement.getAsJsonArray("requirements").size());
        }

        assertAdvancementIngredient("basalt_slab", "tag", "mineralogy:stones/basalt");
        assertAdvancementIngredient("andesite_slab", "item", "mineralogy:andesite");
        assertAdvancementIngredient("andesite_furnace", "tag", "mineralogy:slabs/andesite");

        String allResources = new String(Files.readAllBytes(
                new File(tags, "slabs/basalt.json").toPath()), StandardCharsets.UTF_8)
                + new String(Files.readAllBytes(
                        new File(tags, "slabs/basalt/smooth.json").toPath()), StandardCharsets.UTF_8);
        assertFalse(allResources.contains("minecraft:basalt_slab"));
        assertFalse(allResources.contains("minecraft:polished_basalt_slab"));
    }

    @Test
    public void nativeEquivalentRockFamiliesReuseMinecraftTextures() throws Exception {
        File models = new File(ROOT, "assets/mineralogy/models/block");
        StringBuilder contents = new StringBuilder();
        for (File model : jsonFiles(models)) {
            contents.append(new String(Files.readAllBytes(model.toPath()), StandardCharsets.UTF_8));
        }
        String allModels = contents.toString();

        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            assertEquals(family, 26, occurrences(allModels,
                    "\"minecraft:block/" + family + "\""));
            assertEquals(family, 83, occurrences(allModels,
                    "\"minecraft:block/polished_" + family + "\""));
            assertEquals(family, 0, occurrences(allModels,
                    "\"mineralogy:blocks/" + family + "\""));
            assertEquals(family, 0, occurrences(allModels,
                    "\"mineralogy:blocks/" + family + "_smooth\""));
        }
        assertEquals(13, occurrences(allModels, "\"minecraft:block/basalt_top\""));
        assertEquals(14, occurrences(allModels, "\"minecraft:block/basalt_side\""));
        assertEquals(13, occurrences(allModels,
                "\"minecraft:block/polished_basalt_top\""));
        assertEquals(171, occurrences(allModels,
                "\"minecraft:block/polished_basalt_side\""));
        assertEquals(0, occurrences(allModels, "\"mineralogy:blocks/basalt\""));
        assertEquals(0, occurrences(allModels, "\"mineralogy:blocks/basalt_smooth\""));

        assertEquals(26, occurrences(allModels, "\"minecraft:block/tuff\""));
        assertEquals(83, occurrences(allModels, "\"minecraft:block/polished_tuff\""));
        assertEquals(28, occurrences(allModels, "\"minecraft:block/tuff_bricks\""));
        assertEquals(0, occurrences(allModels, "\"mineralogy:blocks/tuff\""));
        assertEquals(0, occurrences(allModels, "\"mineralogy:blocks/tuff_smooth\""));
        assertEquals(0, occurrences(allModels, "\"mineralogy:blocks/tuff_brick_top\""));
        assertEquals(0, occurrences(allModels, "\"mineralogy:blocks/tuff_brick_side\""));

        // Smooth brick has no native tuff equivalent, and every furnace retains its
        // Mineralogy-specific front while inheriting the matching native family surface.
        assertEquals(15, occurrences(allModels,
                "\"mineralogy:blocks/tuff_smooth_brick_top\""));
        assertEquals(13, occurrences(allModels,
                "\"mineralogy:blocks/tuff_smooth_brick_side\""));
        for (String finish : Arrays.asList("", "_smooth", "_brick", "_smooth_brick")) {
            assertEquals(finish, 1, occurrences(allModels,
                    "\"mineralogy:blocks/tuff" + finish + "_furnace_front_off\""));
            assertEquals(finish, 1, occurrences(allModels,
                    "\"mineralogy:blocks/tuff" + finish + "_furnace_front_on\""));
        }
    }

    @Test
    public void acceptedRecipeDetailsArePresent() throws Exception {
        File recipes = new File(ROOT, "data/mineralogy/recipe");
        assertEquals(4, json(new File(recipes, "gypsum_dust.json"))
                .getAsJsonObject("result").get("count").getAsInt());
        assertEquals(2, json(new File(recipes, "gypsum.json")).getAsJsonArray("pattern").size());
        assertEquals("minecraft:charcoal", json(new File(recipes, "gunpowder_from_charcoal.json"))
                .getAsJsonArray("ingredients").get(0).getAsString());
        assertFalse(new File(recipes, "gunpowder_from_coal.json").exists());
        assertGunpowderRecipe(recipes, "gunpowder_from_sugar", null);
        assertGunpowderRecipe(recipes, "gunpowder_from_charcoal", null);
        assertGunpowderRecipe(recipes, "gunpowder_from_carbon_dust", "c:dusts/carbon");
        assertGunpowderRecipe(recipes, "gunpowder_from_coal_dust", "c:dusts/coal");
        assertEquals("#mineralogy:stones/basalt", json(new File(recipes, "basalt_slab.json"))
                .getAsJsonObject("key").get("x").getAsString());
        assertEquals("mineralogy:basalt", json(new File(recipes, "basalt_raw_slab_recombination.json"))
                .getAsJsonObject("result").get("id").getAsString());
        assertEquals("#c:sands", json(new File(recipes, "basalt_brick_block_polishing.json"))
                .getAsJsonArray("ingredients").get(1).getAsString());
        JsonObject furnace = json(new File(recipes, "basalt_furnace.json"));
        assertEquals("#mineralogy:slabs/basalt", furnace.getAsJsonObject("key")
                .get("x").getAsString());
        assertEquals("minecraft:furnace", furnace.getAsJsonObject("key")
                .get("y").getAsString());
        assertFalse(new File(recipes, "basalt_furnace_from_rock.json").exists());
        JsonObject vanillaFurnace = json(new File(ROOT, "data/minecraft/recipe/furnace.json"));
        assertEquals("#mineralogy:stone_crafting_materials", vanillaFurnace.getAsJsonObject("key")
                .get("#").getAsString());
        assertTrue(new File(ROOT, "data/minecraft/recipe/stone_pickaxe.json").isFile());
        assertFalse(new File(ROOT, "data/minecraft/recipe/stone_spear.json").exists());
        assertFalse(new File(ROOT,
                "data/minecraft/advancement/recipes/combat/stone_spear.json").exists());
        assertEquals("#c:cobblestones", json(new File(ROOT,
                "data/minecraft/tags/item/stone_crafting_materials.json"))
                .getAsJsonArray("values").get(0).getAsString());
        assertEquals("#c:cobblestones", json(new File(ROOT,
                "data/minecraft/tags/item/stone_tool_materials.json"))
                .getAsJsonArray("values").get(0).getAsString());
        assertTagValues(new File(ROOT, "data/c/tags/item/cobblestones.json"),
                "mineralogy:chert", "mineralogy:pumice");
        assertTagValues(new File(ROOT, "data/c/tags/block/cobblestones.json"),
                "mineralogy:chert", "mineralogy:pumice");
        assertTagValues(new File(ROOT, "data/forge/tags/item/cobblestone.json"),
                "#c:cobblestones");
        assertTagValues(new File(ROOT, "data/c/tags/item/paper.json"),
                "minecraft:paper");
        assertTagValues(new File(ROOT, "data/forge/tags/item/paper.json"),
                "#c:paper");
        assertTagValues(new File(ROOT, "data/c/tags/item/lamps/rock_salt.json"),
                "mineralogy:rocksaltlamp");
        assertTagValues(new File(ROOT, "data/forge/tags/item/lamps/rock_salt.json"),
                "#c:lamps/rock_salt");
        assertEquals("#c:paper", json(new File(recipes, "drywall.json"))
                .getAsJsonObject("key").get("p").getAsString());
        assertEquals("#c:lamps/rock_salt", json(new File(recipes, "rocksaltstreetlamp.json"))
                .getAsJsonObject("key").get("x").getAsString());
        assertFalse(new File(ROOT, "data/mineralogy/tags/item/vanilla_furnace_materials.json").exists());
        for (String moss : Arrays.asList("mossy_cobblestone_from_vine",
                "mossy_cobblestone_from_moss_block")) {
            JsonObject recipe = json(new File(ROOT, "data/minecraft/recipe/" + moss + ".json"));
            assertEquals(moss, "mossy_cobblestone", recipe.get("group").getAsString());
        }
        assertEquals("minecraft:red_dye", json(new File(recipes, "drywall_red.json"))
                .getAsJsonArray("ingredients").get(1).getAsString());
        assertEquals("minecraft:green_dye", json(new File(recipes, "drywall_green.json"))
                .getAsJsonArray("ingredients").get(1).getAsString());
        assertEquals("minecraft:yellow_dye", json(new File(recipes, "drywall_yellow.json"))
                .getAsJsonArray("ingredients").get(1).getAsString());
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
    public void neoForge262ResourcesRetainNativeWallsTagsAndPackFormats() throws Exception {
        JsonObject pack = json(new File(ROOT, "pack.mcmeta"));
        assertEquals(107, pack.getAsJsonObject("pack").get("max_format").getAsInt());
        JsonArray dataMinimum = pack.getAsJsonObject("pack").getAsJsonArray("min_format");
        assertEquals(107, dataMinimum.get(0).getAsInt());
        assertEquals(1, dataMinimum.get(1).getAsInt());
        JsonObject resourcePack = json(new File("resourcepack/x16/pack.mcmeta"));
        assertEquals(88, resourcePack.getAsJsonObject("pack").get("min_format").getAsInt());
        assertEquals(88, resourcePack.getAsJsonObject("pack").get("max_format").getAsInt());

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

        assertTrue(new File(ROOT, "data/minecraft/tags/block/walls.json").isFile());
        assertTrue(new File(ROOT, "data/minecraft/tags/item/walls.json").isFile());
        File minecraftRecipes = new File(ROOT, "data/minecraft/recipe");
        File[] overrides = minecraftRecipes.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(overrides);
        assertEquals(48, overrides.length);
        Set<String> overrideNames = new HashSet<String>();
        for (File override : overrides) overrideNames.add(override.getName());
        Set<String> expected = new HashSet<String>(Arrays.asList(
                "furnace.json", "brewing_stand.json", "lever.json", "piston.json",
                "dispenser.json", "dropper.json", "observer.json",
                "mossy_cobblestone_from_vine.json", "mossy_cobblestone_from_moss_block.json",
                "andesite.json", "diorite.json", "stone_axe.json", "stone_hoe.json",
                "stone_pickaxe.json", "stone_shovel.json", "stone_sword.json",
                "polished_andesite.json", "polished_basalt.json",
                "polished_diorite.json", "polished_granite.json", "polished_tuff.json",
                "coast_armor_trim_smithing_template.json",
                "sentry_armor_trim_smithing_template.json",
                "vex_armor_trim_smithing_template.json"));
        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            expected.add(family + "_slab.json");
            expected.add("polished_" + family + "_slab.json");
            expected.add(family + "_slab_from_" + family + "_stonecutting.json");
            expected.add("polished_" + family + "_slab_from_" + family + "_stonecutting.json");
            expected.add("polished_" + family + "_slab_from_polished_" + family
                    + "_stonecutting.json");
        }
        expected.addAll(Arrays.asList(
                "tuff_slab.json", "polished_tuff_slab.json", "tuff_brick_slab.json",
                "tuff_slab_from_tuff_stonecutting.json",
                "polished_tuff_slab_from_tuff_stonecutting.json",
                "polished_tuff_slab_from_polished_tuff_stonecutting.json",
                "tuff_brick_slab_from_tuff_stonecutting.json",
                "tuff_brick_slab_from_polished_tuff_stonecutting.json",
                "tuff_brick_slab_from_tuff_bricks_stonecutting.json"));
        assertEquals(expected, overrideNames);

        for (String family : Arrays.asList("coast", "sentry", "vex")) {
            String id = family + "_armor_trim_smithing_template";
            JsonObject enabled = json(new File(minecraftRecipes, id + ".json"));
            assertEquals(id, "#S#", enabled.getAsJsonArray("pattern").get(0).getAsString());
            assertEquals(id, "#C#", enabled.getAsJsonArray("pattern").get(1).getAsString());
            assertEquals(id, "###", enabled.getAsJsonArray("pattern").get(2).getAsString());
            assertEquals(id, "#mineralogy:cobblestone_equivalents",
                    enabled.getAsJsonObject("key").get("C").getAsString());
            assertEquals(id, "minecraft:" + id,
                    enabled.getAsJsonObject("key").get("S").getAsString());
            assertEquals(id, "minecraft:diamond",
                    enabled.getAsJsonObject("key").get("#").getAsString());
            assertEquals(id, "minecraft:" + id,
                    enabled.getAsJsonObject("result").get("id").getAsString());
            assertEquals(id, 2, enabled.getAsJsonObject("result").get("count").getAsInt());
            assertFalse(id, enabled.has("category"));
            assertTrue(id, enabled.has("show_notification"));
            assertTrue(id, enabled.get("show_notification").getAsBoolean());
        }
        File[] stonecutting = new File(ROOT, "data/mineralogy/recipe")
                .listFiles((dir, name) -> name.contains("stonecutting"));
        assertNotNull(stonecutting);
        assertEquals(0, stonecutting.length);
    }

    @Test
    public void integrationFixturesUseDataFormat107AndSingularDataDirectories() throws Exception {
        for (String sourceSet : Arrays.asList("recipeIntegrationTest", "oilCompatibilityTest")) {
            File fixtureRoot = new File("src/" + sourceSet + "/resources");
            JsonObject pack = json(new File(fixtureRoot, "pack.mcmeta"));
            assertEquals(sourceSet, 107,
                    pack.getAsJsonObject("pack").get("max_format").getAsInt());
            JsonArray minimum = pack.getAsJsonObject("pack").getAsJsonArray("min_format");
            assertEquals(sourceSet, 107, minimum.get(0).getAsInt());
            assertEquals(sourceSet, 1, minimum.get(1).getAsInt());
            assertFalse(sourceSet, new File(fixtureRoot, "data/c/tags/items").exists());
            assertFalse(sourceSet, new File(fixtureRoot, "data/c/tags/fluids").exists());
            assertFalse(sourceSet, new File(fixtureRoot, "data/forge/tags/items").exists());
            assertFalse(sourceSet, new File(fixtureRoot, "data/forge/tags/fluids").exists());
        }

        File oilRoot = new File("src/oilCompatibilityTest/resources");
        assertTrue(new File(oilRoot, "data/c/tags/item/buckets/crude_oil.json").isFile());
        assertTrue(new File(oilRoot, "data/c/tags/fluid/crude_oil.json").isFile());
        assertTrue(new File(oilRoot, "data/forge/tags/item/buckets/crude_oil.json").isFile());
        assertTrue(new File(oilRoot, "data/forge/tags/fluid/crude_oil.json").isFile());
    }

    @Test
    public void nativeSulfurAndCinnabarRemainSeparateFromMineralogyMaterials() throws Exception {
        assertTagValues(new File(ROOT, "data/c/tags/item/storage_blocks/sulfur.json"),
                "mineralogy:sulfur_block");
        assertTagValues(new File(ROOT, "data/c/tags/block/storage_blocks/sulfur.json"),
                "mineralogy:sulfur_block");
        assertTagValues(new File(ROOT, "data/c/tags/item/dusts/sulfur.json"),
                "mineralogy:sulfur_dust");
        assertTagValues(new File(ROOT, "data/forge/tags/item/storage_blocks/sulfur.json"),
                "#c:storage_blocks/sulfur");
        assertTagValues(new File(ROOT, "data/forge/tags/item/dusts/sulfur.json"),
                "#c:dusts/sulfur");

        JsonObject sulfurBlock = json(new File(ROOT,
                "data/mineralogy/recipe/sulfur_block.json"));
        assertEquals("mineralogy:sulfur_dust",
                sulfurBlock.getAsJsonObject("key").get("x").getAsString());
        JsonObject sulfurDust = json(new File(ROOT,
                "data/mineralogy/recipe/sulfur_dust.json"));
        assertEquals("#c:storage_blocks/sulfur",
                sulfurDust.getAsJsonArray("ingredients").get(0).getAsString());

        File recipeDirectory = new File(ROOT, "data/mineralogy/recipe");
        File[] recipes = recipeDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        for (File recipe : recipes) {
            String contents = new String(Files.readAllBytes(recipe.toPath()), StandardCharsets.UTF_8);
            assertFalse(recipe.getName(), contents.contains("minecraft:sulfur"));
            assertFalse(recipe.getName(), contents.contains("minecraft:cinnabar"));
        }
    }

    @Test
    public void generatedRecipeAdvancementsDisableTelemetry() throws Exception {
        File mineralogyAdvancements = new File(ROOT, "data/mineralogy/advancement/recipes");
        List<File> generated = jsonFiles(mineralogyAdvancements);
        assertEquals(1433, generated.size());
        for (File file : generated) {
            JsonObject advancement = json(file);
            assertTrue(file.getPath(), advancement.has("sends_telemetry_event"));
            assertFalse(file.getPath(), advancement.get("sends_telemetry_event").getAsBoolean());
        }

        File minecraftAdvancements = new File(ROOT, "data/minecraft/advancement/recipes");
        List<File> overrides = jsonFiles(minecraftAdvancements);
        assertEquals(21, overrides.size());
        for (File file : overrides) {
            JsonObject wrapper = json(file);
            if (wrapper.has("forge:conditional")) {
                for (JsonElement branch : wrapper.getAsJsonArray("forge:conditional")) {
                    JsonObject advancement = branch.getAsJsonObject();
                    assertTrue(file.getPath(), advancement.has("sends_telemetry_event"));
                    assertFalse(file.getPath(), advancement.get("sends_telemetry_event").getAsBoolean());
                }
            } else {
                assertTrue(file.getPath(), wrapper.has("sends_telemetry_event"));
                assertFalse(file.getPath(), wrapper.get("sends_telemetry_event").getAsBoolean());
            }
        }
    }

    @Test
    public void nativeSlabsAndCobblestoneOverridesUseStableConditionalTags() throws Exception {
        for (String family : Arrays.asList("andesite", "diorite", "granite")) {
            assertNativeSlabOverride(family + "_slab", "minecraft:" + family,
                    "mineralogy:" + family + "_slab", "minecraft:" + family + "_slab", 6);
            assertNativeSlabOverride("polished_" + family + "_slab",
                    "minecraft:polished_" + family, "mineralogy:" + family + "_smooth_slab",
                    "minecraft:polished_" + family + "_slab", 6);
            assertNativeStonecuttingOverride(family + "_slab_from_" + family + "_stonecutting",
                    "minecraft:" + family, "mineralogy:" + family + "_slab",
                    "minecraft:" + family + "_slab");
            assertNativeStonecuttingOverride("polished_" + family + "_slab_from_" + family
                    + "_stonecutting", "minecraft:" + family,
                    "mineralogy:" + family + "_smooth_slab",
                    "minecraft:polished_" + family + "_slab");
            assertNativeStonecuttingOverride("polished_" + family + "_slab_from_polished_"
                    + family + "_stonecutting", "minecraft:polished_" + family,
                    "mineralogy:" + family + "_smooth_slab",
                    "minecraft:polished_" + family + "_slab");
            assertNativeSlabConversion(family + "_slab_to_vanilla",
                    "mineralogy:" + family + "_slab", "minecraft:" + family + "_slab");
            assertNativeSlabConversion(family + "_slab_from_vanilla",
                    "minecraft:" + family + "_slab", "mineralogy:" + family + "_slab");
            assertNativeSlabConversion(family + "_smooth_slab_to_vanilla",
                    "mineralogy:" + family + "_smooth_slab",
                    "minecraft:polished_" + family + "_slab");
            assertNativeSlabConversion(family + "_smooth_slab_from_vanilla",
                    "minecraft:polished_" + family + "_slab",
                    "mineralogy:" + family + "_smooth_slab");
        }

        assertCompositeRockTag("cobblestone_equivalents", "#c:cobblestones");
        assertCompositeRockTag("stone_crafting_materials", "#minecraft:stone_crafting_materials");
        assertCompositeRockTag("stone_tool_materials", "#minecraft:stone_tool_materials");
        for (String family : rockFamilies()) {
            File blockTag = new File(ROOT, "data/mineralogy/tags/block/stones/" + family + ".json");
            assertTrue(family, blockTag.isFile());
            assertTrue(family, json(blockTag).getAsJsonArray("values").toString()
                    .contains("mineralogy:" + family));
        }
        assertTagValues(new File(ROOT, "data/mineralogy/tags/block/stones/basalt.json"),
                "mineralogy:basalt", "minecraft:basalt");

        File advancementRoot = new File(ROOT, "data/minecraft/advancement/recipes");
        assertEquals(21, countJsonFiles(advancementRoot));
        JsonObject furnace = json(new File(advancementRoot, "decorations/furnace.json"));
        assertEquals("#mineralogy:stone_crafting_materials", furnace
                .getAsJsonObject("criteria").getAsJsonObject("has_cobblestone")
                .getAsJsonObject("conditions").getAsJsonArray("items").get(0)
                .getAsJsonObject().get("items").getAsString());

        Object[][] advancementContracts = {
                { "decorations/furnace", "has_cobblestone", "tag", "mineralogy:stone_crafting_materials" },
                { "brewing/brewing_stand", "has_blaze_rod", "item", "minecraft:blaze_rod" },
                { "redstone/lever", "has_cobblestone", "tag", "mineralogy:cobblestone_equivalents" },
                { "redstone/piston", "has_redstone", "item", "minecraft:redstone" },
                { "redstone/dispenser", "has_bow", "item", "minecraft:bow" },
                { "redstone/dropper", "has_redstone", "item", "minecraft:redstone" },
                { "redstone/observer", "has_quartz", "item", "minecraft:quartz" },
                { "building_blocks/mossy_cobblestone_from_vine", "has_vine", "item", "minecraft:vine" },
                { "building_blocks/mossy_cobblestone_from_moss_block", "has_moss_block", "item", "minecraft:moss_block" },
                { "building_blocks/andesite", "has_stone", "item", "minecraft:diorite" },
                { "building_blocks/diorite", "has_quartz", "item", "minecraft:quartz" },
                { "tools/stone_axe", "has_cobblestone", "tag", "mineralogy:stone_tool_materials" },
                { "tools/stone_hoe", "has_cobblestone", "tag", "mineralogy:stone_tool_materials" },
                { "tools/stone_pickaxe", "has_cobblestone", "tag", "mineralogy:stone_tool_materials" },
                { "tools/stone_shovel", "has_cobblestone", "tag", "mineralogy:stone_tool_materials" },
                { "combat/stone_sword", "has_cobblestone", "tag", "mineralogy:stone_tool_materials" }
        };
        for (Object[] contract : advancementContracts) {
            assertStableAdvancement(new File(advancementRoot, contract[0] + ".json"),
                    (String) contract[1], (String) contract[2], (String) contract[3]);
        }
    }

    @Test
    public void oilAndBuildMetadataUseStableTargetIdentities() throws Exception {
        String properties = new String(Files.readAllBytes(new File("gradle.properties").toPath()), StandardCharsets.UTF_8);
        assertTrue(properties.contains("mod_version=6.1.2.2602002"));
        assertTrue(properties.contains("orespawn_curse_file_id=8830635"));
        String build = new String(Files.readAllBytes(new File("build.gradle").toPath()), StandardCharsets.UTF_8);
        assertTrue(build.contains("runtimeOnly(orespawnCoordinate)"));
        assertTrue(build.contains("https://maven.moddev.zone/releases"));
        assertTrue(build.contains("CurseMavenOreSpawnFallback"));
        assertTrue(build.contains("orespawnRelease"));
        String metadata = new String(Files.readAllBytes(new File(ROOT, "META-INF/neoforge.mods.toml").toPath()), StandardCharsets.UTF_8);
        assertFalse(metadata.contains("modLoader="));
        assertFalse(metadata.contains("loaderVersion="));
        assertTrue(metadata.contains("versionRange=\"${neo_version_range}\""));
        assertTrue(metadata.contains("versionRange=\"[4.0.6,5.0.0)\""));
        assertTrue(metadata.contains("ordering=\"AFTER\""));
        assertTrue(new File(ROOT, "assets/mineralogy/textures/items/crude_oil_bucket.png").isFile());
        assertTrue(new File(ROOT, "assets/mineralogy/textures/blocks/crude_oil_still.png").isFile());
        String fluidSource = new String(Files.readAllBytes(new File(
                "src/main/java/zone/moddev/mc/mineralogy/init/MineralogyFluids.java").toPath()), StandardCharsets.UTF_8);
        assertTrue(fluidSource.contains("\"flowing_crude_oil\""));
        assertTrue(fluidSource.contains("\"crude_oil_bucket\""));
        assertFalse(fluidSource.contains("new Identifier(\"crude_oil\")"));

        // Power Advantage historically registered its oil in its own namespace.  Keep
        // Mineralogy's registry identity isolated while contributing both fluids to the
        // shared, non-replacing common tag so a future compatible Power Advantage build can
        // consume either fluid without a registry collision.
        Identifier mineralogyOil = Identifier.tryParse("mineralogy:crude_oil");
        Identifier historicalPowerAdvantageOil = Identifier.tryParse("poweradvantage:crude_oil");
        assertNotEquals(historicalPowerAdvantageOil, mineralogyOil);

        JsonObject fluidTag = json(new File(ROOT, "data/c/tags/fluid/crude_oil.json"));
        assertFalse(fluidTag.get("replace").getAsBoolean());
        assertTrue(fluidTag.getAsJsonArray("values").toString().contains("mineralogy:crude_oil"));
        assertTrue(fluidTag.getAsJsonArray("values").toString().contains("mineralogy:flowing_crude_oil"));

        JsonObject bucketTag = json(new File(ROOT, "data/c/tags/item/buckets/crude_oil.json"));
        assertFalse(bucketTag.get("replace").getAsBoolean());
        assertTrue(bucketTag.getAsJsonArray("values").toString().contains("mineralogy:crude_oil_bucket"));
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
                .getAsJsonObject().get("items").getAsString();
    }

    private static void assertInventoryPredicates(String source, JsonObject advancement) {
        assertNotNull(source, advancement.getAsJsonObject("criteria"));
        for (Map.Entry<String, JsonElement> entry : advancement.getAsJsonObject("criteria").entrySet()) {
            JsonObject criterion = entry.getValue().getAsJsonObject();
            if (!"minecraft:inventory_changed".equals(criterion.get("trigger").getAsString())) {
                continue;
            }
            JsonArray predicates = criterion.getAsJsonObject("conditions").getAsJsonArray("items");
            assertNotNull(source + " " + entry.getKey(), predicates);
            for (JsonElement element : predicates) {
                JsonObject predicate = element.getAsJsonObject();
                assertFalse(source + " " + entry.getKey() + " uses the obsolete singular item key",
                        predicate.has("item"));
                assertEquals(source + " " + entry.getKey(), 1, predicate.size());
                assertTrue(source + " " + entry.getKey(), predicate.has("items"));
                JsonElement holderSet = predicate.get("items");
                if (holderSet.isJsonArray()) {
                    assertTrue(source + " " + entry.getKey(), holderSet.getAsJsonArray().size() > 0);
                    for (JsonElement holder : holderSet.getAsJsonArray()) {
                        assertTrue(source + " " + entry.getKey(), holder.isJsonPrimitive()
                                && !holder.getAsString().isEmpty());
                    }
                } else {
                    assertTrue(source + " " + entry.getKey(), holderSet.isJsonPrimitive()
                            && !holderSet.getAsString().isEmpty());
                }
            }
        }
    }

    private static String expectedSlabTag(String furnaceRecipe) {
        String stem = furnaceRecipe.replaceFirst("_furnace$", "");
        for (String finish : Arrays.asList("smooth_brick", "smooth", "brick")) {
            String suffix = "_" + finish;
            if (stem.endsWith(suffix)) {
                return "mineralogy:slabs/" + stem.substring(0, stem.length() - suffix.length())
                        + "/" + finish;
            }
        }
        return "mineralogy:slabs/" + stem;
    }

    private static void assertTagValues(File file, String... expected) throws Exception {
        JsonArray values = json(file).getAsJsonArray("values");
        assertEquals(file.getPath(), expected.length, values.size());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(file.getPath(), expected[index], values.get(index).getAsString());
        }
    }

    private static void assertRecipeIngredient(String recipeName, String key, String value)
            throws Exception {
        JsonObject recipe = json(new File(ROOT,
                "data/mineralogy/recipe/" + recipeName + ".json"));
        JsonElement ingredient = recipe.has("key")
                ? recipe.getAsJsonObject("key").get("x")
                : recipe.getAsJsonArray("ingredients").get(0);
        String expected = "tag".equals(key) ? "#" + value : value;
        assertEquals(recipeName, expected, ingredient.getAsString());
    }

    private static void assertAdvancementIngredient(String recipeName, String key, String value)
            throws Exception {
        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancement/recipes/" + recipeName + ".json"));
        JsonObject ingredient = advancement.getAsJsonObject("criteria")
                .getAsJsonObject("has_rock").getAsJsonObject("conditions")
                .getAsJsonArray("items").get(0).getAsJsonObject();
        String expected = "tag".equals(key) ? "#" + value : value;
        assertEquals(recipeName, expected, ingredient.get("items").getAsString());
        assertEquals(recipeName, 1, ingredient.size());
    }

    private static void assertNativeSlabOverride(String recipeName, String source,
            String mineralogyResult, String vanillaResult, int count) throws Exception {
        JsonObject enabled = json(new File(ROOT, "data/minecraft/recipe/" + recipeName + ".json"));
        assertEquals(recipeName, "minecraft:crafting_shaped", enabled.get("type").getAsString());
        assertEquals(recipeName, source, enabled.getAsJsonObject("key")
                .get("#").getAsString());
        assertEquals(recipeName, mineralogyResult,
                enabled.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, count,
                enabled.getAsJsonObject("result").get("count").getAsInt());
    }

    private static void assertNativeStonecuttingOverride(String recipeName, String source,
            String mineralogyResult, String vanillaResult) throws Exception {
        JsonObject enabled = json(new File(ROOT, "data/minecraft/recipe/" + recipeName + ".json"));
        assertEquals(recipeName, "minecraft:stonecutting", enabled.get("type").getAsString());
        assertEquals(recipeName, source,
                enabled.get("ingredient").getAsString());
        assertEquals(recipeName, mineralogyResult,
                enabled.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, 2,
                enabled.getAsJsonObject("result").get("count").getAsInt());
        assertFalse(recipeName, enabled.has("count"));
    }

    private static void assertNativeSlabConversion(String recipeName, String source,
            String result) throws Exception {
        JsonObject recipe = json(new File(ROOT,
                "data/mineralogy/recipe/" + recipeName + ".json"));
        assertEquals(recipeName, "minecraft:crafting_shapeless", recipe.get("type").getAsString());
        assertEquals(recipeName, 1, recipe.getAsJsonArray("ingredients").size());
        assertEquals(recipeName, source, recipe.getAsJsonArray("ingredients").get(0)
                .getAsString());
        assertEquals(recipeName, result, recipe.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, 1, recipe.getAsJsonObject("result").get("count").getAsInt());
        assertFalse(recipeName, recipe.has("neoforge:conditions"));

        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancement/recipes/" + recipeName + ".json"));
        assertFalse(recipeName, advancement.has("neoforge:conditions"));
        assertEquals(recipeName, source, criterionItem(advancement, "has_rock"));
        assertEquals(recipeName, "mineralogy:" + recipeName,
                advancement.getAsJsonObject("rewards").getAsJsonArray("recipes")
                        .get(0).getAsString());
    }

    private static void assertCompositeRockTag(String name, String baseTag) throws Exception {
        JsonArray values = json(new File(ROOT,
                "data/mineralogy/tags/item/" + name + ".json")).getAsJsonArray("values");
        assertEquals(name, 28, values.size());
        assertEquals(name, baseTag, values.get(0).getAsString());
        for (int index = 0; index < rockFamilies().size(); index++) {
            assertEquals(name, "#mineralogy:stones/" + rockFamilies().get(index),
                    values.get(index + 1).getAsString());
        }
    }

    private static List<String> rockFamilies() {
        return Arrays.asList("andesite", "basalt", "diorite", "granite", "rhyolite",
                "pegmatite", "diabase", "gabbro", "peridotite", "basaltic_glass",
                "scoria", "tuff", "shale", "conglomerate", "dolomite", "limestone",
                "siltstone", "marble", "slate", "schist", "gneiss", "phyllite",
                "amphibolite", "hornfels", "quartzite", "novaculite", "rock_salt");
    }

    private static Set<String> stringSet(JsonArray values) {
        Set<String> result = new HashSet<String>();
        for (JsonElement value : values) result.add(value.getAsString());
        return result;
    }

    private static void assertRecipeBookFields(String name, JsonObject recipe) {
        String type = recipe.get("type").getAsString();
        if ("minecraft:crafting_shaped".equals(type)) {
            Set<String> categoryFreeRecipes = new HashSet<String>(Arrays.asList(
                    "furnace.json",
                    "brewing_stand.json",
                    "coast_armor_trim_smithing_template.json",
                    "sentry_armor_trim_smithing_template.json",
                    "vex_armor_trim_smithing_template.json"));
            assertEquals(name, !categoryFreeRecipes.contains(name), recipe.has("category"));
            assertTrue(name, recipe.has("show_notification"));
            assertTrue(name, recipe.get("show_notification").getAsBoolean());
        } else if ("minecraft:crafting_shapeless".equals(type)) {
            assertTrue(name, recipe.has("category"));
            assertFalse(name, recipe.has("show_notification"));
        } else if ("minecraft:smelting".equals(type)) {
            assertEquals(name, "blocks", recipe.get("category").getAsString());
        }
    }

    private static void assertStableAdvancement(File file, String criterion, String key,
            String value) throws Exception {
        JsonObject advancement = json(file);
        assertFalse(file.getPath(), advancement.has("advancements"));
        JsonObject ingredient = advancement.getAsJsonObject("criteria")
                .getAsJsonObject(criterion).getAsJsonObject("conditions")
                .getAsJsonArray("items").get(0).getAsJsonObject();
        String actual = ingredient.get("items").getAsString();
        assertEquals(file.getPath(), "tag".equals(key) ? "#" + value : value, actual);
        JsonArray requirements = advancement.getAsJsonArray("requirements");
        assertEquals(file.getPath(), 1, requirements.size());
        assertTrue(file.getPath(), requirements.get(0).isJsonArray());
        assertEquals(file.getPath(), 2, requirements.get(0).getAsJsonArray().size());
    }

    private static int countJsonFiles(File directory) {
        File[] children = directory.listFiles();
        if (children == null) return 0;
        int count = 0;
        for (File child : children) {
            if (child.isDirectory()) count += countJsonFiles(child);
            else if (child.getName().endsWith(".json")) count++;
        }
        return count;
    }

    private static List<File> jsonFiles(File directory) {
        List<File> files = new ArrayList<File>();
        File[] children = directory.listFiles();
        if (children == null) return files;
        for (File child : children) {
            if (child.isDirectory()) files.addAll(jsonFiles(child));
            else if (child.getName().endsWith(".json")) files.add(child);
        }
        return files;
    }

    private static JsonObject json(File file) throws Exception {
        try (java.io.Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static boolean hasOptionalTagEntry(JsonArray values, String id) {
        for (JsonElement value : values) {
            if (!value.isJsonObject()) continue;
            JsonObject entry = value.getAsJsonObject();
            if (id.equals(entry.get("id").getAsString())
                    && !entry.get("required").getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    private static void assertGunpowderRecipe(File recipes, String name, String requiredTag) throws Exception {
        JsonObject recipe = json(new File(recipes, name + ".json"));
        assertEquals(name, 3, recipe.getAsJsonArray("ingredients").size());
        assertGunpowderConditions(name, requiredTag, recipe.getAsJsonArray("neoforge:conditions"));
        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancement/recipes/" + name + ".json"));
        assertGunpowderConditions(name + " advancement", requiredTag,
                advancement.getAsJsonArray("neoforge:conditions"));
    }

    private static void assertGunpowderConditions(String name, String requiredTag, JsonArray conditions) {
        assertEquals(name, 1, conditions.size());
        JsonObject condition = conditions.get(0).getAsJsonObject();
        if (requiredTag != null) {
            assertEquals(name, "neoforge:and", condition.get("type").getAsString());
            JsonArray values = condition.getAsJsonArray("values");
            assertEquals(name, 2, values.size());
            assertEquals(name, "mineralogy:config",
                    values.get(0).getAsJsonObject().get("type").getAsString());
            JsonObject tagCondition = values.get(1).getAsJsonObject();
            assertEquals(name, "neoforge:not", tagCondition.get("type").getAsString());
            JsonObject value = tagCondition.getAsJsonObject("value");
            assertEquals(name, "neoforge:tag_empty", value.get("type").getAsString());
            assertEquals(name, requiredTag, value.get("tag").getAsString());
        } else {
            assertEquals(name, "mineralogy:config", condition.get("type").getAsString());
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

    private static int occurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
