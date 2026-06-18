package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class RockRelief extends RockSlab {
	private static final double THICKNESS = 1.12D;
	private static final VoxelShape[] SHAPES = new VoxelShape[EnumFacing.values().length];

	static {
		SHAPES[EnumFacing.DOWN.ordinal()] = Block.makeCuboidShape(0.0D, 16.0D - THICKNESS, 0.0D,
				16.0D, 16.0D, 16.0D);
		SHAPES[EnumFacing.UP.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D,
				16.0D, THICKNESS, 16.0D);
		SHAPES[EnumFacing.NORTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 16.0D - THICKNESS,
				16.0D, 16.0D, 16.0D);
		SHAPES[EnumFacing.SOUTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D,
				16.0D, 16.0D, THICKNESS);
		SHAPES[EnumFacing.WEST.ordinal()] = Block.makeCuboidShape(16.0D - THICKNESS, 0.0D, 0.0D,
				16.0D, 16.0D, 16.0D);
		SHAPES[EnumFacing.EAST.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D,
				THICKNESS, 16.0D, 16.0D);
	}

	public RockRelief(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name) {
		super(hardness, blastResistance, toolHardnessLevel, sound, name);
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return SHAPES[state.get(FACING).ordinal()];
	}

	@Override
	public VoxelShape getRenderShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return VoxelShapes.empty();
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}
