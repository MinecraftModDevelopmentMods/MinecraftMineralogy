package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyCreativeTab;
import com.mcmoddev.mineralogy.items.MineralFertilizer;
import com.mcmoddev.mineralogy.util.RecipeHelper;
import com.mcmoddev.mineralogy.util.RegistrationHelper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class Items {
	private static boolean initDone = false;
	private static MineralogyCreativeTab mineralogyTab = MineralogyCreativeTab.instance("tabMineralogy");
	
	public static Item gypsumPowder;
	protected static Item sulphurPowder;
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
	
		gypsumPowder = addDust(Constants.GYPSUM);
		sulphurPowder = addDust(Constants.SULFUR);
		phosphorousPowder = addDust(Constants.PHOSPHOROUS);
		nitratePowder = addDust(Constants.NITRATE);
	
		mineralFertilizer = RegistrationHelper.registerItem(new MineralFertilizer(), "mineral_fertilizer")
			.setUnlocalizedName(Mineralogy.MODID + "." + "mineral_fertilizer");
		
		MineralogyRegistry.ItemsToRegister.put(Constants.FERTILIZER, mineralFertilizer);
	}
	
	private static Item addDust(String oreDictionaryName) {
		String dustName = oreDictionaryName.toLowerCase() + "_" + Constants.DUST;

		Item item = RegistrationHelper.registerItem(new Item(), dustName).setUnlocalizedName(Mineralogy.MODID + "." + dustName)
				.setCreativeTab(mineralogyTab);

		MineralogyRegistry.ItemsToRegister.put(Constants.DUST + oreDictionaryName, item);

		NonNullList<ItemStack> blocks = OreDictionary.getOres(Constants.BLOCK.toLowerCase() + oreDictionaryName);

		if (!blocks.isEmpty()) {
			RecipeHelper.addShapelessOreRecipe(Constants.BLOCK.toLowerCase() + oreDictionaryName, new ItemStack(item, 9),
					Ingredient.fromStacks(blocks.get(0)));
		}

		return item;
	}
}
