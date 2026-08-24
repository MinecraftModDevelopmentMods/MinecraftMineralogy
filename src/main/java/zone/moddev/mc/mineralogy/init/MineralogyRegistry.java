package zone.moddev.mc.mineralogy.init;

import java.util.HashMap;
import java.util.Map;

import zone.moddev.mc.mineralogy.util.BlockItemPair;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class MineralogyRegistry {
    public static final Map<String, BlockItemPair> MineralogyBlockRegistry = new HashMap<>(); // all blocks used in this mod (blockID, BlockItemPair)
    public static final Map<String, Item> MineralogyItemRegistry = new HashMap<>(); // all items used in this mod (itemID, item)
    public static final Map<String, Block> BlocksToRegister = new HashMap<>(); // all blocks used in this mod (blockID, BlockItemPair)
    public static final Map<String, Item> ItemsToRegister = new HashMap<>(); // all items used in this mod (itemID, item)
}
