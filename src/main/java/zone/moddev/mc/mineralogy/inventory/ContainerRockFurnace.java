package zone.moddev.mc.mineralogy.inventory;

import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.ContainerData;

public class ContainerRockFurnace extends FurnaceMenu {
	public ContainerRockFurnace(int windowId, Inventory playerInventory, TileEntityRockFurnace furnace,
			ContainerData furnaceData) {
		super(windowId, playerInventory, furnace, furnaceData);
		SlotRockFurnaceOutput outputSlot = new SlotRockFurnaceOutput(playerInventory.player, furnace, 2, 116, 35);
		this.slots.set(2, outputSlot);
	}
}
