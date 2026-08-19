package zone.moddev.mc.mineralogy.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zone.moddev.mc.mineralogy.Mineralogy;

/** Registration and bucket integration for Mineralogy's isolated crude-oil fluid. */
public final class MineralogyFluids {
    public static final String FLUID_NAME = "mineralogy_crude_oil";
    public static final String BLOCK_NAME = "crude_oil";
    public static final String BUCKET_NAME = "crude_oil_bucket";

    public static Fluid crudeOil;
    public static BlockFluidClassic crudeOilBlock;
    public static Item crudeOilBucket;
    private static boolean registered;

    private MineralogyFluids() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        if (FluidRegistry.isFluidRegistered(FLUID_NAME)) {
            throw new IllegalStateException("Fluid name collision: " + FLUID_NAME);
        }

        crudeOil = new Fluid(FLUID_NAME,
                new ResourceLocation(Mineralogy.MODID, "blocks/crude_oil_still"),
                new ResourceLocation(Mineralogy.MODID, "blocks/crude_oil_flow"));
        crudeOil.setDensity(850);
        crudeOil.setViscosity(6000);
        crudeOil.setTemperature(300);
        crudeOil.setLuminosity(0);
        crudeOil.setUnlocalizedName(Mineralogy.MODID + ".crude_oil");
        if (!FluidRegistry.registerFluid(crudeOil)) {
            throw new IllegalStateException("Unable to register fluid " + FLUID_NAME);
        }

        crudeOilBlock = (BlockFluidClassic) new BlockFluidClassic(crudeOil, Material.WATER)
                .setRegistryName(Mineralogy.MODID, BLOCK_NAME)
                .setUnlocalizedName(Mineralogy.MODID + "." + BLOCK_NAME);
        GameRegistry.register(crudeOilBlock);
        Mineralogy.mineralogyBlockRegistry.put(BLOCK_NAME, crudeOilBlock);
        crudeOil.setBlock(crudeOilBlock);

        crudeOilBucket = new ItemBucket(crudeOilBlock)
                .setRegistryName(Mineralogy.MODID, BUCKET_NAME)
                .setUnlocalizedName(Mineralogy.MODID + "." + BUCKET_NAME)
                .setCreativeTab(Mineralogy.mineralogyTab)
                .setMaxStackSize(1);
        GameRegistry.register(crudeOilBucket);
        Mineralogy.mineralogyItemRegistry.put(BUCKET_NAME, crudeOilBucket);

        if (!FluidContainerRegistry.registerFluidContainer(
                crudeOil, new ItemStack(crudeOilBucket), new ItemStack(Items.BUCKET))) {
            throw new IllegalStateException("Unable to register the Mineralogy crude-oil bucket container");
        }
        MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);
        registered = true;
    }

    @SideOnly(Side.CLIENT)
    public static void registerClientModels() {
        final ModelResourceLocation fluidModel = new ModelResourceLocation(Mineralogy.MODID + ":" + BLOCK_NAME, "fluid");
        ModelLoader.setCustomStateMapper(crudeOilBlock, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return fluidModel;
            }
        });
        ModelLoader.setCustomModelResourceLocation(crudeOilBucket, 0,
                new ModelResourceLocation(Mineralogy.MODID + ":" + BUCKET_NAME, "inventory"));
    }

    private enum BucketHandler {
        INSTANCE;

        @SubscribeEvent
        public void onFillBucket(FillBucketEvent event) {
            ItemStack empty = event.getEmptyBucket();
            RayTraceResult target = event.getTarget();
            if (empty == null || empty.getItem() != Items.BUCKET || target == null
                    || target.typeOfHit != RayTraceResult.Type.BLOCK) {
                return;
            }
            BlockPos pos = target.getBlockPos();
            Block block = event.getWorld().getBlockState(pos).getBlock();
            if (block != crudeOilBlock) {
                return;
            }
            if (!crudeOilBlock.isSourceBlock(event.getWorld(), pos)) {
                // Material.WATER would otherwise let vanilla turn a flowing oil block into a water bucket.
                event.setCanceled(true);
                return;
            }
            if (!event.getWorld().isRemote) {
                event.getWorld().setBlockToAir(pos);
            }
            event.setFilledBucket(new ItemStack(crudeOilBucket));
            event.setResult(Event.Result.ALLOW);
        }
    }

}
