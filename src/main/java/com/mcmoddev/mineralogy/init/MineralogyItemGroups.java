package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.blocks.Ore;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.blocks.RockRelief;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mcmoddev.mineralogy.blocks.RockStairs;
import com.mcmoddev.mineralogy.blocks.RockWall;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyItemGroups {
	private MineralogyItemGroups() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static CreativeModeTab forBlock(Block block) {
		if (!MineralogyConfig.groupCreativeTabItemsByType()) {
			return MainGroup.MINERALOGY;
		}

		if (block instanceof RockRelief) {
			return CreativeModeTabs.ITEM;
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

		return CreativeModeTabs.ITEM;
	}

	public static CreativeModeTab forItem() {
		return MineralogyConfig.groupCreativeTabItemsByType() ? CreativeModeTabs.ITEM : MainGroup.MINERALOGY;
	}

	private static CreativeModeTab create(String name, String iconItemName) {
		return new CreativeModeTab(Mineralogy.MODID + "." + name) {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack makeIcon() {
				Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, iconItemName));
				return item != null ? new ItemStack(item) : new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
			}
		};
	}

	private static final class MainGroup {
		private static final CreativeModeTab MINERALOGY = create("mineralogy", "basalt");
	}

	private static final class RockGroup {
		private static final CreativeModeTab ROCK = create("rock", "basalt");
	}

	private static final class StairGroup {
		private static final CreativeModeTab STAIR = create("stair", "basalt_stairs");
	}

	private static final class SlabGroup {
		private static final CreativeModeTab SLAB = create("slab", "basalt_slab");
	}

	private static final class WallGroup {
		private static final CreativeModeTab WALL = create("wall", "basalt_wall");
	}

	private static final class CreativeModeTabs {
		private static final CreativeModeTab ITEM = create("item", "sulfur_dust");
	}
}
