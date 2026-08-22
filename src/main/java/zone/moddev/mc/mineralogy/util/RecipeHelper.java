package zone.moddev.mc.mineralogy.util;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.init.MineralogyRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeHelper {
    public static ShapedRecipes addExactHorizontalThreeRecipe(String name, ItemStack output, ItemStack input) {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(3, Ingredient.EMPTY);
        for (int index = 0; index < ingredients.size(); index++) {
            ingredients.set(index, Ingredient.fromStacks(input.copy()));
        }
        ShapedRecipes recipe = new ShapedRecipes(Mineralogy.MODID, 3, 1, ingredients, output);
        recipe.setRegistryName(new ResourceLocation(Mineralogy.MODID, name));
        MineralogyRegistry.MineralogyRecipeRegistry.put(name, recipe);
        return recipe;
    }

    public static ShapedOreRecipe addShapedOreRecipe(String name, ItemStack output, Object... args) {
        return addShapedOreRecipe(Mineralogy.MODID, name, output, args);
    }

    public static ShapedOreRecipe addShapedOreRecipe(String domain, String name, ItemStack output,  Object... args) {
        ShapedOreRecipe newRecipe = new ShapedOreRecipe(new ResourceLocation(domain, name), output, args);
        newRecipe.setRegistryName(name);

        MineralogyRegistry.MineralogyRecipeRegistry.put(name, newRecipe);

        return newRecipe;
    }

    public static ShapelessOreRecipe addShapelessOreRecipe(String name, ItemStack output, Object... args) {
        return addShapelessOreRecipe(Mineralogy.MODID, name, output, args);
    }

    public static ShapelessOreRecipe addShapelessOreRecipe(String domain, String name, ItemStack output, Object... args) {
        ShapelessOreRecipe newRecipe = new ShapelessOreRecipe(new ResourceLocation(domain, name), output, args);
        newRecipe.setRegistryName(name);

        MineralogyRegistry.MineralogyRecipeRegistry.put(name, newRecipe);

        return newRecipe;
    }

    /**
     * Retains an advancement-referenced recipe identity without exposing a
     * craftable or recipe-book-visible recipe. Forge 1.12 resolves recipe
     * advancement rewards while worlds load and logs an exception if an
     * intentionally disabled recipe is absent from the registry.
     */
    public static IRecipe addUnavailableRecipe(String name) {
        IRecipe recipe = new UnavailableRecipe();
        recipe.setRegistryName(new ResourceLocation(Mineralogy.MODID, name));
        MineralogyRegistry.MineralogyRecipeRegistry.put(name, recipe);
        return recipe;
    }

    private static final class UnavailableRecipe extends IForgeRegistryEntry.Impl<IRecipe>
            implements IRecipe {
        @Override
        public boolean matches(InventoryCrafting inventory, World world) {
            return false;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inventory) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canFit(int width, int height) {
            return false;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }
    }
}
