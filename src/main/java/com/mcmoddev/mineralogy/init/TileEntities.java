package com.mcmoddev.mineralogy.init;

import java.util.ArrayList;
import java.util.List;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.blocks.RockFurnace;
import com.mcmoddev.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class TileEntities {
	public static final BlockEntityType<TileEntityRockFurnace> rock_furnace = null;

	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
		List<Block> furnaceBlocks = new ArrayList<Block>();
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (block instanceof RockFurnace) {
				furnaceBlocks.add(block);
			}
		}

		BlockEntityType<TileEntityRockFurnace> type = BlockEntityType.Builder
				.of(TileEntityRockFurnace::new, furnaceBlocks.toArray(new Block[furnaceBlocks.size()]))
				.build(null);
		type.setRegistryName(Mineralogy.MODID, "rock_furnace");
		event.getRegistry().register(type);
	}

	private TileEntities() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
