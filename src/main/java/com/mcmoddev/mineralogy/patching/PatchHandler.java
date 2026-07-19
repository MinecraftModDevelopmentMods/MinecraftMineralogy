package com.mcmoddev.mineralogy.patching;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
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
	}

	@SubscribeEvent
	public static void remapMissingItems(RegistryEvent.MissingMappings<Item> event) {
		if (!MineralogyConfig.patchUpdate()) {
			return;
		}

		remapMissing(event, SAPROLITE, ForgeRegistries.ITEMS.getValue(LIMESTONE));
		remapMissing(event, PUMMICE, ForgeRegistries.ITEMS.getValue(PUMICE));
	}

	private static <T extends IForgeRegistryEntry<T>> void remapMissing(RegistryEvent.MissingMappings<T> event,
			ResourceLocation oldId, T replacement) {
		// Legacy world snapshots are injected before Forge associates this event
		// with an active mod container, so getMappings() cannot namespace-filter it.
		for (RegistryEvent.MissingMappings.Mapping<T> mapping : event.getAllMappings()) {
			if (!oldId.equals(mapping.key)) {
				continue;
			}

			if (replacement != null) {
				mapping.remap(replacement);
				LOGGER.info("Remapped legacy Mineralogy id '{}' to '{}'", oldId, replacement.getRegistryName());
			} else {
				mapping.warn();
				LOGGER.warn("Could not remap legacy Mineralogy id '{}' because the replacement is not registered", oldId);
			}
		}
	}

	private static ResourceLocation mineralogyId(String path) {
		return new ResourceLocation(Mineralogy.MODID, path);
	}
}
