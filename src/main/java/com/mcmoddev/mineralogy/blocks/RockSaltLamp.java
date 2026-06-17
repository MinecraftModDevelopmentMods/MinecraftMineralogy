package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Particles;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;

import java.util.Random;

public class RockSaltLamp extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", EnumFacing.values());

	private static final VoxelShape STANDING_SHAPE = Block.makeCuboidShape(6.4D, 0.0D, 6.4D, 9.6D, 9.6D, 9.6D);
	private static final VoxelShape NORTH_SHAPE = Block.makeCuboidShape(5.6D, 3.2D, 11.2D, 10.4D, 12.8D, 16.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.makeCuboidShape(5.6D, 3.2D, 0.0D, 10.4D, 12.8D, 4.8D);
	private static final VoxelShape WEST_SHAPE = Block.makeCuboidShape(11.2D, 3.2D, 5.6D, 16.0D, 12.8D, 10.4D);
	private static final VoxelShape EAST_SHAPE = Block.makeCuboidShape(0.0D, 3.2D, 5.6D, 4.8D, 12.8D, 10.4D);

	public RockSaltLamp() {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.25F).lightValue(15).sound(SoundType.STONE));
		this.setRegistryName("rocksaltlamp");
		this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, EnumFacing.UP));
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
		switch (state.get(FACING)) {
			case EAST:
				return EAST_SHAPE;
			case WEST:
				return WEST_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case NORTH:
				return NORTH_SHAPE;
			default:
				return STANDING_SHAPE;
		}
	}

	@Override
	public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return net.minecraft.util.math.shapes.VoxelShapes.empty();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();

		for (EnumFacing facing : context.getNearestLookingDirections()) {
			IBlockState state = this.getDefaultState().with(FACING, facing);
			if (state.isValidPosition(world, pos)) {
				return state;
			}
		}

		return null;
	}

	@Override
	public IBlockState updatePostPlacement(IBlockState state, EnumFacing facing, IBlockState facingState,
			IWorld world, BlockPos currentPos, BlockPos facingPos) {
		return facing.getOpposite() == state.get(FACING) && !state.isValidPosition(world, currentPos)
				? Blocks.AIR.getDefaultState()
				: state;
	}

	@Override
	public boolean isValidPosition(IBlockState state, IWorldReaderBase world, BlockPos pos) {
		EnumFacing facing = state.get(FACING);
		BlockPos supportPos = pos.offset(facing.getOpposite());
		IBlockState support = world.getBlockState(supportPos);

		if (facing == EnumFacing.UP) {
			return support.canPlaceTorchOnTop(world, supportPos);
		}
		if (facing == EnumFacing.DOWN) {
			return support.getBlockFaceShape(world, supportPos, EnumFacing.DOWN) == BlockFaceShape.SOLID;
		}

		return support.getBlockFaceShape(world, supportPos, facing) == BlockFaceShape.SOLID;
	}

	@Override
	public void animateTick(IBlockState state, World world, BlockPos pos, Random random) {
		EnumFacing facing = state.get(FACING);
		double x = (double) pos.getX() + 0.5D;
		double y = (double) pos.getY() + 0.7D;
		double z = (double) pos.getZ() + 0.5D;

		if (facing.getAxis().isHorizontal()) {
			EnumFacing opposite = facing.getOpposite();
			world.spawnParticle(Particles.SMOKE,
					x + 0.27D * (double) opposite.getXOffset(),
					y + 0.22D,
					z + 0.27D * (double) opposite.getZOffset(),
					0.0D, 0.0D, 0.0D);
		} else {
			world.spawnParticle(Particles.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public IBlockState rotate(IBlockState state, Rotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public IBlockState mirror(IBlockState state, Mirror mirror) {
		return state.rotate(mirror.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
		builder.add(FACING);
	}
}
