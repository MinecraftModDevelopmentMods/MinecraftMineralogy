package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
 
/**
*
* @author Jasmine Iwanek
*
*/
public class RockWall extends net.minecraft.block.BlockWall {
	public RockWall(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound) {
		super(materialBlock);
	 	this.setSoundType(sound);
	 	this.blockHardness = hardness;
	 	this.blockResistance = blastResistance;
	 	this.setHarvestLevel("pickaxe", toolHardnessLevel);
	 }
	 
	 @Override
	 public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
	 	list.add(new ItemStack(this, 1, net.minecraft.block.BlockWall.EnumType.NORMAL.getMetadata()));
	 }
 }