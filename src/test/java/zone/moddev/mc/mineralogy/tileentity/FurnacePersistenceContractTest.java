package zone.moddev.mc.mineralogy.tileentity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;
import zone.moddev.mc.mineralogy.MinecraftTestBootstrap;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;

public class FurnacePersistenceContractTest {
    @BeforeClass
    public static void registerVanilla() {
        MinecraftTestBootstrap.registerVanilla();
        GameRegistry.registerTileEntity(TileEntityRockFurnace.class,
                "rock_salt_smooth_brick_furnace");
    }

    @Test
    public void reflectiveLoaderCanConstructAndRestoreFurnaceContents() throws Exception {
        TileEntityRockFurnace original = new TileEntityRockFurnace(1.2D);
        original.setInventorySlotContents(0, new ItemStack(Blocks.IRON_ORE, 7));
        original.setInventorySlotContents(1, new ItemStack(Items.COAL, 3));
        original.setInventorySlotContents(2, new ItemStack(Items.IRON_INGOT, 2));
        original.setField(0, 1200);
        original.setField(1, 1600);
        original.setField(2, 87);
        original.setField(3, 200);

        NBTTagCompound saved = original.writeToNBT(new NBTTagCompound());
        TileEntityRockFurnace loaded = TileEntityRockFurnace.class.newInstance();
        loaded.readFromNBT(saved);

        assertStack(loaded.getStackInSlot(0), Item.getItemFromBlock(Blocks.IRON_ORE), 7);
        assertStack(loaded.getStackInSlot(1), Items.COAL, 3);
        assertStack(loaded.getStackInSlot(2), Items.IRON_INGOT, 2);
        assertEquals(1200, loaded.getField(0));
        assertEquals(1600, loaded.getField(1));
        assertEquals(87, loaded.getField(2));
        assertEquals(200, loaded.getField(3));
        assertFalse(saved.hasKey("BurnModifier"));
    }

    @Test
    public void reflectiveLoadPathRecoversTheOwningRocksBurnModifier() {
        RockFurnace furnace = new RockFurnace(5.0F, 100.0F, 2,
                SoundType.STONE, false, 1.2D);

        assertEquals(1.2D,
                TileEntityRockFurnace.burnModifierFor(furnace, 1.0D), 0.0D);
        assertEquals(1.0D,
                TileEntityRockFurnace.burnModifierFor(Blocks.FURNACE, 1.0D), 0.0D);
    }

    private static void assertStack(ItemStack stack, Item item, int count) {
        assertSame(item, stack.getItem());
        assertEquals(count, stack.stackSize);
    }
}
