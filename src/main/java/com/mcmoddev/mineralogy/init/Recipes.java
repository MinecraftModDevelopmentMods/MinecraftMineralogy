package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Constants;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.util.BlockItemPair;
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

		Item mineralFertilizer = IoC.resolve(Item.class, "fertilizer", Mineralogy.MODID);
		Item blockGypsum = IoC.resolve(BlockItemPair.class, "blockGypsum", Mineralogy.MODID).PairedItem;
		
		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_SUGAR", new ItemStack(Items.GUNPOWDER, 4),
				new Object[] {  Ingredient.fromStacks(new ItemStack(Items.SUGAR)), "dustNitrate",
						"dustSulfur" });
		
		RecipeHelper.addShapelessOreRecipe(Constants.MINERALFERTILIZER, new ItemStack(mineralFertilizer, 1),
				new Object[] { "dustNitrate", "dustPhosphorous" } );

		RecipeHelper.addShapelessOreRecipe(Constants.COBBLESTONE.toUpperCase(), new ItemStack(Blocks.COBBLESTONE, 4),
				Ingredient.fromStacks(new ItemStack(Blocks.STONE)), Ingredient.fromStacks(new ItemStack(Blocks.STONE)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)),
				Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)));
		
		RecipeHelper.addShapedOreRecipe(Constants.GYPSUM.toLowerCase(), new ItemStack(blockGypsum, 1), "xxx", "xxx", "xxx", 'x', "dustGypsum");
		
		Item dryWallWhite = IoC.resolve(BlockItemPair.class, "drywall15", Mineralogy.MODID).PairedItem;
		
		RecipeHelper.addShapedOreRecipe(Constants.DRYWALL, new ItemStack(dryWallWhite, 3), "pgp", "pgp", "pgp", 'p', "paper",
				'g', "dustGypsum");

		for (int i = 0; i < 16; i++) {
			Item dryWall = IoC.resolve(BlockItemPair.class, "drywall" + i, Mineralogy.MODID).PairedItem;
			
			RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(dryWall, 1),
					 new Object[] { "drywallWhite",
					Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)) } );
		}

		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_COAL", new ItemStack(Items.GUNPOWDER, 4),
				new Object[] {  Ingredient.fromStacks(new ItemStack(Items.COAL)), "dustNitrate",
				"dustSulfur" });
		
		RecipeHelper.addShapelessOreRecipe(Constants.GUNPOWDER + "_FROM_CARBON", new
					ItemStack(Items.GUNPOWDER, 4), new Object[] { "dustCarbon", "dustNitrate", "dustSulfur" });
		
		initDone = true;
	}
}
