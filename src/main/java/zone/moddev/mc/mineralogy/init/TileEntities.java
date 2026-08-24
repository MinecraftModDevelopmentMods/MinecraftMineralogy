package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class TileEntities {
	public static final TileEntityType<TileEntityRockFurnace> rock_furnace = null;

	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		TileEntityType<TileEntityRockFurnace> type = TileEntityType.Builder
				.create(() -> new TileEntityRockFurnace())
				.build(null);
		type.setRegistryName(Mineralogy.MODID, "rock_furnace");
		event.getRegistry().register(type);
	}

	private TileEntities() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
