package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
 
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
	 
	 @Override
	 public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
	         return true;
	 }
 }