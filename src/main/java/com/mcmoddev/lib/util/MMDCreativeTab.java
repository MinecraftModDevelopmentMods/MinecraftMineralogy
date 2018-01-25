package com.mcmoddev.lib.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mcmoddev.lib.interfaces.IMMDCreativeTab;
import com.mcmoddev.lib.interfaces.IMMDMaterial;
import com.mcmoddev.lib.interfaces.IMMDObject;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class is an MMDMaterial based Wrapper for making a CreativeTab.
 *
 * @author Jasmine Iwanek
 *
 */
public class MMDCreativeTab extends CreativeTabs implements IMMDCreativeTab {
	private ItemStack iconItem;
	private Block iconBlock;
	
	private boolean searchable;
	private Comparator<ItemStack> comparator;
	
	private static Map<Class<?>, Integer> classSortingValues = new HashMap<>();
	private static Map<IMMDMaterial, Integer> materialSortingValues = new HashMap<>();

	private static final Comparator<ItemStack> DEFAULT = new Comparator<ItemStack>() {

		@Override
		public int compare(ItemStack first, ItemStack second) {
			final int delta = getSortingValue(first) - getSortingValue(second);
			return (delta == 0) ? first.getUnlocalizedName().compareToIgnoreCase(second.getUnlocalizedName()) : delta;
		}
	};

	public static void setClassSortingList(List<Class<?>> sortingList) {				
		for (int i = 0; i < sortingList.size(); i++) {
			classSortingValues.put(sortingList.get(i), i * 100);
		}
	}
	
	protected static void setMaterialSortingValues(List<IMMDMaterial> materials) {
		for (int i = 0; i < materials.size(); i++) {
			materialSortingValues.put(materials.get(i), i * 100);
		}
	}
	
	/**
	 *
	 * @param itemStack
	 *            The ItemStack
	 * @return The output
	 */
	public static int getSortingValue(@Nonnull final ItemStack itemStack) {
		int classVal = 990000;
		int materialVal = 9900;
		if ((itemStack.getItem() instanceof ItemBlock)
				&& (((ItemBlock) itemStack.getItem()).getBlock() instanceof IMMDObject)) {
			classVal = classSortingValues.computeIfAbsent(((ItemBlock) itemStack.getItem()).getBlock().getClass(),
					(Class<?> clazz) -> 990000);
			materialVal = materialSortingValues.computeIfAbsent(
					((IMMDObject) ((ItemBlock) itemStack.getItem()).getBlock()).getMMDMaterial(),
					(IMMDMaterial material) -> 9900);
		} else if (itemStack.getItem() instanceof IMMDObject) {
			classVal = classSortingValues.computeIfAbsent(itemStack.getItem().getClass(), (Class<?> clazz) -> 990000);
			materialVal = materialSortingValues.computeIfAbsent(((IMMDObject) itemStack.getItem()).getMMDMaterial(),
					(IMMDMaterial material) -> 9900);
		}
		return classVal + materialVal + (itemStack.getMetadata() % 100);
	}
	
	public MMDCreativeTab(@Nonnull final String unlocalizedName, @Nonnull final boolean searchable) {
		this(unlocalizedName, searchable, (ItemStack) null);
	}

	public MMDCreativeTab(@Nonnull final String unlocalizedName, @Nonnull final boolean searchable, @Nullable final Block iconBlock) {
		this(unlocalizedName, searchable, new ItemStack(Item.getItemFromBlock(iconBlock)));
	}

	public MMDCreativeTab(@Nonnull final String unlocalizedName, @Nonnull final boolean searchable, @Nullable final Item iconItem) {
		this(unlocalizedName, searchable, new ItemStack(iconItem));
	}
	
	@Override
	public MMDCreativeTab Initialise() {
		if (iconItem == null && MinIoC.getInstance().resolve(ItemStack.class, "defaultIcon", Mineralogy.MODID) != null)
			this.iconItem = MinIoC.getInstance().resolve(ItemStack.class, "defaultIcon", Mineralogy.MODID);
		
		this.setSortingAlgorithm(DEFAULT);
		
		if (searchable)
			setBackgroundImageName("item_search.png");
		
		return this;
	}
	
	public MMDCreativeTab(@Nonnull final String unlocalizedName, @Nonnull final boolean searchable, @Nullable final ItemStack iconItem) {
		super(unlocalizedName);
		
		if (iconItem != null) 
			this.iconItem = iconItem;
		
		this.searchable = searchable;
	}

	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#hasSearchBar()
	 */
	@Override
	public boolean hasSearchBar() {
		return searchable;
	}

	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#displayAllRelevantItems(net.minecraft.util.NonNullList)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(@Nonnull final NonNullList<ItemStack> itemList) {
		super.displayAllRelevantItems(itemList);
		if (comparator != null) {
			itemList.sort(comparator);
		}
	}

	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#getTabIconItem()
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getTabIconItem() {
		return this.iconItem;
	}

	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#setSortingAlgorithm(java.util.Comparator)
	 */
	@Override
	public void setSortingAlgorithm(@Nonnull final Comparator<ItemStack> comparator) {
		this.comparator = comparator;
	}


	public MMDCreativeTab setIconItem(@Nonnull final Block iconBlock) {
		this.iconBlock = iconBlock;
		this.iconItem = new ItemStack(Item.getItemFromBlock(iconBlock));
		return this;
	}

	public MMDCreativeTab setIconItem(@Nonnull final Item iconItem) {
		this.iconItem = new ItemStack(iconItem);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#setTabIconItem(net.minecraft.block.Block)
	 */
	@Override
	public void setTabIconItem(@Nonnull final Block iconBlock) {
		this.iconItem = new ItemStack(Item.getItemFromBlock(iconBlock));
	}

	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#setTabIconItem(net.minecraft.item.Item)
	 */
	@Override
	public void setTabIconItem(@Nonnull final Item iconItem) {
		this.iconItem = new ItemStack(iconItem);
	}

	/* (non-Javadoc)
	 * @see com.mcmoddev.lib.util.IMMDCreativeTab#setTabIconItem(net.minecraft.item.ItemStack)
	 */
	@Override
	public void setTabIconItem(@Nonnull final ItemStack iconItem) {
		this.iconItem = iconItem;
	}

	@Override
	public void setTabIconItem() {
		if (this.iconBlock != null)
			this.setTabIconItem(this.iconBlock);
	}
}
