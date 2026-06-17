package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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

public class RockSaltStreetLamp extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", EnumFacing.values());
	private static final VoxelShape STANDING_SHAPE = Block.makeCuboidShape(6.4D, 0.0D, 6.4D, 9.6D, 28.8D, 9.6D);

	public RockSaltStreetLamp() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.0F).lightValue(15).sound(SoundType.METAL));
		this.setRegistryName("rocksaltstreetlamp");
		this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, EnumFacing.UP));
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return STANDING_SHAPE;
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
		IBlockState state = this.getDefaultState().with(FACING, EnumFacing.UP);
		return state.isValidPosition(context.getWorld(), context.getPos()) ? state : null;
	}

	@Override
	public IBlockState updatePostPlacement(IBlockState state, EnumFacing facing, IBlockState facingState,
			IWorld world, BlockPos currentPos, BlockPos facingPos) {
		return !state.isValidPosition(world, currentPos) ? Blocks.AIR.getDefaultState() : state;
	}

	@Override
	public boolean isValidPosition(IBlockState state, IWorldReaderBase world, BlockPos pos) {
		return world.getBlockState(pos.up()).isAir()
				&& world.getBlockState(pos.down()).canPlaceTorchOnTop(world, pos.down());
	}

	@Override
	public void animateTick(IBlockState state, World world, BlockPos pos, Random random) {
		world.spawnParticle(Particles.SMOKE,
				(double) pos.getX() + 0.5D,
				(double) pos.getY() + 1.92D,
				(double) pos.getZ() + 0.5D,
				0.0D, 0.0D, 0.0D);
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
