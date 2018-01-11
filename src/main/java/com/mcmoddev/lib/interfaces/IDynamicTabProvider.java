package com.mcmoddev.lib.interfaces;

import com.mcmoddev.lib.exceptions.ItemNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IDynamicTabProvider extends ITabProvider {
	/**
	 * Add a new tab to the set
	 * @param tabName Name of tab to add
	 * @param searchable Is the tab searchable
	 * @param modID Id of the mod related to the tab
	 */
	void addTab(String tabName, boolean searchable, String modID);
	
	/**
	 * instructs the tab provider that tabs will be created in a batch process
	 */
	void initialiseRetrospectiveTabGeneration();
	
	/**
	 * instructs the tab provider to assign and create tabs in a batch process
	 */
	void executeRetrospectiveTabGeneration();
	
	/**
	 * adds a block to a tab, determines the tab to be used
	 * @param block Block to add to the tab
	 * @param retrospectiveTabGeneration If true tabs will not be created until executeRetrospectiveTabGeneration is called
	 * @throws ItemNotFoundException There was an error finding a tab mapping for the item
	 * @throws TabNotFoundException If the tab doesn't exist
	 */
	void addToTab(Block block, boolean retrospectiveTabGeneration) throws ItemNotFoundException, TabNotFoundException;
	
	/**
	 * adds an item to a tab, determines the tab to be used
	 * @param item Item to add to the tab
	 * @param retrospectiveTabGeneration If true tabs will not be created until executeRetrospectiveTabGeneration is called
	 * @throws ItemNotFoundException There was an error finding a tab mapping for the item
	 * @throws TabNotFoundException If the tab doesn't exist
	 */
	void addToTab(Item item, boolean retrospectiveTabGeneration) throws ItemNotFoundException, TabNotFoundException;
	
	/**
	 * when retrospectively generating tabs, the amount of items/blocks ideally desired per tab
	 */
	void setOptimalTabSize(int value); 
}
