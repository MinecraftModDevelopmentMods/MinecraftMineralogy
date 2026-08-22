package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Constants;
import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.util.BlockItemPair;
import zone.moddev.mc.mineralogy.util.RecipeHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class Recipes {
    private static boolean initDone = false;

    protected Recipes() {
        throw new IllegalAccessError("Not a instantiable class");
    }

    public static void Init() {
        if (initDone) {
            return;
        }

        MinIoC IoC = MinIoC.getInstance();

        Item mineralFertilizer = IoC.resolve(Item.class, Constants.FERTILIZER, Mineralogy.MODID);
        Item blockGypsum = IoC.resolve(BlockItemPair.class, Constants.BLOCK_GYPSUM, Mineralogy.MODID).PairedItem;
        Item blockChalk = IoC.resolve(BlockItemPair.class, Constants.BLOCK_CHALK, Mineralogy.MODID).PairedItem;
        Item blockRocksalt = IoC.resolve(BlockItemPair.class, Constants.BLOCK_ROCKSALT, Mineralogy.MODID).PairedItem;
        Item dustGypsum = IoC.resolve(Item.class, Constants.DUST_GYPSUM, Mineralogy.MODID);
        Item dustChalk = IoC.resolve(Item.class, Constants.DUST_CHALK, Mineralogy.MODID);
        Item dustRocksalt = IoC.resolve(Item.class, Constants.DUST_ROCKSALT, Mineralogy.MODID);

        Item blockRockSaltLamp = IoC.resolve(BlockItemPair.class, "rocksaltlamp", Mineralogy.MODID).PairedItem;
        Item blockRockSaltStreetLamp = IoC.resolve(BlockItemPair.class, "rocksaltstreetlamp", Mineralogy.MODID).PairedItem;

        if (MineralogyConfig.contentPolicy().mineralDustsEnabled()) {
            RecipeHelper.addShapelessOreRecipe("gunpowder_from_sugar", new ItemStack(Items.GUNPOWDER, 4),
                    Ingredient.fromStacks(new ItemStack(Items.SUGAR)), Constants.DUST_NITRATE, Constants.DUST_SULFUR);
            RecipeHelper.addShapelessOreRecipe("gunpowder_from_charcoal", new ItemStack(Items.GUNPOWDER, 4),
                    Ingredient.fromStacks(new ItemStack(Items.COAL, 1, 1)), Constants.DUST_NITRATE, Constants.DUST_SULFUR);
            RecipeHelper.addShapelessOreRecipe("gunpowder_from_carbon_dust", new ItemStack(Items.GUNPOWDER, 4),
                    Constants.DUST_CARBON, Constants.DUST_NITRATE, Constants.DUST_SULFUR);
            RecipeHelper.addShapelessOreRecipe("gunpowder_from_coal_dust", new ItemStack(Items.GUNPOWDER, 4),
                    "dustCoal", Constants.DUST_NITRATE, Constants.DUST_SULFUR);
        }
        if (MineralogyConfig.contentPolicy().mineralFertilizerEnabled()) {
            RecipeHelper.addShapelessOreRecipe(Constants.MINERALFERTILIZER, new ItemStack(mineralFertilizer, 1),
                    Constants.DUST_NITRATE, "dustPhosphorous");
        }

        RecipeHelper.addShapelessOreRecipe(Constants.COBBLESTONE.toUpperCase(), new ItemStack(Blocks.COBBLESTONE, 4),
                Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)),
                Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)),
                Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));

        RecipeHelper.addShapedOreRecipe(Constants.GYPSUM.toLowerCase(), new ItemStack(blockGypsum, 1), "xx", "xx", 'x', "dustGypsum");
        RecipeHelper.addShapedOreRecipe(Constants.CHALK.toLowerCase(), new ItemStack(blockChalk, 1), "xx", "xx", 'x', "dustChalk");
        RecipeHelper.addShapedOreRecipe(Constants.ROCKSALT.toLowerCase(), new ItemStack(blockRocksalt, 1), "xx", "xx", 'x', "dustRock_salt");
        RecipeHelper.addShapelessOreRecipe(Constants.GYPSUM.toLowerCase() + "_dust", new ItemStack(dustGypsum, 4), Constants.BLOCK_GYPSUM);
        RecipeHelper.addShapelessOreRecipe(Constants.CHALK.toLowerCase() + "_dust", new ItemStack(dustChalk, 4), Constants.BLOCK_CHALK);
        RecipeHelper.addShapelessOreRecipe(Constants.ROCKSALT.toLowerCase() + "_dust", new ItemStack(dustRocksalt, 4), Constants.BLOCK_ROCKSALT);

        Item dryWallWhite = IoC.resolve(BlockItemPair.class, Constants.DRYWALL_WHITE, Mineralogy.MODID).PairedItem;

        if (MineralogyConfig.contentPolicy().drywallsEnabled()) {
            RecipeHelper.addShapedOreRecipe(Constants.DRYWALL, new ItemStack(dryWallWhite, 3), "pgp", "pgp", "pgp", 'p', Constants.PAPER,
                    'g', Constants.DUST_GYPSUM);
        }
        if (MineralogyConfig.contentPolicy().rockSaltLampsEnabled()) {
            RecipeHelper.addShapelessOreRecipe("rocksaltlamp", new ItemStack(blockRockSaltLamp, 1),
                    Ingredient.fromStacks(new ItemStack(blockRocksalt)), Ingredient.fromStacks(new ItemStack(Blocks.TORCH)),
                    Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)));
            RecipeHelper.addShapedOreRecipe("rocksaltstreetlamp", new ItemStack(blockRockSaltStreetLamp, 1),
                    "x", "y", "y", 'x', "lampRocksalt", 'y', Items.IRON_INGOT);
        } else {
            // Their retained 1.12 recipe advancements resolve these IDs while
            // a world loads. Inert placeholders prevent noisy parse failures
            // without making either disabled recipe craftable or visible.
            RecipeHelper.addUnavailableRecipe("rocksaltlamp");
            RecipeHelper.addUnavailableRecipe("rocksaltstreetlamp");
        }

        initDone = true;
    }
}
