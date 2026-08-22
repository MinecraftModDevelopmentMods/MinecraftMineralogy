package zone.moddev.mc.mineralogy;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import zone.moddev.mc.mineralogy.util.RecipeHelper;

/**
 * Registers lossless construction-form conversions for one Mineralogy stone
 * family. Historical full-block crafting remains in {@link Mineralogy}; this
 * helper also supplies the missing brick-to-polished-brick route.
 */
public final class ConstructionRecipeHelper {
    private ConstructionRecipeHelper() {
    }

    public static void registerConvenienceRecipes(String family, Forms raw, Forms brick,
            Forms polished, Forms polishedBrick) {
        addSlabRecombination(family, "raw", raw);
        addSlabRecombination(family, "brick", brick);
        addSlabRecombination(family, "polished", polished);
        addSlabRecombination(family, "polished_brick", polishedBrick);

        addBrickConversions(family, "raw", raw, brick);
        addBrickConversions(family, "polished", polished, polishedBrick);
        addPolishingRecipes(family, "raw", raw, polished);
        addPolishingRecipes(family, "brick", brick, polishedBrick);
        if (brick != null && polishedBrick != null) {
            addPolishingRecipe(family + "_brick_block_polishing", brick.fullBlock, polishedBrick.fullBlock);
        }
    }

    private static void addSlabRecombination(String family, String finish, Forms forms) {
        if (forms != null && forms.fullBlock != null && forms.slab != null) {
            RecipeHelper.addShapelessOreRecipe(family + "_" + finish + "_slab_recombination",
                    new ItemStack(forms.fullBlock), stack(forms.slab), stack(forms.slab));
        }
    }

    private static void addBrickConversions(String family, String finish, Forms source, Forms target) {
        if (source == null || target == null) {
            return;
        }
        addBrickConversion(family + "_" + finish + "_stairs_to_brick", source.stairs, target.stairs);
        addBrickConversion(family + "_" + finish + "_slabs_to_brick", source.slab, target.slab);
        addBrickConversion(family + "_" + finish + "_walls_to_brick", source.wall, target.wall);
    }

    private static void addBrickConversion(String name, Block source, Block target) {
        if (source != null && target != null) {
            RecipeHelper.addShapedOreRecipe(name, new ItemStack(target, 4),
                    "xx", "xx", 'x', stack(source));
        }
    }

    private static void addPolishingRecipes(String family, String finish, Forms source, Forms target) {
        if (source == null || target == null) {
            return;
        }
        addPolishingRecipe(family + "_" + finish + "_stairs_polishing", source.stairs, target.stairs);
        addPolishingRecipe(family + "_" + finish + "_slab_polishing", source.slab, target.slab);
        addPolishingRecipe(family + "_" + finish + "_wall_polishing", source.wall, target.wall);
    }

    private static void addPolishingRecipe(String name, Block source, Block target) {
        if (source != null && target != null) {
            RecipeHelper.addShapelessOreRecipe(name, new ItemStack(target), stack(source), "sand");
        }
    }

    private static ItemStack stack(Block block) {
        return new ItemStack(block, 1, 0);
    }

    public static final class Forms {
        final Block fullBlock;
        final Block stairs;
        final Block slab;
        final Block wall;

        public Forms(Block fullBlock, Block stairs, Block slab, Block wall) {
            this.fullBlock = fullBlock;
            this.stairs = stairs;
            this.slab = slab;
            this.wall = wall;
        }
    }
}
