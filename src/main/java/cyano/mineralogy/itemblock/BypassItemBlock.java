package cyano.mineralogy.itemblock;

import cyano.mineralogy.blocks.RockSlab;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BypassItemBlock extends ItemBlock {

	public BypassItemBlock(Block block) {
		super(block);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		// TODO Auto-generated method stub
		//return super.doesSneakBypassUse(stack, world, pos, player);
		Block block = world.getBlockState(pos).getBlock();
		
		if (block instanceof RockSlab)
			return true;
		
		return false;
	}
}
