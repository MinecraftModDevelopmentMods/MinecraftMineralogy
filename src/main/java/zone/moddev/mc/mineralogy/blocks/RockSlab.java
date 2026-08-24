package zone.moddev.mc.mineralogy.blocks;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class RockSlab extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", EnumFacing.values());
	private static final double THICKNESS = 8.0D;
	private static final VoxelShape[] SHAPES = new VoxelShape[EnumFacing.values().length];

	static {
		SHAPES[EnumFacing.DOWN.ordinal()] = Block.makeCuboidShape(0.0D, THICKNESS, 0.0D, 16.0D, 16.0D, 16.0D);
		SHAPES[EnumFacing.UP.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, THICKNESS, 16.0D);
		SHAPES[EnumFacing.NORTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, THICKNESS, 16.0D, 16.0D, 16.0D);
		SHAPES[EnumFacing.SOUTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, THICKNESS);
		SHAPES[EnumFacing.WEST.ordinal()] = Block.makeCuboidShape(THICKNESS, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		SHAPES[EnumFacing.EAST.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, THICKNESS, 16.0D, 16.0D);
	}

	private final String doubleSlabName;
	private final int toolHardnessLevel;

	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound) {
		this(hardness, blastResistance, toolHardnessLevel, sound, null, "");
	}

	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name) {
		this(hardness, blastResistance, toolHardnessLevel, sound, name, "");
	}

	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name,
			String doubleSlabName) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hardness, blastResistance).sound(sound));
		this.toolHardnessLevel = toolHardnessLevel;
		this.doubleSlabName = doubleSlabName;
		this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, EnumFacing.UP));

		if (name != null) {
			this.setRegistryName(name);
		}
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return SHAPES[state.get(FACING).ordinal()];
	}

	@Override
	public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return getShape(state, world, pos);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, IBlockState state, BlockPos pos, EnumFacing face) {
		return state.get(FACING) == face ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public IBlockState getStateForPlacement(net.minecraft.item.BlockItemUseContext context) {
		IBlockState state = this.getDefaultState().with(FACING, getPlacementFacing(context));
		return state.isValidPosition(context.getWorld(), context.getPos()) ? state : null;
	}

	@Override
	public boolean isValidPosition(IBlockState state, IWorldReaderBase world, BlockPos pos) {
		return canPlaceAtAnyFace(world, pos);
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (this.doubleSlabName == null || this.doubleSlabName.isEmpty() || facing != state.get(FACING)) {
			return super.onBlockActivated(state, world, pos, player, hand, facing, hitX, hitY, hitZ);
		}

		ItemStack held = player.getHeldItem(hand);
		ResourceLocation slabItemName = held.isEmpty() ? null : held.getItem().getRegistryName();

		if (!this.getRegistryName().equals(slabItemName)) {
			return super.onBlockActivated(state, world, pos, player, hand, facing, hitX, hitY, hitZ);
		}

		Block doubleSlab = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, this.doubleSlabName));
		if (!(doubleSlab instanceof DoubleSlab)) {
			return super.onBlockActivated(state, world, pos, player, hand, facing, hitX, hitY, hitZ);
		}

		world.setBlockState(pos, doubleSlab.getDefaultState());
		if (!player.isCreative()) {
			held.shrink(1);
		}

		return true;
	}

	@Override
	public ToolType getHarvestTool(IBlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(IBlockState state) {
		return toolHardnessLevel;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
		builder.add(FACING);
	}

	private static boolean canPlaceAt(IBlockReader world, BlockPos pos, EnumFacing facing) {
		BlockPos supportPos = pos.offset(facing.getOpposite());
		IBlockState support = world.getBlockState(supportPos);
		return support.getBlockFaceShape(world, supportPos, facing) != BlockFaceShape.UNDEFINED;
	}

	private static boolean canPlaceAtAnyFace(IBlockReader world, BlockPos pos) {
		for (EnumFacing facing : FACING.getAllowedValues()) {
			if (canPlaceAt(world, pos, facing)) {
				return true;
			}
		}

		return false;
	}

	private static EnumFacing getPlacementFacing(net.minecraft.item.BlockItemUseContext context) {
		EnumFacing face = context.getFace();
		float hitX = context.getHitX();
		float hitY = context.getHitY();
		float hitZ = context.getHitZ();
		float up;
		float right;
		EnumFacing.Axis upRotationAxis;
		EnumFacing.Axis rightRotationAxis;

		switch (face) {
			case UP:
				up = hitZ - 0.5F;
				right = hitX - 0.5F;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Z;
				break;
			case EAST:
				up = hitY - 0.5F;
				right = hitZ - 0.5F;
				upRotationAxis = EnumFacing.Axis.Z;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			case SOUTH:
				up = 0.5F - hitY;
				right = 0.5F - hitX;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			case DOWN:
				up = 0.5F - hitZ;
				right = 0.5F - hitX;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Z;
				break;
			case WEST:
				up = 0.5F - hitY;
				right = 0.5F - hitZ;
				upRotationAxis = EnumFacing.Axis.Z;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			case NORTH:
				up = hitY - 0.5F;
				right = hitX - 0.5F;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			default:
				return face;
		}

		if (Math.abs(up) < 0.25F && Math.abs(right) < 0.25F) {
			return face;
		}

		boolean upOrRight = up + right > 0;
		boolean upOrLeft = up - right > 0;

		if (upOrRight) {
			return upOrLeft ? face.rotateAround(upRotationAxis)
					: face.rotateAround(rightRotationAxis).getOpposite();
		}

		return upOrLeft ? face.rotateAround(rightRotationAxis)
				: face.rotateAround(upRotationAxis).getOpposite();
	}
}
