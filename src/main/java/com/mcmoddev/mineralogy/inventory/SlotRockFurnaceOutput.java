package com.mcmoddev.mineralogy.inventory;

import java.util.Map;

import com.mcmoddev.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class SlotRockFurnaceOutput extends Slot {
	private final PlayerEntity player;
	private final TileEntityRockFurnace furnace;
	private int removeCount;

	public SlotRockFurnaceOutput(PlayerEntity player, TileEntityRockFurnace furnace, int index, int xPosition,
			int yPosition) {
		super(furnace, index, xPosition, yPosition);
		this.player = player;
		this.furnace = furnace;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if (getHasStack()) {
			removeCount += Math.min(amount, getStack().getCount());
		}

		return super.decrStackSize(amount);
	}

	@Override
	public ItemStack onTake(PlayerEntity player, ItemStack stack) {
		onCrafting(stack);
		super.onTake(player, stack);
		return stack;
	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		removeCount += amount;
		onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		stack.onCrafting(player.world, player, removeCount);

		if (!player.world.isRemote) {
			spawnExperience();
			furnace.onCrafting(player);
		}

		removeCount = 0;
		BasicEventHooks.firePlayerSmeltedEvent(player, stack);
	}

	private void spawnExperience() {
		for (Map.Entry<ResourceLocation, Integer> entry : furnace.getRecipeUseCounts().entrySet()) {
			IRecipe<?> recipe = player.world.getRecipeManager().getRecipe(entry.getKey()).orElse(null);
			float experience = recipe instanceof FurnaceRecipe ? ((FurnaceRecipe) recipe).getExperience() : 0.0F;
			int amount = getExperienceAmount(entry.getValue().intValue(), experience);

			while (amount > 0) {
				int split = ExperienceOrbEntity.getXPSplit(amount);
				amount -= split;
				if (player.world instanceof ServerWorld) {
					((ServerWorld) player.world).addEntity(new ExperienceOrbEntity(player.world, player.getPosX(),
							player.getPosY() + 0.5D, player.getPosZ() + 0.5D, split));
				}
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

		int amount = MathHelper.floor((float) smeltedCount * experience);
		if (amount < MathHelper.ceil((float) smeltedCount * experience)
				&& Math.random() < (double) ((float) smeltedCount * experience - (float) amount)) {
			++amount;
		}

		return amount;
	}
}
