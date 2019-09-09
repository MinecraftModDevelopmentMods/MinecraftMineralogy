package cyano.mineralogy.blocks;

import java.util.List;

import cyano.mineralogy.Mineralogy;
import cyano.mineralogy.tileentity.TileEntityRockFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
 
/**
*
* @author SkyBlade1978
*/
public class RockFurnace extends BlockFurnace {	
	public RockFurnace(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound) {
		super(false);
	 	this.setSoundType(sound);
	 	this.blockHardness = hardness;
	 	this.blockResistance = blastResistance;
	 	this.setHarvestLevel("pickaxe", toolHardnessLevel);
	 	this.setCreativeTab(Mineralogy.mineralogyTab);
	 }
	
	private static boolean keepInventory;
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!keepInventory)
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityFurnace)
            {
                InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityFurnace)tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }
        }

        super.breakBlock(worldIn, pos, state);
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
	 
	 @Override
		public boolean hasTileEntity(IBlockState state) {
			return true;
		}
	 
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityRockFurnace();
	}
	
	public static void setState(boolean active, World worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        
        String name = iblockstate.getBlock().getRegistryName().getResourcePath();
        
        keepInventory = true;

        if (active)
        {
        	RockFurnace thisFurnace = (RockFurnace)Mineralogy.mineralogyBlockRegistry.get(name); // TODO make lit object

            worldIn.setBlockState(pos, thisFurnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
            worldIn.setBlockState(pos, thisFurnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
        }
        else
        {
        	RockFurnace thisFurnace = (RockFurnace)Mineralogy.mineralogyBlockRegistry.get(name);

            worldIn.setBlockState(pos, thisFurnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
            worldIn.setBlockState(pos, thisFurnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
        }

        keepInventory = false;

        if (tileentity != null)
        {
            tileentity.validate();
            worldIn.setTileEntity(pos, tileentity);
        }
    }
 }
