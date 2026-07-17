package com.mcmoddev.mineralogy;

import java.util.List;

import com.mcmoddev.mineralogy.init.MineralogyFluids;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.api.MineralogyOreIntegration;
import com.mcmoddev.mineralogy.worldgen.MineralogyOreGeneration;
import com.mcmoddev.mineralogy.worldgen.GeomeConfig;
import com.mcmoddev.mineralogy.worldgen.GeomeDistributionSampler;
import com.mcmoddev.mineralogy.worldgen.OilDepositFeature;
import com.mcmoddev.mineralogy.worldgen.StoneReplacer;
import com.mcmoddev.mineralogy.worldgen.WorldGeologyProfileManager;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Preset;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;
import com.mcmoddev.mineralogy.worldgen.WorldGeologyProfile;
import com.mcmoddev.mineralogy.worldgen.WorldgenBenchmark;

import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueInterMod);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		MineralogyFluids.register(FMLJavaModLoadingContext.get().getModEventBus());
		MinecraftForge.EVENT_BUS.addListener(StoneReplacer::onBiomeLoading);
		MinecraftForge.EVENT_BUS.addListener(MineralogyOreGeneration::onBiomeLoading);
		MinecraftForge.EVENT_BUS.addListener(OilDepositFeature::onBiomeLoading);
		MinecraftForge.EVENT_BUS.addListener(WorldGeologyProfileManager::onServerAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(WorldGeologyProfileManager::onServerStopped);
		WorldgenBenchmark.register();
	}

	private void loadComplete(final FMLLoadCompleteEvent event) {
		event.enqueueWork(() -> {
			// Provider mods may create their handoff file during common setup. Scan again
			// after every mod has completed that phase, then refresh new-world defaults.
			MineralogyOreIntegration.initialize();
			GeomeConfig.bake();
			MineralogyOreGeneration.refreshWorldConfig();
		});
	}

	private void enqueueInterMod(final InterModEnqueueEvent event) {
		// Common setup has completed for every mod. Provider mods can create their
		// handoff file there, then query the API during IMC processing or later.
		MineralogyOreIntegration.initialize();
		GeomeConfig.bake();
		MineralogyOreGeneration.refreshWorldConfig();
		MineralogyOreIntegration.markFeatureReady();
	}

	private void setup(final FMLCommonSetupEvent event) {
		MineralogyConfig.bake();
		applyGeologyConfigOverrides();
		MineralogyOreIntegration.initialize();
		GeomeConfig.bake();
		logGeomeSampler();
		event.enqueueWork(() -> {
			StoneReplacer.registerConfiguredFeature();
			MineralogyOreGeneration.registerConfiguredFeatures();
			OilDepositFeature.registerConfiguredFeature();
		});
	}

	private static void logGeomeSampler() {
		if (!Boolean.getBoolean("mineralogy.geomeSampler")) {
			return;
		}

		WorldGeologyProfile original = GeomeConfig.globalProfile();
		String defaultSeed = Long.toString(Long.getLong("mineralogy.geomeSamplerSeed", 19780401L));
		String[] samplerSeeds = System.getProperty("mineralogy.geomeSamplerSeeds", defaultSeed).split(",");
		String profileFilter = System.getProperty("mineralogy.geomeSamplerProfiles", "all");
		boolean includeBiomeAudit = Boolean.parseBoolean(
				System.getProperty("mineralogy.geomeSamplerBiomeAudit", "true"));
		try {
			for (String seedText : samplerSeeds) {
				long samplerSeed = Long.parseLong(seedText.trim());
				for (Preset preset : new Preset[] {
						Preset.TINY, Preset.SMALL, Preset.AVERAGE, Preset.LARGE, Preset.HUGE }) {
					if (!samplerProfileEnabled(profileFilter, preset.configName())) {
						continue;
					}
					WorldGeologyProfile profile = original
							.withSelection(GeologyMode.GEOME, preset, preset, preset, preset, preset,
									original.placeCrudeOil());
					logSamplerProfile("Sky " + preset.configName(), samplerSeed, profile, includeBiomeAudit);
				}
				if (samplerProfileEnabled(profileFilter, "mixed_huge")) {
					WorldGeologyProfile mixedHuge = original
							.withSelection(GeologyMode.GEOME, Preset.AVERAGE, Preset.HUGE, Preset.HUGE,
									Preset.HUGE, Preset.HUGE, original.placeCrudeOil());
					logSamplerProfile("Sky mixed-huge", samplerSeed, mixedHuge, includeBiomeAudit);
				}
			}
		} finally {
			GeomeConfig.applyWorldProfile(original);
		}
	}

	private static boolean samplerProfileEnabled(String filter, String profile) {
		if ("all".equalsIgnoreCase(filter.trim())) {
			return true;
		}
		for (String configured : filter.split(",")) {
			if (profile.equalsIgnoreCase(configured.trim())) {
				return true;
			}
		}
		return false;
	}

	private static void logSamplerProfile(String label, long seed, WorldGeologyProfile profile,
			boolean includeBiomeAudit) {
		GeomeConfig.applyWorldProfile(profile);
		String terrainSample = System.getProperty("mineralogy.geomeSamplerTerrain");
		if (terrainSample == null || terrainSample.trim().isEmpty()) {
			LOGGER.info("\n{} sampler\n{}", label,
					GeomeDistributionSampler.sample(seed, ForgeRegistries.BIOMES.getValues(), 8, 8,
							includeBiomeAudit));
			return;
		}
		try {
			LOGGER.info("\n{} sampler\n{}", label,
					GeomeDistributionSampler.sampleTerrain(seed, java.nio.file.Paths.get(terrainSample)));
		} catch (java.io.IOException e) {
			LOGGER.error("Could not replay Mineralogy terrain sample '{}'", terrainSample, e);
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
