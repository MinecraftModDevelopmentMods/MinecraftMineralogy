package zone.moddev.mc.mineralogy.blocks;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class RockSlab extends Block implements NamedMineralogyBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	private static final double THICKNESS = 8.0D;
	private static final VoxelShape[] SHAPES = new VoxelShape[Direction.values().length];

	static {
		SHAPES[Direction.DOWN.ordinal()] = Block.box(0.0D, THICKNESS, 0.0D, 16.0D, 16.0D, 16.0D);
		SHAPES[Direction.UP.ordinal()] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, THICKNESS, 16.0D);
		SHAPES[Direction.NORTH.ordinal()] = Block.box(0.0D, 0.0D, THICKNESS, 16.0D, 16.0D, 16.0D);
		SHAPES[Direction.SOUTH.ordinal()] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, THICKNESS);
		SHAPES[Direction.WEST.ordinal()] = Block.box(THICKNESS, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
		SHAPES[Direction.EAST.ordinal()] = Block.box(0.0D, 0.0D, 0.0D, THICKNESS, 16.0D, 16.0D);
	}

	private final String doubleSlabName;
	private final int toolHardnessLevel;
	private final String registryPath;

	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound) {
		this(hardness, blastResistance, toolHardnessLevel, sound, null, "");
	}

	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name) {
		this(hardness, blastResistance, toolHardnessLevel, sound, name, "");
	}

	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name,
			String doubleSlabName) {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(hardness, blastResistance).sound(sound)
				.requiresCorrectToolForDrops());
		this.toolHardnessLevel = toolHardnessLevel;
		this.doubleSlabName = doubleSlabName;
		this.registryPath = name;
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.UP));
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(FACING).ordinal()];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return getShape(state, world, pos, context);
	}

	public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
		BlockState state = this.defaultBlockState().setValue(FACING, getPlacementFacing(context));
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return canPlaceAtAnyFace(world, pos);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		Direction facing = hit.getDirection();
		if (this.doubleSlabName == null || this.doubleSlabName.isEmpty() || facing != state.getValue(FACING)) {
			return super.use(state, world, pos, player, hand, hit);
		}

		ItemStack held = player.getItemInHand(hand);
		ResourceLocation slabItemName = held.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(held.getItem());

		if (!ForgeRegistries.BLOCKS.getKey(this).equals(slabItemName)) {
			return super.use(state, world, pos, player, hand, hit);
		}

		Block doubleSlab = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, this.doubleSlabName));
		if (!(doubleSlab instanceof DoubleSlab)) {
			return super.use(state, world, pos, player, hand, hit);
		}

		world.setBlock(pos, doubleSlab.defaultBlockState(), 3);
		if (!player.isCreative()) {
			held.shrink(1);
		}

		return InteractionResult.SUCCESS;
	}
@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	private static boolean canPlaceAt(BlockGetter world, BlockPos pos, Direction facing) {
		BlockPos supportPos = pos.relative(facing.getOpposite());
		BlockState support = world.getBlockState(supportPos);
		return support.isFaceSturdy(world, supportPos, facing);
	}

	private static boolean canPlaceAtAnyFace(BlockGetter world, BlockPos pos) {
		for (Direction facing : FACING.getPossibleValues()) {
			if (canPlaceAt(world, pos, facing)) {
				return true;
			}
		}

		return false;
	}

	private static Direction getPlacementFacing(net.minecraft.world.item.context.BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		Vec3 hitVec = context.getClickLocation();
		BlockPos pos = context.getClickedPos();
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
				return face.getClockWise();
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
