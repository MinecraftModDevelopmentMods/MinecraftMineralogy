package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.OreDictionary;
import zone.moddev.mc.mineralogy.blocks.Gypsum;
import zone.moddev.mc.mineralogy.init.MineralogyRegistry;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.util.RecipeHelper;

public class RecipeContractTest {
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

        String recipes = source("src/main/java/zone/moddev/mc/mineralogy/init/Recipes.java");
        assertTrue(recipes.contains("new ItemStack(blockGypsum, 1), \"xx\", \"xx\""));
        assertTrue(recipes.contains("new ItemStack(dustGypsum, 4)"));
        assertFalse(recipes.contains("new ItemStack(dustGypsum, 9)"));
    }

    @Test
    public void gunpowderUsesSugarTwoDustAliasesAndExactCharcoal() throws Exception {
        String recipes = source("src/main/java/zone/moddev/mc/mineralogy/init/Recipes.java");
        assertEquals(4, occurrences(recipes, "new ItemStack(Items.GUNPOWDER, 4)"));
        assertTrue(recipes.contains("new ItemStack(Items.COAL, 1, 1)"));
        assertTrue(recipes.contains("Constants.DUST_CARBON"));
        assertTrue(recipes.contains("\"dustCoal\""));
        assertTrue(recipes.contains("new ItemStack(Items.SUGAR)"));
        assertFalse(recipes.contains("new ItemStack(Items.COAL, 1, 0)"));
    }

    @Test
    public void exactNativeSlabRecipeRejectsWrongBlockAndMetadata() {
        MineralogyRegistry.MineralogyRecipeRegistry.clear();
        ShapedRecipes recipe = RecipeHelper.addExactHorizontalThreeRecipe("exact_test_slab",
                new ItemStack(Blocks.STONE_SLAB, 6), new ItemStack(Blocks.STONE, 1, 0));

        assertTrue(recipe.matches(topRow(new ItemStack(Blocks.STONE, 1, 0),
                new ItemStack(Blocks.STONE, 1, 0), new ItemStack(Blocks.STONE, 1, 0)), null));
        assertFalse(recipe.matches(topRow(new ItemStack(Blocks.STONE, 1, 0),
                new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.STONE, 1, 0)), null));
        assertFalse(recipe.matches(topRow(new ItemStack(Blocks.STONE, 1, 1),
                new ItemStack(Blocks.STONE, 1, 1), new ItemStack(Blocks.STONE, 1, 1)), null));
        assertSame(Item.getItemFromBlock(Blocks.STONE_SLAB), recipe.getRecipeOutput().getItem());
        assertEquals(6, recipe.getRecipeOutput().getCount());
    }

    @Test
    public void unavailableAdvancementRecipeIdentityIsInertAndHidden() {
        MineralogyRegistry.MineralogyRecipeRegistry.clear();
        IRecipe recipe = RecipeHelper.addUnavailableRecipe("unavailable_test");

        assertFalse(recipe.matches(shapeless(new ItemStack(Blocks.STONE)), null));
        assertFalse(recipe.canFit(3, 3));
        assertTrue(recipe.getCraftingResult(shapeless(new ItemStack(Blocks.STONE))).isEmpty());
        assertTrue(recipe.getRecipeOutput().isEmpty());
        assertTrue(recipe.isDynamic());
        assertSame(recipe, MineralogyRegistry.MineralogyRecipeRegistry.get("unavailable_test"));
    }

    @Test
    public void completeConstructionFamilyRegistersExactlySeventeenRecipes() {
        MineralogyRegistry.MineralogyRecipeRegistry.clear();
        ConstructionRecipeHelper.registerConvenienceRecipes("test",
                forms(Blocks.STONE, Blocks.STONE_STAIRS, Blocks.STONE_SLAB, Blocks.COBBLESTONE_WALL),
                forms(Blocks.BRICK_BLOCK, Blocks.BRICK_STAIRS, Blocks.PURPUR_SLAB, Blocks.NETHER_BRICK_FENCE),
                forms(Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS, Blocks.STONE_SLAB2, Blocks.OAK_FENCE),
                forms(Blocks.NETHER_BRICK, Blocks.NETHER_BRICK_STAIRS, Blocks.WOODEN_SLAB, Blocks.SPRUCE_FENCE));

        assertEquals(17, MineralogyRegistry.MineralogyRecipeRegistry.size());
    }

    @Test
    public void constructionRecipesRequireMatchingFormsAndAcceptBothSands() {
        MineralogyRegistry.MineralogyRecipeRegistry.clear();
        OreDictionary.registerOre("sand", new ItemStack(Blocks.SAND, 1, OreDictionary.WILDCARD_VALUE));
        ConstructionRecipeHelper.registerConvenienceRecipes("test",
                forms(Blocks.STONE, Blocks.STONE_STAIRS, Blocks.STONE_SLAB, Blocks.COBBLESTONE_WALL),
                forms(Blocks.BRICK_BLOCK, Blocks.BRICK_STAIRS, Blocks.PURPUR_SLAB, Blocks.NETHER_BRICK_FENCE),
                forms(Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS, Blocks.STONE_SLAB2, Blocks.OAK_FENCE),
                forms(Blocks.NETHER_BRICK, Blocks.NETHER_BRICK_STAIRS, Blocks.WOODEN_SLAB, Blocks.SPRUCE_FENCE));

        IRecipe recombine = recipe("test_raw_slab_recombination");
        assertTrue(recombine.matches(shapeless(new ItemStack(Blocks.STONE_SLAB),
                new ItemStack(Blocks.STONE_SLAB)), null));
        assertFalse(recombine.matches(shapeless(new ItemStack(Blocks.STONE_SLAB),
                new ItemStack(Blocks.WOODEN_SLAB)), null));

        IRecipe brickStairs = recipe("test_raw_stairs_to_brick");
        assertTrue(brickStairs.matches(square(Blocks.STONE_STAIRS, Blocks.STONE_STAIRS,
                Blocks.STONE_STAIRS, Blocks.STONE_STAIRS), null));
        assertFalse(brickStairs.matches(square(Blocks.STONE_STAIRS, Blocks.STONE_STAIRS,
                Blocks.STONE_STAIRS, Blocks.QUARTZ_STAIRS), null));
        assertEquals(4, brickStairs.getRecipeOutput().getCount());

        IRecipe polish = recipe("test_brick_block_polishing");
        assertTrue(polish.matches(shapeless(new ItemStack(Blocks.BRICK_BLOCK),
                new ItemStack(Blocks.SAND, 1, 0)), null));
        assertTrue(polish.matches(shapeless(new ItemStack(Blocks.BRICK_BLOCK),
                new ItemStack(Blocks.SAND, 1, 1)), null));
        assertFalse(polish.matches(shapeless(new ItemStack(Blocks.BRICK_BLOCK),
                new ItemStack(Blocks.GRAVEL)), null));
        assertSame(Item.getItemFromBlock(Blocks.NETHER_BRICK), polish.getRecipeOutput().getItem());
    }

    @Test
    public void missingFormsDoNotCreateDanglingRoutes() {
        MineralogyRegistry.MineralogyRecipeRegistry.clear();
        ConstructionRecipeHelper.registerConvenienceRecipes("partial",
                forms(Blocks.STONE, null, null, null),
                forms(Blocks.BRICK_BLOCK, null, null, null),
                forms(Blocks.QUARTZ_BLOCK, null, null, null),
                forms(Blocks.NETHER_BRICK, null, null, null));
        assertEquals(1, MineralogyRegistry.MineralogyRecipeRegistry.size());
        assertTrue(MineralogyRegistry.MineralogyRecipeRegistry.containsKey("partial_brick_block_polishing"));
    }

    @Test
    public void optionalRecipeFamiliesAreGuardedByTheirIndependentPolicies() throws Exception {
        String recipes = source("src/main/java/zone/moddev/mc/mineralogy/init/Recipes.java");
        String blocks = source("src/main/java/zone/moddev/mc/mineralogy/init/Blocks.java");
        String ores = source("src/main/java/zone/moddev/mc/mineralogy/init/Ores.java");
        assertTrue(recipes.contains("contentPolicy().drywallsEnabled()"));
        assertTrue(recipes.contains("contentPolicy().rockSaltLampsEnabled()"));
        assertTrue(recipes.contains("contentPolicy().mineralDustsEnabled()"));
        assertTrue(recipes.contains("contentPolicy().mineralFertilizerEnabled()"));
        assertTrue(blocks.contains("contentPolicy().drywallsEnabled()"));
        assertTrue(ores.contains("contentPolicy().mineralDustsEnabled()"));
    }

    private static ConstructionRecipeHelper.Forms forms(Block full, Block stairs, Block slab, Block wall) {
        return new ConstructionRecipeHelper.Forms(full, stairs, slab, wall);
    }

    private static IRecipe recipe(String name) {
        return MineralogyRegistry.MineralogyRecipeRegistry.get(name);
    }

    private static InventoryCrafting topRow(ItemStack first, ItemStack second, ItemStack third) {
        return grid(first, second, third, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    private static InventoryCrafting square(Block first, Block second, Block third, Block fourth) {
        return grid(new ItemStack(first), new ItemStack(second), ItemStack.EMPTY,
                new ItemStack(third), new ItemStack(fourth), ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    private static InventoryCrafting shapeless(ItemStack... stacks) {
        ItemStack[] contents = new ItemStack[9];
        java.util.Arrays.fill(contents, ItemStack.EMPTY);
        System.arraycopy(stacks, 0, contents, 0, stacks.length);
        return grid(contents);
    }

    private static InventoryCrafting grid(ItemStack... stacks) {
        InventoryCrafting inventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return true;
            }
        }, 3, 3);
        for (int index = 0; index < stacks.length; index++) {
            inventory.setInventorySlotContents(index, stacks[index]);
        }
        return inventory;
    }

    private static String source(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }

    private static int occurrences(String source, String search) {
        int count = 0;
        for (int offset = 0; (offset = source.indexOf(search, offset)) >= 0; offset += search.length()) {
            count++;
        }
        return count;
    }
}
