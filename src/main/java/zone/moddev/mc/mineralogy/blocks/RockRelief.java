package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class RockRelief extends RockSlab {
	private static final double THICKNESS = 1.12D;
	private static final VoxelShape[] SHAPES = new VoxelShape[Direction.values().length];

	static {
		SHAPES[Direction.DOWN.ordinal()] = Block.makeCuboidShape(0.0D, 16.0D - THICKNESS, 0.0D,
				16.0D, 16.0D, 16.0D);
		SHAPES[Direction.UP.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D,
				16.0D, THICKNESS, 16.0D);
		SHAPES[Direction.NORTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 16.0D - THICKNESS,
				16.0D, 16.0D, 16.0D);
		SHAPES[Direction.SOUTH.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D,
				16.0D, 16.0D, THICKNESS);
		SHAPES[Direction.WEST.ordinal()] = Block.makeCuboidShape(16.0D - THICKNESS, 0.0D, 0.0D,
				16.0D, 16.0D, 16.0D);
		SHAPES[Direction.EAST.ordinal()] = Block.makeCuboidShape(0.0D, 0.0D, 0.0D,
				THICKNESS, 16.0D, 16.0D);
	}

	public RockRelief(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String name) {
		super(hardness, blastResistance, toolHardnessLevel, sound, name);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return SHAPES[state.get(FACING).ordinal()];
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
		return VoxelShapes.empty();
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
		return false;
	}
}
