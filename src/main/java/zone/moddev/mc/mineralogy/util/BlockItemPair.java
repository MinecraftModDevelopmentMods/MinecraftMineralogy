package zone.moddev.mc.mineralogy.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

public class BlockItemPair {

	public final Block PairedBlock;
	public final Item PairedItem;

	public BlockItemPair(Block block, Item item) {
		PairedBlock = block;
		PairedItem = item;
	}
}
