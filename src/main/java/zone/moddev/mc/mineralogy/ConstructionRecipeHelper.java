package zone.moddev.mc.mineralogy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Registers lossless construction-form conversions for one Mineralogy stone
 * family. Historical full-block crafting remains in {@link Mineralogy}; this
 * helper also supplies the missing brick-to-polished-brick route.
 */
final class ConstructionRecipeHelper {
    private ConstructionRecipeHelper() {
    }

    static void registerConvenienceRecipes(Forms raw, Forms brick,
            Forms polished, Forms polishedBrick) {
        for (IRecipe recipe : createConvenienceRecipes(raw, brick, polished, polishedBrick)) {
            GameRegistry.addRecipe(recipe);
        }
    }

    static List<IRecipe> createConvenienceRecipes(Forms raw, Forms brick,
            Forms polished, Forms polishedBrick) {
        List<IRecipe> recipes = new ArrayList<IRecipe>();

        addSlabRecombination(recipes, raw);
        addSlabRecombination(recipes, brick);
        addSlabRecombination(recipes, polished);
        addSlabRecombination(recipes, polishedBrick);

        addBrickConversions(recipes, raw, brick);
        addBrickConversions(recipes, polished, polishedBrick);

        addPolishingRecipes(recipes, raw, polished);
        addPolishingRecipes(recipes, brick, polishedBrick);
        addFullBlockPolishingRecipe(recipes, brick, polishedBrick);

        return recipes;
    }

    static IRecipe createSlabRecombination(Block slab, Block fullBlock) {
        return new ShapelessOreRecipe(new ItemStack(fullBlock),
                wildcardStack(slab), wildcardStack(slab));
    }

    static IRecipe createBrickConversion(Block source, Block target) {
        return new ShapedOreRecipe(new ItemStack(target, 4),
                "xx", "xx", 'x', wildcardStack(source));
    }

    static IRecipe createPolishingRecipe(Block source, Block target) {
        return new ShapelessOreRecipe(new ItemStack(target), wildcardStack(source), "sand");
    }

    private static void addSlabRecombination(List<IRecipe> recipes, Forms forms) {
        if (forms != null && forms.fullBlock != null && forms.slab != null) {
            recipes.add(createSlabRecombination(forms.slab, forms.fullBlock));
        }
    }

    private static void addBrickConversions(List<IRecipe> recipes, Forms source, Forms target) {
        if (source == null || target == null) {
            return;
        }
        addBrickConversion(recipes, source.stairs, target.stairs);
        addBrickConversion(recipes, source.slab, target.slab);
        addBrickConversion(recipes, source.wall, target.wall);
    }

    private static void addBrickConversion(List<IRecipe> recipes, Block source, Block target) {
        if (source != null && target != null) {
            recipes.add(createBrickConversion(source, target));
        }
    }

    private static void addPolishingRecipes(List<IRecipe> recipes, Forms source, Forms target) {
        if (source == null || target == null) {
            return;
        }
        addPolishingRecipe(recipes, source.stairs, target.stairs);
        addPolishingRecipe(recipes, source.slab, target.slab);
        addPolishingRecipe(recipes, source.wall, target.wall);
    }

    private static void addPolishingRecipe(List<IRecipe> recipes, Block source, Block target) {
        if (source != null && target != null) {
            recipes.add(createPolishingRecipe(source, target));
        }
    }

    private static void addFullBlockPolishingRecipe(List<IRecipe> recipes,
            Forms source, Forms target) {
        if (source != null && target != null) {
            addPolishingRecipe(recipes, source.fullBlock, target.fullBlock);
        }
    }

    private static ItemStack wildcardStack(Block block) {
        return new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
    }

    static final class Forms {
        final Block fullBlock;
        final Block stairs;
        final Block slab;
        final Block wall;

        Forms(Block fullBlock, Block stairs, Block slab, Block wall) {
            this.fullBlock = fullBlock;
            this.stairs = stairs;
            this.slab = slab;
            this.wall = wall;
        }
    }
}
