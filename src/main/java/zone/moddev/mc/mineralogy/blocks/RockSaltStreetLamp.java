package zone.moddev.mc.mineralogy.blocks;

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

import net.minecraft.util.RandomSource;

public class RockSaltStreetLamp extends Block implements NamedMineralogyBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	private static final VoxelShape STANDING_SHAPE = Block.box(6.4D, 0.0D, 6.4D, 9.6D, 28.8D, 9.6D);

	public RockSaltStreetLamp() {
		super(BlockBehaviour.Properties.of(Material.METAL).strength(1.0F)
				.lightLevel(state -> 15).sound(SoundType.METAL));
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.UP));
	}

	@Override
	public String mineralogyRegistryPath() {
		return "rocksaltstreetlamp";
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return STANDING_SHAPE;
	}

	public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = this.defaultBlockState().setValue(FACING, Direction.UP);
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
			LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		return !state.canSurvive(world, currentPos) ? Blocks.AIR.defaultBlockState() : state;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos supportPos = pos.below();
		return world.getBlockState(pos.above()).isAir()
				&& world.getBlockState(supportPos).isFaceSturdy(world, supportPos, Direction.UP);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		world.addParticle(ParticleTypes.SMOKE,
				(double) pos.getX() + 0.5D,
				(double) pos.getY() + 1.92D,
				(double) pos.getZ() + 0.5D,
				0.0D, 0.0D, 0.0D);
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
