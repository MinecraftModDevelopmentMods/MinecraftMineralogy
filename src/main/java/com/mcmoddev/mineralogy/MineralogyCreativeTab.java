package com.mcmoddev.mineralogy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class MineralogyCreativeTab extends CreativeTabs {

	private static MineralogyCreativeTab _instance; 
	
	public static MineralogyCreativeTab instance(String label) {
		if (_instance == null)
			_instance = new MineralogyCreativeTab(label);
		
		return _instance;
	}
	
	private MineralogyCreativeTab(String label) {
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
