package com.mcmoddev.mineralogy.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mcmoddev.mineralogy.util.BlockItemPair;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;

public class MineralogyRegistry {
	public static final List<Block> sedimentaryStones = new ArrayList<>(); // stone block replacements that are Sedimentary
	public static final List<Block> metamorphicStones = new ArrayList<>(); // stone block replacements that are Metamorphic
	public static final List<Block> igneousStones = new ArrayList<>(); // stone block replacements that are Igneous
	public static final Map<String, BlockItemPair> MineralogyBlockRegistry = new HashMap<>(); // all blocks used in this mod (blockID, BlockItemPair)
	public static final Map<String, Item> MineralogyItemRegistry = new HashMap<>(); // all items used in this mod (itemID, item)
	public static final Map<String, IRecipe> MineralogyRecipeRegistry = new HashMap<>(); // all recipes used in this mod (recipeID, IRecipe)

	public static final Map<String, Block> BlocksToRegister = new HashMap<>(); // all blocks used in this mod (blockID, BlockItemPair)
	public static final Map<String, Item> ItemsToRegister = new HashMap<>(); // all items used in this mod (itemID, item)
}
