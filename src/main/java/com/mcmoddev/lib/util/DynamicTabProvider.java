package com.mcmoddev.lib.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.exceptions.ItemNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;
import com.mcmoddev.lib.interfaces.IDynamicTabProvider;
import com.mcmoddev.lib.interfaces.IMMDMaterial;
import com.mcmoddev.lib.interfaces.ITabProvider;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public final class DynamicTabProvider implements IDynamicTabProvider {
	private Map<String, MMDCreativeTab> tabs = new HashMap<>();
	private Map<String, String> tabsByMod = new HashMap<>();
	
	private Multimap<String, String> tabItemMapping = ArrayListMultimap.create();
	private IDynamicTabProvider.DefaultTabGenerationMode generationMode = DefaultTabGenerationMode.ByClass;
	
	private MinIoC IoC;
	private ItemStack defaultIcon;
	
	public DynamicTabProvider() {
		IoC = MinIoC.getInstance();
		defaultIcon = IoC.resolve(ItemStack.class, "defaultIcon", Mineralogy.MODID);
	}
	
	private MMDCreativeTab getTabByName(String tabName) throws TabNotFoundException {
		MMDCreativeTab tab = tabs.get(tabName);
		
		if (tab == null)
			throw new TabNotFoundException(tabName);
		
		return tab;
	}

	@Override
	public DynamicTabProvider addToTab(String tabName, Block block) throws TabNotFoundException {
		
		MMDCreativeTab tab = getTabByName(tabName);
		
		MinIoC.getInstance().resolve(ItemStack.class, "defaultIcon", Mineralogy.MODID);
		
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			if (tab.getTabIconItem().getItem().getRegistryName().equals(defaultIcon.getItem().getRegistryName()))
				tab.setIconItem(block);
		
		block.setCreativeTab(tab);
		return this;
	}
	
	
	
	@Override
	public DynamicTabProvider addToTab(String tabName, Item item) throws TabNotFoundException {	
		MMDCreativeTab tab = getTabByName(tabName);
		
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			if (tab.getTabIconItem().getItem().getRegistryName().equals(defaultIcon.getItem().getRegistryName()))
				tab.setIconItem(item);
			
		item.setCreativeTab(tab);
		return this;
	}

	@Override
	public DynamicTabProvider setIcon(String tabName, IMMDMaterial material) throws TabNotFoundException {
		tabs.get(tabName).setTabIconItem(new ItemStack(Item.getItemFromBlock(material.hasBlock(Names.BLOCK) ?  material.getBlock(Names.BLOCK) : net.minecraft.init.Blocks.IRON_BLOCK)));
		return this;
	}

	private Optional<String> getTab(String itemName, String modID)  {
		for (String tab : tabItemMapping.get(itemName))
			if (modID.equals(tabsByMod.get(tab))) 
				return Optional.of(tab);
		
		return Optional.empty();
	}
	
	public DynamicTabProvider setTabItemMapping(String tabName, String itemName) {
		tabItemMapping.put(itemName, tabName);
		return this;
	}

	private Optional<String> getTab(String itemName)   {
		return tabItemMapping.get(itemName).stream().findFirst();
	}

	@Override
	public DynamicTabProvider addTab(String tabName, boolean searchable, String modID) {
		if (tabs.get(tabName) != null)
			return this;
		
		MMDCreativeTab tab = new MMDCreativeTab(String.format("%s.%s", modID, tabName), searchable);
		
		tab.Initialise();
		
		tabs.put(tabName, tab);
		tabsByMod.put(tabName, modID);
		setTabItemMapping(tabName, tabName); // add a like to like mapping
		
		return this;
	}

	@Override
	public DynamicTabProvider addToTab(Block block) throws TabNotFoundException, ItemNotFoundException {
		addToTab(getTab(block), block);
		return this;
	}

	@Override
	public DynamicTabProvider addToTab(Item item) throws TabNotFoundException, ItemNotFoundException {
		addToTab(getTab(item), item);
		return this;
	}

	@Override
	public String[] getTabs() {
		return tabs.keySet().toArray(new String[0]);
	}
	
	@Override
	public String getTab(Item item) {
		return getTabBySequence(item.getRegistryName().getResourcePath(), 
				item.getRegistryName().getResourceDomain(), item.getClass().getSimpleName());
	}

	@Override
	public String getTab(Block block) {	
		return getTabBySequence(block.getRegistryName().getResourcePath(), 
				block.getRegistryName().getResourceDomain(), block.getClass().getSimpleName());
	}

	private String getTabBySequence(String path, String domain, String simpleName) {
		return getTab(path, domain) // try getting a tab mapping
				.orElseGet(() -> getTab(path) // try a tab mapping without the mod id
				.orElseGet(() -> getTab(domain, domain) // try a tab mapping without just mod id
				.orElseGet(() -> getTab(simpleName, domain) // try and map on class name and mod id
				.orElseGet(() -> getTab(simpleName) // try and map just on class name
				.orElseGet( () -> { // add a tab to match the classname 
					if (generationMode == DefaultTabGenerationMode.ByClass) {
						addTab(simpleName, true, domain); 
						return simpleName;
					} else {
						addTab(domain, true, domain); 
						return domain;
					}
					} ))))); 
	}

	@Override
	public IDynamicTabProvider setDefaultTabCreationLogic(DefaultTabGenerationMode generationMode) {
		this.generationMode = generationMode;
		return this;
	}

	@Override
	public IDynamicTabProvider setTabIcons() {
		for (Entry<String, MMDCreativeTab> tabEntry : tabs.entrySet())
			tabEntry.getValue().setTabIconItem();
		
		return this;
	}

	@Override
	public ITabProvider setIcon(String tabName, ItemStack iconItem) throws TabNotFoundException {
		tabs.get(tabName).setTabIconItem(iconItem);
		return this;
	}
}
