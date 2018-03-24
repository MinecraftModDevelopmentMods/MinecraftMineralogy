package com.mcmoddev.mineralogy.lib.interfaces;

import java.util.Comparator;

import com.mcmoddev.mineralogy.lib.util.MMDCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IMMDCreativeTab {

	boolean hasSearchBar();

	void displayAllRelevantItems(NonNullList<ItemStack> itemList);

	ItemStack getTabIconItem();

	void setSortingAlgorithm(Comparator<ItemStack> comparator);

	void setTabIconItem();
	void setTabIconItem(Block iconBlock);

	void setTabIconItem(Item iconItem);

	void setTabIconItem(ItemStack iconItem);
	
	MMDCreativeTab Initialise();
}