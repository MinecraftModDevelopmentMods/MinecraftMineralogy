package com.mcmoddev.mineralogy.util;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.lib.exceptions.ItemNotFoundException;
import com.mcmoddev.mineralogy.lib.exceptions.TabNotFoundException;
import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class RegistrationHelper {
	public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName) {
		return registerBlock(block, name, oreDictionaryName, true, 64);
	}
	
	public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName, int maxStackSize) {
		return registerBlock(block, name, oreDictionaryName, true, maxStackSize);
	}
	
	public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName, boolean addToTab, int maxStackSize) {
		block.setTranslationKey(Mineralogy.MODID + "." + name);
		block.setRegistryName(name);
		Item item = null;
		
		if (addToTab)
			item = registerItem(new ItemBlock(block), name, maxStackSize);
		
		MinIoC IoC = MinIoC.getInstance();
		
		BlockItemPair pair = new BlockItemPair(block, item);

		IoC.register(BlockItemPair.class, pair, name, Mineralogy.MODID);
		
		try {
			if (addToTab)
				MinIoC.getInstance().resolve(IDynamicTabProvider.class).addToTab(block);
		} catch (TabNotFoundException | ItemNotFoundException e) {
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
		
		MineralogyRegistry.MineralogyItemRegistry.put(name, item);
		return item;
	}
}