package cyano.mineralogy.blocks;

import java.util.List;

import cyano.mineralogy.Mineralogy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
 
/**
*
* @author Jasmine Iwanek
*
*/
public class RockWall extends BlockWall {
	public RockWall(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound) {
		super(materialBlock);
	 	this.setSoundType(sound);
	 	this.blockHardness = hardness;
	 	this.blockResistance = blastResistance;
	 	this.setHarvestLevel("pickaxe", toolHardnessLevel);
	 	this.setCreativeTab(Mineralogy.mineralogyTab);
	 }
	 
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(final Item itemIn, final CreativeTabs tab, final List<ItemStack> list) {
		list.add(new ItemStack(itemIn));
	}

	 
	 @Override
	 public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
	         return true;
	 }
 }
