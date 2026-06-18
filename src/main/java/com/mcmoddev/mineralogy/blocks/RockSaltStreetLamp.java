package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Random;

public class RockSaltStreetLamp extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	private static final VoxelShape STANDING_SHAPE = Block.makeCuboidShape(6.4D, 0.0D, 6.4D, 9.6D, 28.8D, 9.6D);

	public RockSaltStreetLamp() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.0F)
				.setLightLevel(state -> 15).sound(SoundType.METAL));
		this.setRegistryName("rocksaltstreetlamp");
		this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, Direction.UP));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return STANDING_SHAPE;
	}

	public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = this.getDefaultState().with(FACING, Direction.UP);
		return state.isValidPosition(context.getWorld(), context.getPos()) ? state : null;
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState,
			IWorld world, BlockPos currentPos, BlockPos facingPos) {
		return !state.isValidPosition(world, currentPos) ? Blocks.AIR.getDefaultState() : state;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return world.getBlockState(pos.up()).isAir()
				&& world.getBlockState(pos.down()).isSolidSide(world, pos.down(), Direction.UP);
	}

	@Override
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		world.addParticle(ParticleTypes.SMOKE,
				(double) pos.getX() + 0.5D,
				(double) pos.getY() + 1.92D,
				(double) pos.getZ() + 0.5D,
				0.0D, 0.0D, 0.0D);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
