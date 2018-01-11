package com.mcmoddev.lib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.exceptions.ItemNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;
import com.mcmoddev.lib.interfaces.IDynamicTabProvider;
import com.mcmoddev.lib.interfaces.IMMDMaterial;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class DynamicTabProvider implements IDynamicTabProvider {
	private Map<String, MMDCreativeTab> tabs = new HashMap<>();
	private Map<String, String> tabsByMod = new HashMap<>();
	
	private Multimap<String, String> tabItemMapping = ArrayListMultimap.create();
		
	private boolean retrospectiveTabGeneration = false;
	
	private List<Block> retrospectiveBlocks = new ArrayList<>();
	private List<Item> retrospectiveItems = new ArrayList<>();
	
	private int optimalTabSize = 50;
	
	private MMDCreativeTab getTabByName(String tabName) throws TabNotFoundException {
		MMDCreativeTab tab = tabs.get(tabName);
		
		if (tab == null)
			throw new TabNotFoundException(tabName);
		
		return tab;
	}

	@Override
	public void addToTab(String tabName, Block block) throws TabNotFoundException {
		block.setCreativeTab(getTabByName(tabName));
	}
	
	@Override
	public void addToTab(String tabName, Item item) throws TabNotFoundException {	
		item.setCreativeTab(getTabByName(tabName));
	}

	@Override
	public void setIcon(String tabName, IMMDMaterial material) throws TabNotFoundException {
		tabs.get(tabName).setTabIconItem(new ItemStack(Item.getItemFromBlock(material.hasBlock(Names.BLOCK) ?  material.getBlock(Names.BLOCK) : net.minecraft.init.Blocks.IRON_BLOCK)));
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
	public void addToTab(Block block) throws TabNotFoundException, ItemNotFoundException {
		addToTab(block, retrospectiveTabGeneration);
	}

	@Override
	public void addToTab(Item item) throws TabNotFoundException, ItemNotFoundException {
		addToTab(item, retrospectiveTabGeneration);
	}

	@Override
	public String[] getTabs() {
		return tabs.keySet().toArray(new String[0]);
	}

	@Override
	public void initialiseRetrospectiveTabGeneration() {
		retrospectiveTabGeneration = true;	
	}

	@Override
	public void executeRetrospectiveTabGeneration() {
		distributeToTabs(retrospectiveBlocks, retrospectiveItems);
	}
	
	private void distributeToTabs (List<Block> blocks, List<Item> items) {
		List<Block> cascadeBlocks = new ArrayList<>();
		List<Item> cascadeItems = new ArrayList<>();
		
		if (blocks.size() <= optimalTabSize) {
			
		} else {
			// TODO: splitting down 
		}
		
		if (items.size() <= optimalTabSize) {
			
		} else {
			// TODO: splitting down 
		}
		
		if(cascadeBlocks.isEmpty() && cascadeItems.isEmpty())
			return;
		else
			distributeToTabs(cascadeBlocks, cascadeItems);
	}
	
	@Override
	public String getTab(Item item) throws ItemNotFoundException {
		return getTabBySequence(item.getRegistryName().getResourcePath(), 
				item.getRegistryName().getResourceDomain(), item.getClass().getSimpleName());
	}

	@Override
	public String getTab(Block block) throws ItemNotFoundException {	
		return getTabBySequence(block.getRegistryName().getResourcePath(), 
				block.getRegistryName().getResourceDomain(), block.getClass().getSimpleName());
	}

	private String getTabBySequence(String path, String domain, String simpleName) throws ItemNotFoundException {
		String tab;
		
		// 1) Try to get a tab mapping based on item name and mod id
		tab = getTab(path, domain);
		if (!tab.isEmpty()) return tab;
		
		//2) Try to get a tab mapping based on item name
		tab = getTab(path);
		if (!tab.isEmpty()) return tab;
		
		//3) Try to get a tab mapping based on item class name and mod id
		tab = getTab(simpleName, domain);
		if (!tab.isEmpty()) return tab;
		
		//4) Try to get a tab mapping based on item class name only
		tab = getTab(simpleName);
		if (!tab.isEmpty()) return tab;
		
		throw new ItemNotFoundException(path);
	}

	@Override
	public void addToTab(Block block, boolean retrospectiveTabGeneration) throws ItemNotFoundException, TabNotFoundException {
		if (retrospectiveTabGeneration) {
			retrospectiveBlocks.add(block);
		} else {
			addToTab(getTab(block), block);
		}
	}

	@Override
	public void addToTab(Item item, boolean retrospectiveTabGeneration) throws ItemNotFoundException, TabNotFoundException {
		if (retrospectiveTabGeneration) {
			retrospectiveItems.add(item);
		} else {
			addToTab(getTab(item), item);
		}
	}

	@Override
	public void setOptimalTabSize(int value) {
		optimalTabSize = value;
	}
}
