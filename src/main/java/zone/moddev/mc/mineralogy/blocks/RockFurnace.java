package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;
import com.mojang.serialization.MapCodec;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.init.TileEntities;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.Containers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraftforge.registries.ForgeRegistries;

public class RockFurnace extends BaseEntityBlock implements NamedMineralogyBlock {
	public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static boolean keepInventory;

	private final boolean burning;
	private final float burnModifier;
	private final int toolHardnessLevel;
	private final String registryPath;

	public RockFurnace(float hardness, float blastResistance, int toolHardnessLevel, boolean burning,
			float burnModifier, String name) {
		super(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STONE).strength(hardness, blastResistance)
				.sound(SoundType.STONE).lightLevel(state -> burning ? 14 : 0).requiresCorrectToolForDrops());
		this.burning = burning;
		this.burnModifier = burnModifier;
		this.toolHardnessLevel = toolHardnessLevel;
		this.registryPath = name;
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return MapCodec.unit(this);
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity instanceof TileEntityRockFurnace) {
				((TileEntityRockFurnace) tileEntity).setCustomInventoryName(stack.getHoverName());
			}
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileEntityRockFurnace) {
			player.openMenu((TileEntityRockFurnace) tileEntity);
			player.awardStat(Stats.INTERACT_WITH_FURNACE);
		}

		return InteractionResult.SUCCESS;
	}

	public static void setState(boolean active, Level world, BlockPos pos) {
		BlockState oldState = world.getBlockState(pos);
		Block oldBlock = oldState.getBlock();
		Block newBlock = getStateBlock(oldBlock, active);

		if (!(newBlock instanceof RockFurnace) || newBlock == oldBlock) {
			return;
		}

		BlockEntity tileEntity = world.getBlockEntity(pos);
		keepInventory = true;
		world.setBlock(pos, newBlock.defaultBlockState().setValue(FACING, oldState.getValue(FACING)), 3);
		keepInventory = false;

		if (tileEntity != null) {
			tileEntity.clearRemoved();
			world.setBlockEntity(tileEntity);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityRockFurnace(pos, state, burnModifier);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> blockEntityType) {
		return world.isClientSide ? null
				: createTickerHelper(blockEntityType, TileEntities.rock_furnace, TileEntityRockFurnace::serverTick);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.singletonList(new ItemStack(getUnlitBlock(state.getBlock())));
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			if (!keepInventory) {
				BlockEntity tileEntity = world.getBlockEntity(pos);
				if (tileEntity instanceof TileEntityRockFurnace) {
					Containers.dropContents(world, pos, (TileEntityRockFurnace) tileEntity);
					world.updateNeighbourForOutputSignal(pos, this);
				}
			}

			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos,
			Player player) {
		return new ItemStack(getUnlitBlock(state.getBlock()));
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (!burning) {
			return;
		}

		Direction facing = state.getValue(FACING);
		double x = (double) pos.getX() + 0.5D;
		double y = (double) pos.getY();
		double z = (double) pos.getZ() + 0.5D;

		if (random.nextDouble() < 0.1D) {
			world.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F,
					false);
		}

		double offset = 0.52D;
		double randomOffset = random.nextDouble() * 0.6D - 0.3D;
		Direction.Axis axis = facing.getAxis();
		double xOffset = axis == Direction.Axis.X ? (double) facing.getStepX() * offset : randomOffset;
		double zOffset = axis == Direction.Axis.Z ? (double) facing.getStepZ() * offset : randomOffset;

		world.addParticle(ParticleTypes.SMOKE, x + xOffset, y + random.nextDouble() * 6.0D / 16.0D, z + zOffset,
				0.0D, 0.0D, 0.0D);
		world.addParticle(ParticleTypes.FLAME, x + xOffset, y + random.nextDouble() * 6.0D / 16.0D, z + zOffset,
				0.0D, 0.0D, 0.0D);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	public float getBurnModifier() {
		return burnModifier;
	}

	public boolean isBurningVariant() {
		return burning;
	}

	private static Block getStateBlock(Block block, boolean active) {
		ResourceLocation name = ForgeRegistries.BLOCKS.getKey(block);
		if (name == null) {
			return block;
		}

		String path = name.getPath();
		if (active && !path.startsWith("lit_")) {
			path = "lit_" + path;
		} else if (!active && path.startsWith("lit_")) {
			path = path.substring(4);
		}

		Block stateBlock = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, path));
		return stateBlock == null ? block : stateBlock;
	}

	private static Block getUnlitBlock(Block block) {
		return getStateBlock(block, false);
	}
}
