package com.mcmoddev.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

public class RockRelief extends RockSlab {
	private static final double THICKNESS = 1.12D;
	private static final VoxelShape[] SHAPES = new VoxelShape[Direction.values().length];

	static {
		SHAPES[Direction.DOWN.ordinal()] = Block.box(0.0D, 16.0D - THICKNESS, 0.0D,
				16.0D, 16.0D, 16.0D);
		SHAPES[Direction.UP.ordinal()] = Block.box(0.0D, 0.0D, 0.0D,
				16.0D, THICKNESS, 16.0D);
		SHAPES[Direction.NORTH.ordinal()] = Block.box(0.0D, 0.0D, 16.0D - THICKNESS,
				16.0D, 16.0D, 16.0D);
		SHAPES[Direction.SOUTH.ordinal()] = Block.box(0.0D, 0.0D, 0.0D,
				16.0D, 16.0D, THICKNESS);
		SHAPES[Direction.WEST.ordinal()] = Block.box(16.0D - THICKNESS, 0.0D, 0.0D,
				16.0D, 16.0D, 16.0D);
		SHAPES[Direction.EAST.ordinal()] = Block.box(0.0D, 0.0D, 0.0D,
				THICKNESS, 16.0D, 16.0D);
	}

	public RockRelief(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name) {
		super(hardness, blastResistance, toolHardnessLevel, sound, name);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(FACING).ordinal()];
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return false;
	}
}
