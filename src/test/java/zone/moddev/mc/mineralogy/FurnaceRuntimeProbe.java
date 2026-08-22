package zone.moddev.mc.mineralogy;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

/** Test-classpath-only real-save furnace acceptance probe. Never packaged in production. */
@Mod(modid = FurnaceRuntimeProbe.MODID, name = "Mineralogy Furnace Runtime Probe",
        version = "1", dependencies = "required-after:mineralogy")
public final class FurnaceRuntimeProbe {
    static final String MODID = "mineralogyfurnaceprobe";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final BlockPos FURNACE_POS = new BlockPos(8, 200, 8);

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        String phase = System.getProperty("mineralogy.runtimeFurnaceProbePhase", "");
        if (phase.isEmpty()) {
            return;
        }

        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            WorldServer world = server.getWorld(0);
            world.getChunk(FURNACE_POS.getX() >> 4, FURNACE_POS.getZ() >> 4);

            if ("fresh".equals(phase)) {
                createOccupiedFurnace(world);
                server.saveAllWorlds(false);
                LOGGER.info("MINERALOGY_FURNACE_RUNTIME_PROBE_PASS phase=fresh "
                        + "block=mineralogy:basalt_furnace input=7 fuel=3 output=2 "
                        + "burn=1200 cook=87 total=200");
            } else if ("reload".equals(phase)) {
                verifyReloadedFurnace(world);
                LOGGER.info("MINERALOGY_FURNACE_RUNTIME_PROBE_PASS phase=reload "
                        + "block=mineralogy:basalt_furnace input=7 fuel=3 output=2 "
                        + "burn=1200 cook=87 total=200 recoveredBurn=1920");
            } else {
                throw new IllegalArgumentException("Unknown furnace probe phase: " + phase);
            }
            server.initiateShutdown();
        } catch (Throwable failure) {
            LOGGER.error("MINERALOGY_FURNACE_RUNTIME_PROBE_FAIL phase={}", phase, failure);
            throw failure;
        }
    }

    private static void createOccupiedFurnace(WorldServer world) {
        Block furnace = Block.REGISTRY.getObject(new ResourceLocation("mineralogy", "basalt_furnace"));
        require(furnace != null && furnace != Blocks.AIR, "basalt furnace registry identity");
        require(world.setBlockState(FURNACE_POS, furnace.getDefaultState(), 3), "furnace placement");

        TileEntityRockFurnace tile = furnace(world);
        IInventory inventory = tile;
        inventory.setInventorySlotContents(0, new ItemStack(Blocks.IRON_ORE, 7));
        inventory.setInventorySlotContents(1, new ItemStack(Items.COAL, 3));
        inventory.setInventorySlotContents(2, new ItemStack(Items.IRON_INGOT, 2));
        inventory.setField(0, 1200);
        inventory.setField(1, 1600);
        inventory.setField(2, 87);
        inventory.setField(3, 200);
        world.getChunk(FURNACE_POS).markDirty();
    }

    private static void verifyReloadedFurnace(WorldServer world) {
        require("mineralogy:basalt_furnace".equals(
                String.valueOf(world.getBlockState(FURNACE_POS).getBlock().getRegistryName())),
                "saved furnace block identity");
        TileEntityRockFurnace tile = furnace(world);
        IInventory inventory = tile;
        requireStack(inventory.getStackInSlot(0), Item.getItemFromBlock(Blocks.IRON_ORE), 7, "input");
        requireStack(inventory.getStackInSlot(1), Items.COAL, 3, "fuel");
        requireStack(inventory.getStackInSlot(2), Items.IRON_INGOT, 2, "output");
        require(inventory.getField(0) == 1200, "burn progress");
        require(inventory.getField(1) == 1600, "current fuel time");
        require(inventory.getField(2) == 87, "cook progress");
        require(inventory.getField(3) == 200, "total cook time");

        inventory.setField(0, 0);
        inventory.setField(1, 0);
        inventory.setField(2, 0);
        ((ITickable) tile).update();
        require(inventory.getField(0) == 1920 && inventory.getField(1) == 1920,
                "lazy basalt burn-modifier recovery");
    }

    private static TileEntityRockFurnace furnace(WorldServer world) {
        TileEntity tile = world.getTileEntity(FURNACE_POS);
        require(tile instanceof TileEntityRockFurnace, "rock furnace tile reconstruction");
        return (TileEntityRockFurnace) tile;
    }

    private static void requireStack(ItemStack stack, Item item, int count, String check) {
        require(stack.getItem() == item && stack.getCount() == count, check);
    }

    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new IllegalStateException("Furnace runtime check failed: " + check);
        }
    }
}
