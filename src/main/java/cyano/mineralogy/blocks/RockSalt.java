package cyano.mineralogy.blocks;

import cyano.mineralogy.Mineralogy;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RockSalt extends Rock {
	private final String itemName = "rock_salt";
	public RockSalt() {
		super(false,(float)0.75, (float)1, 0, SoundType.STONE);
		this.setUnlocalizedName(Mineralogy.MODID + "_" + itemName);
		this.setCreativeTab(Mineralogy.mineralogyTab);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Arrays.asList(new ItemStack(Mineralogy.rockSaltPowder, 4));
	}
}
