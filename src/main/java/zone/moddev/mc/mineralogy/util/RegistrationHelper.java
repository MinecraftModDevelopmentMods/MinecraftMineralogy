package zone.moddev.mc.mineralogy.util;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.ItemBlock.BypassItemBlock;
import zone.moddev.mc.mineralogy.init.MineralogyRegistry;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.lib.exceptions.TabNotFoundException;
import zone.moddev.mc.mineralogy.lib.interfaces.IDynamicTabProvider;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class RegistrationHelper {
    public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName) {
        return registerBlock(block, name, oreDictionaryName, true, 64, false);
    }

    public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName, int maxStackSize) {
        return registerBlock(block, name, oreDictionaryName, true, maxStackSize, false);
    }

    public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName, boolean addToTab, int maxStackSize, boolean bypassSneak) {
        block.setTranslationKey(Mineralogy.MODID + "." + name);
        block.setRegistryName(name);
        Item item = null;

        if (addToTab) {
            if(bypassSneak)
                item = registerItem(new BypassItemBlock(block), name, maxStackSize);
            else
                item = registerItem(new ItemBlock(block), name, maxStackSize);
        } else {
            oreDictionaryName = "ITEMLESS" + oreDictionaryName;
        }
        MinIoC IoC = MinIoC.getInstance();

        BlockItemPair pair = new BlockItemPair(block, item);

        IoC.register(BlockItemPair.class, pair, name, Mineralogy.MODID);

        try {
            if (addToTab && MineralogyConfig.isCreativeVisible(name))
                MinIoC.getInstance().resolve(IDynamicTabProvider.class)
                        .addToTab(MineralogyConfig.creativeTabName(block), block);
        } catch (TabNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MineralogyRegistry.BlocksToRegister.put(oreDictionaryName, block);
        MineralogyRegistry.MineralogyBlockRegistry.put(name, pair);

        return pair;
    }

    public static Item registerItem(Item item, String name) {
        return registerItem(item, name, 64);
    }

    public static Item registerItem(Item item, String name, int maxStackSize) {
        String itemName = Mineralogy.MODID + "." + name;

        item.setTranslationKey(itemName);
        item.setRegistryName(name);
        item.setMaxStackSize(maxStackSize);
        if (!MineralogyConfig.isCreativeVisible(name)) {
            item.setCreativeTab(null);
        }


        MineralogyRegistry.MineralogyItemRegistry.put(name, item);
        return item;
    }
}
