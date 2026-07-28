package zone.moddev.mc.mineralogy.patching;

import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID)
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
	public static void remapMissingMappings(MissingMappingsEvent event) {
		if (!MineralogyConfig.patchUpdate()) {
			return;
		}

		remapMissing(event.getMappings(ForgeRegistries.Keys.BLOCKS, Mineralogy.MODID),
				SAPROLITE, ForgeRegistries.BLOCKS.getValue(LIMESTONE));
		remapMissing(event.getMappings(ForgeRegistries.Keys.BLOCKS, Mineralogy.MODID),
				PUMMICE, ForgeRegistries.BLOCKS.getValue(PUMICE));
		remapMissing(event.getMappings(ForgeRegistries.Keys.ITEMS, Mineralogy.MODID),
				SAPROLITE, ForgeRegistries.ITEMS.getValue(LIMESTONE));
		remapMissing(event.getMappings(ForgeRegistries.Keys.ITEMS, Mineralogy.MODID),
				PUMMICE, ForgeRegistries.ITEMS.getValue(PUMICE));
	}

	private static <T> void remapMissing(List<MissingMappingsEvent.Mapping<T>> mappings,
			ResourceLocation oldId, T replacement) {
		for (MissingMappingsEvent.Mapping<T> mapping : mappings) {
			if (!oldId.equals(mapping.getKey())) {
				continue;
			}

			if (replacement != null) {
				mapping.remap(replacement);
				LOGGER.info("Remapped legacy Mineralogy id '{}'", oldId);
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
