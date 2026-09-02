package zone.moddev.mc.mineralogy.init;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TileEntities {
	public static BlockEntityType<TileEntityRockFurnace> rock_furnace;

	@SubscribeEvent
	public static void registerTileEntities(RegisterEvent event) {
		if (!ForgeRegistries.Keys.BLOCK_ENTITY_TYPES.equals(event.getRegistryKey())) {
			return;
		}
		IForgeRegistry<BlockEntityType<?>> registry = event.getForgeRegistry();
		List<Block> furnaceBlocks = new ArrayList<Block>();
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (block instanceof RockFurnace) {
				furnaceBlocks.add(block);
			}
		}

		rock_furnace = BlockEntityType.Builder
				.of(TileEntityRockFurnace::new, furnaceBlocks.toArray(new Block[furnaceBlocks.size()]))
				.build(null);
		registry.register(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "rock_furnace"), rock_furnace);
	}

	private TileEntities() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
