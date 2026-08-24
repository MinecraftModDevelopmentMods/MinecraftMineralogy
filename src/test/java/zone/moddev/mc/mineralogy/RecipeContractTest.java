package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zone.moddev.mc.mineralogy.blocks.Gypsum;
import zone.moddev.mc.mineralogy.ioc.MinIoC;

public class RecipeContractTest {
    private static final File RECIPE_ROOT = new File("src/main/resources/assets/mineralogy/recipes");
    private static final File ADVANCEMENT_ROOT =
            new File("src/main/resources/assets/mineralogy/advancements/recipes");

    private static final String[] FAMILIES = {
            "andesite", "basalt", "diorite", "granite", "rhyolite", "pegmatite", "diabase",
            "gabbro", "peridotite", "basaltic_glass", "scoria", "tuff", "shale", "conglomerate",
            "dolomite", "limestone", "marble", "siltstone", "slate", "schist", "gneiss",
            "phyllite", "amphibolite", "hornfels", "quartzite", "novaculite", "rock_salt"
    };

    private static final String[] FAMILY_RECIPE_SUFFIXES = {
            "cobblestone", "stairs", "slab", "furnace", "wall",
            "brick", "brick_stairs", "brick_slab", "brick_furnace", "brick_wall",
            "smooth", "smooth_stairs", "smooth_slab", "smooth_furnace", "smooth_wall",
            "smooth_brick", "smooth_brick_stairs", "smooth_brick_slab",
            "smooth_brick_furnace", "smooth_brick_wall",
            "relief_blank", "relief_cross", "relief_hammer", "relief_horizontal", "relief_left",
            "relief_plus", "relief_right", "relief_i", "relief_vertical", "relief_axe",
            "relief_hoe", "relief_pickaxe", "relief_sword",
            "raw_slab_recombination", "brick_slab_recombination",
            "polished_slab_recombination", "polished_brick_slab_recombination",
            "raw_stairs_to_brick", "raw_slabs_to_brick", "raw_walls_to_brick",
            "polished_stairs_to_brick", "polished_slabs_to_brick", "polished_walls_to_brick",
            "raw_stairs_polishing", "raw_slab_polishing", "raw_wall_polishing",
            "brick_stairs_polishing", "brick_slab_polishing", "brick_wall_polishing",
            "brick_block_polishing"
    };

    private static final String[] RELIEFS = {
            "blank", "cross", "hammer", "horizontal", "left", "plus", "right", "i",
            "vertical", "axe", "hoe", "pickaxe", "sword"
    };

    private static final String[] GLOBAL_RECIPE_NAMES = {
            "drywall_black", "drywall_red", "drywall_green", "drywall_brown",
            "drywall_blue", "drywall_purple", "drywall_cyan", "drywall_silver",
            "drywall_gray", "drywall_pink", "drywall_lime", "drywall_yellow",
            "drywall_light_blue", "drywall_magenta", "drywall_orange", "drywall_white",
            "gunpowder_from_sugar", "gunpowder_from_charcoal",
            "gunpowder_from_carbon_dust", "gunpowder_from_coal_dust",
            "mineralfertilizer", "cobblestone", "gypsum", "gypsum_dust",
            "chalk", "chalk_dust", "rock_salt", "rock_salt_dust", "drywall",
            "rocksaltlamp", "rocksaltstreetlamp", "sulfur_block", "sulfur_dust",
            "phosphorous_block", "phosphorous_dust", "nitrate_block", "nitrate_dust"
    };

    @BeforeClass
    public static void registerVanilla() {
        MinecraftTestBootstrap.registerVanilla();
    }

    @Test
    public void gypsumDropsOneToThreeDustAndPacksFourEachWay() throws Exception {
        Item powder = new Item();
        MinIoC.getInstance().register(Item.class, powder, Constants.DUST_GYPSUM, Mineralogy.MODID);
        Gypsum gypsum = new Gypsum();
        Field randomField = Gypsum.class.getDeclaredField("prng");
        randomField.setAccessible(true);
        ((Random) randomField.get(gypsum)).setSeed(123456789L);

        for (int index = 0; index < 64; index++) {
            List<ItemStack> drops = gypsum.getDrops(null, null, gypsum.getDefaultState(), 0);
            assertEquals(1, drops.size());
            assertSame(powder, drops.get(0).getItem());
            assertTrue(drops.get(0).getCount() >= 1);
            assertTrue(drops.get(0).getCount() <= 3);
        }
        assertTrue(gypsum.canSilkHarvest(null, null, gypsum.getDefaultState(), null));

        assertFourDustStoragePair("gypsum", "dustGypsum", "blockGypsum", "gypsum_dust");
        assertFourDustStoragePair("chalk", "dustChalk", "blockChalk", "chalk_dust");
        assertFourDustStoragePair("rock_salt", "dustRock_salt", "blockRocksalt", "rock_salt_dust");
    }

    @Test
    public void gunpowderUsesSugarTwoDustAliasesAndExactCharcoal() throws Exception {
        JsonObject sugar = json("gunpowder_from_sugar");
        JsonObject charcoal = json("gunpowder_from_charcoal");
        JsonObject carbon = json("gunpowder_from_carbon_dust");
        JsonObject coalDust = json("gunpowder_from_coal_dust");

        for (JsonObject recipe : Arrays.asList(sugar, charcoal, carbon, coalDust)) {
            assertEquals("forge:ore_shapeless", recipe.get("type").getAsString());
            assertEquals("minecraft:gunpowder", result(recipe).get("item").getAsString());
            assertEquals(4, result(recipe).get("count").getAsInt());
            assertConfigCondition(recipe, ContentPolicy.ENABLE_MINERAL_DUSTS);
            assertOreIngredient(recipe.getAsJsonArray("ingredients").get(1), "dustNitrate");
            assertOreIngredient(recipe.getAsJsonArray("ingredients").get(2), "dustSulfur");
        }

        assertItemIngredient(sugar.getAsJsonArray("ingredients").get(0), "minecraft:sugar", null);
        assertItemIngredient(charcoal.getAsJsonArray("ingredients").get(0), "minecraft:coal", 1);
        assertOreIngredient(carbon.getAsJsonArray("ingredients").get(0), "dustCarbon");
        assertOreIngredient(coalDust.getAsJsonArray("ingredients").get(0), "dustCoal");
        assertFalse(charcoal.toString().contains("\"data\":0"));
    }

    @Test
    public void allCraftingRecipesAreNativeTargetJson() throws Exception {
        File[] recipes = recipeFiles();
        assertEquals(27 * 50 + 37, recipes.length);
        assertTrue(new File(RECIPE_ROOT, "_factories.json").isFile());
        assertTrue(new File("scripts/generate-recipes.ps1").isFile());

        Set<String> allowedTypes = new HashSet<String>(Arrays.asList(
                "minecraft:crafting_shaped", "minecraft:crafting_shapeless",
                "forge:ore_shaped", "forge:ore_shapeless"));
        for (File file : recipes) {
            JsonObject recipe = json(file);
            assertTrue(file.getName(), allowedTypes.contains(recipe.get("type").getAsString()));
            assertTrue(file.getName(), recipe.has("conditions"));
            assertTrue(file.getName(), recipe.get("conditions").isJsonArray());
            assertTrue(file.getName(), recipe.has("result"));
            validateConditions(file.getName(), recipe.getAsJsonArray("conditions"));
        }

        assertFalse(new File("src/main/java/zone/moddev/mc/mineralogy/ConstructionRecipeHelper.java").exists());
        assertFalse(new File("src/main/java/zone/moddev/mc/mineralogy/init/Recipes.java").exists());
        assertFalse(new File("src/main/java/zone/moddev/mc/mineralogy/util/RecipeHelper.java").exists());

        String production = allJavaSource(new File("src/main/java"));
        assertFalse(production.contains("RecipeHelper"));
        assertFalse(production.contains("ConstructionRecipeHelper"));
        assertFalse(production.contains("MineralogyRecipeRegistry"));
        assertFalse(production.contains("ShapedOreRecipe"));
        assertFalse(production.contains("ShapelessOreRecipe"));
        assertFalse(production.contains("addShapedRecipe"));
        assertFalse(production.contains("addShapelessRecipe"));
        assertFalse(production.contains("Register<IRecipe>"));

        // Forge 1.12 has no target-native JSON smelting loader. These are the
        // only recipe-like registrations intentionally retained in Java.
        assertEquals(2, occurrences(production, "GameRegistry.addSmelting("));
    }

    @Test
    public void everyRockFamilyHasTheCompleteFiftyRecipeMatrix() throws Exception {
        assertEquals(50, FAMILY_RECIPE_SUFFIXES.length);
        Set<String> expected = new HashSet<String>();
        for (String family : FAMILIES) {
            for (String suffix : FAMILY_RECIPE_SUFFIXES) {
                String name = family + "_" + suffix;
                expected.add(name);
                assertTrue(name, new File(RECIPE_ROOT, name + ".json").isFile());
            }
        }
        assertEquals(27 * 50, expected.size());

        Set<String> actualFamilyRecipes = Arrays.stream(recipeFiles())
                .map(file -> file.getName().substring(0, file.getName().length() - 5))
                .filter(name -> Arrays.stream(FAMILIES).anyMatch(
                        family -> name.startsWith(family + "_")))
                .filter(name -> !"rock_salt_dust".equals(name))
                .collect(Collectors.toSet());
        assertEquals(expected, actualFamilyRecipes);
    }

    @Test
    public void globalRecipeInventoryIsExact() {
        assertEquals(37, GLOBAL_RECIPE_NAMES.length);
        Set<String> expected = new HashSet<String>(Arrays.asList(GLOBAL_RECIPE_NAMES));
        Set<String> actual = Arrays.stream(recipeFiles())
                .map(file -> file.getName().substring(0, file.getName().length() - 5))
                .filter(name -> !Arrays.stream(FAMILIES).anyMatch(
                        family -> name.startsWith(family + "_")
                                && !"rock_salt_dust".equals(name)))
                .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    public void exactNativeSlabRecipesPreserveMineralogyAndVanillaSeparation() throws Exception {
        for (String family : FAMILIES) {
            assertExactSlabRecipe(family + "_slab", family, family + "_slab");
            assertExactSlabRecipe(family + "_brick_slab", family + "_brick", family + "_brick_slab");
            assertExactSlabRecipe(family + "_smooth_slab", family + "_smooth", family + "_smooth_slab");
            assertExactSlabRecipe(family + "_smooth_brick_slab",
                    family + "_smooth_brick", family + "_smooth_brick_slab");
        }

        for (File file : recipeFiles()) {
            assertFalse(file.getName(), "minecraft:stone_slab".equals(
                    result(json(file)).get("item").getAsString()));
        }
        String production = allJavaSource(new File("src/main/java"));
        assertFalse(production.contains("ForgeRegistries.RECIPES.getValuesCollection().remove"));
        assertFalse(production.contains("minecraft:stone_slab"));
    }

    @Test
    public void constructionConvenienceMatrixIsExactAndConditional() throws Exception {
        for (String family : FAMILIES) {
            assertSlabRecombination(family, "raw", family + "_slab", family);
            assertSlabRecombination(family, "brick", family + "_brick_slab", family + "_brick");
            assertSlabRecombination(family, "polished",
                    family + "_smooth_slab", family + "_smooth");
            assertSlabRecombination(family, "polished_brick",
                    family + "_smooth_brick_slab", family + "_smooth_brick");

            assertTwoByTwo(family + "_raw_stairs_to_brick",
                    family + "_stairs", family + "_brick_stairs");
            assertTwoByTwo(family + "_raw_slabs_to_brick",
                    family + "_slab", family + "_brick_slab");
            assertTwoByTwo(family + "_raw_walls_to_brick",
                    family + "_wall", family + "_brick_wall");
            assertTwoByTwo(family + "_polished_stairs_to_brick",
                    family + "_smooth_stairs", family + "_smooth_brick_stairs");
            assertTwoByTwo(family + "_polished_slabs_to_brick",
                    family + "_smooth_slab", family + "_smooth_brick_slab");
            assertTwoByTwo(family + "_polished_walls_to_brick",
                    family + "_smooth_wall", family + "_smooth_brick_wall");

            assertSandPolishing(family + "_raw_stairs_polishing",
                    family + "_stairs", family + "_smooth_stairs");
            assertSandPolishing(family + "_raw_slab_polishing",
                    family + "_slab", family + "_smooth_slab");
            assertSandPolishing(family + "_raw_wall_polishing",
                    family + "_wall", family + "_smooth_wall");
            assertSandPolishing(family + "_brick_stairs_polishing",
                    family + "_brick_stairs", family + "_smooth_brick_stairs");
            assertSandPolishing(family + "_brick_slab_polishing",
                    family + "_brick_slab", family + "_smooth_brick_slab");
            assertSandPolishing(family + "_brick_wall_polishing",
                    family + "_brick_wall", family + "_smooth_brick_wall");
            assertSandPolishing(family + "_brick_block_polishing",
                    family + "_brick", family + "_smooth_brick");
        }
    }

    @Test
    public void reliefRecipesMatchTheExactOneTenBlankFirstContract() throws Exception {
        for (String family : FAMILIES) {
            String blank = family + "_relief_blank";
            JsonObject blankRecipe = json(blank);
            assertEquals(blank, "minecraft:crafting_shaped", blankRecipe.get("type").getAsString());
            assertEquals(blank, Arrays.asList("xxx", "xxx", "xxx"),
                    strings(blankRecipe.getAsJsonArray("pattern")));
            assertItemIngredient(blankRecipe.getAsJsonObject("key").get("x"),
                    "mineralogy:" + family + "_smooth", null);
            assertEquals(blank, "mineralogy:" + blank,
                    result(blankRecipe).get("item").getAsString());
            assertEquals(blank, 16, result(blankRecipe).get("count").getAsInt());

            assertReliefShape(family, "cross", Arrays.asList("x x", "   ", "x x"), 4);
            assertReliefShape(family, "horizontal", Arrays.asList("xxx"), 3);
            assertReliefShape(family, "left", Arrays.asList("x  ", " x ", "  x"), 3);
            assertReliefShape(family, "plus", Arrays.asList(" x ", "xxx", " x "), 5);
            assertReliefShape(family, "i", Arrays.asList("xxx", " x ", "xxx"), 7);
            assertReliefShape(family, "vertical", Arrays.asList("x", "x", "x"), 3);

            String hammer = family + "_relief_hammer";
            JsonObject hammerRecipe = json(hammer);
            assertEquals(hammer, "minecraft:crafting_shaped",
                    hammerRecipe.get("type").getAsString());
            assertEquals(hammer, Arrays.asList("zxz", "zyz", "zzz"),
                    strings(hammerRecipe.getAsJsonArray("pattern")));
            JsonObject hammerKey = hammerRecipe.getAsJsonObject("key");
            assertItemIngredient(hammerKey.get("x"), "mineralogy:" + family + "_smooth", null);
            assertItemIngredient(hammerKey.get("y"), "minecraft:stick", null);
            assertItemIngredient(hammerKey.get("z"), "mineralogy:" + blank, null);
            assertEquals(hammer, 7, result(hammerRecipe).get("count").getAsInt());

            String right = family + "_relief_right";
            JsonObject rightRecipe = json(right);
            assertEquals(right, "minecraft:crafting_shapeless",
                    rightRecipe.get("type").getAsString());
            assertEquals(right, 2, rightRecipe.getAsJsonArray("ingredients").size());
            for (JsonElement ingredient : rightRecipe.getAsJsonArray("ingredients")) {
                assertItemIngredient(ingredient, "mineralogy:" + family + "_relief_left", null);
            }
            assertEquals(right, 2, result(rightRecipe).get("count").getAsInt());

            for (String tool : new String[] { "axe", "hoe", "pickaxe", "sword" }) {
                String name = family + "_relief_" + tool;
                JsonObject recipe = json(name);
                assertEquals(name, "minecraft:crafting_shapeless",
                        recipe.get("type").getAsString());
                JsonArray ingredients = recipe.getAsJsonArray("ingredients");
                assertEquals(name, 9, ingredients.size());
                for (int index = 0; index < 8; index++) {
                    assertItemIngredient(ingredients.get(index), "mineralogy:" + blank, null);
                }
                assertItemIngredient(ingredients.get(8), "minecraft:stone_" + tool, 0);
                assertEquals(name, 8, result(recipe).get("count").getAsInt());
            }
        }
    }

    @Test
    public void optionalRecipeFamiliesUseIndependentForgeConditions() throws Exception {
        assertConfigCondition(json("drywall"), ContentPolicy.ENABLE_DRYWALLS);
        for (String color : new String[] {
                "black", "red", "green", "brown", "blue", "purple", "cyan", "silver",
                "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white"
        }) {
            assertConfigCondition(json("drywall_" + color), ContentPolicy.ENABLE_DRYWALLS);
        }

        assertConfigCondition(json("rocksaltlamp"), ContentPolicy.ENABLE_ROCK_SALT_LAMPS);
        assertConfigCondition(json("rocksaltstreetlamp"), ContentPolicy.ENABLE_ROCK_SALT_LAMPS);
        assertConfigCondition(json("mineralfertilizer"), ContentPolicy.ENABLE_MINERAL_FERTILIZER);

        for (String name : new String[] {
                "gunpowder_from_sugar", "gunpowder_from_charcoal",
                "gunpowder_from_carbon_dust", "gunpowder_from_coal_dust",
                "sulfur_block", "sulfur_dust", "phosphorous_block", "phosphorous_dust",
                "nitrate_block", "nitrate_dust"
        }) {
            assertConfigCondition(json(name), ContentPolicy.ENABLE_MINERAL_DUSTS);
        }

        JsonObject factories = json(new File(RECIPE_ROOT, "_factories.json"));
        assertEquals("zone.moddev.mc.mineralogy.recipe.ConfigConditionFactory",
                factories.getAsJsonObject("conditions").get("config").getAsString());
        byte[] recipeFactories = Files.readAllBytes(new File(RECIPE_ROOT, "_factories.json").toPath());
        byte[] advancementFactories = Files.readAllBytes(
                new File(ADVANCEMENT_ROOT.getParentFile(), "_factories.json").toPath());
        assertTrue(Arrays.equals(recipeFactories, advancementFactories));
    }

    @Test
    public void everyRecipeHasAConditionMatchedAdvancement() throws Exception {
        File[] advancements = ADVANCEMENT_ROOT.listFiles(
                (directory, name) -> name.endsWith(".json") && !name.startsWith("_"));
        assertTrue(ADVANCEMENT_ROOT.isDirectory());
        assertEquals(recipeFiles().length, advancements == null ? 0 : advancements.length);

        Set<String> rewardedRecipes = new HashSet<String>();
        for (File file : advancements) {
            JsonObject advancement = json(file);
            JsonArray rewards = advancement.getAsJsonObject("rewards").getAsJsonArray("recipes");
            assertEquals(file.getName(), 1, rewards.size());
            String recipeId = rewards.get(0).getAsString();
            assertTrue(file.getName(), recipeId.startsWith("mineralogy:"));
            assertTrue(file.getName(), rewardedRecipes.add(recipeId));
            JsonObject recipe = json(recipeId.substring("mineralogy:".length()));
            assertEquals(file.getName(), recipe.getAsJsonArray("conditions"),
                    advancement.getAsJsonArray("conditions"));
        }
        assertEquals(recipeFiles().length, rewardedRecipes.size());
    }

    @Test
    public void recipeAdvancementsUseDirectInputsWithoutRecursiveRecipeUnlocks() throws Exception {
        for (File file : advancementFiles()) {
            JsonObject advancement = json(file);
            JsonObject criteria = advancement.getAsJsonObject("criteria");
            assertFalse(file.getName(), criteria.has("has_material_recipe"));

            String recipeName = file.getName().substring(0, file.getName().length() - 5);
            JsonObject ownRecipe = criteria.getAsJsonObject("has_the_recipe");
            assertNotNull(file.getName(), ownRecipe);
            assertEquals(file.getName(), "mineralogy:" + recipeName,
                    ownRecipe.getAsJsonObject("conditions").get("recipe").getAsString());
            assertNotNull(file.getName(), criteria.getAsJsonObject("has_rock"));
            if (!criteria.has("has_sand")) {
                JsonArray requirements = advancement.getAsJsonArray("requirements");
                assertEquals(file.getName(), 1, requirements.size());
                assertEquals(file.getName(), Arrays.asList("has_the_recipe", "has_rock"),
                        strings(requirements.get(0).getAsJsonArray()));
            }
        }

        for (String family : FAMILIES) {
            assertSourceUnlock(family + "_brick_stairs", family + "_brick");
            assertSourceUnlock(family + "_brick_slab", family + "_brick");
            assertSourceUnlock(family + "_brick_furnace", family + "_brick");
            assertSourceUnlock(family + "_brick_wall", family + "_brick");

            assertSourceUnlock(family + "_smooth_stairs", family + "_smooth");
            assertSourceUnlock(family + "_smooth_slab", family + "_smooth");
            assertSourceUnlock(family + "_smooth_furnace", family + "_smooth");
            assertSourceUnlock(family + "_smooth_wall", family + "_smooth");
            assertSourceUnlock(family + "_smooth_brick", family + "_smooth");
            assertSourceUnlock(family + "_relief_blank", family + "_smooth");
            for (String relief : RELIEFS) {
                if (!"blank".equals(relief) && !"right".equals(relief)) {
                    assertSourceUnlock(family + "_relief_" + relief, family + "_relief_blank");
                }
            }
            assertSourceUnlock(family + "_relief_right", family + "_relief_left");

            assertSourceUnlock(family + "_smooth_brick_stairs", family + "_smooth_brick");
            assertSourceUnlock(family + "_smooth_brick_slab", family + "_smooth_brick");
            assertSourceUnlock(family + "_smooth_brick_furnace", family + "_smooth_brick");
            assertSourceUnlock(family + "_smooth_brick_wall", family + "_smooth_brick");

            assertSourceUnlock(family + "_raw_slab_recombination", family + "_slab");
            assertSourceUnlock(family + "_polished_slab_recombination", family + "_smooth_slab");
            assertSourceUnlock(family + "_raw_stairs_polishing", family + "_stairs");
            assertSourceUnlock(family + "_brick_block_polishing", family + "_brick");
        }
    }

    @Test
    public void polishingUnlocksRequireTheirSourceAndAcceptedSand() throws Exception {
        for (String family : FAMILIES) {
            assertSandGatedUnlock(family + "_smooth", family, false);
            for (String finish : new String[] { "raw", "brick" }) {
                for (String shape : new String[] { "stairs", "slab", "wall" }) {
                    String source = family + ("raw".equals(finish) ? "" : "_brick") + "_" + shape;
                    assertSandGatedUnlock(family + "_" + finish + "_" + shape + "_polishing",
                            source, true);
                }
            }
            assertSandGatedUnlock(family + "_brick_block_polishing", family + "_brick", true);
        }
    }

    private static void assertFourDustStoragePair(String name, String dustOre,
            String blockOre, String dustItem) throws Exception {
        JsonObject pack = json(name);
        assertEquals("forge:ore_shaped", pack.get("type").getAsString());
        assertEquals(Arrays.asList("xx", "xx"), strings(pack.getAsJsonArray("pattern")));
        assertOreIngredient(pack.getAsJsonObject("key").get("x"), dustOre);
        assertEquals(1, result(pack).get("count").getAsInt());

        JsonObject unpack = json(name + "_dust");
        assertEquals("forge:ore_shapeless", unpack.get("type").getAsString());
        assertEquals(1, unpack.getAsJsonArray("ingredients").size());
        assertOreIngredient(unpack.getAsJsonArray("ingredients").get(0), blockOre);
        assertEquals("mineralogy:" + dustItem, result(unpack).get("item").getAsString());
        assertEquals(4, result(unpack).get("count").getAsInt());
    }

    private static void assertSourceUnlock(String advancementName,
            String sourceItemName) throws Exception {
        JsonObject advancement = json(new File(ADVANCEMENT_ROOT, advancementName + ".json"));
        JsonObject inventory = advancement.getAsJsonObject("criteria").getAsJsonObject("has_rock");
        assertNotNull(advancementName, inventory);
        assertEquals(advancementName, "minecraft:inventory_changed",
                inventory.get("trigger").getAsString());
        JsonArray items = inventory.getAsJsonObject("conditions").getAsJsonArray("items");
        assertEquals(advancementName, 1, items.size());
        assertEquals(advancementName, "mineralogy:" + sourceItemName,
                items.get(0).getAsJsonObject().get("item").getAsString());
    }

    private static void assertSandGatedUnlock(String advancementName,
            String sourceItemName, boolean acceptsRedSand) throws Exception {
        JsonObject advancement = json(new File(ADVANCEMENT_ROOT, advancementName + ".json"));
        JsonObject criteria = advancement.getAsJsonObject("criteria");
        assertSourceUnlock(advancementName, sourceItemName);
        assertInventoryItem(criteria.getAsJsonObject("has_sand"), "minecraft:sand", 0);
        assertEquals(advancementName, acceptsRedSand, criteria.has("has_red_sand"));
        if (acceptsRedSand) {
            assertInventoryItem(criteria.getAsJsonObject("has_red_sand"), "minecraft:sand", 1);
        }

        JsonArray requirements = advancement.getAsJsonArray("requirements");
        assertEquals(advancementName, 2, requirements.size());
        assertEquals(advancementName, Arrays.asList("has_the_recipe", "has_rock"),
                strings(requirements.get(0).getAsJsonArray()));
        List<String> sandAlternatives = strings(requirements.get(1).getAsJsonArray());
        assertEquals(advancementName,
                acceptsRedSand
                        ? Arrays.asList("has_the_recipe", "has_sand", "has_red_sand")
                        : Arrays.asList("has_the_recipe", "has_sand"),
                sandAlternatives);
    }

    private static void assertInventoryItem(JsonObject criterion, String expectedItem, int data) {
        assertNotNull(expectedItem, criterion);
        assertEquals(expectedItem, "minecraft:inventory_changed", criterion.get("trigger").getAsString());
        JsonArray items = criterion.getAsJsonObject("conditions").getAsJsonArray("items");
        assertEquals(expectedItem, 1, items.size());
        assertEquals(expectedItem, expectedItem,
                items.get(0).getAsJsonObject().get("item").getAsString());
        assertEquals(expectedItem, data,
                items.get(0).getAsJsonObject().get("data").getAsInt());
    }

    private static void assertReliefShape(String family, String relief,
            List<String> pattern, int count) throws Exception {
        String name = family + "_relief_" + relief;
        JsonObject recipe = json(name);
        assertEquals(name, "minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(name, pattern, strings(recipe.getAsJsonArray("pattern")));
        assertItemIngredient(recipe.getAsJsonObject("key").get("x"),
                "mineralogy:" + family + "_relief_blank", null);
        assertEquals(name, "mineralogy:" + name, result(recipe).get("item").getAsString());
        assertEquals(name, count, result(recipe).get("count").getAsInt());
    }

    private static void assertExactSlabRecipe(String name, String input, String output) throws Exception {
        JsonObject recipe = json(name);
        assertEquals(name, "minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(name, Arrays.asList("xxx"), strings(recipe.getAsJsonArray("pattern")));
        JsonObject ingredient = recipe.getAsJsonObject("key").getAsJsonObject("x");
        assertEquals(name, "mineralogy:" + input, ingredient.get("item").getAsString());
        assertFalse(name, ingredient.has("type"));
        assertFalse(name, ingredient.has("data"));
        assertEquals(name, "mineralogy:" + output, result(recipe).get("item").getAsString());
        assertEquals(name, 6, result(recipe).get("count").getAsInt());
        assertItemExistsCondition(recipe, "mineralogy:" + output);
    }

    private static void assertSlabRecombination(String family, String finish,
            String slab, String block) throws Exception {
        String name = family + "_" + finish + "_slab_recombination";
        JsonObject recipe = json(name);
        assertEquals(name, "minecraft:crafting_shapeless", recipe.get("type").getAsString());
        JsonArray ingredients = recipe.getAsJsonArray("ingredients");
        assertEquals(name, 2, ingredients.size());
        assertItemIngredient(ingredients.get(0), "mineralogy:" + slab, null);
        assertItemIngredient(ingredients.get(1), "mineralogy:" + slab, null);
        assertEquals(name, "mineralogy:" + block, result(recipe).get("item").getAsString());
        assertEquals(name, 1, result(recipe).get("count").getAsInt());
        assertItemExistsCondition(recipe, "mineralogy:" + slab);
        assertItemExistsCondition(recipe, "mineralogy:" + block);
    }

    private static void assertTwoByTwo(String name, String input, String output) throws Exception {
        JsonObject recipe = json(name);
        assertEquals(name, "minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(name, Arrays.asList("xx", "xx"), strings(recipe.getAsJsonArray("pattern")));
        assertItemIngredient(recipe.getAsJsonObject("key").get("x"), "mineralogy:" + input, null);
        assertEquals(name, "mineralogy:" + output, result(recipe).get("item").getAsString());
        assertEquals(name, 4, result(recipe).get("count").getAsInt());
        assertItemExistsCondition(recipe, "mineralogy:" + input);
        assertItemExistsCondition(recipe, "mineralogy:" + output);
    }

    private static void assertSandPolishing(String name, String input, String output) throws Exception {
        JsonObject recipe = json(name);
        assertEquals(name, "forge:ore_shapeless", recipe.get("type").getAsString());
        JsonArray ingredients = recipe.getAsJsonArray("ingredients");
        assertEquals(name, 2, ingredients.size());
        assertItemIngredient(ingredients.get(0), "mineralogy:" + input, null);
        assertOreIngredient(ingredients.get(1), "sand");
        assertEquals(name, "mineralogy:" + output, result(recipe).get("item").getAsString());
        assertEquals(name, 1, result(recipe).get("count").getAsInt());
        assertItemExistsCondition(recipe, "mineralogy:" + input);
        assertItemExistsCondition(recipe, "mineralogy:" + output);
    }

    private static void assertItemIngredient(JsonElement element, String item, Integer data) {
        JsonObject ingredient = element.getAsJsonObject();
        assertEquals(item, ingredient.get("item").getAsString());
        if (data == null) {
            assertFalse(item, ingredient.has("data"));
        } else {
            assertEquals(item, data.intValue(), ingredient.get("data").getAsInt());
        }
    }

    private static void assertOreIngredient(JsonElement element, String ore) {
        JsonObject ingredient = element.getAsJsonObject();
        assertEquals(ore, "forge:ore_dict", ingredient.get("type").getAsString());
        assertEquals(ore, ore, ingredient.get("ore").getAsString());
    }

    private static void assertConfigCondition(JsonObject recipe, String flag) {
        JsonArray conditions = recipe.getAsJsonArray("conditions");
        for (JsonElement element : conditions) {
            JsonObject condition = element.getAsJsonObject();
            if ("mineralogy:config".equals(condition.get("type").getAsString())
                    && flag.equals(condition.get("flag").getAsString())) {
                return;
            }
        }
        throw new AssertionError("Missing Mineralogy config condition " + flag + " in " + recipe);
    }

    private static void assertItemExistsCondition(JsonObject recipe, String item) {
        for (JsonElement element : recipe.getAsJsonArray("conditions")) {
            JsonObject condition = element.getAsJsonObject();
            if ("minecraft:item_exists".equals(condition.get("type").getAsString())
                    && item.equals(condition.get("item").getAsString())) {
                return;
            }
        }
        throw new AssertionError("Missing item-exists condition for " + item + " in " + recipe);
    }

    private static void validateConditions(String name, JsonArray conditions) {
        for (JsonElement element : conditions) {
            assertTrue(name, element.isJsonObject());
            JsonObject condition = element.getAsJsonObject();
            String type = condition.get("type").getAsString();
            assertTrue(name, "minecraft:item_exists".equals(type) || "mineralogy:config".equals(type));
            if ("minecraft:item_exists".equals(type)) {
                assertFalse(name, condition.get("item").getAsString().trim().isEmpty());
            } else {
                assertTrue(name, Arrays.asList(
                        ContentPolicy.ENABLE_DRYWALLS,
                        ContentPolicy.ENABLE_ROCK_SALT_LAMPS,
                        ContentPolicy.ENABLE_MINERAL_DUSTS,
                        ContentPolicy.ENABLE_MINERAL_FERTILIZER)
                        .contains(condition.get("flag").getAsString()));
            }
        }
    }

    private static JsonObject result(JsonObject recipe) {
        return recipe.getAsJsonObject("result");
    }

    private static List<String> strings(JsonArray array) {
        java.util.ArrayList<String> strings = new java.util.ArrayList<String>();
        for (JsonElement element : array) {
            strings.add(element.getAsString());
        }
        return strings;
    }

    private static File[] recipeFiles() {
        File[] files = RECIPE_ROOT.listFiles(
                (directory, name) -> name.endsWith(".json") && !name.startsWith("_"));
        assertTrue(RECIPE_ROOT.isDirectory());
        return files == null ? new File[0] : files;
    }

    private static File[] advancementFiles() {
        File[] files = ADVANCEMENT_ROOT.listFiles(
                (directory, name) -> name.endsWith(".json") && !name.startsWith("_"));
        assertTrue(ADVANCEMENT_ROOT.isDirectory());
        return files == null ? new File[0] : files;
    }

    private static JsonObject json(String recipeName) throws Exception {
        return json(new File(RECIPE_ROOT, recipeName + ".json"));
    }

    private static JsonObject json(File file) throws Exception {
        assertTrue(file.getPath(), file.isFile());
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private static String allJavaSource(File root) throws Exception {
        try (Stream<Path> paths = Files.walk(root.toPath())) {
            StringBuilder source = new StringBuilder();
            for (Path path : paths.filter(value -> value.toString().endsWith(".java"))
                    .collect(Collectors.toList())) {
                source.append(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
            }
            return source.toString();
        }
    }

    private static int occurrences(String source, String search) {
        int count = 0;
        for (int offset = 0; (offset = source.indexOf(search, offset)) >= 0; offset += search.length()) {
            count++;
        }
        return count;
    }
}
