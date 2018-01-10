package com.mcmoddev.lib.interfaces;

import com.google.common.collect.ImmutableList;
import com.mcmoddev.lib.data.MaterialStats;
import com.mcmoddev.lib.data.MaterialType;
import com.mcmoddev.lib.data.Names;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public interface IMMDMaterial {

	String getName();

	String getCapitalizedName();

	/**
	 * 
	 * @return MaterialType The type of material this is
	 */
	MaterialType getType();

	String toString();

	int hashCode();

	boolean equals(Object o);

	/**
	 * Gets the amount of XP per ore block that is smelted
	 * 
	 * @return XP value per ore block
	 */
	float getOreSmeltXP();

	/**
	 * Gets the tool harvest level
	 * 
	 * @return an integer from -1 (equivalent to no tool) to 3 (diamond tool
	 *         equivalent)
	 */
	int getToolHarvestLevel();

	/**
	 * Gets the tool harvest level needed from a tool trying to mine this
	 * metal's ore and other blocks
	 * 
	 * @return an integer from -1 (equivalent to no tool) to 3 (diamond tool
	 *         equivalent)
	 */
	int getRequiredHarvestLevel();

	/**
	 * Gets the resistance of blocks made from this metal to explosions
	 * 
	 * @return the blast resistance score
	 */
	float getBlastResistance();

	/**
	 * Gets the number used to determine how quickly a block is mined with a
	 * tool made from this material
	 * 
	 * @return the number used to determine how quickly a block is mined
	 */
	float getToolEfficiency();

	/**
	 * Gets the hardness of the ore block for this material
	 * 
	 * @return the hardness of the ore block for this material
	 */
	float getOreBlockHardness();

	/**
	 * Gets the hardness for blocks made from this material
	 * 
	 * @return the hardness for blocks made from this material
	 */
	float getBlockHardness();

	/**
	 * Gets the number of uses of a tool made from this material
	 * 
	 * @return The number of uses of a tool made from this material
	 */
	int getToolDurability();

	/**
	 * Gets the number used to determine how much damage an armor item can take.
	 * 
	 * @return The number used to determine how much damage an armor item can
	 *         take.
	 */
	int getArmorMaxDamageFactor();

	/**
	 * Gets the protection value for helmets, chestplates, leg armor, and boots
	 * made from this material
	 * 
	 * @return the protection value for helmets, chestplates, leg armor, and
	 *         boots made from this material
	 */
	int[] getDamageReductionArray();

	/**
	 * Gets the base damage from attacks with tools made from this material
	 * 
	 * @return the base damage from attacks with tools made from this material
	 */
	float getBaseAttackDamage();

	/**
	 * Gets the enchantability score for this material
	 * 
	 * @return the enchantability score for this material
	 */
	int getEnchantability();

	String getEnumName();

	/**
	 * Gets the tint color for this material
	 * 
	 * @return the tint color for this material
	 */
	int getTintColor();

	/**
	 * Sets the blast resistance of the material. Should only be used as a
	 * builder method.
	 * 
	 * @param resistance
	 *            The resistance for the material.
	 * @return An instance of the material, for quality of life.
	 */
	IMMDMaterial setBlastResistance(float resistance);

	/**
	 * Sets the base weapon damage for the material. Should only be used as a
	 * builder method.
	 * 
	 * @param damage
	 *            The base damage of the material.
	 * @return An instance of the material, for quality of life.
	 */
	IMMDMaterial setBaseDamage(float damage);

	/**
	 * Adds a new item to the list of known items made from this material
	 * 
	 * @param name
	 *            The name of the item. Existing names can be found in
	 *            com.mcmoddev.lib.data.Names
	 * @param item
	 *            The item to add
	 * @return an instance of the material - QOL and call chaining
	 */
	IMMDMaterial addNewItem(Names name, Item item);

	/**
	 * Adds a new item to the list of known items made from this material
	 * 
	 * @param name
	 *            The name of the item. Existing names can be found in
	 *            com.mcmoddev.lib.data.Names
	 * @param item
	 *            The item to add
	 * @return an instance of the material - QOL and call chaining
	 */
	IMMDMaterial addNewItem(String name, Item item);

	/**
	 * Adds a new block to the list of known items made from this material
	 * 
	 * @param name
	 *            The name of the block. Existing names can be found in
	 *            com.mcmoddev.lib.data.Names
	 * @param block
	 *            The block to add
	 * @return an instance of the material - QOL and call chaining
	 */
	IMMDMaterial addNewBlock(Names name, Block block);

	/**
	 * Adds a new block to the list of known items made from this material
	 * 
	 * @param name
	 *            The name of the block. Existing names can be found in
	 *            com.mcmoddev.lib.data.Names
	 * @param block
	 *            The block to add
	 * @return an instance of the material - QOL and call chaining
	 */
	IMMDMaterial addNewBlock(String name, Block block);

	/**
	 * Get the item with name 'name' if it exists, null is returned if the item
	 * does not exist
	 * 
	 * @param name
	 *            Name of the item to retrieve
	 * @return the Item registered with the material, null if one of that name
	 *         was not registered
	 */
	Item getItem(Names name);

	/**
	 * Get the item with name 'name' if it exists, null is returned if the item
	 * does not exist
	 * 
	 * @param name
	 *            Name of the item to retrieve
	 * @return the Item registered with the material, null if one of that name
	 *         was not registered
	 */
	Item getItem(String name);

	/**
	 * Get the block with name 'name' if it exists, null is returned if the
	 * block does not exist
	 * 
	 * @param name
	 *            Name of the item to retrieve
	 * @return the Block registered with the material, null if one of that name
	 *         was not registered
	 */
	Block getBlock(Names name);

	/**
	 * Get the block with name 'name' if it exists, null is returned if the
	 * block does not exist
	 * 
	 * @param name
	 *            Name of the item to retrieve
	 * @return the Block registered with the material, null if one of that name
	 *         was not registered
	 */
	Block getBlock(String name);

	/**
	 * Get all the blocks that are made from this material
	 * @return ImmutableList&lt;Block&gt; - the blocks
	 */
	ImmutableList<Block> getBlocks();

	/**
	 * Get all the items that are made from/with this material
	 * @return ImmutableList&lt;Item&gt; - the items
	 */
	ImmutableList<Item> getItems();

	boolean hasOre();

	boolean hasBlend();

	boolean isRare();

	boolean regenerates();

	boolean hasItem(Names name);

	boolean hasItem(String name);

	boolean hasBlock(String name);

	boolean hasBlock(Names name);

	float getStat(MaterialStats name);

	void setStat(MaterialStats name, float value);

	Fluid getFluid();

	void setFluid(Fluid f);

	BlockFluidClassic getFluidBlock();

	void setFluidBlock(BlockFluidClassic b);

	void setRegenerates(boolean regen);

	Material getVanillaMaterial();

	SoundType getSoundType();

	int getSpawnSize();

	IMMDMaterial setSpawnSize(int size);

	int getDefaultDimension();

	IMMDMaterial setDefaultDimension(int dim);

}