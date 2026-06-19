package com.mcmoddev.mineralogy;

import java.util.List;

import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.worldgen.MineralogyOreGeneration;
import com.mcmoddev.mineralogy.worldgen.GeomeConfig;
import com.mcmoddev.mineralogy.worldgen.GeomeDistributionSampler;
import com.mcmoddev.mineralogy.worldgen.StoneReplacer;

import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mineralogy.MODID)
public class Mineralogy {
	public static Mineralogy instance;

	public static final String MODID = "mineralogy";
	public static final String NAME = "Mineralogy";
	public static final String VERSION = getVersion();

	private static final Logger LOGGER = LogManager.getLogger();

	private static String getVersion() {
		Package metadata = Mineralogy.class.getPackage();
		String version = metadata == null ? null : metadata.getImplementationVersion();
		return version == null ? "DEV" : version;
	}

	public Mineralogy() {
		instance = this;
		MineralogyConfig.register();
		MineralogyConfig.registerRecipeConditions();
		MineralogyConfig.registerAdvancementPredicates();

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.addListener(StoneReplacer::onBiomeLoading);
		MinecraftForge.EVENT_BUS.addListener(MineralogyOreGeneration::onBiomeLoading);
	}

	private void setup(final FMLCommonSetupEvent event) {
		MineralogyConfig.bake();
		applyGeologyConfigOverrides();
		GeomeConfig.bake();
		logGeomeSampler();
		event.enqueueWork(() -> {
			StoneReplacer.registerConfiguredFeature();
			MineralogyOreGeneration.registerConfiguredFeatures();
		});
	}

	private static void logGeomeSampler() {
		if (Boolean.getBoolean("mineralogy.geomeSampler")) {
			LOGGER.info("\n{}", GeomeDistributionSampler.sample(19780401L, ForgeRegistries.BIOMES.getValues(), 8, 8));
		}
	}

	private static void applyGeologyConfigOverrides() {
		addBlocks(MineralogyRegistry.igneousStones, MineralogyConfig.igneousWhitelist());
		addBlocks(MineralogyRegistry.metamorphicStones, MineralogyConfig.metamorphicWhitelist());
		addBlocks(MineralogyRegistry.sedimentaryStones, MineralogyConfig.sedimentaryWhitelist());
		removeBlocks(MineralogyRegistry.igneousStones, MineralogyConfig.igneousBlacklist());
		removeBlocks(MineralogyRegistry.metamorphicStones, MineralogyConfig.metamorphicBlacklist());
		removeBlocks(MineralogyRegistry.sedimentaryStones, MineralogyConfig.sedimentaryBlacklist());
	}

	private static void addBlocks(List<Block> target, List<String> ids) {
		for (String id : ids) {
			Block block = getBlock(id);
			if (block != null && !target.contains(block)) {
				target.add(block);
			}
		}
	}

	private static void removeBlocks(List<Block> target, List<String> ids) {
		for (String id : ids) {
			Block block = getBlock(id);
			if (block != null) {
				target.remove(block);
			}
		}
	}

	private static Block getBlock(String id) {
		try {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
			if (block == null) {
				LOGGER.warn("Ignoring unknown Mineralogy geology config block id '{}'", id);
			}
			return block;
		} catch (RuntimeException e) {
			LOGGER.warn("Ignoring invalid Mineralogy geology config block id '{}'", id);
			return null;
		}
	}
}
