package cyano.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

import cyano.mineralogy.Mineralogy;

public class RockSlab extends net.minecraft.block.Block {

	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	private static float thickness = 0.5f;
	private String _doubleSlab = "";
	
	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[EnumFacing.values().length];
	static {
		for(int i = 0; i < EnumFacing.values().length; i++) {
			EnumFacing orientation = EnumFacing.values()[i];
			float x1 = 0, x2 = 1, y1 = 0,y2 = 1, z1 = 0, z2 = 1;
			switch(orientation) {
				case DOWN:
					y1 = 1f - thickness;
					break;
				case SOUTH:
					z2 = thickness;
					break;
				case NORTH:
					z1 = 1f - thickness;
					break;
				case EAST:
					x2 = thickness;
					break;
				case WEST:
					x1 = 1f - thickness;
					break;
				case UP:
				default:
					y2 = thickness;
					break;
			}
			BOXES[orientation.ordinal()] = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
		}
	}
	
	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound) {
		super(Material.ROCK);
		this.setHardness((float)hardness); // dirt is 0.5, grass is 0.6, stone is 1.5,iron ore is 3, obsidian is 50
		this.setResistance((float)blastResistance); // dirt is 0, iron ore is 5, stone is 10, obsidian is 2000
		this.setSoundType(sound); // sound for stone
		this.setHarvestLevel("pickaxe", toolHardnessLevel);
		this.setCreativeTab(Mineralogy.mineralogyTab);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(FACING,EnumFacing.UP));
		this.useNeighborBrightness = true;
	}
	
	public RockSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, String doubleSlab) {
		super(Material.ROCK);
		this.setHardness((float)hardness); // dirt is 0.5, grass is 0.6, stone is 1.5,iron ore is 3, obsidian is 50
		this.setResistance((float)blastResistance); // dirt is 0, iron ore is 5, stone is 10, obsidian is 2000
		this.setSoundType(sound); // sound for stone
		this.setHarvestLevel("pickaxe", toolHardnessLevel);
		this.setCreativeTab(Mineralogy.mineralogyTab);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(FACING,EnumFacing.UP));
		this.useNeighborBrightness = true;
		_doubleSlab = doubleSlab;
	}

	@Override
	public boolean isOpaqueCube(IBlockState bs) {
		return false;
	}


	@Override
	public boolean isFullCube(IBlockState bs) {
		return false;
	}

	@Override
	public IBlockState getStateForPlacement(final World w, final BlockPos coord, final EnumFacing face,
									 final float partialX, final float partialY, final float partialZ,
									 final int i, final EntityLivingBase placer) {
		IBlockState defaultState = this.getDefaultState().withProperty(FACING, face);
		// redimension to face-local up and right dimensions
		float up, right;
		EnumFacing.Axis upRotationAxis, rightRotationAxis;
		switch(face){
			case UP: // works
				up = partialZ - 0.5F;
				right = partialX - 0.5F;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Z;
				break;
			case EAST: // works
				up = partialY - 0.5F;
				right = partialZ - 0.5F;
				upRotationAxis = EnumFacing.Axis.Z;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			case SOUTH:
				up = 0.5F - partialY;
				right = 0.5F - partialX;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			case DOWN:
				up = 0.5F - partialZ;
				right = 0.5F - partialX;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Z;
				break;
			case WEST:
				up = 0.5F - partialY;
				right = 0.5F - partialZ;
				upRotationAxis = EnumFacing.Axis.Z;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			case NORTH: // works
				up = partialY - 0.5F;
				right = partialX - 0.5F;
				upRotationAxis = EnumFacing.Axis.X;
				rightRotationAxis = EnumFacing.Axis.Y;
				break;
			default: return defaultState;
		}
		if(Math.abs(up) < 0.25F && Math.abs(right) < 0.25F) {
			// no rotation
			return defaultState;
		}
		boolean upOrRight = up + right > 0;
		boolean upOrLeft = up - right > 0;
		if(upOrRight) {
			// up or right
			if(upOrLeft) {
				// up
				return defaultState.withProperty(FACING, face.rotateAround(upRotationAxis));
			} else {
				// right
				return defaultState.withProperty(FACING, face.rotateAround(rightRotationAxis).getOpposite());
			}
		} else {
			// down or left
			if(upOrLeft) {
				// left
				return defaultState.withProperty(FACING, face.rotateAround(rightRotationAxis));
			} else {
				// down
				return defaultState.withProperty(FACING, face.rotateAround(upRotationAxis).getOpposite());
			}
		}
	}

	@Override
	public IBlockState getStateFromMeta(final int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(final IBlockState bs) {
		int i = ((EnumFacing)bs.getValue(FACING)).getIndex();
		return i;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public AxisAlignedBB getBoundingBox(final IBlockState bs, final IBlockAccess world, final BlockPos coord) {
		final EnumFacing orientation = (EnumFacing) bs.getValue(FACING);
		return BOXES[orientation.ordinal()];
	}

	@Override
	public void addCollisionBoxToList(final IBlockState bs, final World world, final BlockPos coord,
									  final AxisAlignedBB box, final List<AxisAlignedBB> collisionBoxList,
									  final Entity entity) {

		final EnumFacing orientation = (EnumFacing) world.getBlockState(coord).getValue(FACING);
		super.addCollisionBoxToList(coord, box, collisionBoxList, BOXES[orientation.ordinal()]);
	}
	

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (_doubleSlab != "") {
			IBlockState iblockstate = worldIn.getBlockState(pos);
			
			Block block = iblockstate.getBlock();
			EnumFacing targetBlockOrientation = iblockstate.getValue(FACING);
			
			boolean doubleIt = false;
			        
			DoubleSlab doubleBlock =  (DoubleSlab)Mineralogy.mineralogyBlockRegistry.get(_doubleSlab);
			
	        IBlockState doubleSlabBlockstate = doubleBlock.getDefaultState();
	        
			ItemStack itemStack;
			
			if (hand == EnumHand.MAIN_HAND)
				itemStack = playerIn.getHeldItemMainhand();
			else 
				itemStack = playerIn.getHeldItemOffhand();
			
			if (itemStack == null)
				return false;
			
			if (side ==  targetBlockOrientation)
				doubleIt = true;
	
			if (doubleIt && (block.getRegistryName().getResourcePath().equals(itemStack.getItem().getRegistryName().getResourcePath()))) {
				worldIn.setBlockState(pos, doubleSlabBlockstate);
				
				if (!playerIn.isCreative()) {
					if (hand == EnumHand.MAIN_HAND) {
						int newCount = playerIn.getHeldItemMainhand().stackSize -1;
						
						playerIn.getHeldItemMainhand().stackSize = newCount;
					} else  {
						int newCount = playerIn.getHeldItemOffhand().stackSize -1;
						
						playerIn.getHeldItemOffhand().stackSize = newCount;
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		IBlockState iblockstate = worldIn.getBlockState(pos.offset(side.getOpposite()));
		
		Block block = iblockstate.getBlock();
		
		if (block.getRegistryName().getResourcePath().equals(this.getRegistryName().getResourcePath())) {
			EnumFacing targetBlockOrientation = iblockstate.getValue(FACING);
			
			if (side == targetBlockOrientation) {
				return false;
			}
			
			return true;
		}
		
		return super.canPlaceBlockOnSide(worldIn, pos, side);
	}
}
