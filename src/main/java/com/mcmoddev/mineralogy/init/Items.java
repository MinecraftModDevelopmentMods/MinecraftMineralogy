package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.items.MineralFertilizer;
import com.mcmoddev.mineralogy.util.RecipeHelper;
import com.mcmoddev.mineralogy.util.RegistrationHelper;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class Items {
	private static boolean initDone = false;
	private static CreativeTabs mineralogyTab;
	
	public static Item gypsumPowder;
	protected static Item sulfurPowder;
	protected static Item phosphorousPowder;
	protected static Item nitratePowder; // aka "saltpeter"
	protected static Item mineralFertilizer;
	
	protected Items() {
		throw new IllegalAccessError("Not a instantiable class");
	}

	/**
	 *
	 */
	public static void init() {
		if (initDone) {
			return;
		}
		
		MinIoC IoC = MinIoC.getInstance();
		
		mineralogyTab = MinIoC.getInstance().resolve(CreativeTabs.class);
		
		gypsumPowder = addDust(Constants.GYPSUM);
		IoC.register(Item.class, gypsumPowder, "dustGypsum", Mineralogy.MODID);
		
		RecipeHelper.addShapelessOreRecipe(Constants.GYPSUM.toLowerCase() + "_dust", new ItemStack(gypsumPowder, 9), "blockGypsum");
		
		sulfurPowder = addDust(Constants.SULFUR);
		phosphorousPowder = addDust(Constants.PHOSPHOROUS);
		nitratePowder = addDust(Constants.NITRATE);
	
		mineralFertilizer = RegistrationHelper.registerItem(new MineralFertilizer(), "mineral_fertilizer")
			.setUnlocalizedName(Mineralogy.MODID + "." + "mineral_fertilizer");
		
		MineralogyRegistry.ItemsToRegister.put(Constants.FERTILIZER, mineralFertilizer);
		IoC.register(Item.class, mineralFertilizer, "fertilizer", Mineralogy.MODID);
		
		initDone = true;
	}
	
	private static Item addDust(String oreDictionaryName) {
		String dustName = oreDictionaryName.toLowerCase() + "_" + Constants.DUST;

		Item item = RegistrationHelper.registerItem(new Item(), dustName).setUnlocalizedName(Mineralogy.MODID + "." + dustName)
				.setCreativeTab(mineralogyTab);

		MineralogyRegistry.ItemsToRegister.put(Constants.DUST + oreDictionaryName, item);
		MinIoC.getInstance().register(Item.class, item, Constants.DUST + oreDictionaryName, Mineralogy.MODID);
		
		RecipeHelper.addShapelessOreRecipe(Constants.BLOCK.toLowerCase() + oreDictionaryName, new ItemStack(item, 9),
				Constants.BLOCK.toLowerCase() + oreDictionaryName);

		return item;
	}
}
