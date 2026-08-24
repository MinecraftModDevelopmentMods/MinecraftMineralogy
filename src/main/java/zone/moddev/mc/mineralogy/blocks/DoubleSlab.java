package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.Random;

public class DoubleSlab extends Block {
	private final Block drops;
	private final Block fullBlock;
	private final int toolHardnessLevel;

	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, Block drops,
			String name) {
		this(hardness, blastResistance, toolHardnessLevel, sound, drops, drops, name);
	}

	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, Block drops,
			Block fullBlock, String name) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hardness, blastResistance).sound(sound));
		this.drops = drops;
		this.fullBlock = fullBlock;
		this.toolHardnessLevel = toolHardnessLevel;
		this.setRegistryName(name);
	}

	@Override
	public ItemStack getItem(IBlockReader world, BlockPos pos, IBlockState state) {
		return new ItemStack(drops);
	}

	@Override
	public IItemProvider getItemDropped(IBlockState state, World world, BlockPos pos, int fortune) {
		return drops;
	}

	@Override
	public int quantityDropped(IBlockState state, Random random) {
		return 2;
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(fullBlock);
	}

	@Override
	public ToolType getHarvestTool(IBlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(IBlockState state) {
		return toolHardnessLevel;
	}
}
