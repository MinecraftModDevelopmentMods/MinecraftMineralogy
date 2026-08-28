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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
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
    private static final Container DUMMY_CONTAINER = new Container(null, -1) {
        @Override
        public boolean canInteractWith(PlayerEntity playerIn) {
            return false;
        }
    };

    @BeforeClass
    public static void initializeRegistriesAndConditions() {
        Bootstrap.register();
        CraftingHelper.register(NotCondition.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("minecraft", "item"),
                VanillaIngredientSerializer.INSTANCE);
        MineralogyConfig.registerRecipeConditions();
    }

    @Test
    public void enabledNativeBasaltMatchesEveryCoveredVanillaRecipe() throws Exception {
        ITagCollectionSupplier previous = TagCollectionManager.getManager();
        try {
            installRecipeTags();
            loadConfig(true);
            for (String recipeName : recipeNames()) {
                ICraftingRecipe recipe = recipe(recipeName);
                CraftingInventory inventory = inventory(recipeName, Blocks.BASALT.asItem());
                assertTrue(recipeName, recipe.matches(inventory, null));
                assertEquals(recipeName, "minecraft:" + recipeName,
                        recipe.getRecipeOutput().getItem().getRegistryName().toString());
            }
        } finally {
            TagCollectionManager.setManager(previous);
        }
    }

    @Test
    public void disabledEquivalenceRejectsBasaltButKeepsVanillaMaterials() throws Exception {
        ITagCollectionSupplier previous = TagCollectionManager.getManager();
        try {
            installRecipeTags();
            loadConfig(false);
            for (String recipeName : recipeNames()) {
                ICraftingRecipe recipe = recipe(recipeName);
                assertFalse(recipeName, recipe.matches(inventory(recipeName, Blocks.BASALT.asItem()), null));
                assertTrue(recipeName, recipe.matches(inventory(recipeName, Blocks.COBBLESTONE.asItem()), null));
            }
            assertTrue(recipe("furnace").matches(inventory("furnace", Blocks.BLACKSTONE.asItem()), null));
            assertTrue(recipe("brewing_stand").matches(
                    inventory("brewing_stand", Blocks.BLACKSTONE.asItem()), null));
        } finally {
            TagCollectionManager.setManager(previous);
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
        ICraftingRecipe toVanilla = mineralogyRecipe(prefix + "_to_vanilla", standIn);
        assertTrue(prefix, toVanilla.matches(shapeless(new ItemStack(standIn)), null));
        assertFalse(prefix, toVanilla.matches(shapeless(new ItemStack(vanilla)), null));
        assertEquals(prefix, vanilla, toVanilla.getRecipeOutput().getItem());
        assertEquals(prefix, 1, toVanilla.getRecipeOutput().getCount());

        ICraftingRecipe fromVanilla = mineralogyRecipe(prefix + "_from_vanilla", standIn);
        assertTrue(prefix, fromVanilla.matches(shapeless(new ItemStack(vanilla)), null));
        assertFalse(prefix, fromVanilla.matches(shapeless(new ItemStack(standIn)), null));
        assertEquals(prefix, standIn, fromVanilla.getRecipeOutput().getItem());
        assertEquals(prefix, 1, fromVanilla.getRecipeOutput().getCount());
    }

    private static void installRecipeTags() {
        Map<ResourceLocation, ITag<Item>> tags = new LinkedHashMap<ResourceLocation, ITag<Item>>();
        tags.put(id("forge:cobblestone"), tag(Blocks.COBBLESTONE.asItem()));
        tags.put(id("minecraft:stone_crafting_materials"),
                tag(Blocks.COBBLESTONE.asItem(), Blocks.BLACKSTONE.asItem()));
        tags.put(id("minecraft:stone_tool_materials"), tag(Blocks.COBBLESTONE.asItem()));
        tags.put(id("minecraft:planks"), tag(Blocks.OAK_PLANKS.asItem()));
        tags.put(id("mineralogy:cobblestone_equivalents"),
                tag(Blocks.COBBLESTONE.asItem(), Blocks.BASALT.asItem(), Blocks.DIORITE.asItem()));
        tags.put(id("mineralogy:stone_crafting_materials"), tag(Blocks.COBBLESTONE.asItem(),
                Blocks.BLACKSTONE.asItem(), Blocks.BASALT.asItem(), Blocks.DIORITE.asItem()));
        tags.put(id("mineralogy:stone_tool_materials"),
                tag(Blocks.COBBLESTONE.asItem(), Blocks.BASALT.asItem(), Blocks.DIORITE.asItem()));
        TagCollectionManager.setManager(ITagCollectionSupplier.getTagCollectionSupplier(
                ITagCollection.getEmptyTagCollection(), ITagCollection.getTagCollectionFromMap(tags),
                ITagCollection.getEmptyTagCollection(), ITagCollection.getEmptyTagCollection()));
    }

    private static ITag<Item> tag(Item... items) {
        return ITag.getTagOf(new LinkedHashSet<Item>(Arrays.asList(items)));
    }

    private static ResourceLocation id(String value) {
        return new ResourceLocation(value);
    }

    private static void loadConfig(boolean enabled) throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-recipe-condition");
        Files.write(directory.resolve(MineralogyConfig.FILE_NAME),
                ("[options]\nCOBBLESTONE_EQUIVILENT = " + enabled + "\n")
                        .getBytes(StandardCharsets.UTF_8));
        MineralogyConfig.load(directory);
    }

    private static ICraftingRecipe recipe(String name) throws Exception {
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
        IRecipe<?> deserialized = RecipeManager.deserializeRecipe(new ResourceLocation("minecraft", name), selected);
        assertTrue(name, deserialized instanceof ICraftingRecipe);
        return (ICraftingRecipe) deserialized;
    }

    private static ICraftingRecipe mineralogyRecipe(String name, Item mineralogyStandIn)
            throws Exception {
        JsonObject data = json(new File(MINERALOGY_RECIPE_ROOT, name + ".json"));
        String standInId = mineralogyStandIn.getRegistryName().toString();
        JsonObject ingredient = data.getAsJsonArray("ingredients").get(0).getAsJsonObject();
        if (ingredient.get("item").getAsString().startsWith("mineralogy:")) {
            ingredient.addProperty("item", standInId);
        }
        JsonObject result = data.getAsJsonObject("result");
        if (result.get("item").getAsString().startsWith("mineralogy:")) {
            result.addProperty("item", standInId);
        }
        IRecipe<?> deserialized = RecipeManager.deserializeRecipe(
                new ResourceLocation("mineralogy", name), data);
        assertTrue(name, deserialized instanceof ICraftingRecipe);
        return (ICraftingRecipe) deserialized;
    }

    private static CraftingInventory inventory(String name, Item rock) {
        if ("mossy_cobblestone".equals(name)) {
            return shapeless(new ItemStack(rock), new ItemStack(Blocks.VINE));
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
                ingredients.put('B', net.minecraft.item.Items.BLAZE_ROD);
                break;
            case "lever":
                pattern = new String[] { "X", "#" };
                ingredients.put('X', net.minecraft.item.Items.STICK);
                break;
            case "piston":
                pattern = new String[] { "TTT", "#X#", "#R#" };
                ingredients.put('T', Blocks.OAK_PLANKS.asItem());
                ingredients.put('X', net.minecraft.item.Items.IRON_INGOT);
                ingredients.put('R', net.minecraft.item.Items.REDSTONE);
                break;
            case "dispenser":
                pattern = new String[] { "###", "#X#", "#R#" };
                ingredients.put('X', net.minecraft.item.Items.BOW);
                ingredients.put('R', net.minecraft.item.Items.REDSTONE);
                break;
            case "dropper":
                pattern = new String[] { "###", "# #", "#R#" };
                ingredients.put('R', net.minecraft.item.Items.REDSTONE);
                break;
            case "observer":
                pattern = new String[] { "###", "RRQ", "###" };
                ingredients.put('R', net.minecraft.item.Items.REDSTONE);
                ingredients.put('Q', net.minecraft.item.Items.QUARTZ);
                break;
            case "diorite":
                pattern = new String[] { "CQ", "QC" };
                ingredients.put('C', rock);
                ingredients.put('Q', net.minecraft.item.Items.QUARTZ);
                break;
            case "stone_axe": pattern = new String[] { "XX", "X#", " #" }; ingredients.put('X', rock); ingredients.put('#', net.minecraft.item.Items.STICK); break;
            case "stone_hoe": pattern = new String[] { "XX", " #", " #" }; ingredients.put('X', rock); ingredients.put('#', net.minecraft.item.Items.STICK); break;
            case "stone_pickaxe": pattern = new String[] { "XXX", " # ", " # " }; ingredients.put('X', rock); ingredients.put('#', net.minecraft.item.Items.STICK); break;
            case "stone_shovel": pattern = new String[] { "X", "#", "#" }; ingredients.put('X', rock); ingredients.put('#', net.minecraft.item.Items.STICK); break;
            case "stone_sword": pattern = new String[] { "X", "X", "#" }; ingredients.put('X', rock); ingredients.put('#', net.minecraft.item.Items.STICK); break;
            default: throw new IllegalArgumentException(name);
        }
        return shaped(pattern, ingredients);
    }

    private static CraftingInventory shaped(String[] pattern, Map<Character, Item> ingredients) {
        CraftingInventory inventory = new CraftingInventory(DUMMY_CONTAINER, 3, 3);
        for (int row = 0; row < pattern.length; row++) {
            for (int column = 0; column < pattern[row].length(); column++) {
                Item item = ingredients.get(pattern[row].charAt(column));
                if (item != null) inventory.setInventorySlotContents(row * 3 + column, new ItemStack(item));
            }
        }
        return inventory;
    }

    private static CraftingInventory shapeless(ItemStack... stacks) {
        CraftingInventory inventory = new CraftingInventory(DUMMY_CONTAINER, 3, 3);
        for (int index = 0; index < stacks.length; index++) inventory.setInventorySlotContents(index, stacks[index]);
        return inventory;
    }

    private static String[] recipeNames() {
        return new String[] { "furnace", "brewing_stand", "lever", "piston", "dispenser",
                "dropper", "observer", "mossy_cobblestone", "andesite", "diorite",
                "stone_axe", "stone_hoe", "stone_pickaxe", "stone_shovel", "stone_sword" };
    }

    private static JsonObject json(File file) throws Exception {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
