package zone.moddev.mc.mineralogy.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowingFluid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.init.MineralogyItemGroups;

/** Target-native source, flowing block and bucket for isolated Mineralogy crude oil. */
@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MineralogyFluids {
    public static CrudeOilFluid.Source CRUDE_OIL;
    public static CrudeOilFluid.Flowing FLOWING_CRUDE_OIL;
    public static BlockFlowingFluid CRUDE_OIL_BLOCK;
    public static Item CRUDE_OIL_BUCKET;

    private MineralogyFluids() {
    }

    /**
     * Forge 25 still uses Minecraft's vanilla fluid registry. Register the two
     * fluid states during mod construction, before Forge fires block and item
     * registry events.
     */
    public static void registerFluids() {
        if (CRUDE_OIL != null) {
            return;
        }
        FLOWING_CRUDE_OIL = new CrudeOilFluid.Flowing();
        CRUDE_OIL = new CrudeOilFluid.Source();
        IRegistry.field_212619_h.put(new ResourceLocation(Mineralogy.MODID, "flowing_crude_oil"), FLOWING_CRUDE_OIL);
        IRegistry.field_212619_h.put(new ResourceLocation(Mineralogy.MODID, "crude_oil"), CRUDE_OIL);
        for (Fluid fluid : new Fluid[] { FLOWING_CRUDE_OIL, CRUDE_OIL }) {
            for (IFluidState state : fluid.getStateContainer().getValidStates()) {
                Fluid.STATE_REGISTRY.add(state);
            }
        }
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        CRUDE_OIL_BLOCK = (BlockFlowingFluid) new CrudeOilBlock(CRUDE_OIL,
                Block.Properties.create(Material.WATER).doesNotBlockMovement()
                        .hardnessAndResistance(100.0F).variableOpacity())
                .setRegistryName(Mineralogy.MODID, "crude_oil");
        event.getRegistry().register(CRUDE_OIL_BLOCK);
    }

    private static final class CrudeOilBlock extends BlockFlowingFluid {
        private CrudeOilBlock(FlowingFluid fluid, Block.Properties properties) {
            super(fluid, properties);
        }
    }

    @SubscribeEvent
    public static void registerBucket(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().maxStackSize(1);
        if (MineralogyConfig.isCreativeVisible("crude_oil_bucket")) {
            properties.group(MineralogyItemGroups.forItem());
        }
        CRUDE_OIL_BUCKET = new ItemBucket(CRUDE_OIL, properties)
                .setRegistryName(Mineralogy.MODID, "crude_oil_bucket");
        event.getRegistry().register(CRUDE_OIL_BUCKET);
    }

    public abstract static class CrudeOilFluid extends FlowingFluid {
        @Override public Fluid getFlowingFluid() { return FLOWING_CRUDE_OIL; }
        @Override public Fluid getStillFluid() { return CRUDE_OIL; }
        @Override public Item getFilledBucket() { return CRUDE_OIL_BUCKET; }

        @Override
        @OnlyIn(Dist.CLIENT)
        public BlockRenderLayer getRenderLayer() {
            return BlockRenderLayer.TRANSLUCENT;
        }

        @Override protected boolean canSourcesMultiply() { return false; }
        @Override protected void beforeReplacingBlock(IWorld world, BlockPos pos, IBlockState state) {
            state.dropBlockAsItem(world.getWorld(), pos, 0);
        }
        @Override public int getSlopeFindDistance(IWorldReaderBase world) { return 4; }
        @Override public int getLevelDecreasePerBlock(IWorldReaderBase world) { return 1; }
        @Override public int getTickRate(IWorldReaderBase world) { return 15; }
        @Override protected float getExplosionResistance() { return 100.0F; }
        @Override public boolean isEquivalentTo(Fluid fluid) {
            return fluid == CRUDE_OIL || fluid == FLOWING_CRUDE_OIL;
        }
        @Override protected boolean canOtherFlowInto(IFluidState state, Fluid fluid, EnumFacing direction) {
            return direction == EnumFacing.DOWN && !fluid.isEquivalentTo(this);
        }
        @Override public IBlockState getBlockState(IFluidState state) {
            return CRUDE_OIL_BLOCK.getDefaultState()
                    .with(BlockFlowingFluid.LEVEL, Integer.valueOf(getLevelFromState(state)));
        }

        public static final class Flowing extends CrudeOilFluid {
            @Override protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder) {
                super.fillStateContainer(builder);
                builder.add(LEVEL_1_TO_8);
            }
            @Override public int getLevel(IFluidState state) { return state.get(LEVEL_1_TO_8); }
            @Override public boolean isSource(IFluidState state) { return false; }
        }

        public static final class Source extends CrudeOilFluid {
            @Override public int getLevel(IFluidState state) { return 8; }
            @Override public boolean isSource(IFluidState state) { return true; }
        }
    }
}
