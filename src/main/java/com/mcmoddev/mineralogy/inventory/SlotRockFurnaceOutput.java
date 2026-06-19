package com.mcmoddev.mineralogy.inventory;

import java.util.Map;

import com.mcmoddev.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.hooks.BasicEventHooks;

public class SlotRockFurnaceOutput extends Slot {
	private final Player player;
	private final TileEntityRockFurnace furnace;
	private int removeCount;

	public SlotRockFurnaceOutput(Player player, TileEntityRockFurnace furnace, int index, int xPosition,
			int yPosition) {
		super(furnace, index, xPosition, yPosition);
		this.player = player;
		this.furnace = furnace;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack remove(int amount) {
		if (hasItem()) {
			removeCount += Math.min(amount, getItem().getCount());
		}

		return super.remove(amount);
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		checkTakeAchievements(stack);
		super.onTake(player, stack);
	}

	@Override
	protected void onQuickCraft(ItemStack stack, int amount) {
		removeCount += amount;
		checkTakeAchievements(stack);
	}

	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		stack.onCraftedBy(player.level, player, removeCount);

		if (!player.level.isClientSide) {
			spawnExperience();
			furnace.onCrafting(player);
		}

		removeCount = 0;
		BasicEventHooks.firePlayerSmeltedEvent(player, stack);
	}

	private void spawnExperience() {
		for (Map.Entry<ResourceLocation, Integer> entry : furnace.getRecipeUseCounts().entrySet()) {
			Recipe<?> recipe = player.level.getRecipeManager().byKey(entry.getKey()).orElse(null);
			float experience = recipe instanceof SmeltingRecipe ? ((SmeltingRecipe) recipe).getExperience() : 0.0F;
			int amount = getExperienceAmount(entry.getValue().intValue(), experience);

			if (amount > 0 && player.level instanceof ServerLevel) {
				Vec3 position = player.position().add(0.0D, 0.5D, 0.0D);
				ExperienceOrb.award((ServerLevel) player.level, position, amount);
			}
		}
	}

	private static int getExperienceAmount(int smeltedCount, float experience) {
		if (experience == 0.0F) {
			return 0;
		}

		if (experience >= 1.0F) {
			return smeltedCount;
		}

		int amount = Mth.floor((float) smeltedCount * experience);
		if (amount < Mth.ceil((float) smeltedCount * experience)
				&& Math.random() < (double) ((float) smeltedCount * experience - (float) amount)) {
			++amount;
		}

		return amount;
	}
}
