//package com.mcmoddev.mineralogy.util;
//
//import com.mcmoddev.mineralogy.Mineralogy;
//import com.mcmoddev.mineralogy.init.MineralogyRegistry;
//import com.mcmoddev.mineralogy.ioc.MinIoC;
//import com.mcmoddev.mineralogy.lib.exceptions.ItemNotFoundException;
//import com.mcmoddev.mineralogy.lib.exceptions.TabNotFoundException;
//import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;
//
//import net.minecraft.block.Block;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemBlock;
//
//public class RegistrationHelper {
//	public static BlockItemPair registerBlock(Block block, String name, String oreDictionaryName) {
//		block.setTranslationKey(Mineralogy.MODID + "." + name);
//		block.setRegistryName(name);
//
//		Item item = registerItem(new ItemBlock(block), name);
//		
//		BlockItemPair pair = new BlockItemPair(block, item);
//
//		try {
//			MinIoC.getInstance().resolve(IDynamicTabProvider.class).addToTab(block);
//		} catch (TabNotFoundException | ItemNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		MineralogyRegistry.BlocksToRegister.put(oreDictionaryName, block);
//		MineralogyRegistry.MineralogyBlockRegistry.put(name, pair);
//
//		return pair;
//	}
//
//	public static Item registerItem(Item item, String name) {
//		String itemName = Mineralogy.MODID + "." + name;
//
//		item.setTranslationKey(itemName);
//		item.setRegistryName(name);
//		
//		MineralogyRegistry.MineralogyItemRegistry.put(name, item);
//		return item;
//	}
//}