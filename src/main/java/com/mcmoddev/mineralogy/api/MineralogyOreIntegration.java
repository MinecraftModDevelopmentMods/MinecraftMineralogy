package com.mcmoddev.mineralogy.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mcmoddev.mineralogy.worldgen.RockFamily;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Optional integration API for mods that delegate their ore generation to
 * Mineralogy. Calling mods must keep their own generation unless this reports
 * {@link ProviderStatus#ACTIVE}.
 */
public final class MineralogyOreIntegration {
	public enum ProviderStatus {
		PENDING,
		ACTIVE,
		INACTIVE
	}

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern MOD_ID = Pattern.compile("^[a-z][a-z0-9_.-]{1,63}$");
	private static final String FILE_SUFFIX = "-mineralogy.json";
	private static final int PROVIDER_SCHEMA = 1;
	private static final Map<String, ProviderDefinition> PROVIDERS = new LinkedHashMap<>();
	private static final Map<String, String> ORE_OWNERS = new HashMap<>();
	private static boolean initialized;
	private static boolean featureReady;

	private MineralogyOreIntegration() {
	}

	public static synchronized void initialize() {
		initialize(FMLPaths.CONFIGDIR.get());
	}

	static synchronized void initialize(Path configDirectory) {
		PROVIDERS.clear();
		ORE_OWNERS.clear();
		initialized = true;
		if (!Files.isDirectory(configDirectory)) {
			return;
		}

		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDirectory, "*" + FILE_SUFFIX)) {
			for (Path path : stream) {
				files.add(path);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not scan Mineralogy ore-provider files in '{}'", configDirectory, e);
			return;
		}
		files.sort(Comparator.comparing(path -> path.getFileName().toString()));
		for (Path path : files) {
			loadProvider(path);
		}
	}

	public static synchronized ProviderStatus getProviderStatus(String providerModId) {
		if (!initialized || (PROVIDERS.containsKey(providerModId) && !featureReady)) {
			return ProviderStatus.PENDING;
		}
		return featureReady && PROVIDERS.containsKey(providerModId)
				? ProviderStatus.ACTIVE : ProviderStatus.INACTIVE;
	}

	public static boolean isProviderActive(String providerModId) {
		return getProviderStatus(providerModId) == ProviderStatus.ACTIVE;
	}

	public static synchronized void markFeatureReady() {
		featureReady = true;
		for (String provider : PROVIDERS.keySet()) {
			LOGGER.info("Mineralogy ore-provider takeover active for '{}'", provider);
		}
	}

	/** Merge provider defaults without overwriting pack or world definitions. */
	public static synchronized boolean mergeProviderOres(JsonObject target) {
		JsonObject ores = object(target, "ores");
		JsonObject manifests = object(target, "ore_providers");
		boolean changed = false;
		for (ProviderDefinition provider : PROVIDERS.values()) {
			JsonObject manifest = manifests.has(provider.modId) && manifests.get(provider.modId).isJsonObject()
					? manifests.getAsJsonObject(provider.modId) : new JsonObject();
			Set<String> known = stringSet(manifest.get("known_ores"));
			Set<String> current = provider.ores.keySet();

			for (Entry<String, JsonObject> entry : provider.ores.entrySet()) {
				String oreId = entry.getKey();
				boolean previouslyKnown = known.contains(oreId);
				if (!ores.has(oreId) && !previouslyKnown) {
					ores.add(oreId, entry.getValue().deepCopy());
					changed = true;
				}
				if (ores.has(oreId) && ores.get(oreId).isJsonObject()
						&& !ores.getAsJsonObject(oreId).has("source_provider")) {
					ores.getAsJsonObject(oreId).addProperty("source_provider", provider.modId);
					changed = true;
				}
				known.add(oreId);
				if (ores.has(oreId) && ores.get(oreId).isJsonObject()) {
					ores.getAsJsonObject(oreId).remove("orphaned_provider");
				}
			}

			for (String knownOre : known) {
				if (!current.contains(knownOre) && ores.has(knownOre) && ores.get(knownOre).isJsonObject()) {
					ores.getAsJsonObject(knownOre).addProperty("orphaned_provider", true);
					changed = true;
				}
			}

			manifest.addProperty("provider_revision", provider.revision);
			JsonArray knownArray = new JsonArray();
			List<String> sortedKnown = new ArrayList<>(known);
			Collections.sort(sortedKnown);
			for (String oreId : sortedKnown) {
				knownArray.add(oreId);
			}
			manifest.add("known_ores", knownArray);
			manifests.add(provider.modId, manifest);
		}
		target.add("ores", ores);
		target.add("ore_providers", manifests);
		return changed;
	}

	public static synchronized Set<String> activeProviderIds() {
		return Collections.unmodifiableSet(new HashSet<>(PROVIDERS.keySet()));
	}

	private static void loadProvider(Path path) {
		String fileName = path.getFileName().toString();
		String fileProvider = fileName.substring(0, fileName.length() - FILE_SUFFIX.length());
		if (!MOD_ID.matcher(fileProvider).matches() || !ModList.get().isLoaded(fileProvider)) {
			return;
		}

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonElement element = new JsonParser().parse(reader);
			if (!element.isJsonObject()) {
				throw new JsonSyntaxException("root is not an object");
			}
			JsonObject root = element.getAsJsonObject();
			if (integer(root, "schema_version", -1) != PROVIDER_SCHEMA) {
				throw new JsonSyntaxException("unsupported schema_version");
			}
			String declaredProvider = string(root, "provider_modid", "");
			if (!fileProvider.equals(declaredProvider)) {
				throw new JsonSyntaxException("provider_modid does not match the file name");
			}
			int revision = integer(root, "provider_revision", -1);
			if (revision < 1) {
				throw new JsonSyntaxException("provider_revision must be at least 1");
			}
			JsonObject oreRoot = requiredObject(root, "ores");
			if (oreRoot.entrySet().isEmpty()) {
				throw new JsonSyntaxException("provider defines no ores");
			}

			LinkedHashMap<String, JsonObject> ores = new LinkedHashMap<>();
			for (Entry<String, JsonElement> oreEntry : oreRoot.entrySet()) {
				validateOre(fileProvider, oreEntry.getKey(), oreEntry.getValue());
				String previousOwner = ORE_OWNERS.get(oreEntry.getKey());
				if (previousOwner != null) {
					throw new JsonSyntaxException("ore '" + oreEntry.getKey() + "' is already owned by " + previousOwner);
				}
				JsonObject ore = oreEntry.getValue().getAsJsonObject().deepCopy();
				ore.addProperty("source_provider", fileProvider);
				if (!ore.has("enabled")) {
					ore.addProperty("enabled", true);
				}
				ores.put(oreEntry.getKey(), ore);
			}

			for (String oreId : ores.keySet()) {
				ORE_OWNERS.put(oreId, fileProvider);
			}
			PROVIDERS.put(fileProvider, new ProviderDefinition(fileProvider, revision, ores));
			LOGGER.info("Loaded Mineralogy ore-provider '{}' revision {} with {} ores",
					fileProvider, revision, ores.size());
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Rejected Mineralogy ore-provider file '{}'; {} native ore generation must remain enabled",
					path, fileProvider, e);
		}
	}

	private static void validateOre(String provider, String oreIdText, JsonElement element) {
		ResourceLocation oreId = new ResourceLocation(oreIdText);
		if (!provider.equals(oreId.getNamespace())) {
			throw new JsonSyntaxException("provider may only own ores in its own namespace: " + oreId);
		}
		Block ore = ForgeRegistries.BLOCKS.getValue(oreId);
		if (ore == null || ore == Blocks.AIR) {
			throw new JsonSyntaxException("unknown ore block: " + oreId);
		}
		if (!element.isJsonObject()) {
			throw new JsonSyntaxException("ore entry is not an object: " + oreId);
		}
		JsonObject dimensions = requiredObject(element.getAsJsonObject(), "dimensions");
		if (dimensions.entrySet().isEmpty()) {
			throw new JsonSyntaxException("ore has no dimensions: " + oreId);
		}
		for (Entry<String, JsonElement> dimensionEntry : dimensions.entrySet()) {
			new ResourceLocation(dimensionEntry.getKey());
			if (!dimensionEntry.getValue().isJsonObject()) {
				throw new JsonSyntaxException("dimension entry is not an object: " + dimensionEntry.getKey());
			}
			JsonObject dimension = dimensionEntry.getValue().getAsJsonObject();
			if (!bool(dimension, "enabled", true)) {
				continue;
			}
			int minY = integer(dimension, "min_y", Integer.MIN_VALUE);
			int maxY = integer(dimension, "max_y", Integer.MIN_VALUE);
			if (minY < -2048 || maxY > 2048 || minY > maxY) {
				throw new JsonSyntaxException("invalid Y range for " + oreId + " in " + dimensionEntry.getKey());
			}
			double frequency = decimal(dimension, "frequency", -1.0D);
			int quantity = integer(dimension, "quantity", -1);
			if (frequency < 0.0D || frequency > 64.0D || quantity < 1 || quantity > 64) {
				throw new JsonSyntaxException("invalid frequency or quantity for " + oreId);
			}
			boolean hasHosts = validBlockArray(dimension.get("host_blocks"))
					|| validIdArray(dimension.get("host_tags"));
			if ("minecraft:overworld".equals(dimensionEntry.getKey())) {
				hasHosts |= validFamilies(dimension.get("host_families"));
			}
			if (!hasHosts) {
				throw new JsonSyntaxException("enabled ore dimension has no valid hosts for " + oreId);
			}
		}
	}

	private static boolean validFamilies(JsonElement element) {
		if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() == 0) {
			return false;
		}
		for (JsonElement family : element.getAsJsonArray()) {
			RockFamily.fromConfigName(family.getAsString());
		}
		return true;
	}

	private static boolean validIdArray(JsonElement element) {
		if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() == 0) {
			return false;
		}
		for (JsonElement id : element.getAsJsonArray()) {
			new ResourceLocation(id.getAsString());
		}
		return true;
	}

	private static boolean validBlockArray(JsonElement element) {
		if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() == 0) {
			return false;
		}
		for (JsonElement value : element.getAsJsonArray()) {
			ResourceLocation id = new ResourceLocation(value.getAsString());
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block == null || block == Blocks.AIR) {
				throw new JsonSyntaxException("unknown host block: " + id);
			}
		}
		return true;
	}

	private static JsonObject requiredObject(JsonObject root, String key) {
		if (!root.has(key) || !root.get(key).isJsonObject()) {
			throw new JsonSyntaxException("missing object '" + key + "'");
		}
		return root.getAsJsonObject(key);
	}

	private static JsonObject object(JsonObject root, String key) {
		if (!root.has(key) || !root.get(key).isJsonObject()) {
			JsonObject value = new JsonObject();
			root.add(key, value);
			return value;
		}
		return root.getAsJsonObject(key);
	}

	private static Set<String> stringSet(JsonElement element) {
		Set<String> result = new HashSet<>();
		if (element != null && element.isJsonArray()) {
			for (JsonElement value : element.getAsJsonArray()) {
				result.add(value.getAsString());
			}
		}
		return result;
	}

	private static int integer(JsonObject root, String key, int fallback) {
		return root.has(key) ? root.get(key).getAsInt() : fallback;
	}

	private static double decimal(JsonObject root, String key, double fallback) {
		return root.has(key) ? root.get(key).getAsDouble() : fallback;
	}

	private static String string(JsonObject root, String key, String fallback) {
		return root.has(key) ? root.get(key).getAsString() : fallback;
	}

	private static boolean bool(JsonObject root, String key, boolean fallback) {
		return root.has(key) ? root.get(key).getAsBoolean() : fallback;
	}

	private static final class ProviderDefinition {
		final String modId;
		final int revision;
		final LinkedHashMap<String, JsonObject> ores;

		ProviderDefinition(String modId, int revision, LinkedHashMap<String, JsonObject> ores) {
			this.modId = modId;
			this.revision = revision;
			this.ores = ores;
		}
	}
}
