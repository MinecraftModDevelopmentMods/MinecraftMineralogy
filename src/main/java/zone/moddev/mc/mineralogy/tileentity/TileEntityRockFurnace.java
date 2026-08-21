package zone.moddev.mc.mineralogy.tileentity;

import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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
    private static final double DEFAULT_BURN_MODIFIER = 1.0D;

    double _burnModifier;
    private boolean recoverBurnModifierFromBlock;

    /**
     * Required by Minecraft's reflective tile-entity loader.
     *
     * <p>The owning block is not available until after NBT has been read, so
     * the material-specific burn modifier is recovered lazily on the first
     * fuel calculation.</p>
     */
    public TileEntityRockFurnace()
    {
        this(DEFAULT_BURN_MODIFIER, true);
    }

    public TileEntityRockFurnace(double burnModifier)
    {
        this(burnModifier, false);
    }

    private TileEntityRockFurnace(double burnModifier, boolean recoverBurnModifierFromBlock)
    {
        super();

        _burnModifier = burnModifier;
        this.recoverBurnModifierFromBlock = recoverBurnModifierFromBlock;
    }

    private double getEffectiveBurnModifier()
    {
        if (recoverBurnModifierFromBlock && this.getWorld() != null)
        {
            _burnModifier = burnModifierFor(
                    this.getWorld().getBlockState(this.pos).getBlock(),
                    DEFAULT_BURN_MODIFIER);
            recoverBurnModifierFromBlock = false;
        }

        return _burnModifier;
    }

    static double burnModifierFor(Block block, double fallback)
    {
        if (block instanceof RockFurnace)
        {
            return ((RockFurnace) block).getBurnModifier();
        }

        return fallback;
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
                if (!this.getWorld().isRemote)
                {
                    if (this.isBurning() || super.getStackInSlot(1) != null && super.getStackInSlot(0) != null)
                    {
                        try {
                            if (!this.isBurning() && this.canSmelt())
                            {
                                int burnTime = (int) (getItemBurnTime(super.getStackInSlot(1)) * getEffectiveBurnModifier());

                                super.setField(0, burnTime);
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
                        super.setField(2, MathHelper.clamp_int(super.getField(2) - 2, 0, super.getField(3)));
                    }

                    if (flag != this.isBurning())
                    {
                        flag1 = true;
                        RockFurnace.setState(this.isBurning(), this.getWorld(), this.pos);
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
