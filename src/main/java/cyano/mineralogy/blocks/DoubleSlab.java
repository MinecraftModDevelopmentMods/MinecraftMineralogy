package cyano.mineralogy.blocks;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DoubleSlab extends net.minecraft.block.Block {
	private net.minecraft.block.Block _drops;
	
	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, net.minecraft.block.Block drops) {
		super(Material.ROCK);
		this.setHardness(hardness); // dirt is 0.5, grass is 0.6, stone is 1.5,iron ore is 3, obsidian is 50
		this.setResistance(blastResistance); // dirt is 0, iron ore is 5, stone is 10, obsidian is 2000
		this.setSoundType(sound); // sound for stone
		this.setHarvestLevel("pickaxe", toolHardnessLevel);
		_drops = drops;
	}
	
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(_drops);
	}

//	@Override
//	protected ItemStack getSilkTouchDrop(IBlockState state) {
//		return new ItemStack(_drops, 2);
//	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Arrays.asList(new ItemStack(_drops, 2));
	}
}
