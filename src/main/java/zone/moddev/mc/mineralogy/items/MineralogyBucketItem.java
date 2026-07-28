package zone.moddev.mc.mineralogy.items;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class MineralogyBucketItem extends BucketItem {
	private final Supplier<? extends Fluid> fluidSupplier;

	public MineralogyBucketItem(Supplier<? extends Fluid> fluid, Item.Properties properties) {
		super(fluid, properties);
		this.fluidSupplier = fluid;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		Fluid fluid = getFluid();
		BlockHitResult hit = getPlayerPOVHitResult(world, player,
				fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
		InteractionResultHolder<ItemStack> eventResult =
				net.minecraftforge.event.ForgeEventFactory.onBucketUse(player, world, held, hit);
		if (eventResult != null) {
			return eventResult;
		}
		if (hit.getType() == HitResult.Type.MISS || hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(held);
		}

		BlockPos hitPos = hit.getBlockPos();
		Direction direction = hit.getDirection();
		BlockPos placePos = hitPos.relative(direction);
		if (!world.mayInteract(player, hitPos) || !player.mayUseItemAt(placePos, direction, held)) {
			return InteractionResultHolder.fail(held);
		}

		if (fluid == Fluids.EMPTY) {
			BlockState targetState = world.getBlockState(hitPos);
			if (targetState.getBlock() instanceof BucketPickup) {
				BucketPickup pickup = (BucketPickup) targetState.getBlock();
				ItemStack filled = pickup.pickupBlock(world, hitPos, targetState);
				if (!filled.isEmpty()) {
					player.awardStat(Stats.ITEM_USED.get(this));
					pickup.getPickupSound(targetState).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
					world.gameEvent(player, GameEvent.FLUID_PICKUP, hitPos);
					ItemStack result = ItemUtils.createFilledResult(held, player, filled);
					if (!world.isClientSide) {
						CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, filled);
					}
					return InteractionResultHolder.sidedSuccess(result, world.isClientSide());
				}
			}
			return InteractionResultHolder.fail(held);
		}

		BlockState targetState = world.getBlockState(hitPos);
		BlockPos actualPlacePos = canBlockContainFluid(world, hitPos, targetState) ? hitPos : placePos;
		if (emptyContents(player, world, actualPlacePos, hit, held)) {
			checkExtraContent(player, world, held, actualPlacePos);
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, actualPlacePos, held);
			}
			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(held, player), world.isClientSide());
		}
		return InteractionResultHolder.fail(held);
	}

	@Override
	public boolean emptyContents(@Nullable Player player, Level world, BlockPos pos,
			@Nullable BlockHitResult hit, @Nullable ItemStack container) {
		Fluid fluid = getFluid();
		if (!(fluid instanceof FlowingFluid)) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Material material = state.getMaterial();
		boolean canReplace = state.canBeReplaced(fluid);
		boolean canPlace = state.isAir() || canReplace
				|| block instanceof LiquidBlockContainer
						&& ((LiquidBlockContainer) block).canPlaceLiquid(world, pos, state, fluid);
		Optional<net.minecraftforge.fluids.FluidStack> containedFluid =
				Optional.ofNullable(container).flatMap(FluidUtil::getFluidContained);

		if (!canPlace) {
			return hit != null && emptyContents(player, world, hit.getBlockPos().relative(hit.getDirection()),
					null, container);
		}
		if (world.dimensionType().ultraWarm() && containedFluid.isPresent()
				&& fluid.getAttributes().doesVaporize(world, pos, containedFluid.get())) {
			fluid.getAttributes().vaporize(player, world, pos, containedFluid.get());
			return true;
		}
		if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
					2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			for (int i = 0; i < 8; i++) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(),
						z + Math.random(), 0.0D, 0.0D, 0.0D);
			}
			return true;
		}
		if (block instanceof LiquidBlockContainer
				&& ((LiquidBlockContainer) block).canPlaceLiquid(world, pos, state, fluid)) {
			((LiquidBlockContainer) block).placeLiquid(world, pos, state, ((FlowingFluid) fluid).getSource(false));
			playEmptySound(player, world, pos);
			return true;
		}
		if (!world.isClientSide && canReplace && !material.isLiquid()) {
			world.destroyBlock(pos, true);
		}
		if (!world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11)
				&& !state.getFluidState().isSource()) {
			return false;
		}
		playEmptySound(player, world, pos);
		return true;
	}

	@Override
	public Fluid getFluid() {
		return fluidSupplier.get();
	}

	@Override
	protected void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos) {
		Fluid fluid = getFluid();
		SoundEvent sound = fluid.getAttributes().getEmptySound();
		if (sound == null) {
			sound = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		}
		world.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new FluidBucketWrapper(stack);
	}

	private boolean canBlockContainFluid(Level world, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof LiquidBlockContainer
				&& ((LiquidBlockContainer) state.getBlock()).canPlaceLiquid(world, pos, state, getFluid());
	}
}
