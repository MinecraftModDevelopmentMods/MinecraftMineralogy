package zone.moddev.mc.mineralogy.blocks;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Chert extends Rock {
	private final Random prng = new Random();

	public Chert() {
		super(false, 1.5F, 10.0F, 1, SoundType.STONE, "chert");
	}

	@Override
	public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
		if (prng.nextInt(10) == 0) {
			drops.add(new ItemStack(Items.FLINT, 1 + Math.max(0, fortune)));
		} else {
			super.getDrops(state, drops, world, pos, fortune);
		}
	}
}
