package com.mcmoddev.mineralogy.api;

import java.util.Optional;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.integration.WorldgenIntegrationManager;
import com.mcmoddev.mineralogy.worldgen.WorldGeologyProfileManager;
import com.mcmoddev.mineralogy.worldgen.GeomeConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.InterModComms;

/** Entry point for Mineralogy API version 1. */
public final class MineralogyApi {
	public static final int API_VERSION = 1;
	public static final String IMC_WORLDGEN_PROVIDER = "worldgen_provider_v1";

	private MineralogyApi() {
	}

	/**
	 * Enqueues a provider through Forge IMC. Call this during
	 * {@code InterModEnqueueEvent}.
	 */
	public static boolean enqueue(WorldgenProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider cannot be null");
		}
		return InterModComms.sendTo(provider.modId(), Mineralogy.MODID,
				IMC_WORLDGEN_PROVIDER, () -> provider);
	}

	public static ProviderStatus getProviderStatus(String providerModId) {
		return WorldgenIntegrationManager.getProviderStatus(providerModId);
	}

	public static boolean isOreTakeoverActive(String providerModId) {
		return WorldgenIntegrationManager.isOreTakeoverActive(providerModId);
	}

	public static Optional<GeologyProfileView> getActiveProfile(MinecraftServer server) {
		if (server == null || WorldGeologyProfileManager.activeServer() != server) {
			return Optional.empty();
		}
		return Optional.of(new GeologyProfileView(WorldGeologyProfileManager.activeProfile().toJson()));
	}

	public static Optional<GeologySampler> createSampler(ServerLevel level) {
		if (level == null || WorldGeologyProfileManager.activeServer() != level.getServer()
				|| GeomeConfig.baked(level.dimension()) == null) {
			return Optional.empty();
		}
		return Optional.of(MineralogyGeologySampler.create(level));
	}
}
