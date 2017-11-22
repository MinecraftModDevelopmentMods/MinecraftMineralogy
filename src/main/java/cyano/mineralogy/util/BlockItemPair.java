package cyano.mineralogy.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BlockItemPair {
	public final Block PairedBlock;
	public final Item PairedItem;
	
	public BlockItemPair(Block block, Item item) 
	{
		PairedBlock = block;
		PairedItem = item;
	}
}
