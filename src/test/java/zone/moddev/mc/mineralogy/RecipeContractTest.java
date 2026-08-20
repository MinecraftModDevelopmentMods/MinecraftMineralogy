package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import zone.moddev.mc.mineralogy.blocks.Gypsum;

public class RecipeContractTest {
    @Test
    public void gypsumKeepsHistoricalMiningAndFourDustConversion() throws Exception {
        Item originalPowder = Mineralogy.gypsumPowder;
        try {
            Mineralogy.gypsumPowder = new Item();
            Gypsum gypsum = new Gypsum();
            Field randomField = Gypsum.class.getDeclaredField("prng");
            randomField.setAccessible(true);
            ((Random) randomField.get(gypsum)).setSeed(123456789L);

            for (int i = 0; i < 64; i++) {
                List<ItemStack> drops = gypsum.getDrops(null, null, gypsum.getDefaultState(), 0);
                assertEquals(1, drops.size());
                assertSame(Mineralogy.gypsumPowder, drops.get(0).getItem());
                assertTrue(drops.get(0).stackSize >= 1);
                assertTrue(drops.get(0).stackSize <= 3);
            }
            assertTrue(gypsum.canSilkHarvest(null, null, gypsum.getDefaultState(), null));
        } finally {
            Mineralogy.gypsumPowder = originalPowder;
        }

        String source = mineralogySource();
        assertTrue(source.contains("new ItemStack(gypsumPowder, 4), blockGypsum"));
        assertTrue(source.contains("new ItemStack(blockGypsum), \"xx\", \"xx\", 'x', dustGypsum"));
        assertFalse(source.contains("new ItemStack(gypsumPowder, 9)"));
        assertTrue(source.contains("new ItemStack(chalkPowder, 4), blockChalk"));
        assertTrue(source.contains("new ItemStack(saltPowder, 4), blockSalt"));
    }

    @Test
    public void gunpowderKeepsBothDustAliasesAndExactCharcoalFallback() throws Exception {
        String source = mineralogySource();

        assertEquals(4, countOccurrences(source, "new ItemStack(Items.GUNPOWDER, 4)"));
        assertTrue(source.contains("new ItemStack(Items.COAL,1,1), dustNitrate, dustSulfur"));
        assertTrue(source.contains("dustCarbon, dustNitrate, dustSulfur"));
        assertTrue(source.contains("dustCoal, dustNitrate, dustSulfur"));
        assertTrue(source.contains("Items.SUGAR, dustNitrate, dustSulfur"));
        assertFalse(source.contains("new ItemStack(Items.COAL,1,0), dustNitrate, dustSulfur"));
    }

    @Test
    public void vanillaStoneSlabRecipeRemainsExactAndUnmodified() throws Exception {
        String source = mineralogySource();
        assertFalse(source.contains("CraftingManager.getInstance().getRecipeList()"));
        assertFalse(source.contains("remove default stone slab recipe"));

        ItemStack vanillaResult = CraftingManager.getInstance().findMatchingRecipe(
                topRow(new ItemStack(Blocks.STONE), new ItemStack(Blocks.STONE),
                        new ItemStack(Blocks.STONE)), null);
        assertSame(Item.getItemFromBlock(Blocks.STONE_SLAB), vanillaResult.getItem());
        assertEquals(0, vanillaResult.getItemDamage());
        assertEquals(6, vanillaResult.stackSize);

        OreDictionary.registerOre("stone", Blocks.NETHERRACK);
        assertNull(CraftingManager.getInstance().findMatchingRecipe(
                topRow(new ItemStack(Blocks.NETHERRACK), new ItemStack(Blocks.NETHERRACK),
                        new ItemStack(Blocks.NETHERRACK)), null));
    }

    @Test
    public void forgeGeneralizedCobblestoneRecipeAcceptsARegisteredRock() {
        OreDictionary.registerOre("cobblestone", Blocks.NETHERRACK);

        ItemStack result = CraftingManager.getInstance().findMatchingRecipe(
                grid(new ItemStack(Items.STICK), null, null,
                        new ItemStack(Blocks.NETHERRACK), null, null,
                        null, null, null), null);

        assertNotNull(result);
        assertSame(Item.getItemFromBlock(Blocks.LEVER), result.getItem());
        assertEquals(1, result.stackSize);
    }

    @Test
    public void everySlabFinishUsesAnExactNativeRecipe() throws Exception {
        String source = mineralogySource();

        assertEquals(4, countOccurrences(source, "GameRegistry.addShapedRecipe(new ItemStack("));
        assertTrue(source.contains("new ItemStack(rockSlab, 6),\n                    \"xxx\", 'x', new ItemStack(rock, 1, 0)"));
        assertTrue(source.contains("new ItemStack(brickSlab, 6),\n                        \"xxx\", 'x', new ItemStack(brick, 1, 0)"));
        assertTrue(source.contains("new ItemStack(smoothSlab, 6),\n                        \"xxx\", 'x', new ItemStack(smooth, 1, 0)"));
        assertTrue(source.contains("new ItemStack(smoothBrickSlab, 6),\n                            \"xxx\", 'x', new ItemStack(smoothBrick, 1, 0)"));
        assertFalse(source.contains("new ShapedOreRecipe(new ItemStack(rockSlab, 6)"));
        assertFalse(source.contains("new ShapedOreRecipe(new ItemStack(brickSlab, 6)"));
        assertFalse(source.contains("new ShapedOreRecipe(new ItemStack(smoothSlab, 6)"));
        assertFalse(source.contains("new ShapedOreRecipe(new ItemStack(smoothBrickSlab, 6)"));

        assertExactSlabRecipe(Blocks.NETHERRACK, Blocks.STONE_SLAB,
                Blocks.BRICK_BLOCK);
        assertExactSlabRecipe(Blocks.BRICK_BLOCK, Blocks.PURPUR_SLAB,
                Blocks.QUARTZ_BLOCK);
        assertExactSlabRecipe(Blocks.QUARTZ_BLOCK, Blocks.STONE_SLAB2,
                Blocks.NETHER_BRICK);
        assertExactSlabRecipe(Blocks.NETHER_BRICK, Blocks.WOODEN_SLAB,
                Blocks.NETHERRACK);
    }

    @Test
    public void exactMineralogySlabRecipeSortsBeforeGeneralizedStoneRecipe() {
        String generalizedStone = "stoneRecipeContractSlab";
        OreDictionary.registerOre(generalizedStone, Blocks.NETHERRACK);

        IRecipe generalizedRecipe = new ShapedOreRecipe(
                new ItemStack(Blocks.STONE_SLAB, 6, 0),
                "xxx", 'x', generalizedStone);
        IRecipe exactRecipe = exactSlabRecipe(Blocks.NETHERRACK,
                Blocks.NETHER_BRICK);
        List<IRecipe> recipes = new ArrayList<IRecipe>();
        recipes.add(generalizedRecipe);
        recipes.add(exactRecipe);

        RecipeSorter.sortCraftManager();
        Collections.sort(recipes, RecipeSorter.INSTANCE);

        assertSame(exactRecipe, recipes.get(0));
        assertTrue(generalizedRecipe.matches(topRow(new ItemStack(Blocks.NETHERRACK),
                new ItemStack(Blocks.NETHERRACK), new ItemStack(Blocks.NETHERRACK)), null));
        assertSame(Item.getItemFromBlock(Blocks.NETHER_BRICK),
                firstMatchingRecipe(recipes, topRow(new ItemStack(Blocks.NETHERRACK),
                        new ItemStack(Blocks.NETHERRACK),
                        new ItemStack(Blocks.NETHERRACK))).getRecipeOutput().getItem());
    }

    @Test
    public void completeConstructionFamilyCreatesOnlyTheSeventeenRequestedRecipes() {
        List<IRecipe> recipes = ConstructionRecipeHelper.createConvenienceRecipes(
                forms(Blocks.STONE, Blocks.STONE_STAIRS, Blocks.STONE_SLAB,
                        Blocks.COBBLESTONE_WALL),
                forms(Blocks.BRICK_BLOCK, Blocks.BRICK_STAIRS, Blocks.PURPUR_SLAB,
                        Blocks.NETHER_BRICK_FENCE),
                forms(Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS, Blocks.STONE_SLAB2,
                        Blocks.OAK_FENCE),
                forms(Blocks.NETHER_BRICK, Blocks.NETHER_BRICK_STAIRS, Blocks.WOODEN_SLAB,
                        Blocks.SPRUCE_FENCE));

        assertEquals(17, recipes.size());
    }

    @Test
    public void missingConfigControlledFormsDoNotCreateDanglingRecipes() {
        ConstructionRecipeHelper.Forms raw = forms(null, Blocks.STONE_STAIRS, null, null);
        ConstructionRecipeHelper.Forms brick = forms(null, null, null, null);

        assertTrue(ConstructionRecipeHelper.createConvenienceRecipes(
                raw, brick, null, null).isEmpty());
    }

    @Test
    public void twoMatchingSlabsRecombineButMixedSlabsDoNot() {
        IRecipe recipe = ConstructionRecipeHelper.createSlabRecombination(
                Blocks.STONE_SLAB, Blocks.STONE);

        assertTrue(recipe.matches(shapeless(new ItemStack(Blocks.STONE_SLAB, 1, 0),
                new ItemStack(Blocks.STONE_SLAB, 1, 5)), null));
        assertFalse(recipe.matches(shapeless(new ItemStack(Blocks.STONE_SLAB),
                new ItemStack(Blocks.WOODEN_SLAB)), null));
        assertSame(Item.getItemFromBlock(Blocks.STONE), recipe.getRecipeOutput().getItem());
        assertEquals(1, recipe.getRecipeOutput().stackSize);
    }

    @Test
    public void brickConversionRequiresFourMatchingConstructionForms() {
        IRecipe recipe = ConstructionRecipeHelper.createBrickConversion(
                Blocks.STONE_STAIRS, Blocks.BRICK_STAIRS);

        assertTrue(recipe.matches(square(new ItemStack(Blocks.STONE_STAIRS),
                new ItemStack(Blocks.STONE_STAIRS), new ItemStack(Blocks.STONE_STAIRS),
                new ItemStack(Blocks.STONE_STAIRS)), null));
        assertFalse(recipe.matches(square(new ItemStack(Blocks.STONE_STAIRS),
                new ItemStack(Blocks.STONE_STAIRS), new ItemStack(Blocks.STONE_STAIRS),
                new ItemStack(Blocks.QUARTZ_STAIRS)), null));
        assertSame(Item.getItemFromBlock(Blocks.BRICK_STAIRS),
                recipe.getRecipeOutput().getItem());
        assertEquals(4, recipe.getRecipeOutput().stackSize);
    }

    @Test
    public void polishingAcceptsOreDictionarySandButRejectsOtherIngredients() {
        OreDictionary.registerOre("sand",
                new ItemStack(Blocks.SAND, 1, OreDictionary.WILDCARD_VALUE));
        IRecipe recipe = ConstructionRecipeHelper.createPolishingRecipe(
                Blocks.STONE_STAIRS, Blocks.QUARTZ_STAIRS);

        assertTrue(recipe.matches(shapeless(new ItemStack(Blocks.STONE_STAIRS),
                new ItemStack(Blocks.SAND, 1, 1)), null));
        assertFalse(recipe.matches(shapeless(new ItemStack(Blocks.STONE_STAIRS),
                new ItemStack(Blocks.GRAVEL)), null));
        assertSame(Item.getItemFromBlock(Blocks.QUARTZ_STAIRS),
                recipe.getRecipeOutput().getItem());
    }

    @Test
    public void brickBlockPolishesWithoutAddingOtherFullBlockRoutes() throws Exception {
        OreDictionary.registerOre("sand",
                new ItemStack(Blocks.SAND, 1, OreDictionary.WILDCARD_VALUE));
        List<IRecipe> recipes = ConstructionRecipeHelper.createConvenienceRecipes(
                forms(Blocks.STONE, null, null, null),
                forms(Blocks.BRICK_BLOCK, null, null, null),
                forms(Blocks.QUARTZ_BLOCK, null, null, null),
                forms(Blocks.NETHER_BRICK, null, null, null));

        assertEquals(1, recipes.size());
        assertPolishesTo(recipes, Blocks.BRICK_BLOCK,
                new ItemStack(Blocks.SAND, 1, 0), Blocks.NETHER_BRICK);
        assertPolishesTo(recipes, Blocks.BRICK_BLOCK,
                new ItemStack(Blocks.SAND, 1, 1), Blocks.NETHER_BRICK);
        assertNull(firstMatchingRecipe(recipes, shapeless(
                new ItemStack(Blocks.BRICK_BLOCK), new ItemStack(Blocks.GRAVEL))));
        assertNull(firstMatchingRecipe(recipes, shapeless(
                new ItemStack(Blocks.STONE), new ItemStack(Blocks.SAND))));
        assertTrue(mineralogySource().contains(
                "new ShapelessOreRecipe(new ItemStack(smooth, 1), rock, \"sand\")"));
    }

    @Test
    public void missingBrickBlockOrPolishedBrickBlockOmitsFullBlockRoute() {
        ConstructionRecipeHelper.Forms raw = forms(null, null, null, null);
        ConstructionRecipeHelper.Forms polished = forms(null, null, null, null);

        assertTrue(ConstructionRecipeHelper.createConvenienceRecipes(
                raw, forms(null, null, null, null), polished,
                forms(Blocks.NETHER_BRICK, null, null, null)).isEmpty());
        assertTrue(ConstructionRecipeHelper.createConvenienceRecipes(
                raw, forms(Blocks.BRICK_BLOCK, null, null, null), polished,
                forms(null, null, null, null)).isEmpty());
    }

    private static ConstructionRecipeHelper.Forms forms(Block fullBlock, Block stairs,
            Block slab, Block wall) {
        return new ConstructionRecipeHelper.Forms(fullBlock, stairs, slab, wall);
    }

    private static void assertExactSlabRecipe(Block source, Block target,
            Block differentSource) {
        IRecipe recipe = exactSlabRecipe(source, target);
        InventoryCrafting matching = topRow(new ItemStack(source),
                new ItemStack(source), new ItemStack(source));
        InventoryCrafting mixed = topRow(new ItemStack(source),
                new ItemStack(differentSource), new ItemStack(source));

        assertTrue(recipe instanceof ShapedRecipes);
        assertTrue(recipe.matches(matching, null));
        assertFalse(recipe.matches(mixed, null));
        assertSame(Item.getItemFromBlock(target), recipe.getRecipeOutput().getItem());
        assertEquals(6, recipe.getRecipeOutput().stackSize);
    }

    private static IRecipe exactSlabRecipe(Block source, Block target) {
        return new ShapedRecipes(3, 1, new ItemStack[] {
                new ItemStack(source, 1, 0),
                new ItemStack(source, 1, 0),
                new ItemStack(source, 1, 0)
        }, new ItemStack(target, 6));
    }

    private static IRecipe firstMatchingRecipe(List<IRecipe> recipes,
            InventoryCrafting inventory) {
        for (IRecipe recipe : recipes) {
            if (recipe.matches(inventory, null)) {
                return recipe;
            }
        }
        return null;
    }

    private static void assertPolishesTo(List<IRecipe> recipes, Block source,
            ItemStack sand, Block target) {
        IRecipe recipe = firstMatchingRecipe(recipes, shapeless(
                new ItemStack(source), sand));
        assertSame(Item.getItemFromBlock(target), recipe.getRecipeOutput().getItem());
        assertEquals(1, recipe.getRecipeOutput().stackSize);
    }

    private static InventoryCrafting topRow(ItemStack first, ItemStack second, ItemStack third) {
        return grid(first, second, third, null, null, null, null, null, null);
    }

    private static InventoryCrafting square(ItemStack first, ItemStack second,
            ItemStack third, ItemStack fourth) {
        return grid(first, second, null, third, fourth, null, null, null, null);
    }

    private static InventoryCrafting shapeless(ItemStack... stacks) {
        ItemStack[] grid = new ItemStack[9];
        System.arraycopy(stacks, 0, grid, 0, stacks.length);
        return grid(grid);
    }

    private static InventoryCrafting grid(ItemStack... stacks) {
        InventoryCrafting inventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return true;
            }
        }, 3, 3);
        for (int i = 0; i < stacks.length; i++) {
            inventory.setInventorySlotContents(i, stacks[i]);
        }
        return inventory;
    }

    private static String mineralogySource() throws Exception {
        return new String(Files.readAllBytes(new File(
                "src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java").toPath()),
                StandardCharsets.UTF_8);
    }

    private static int countOccurrences(String value, String search) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(search, offset)) >= 0) {
            count++;
            offset += search.length();
        }
        return count;
    }
}
