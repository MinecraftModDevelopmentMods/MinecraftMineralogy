package cyano.mineralogy.tileentity;

import cyano.mineralogy.blocks.RockFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import scala.actors.Debug;

/**
*
* @author SkyBlade1978
*/
public class TileEntityRockFurnace extends TileEntityFurnace {
	public TileEntityRockFurnace()
	{
		super();
	}
	
// set / get field keys 
//0: furnaceBurnTime;
//1: currentItemBurnTime;
//2: cookTime;
//3: totalCookTime;
	
	public boolean canSmelt()
    {
        if (super.getStackInSlot(0) == null)
        {
            return false;
        }
        else
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(super.getStackInSlot(0));
            if (itemstack == null) return false;
            if (super.getStackInSlot(2) == null) return true;
            if (!super.getStackInSlot(2).isItemEqual(itemstack)) return false;
            int result = super.getStackInSlot(2).stackSize + itemstack.stackSize;
            return result <= getInventoryStackLimit() && result <= super.getStackInSlot(2).getMaxStackSize();
        }
    }

	@Override
    public void update()
    {
		try {
			boolean flag = this.isBurning();
	        boolean flag1 = false;
	       
	        try {
	        	if (this.isBurning())
		        {
		        	super.setField(0, super.getField(0) -1);
		        }	
			} catch (Exception e) {
				Debug.error(e.toString());
			}
	        

	        try {
	        	if (!this.world.isRemote)
		        {
		            if (this.isBurning() || super.getStackInSlot(1) != null && super.getStackInSlot(0) != null)
		            {
		            	try {
		            		if (!this.isBurning() && this.canSmelt())
			                {
			                	super.setField(0, getItemBurnTime(super.getStackInSlot(1)));
			                    super.setField(1, super.getField(0));

			                    if (this.isBurning())
			                    {
			                        flag1 = true;

			                        if (super.getStackInSlot(1) != null)
			                        {
			                        	super.decrStackSize(1, 1);
			                        	
			                        	ItemStack items = super.getStackInSlot(1);

			                            if (items == null || items.stackSize == 0)
			                            {
			                            	
			                            	items = new ItemStack(Blocks.COAL_ORE);
			                            }
			                        }
			                    }
			                }
						} catch (Exception e) {
							Debug.error(e.toString());
						}
		                

		            	try {
		            		if (this.isBurning() && this.canSmelt())
			                {  	
			                	super.setField(2, super.getField(2) + 1);
			                    
			                    if (super.getField(2)  == super.getField(3))
			                    {
			                        super.setField(2, 0);
			                        
			                        super.setField(3, this.getCookTime(super.getStackInSlot(0)));
			                        this.smeltItem();
			                        flag1 = true;
			                    }
			                }
			                else
			                {
			                	super.setField(2, 0);
			                }
						} catch (Exception e) {
							Debug.error(e.toString());
						}
		                
		            }
		            else if (!this.isBurning() && super.getField(2) > 0)
		            {
		            	super.setField(2, MathHelper.clamp(super.getField(2) - 2, 0, super.getField(3)));
		            }

		            if (flag != this.isBurning())
		            {
		                flag1 = true;
		                RockFurnace.setState(this.isBurning(), this.world, this.pos);
		            }
		        }

		        if (flag1)
		        {
		            this.markDirty();
		        }
			} catch (Exception e) {
				Debug.error(e.toString());
			}
	        

		} catch (Exception e) {
			Debug.error(e.toString());
		}
	}
}
