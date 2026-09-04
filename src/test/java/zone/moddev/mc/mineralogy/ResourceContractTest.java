package zone.moddev.mc.mineralogy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
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
        assertTrue(text.contains("minecraft:stone_ore_replaceables"));
        assertTrue(text.contains("minecraft:deepslate_ore_replaceables"));
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
                "data/mineralogy/advancements/mineralogy/all_the_rocks.json"));
        JsonObject icon = advancement.getAsJsonObject("display").getAsJsonObject("icon");
        assertEquals("mineralogy:basalt", icon.get("id").getAsString());
        assertFalse(icon.has("item"));
    }

    @Test
    public void everyRecipeHasAProgressiveAdvancement() throws Exception {
        File recipeDir = new File(ROOT, "data/mineralogy/recipes");
        File advancementDir = new File(ROOT, "data/mineralogy/advancements/recipes");
        File[] recipes = recipeDir.listFiles((dir, name) -> name.endsWith(".json"));
        File[] advancements = advancementDir.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertNotNull(advancements);
        assertEquals(1427, recipes.length);
        assertEquals(1427, advancements.length);

        Set<String> advancementNames = new HashSet<String>();
        for (File file : advancements) advancementNames.add(file.getName());
        for (File recipeFile : recipes) {
            JsonObject recipe = json(recipeFile);
            assertFalse(recipeFile.getName(), recipe.get("type").getAsString().startsWith("forge:ore_"));
            assertRecipeBookFields(recipeFile.getName(), recipe);
            assertTrue(recipeFile.getName(), advancementNames.contains(recipeFile.getName()));
            JsonObject advancement = json(new File(advancementDir, recipeFile.getName()));
            assertEquals("mineralogy:" + stripJson(recipeFile.getName()),
                    advancement.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
            assertTrue(advancement.getAsJsonObject("criteria").has("has_the_recipe"));
            assertTrue(advancement.getAsJsonObject("criteria").has("has_rock"));
            if (recipe.has("forge:condition")) {
                assertTrue(recipeFile.getName(), recipe.get("forge:condition").isJsonObject());
                assertTrue(recipeFile.getName(), advancement.has("forge:condition"));
                assertEquals(recipeFile.getName(), recipe.get("forge:condition"),
                        advancement.get("forge:condition"));
            } else {
                assertFalse(recipeFile.getName(), advancement.has("forge:condition"));
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
    public void advancementsUseMinecraft1206HolderSetPredicateSchema() throws Exception {
        File mineralogy = new File(ROOT, "data/mineralogy/advancements");
        for (File file : jsonFiles(mineralogy)) {
            JsonObject advancement = json(file);
            if (advancement.has("criteria")) {
                assertInventoryPredicates(file.getPath(), advancement);
            }
        }

        File minecraft = new File(ROOT, "data/minecraft/advancements/recipes");
        for (File file : jsonFiles(minecraft)) {
            JsonObject wrapper = json(file);
            if (wrapper.has("advancements")) {
                for (JsonElement branch : wrapper.getAsJsonArray("advancements")) {
                    assertInventoryPredicates(file.getPath(), branch.getAsJsonObject());
                }
            } else {
                assertInventoryPredicates(file.getPath(), wrapper);
            }
        }
    }

    @Test
    public void minecraftOverridesUseForge50WrappersAndRecipeBookFields() throws Exception {
        File recipeDirectory = new File(ROOT, "data/minecraft/recipes");
        File[] recipes = recipeDirectory.listFiles((directory, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertEquals(38, recipes.length);
        for (File file : recipes) {
            JsonObject outer = json(file);
            if ("forge:conditional".equals(outer.get("type").getAsString())) {
                assertEquals(file.getName(), 2, outer.getAsJsonArray("recipes").size());
                for (JsonElement element : outer.getAsJsonArray("recipes")) {
                    JsonObject branch = element.getAsJsonObject();
                    assertTrue(file.getName(), branch.get("forge:condition").isJsonObject());
                    assertRecipeBookFields(file.getName(), branch.getAsJsonObject("recipe"));
                }
            } else {
                assertRecipeBookFields(file.getName(), outer);
            }
        }
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
        File tags = new File(ROOT, "data/mineralogy/tags/items");
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
                "mineralogy:tuff_smooth");
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

        assertRecipeIngredient("basalt_slab", "tag", "mineralogy:stones/basalt");
        assertRecipeIngredient("basalt_stairs", "tag", "mineralogy:stones/basalt");
        assertRecipeIngredient("basalt_wall", "tag", "mineralogy:stones/basalt");
        assertRecipeIngredient("basalt_smooth_slab", "tag", "mineralogy:stones/basalt/smooth");
        assertRecipeIngredient("basalt_smooth_stairs", "tag", "mineralogy:stones/basalt/smooth");
        assertRecipeIngredient("basalt_smooth_wall", "tag", "mineralogy:stones/basalt/smooth");
        assertRecipeIngredient("tuff_slab", "tag", "mineralogy:stones/tuff");
        assertRecipeIngredient("tuff_stairs", "tag", "mineralogy:stones/tuff");
        assertRecipeIngredient("tuff_wall", "tag", "mineralogy:stones/tuff");
        assertRecipeIngredient("tuff_smooth", "tag", "mineralogy:stones/tuff");

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
        for (String family : Arrays.asList("andesite", "basalt", "diorite", "granite")) {
            assertRecipeIngredient(family + "_brick", "tag", "mineralogy:stones/" + family);
            assertAdvancementIngredient(family + "_brick", "tag", "mineralogy:stones/" + family);
            assertRecipeIngredient(family + "_smooth", "item", "mineralogy:" + family);
            assertAdvancementIngredient(family + "_smooth", "item", "mineralogy:" + family);
            assertAdvancementIngredient(family + "_relief_blank", "tag",
                    "mineralogy:stones/" + family + "/smooth");

            JsonObject vanillaPolished = json(new File(ROOT,
                    "data/minecraft/recipes/polished_" + family + ".json"));
            assertEquals("minecraft:crafting_shapeless",
                    vanillaPolished.get("type").getAsString());
            assertEquals("minecraft:" + family, vanillaPolished.getAsJsonArray("ingredients")
                    .get(0).getAsJsonObject().get("item").getAsString());
            assertEquals("minecraft:sand", vanillaPolished.getAsJsonArray("ingredients")
                    .get(1).getAsJsonObject().get("item").getAsString());
            assertEquals("minecraft:polished_" + family, vanillaPolished
                    .getAsJsonObject("result").get("id").getAsString());
            assertEquals(1, vanillaPolished.getAsJsonObject("result").get("count").getAsInt());

            JsonObject nativeAdvancement = json(new File(ROOT,
                    "data/minecraft/advancements/recipes/building_blocks/polished_"
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
        assertEquals("mineralogy:stones/basalt", json(new File(recipes, "basalt_slab.json"))
                .getAsJsonObject("key").getAsJsonObject("x").get("tag").getAsString());
        assertEquals("mineralogy:basalt", json(new File(recipes, "basalt_raw_slab_recombination.json"))
                .getAsJsonObject("result").get("id").getAsString());
        assertEquals("forge:sand", json(new File(recipes, "basalt_brick_block_polishing.json"))
                .getAsJsonArray("ingredients").get(1).getAsJsonObject().get("tag").getAsString());
        JsonObject furnace = json(new File(recipes, "basalt_furnace.json"));
        assertEquals("mineralogy:slabs/basalt", furnace.getAsJsonObject("key")
                .getAsJsonObject("x").get("tag").getAsString());
        assertEquals("minecraft:furnace", furnace.getAsJsonObject("key")
                .getAsJsonObject("y").get("item").getAsString());
        assertFalse(new File(recipes, "basalt_furnace_from_rock.json").exists());
        JsonObject furnaceOverride = json(new File(ROOT, "data/minecraft/recipes/furnace.json"));
        JsonObject vanillaFurnace = conditionalRecipe(furnaceOverride, 0);
        assertEquals("mineralogy:stone_crafting_materials", vanillaFurnace.getAsJsonObject("key")
                .getAsJsonObject("#").get("tag").getAsString());
        assertEquals("minecraft:stone_crafting_materials", conditionalRecipe(furnaceOverride, 1)
                .getAsJsonObject("key").getAsJsonObject("#").get("tag").getAsString());
        assertEquals("COBBLESTONE_EQUIVILENT", furnaceOverride.getAsJsonArray("recipes").get(0)
                .getAsJsonObject().getAsJsonObject("forge:condition")
                .get("flag").getAsString());
        assertTrue(new File(ROOT, "data/minecraft/recipes/stone_pickaxe.json").isFile());
        assertEquals("#forge:cobblestone", json(new File(ROOT,
                "data/minecraft/tags/items/stone_crafting_materials.json"))
                .getAsJsonArray("values").get(0).getAsString());
        assertEquals("#forge:cobblestone", json(new File(ROOT,
                "data/minecraft/tags/items/stone_tool_materials.json"))
                .getAsJsonArray("values").get(0).getAsString());
        assertTagValues(new File(ROOT, "data/forge/tags/items/cobblestone.json"),
                "mineralogy:chert", "mineralogy:pumice");
        assertTagValues(new File(ROOT, "data/forge/tags/blocks/cobblestone.json"),
                "mineralogy:chert", "mineralogy:pumice");
        assertFalse(new File(ROOT, "data/mineralogy/tags/items/vanilla_furnace_materials.json").exists());
        for (String moss : Arrays.asList("mossy_cobblestone_from_vine",
                "mossy_cobblestone_from_moss_block")) {
            JsonObject wrapper = json(new File(ROOT, "data/minecraft/recipes/" + moss + ".json"));
            assertEquals(moss, "mossy_cobblestone", conditionalRecipe(wrapper, 0)
                    .get("group").getAsString());
            assertEquals(moss, "mossy_cobblestone", conditionalRecipe(wrapper, 1)
                    .get("group").getAsString());
        }
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
    public void forge50ResourcesRetainNativeWallsTagsAndPackFormats() throws Exception {
        JsonObject pack = json(new File(ROOT, "pack.mcmeta"));
        assertEquals(32, pack.getAsJsonObject("pack").get("pack_format").getAsInt());
        assertFalse(pack.getAsJsonObject("pack").has("forge:resource_pack_format"));
        assertFalse(pack.getAsJsonObject("pack").has("forge:data_pack_format"));

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
        assertEquals(38, overrides.length);
        Set<String> overrideNames = new HashSet<String>();
        for (File override : overrides) overrideNames.add(override.getName());
        Set<String> expected = new HashSet<String>(Arrays.asList(
                "furnace.json", "brewing_stand.json", "lever.json", "piston.json",
                "dispenser.json", "dropper.json", "observer.json",
                "mossy_cobblestone_from_vine.json", "mossy_cobblestone_from_moss_block.json",
                "andesite.json", "diorite.json", "stone_axe.json", "stone_hoe.json",
                "stone_pickaxe.json", "stone_shovel.json", "stone_sword.json",
                "polished_andesite.json", "polished_basalt.json",
                "polished_diorite.json", "polished_granite.json",
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
        assertEquals(expected, overrideNames);

        for (String family : Arrays.asList("coast", "sentry", "vex")) {
            String id = family + "_armor_trim_smithing_template";
            JsonObject wrapper = json(new File(minecraftRecipes, id + ".json"));
            assertEquals(id, 2, wrapper.getAsJsonArray("recipes").size());
            JsonObject enabled = conditionalRecipe(wrapper, 0);
            JsonObject disabled = conditionalRecipe(wrapper, 1);
            assertEquals(id, "#S#", enabled.getAsJsonArray("pattern").get(0).getAsString());
            assertEquals(id, "#C#", enabled.getAsJsonArray("pattern").get(1).getAsString());
            assertEquals(id, "###", enabled.getAsJsonArray("pattern").get(2).getAsString());
            assertEquals(id, "mineralogy:cobblestone_equivalents",
                    enabled.getAsJsonObject("key").getAsJsonObject("C").get("tag").getAsString());
            assertEquals(id, "minecraft:cobblestone",
                    disabled.getAsJsonObject("key").getAsJsonObject("C").get("item").getAsString());
            for (JsonObject branch : Arrays.asList(enabled, disabled)) {
                assertEquals(id, "minecraft:" + id,
                        branch.getAsJsonObject("key").getAsJsonObject("S").get("item").getAsString());
                assertEquals(id, "minecraft:diamond",
                        branch.getAsJsonObject("key").getAsJsonObject("#").get("item").getAsString());
                assertEquals(id, "minecraft:" + id,
                        branch.getAsJsonObject("result").get("id").getAsString());
                assertEquals(id, 2, branch.getAsJsonObject("result").get("count").getAsInt());
            }
        }
        File[] stonecutting = new File(ROOT, "data/mineralogy/recipes")
                .listFiles((dir, name) -> name.contains("stonecutting"));
        assertNotNull(stonecutting);
        assertEquals(0, stonecutting.length);
    }

    @Test
    public void generatedRecipeAdvancementsDisableTelemetry() throws Exception {
        File mineralogyAdvancements = new File(ROOT, "data/mineralogy/advancements/recipes");
        List<File> generated = jsonFiles(mineralogyAdvancements);
        assertEquals(1427, generated.size());
        for (File file : generated) {
            JsonObject advancement = json(file);
            assertTrue(file.getPath(), advancement.has("sends_telemetry_event"));
            assertFalse(file.getPath(), advancement.get("sends_telemetry_event").getAsBoolean());
        }

        File minecraftAdvancements = new File(ROOT, "data/minecraft/advancements/recipes");
        List<File> overrides = jsonFiles(minecraftAdvancements);
        assertEquals(20, overrides.size());
        for (File file : overrides) {
            JsonObject wrapper = json(file);
            if (wrapper.has("advancements")) {
                for (JsonElement branch : wrapper.getAsJsonArray("advancements")) {
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

        assertCompositeRockTag("cobblestone_equivalents", "#forge:cobblestone");
        assertCompositeRockTag("stone_crafting_materials", "#minecraft:stone_crafting_materials");
        assertCompositeRockTag("stone_tool_materials", "#minecraft:stone_tool_materials");
        for (String family : rockFamilies()) {
            File blockTag = new File(ROOT, "data/mineralogy/tags/blocks/stones/" + family + ".json");
            assertTrue(family, blockTag.isFile());
            assertTrue(family, json(blockTag).getAsJsonArray("values").toString()
                    .contains("mineralogy:" + family));
        }
        assertTagValues(new File(ROOT, "data/mineralogy/tags/blocks/stones/basalt.json"),
                "mineralogy:basalt", "minecraft:basalt");

        File advancementRoot = new File(ROOT, "data/minecraft/advancements/recipes");
        assertEquals(20, countJsonFiles(advancementRoot));
        JsonObject furnace = json(new File(advancementRoot, "decorations/furnace.json"));
        assertEquals("#mineralogy:stone_crafting_materials", conditionalAdvancement(furnace, 0)
                .getAsJsonObject("criteria").getAsJsonObject("has_cobblestone")
                .getAsJsonObject("conditions").getAsJsonArray("items").get(0)
                .getAsJsonObject().get("items").getAsString());
        assertEquals("#minecraft:stone_crafting_materials", conditionalAdvancement(furnace, 1)
                .getAsJsonObject("criteria").getAsJsonObject("has_cobblestone")
                .getAsJsonObject("conditions").getAsJsonArray("items").get(0)
                .getAsJsonObject().get("items").getAsString());

        Object[][] advancementContracts = {
                { "decorations/furnace", "has_cobblestone", "tag",
                        "mineralogy:stone_crafting_materials", "minecraft:stone_crafting_materials" },
                { "brewing/brewing_stand", "has_blaze_rod", "item",
                        "minecraft:blaze_rod", "minecraft:blaze_rod" },
                { "redstone/lever", "has_cobblestone", "tag",
                        "mineralogy:cobblestone_equivalents", "forge:cobblestone" },
                { "redstone/piston", "has_redstone", "item", "minecraft:redstone", "minecraft:redstone" },
                { "redstone/dispenser", "has_bow", "item", "minecraft:bow", "minecraft:bow" },
                { "redstone/dropper", "has_redstone", "item", "minecraft:redstone", "minecraft:redstone" },
                { "redstone/observer", "has_quartz", "item", "minecraft:quartz", "minecraft:quartz" },
                { "building_blocks/mossy_cobblestone_from_vine", "has_vine", "item", "minecraft:vine", "minecraft:vine" },
                { "building_blocks/mossy_cobblestone_from_moss_block", "has_moss_block", "item", "minecraft:moss_block", "minecraft:moss_block" },
                { "building_blocks/andesite", "has_stone", "item", "minecraft:diorite", "minecraft:diorite" },
                { "building_blocks/diorite", "has_quartz", "item", "minecraft:quartz", "minecraft:quartz" },
                { "tools/stone_axe", "has_cobblestone", "tag", "mineralogy:stone_tool_materials", "minecraft:stone_tool_materials" },
                { "tools/stone_hoe", "has_cobblestone", "tag", "mineralogy:stone_tool_materials", "minecraft:stone_tool_materials" },
                { "tools/stone_pickaxe", "has_cobblestone", "tag", "mineralogy:stone_tool_materials", "minecraft:stone_tool_materials" },
                { "tools/stone_shovel", "has_cobblestone", "tag", "mineralogy:stone_tool_materials", "minecraft:stone_tool_materials" },
                { "combat/stone_sword", "has_cobblestone", "tag", "mineralogy:stone_tool_materials", "minecraft:stone_tool_materials" }
        };
        for (Object[] contract : advancementContracts) {
            assertConditionalAdvancement(new File(advancementRoot, contract[0] + ".json"),
                    (String) contract[1], (String) contract[2], (String) contract[3],
                    (String) contract[4]);
        }
    }

    @Test
    public void oilAndBuildMetadataUseStableTargetIdentities() throws Exception {
        String properties = new String(Files.readAllBytes(new File("gradle.properties").toPath()), StandardCharsets.UTF_8);
        assertTrue(properties.contains("mod_version=6.1.2.120061"));
        assertTrue(properties.contains("orespawn_curse_file_id=8786031"));
        String build = new String(Files.readAllBytes(new File("build.gradle").toPath()), StandardCharsets.UTF_8);
        assertTrue(build.contains("runtimeOnly \"curse.maven:mmd-orespawn-"));
        assertTrue(build.contains("orespawnRelease"));
        String metadata = new String(Files.readAllBytes(new File(ROOT, "META-INF/mods.toml").toPath()), StandardCharsets.UTF_8);
        assertTrue(metadata.contains("loaderVersion=\"[50,)\""));
        assertTrue(metadata.contains("versionRange=\"[50.2.0,51)\""));
        assertTrue(metadata.contains("versionRange=\"[4.0.6,5.0.0)\""));
        assertTrue(metadata.contains("ordering=\"AFTER\""));
        assertTrue(new File(ROOT, "assets/mineralogy/textures/items/crude_oil_bucket.png").isFile());
        assertTrue(new File(ROOT, "assets/mineralogy/textures/blocks/crude_oil_still.png").isFile());
        String fluidSource = new String(Files.readAllBytes(new File(
                "src/main/java/zone/moddev/mc/mineralogy/init/MineralogyFluids.java").toPath()), StandardCharsets.UTF_8);
        assertTrue(fluidSource.contains("\"flowing_crude_oil\""));
        assertTrue(fluidSource.contains("\"crude_oil_bucket\""));
        assertFalse(fluidSource.contains("new ResourceLocation(\"crude_oil\")"));

        // Power Advantage historically registered its oil in its own namespace.  Keep
        // Mineralogy's registry identity isolated while contributing both fluids to the
        // shared, non-replacing Forge tag so a future compatible Power Advantage build can
        // consume either fluid without a registry collision.
        ResourceLocation mineralogyOil = ResourceLocation.tryParse("mineralogy:crude_oil");
        ResourceLocation historicalPowerAdvantageOil = ResourceLocation.tryParse("poweradvantage:crude_oil");
        assertNotEquals(historicalPowerAdvantageOil, mineralogyOil);

        JsonObject fluidTag = json(new File(ROOT, "data/forge/tags/fluids/crude_oil.json"));
        assertFalse(fluidTag.get("replace").getAsBoolean());
        assertTrue(fluidTag.getAsJsonArray("values").toString().contains("mineralogy:crude_oil"));
        assertTrue(fluidTag.getAsJsonArray("values").toString().contains("mineralogy:flowing_crude_oil"));

        JsonObject bucketTag = json(new File(ROOT, "data/forge/tags/items/buckets/crude_oil.json"));
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
                "data/mineralogy/recipes/" + recipeName + ".json"));
        JsonObject ingredient = recipe.has("key")
                ? recipe.getAsJsonObject("key").getAsJsonObject("x")
                : recipe.getAsJsonArray("ingredients").get(0).getAsJsonObject();
        assertEquals(recipeName, value, ingredient.get(key).getAsString());
        assertEquals(recipeName, 1, ingredient.size());
    }

    private static void assertAdvancementIngredient(String recipeName, String key, String value)
            throws Exception {
        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancements/recipes/" + recipeName + ".json"));
        JsonObject ingredient = advancement.getAsJsonObject("criteria")
                .getAsJsonObject("has_rock").getAsJsonObject("conditions")
                .getAsJsonArray("items").get(0).getAsJsonObject();
        String expected = "tag".equals(key) ? "#" + value : value;
        assertEquals(recipeName, expected, ingredient.get("items").getAsString());
        assertEquals(recipeName, 1, ingredient.size());
    }

    private static void assertNativeSlabOverride(String recipeName, String source,
            String mineralogyResult, String vanillaResult, int count) throws Exception {
        JsonObject wrapper = json(new File(ROOT, "data/minecraft/recipes/" + recipeName + ".json"));
        assertEquals(recipeName, "forge:conditional", wrapper.get("type").getAsString());
        JsonObject enabled = conditionalRecipe(wrapper, 0);
        JsonObject fallback = conditionalRecipe(wrapper, 1);
        assertEquals(recipeName, source, enabled.getAsJsonObject("key")
                .getAsJsonObject("#").get("item").getAsString());
        assertEquals(recipeName, mineralogyResult,
                enabled.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, vanillaResult,
                fallback.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, count,
                enabled.getAsJsonObject("result").get("count").getAsInt());
    }

    private static void assertNativeStonecuttingOverride(String recipeName, String source,
            String mineralogyResult, String vanillaResult) throws Exception {
        JsonObject wrapper = json(new File(ROOT, "data/minecraft/recipes/" + recipeName + ".json"));
        JsonObject enabled = conditionalRecipe(wrapper, 0);
        JsonObject fallback = conditionalRecipe(wrapper, 1);
        assertEquals(recipeName, "minecraft:stonecutting", enabled.get("type").getAsString());
        assertEquals(recipeName, source,
                enabled.getAsJsonObject("ingredient").get("item").getAsString());
        assertEquals(recipeName, mineralogyResult,
                enabled.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, vanillaResult,
                fallback.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, 2,
                enabled.getAsJsonObject("result").get("count").getAsInt());
        assertFalse(recipeName, enabled.has("count"));
    }

    private static void assertNativeSlabConversion(String recipeName, String source,
            String result) throws Exception {
        JsonObject recipe = json(new File(ROOT,
                "data/mineralogy/recipes/" + recipeName + ".json"));
        assertEquals(recipeName, "minecraft:crafting_shapeless", recipe.get("type").getAsString());
        assertEquals(recipeName, 1, recipe.getAsJsonArray("ingredients").size());
        assertEquals(recipeName, source, recipe.getAsJsonArray("ingredients").get(0)
                .getAsJsonObject().get("item").getAsString());
        assertEquals(recipeName, result, recipe.getAsJsonObject("result").get("id").getAsString());
        assertEquals(recipeName, 1, recipe.getAsJsonObject("result").get("count").getAsInt());
        assertEquals(recipeName, "forge:and",
                recipe.getAsJsonObject("forge:condition").get("type").getAsString());
        assertEquals(recipeName, 2, recipe.getAsJsonObject("forge:condition")
                .getAsJsonArray("values").size());

        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancements/recipes/" + recipeName + ".json"));
        assertEquals(recipeName, recipe.get("forge:condition"), advancement.get("forge:condition"));
        assertEquals(recipeName, source, criterionItem(advancement, "has_rock"));
        assertEquals(recipeName, "mineralogy:" + recipeName,
                advancement.getAsJsonObject("rewards").getAsJsonArray("recipes")
                        .get(0).getAsString());
    }

    private static void assertCompositeRockTag(String name, String baseTag) throws Exception {
        JsonArray values = json(new File(ROOT,
                "data/mineralogy/tags/items/" + name + ".json")).getAsJsonArray("values");
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

    private static JsonObject conditionalRecipe(JsonObject wrapper, int index) {
        return wrapper.getAsJsonArray("recipes").get(index).getAsJsonObject()
                .getAsJsonObject("recipe");
    }

    private static Set<String> stringSet(JsonArray values) {
        Set<String> result = new HashSet<String>();
        for (JsonElement value : values) result.add(value.getAsString());
        return result;
    }

    private static void assertRecipeBookFields(String name, JsonObject recipe) {
        String type = recipe.get("type").getAsString();
        if ("minecraft:crafting_shaped".equals(type)) {
            assertTrue(name, recipe.has("category"));
            assertTrue(name, recipe.has("show_notification"));
            assertTrue(name, recipe.get("show_notification").getAsBoolean());
        } else if ("minecraft:crafting_shapeless".equals(type)) {
            assertTrue(name, recipe.has("category"));
            assertFalse(name, recipe.has("show_notification"));
        } else if ("minecraft:smelting".equals(type)) {
            assertEquals(name, "blocks", recipe.get("category").getAsString());
        }
    }

    private static JsonObject conditionalAdvancement(JsonObject wrapper, int index) {
        return wrapper.getAsJsonArray("advancements").get(index).getAsJsonObject();
    }

    private static void assertConditionalAdvancement(File file, String criterion, String key,
            String enabledValue, String fallbackValue) throws Exception {
        JsonObject wrapper = json(file);
        assertEquals(file.getPath(), 2, wrapper.getAsJsonArray("advancements").size());
        assertEquals(file.getPath(), "mineralogy:config", wrapper.getAsJsonArray("advancements")
                .get(0).getAsJsonObject().getAsJsonObject("forge:condition")
                .get("type").getAsString());
        assertEquals(file.getPath(), "forge:not", wrapper.getAsJsonArray("advancements")
                .get(1).getAsJsonObject().getAsJsonObject("forge:condition")
                .get("type").getAsString());
        for (int branch = 0; branch < 2; branch++) {
            JsonObject advancement = conditionalAdvancement(wrapper, branch);
            JsonObject ingredient = advancement.getAsJsonObject("criteria")
                    .getAsJsonObject(criterion).getAsJsonObject("conditions")
                    .getAsJsonArray("items").get(0).getAsJsonObject();
            String actual = ingredient.get("items").getAsString();
            String expected = branch == 0 ? enabledValue : fallbackValue;
            assertEquals(file.getPath(), "tag".equals(key) ? "#" + expected : expected, actual);
            JsonArray requirements = advancement.getAsJsonArray("requirements");
            assertEquals(file.getPath(), 1, requirements.size());
            assertTrue(file.getPath(), requirements.get(0).isJsonArray());
            assertEquals(file.getPath(), 2, requirements.get(0).getAsJsonArray().size());
        }
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

    @Test
    public void miningTagsUseSupportedOptionalEntryObjects() throws Exception {
        String[] paths = {
                "data/minecraft/tags/blocks/mineable/pickaxe.json",
                "data/minecraft/tags/blocks/needs_iron_tool.json",
                "data/minecraft/tags/blocks/needs_stone_tool.json"
        };
        int[] totals = { 1133, 123, 329 };
        int[] optionalTotals = { 1080, 120, 320 };
        assertMiningTagContracts(paths, totals, optionalTotals);
    }

    private static JsonObject json(File file) throws Exception {
        try (java.io.Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void assertMiningTagContracts(String[] paths, int[] totals,
            int[] optionalTotals) throws Exception {
        for (int index = 0; index < paths.length; index++) {
            JsonObject tag = json(new File(ROOT, paths[index]));
            assertFalse(paths[index], tag.has("optional"));
            JsonArray values = tag.getAsJsonArray("values");
            assertEquals(paths[index], totals[index], values.size());
            Set<String> ids = new HashSet<String>();
            int optionalEntries = 0;
            for (JsonElement value : values) {
                String id;
                if (value.isJsonObject()) {
                    JsonObject entry = value.getAsJsonObject();
                    assertEquals(paths[index], 2, entry.size());
                    assertTrue(paths[index], entry.has("id"));
                    assertTrue(paths[index], entry.has("required"));
                    assertFalse(paths[index], entry.get("required").getAsBoolean());
                    id = entry.get("id").getAsString();
                    assertTrue(paths[index], id.startsWith("mineralogy:"));
                    optionalEntries++;
                } else {
                    id = value.getAsString();
                }
                assertTrue(paths[index] + " duplicate " + id, ids.add(id));
            }
            assertEquals(paths[index], optionalTotals[index], optionalEntries);
        }
        JsonArray pickaxe = json(new File(ROOT, paths[0])).getAsJsonArray("values");
        JsonArray iron = json(new File(ROOT, paths[1])).getAsJsonArray("values");
        assertTrue(pickaxe.contains(JsonParser.parseString("\"mineralogy:basalt\"")));
        assertTrue(iron.contains(JsonParser.parseString("\"mineralogy:basalt\"")));
        assertTrue(hasOptionalTagEntry(pickaxe, "mineralogy:basalt_smooth"));
        assertTrue(hasOptionalTagEntry(iron, "mineralogy:basalt_smooth"));
    }

    private static boolean hasOptionalTagEntry(JsonArray values, String id) {
        for (JsonElement value : values) {
            if (!value.isJsonObject()) continue;
            JsonObject entry = value.getAsJsonObject();
            if (id.equals(entry.get("id").getAsString())
                    && !entry.get("required").getAsBoolean()) return true;
        }
        return false;
    }

    private static void assertGunpowderRecipe(File recipes, String name, String requiredTag) throws Exception {
        JsonObject recipe = json(new File(recipes, name + ".json"));
        assertEquals(name, 3, recipe.getAsJsonArray("ingredients").size());
        assertGunpowderConditions(name, requiredTag, recipe.getAsJsonObject("forge:condition"));
        JsonObject advancement = json(new File(ROOT,
                "data/mineralogy/advancements/recipes/" + name + ".json"));
        assertGunpowderConditions(name + " advancement", requiredTag,
                advancement.getAsJsonObject("forge:condition"));
    }

    private static void assertGunpowderConditions(String name, String requiredTag, JsonObject condition) {
        if (requiredTag != null) {
            assertEquals(name, "forge:and", condition.get("type").getAsString());
            JsonArray conditions = condition.getAsJsonArray("values");
            assertEquals(name, 2, conditions.size());
            assertEquals(name, "mineralogy:config",
                    conditions.get(0).getAsJsonObject().get("type").getAsString());
            JsonObject tagCondition = conditions.get(1).getAsJsonObject();
            assertEquals(name, "forge:not", tagCondition.get("type").getAsString());
            JsonObject value = tagCondition.getAsJsonObject("value");
            assertEquals(name, "forge:tag_empty", value.get("type").getAsString());
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
}
