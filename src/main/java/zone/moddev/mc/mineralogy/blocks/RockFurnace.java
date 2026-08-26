package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.block.Block;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class RockFurnace extends ContainerBlock {
	public static final net.minecraft.state.DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static boolean keepInventory;

	private final boolean burning;
	private final float burnModifier;
	private final int toolHardnessLevel;

	public RockFurnace(float hardness, float blastResistance, int toolHardnessLevel, boolean burning,
			float burnModifier, String name) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hardness, blastResistance)
				.sound(SoundType.STONE).lightValue(burning ? 14 : 0));
		this.burning = burning;
		this.burnModifier = burnModifier;
		this.toolHardnessLevel = toolHardnessLevel;
		this.setRegistryName(name);
		this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, Direction.NORTH));
	}

	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		if (stack.hasDisplayName()) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityRockFurnace) {
				((TileEntityRockFurnace) tileEntity).setCustomInventoryName(stack.getDisplayName());
			}
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand hand, BlockRayTraceResult hit) {
		if (world.isRemote) {
			return true;
		}

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityRockFurnace) {
			player.openContainer((TileEntityRockFurnace) tileEntity);
			player.addStat(Stats.INTERACT_WITH_FURNACE);
		}

		return true;
	}

	public static void setState(boolean active, World world, BlockPos pos) {
		BlockState oldState = world.getBlockState(pos);
		Block oldBlock = oldState.getBlock();
		Block newBlock = getStateBlock(oldBlock, active);

		if (!(newBlock instanceof RockFurnace) || newBlock == oldBlock) {
			return;
		}

		TileEntity tileEntity = world.getTileEntity(pos);
		keepInventory = true;
		world.setBlockState(pos, newBlock.getDefaultState().with(FACING, oldState.get(FACING)), 3);
		keepInventory = false;

		if (tileEntity != null) {
			tileEntity.validate();
			world.setTileEntity(pos, tileEntity);
		}
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new TileEntityRockFurnace(burnModifier);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.singletonList(new ItemStack(getUnlitBlock(state.getBlock())));
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			if (!keepInventory) {
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity instanceof TileEntityRockFurnace) {
					InventoryHelper.dropInventoryItems(world, pos, (TileEntityRockFurnace) tileEntity);
					world.updateComparatorOutputLevel(pos, this);
				}
			}

			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
		return Container.calcRedstone(world.getTileEntity(pos));
	}

	@Override
	public ItemStack getItem(IBlockReader world, BlockPos pos, BlockState state) {
		return new ItemStack(getUnlitBlock(state.getBlock()));
	}

	@Override
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		if (!burning) {
			return;
		}

		Direction facing = state.get(FACING);
		double x = (double) pos.getX() + 0.5D;
		double y = (double) pos.getY();
		double z = (double) pos.getZ() + 0.5D;

		if (random.nextDouble() < 0.1D) {
			world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F,
					false);
		}

		double offset = 0.52D;
		double randomOffset = random.nextDouble() * 0.6D - 0.3D;
		Direction.Axis axis = facing.getAxis();
		double xOffset = axis == Direction.Axis.X ? (double) facing.getXOffset() * offset : randomOffset;
		double zOffset = axis == Direction.Axis.Z ? (double) facing.getZOffset() * offset : randomOffset;

		world.addParticle(ParticleTypes.SMOKE, x + xOffset, y + random.nextDouble() * 6.0D / 16.0D, z + zOffset,
				0.0D, 0.0D, 0.0D);
		world.addParticle(ParticleTypes.FLAME, x + xOffset, y + random.nextDouble() * 6.0D / 16.0D, z + zOffset,
				0.0D, 0.0D, 0.0D);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
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
	public ToolType getHarvestTool(BlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(BlockState state) {
		return toolHardnessLevel;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	public float getBurnModifier() {
		return burnModifier;
	}

	public boolean isBurningVariant() {
		return burning;
	}

	private static Block getStateBlock(Block block, boolean active) {
		ResourceLocation name = block.getRegistryName();
		if (name == null) {
			return block;
		}

		String path = name.getPath();
		if (active && !path.startsWith("lit_")) {
			path = "lit_" + path;
		} else if (!active && path.startsWith("lit_")) {
			path = path.substring(4);
		}

		Block stateBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, path));
		return stateBlock == null ? block : stateBlock;
	}

	private static Block getUnlitBlock(Block block) {
		return getStateBlock(block, false);
	}
}
