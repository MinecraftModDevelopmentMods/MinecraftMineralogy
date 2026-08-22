package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Constants;
import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.items.MineralFertilizer;
import zone.moddev.mc.mineralogy.lib.exceptions.TabNotFoundException;
import zone.moddev.mc.mineralogy.lib.interfaces.IDynamicTabProvider;
import zone.moddev.mc.mineralogy.util.RegistrationHelper;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class Items {
    private static boolean initDone = false;

    protected Items() {
        throw new IllegalAccessError("Not a instantiable class");
    }

    /**
     *
     */
    public static void init() {
        if (initDone) {
            return;
        }

        MinIoC IoC = MinIoC.getInstance();

        Item gypsumPowder = addDust(Constants.GYPSUM);;
        Item chalkPowder = addDust(Constants.CHALK);;
        Item rocksaltPowder = addDust(Constants.ROCKSALT);;
        Item sulphurPowder = addDust(Constants.SULFUR);;
        Item phosphorousPowder = addDust(Constants.PHOSPHOROUS);;
        Item nitratePowder = addDust(Constants.NITRATE);

        Item mineralFertilizer = RegistrationHelper.registerItem(new MineralFertilizer(), "mineral_fertilizer")
                .setTranslationKey(Mineralogy.MODID + "." + "mineral_fertilizer");
        if (MineralogyConfig.contentPolicy().mineralFertilizerEnabled()) {
            if (MineralogyConfig.groupCreativeTabItemsByType()) {
                addToMineralogyTab(mineralFertilizer);
            } else {
                mineralFertilizer.setCreativeTab(CreativeTabs.MATERIALS);
            }
        } else {
            mineralFertilizer.setCreativeTab(null);
        }

        IoC.register(Item.class, gypsumPowder, Constants.DUST_GYPSUM, Mineralogy.MODID);
        IoC.register(Item.class, chalkPowder, Constants.DUST_CHALK, Mineralogy.MODID);
        IoC.register(Item.class, rocksaltPowder, Constants.DUST_ROCKSALT, Mineralogy.MODID);
        IoC.register(Item.class, sulphurPowder, Constants.SULFUR, Mineralogy.MODID);
        IoC.register(Item.class, phosphorousPowder, Constants.PHOSPHOROUS, Mineralogy.MODID);
        IoC.register(Item.class, nitratePowder, Constants.NITRATE, Mineralogy.MODID);
        IoC.register(Item.class, mineralFertilizer, Constants.FERTILIZER, Mineralogy.MODID);

        MineralogyRegistry.ItemsToRegister.put(Constants.FERTILIZER, mineralFertilizer);
        MineralogyRegistry.ItemsToRegister.put("dustRocksalt", rocksaltPowder);
        MineralogyRegistry.ItemsToRegister.put("dustSulphur", sulphurPowder);
        MineralogyRegistry.ItemsToRegister.put("sulfur", sulphurPowder);
        MineralogyRegistry.ItemsToRegister.put("sulphur", sulphurPowder);

        initDone = true;
    }

    private static Item addDust(String oreDictionaryName) {
        String dustName = oreDictionaryName.toLowerCase() + "_" + Constants.DUST;

        Item item = RegistrationHelper.registerItem(new Item(), dustName).setTranslationKey(Mineralogy.MODID + "." + dustName);

        addToMineralogyTab(item);

        MineralogyRegistry.ItemsToRegister.put(Constants.DUST + oreDictionaryName, item);
        MinIoC.getInstance().register(Item.class, item, Constants.DUST + oreDictionaryName, Mineralogy.MODID);

        return item;
    }

    private static void addToMineralogyTab(Item item) {
        if (!MineralogyConfig.isCreativeVisible(item.getRegistryName().getPath())) {
            item.setCreativeTab(null);
            return;
        }
        try {
            MinIoC.getInstance().resolve(IDynamicTabProvider.class)
                    .addToTab(MineralogyConfig.itemCreativeTabName(), item);
        } catch (TabNotFoundException e) {
            Mineralogy.LOGGER.warn("Unable to place {} on a Mineralogy creative tab", item.getRegistryName(), e);
        }
    }
}
