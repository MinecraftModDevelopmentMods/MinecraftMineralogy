package zone.moddev.mc.mineralogy.fluids;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.init.MineralogyItemGroups;

/** Forge 37-native source, flowing block and bucket for Mineralogy crude oil. */
@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MineralogyFluids {
    private static final ResourceLocation STILL_TEXTURE =
            new ResourceLocation(Mineralogy.MODID, "blocks/crude_oil_still");
    private static final ResourceLocation FLOW_TEXTURE =
            new ResourceLocation(Mineralogy.MODID, "blocks/crude_oil_flow");

    public static ForgeFlowingFluid.Source CRUDE_OIL;
    public static ForgeFlowingFluid.Flowing FLOWING_CRUDE_OIL;
    public static LiquidBlock CRUDE_OIL_BLOCK;
    public static Item CRUDE_OIL_BUCKET;

    private static final ForgeFlowingFluid.Properties PROPERTIES =
            new ForgeFlowingFluid.Properties(() -> CRUDE_OIL, () -> FLOWING_CRUDE_OIL,
                    FluidAttributes.builder(STILL_TEXTURE, FLOW_TEXTURE)
                            .translationKey("fluid.mineralogy.crude_oil")
                            .density(850)
                            .viscosity(2000))
                    .bucket(() -> CRUDE_OIL_BUCKET)
                    .block(() -> CRUDE_OIL_BLOCK)
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .tickRate(15)
                    .explosionResistance(100.0F);

    private MineralogyFluids() {
    }

    @SubscribeEvent
    public static void registerFluids(RegistryEvent.Register<Fluid> event) {
        CRUDE_OIL = (ForgeFlowingFluid.Source) new ForgeFlowingFluid.Source(PROPERTIES)
                .setRegistryName(Mineralogy.MODID, "crude_oil");
        FLOWING_CRUDE_OIL = (ForgeFlowingFluid.Flowing) new ForgeFlowingFluid.Flowing(PROPERTIES)
                .setRegistryName(Mineralogy.MODID, "flowing_crude_oil");
        event.getRegistry().registerAll(CRUDE_OIL, FLOWING_CRUDE_OIL);
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        CRUDE_OIL_BLOCK = (LiquidBlock) new LiquidBlock(() -> CRUDE_OIL,
                BlockBehaviour.Properties.of(Material.WATER).noCollission()
                        .strength(100.0F).noDrops())
                .setRegistryName(Mineralogy.MODID, "crude_oil");
        event.getRegistry().register(CRUDE_OIL_BLOCK);
    }

    @SubscribeEvent
    public static void registerBucket(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET);
        if (MineralogyConfig.isCreativeVisible("crude_oil_bucket")) {
            properties.tab(MineralogyItemGroups.forItem());
        }
        CRUDE_OIL_BUCKET = new BucketItem(() -> CRUDE_OIL, properties)
                .setRegistryName(Mineralogy.MODID, "crude_oil_bucket");
        event.getRegistry().register(CRUDE_OIL_BUCKET);
    }
}
