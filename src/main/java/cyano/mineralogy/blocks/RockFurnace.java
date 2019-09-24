package cyano.mineralogy.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import cyano.mineralogy.Mineralogy;
import cyano.mineralogy.tileentity.TileEntityRockFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
	public RockFurnace(float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound, Boolean lit) {
		super(lit);
	 	this.setSoundType(sound);
	 	this.blockHardness = hardness;
	 	this.blockResistance = blastResistance;
	 	this.setHarvestLevel("pickaxe", toolHardnessLevel);
	 	
	 	if (!lit)
	 		this.setCreativeTab(Mineralogy.mineralogyTab);
	 }
	
	@Override
	@Nullable
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
		Block drop = state.getBlock();
		ResourceLocation resource = drop.getRegistryName();
		String path = resource.getResourcePath();
		
		if (path.startsWith("lit_"))
			drop = Block.getBlockFromName(resource.getResourceDomain() + ":" + path.substring(4, path.length()));
		
        return Item.getItemFromBlock(drop);
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
        TileEntityRockFurnace tileentity = (TileEntityRockFurnace)worldIn.getTileEntity(pos);
        
        ItemStack input = tileentity.getStackInSlot(0);
        ItemStack fuel = tileentity.getStackInSlot(1);
        ItemStack output = tileentity.getStackInSlot(2);
        
        String name = iblockstate.getBlock().getRegistryName().getResourcePath();
        
        if (active && !name.startsWith("lit_"))
        	name = "lit_" + name;
        
        if (!active && name.startsWith("lit_"))
        	name = name.substring(4, name.length());
        
        RockFurnace thisFurnace = (RockFurnace)Mineralogy.mineralogyBlockRegistry.get(name);
      
        tileentity.clear();
        
        worldIn.setBlockState(pos, thisFurnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
        worldIn.setBlockState(pos, thisFurnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);

        if (tileentity != null)
        {
            tileentity.validate();
            
            tileentity.setInventorySlotContents(0, input);
            tileentity.setInventorySlotContents(1, fuel);
            tileentity.setInventorySlotContents(2, output);
            
            worldIn.setTileEntity(pos, tileentity);
        }
    }
 }
