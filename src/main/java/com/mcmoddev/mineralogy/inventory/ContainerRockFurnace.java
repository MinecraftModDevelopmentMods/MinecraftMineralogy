package com.mcmoddev.mineralogy.inventory;

import com.mcmoddev.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;

public class ContainerRockFurnace extends ContainerFurnace {
	public ContainerRockFurnace(InventoryPlayer playerInventory, TileEntityRockFurnace furnace) {
		super(playerInventory, furnace);
		SlotRockFurnaceOutput outputSlot = new SlotRockFurnaceOutput(playerInventory.player, furnace, 2, 116, 35);
		outputSlot.slotNumber = 2;
		this.inventorySlots.set(2, outputSlot);
	}
}
