package zone.moddev.mc.mineralogy.blocks;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class MineralogyLiquidBlock extends LiquidBlock {
	private final Supplier<? extends FlowingFluid> fluidSupplier;

	public MineralogyLiquidBlock(Supplier<? extends FlowingFluid> fluid, BlockBehaviour.Properties properties) {
		super(fluid, properties);
		this.fluidSupplier = fluid;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return !state.getFluidState().is(FluidTags.LAVA);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
		return adjacentState.getFluidState().getType().isSame(getFluid());
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		scheduleFluidTick(world, pos, state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState adjacentState,
			LevelAccessor world, BlockPos pos, BlockPos adjacentPos) {
		if (state.getFluidState().isSource() || adjacentState.getFluidState().isSource()) {
			scheduleFluidTick(world, pos, state);
		}
		return state;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos,
			boolean isMoving) {
		scheduleFluidTick(world, pos, state);
	}

	@Override
	public ItemStack pickupBlock(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		if (state.getValue(LEVEL) == 0) {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
			return new ItemStack(getFluid().getBucket());
		}
		return ItemStack.EMPTY;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return getFluid().getPickupSound();
	}

	@Override
	public FlowingFluid getFluid() {
		return fluidSupplier.get();
	}

	private void scheduleFluidTick(LevelAccessor world, BlockPos pos, BlockState state) {
		world.scheduleTick(pos, state.getFluidState().getType(), getFluid().getTickDelay(world));
	}
}
