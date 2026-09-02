package zone.moddev.mc.mineralogy.fixture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(HistoricalOilCompatibilityProbe.MODID)
public final class HistoricalOilCompatibilityProbe {
    public static final String MODID = "poweradvantage";

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    private static final RegistryObject<FluidType> TYPE = FLUID_TYPES.register("crude_oil",
            () -> new FluidType(FluidType.Properties.create().density(850).viscosity(6000)));
    private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
            TYPE, HistoricalOilCompatibilityProbe::source,
            HistoricalOilCompatibilityProbe::flowing)
            .bucket(HistoricalOilCompatibilityProbe::bucket)
            .block(HistoricalOilCompatibilityProbe::block)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .tickRate(15);
    private static final RegistryObject<ForgeFlowingFluid.Source> SOURCE = FLUIDS.register("crude_oil",
            () -> new ForgeFlowingFluid.Source(PROPERTIES));
    private static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING = FLUIDS.register("flowing_crude_oil",
            () -> new ForgeFlowingFluid.Flowing(PROPERTIES));
    private static final RegistryObject<LiquidBlock> BLOCK = BLOCKS.register("crude_oil",
            () -> new LiquidBlock(HistoricalOilCompatibilityProbe::source,
                    BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable()
                            .noCollission().strength(100.0F).noLootTable().liquid()
                            .pushReaction(PushReaction.DESTROY)));
    private static final RegistryObject<Item> BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(HistoricalOilCompatibilityProbe::source,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public HistoricalOilCompatibilityProbe(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(ServerStartedEvent event) {
        Fluid mineralogy = requireFluid("mineralogy", "crude_oil");
        Fluid historical = requireFluid(MODID, "crude_oil");
        Item mineralogyBucket = requireItem("mineralogy", "crude_oil_bucket");
        Item historicalBucket = requireItem(MODID, "crude_oil_bucket");
        TagKey<Fluid> oilTag = TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath("forge", "crude_oil"));
        TagKey<Item> bucketTag = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("forge", "buckets/crude_oil"));

        boolean distinctFluids = mineralogy != historical;
        boolean distinctBuckets = mineralogyBucket != historicalBucket;
        boolean additiveFluidTag = mineralogy.defaultFluidState().is(oilTag)
                && historical.defaultFluidState().is(oilTag);
        boolean additiveBucketTag = new ItemStack(mineralogyBucket).is(bucketTag)
                && new ItemStack(historicalBucket).is(bucketTag);
        if (!distinctFluids || !distinctBuckets || !additiveFluidTag || !additiveBucketTag) {
            throw new IllegalStateException("Historical crude-oil coexistence contract failed: distinctFluids="
                    + distinctFluids + ", distinctBuckets=" + distinctBuckets + ", additiveFluidTag="
                    + additiveFluidTag + ", additiveBucketTag=" + additiveBucketTag);
        }

        String result = "mineralogy_fluid=mineralogy:crude_oil\n"
                + "historical_fluid=poweradvantage:crude_oil\n"
                + "distinct_fluids=true\n"
                + "mineralogy_bucket=mineralogy:crude_oil_bucket\n"
                + "historical_bucket=poweradvantage:crude_oil_bucket\n"
                + "distinct_buckets=true\n"
                + "additive_fluid_tag=true\n"
                + "additive_bucket_tag=true\n";
        try {
            Files.writeString(Path.of("oil-compatibility-pass.properties"), result,
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write oil compatibility marker", exception);
        }
        event.getServer().halt(false);
    }

    private static Fluid requireFluid(String namespace, String path) {
        Fluid result = ForgeRegistries.FLUIDS.getValue(
                ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (result == null) {
            throw new IllegalStateException("Missing fluid " + namespace + ':' + path);
        }
        return result;
    }

    private static Item requireItem(String namespace, String path) {
        Item result = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (result == null) {
            throw new IllegalStateException("Missing item " + namespace + ':' + path);
        }
        return result;
    }

    private static FlowingFluid source() {
        return SOURCE.get();
    }

    private static FlowingFluid flowing() {
        return FLOWING.get();
    }

    private static LiquidBlock block() {
        return BLOCK.get();
    }

    private static Item bucket() {
        return BUCKET.get();
    }
}
