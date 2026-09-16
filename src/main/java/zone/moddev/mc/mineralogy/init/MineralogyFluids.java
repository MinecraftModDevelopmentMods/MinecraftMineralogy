package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.MineralogyLiquidBlock;
import zone.moddev.mc.mineralogy.items.MineralogyBucketItem;

import net.neoforged.neoforge.common.SoundActions;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class MineralogyFluids {
	private static final DeferredRegister<FluidType> FLUID_TYPES =
			DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Mineralogy.MODID);
	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID,
			Mineralogy.MODID);
	private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
			DeferredRegister.create(BuiltInRegistries.BLOCK, Mineralogy.MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM,
			Mineralogy.MODID);

	private static final Identifier CRUDE_OIL_STILL = Identifier.fromNamespaceAndPath(Mineralogy.MODID, "blocks/crude_oil_still");
	private static final Identifier CRUDE_OIL_FLOW = Identifier.fromNamespaceAndPath(Mineralogy.MODID, "blocks/crude_oil_flow");

	public static final DeferredHolder<FluidType, FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil",
			() -> new FluidType(FluidType.Properties.create()
					.density(850)
					.viscosity(6000)
					.temperature(300)
					.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
					.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

	private static final BaseFlowingFluid.Properties CRUDE_OIL_PROPERTIES =
			new BaseFlowingFluid.Properties(MineralogyFluids::crudeOilType,
					MineralogyFluids::crudeOil,
					MineralogyFluids::flowingCrudeOil)
					.bucket(MineralogyFluids::crudeOilBucket)
					.block(MineralogyFluids::crudeOilBlock)
					.slopeFindDistance(2)
					.levelDecreasePerBlock(2)
					.tickRate(15)
					.explosionResistance(100.0F);

	public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> CRUDE_OIL =
			FLUIDS.register("crude_oil", () -> new BaseFlowingFluid.Source(CRUDE_OIL_PROPERTIES));
	public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_CRUDE_OIL =
			FLUIDS.register("flowing_crude_oil", () -> new BaseFlowingFluid.Flowing(CRUDE_OIL_PROPERTIES));
	public static final DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> CRUDE_OIL_BLOCK = BLOCKS.register("crude_oil",
			() -> new MineralogyLiquidBlock(MineralogyFluids::crudeOilFlowing,
					RegistrationProperties.block(BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable()
							.noCollision().strength(100.0F).noLootTable().liquid()
							.pushReaction(PushReaction.POPPED), "crude_oil")));
	public static final DeferredHolder<Item, Item> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
			() -> new MineralogyBucketItem(MineralogyFluids::crudeOil,
					RegistrationProperties.item(
							new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1), "crude_oil_bucket")));

	public static void register(IEventBus modEventBus) {
		FLUID_TYPES.register(modEventBus);
		FLUIDS.register(modEventBus);
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
	}

	public static Identifier crudeOilStillTexture() {
		return CRUDE_OIL_STILL;
	}

	public static Identifier crudeOilFlowTexture() {
		return CRUDE_OIL_FLOW;
	}

	public static Fluid crudeOil() {
		return CRUDE_OIL.get();
	}

	public static FluidType crudeOilType() {
		return CRUDE_OIL_TYPE.get();
	}

	public static FlowingFluid crudeOilFlowing() {
		return CRUDE_OIL.get();
	}

	public static Fluid flowingCrudeOil() {
		return FLOWING_CRUDE_OIL.get();
	}

	public static LiquidBlock crudeOilBlock() {
		return CRUDE_OIL_BLOCK.get();
	}

	public static Item crudeOilBucket() {
		return CRUDE_OIL_BUCKET.get();
	}

	private MineralogyFluids() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
