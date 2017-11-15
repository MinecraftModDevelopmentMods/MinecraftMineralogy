package cyano.mineralogy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// This seems really not nice design, it'll do til we refactor the whole thing..
@Mod.EventBusSubscriber(modid = Mineralogy.MODID)
public class MineralogyEventBusSubscriber {
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
	    event.getRegistry().registerAll(Mineralogy.MineralogyBlockRegistry.values().toArray(new Block[Mineralogy.MineralogyBlockRegistry.size()]));
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
	    event.getRegistry().registerAll(Mineralogy.MineralogyItemRegistry.values().toArray(new Item[Mineralogy.MineralogyItemRegistry.size()]));
	}
}
