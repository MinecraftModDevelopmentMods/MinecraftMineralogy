package com.mcmoddev.mineralogy.factory;

import com.mcmoddev.lib.interfaces.IDynamicTabProvider;
import com.mcmoddev.lib.util.DynamicTabProvider;
import com.mcmoddev.mineralogy.Mineralogy;

public class TabProviderFactory {

	public static IDynamicTabProvider getTabProvider() {
		IDynamicTabProvider tabProvider = new DynamicTabProvider();
		
		tabProvider.addTab("Block", true, Mineralogy.MODID);
		tabProvider.addTab("Item", true, Mineralogy.MODID);
		
		return tabProvider;
	}
}
