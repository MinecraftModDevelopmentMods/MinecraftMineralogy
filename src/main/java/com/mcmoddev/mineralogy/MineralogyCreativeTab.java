package com.mcmoddev.mineralogy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class MineralogyCreativeTab extends CreativeTabs {

	public MineralogyCreativeTab(String label) {
		super(label);
		setBackgroundImageName("item_search.png");
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Blocks.STONE);
	}

	@Override
	public boolean hasSearchBar() {
		return true;
	}
}
