package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.util.RecipeHelper;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class Recipes {
	private static boolean initDone = false;	

	protected Recipes() {
		throw new IllegalAccessError("Not a instantiable class");
	}
	
	public static void Init() {
		if (initDone) {
			return;
		}
		
		MinIoC IoC = MinIoC.getInstance();

		Item sulfurPowder = IoC.resolve(Item.class, "dustSulfur", Mineralogy.MODID);
		Item phosphorousPowder = IoC.resolve(Item.class, "dustPhosphorous", Mineralogy.MODID);
		Item nitratePowder = IoC.resolve(Item.class, "dustNitrate", Mineralogy.MODID); 
		Item mineralFertilizer = IoC.resolve(Item.class, "fertilizer", Mineralogy.MODID);
		
		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_SUGAR", new ItemStack(Items.GUNPOWDER, 4),
				Ingredient.fromStacks(new ItemStack(Items.SUGAR)), Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(sulfurPowder)));
		
		RecipeHelper.addShapelessOreRecipe("mineralFertilizer", new ItemStack(mineralFertilizer, 1),
				Ingredient.fromStacks(new ItemStack(nitratePowder)),
				Ingredient.fromStacks(new ItemStack(phosphorousPowder)));

		RecipeHelper.addShapelessOreRecipe(Constants.COBBLESTONE.toUpperCase(), new ItemStack(Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));
	}
}
