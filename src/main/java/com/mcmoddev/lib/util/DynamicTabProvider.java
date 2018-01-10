package com.mcmoddev.lib.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mcmoddev.lib.exceptions.MaterialNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;
import com.mcmoddev.lib.interfaces.IDynamicTabProvider;
import com.mcmoddev.lib.interfaces.IMMDCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public final class DynamicTabProvider implements IDynamicTabProvider {
	private Map<String, MMDCreativeTab> tabs = new HashMap<>();

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

	@Override
	public String getTab(String itemName, String modID) {
		return getTab(itemName);
	}

	@Override
	public void setTabItemMapping(String tabName, String itemName) {
		tabItemMapping.put(itemName, tabName);
	}

	@Override
	public String getTab(String itemName) {
		return tabItemMapping.get(itemName).stream().findFirst().orElse(""); // TODO: default tab
	}

	@Override
	public void addTab(String tabName, boolean searchable, String modID) {
		// TODO Auto-generated method stub
		String internalTabName = String.format("%s.%s", modID, tabName);
		IMMDCreativeTab tab = new MMDCreativeTab(internalTabName, searchable);
		
//		if (itemGroupsByModID.containsKey(modName)) {
//			itemGroupsByModID.get(modName).add(tab);
//		} else {
//			List<MMDCreativeTab> nl = new ArrayList<>();
//			nl.add(tab);
//			itemGroupsByModID.put(modName, nl);
//		}
//
//		return itemGroupsByModID.get(modName).size() - 1;		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialisePostemptiveTabGeneration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executePostemptiveTabGeneration() {
		// TODO Auto-generated method stub
		
	}
}
