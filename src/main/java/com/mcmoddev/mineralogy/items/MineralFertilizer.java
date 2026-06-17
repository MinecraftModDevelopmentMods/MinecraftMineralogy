package com.mcmoddev.mineralogy.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoneMeal;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MineralFertilizer extends Item {
	private final ItemStack phantomBonemeal = new ItemStack(Items.BONE_MEAL, 27);

	public MineralFertilizer() {
		this(com.mcmoddev.mineralogy.init.MineralogyItemGroups.forItem());
	}

	public MineralFertilizer(ItemGroup group) {
		super(new Item.Properties().group(group));
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos target = context.getPos();
		EntityPlayer player = context.getPlayer();

		boolean canUse = ItemBoneMeal.applyBonemeal(context.getItem(), world, target, player);
		if (canUse) {
			phantomBonemeal.setCount(27);
			for (int dx = -2; dx <= 2; dx++) {
				for (int dy = -2; dy <= 2; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						if ((dx | dy | dz) == 0) {
							continue;
						}
						ItemBoneMeal.applyBonemeal(phantomBonemeal, world, target.add(dx, dy, dz), player);
					}
				}
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
