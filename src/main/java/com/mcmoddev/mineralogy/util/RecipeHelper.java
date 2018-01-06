package com.mcmoddev.mineralogy.util;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeHelper {
	public static ShapedOreRecipe addShapedOreRecipe(String name, ItemStack output, Object... args) {
		return addShapedOreRecipe(Mineralogy.MODID, name, output, args);
	}

	public static ShapedOreRecipe addShapedOreRecipe(String domain, String name, ItemStack output,  Object... args) {
		ShapedOreRecipe newRecipe = new ShapedOreRecipe(new ResourceLocation(domain, name), output, args);
		newRecipe.setRegistryName(name);
		
		MineralogyRegistry.MineralogyRecipeRegistry.put(name, newRecipe);
		
		return newRecipe;
	}

	public static ShapelessOreRecipe addShapelessOreRecipe(String name, ItemStack output, Object... args) {
		return addShapelessOreRecipe(Mineralogy.MODID, name, output, args);
	}

	public static ShapelessOreRecipe addShapelessOreRecipe(String domain, String name, ItemStack output, Object... args) {
		ShapelessOreRecipe newRecipe = new ShapelessOreRecipe(new ResourceLocation(domain, name), output, args);
		newRecipe.setRegistryName(name);
		
		MineralogyRegistry.MineralogyRecipeRegistry.put(name, newRecipe);
		
		return newRecipe;
	}
}
