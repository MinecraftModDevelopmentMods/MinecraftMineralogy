package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Constants;
import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.util.BlockItemPair;
import zone.moddev.mc.mineralogy.util.RecipeHelper;
import zone.moddev.mc.mineralogy.util.RegistrationHelper;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Ores {
    private static boolean initDone = false;

    protected Ores() {
        throw new IllegalAccessError("Not a instantiable class");
    }

    public static void Init() {
        if (initDone) {
            return;
        }

        MinIoC IoC = MinIoC.getInstance();

        Item sulfurPowder = IoC.resolve(Item.class, "dustSulfur", Mineralogy.MODID);
        Item phosphorousPowder = IoC.resolve(Item.class, "dustPhosphorous", Mineralogy.MODID);
        Item nitratePowder = IoC.resolve(Item.class, "dustNitrate", Mineralogy.MODID);

        // register ores
        addOre(Constants.SULFUR, sulfurPowder, 1, 4, 0);
        addOre(Constants.PHOSPHOROUS, phosphorousPowder, 1, 4, 0);
        addOre(Constants.NITRATE, nitratePowder, 1, 4, 0);

        initDone = true;
    }

    private static Block addOre(String oreDictionaryName, Item oreDropItem, int numMin, int numMax, int pickLevel) {
        String oreName = oreDictionaryName.toLowerCase() + "_" + Constants.ORE;

        Block oreBlock = new Ore(oreName, oreDropItem, numMin, numMax, pickLevel)
                .setTranslationKey(Mineralogy.MODID + "." + oreName);

        RegistrationHelper.registerBlock(oreBlock, oreName, Constants.ORE + oreDictionaryName);
        if (Constants.SULFUR.equals(oreDictionaryName)) {
            MineralogyRegistry.BlocksToRegister.put("oreSulphur", oreBlock);
        }

        addBlock(oreDictionaryName, 0, oreDropItem);

        return oreBlock;
    }

    private static Block addBlock(String oreDictionaryName, int pickLevel, Item dust) {
        String name = oreDictionaryName.toLowerCase() + "_block";

        BlockItemPair pair = RegistrationHelper.registerBlock(new Rock(false, (float) 1.5, (float) 10, 0, SoundType.STONE), name,
                Constants.BLOCK.toLowerCase() + oreDictionaryName);

        if (MineralogyConfig.contentPolicy().mineralDustsEnabled()) {
            RecipeHelper.addShapedOreRecipe(name, new ItemStack(pair.PairedItem), "xxx", "xxx", "xxx", 'x', dust);
            RecipeHelper.addShapelessOreRecipe(oreDictionaryName.toLowerCase() + "_dust", new ItemStack(dust, 9),
                    "block" + oreDictionaryName);
        }

        return pair.PairedBlock;
    }
}
