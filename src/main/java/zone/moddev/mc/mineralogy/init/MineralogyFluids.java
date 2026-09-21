package zone.moddev.mc.mineralogy.init;

import java.util.function.Consumer;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.MineralogyLiquidBlock;
import zone.moddev.mc.mineralogy.items.MineralogyBucketItem;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MineralogyFluids {
	private static final DeferredRegister<FluidType> FLUID_TYPES =
			DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Mineralogy.MODID);
	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
			Mineralogy.MODID);
	private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
			DeferredRegister.create(ForgeRegistries.BLOCKS, Mineralogy.MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Mineralogy.MODID);

	private static final Identifier CRUDE_OIL_STILL = Identifier.fromNamespaceAndPath(Mineralogy.MODID,
			"blocks/crude_oil_still");
	private static final Identifier CRUDE_OIL_FLOW = Identifier.fromNamespaceAndPath(Mineralogy.MODID,
			"blocks/crude_oil_flow");

	public static final RegistryObject<FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil",
			() -> new FluidType(FluidType.Properties.create()
					.density(850)
					.viscosity(6000)
					.temperature(300)
					.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
					.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
				@Override
				public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
					consumer.accept(new IClientFluidTypeExtensions() {
						@Override
						public Identifier getStillTexture() {
							return CRUDE_OIL_STILL;
						}

						@Override
						public Identifier getFlowingTexture() {
							return CRUDE_OIL_FLOW;
						}
					});
				}
			});

	private static final ForgeFlowingFluid.Properties CRUDE_OIL_PROPERTIES =
			new ForgeFlowingFluid.Properties(MineralogyFluids::crudeOilType,
					MineralogyFluids::crudeOil,
					MineralogyFluids::flowingCrudeOil)
					.bucket(MineralogyFluids::crudeOilBucket)
					.block(MineralogyFluids::crudeOilBlock)
					.slopeFindDistance(2)
					.levelDecreasePerBlock(2)
					.tickRate(15)
					.explosionResistance(100.0F);

	public static final RegistryObject<ForgeFlowingFluid.Source> CRUDE_OIL =
			FLUIDS.register("crude_oil", () -> new ForgeFlowingFluid.Source(CRUDE_OIL_PROPERTIES));
	public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_CRUDE_OIL =
			FLUIDS.register("flowing_crude_oil", () -> new ForgeFlowingFluid.Flowing(CRUDE_OIL_PROPERTIES));
	public static final RegistryObject<LiquidBlock> CRUDE_OIL_BLOCK = BLOCKS.register("crude_oil",
			() -> new MineralogyLiquidBlock(MineralogyFluids::crudeOilFlowing,
					RegistrationProperties.block(BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable()
							.noCollision().strength(100.0F).noLootTable().liquid()
							.pushReaction(PushReaction.POPPED), "crude_oil")));
	public static final RegistryObject<Item> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
			() -> new MineralogyBucketItem(MineralogyFluids::crudeOil,
					RegistrationProperties.item(
							new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1), "crude_oil_bucket")));

	public static void register(BusGroup modBusGroup) {
		FLUID_TYPES.register(modBusGroup);
		FLUIDS.register(modBusGroup);
		BLOCKS.register(modBusGroup);
		ITEMS.register(modBusGroup);
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
