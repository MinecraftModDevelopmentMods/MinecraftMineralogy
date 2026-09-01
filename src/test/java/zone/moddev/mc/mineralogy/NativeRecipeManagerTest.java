package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import org.junit.BeforeClass;
import org.junit.Test;

/** Exercises generated overrides through Forge conditions and Minecraft's recipe deserializers. */
public class NativeRecipeManagerTest {
    private static final File RECIPE_ROOT = new File("src/main/resources/data/minecraft/recipes");
    private static final File MINERALOGY_RECIPE_ROOT = new File(
            "src/main/resources/data/mineralogy/recipes");
    @BeforeClass
    public static void initializeRegistriesAndConditions() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        CraftingHelper.register(NotCondition.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("minecraft", "item"),
                VanillaIngredientSerializer.INSTANCE);
        MineralogyConfig.registerRecipeConditions();
    }

    @Test
    public void enabledNativeAndLegacyFamilyRepresentativesMatchEveryCoveredVanillaRecipe()
            throws Exception {
        Map<TagKey<Item>, List<Holder<Item>>> previous = snapshotItemTags();
        try {
            installRecipeTags();
            loadConfig(true);
            for (Item rock : ordinaryRockInputs()) {
                for (String recipeName : recipeNames()) {
                    CraftingRecipe recipe = recipe(recipeName);
                    CraftingContainer inventory = inventory(recipeName, rock);
                    assertTrue(recipeName + " with " + Registry.ITEM.getKey(rock),
                            recipe.matches(inventory, null));
                    String expectedOutput = recipeName.startsWith("mossy_cobblestone_from_")
                            ? "minecraft:mossy_cobblestone" : "minecraft:" + recipeName;
                    assertEquals(recipeName, expectedOutput,
                            Registry.ITEM.getKey(recipe.getResultItem().getItem()).toString());
                }
            }
        } finally {
            Registry.ITEM.bindTags(previous);
        }
    }

    @Test
    public void disabledEquivalenceRejectsOrdinaryFamiliesButKeepsVanillaChertAndPumice()
            throws Exception {
        Map<TagKey<Item>, List<Holder<Item>>> previous = snapshotItemTags();
        try {
            installRecipeTags();
            loadConfig(false);
            for (String recipeName : recipeNames()) {
                CraftingRecipe recipe = recipe(recipeName);
                for (Item rock : ordinaryRockInputs()) {
                    assertFalse(recipeName + " with " + Registry.ITEM.getKey(rock),
                            recipe.matches(inventory(recipeName, rock), null));
                }
                assertTrue(recipeName, recipe.matches(inventory(recipeName, Blocks.COBBLESTONE.asItem()), null));
                // Stand-ins for always-tagged Mineralogy chert and pumice.
                assertTrue(recipeName + " with chert stand-in",
                        recipe.matches(inventory(recipeName, Blocks.NETHERRACK.asItem()), null));
                assertTrue(recipeName + " with pumice stand-in",
                        recipe.matches(inventory(recipeName, Blocks.END_STONE.asItem()), null));
            }
            assertTrue(recipe("furnace").matches(inventory("furnace", Blocks.BLACKSTONE.asItem()), null));
            assertTrue(recipe("brewing_stand").matches(
                    inventory("brewing_stand", Blocks.BLACKSTONE.asItem()), null));
            assertTrue(recipe("furnace").matches(
                    inventory("furnace", Blocks.COBBLED_DEEPSLATE.asItem()), null));
            assertTrue(recipe("stone_pickaxe").matches(
                    inventory("stone_pickaxe", Blocks.COBBLED_DEEPSLATE.asItem()), null));
        } finally {
            Registry.ITEM.bindTags(previous);
        }
    }

    @Test
    public void nativeAndMineralogySlabsConvertExactlyOneForOneInBothDirections()
            throws Exception {
        Object[][] bridges = {
                { "andesite_slab", Blocks.ANDESITE_SLAB.asItem() },
                { "andesite_smooth_slab", Blocks.POLISHED_ANDESITE_SLAB.asItem() },
                { "diorite_slab", Blocks.DIORITE_SLAB.asItem() },
                { "diorite_smooth_slab", Blocks.POLISHED_DIORITE_SLAB.asItem() },
                { "granite_slab", Blocks.GRANITE_SLAB.asItem() },
                { "granite_smooth_slab", Blocks.POLISHED_GRANITE_SLAB.asItem() }
        };
        for (Object[] bridge : bridges) {
            assertSlabConversion((String) bridge[0], (Item) bridge[1]);
        }
    }

    private static void assertSlabConversion(String prefix, Item vanilla)
            throws Exception {
        Item standIn = Blocks.COBBLESTONE.asItem();
        CraftingRecipe toVanilla = mineralogyRecipe(prefix + "_to_vanilla", standIn);
        assertTrue(prefix, toVanilla.matches(shapeless(new ItemStack(standIn)), null));
        assertFalse(prefix, toVanilla.matches(shapeless(new ItemStack(vanilla)), null));
        assertEquals(prefix, vanilla, toVanilla.getResultItem().getItem());
        assertEquals(prefix, 1, toVanilla.getResultItem().getCount());

        CraftingRecipe fromVanilla = mineralogyRecipe(prefix + "_from_vanilla", standIn);
        assertTrue(prefix, fromVanilla.matches(shapeless(new ItemStack(vanilla)), null));
        assertFalse(prefix, fromVanilla.matches(shapeless(new ItemStack(standIn)), null));
        assertEquals(prefix, standIn, fromVanilla.getResultItem().getItem());
        assertEquals(prefix, 1, fromVanilla.getResultItem().getCount());
    }

    private static void installRecipeTags() {
        Map<TagKey<Item>, List<Holder<Item>>> tags = snapshotItemTags();
        tags.put(tagKey("forge:cobblestone"), holders(Blocks.COBBLESTONE.asItem(),
                Blocks.NETHERRACK.asItem(), Blocks.END_STONE.asItem()));
        tags.put(tagKey("minecraft:stone_crafting_materials"),
                holders(Blocks.COBBLESTONE.asItem(), Blocks.BLACKSTONE.asItem(),
                        Blocks.COBBLED_DEEPSLATE.asItem(), Blocks.NETHERRACK.asItem(),
                        Blocks.END_STONE.asItem()));
        tags.put(tagKey("minecraft:stone_tool_materials"), holders(Blocks.COBBLESTONE.asItem(),
                Blocks.BLACKSTONE.asItem(), Blocks.COBBLED_DEEPSLATE.asItem(),
                Blocks.NETHERRACK.asItem(), Blocks.END_STONE.asItem()));
        tags.put(tagKey("minecraft:planks"), holders(Blocks.OAK_PLANKS.asItem()));
        tags.put(tagKey("mineralogy:cobblestone_equivalents"),
                holders(Blocks.COBBLESTONE.asItem(), Blocks.NETHERRACK.asItem(), Blocks.END_STONE.asItem(),
                        Blocks.BASALT.asItem(), Blocks.TUFF.asItem(), Blocks.ANDESITE.asItem(),
                        Blocks.DIORITE.asItem(), Blocks.GRANITE.asItem(), Blocks.CALCITE.asItem()));
        tags.put(tagKey("mineralogy:stone_crafting_materials"), holders(Blocks.COBBLESTONE.asItem(),
                Blocks.BLACKSTONE.asItem(), Blocks.COBBLED_DEEPSLATE.asItem(),
                Blocks.NETHERRACK.asItem(), Blocks.END_STONE.asItem(), Blocks.BASALT.asItem(),
                Blocks.TUFF.asItem(), Blocks.ANDESITE.asItem(), Blocks.DIORITE.asItem(),
                Blocks.GRANITE.asItem(), Blocks.CALCITE.asItem()));
        tags.put(tagKey("mineralogy:stone_tool_materials"), holders(Blocks.COBBLESTONE.asItem(),
                Blocks.BLACKSTONE.asItem(), Blocks.COBBLED_DEEPSLATE.asItem(),
                Blocks.NETHERRACK.asItem(), Blocks.END_STONE.asItem(), Blocks.BASALT.asItem(),
                Blocks.TUFF.asItem(), Blocks.ANDESITE.asItem(), Blocks.DIORITE.asItem(),
                Blocks.GRANITE.asItem(), Blocks.CALCITE.asItem()));
        Registry.ITEM.bindTags(tags);
    }

    private static Map<TagKey<Item>, List<Holder<Item>>> snapshotItemTags() {
        Map<TagKey<Item>, List<Holder<Item>>> result = new IdentityHashMap<>();
        Registry.ITEM.getTags().forEach(pair -> {
            List<Holder<Item>> holders = new ArrayList<>();
            pair.getSecond().forEach(holders::add);
            result.put(pair.getFirst(), holders);
        });
        return result;
    }

    private static List<Holder<Item>> holders(Item... items) {
        List<Holder<Item>> result = new ArrayList<>();
        for (Item item : items) {
            ResourceKey<Item> key = Registry.ITEM.getResourceKey(item).get();
            result.add(Registry.ITEM.getHolderOrThrow(key));
        }
        return result;
    }

    private static TagKey<Item> tagKey(String value) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(value));
    }

    private static void loadConfig(boolean enabled) throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-recipe-condition");
        Files.write(directory.resolve(MineralogyConfig.FILE_NAME),
                ("[options]\nCOBBLESTONE_EQUIVILENT = " + enabled + "\n")
                        .getBytes(StandardCharsets.UTF_8));
        MineralogyConfig.load(directory);
    }

    private static CraftingRecipe recipe(String name) throws Exception {
        JsonObject wrapper = json(new File(RECIPE_ROOT, name + ".json"));
        JsonObject selected = null;
        for (JsonElement element : wrapper.getAsJsonArray("recipes")) {
            JsonObject candidate = element.getAsJsonObject();
            if (CraftingHelper.processConditions(candidate.getAsJsonArray("conditions"))) {
                selected = candidate.getAsJsonObject("recipe");
                break;
            }
        }
        assertTrue(name + " has no selected conditional branch", selected != null);
        Recipe<?> deserialized = RecipeManager.fromJson(new ResourceLocation("minecraft", name), selected);
        assertTrue(name, deserialized instanceof CraftingRecipe);
        return (CraftingRecipe) deserialized;
    }

    private static CraftingRecipe mineralogyRecipe(String name, Item mineralogyStandIn)
            throws Exception {
        JsonObject data = json(new File(MINERALOGY_RECIPE_ROOT, name + ".json"));
        String standInId = Registry.ITEM.getKey(mineralogyStandIn).toString();
        JsonObject ingredient = data.getAsJsonArray("ingredients").get(0).getAsJsonObject();
        if (ingredient.get("item").getAsString().startsWith("mineralogy:")) {
            ingredient.addProperty("item", standInId);
        }
        JsonObject result = data.getAsJsonObject("result");
        if (result.get("item").getAsString().startsWith("mineralogy:")) {
            result.addProperty("item", standInId);
        }
        Recipe<?> deserialized = RecipeManager.fromJson(
                new ResourceLocation("mineralogy", name), data);
        assertTrue(name, deserialized instanceof CraftingRecipe);
        return (CraftingRecipe) deserialized;
    }

    private static CraftingContainer inventory(String name, Item rock) {
        if ("mossy_cobblestone_from_vine".equals(name)) {
            return shapeless(new ItemStack(rock), new ItemStack(Blocks.VINE));
        }
        if ("mossy_cobblestone_from_moss_block".equals(name)) {
            return shapeless(new ItemStack(rock), new ItemStack(Blocks.MOSS_BLOCK));
        }
        if ("andesite".equals(name)) {
            return shapeless(new ItemStack(Blocks.DIORITE), new ItemStack(rock));
        }

        String[] pattern;
        Map<Character, Item> ingredients = new LinkedHashMap<Character, Item>();
        ingredients.put('#', rock);
        switch (name) {
            case "furnace": pattern = new String[] { "###", "# #", "###" }; break;
            case "brewing_stand":
                pattern = new String[] { " B ", "###" };
                ingredients.put('B', Items.BLAZE_ROD);
                break;
            case "lever":
                pattern = new String[] { "X", "#" };
                ingredients.put('X', Items.STICK);
                break;
            case "piston":
                pattern = new String[] { "TTT", "#X#", "#R#" };
                ingredients.put('T', Blocks.OAK_PLANKS.asItem());
                ingredients.put('X', Items.IRON_INGOT);
                ingredients.put('R', Items.REDSTONE);
                break;
            case "dispenser":
                pattern = new String[] { "###", "#X#", "#R#" };
                ingredients.put('X', Items.BOW);
                ingredients.put('R', Items.REDSTONE);
                break;
            case "dropper":
                pattern = new String[] { "###", "# #", "#R#" };
                ingredients.put('R', Items.REDSTONE);
                break;
            case "observer":
                pattern = new String[] { "###", "RRQ", "###" };
                ingredients.put('R', Items.REDSTONE);
                ingredients.put('Q', Items.QUARTZ);
                break;
            case "diorite":
                pattern = new String[] { "CQ", "QC" };
                ingredients.put('C', rock);
                ingredients.put('Q', Items.QUARTZ);
                break;
            case "stone_axe": pattern = new String[] { "XX", "X#", " #" }; ingredients.put('X', rock); ingredients.put('#', Items.STICK); break;
            case "stone_hoe": pattern = new String[] { "XX", " #", " #" }; ingredients.put('X', rock); ingredients.put('#', Items.STICK); break;
            case "stone_pickaxe": pattern = new String[] { "XXX", " # ", " # " }; ingredients.put('X', rock); ingredients.put('#', Items.STICK); break;
            case "stone_shovel": pattern = new String[] { "X", "#", "#" }; ingredients.put('X', rock); ingredients.put('#', Items.STICK); break;
            case "stone_sword": pattern = new String[] { "X", "X", "#" }; ingredients.put('X', rock); ingredients.put('#', Items.STICK); break;
            default: throw new IllegalArgumentException(name);
        }
        return shaped(pattern, ingredients);
    }

    private static CraftingContainer shaped(String[] pattern, Map<Character, Item> ingredients) {
        CraftingContainer inventory = new CraftingContainer(dummyContainer(), 3, 3);
        for (int row = 0; row < pattern.length; row++) {
            for (int column = 0; column < pattern[row].length(); column++) {
                Item item = ingredients.get(pattern[row].charAt(column));
                if (item != null) inventory.setItem(row * 3 + column, new ItemStack(item));
            }
        }
        return inventory;
    }

    private static CraftingContainer shapeless(ItemStack... stacks) {
        CraftingContainer inventory = new CraftingContainer(dummyContainer(), 3, 3);
        for (int index = 0; index < stacks.length; index++) inventory.setItem(index, stacks[index]);
        return inventory;
    }

    private static AbstractContainerMenu dummyContainer() {
        return new AbstractContainerMenu(null, -1) {
            @Override
            public boolean stillValid(Player playerIn) {
                return false;
            }
        };
    }

    private static String[] recipeNames() {
        return new String[] { "furnace", "brewing_stand", "lever", "piston", "dispenser",
                "dropper", "observer", "mossy_cobblestone_from_vine",
                "mossy_cobblestone_from_moss_block", "andesite", "diorite",
                "stone_axe", "stone_hoe", "stone_pickaxe", "stone_shovel", "stone_sword" };
    }

    private static Item[] ordinaryRockInputs() {
        return new Item[] { Blocks.BASALT.asItem(), Blocks.TUFF.asItem(),
                Blocks.ANDESITE.asItem(), Blocks.DIORITE.asItem(), Blocks.GRANITE.asItem(),
                Blocks.CALCITE.asItem() };
    }

    private static JsonObject json(File file) throws Exception {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
