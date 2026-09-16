package zone.moddev.mc.mineralogy.oilfixture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(HistoricalOilCompatibilityProbe.MODID)
public final class HistoricalOilCompatibilityProbe {
    public static final String MODID = "poweradvantage";

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, MODID);
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    private static final DeferredHolder<FluidType, FluidType> TYPE = FLUID_TYPES.register("crude_oil",
            () -> new FluidType(FluidType.Properties.create().density(850).viscosity(6000)));
    private static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(
            TYPE, HistoricalOilCompatibilityProbe::source,
            HistoricalOilCompatibilityProbe::flowing)
            .bucket(HistoricalOilCompatibilityProbe::bucket)
            .block(HistoricalOilCompatibilityProbe::block)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .tickRate(15);
    private static final DeferredHolder<Fluid, BaseFlowingFluid.Source> SOURCE = FLUIDS.register("crude_oil",
            () -> new BaseFlowingFluid.Source(PROPERTIES));
    private static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING = FLUIDS.register("flowing_crude_oil",
            () -> new BaseFlowingFluid.Flowing(PROPERTIES));
    private static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> BLOCK = BLOCKS.register("crude_oil",
            () -> new LiquidBlock(source(),
                    blockProperties(BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable(), "crude_oil")
                            .noCollision().strength(100.0F).noLootTable().liquid()
							.pushReaction(PushReaction.POPPED)));
    private static final DeferredHolder<Item, Item> BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(source(),
                    itemProperties(new Item.Properties(), "crude_oil_bucket")
                            .craftRemainder(Items.BUCKET).stacksTo(1)));

    public HistoricalOilCompatibilityProbe(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(ServerStartedEvent event) {
        Fluid mineralogy = requireFluid("mineralogy", "crude_oil");
        Fluid historical = requireFluid(MODID, "crude_oil");
        Item mineralogyBucket = requireItem("mineralogy", "crude_oil_bucket");
        Item historicalBucket = requireItem(MODID, "crude_oil_bucket");
        TagKey<Fluid> oilTag = TagKey.create(Registries.FLUID,
                Identifier.fromNamespaceAndPath("c", "crude_oil"));
        TagKey<Item> bucketTag = TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "buckets/crude_oil"));

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
        Fluid result = BuiltInRegistries.FLUID.getValue(
                Identifier.fromNamespaceAndPath(namespace, path));
        if (result == null) {
            throw new IllegalStateException("Missing fluid " + namespace + ':' + path);
        }
        return result;
    }

    private static Item requireItem(String namespace, String path) {
        Item result = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(namespace, path));
        if (result == null) {
            throw new IllegalStateException("Missing item " + namespace + ':' + path);
        }
        return result;
    }

    private static BlockBehaviour.Properties blockProperties(BlockBehaviour.Properties properties, String path) {
        return properties.setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MODID, path)));
    }

    private static Item.Properties itemProperties(Item.Properties properties, String path) {
        return properties.setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(MODID, path)));
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
