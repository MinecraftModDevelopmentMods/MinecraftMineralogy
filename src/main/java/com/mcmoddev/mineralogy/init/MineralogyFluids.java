package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.blocks.MineralogyLiquidBlock;
import com.mcmoddev.mineralogy.items.MineralogyBucketItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MineralogyFluids {
	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
			Mineralogy.MODID);
	private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
			DeferredRegister.create(ForgeRegistries.BLOCKS, Mineralogy.MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Mineralogy.MODID);

	private static final ResourceLocation CRUDE_OIL_STILL = new ResourceLocation(Mineralogy.MODID,
			"blocks/crude_oil_still");
	private static final ResourceLocation CRUDE_OIL_FLOW = new ResourceLocation(Mineralogy.MODID,
			"blocks/crude_oil_flow");
	private static final ForgeFlowingFluid.Properties CRUDE_OIL_PROPERTIES =
			new ForgeFlowingFluid.Properties(MineralogyFluids::crudeOil,
					MineralogyFluids::flowingCrudeOil,
					FluidAttributes.builder(CRUDE_OIL_STILL, CRUDE_OIL_FLOW)
							.density(850)
							.viscosity(6000)
							.temperature(300)
							.sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY))
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
					BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
	public static final RegistryObject<Item> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
			() -> new MineralogyBucketItem(MineralogyFluids::crudeOil,
					new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
							.tab(MineralogyItemGroups.forItem())));

	public static void register(IEventBus modBus) {
		FLUIDS.register(modBus);
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
	}

	public static Fluid crudeOil() {
		return CRUDE_OIL.get();
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
