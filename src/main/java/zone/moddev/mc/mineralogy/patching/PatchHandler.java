package zone.moddev.mc.mineralogy.patching;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PatchHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ResourceLocation SAPROLITE = mineralogyId("saprolite");
	private static final ResourceLocation PUMMICE = mineralogyId("pummice");
	private static final ResourceLocation LIMESTONE = mineralogyId("limestone");
	private static final ResourceLocation PUMICE = mineralogyId("pumice");
	private static final ResourceLocation GRASS_PATH = minecraftId("grass_path");
	private static final ResourceLocation DIRT_PATH = minecraftId("dirt_path");
	private static final ResourceLocation SWEET_BERRIES_PICK = minecraftId("item.sweet_berries.pick_from_bush");
	private static final ResourceLocation SWEET_BERRY_BUSH_PICK = minecraftId("block.sweet_berry_bush.pick_berries");

	private PatchHandler() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	@SubscribeEvent
	public static void remapMissingBlocks(RegistryEvent.MissingMappings<Block> event) {
		if (!MineralogyConfig.patchUpdate()) {
			return;
		}

		remapMissing(event, SAPROLITE, ForgeRegistries.BLOCKS.getValue(LIMESTONE));
		remapMissing(event, PUMMICE, ForgeRegistries.BLOCKS.getValue(PUMICE));
		remapMissing(event, GRASS_PATH, ForgeRegistries.BLOCKS.getValue(DIRT_PATH));
	}

	@SubscribeEvent
	public static void remapMissingItems(RegistryEvent.MissingMappings<Item> event) {
		if (!MineralogyConfig.patchUpdate()) {
			return;
		}

		remapMissing(event, SAPROLITE, ForgeRegistries.ITEMS.getValue(LIMESTONE));
		remapMissing(event, PUMMICE, ForgeRegistries.ITEMS.getValue(PUMICE));
		remapMissing(event, GRASS_PATH, ForgeRegistries.ITEMS.getValue(DIRT_PATH));
	}

	@SubscribeEvent
	public static void remapMissingSounds(RegistryEvent.MissingMappings<SoundEvent> event) {
		if (!MineralogyConfig.patchUpdate()) {
			return;
		}

		remapMissing(event, SWEET_BERRIES_PICK, ForgeRegistries.SOUND_EVENTS.getValue(SWEET_BERRY_BUSH_PICK));
	}

	private static <T extends IForgeRegistryEntry<T>> void remapMissing(RegistryEvent.MissingMappings<T> event,
			ResourceLocation oldId, T replacement) {
		for (RegistryEvent.MissingMappings.Mapping<T> mapping : event.getAllMappings()) {
			if (!oldId.equals(mapping.key)) {
				continue;
			}

			if (replacement != null) {
				mapping.remap(replacement);
				LOGGER.info("Remapped legacy registry id '{}' to '{}'", oldId, replacement.getRegistryName());
			} else {
				mapping.warn();
				LOGGER.warn("Could not remap legacy registry id '{}' because the replacement is not registered", oldId);
			}
		}
	}

	private static ResourceLocation mineralogyId(String path) {
		return new ResourceLocation(Mineralogy.MODID, path);
	}

	private static ResourceLocation minecraftId(String path) {
		return new ResourceLocation("minecraft", path);
	}
}
