package com.mcmoddev.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import java.util.Random;

public class RockSaltLamp extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	private static final VoxelShape STANDING_SHAPE = Block.box(6.4D, 0.0D, 6.4D, 9.6D, 9.6D, 9.6D);
	private static final VoxelShape NORTH_SHAPE = Block.box(5.6D, 3.2D, 11.2D, 10.4D, 12.8D, 16.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(5.6D, 3.2D, 0.0D, 10.4D, 12.8D, 4.8D);
	private static final VoxelShape WEST_SHAPE = Block.box(11.2D, 3.2D, 5.6D, 16.0D, 12.8D, 10.4D);
	private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 3.2D, 5.6D, 4.8D, 12.8D, 10.4D);

	public RockSaltLamp() {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(0.25F)
				.lightLevel(state -> 15).sound(SoundType.STONE));
		this.setRegistryName("rocksaltlamp");
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.UP));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
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
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return net.minecraft.world.phys.shapes.Shapes.empty();
	}

	public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();

		for (Direction facing : context.getNearestLookingDirections()) {
			BlockState state = this.defaultBlockState().setValue(FACING, facing);
			if (state.canSurvive(world, pos)) {
				return state;
			}
		}

		return null;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
			LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		return facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(world, currentPos)
				? Blocks.AIR.defaultBlockState()
				: state;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos supportPos = pos.relative(facing.getOpposite());
		BlockState support = world.getBlockState(supportPos);

		if (facing == Direction.UP) {
			return support.isFaceSturdy(world, supportPos, Direction.UP);
		}
		if (facing == Direction.DOWN) {
			return support.isFaceSturdy(world, supportPos, Direction.DOWN);
		}

		return support.isFaceSturdy(world, supportPos, facing);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
		Direction facing = state.getValue(FACING);
		double x = (double) pos.getX() + 0.5D;
		double y = (double) pos.getY() + 0.7D;
		double z = (double) pos.getZ() + 0.5D;

		if (facing.getAxis().isHorizontal()) {
			Direction opposite = facing.getOpposite();
			world.addParticle(ParticleTypes.SMOKE,
					x + 0.27D * (double) opposite.getStepX(),
					y + 0.22D,
					z + 0.27D * (double) opposite.getStepZ(),
					0.0D, 0.0D, 0.0D);
		} else {
			world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
