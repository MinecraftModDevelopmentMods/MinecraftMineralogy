package com.mcmoddev.mineralogy.inventory;

import com.mcmoddev.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.util.IIntArray;

public class ContainerRockFurnace extends FurnaceContainer {
	public ContainerRockFurnace(int windowId, PlayerInventory playerInventory, TileEntityRockFurnace furnace,
			IIntArray furnaceData) {
		super(windowId, playerInventory, furnace, furnaceData);
		SlotRockFurnaceOutput outputSlot = new SlotRockFurnaceOutput(playerInventory.player, furnace, 2, 116, 35);
		outputSlot.slotNumber = 2;
		this.inventorySlots.set(2, outputSlot);
	}
}
