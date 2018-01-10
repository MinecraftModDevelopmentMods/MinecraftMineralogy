package com.mcmoddev.lib.interfaces;

import com.mcmoddev.lib.exceptions.MaterialNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;


/**
 * This interface defines a tabprovider, which should provide the encapsulated
 * capability to add blocks/items to tabs
 *
 * @author SkyBlade1978
 *
 */
public interface ITabProvider {
	/**
	 * adds a block to a tab
	 * @param block Block to add to the tab
	 * @param tabName Name of the tab to add the block to
	 * @throws TabNotFoundException If the tab doesn't exist
	 */
	void addBlockToTab(String tabName, Block block) throws TabNotFoundException;
	
	/**
	 * adds an item to a tab
	 * @param item Item to add to the tab
	 * @param tabName Name of the tab to add the item to
	 * @throws TabNotFoundException If the tab doesn't exist
	 */
	void addItemToTab(String tabName, Item item) throws TabNotFoundException;
	
	/**
	 * adds a block to a tab, determines the tab to be used
	 * @param block Block to add to the tab
	 * @throws TabNotFoundException If the tab doesn't exist
	 */
	void addBlockToTab(Block block) throws TabNotFoundException;
	
	/**
	 * adds an item to a tab, determines the tab to be used
	 * @param item Item to add to the tab
	 * @throws TabNotFoundException If the tab doesn't exist
	 */
	void addItemToTab(Item item) throws TabNotFoundException;
	
	/**
	 * sets an icon to a tab
	 * @param tabName The name of the tab to set the icon for
	 * @param materialName The name of the material to use the icon from for the tab
	 * @throws TabNotFoundException There was an error setting the tab icon
	 * @throws MaterialNotFoundException There was an error finding the material
	 */
	void setIcon(String tabName, String materialName) throws TabNotFoundException, MaterialNotFoundException;
	
	/**
	 * Gets a recommended tab for an item (block or item)
	 * @param itemName The name of the type of block or item to match against a tab
	 * @param modID The preferred modID of the tab to be selected
	 * @return Returns the name of the recommended tab
	 */
	String getTab(String itemName, String modID);

	/**
	 * Gets a recommended tab for an item (block or item)
	 * @param itemName The name of the type of block or item to match against a tab
	 * @return Returns the name of the recommended tab
	 */
	String getTab(String itemName);

	/**
	 * Gets a list of all tabs
	 * @return Returns a list of all tabs
	 */
	String[] getTabs();
	
	/**
	 * tells the tabprovider which tabs should be provided for which type items/blocks
	 * @param tabName name of tab to be mapped
	 * @param itemName item or block type to map to tab
	 */
	void setTabItemMapping(String tabName, String itemName);
}
