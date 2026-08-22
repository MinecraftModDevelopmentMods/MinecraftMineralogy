package zone.moddev.mc.mineralogy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.moddev.mc.mineralogy.fluids.MineralogyFluids;

/** Test-classpath-only real-server acceptance probe. Never packaged in the production jar. */
@Mod(modid = OilRuntimeProbe.MODID, name = "Mineralogy Oil Runtime Probe", version = "1", dependencies = "required-after:mineralogy")
public final class OilRuntimeProbe {
    static final String MODID = "mineralogyoilprobe";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        if (!Boolean.getBoolean("mineralogy.runtimeOilProbe")) {
            return;
        }
        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            WorldServer world = server.getWorld(0);
            EntityPlayer player = FakePlayerFactory.getMinecraft(world);
            BlockPos base = new BlockPos(0, 200, 0);
            world.getChunk(base.getX() >> 4, base.getZ() >> 4);

            require(FluidRegistry.getFluid(MineralogyFluids.FLUID_NAME) == MineralogyFluids.crudeOil,
                    "isolated fluid registry identity");
            boolean powerAdvantageLoaded = Loader.isModLoaded("poweradvantage");
            if (powerAdvantageLoaded) {
                Fluid historicalCrudeOil = FluidRegistry.getFluid("crude_oil");
                require(historicalCrudeOil != null,
                        "Power Advantage historical crude-oil registration");
                require(historicalCrudeOil != MineralogyFluids.crudeOil,
                        "Power Advantage and Mineralogy crude-oil isolation");
                require(!historicalCrudeOil.getName().equals(MineralogyFluids.crudeOil.getName()),
                        "Power Advantage and Mineralogy crude-oil names");
            }
            ItemStack filled = new ItemStack(MineralogyFluids.crudeOilBucket);
            FluidStack contained = FluidUtil.getFluidContained(filled);
            require(contained != null && contained.getFluid() == MineralogyFluids.crudeOil
                            && contained.amount == Fluid.BUCKET_VOLUME,
                    "fluid-capability registration");

            world.setBlockState(base, MineralogyFluids.crudeOilBlock.getDefaultState(), 3);
            FillBucketEvent fill = new FillBucketEvent(player, new ItemStack(Items.BUCKET), world,
                    new RayTraceResult(new Vec3d(base.getX() + 0.5D, base.getY() + 0.5D,
                            base.getZ() + 0.5D), EnumFacing.UP, base));
            MinecraftForge.EVENT_BUS.post(fill);
            require(fill.getFilledBucket() != null
                            && fill.getFilledBucket().getItem() == MineralogyFluids.crudeOilBucket,
                    "source pickup result");
            require(world.isAirBlock(base), "source pickup removal");

            BlockPos placement = base.add(3, 0, 0);
            world.setBlockState(placement.down(), Blocks.STONE.getDefaultState(), 3);
            require(((ItemBucket) MineralogyFluids.crudeOilBucket)
                    .tryPlaceContainedLiquid(player, world, placement), "bucket placement return value");
            require(world.getBlockState(placement).getBlock() == MineralogyFluids.crudeOilBlock,
                    "bucket placement block");

            BlockPos flow = base.add(6, 0, 0);
            world.setBlockState(flow.down(), Blocks.STONE.getDefaultState(), 3);
            for (EnumFacing side : EnumFacing.HORIZONTALS) {
                world.setBlockState(flow.offset(side).down(), Blocks.STONE.getDefaultState(), 3);
                world.setBlockToAir(flow.offset(side));
            }
            world.setBlockState(flow, MineralogyFluids.crudeOilBlock.getDefaultState(), 3);
            MineralogyFluids.crudeOilBlock.updateTick(world, flow,
                    world.getBlockState(flow), world.rand);
            boolean flowed = false;
            for (EnumFacing side : EnumFacing.HORIZONTALS) {
                Block block = world.getBlockState(flow.offset(side)).getBlock();
                flowed |= block == MineralogyFluids.crudeOilBlock;
            }
            require(flowed, "scheduled fluid flow");

            LOGGER.info("MINERALOGY_OIL_RUNTIME_PROBE_PASS pickup=true placement=true flow=true "
                            + "container=true fluid={} powerAdvantageCoexistence={}",
                    MineralogyFluids.FLUID_NAME, powerAdvantageLoaded);
            server.initiateShutdown();
        } catch (Throwable failure) {
            LOGGER.error("MINERALOGY_OIL_RUNTIME_PROBE_FAIL", failure);
            throw failure;
        }
    }

    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new IllegalStateException("Oil runtime check failed: " + check);
        }
    }
}
