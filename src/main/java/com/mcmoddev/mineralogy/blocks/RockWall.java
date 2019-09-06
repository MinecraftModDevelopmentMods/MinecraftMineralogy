package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
 
/**
*
* @author Jasmine Iwanek
*
*/
public class RockWall extends net.minecraft.block.WallBlock {
	public RockWall(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound, String name) {
		super(Block.Properties.create(Material.ROCK).harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(hardness, blastResistance).sound(sound));
		
		this.setRegistryName(name);
	 }
	
//	@Override
//	public boolean canBeConnectedTo(BlockState state, IBlockReader world, BlockPos pos, Direction facing) {
//		// TODO Auto-generated method stub
//		//return super.canBeConnectedTo(state, world, pos, facing);
//		return true;
//	}
	
//	 @Override
//	 public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
//	 	list.add(new ItemStack(this, 1, net.minecraft.block.BlockWall.EnumType.NORMAL.getMetadata()));
//	 }
	 
//	 @Override
//	 public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
//	         return true;
//	 }
 }
