package com.mcmoddev.lib.util;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mcmoddev.lib.exceptions.ItemNotFoundException;
import com.mcmoddev.lib.exceptions.MaterialNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;
import com.mcmoddev.lib.interfaces.IDynamicTabProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class DynamicTabProvider implements IDynamicTabProvider {
	private Map<String, MMDCreativeTab> tabs = new HashMap<>();
	private Map<String, String> tabsByMod =new HashMap<>();
	
	private Multimap<String, String> tabItemMapping = ArrayListMultimap.create();
		
	private MMDCreativeTab getTabByName(String tabName) throws TabNotFoundException {
		MMDCreativeTab tab = tabs.get(tabName);
		
		if (tab == null)
			throw new TabNotFoundException(tabName);
		
		return tab;
	}

	@Override
	public void addBlockToTab(String tabName, Block block) throws TabNotFoundException {
		block.setCreativeTab(getTabByName(tabName));
	}
	
	@Override
	public void addItemToTab(String tabName, Item item) throws TabNotFoundException {	
		item.setCreativeTab(getTabByName(tabName));
	}

	@Override
	public void setIcon(String tabName, String materialName) throws TabNotFoundException, MaterialNotFoundException {
//		Block temp;
//		ItemStack blocksTabIconItem;
//
//		MMDMaterial material = Materials.getMaterialByName(materialName);
//		
//		if (material.getName().equals(materialName) && (material.hasBlock(Names.BLOCK)))
//			temp = material.getBlock(Names.BLOCK);
//		else
//			temp = net.minecraft.init.Blocks.IRON_BLOCK;
//		
//		blocksTabIconItem = new ItemStack(Item.getItemFromBlock(temp));
//		blocksTab.setTabIconItem(blocksTabIconItem);
	}

	private String getTab(String itemName, String modID)  {
		for (String tab : tabItemMapping.get(itemName))
			if (modID.equals(tabsByMod.get(tab))) 
				return tab;
		
		return "";
	}


	public void setTabItemMapping(String tabName, String itemName) {
		tabItemMapping.put(itemName, tabName);
	}

	private String getTab(String itemName)   {
		return tabItemMapping.get(itemName).stream().findFirst().orElse("");
	}

	@Override
	public void addTab(String tabName, boolean searchable, String modID) {
		String internalTabName = String.format("%s.%s", modID, tabName);
		MMDCreativeTab tab = new MMDCreativeTab(internalTabName, searchable);
		
		tabs.put(tabName, tab);
		tabsByMod.put(tabName, modID);
	}

	@Override
	public void addBlockToTab(Block block) throws TabNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addItemToTab(Item item) throws TabNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getTabs() {
		return (String[]) tabs.keySet().toArray();
	}

	@Override
	public void initialisePostemptiveTabGeneration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executePostemptiveTabGeneration() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getTab(Item item) throws ItemNotFoundException {
		String tab;
		ResourceLocation resourceLocation = item.getRegistryName();
		
		// 1) Try to get a tab mapping based on item name and mod id
		tab = getTab(resourceLocation.getResourcePath(), resourceLocation.getResourceDomain());
		if (!tab.equals("")) return tab;
		
		//2) Try to get a tab mapping based on item name
		tab = getTab(resourceLocation.getResourcePath());
		if (!tab.equals("")) return tab;
		
		//3) Try to get a tab mapping based on item class name and mod id
		tab = getTab(item.getClass().getSimpleName(), resourceLocation.getResourceDomain());
		if (!tab.equals("")) return tab;
		
		//4) Try to get a tab mapping based on item class name only
		tab = getTab(item.getClass().getSimpleName());
		if (!tab.equals("")) return tab;
		
		throw new ItemNotFoundException(resourceLocation.getResourcePath());
	}
}
