package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.blocks.Ore;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockRelief;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.blocks.RockWall;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyItemGroups {
	private MineralogyItemGroups() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static ItemGroup forBlock(Block block) {
		if (!MineralogyConfig.groupCreativeTabItemsByType()) {
			return MainGroup.MINERALOGY;
		}

		if (block instanceof RockRelief) {
			return ItemGroups.ITEM;
		}
		if (block instanceof RockStairs) {
			return StairGroup.STAIR;
		}
		if (block instanceof RockSlab) {
			return SlabGroup.SLAB;
		}
		if (block instanceof RockWall) {
			return WallGroup.WALL;
		}
		if (block instanceof Rock || block instanceof Ore) {
			return RockGroup.ROCK;
		}

		return ItemGroups.ITEM;
	}

	public static ItemGroup forItem() {
		return MineralogyConfig.groupCreativeTabItemsByType() ? ItemGroups.ITEM : MainGroup.MINERALOGY;
	}

	private static ItemGroup create(String name, String iconItemName) {
		return new ItemGroup(Mineralogy.MODID + "." + name) {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, iconItemName));
				return item != null ? new ItemStack(item) : new ItemStack(net.minecraft.item.Items.IRON_PICKAXE);
			}
		};
	}

	private static final class MainGroup {
		private static final ItemGroup MINERALOGY = create("mineralogy", "basalt");
	}

	private static final class RockGroup {
		private static final ItemGroup ROCK = create("rock", "basalt");
	}

	private static final class StairGroup {
		private static final ItemGroup STAIR = create("stair", "basalt_stairs");
	}

	private static final class SlabGroup {
		private static final ItemGroup SLAB = create("slab", "basalt_slab");
	}

	private static final class WallGroup {
		private static final ItemGroup WALL = create("wall", "basalt_wall");
	}

	private static final class ItemGroups {
		private static final ItemGroup ITEM = create("item", "sulfur_dust");
	}
}
