package zone.moddev.mc.mineralogy;

import java.util.Map;

import zone.moddev.mc.mineralogy.init.MineralogyRegistry;
import zone.moddev.mc.mineralogy.data.Material;
import zone.moddev.mc.mineralogy.data.MaterialData;
import zone.moddev.mc.mineralogy.patching.PatchHandler;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

// This seems really not nice design, it'll do til we refactor the whole thing..
@Mod.EventBusSubscriber(modid = Mineralogy.MODID)
public class MineralogyEventBusSubscriber {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        MineralogyRegistry.MineralogyBlockRegistry.values().forEach(block -> event.getRegistry().register(block.PairedBlock));

        event.getRegistry().registerAll(PatchHandler.MineralogyPatchRegistry.values()
                .toArray(new Block[PatchHandler.MineralogyPatchRegistry.size()]));
    }



    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                MineralogyRegistry.MineralogyItemRegistry.values().toArray(new Item[MineralogyRegistry.MineralogyItemRegistry.size()]));

        for (Map.Entry<String, Block> map : MineralogyRegistry.BlocksToRegister.entrySet())  {
            if (!map.getKey().contains("ITEMLESS"))
                OreDictionary.registerOre(map.getKey(), map.getValue());
        }
        for (Material material : MaterialData.toArray()) {
            registerRawStone(material.materialName.toLowerCase());
        }
        registerRawStone(Constants.ROCKSALT.toLowerCase());

        registerRawCobblestone(Constants.CHERT);
        registerRawCobblestone(Constants.PUMICE);
        // Make every raw Mineralogy rock equivalent to cobblestone when requested.
        if (MineralogyConfig.makeRockCobblestoneEquivilent()) {
            for (Material material : MaterialData.toArray()) {
                registerRawCobblestone(material.materialName.toLowerCase());
            }
            registerRawCobblestone(Constants.ROCKSALT.toLowerCase());
            }

        for (Map.Entry<String, Item> map : MineralogyRegistry.ItemsToRegister.entrySet())
            OreDictionary.registerOre(map.getKey(), map.getValue());
    }

    private static void registerRawCobblestone(String registryName) {
        zone.moddev.mc.mineralogy.util.BlockItemPair pair =
                MineralogyRegistry.MineralogyBlockRegistry.get(registryName);
        if (pair != null) {
            OreDictionary.registerOre(Constants.COBBLESTONE, pair.PairedBlock);
        }
    }

    private static void registerRawStone(String registryName) {
        zone.moddev.mc.mineralogy.util.BlockItemPair pair =
                MineralogyRegistry.MineralogyBlockRegistry.get(registryName);
        if (pair != null) {
            OreDictionary.registerOre("stone", pair.PairedBlock);
        }
    }

}
