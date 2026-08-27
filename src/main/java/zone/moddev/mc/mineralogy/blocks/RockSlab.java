package zone.moddev.mc.mineralogy.blocks;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class RockSlab extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	private static final double THICKNESS = 8.0D;
	private static final VoxelShape[] SHAPES = new VoxelShape[Direction.values().length];

	static {
		SHAPES[Direction.DOWN.ordinal()] = Block.makeCuboidShape(0.0D, THICKNESS, 0.0D, 16.0D, 16.0D, 16.0D);
		SHAPES[Direction.UP.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, THICKNESS, 16.0D);
		SHAPES[Direction.NORTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, THICKNESS, 16.0D, 16.0D, 16.0D);
		SHAPES[Direction.SOUTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, THICKNESS);
		SHAPES[Direction.WEST.ordinal()] = Block.makeCuboidShape(THICKNESS, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		SHAPES[Direction.EAST.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, THICKNESS, 16.0D, 16.0D);
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
		this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, Direction.UP));

		if (name != null) {
			this.setRegistryName(name);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return SHAPES[state.get(FACING).ordinal()];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return getShape(state, world, pos, context);
	}

	public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(net.minecraft.item.BlockItemUseContext context) {
		BlockState state = this.getDefaultState().with(FACING, getPlacementFacing(context));
		return state.isValidPosition(context.getWorld(), context.getPos()) ? state : null;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return canPlaceAtAnyFace(world, pos);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand hand, BlockRayTraceResult hit) {
		Direction facing = hit.getFace();
		if (this.doubleSlabName == null || this.doubleSlabName.isEmpty() || facing != state.get(FACING)) {
			return super.onBlockActivated(state, world, pos, player, hand, hit);
		}

		ItemStack held = player.getHeldItem(hand);
		ResourceLocation slabItemName = held.isEmpty() ? null : held.getItem().getRegistryName();

		if (!this.getRegistryName().equals(slabItemName)) {
			return super.onBlockActivated(state, world, pos, player, hand, hit);
		}

		Block doubleSlab = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, this.doubleSlabName));
		if (!(doubleSlab instanceof DoubleSlab)) {
			return super.onBlockActivated(state, world, pos, player, hand, hit);
		}

		world.setBlockState(pos, doubleSlab.getDefaultState());
		if (!player.isCreative()) {
			held.shrink(1);
		}

		return ActionResultType.SUCCESS;
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

	private static boolean canPlaceAt(IBlockReader world, BlockPos pos, Direction facing) {
		BlockPos supportPos = pos.offset(facing.getOpposite());
		BlockState support = world.getBlockState(supportPos);
		return support.isSolidSide(world, supportPos, facing);
	}

	private static boolean canPlaceAtAnyFace(IBlockReader world, BlockPos pos) {
		for (Direction facing : FACING.getAllowedValues()) {
			if (canPlaceAt(world, pos, facing)) {
				return true;
			}
		}

		return false;
	}

	private static Direction getPlacementFacing(net.minecraft.item.BlockItemUseContext context) {
		Direction face = context.getFace();
		Vector3d hitVec = context.getHitVec();
		BlockPos pos = context.getPos();
		float hitX = (float) (hitVec.x - (double) pos.getX());
		float hitY = (float) (hitVec.y - (double) pos.getY());
		float hitZ = (float) (hitVec.z - (double) pos.getZ());
		float up;
		float right;
		Direction.Axis upRotationAxis;
		Direction.Axis rightRotationAxis;

		switch (face) {
			case UP:
				up = hitZ - 0.5F;
				right = hitX - 0.5F;
				upRotationAxis = Direction.Axis.X;
				rightRotationAxis = Direction.Axis.Z;
				break;
			case EAST:
				up = hitY - 0.5F;
				right = hitZ - 0.5F;
				upRotationAxis = Direction.Axis.Z;
				rightRotationAxis = Direction.Axis.Y;
				break;
			case SOUTH:
				up = 0.5F - hitY;
				right = 0.5F - hitX;
				upRotationAxis = Direction.Axis.X;
				rightRotationAxis = Direction.Axis.Y;
				break;
			case DOWN:
				up = 0.5F - hitZ;
				right = 0.5F - hitX;
				upRotationAxis = Direction.Axis.X;
				rightRotationAxis = Direction.Axis.Z;
				break;
			case WEST:
				up = 0.5F - hitY;
				right = 0.5F - hitZ;
				upRotationAxis = Direction.Axis.Z;
				rightRotationAxis = Direction.Axis.Y;
				break;
			case NORTH:
				up = hitY - 0.5F;
				right = hitX - 0.5F;
				upRotationAxis = Direction.Axis.X;
				rightRotationAxis = Direction.Axis.Y;
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
			return upOrLeft ? rotateAround(face, upRotationAxis)
					: rotateAround(face, rightRotationAxis).getOpposite();
		}

		return upOrLeft ? rotateAround(face, rightRotationAxis)
				: rotateAround(face, upRotationAxis).getOpposite();
	}

	private static Direction rotateAround(Direction face, Direction.Axis axis) {
		switch (axis) {
			case X:
				if (face == Direction.WEST || face == Direction.EAST) {
					return face;
				}
				return rotateX(face);
			case Y:
				if (face == Direction.UP || face == Direction.DOWN) {
					return face;
				}
				return face.rotateY();
			case Z:
				if (face == Direction.NORTH || face == Direction.SOUTH) {
					return face;
				}
				return rotateZ(face);
			default:
				return face;
		}
	}

	private static Direction rotateX(Direction face) {
		switch (face) {
			case NORTH:
				return Direction.DOWN;
			case SOUTH:
				return Direction.UP;
			case UP:
				return Direction.NORTH;
			case DOWN:
				return Direction.SOUTH;
			default:
				return face;
		}
	}

	private static Direction rotateZ(Direction face) {
		switch (face) {
			case EAST:
				return Direction.DOWN;
			case WEST:
				return Direction.UP;
			case UP:
				return Direction.EAST;
			case DOWN:
				return Direction.WEST;
			default:
				return face;
		}
	}
}
