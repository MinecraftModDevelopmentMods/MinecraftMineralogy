package com.mcmoddev.mineralogy.init;

import java.util.ArrayList;
import java.util.List;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.blocks.DryWall;
import com.mcmoddev.mineralogy.blocks.Ore;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockFurnace;
import com.mcmoddev.mineralogy.blocks.RockRelief;
import com.mcmoddev.mineralogy.blocks.RockSaltLamp;
import com.mcmoddev.mineralogy.blocks.RockSaltStreetLamp;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.blocks.RockWall;
import com.mcmoddev.mineralogy.items.MineralFertilizer;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class Items {
	public static final BlockItem basalt = null;
	public static final Item sulfur_dust = null;
	public static final Item phosphorous_dust = null;
	public static final Item nitrate_dust = null;
	public static final Item gypsum_dust = null;
	public static final Item chalk_dust = null;
	public static final Item rock_salt_dust = null;
	public static final Item salt_dust = null;
	public static final Item mineral_fertilizer = null;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(
				createItem("sulfur_dust"),
				createItem("phosphorous_dust"),
				createItem("nitrate_dust"),
				createItem("gypsum_dust"),
				createItem("chalk_dust"),
				createItem("rock_salt_dust"),
				createItem("salt_dust"),
				createFertilizer()
		);

		List<Item> blockItems = new ArrayList<Item>();

		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (isMineralogyBlockItem(block)) {
				blockItems.add(createBlockItem(block));
			}
		}

		event.getRegistry().registerAll(blockItems.toArray(new Item[blockItems.size()]));
	}

	private static boolean isMineralogyBlockItem(Block block) {
		ResourceLocation registryName = block.getRegistryName();

		return registryName != null
				&& Mineralogy.MODID.equals(registryName.getNamespace())
				&& (block instanceof Rock
						|| block instanceof RockStairs
						|| block instanceof RockWall
						|| block instanceof RockSlab
						|| isUnlitRockFurnace(block)
						|| block instanceof RockRelief
						|| block instanceof Ore
						|| block instanceof DryWall
						|| block instanceof RockSaltLamp
						|| block instanceof RockSaltStreetLamp);
	}

	private static BlockItem createBlockItem(Block block) {
		Item.Properties properties = new Item.Properties().group(MineralogyItemGroups.forBlock(block));
		if (block instanceof RockFurnace) {
			properties.maxStackSize(1);
		} else if (block instanceof RockSaltStreetLamp) {
			properties.maxStackSize(16);
		}

		BlockItem item = new BlockItem(block, properties);
		item.setRegistryName(block.getRegistryName());
		return item;
	}

	private static boolean isUnlitRockFurnace(Block block) {
		ResourceLocation registryName = block.getRegistryName();
		return block instanceof RockFurnace
				&& registryName != null
				&& !registryName.getPath().startsWith("lit_");
	}

	private static Item createItem(String name) {
		Item item = new Item(new Item.Properties().group(MineralogyItemGroups.forItem()));
		item.setRegistryName(Mineralogy.MODID, name);
		return item;
	}

	private static Item createFertilizer() {
		MineralFertilizer item = new MineralFertilizer(MineralogyItemGroups.forItem());
		item.setRegistryName(Mineralogy.MODID, "mineral_fertilizer");
		return item;
	}

	private Items() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
