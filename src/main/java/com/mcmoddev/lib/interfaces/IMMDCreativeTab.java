package com.mcmoddev.lib.interfaces;

import java.util.Comparator;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMMDCreativeTab {

	boolean hasSearchBar();

	void displayAllRelevantItems(NonNullList<ItemStack> itemList);

	ItemStack getTabIconItem();

	void setSortingAlgorithm(Comparator<ItemStack> comparator);

	void setTabIconItem(Block iconBlock);

	void setTabIconItem(Item iconItem);

	void setTabIconItem(ItemStack iconItem);

}