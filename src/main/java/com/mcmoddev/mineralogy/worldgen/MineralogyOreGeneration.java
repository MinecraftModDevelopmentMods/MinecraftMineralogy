package com.mcmoddev.mineralogy.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.mineralogy.Mineralogy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** One dynamic feature for every Mineralogy-managed ore and dimension. */
public final class MineralogyOreGeneration extends Feature<NoneFeatureConfiguration> {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final MineralogyOreGeneration FEATURE = new MineralogyOreGeneration();
	private static final BakedOre[] NO_ORES = new BakedOre[0];
	private static final Map<ResourceKey<Level>, BakedOre[]> EMPTY_DIMENSIONS = Collections.emptyMap();
	private static final Object CLASSIFIER_LOCK = new Object();

	private static Holder<PlacedFeature> placedFeature;
	private static volatile Map<ResourceKey<Level>, BakedOre[]> oresByDimension = EMPTY_DIMENSIONS;
	private static volatile BakedGeomeConfig geomeConfig;
	private static volatile GeomeGeology classifier;
	private static volatile long classifierSeed = Long.MIN_VALUE;
	private static final ThreadLocal<GenerationScratch> GENERATION_SCRATCH =
			ThreadLocal.withInitial(GenerationScratch::new);

	private MineralogyOreGeneration() {
		super(NoneFeatureConfiguration.CODEC);
		setRegistryName(Mineralogy.MODID, "managed_ores");
	}

	public static void registerConfiguredFeatures() {
		ResourceLocation id = new ResourceLocation(Mineralogy.MODID, "managed_ores");
		Holder<ConfiguredFeature<?, ?>> configured = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
				id, new ConfiguredFeature<NoneFeatureConfiguration, MineralogyOreGeneration>(FEATURE,
						NoneFeatureConfiguration.INSTANCE));
		placedFeature = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id,
				new PlacedFeature(configured, Collections.emptyList()));
		refreshWorldConfig();
	}

	public static void refreshWorldConfig() {
		geomeConfig = GeomeConfig.baked();
		oresByDimension = bakeOres(WorldGeologyProfileManager.activeProfile().rootCopy(), geomeConfig);
		synchronized (CLASSIFIER_LOCK) {
			classifier = null;
			classifierSeed = Long.MIN_VALUE;
		}
		GENERATION_SCRATCH.remove();
	}

	public static void onBiomeLoading(BiomeLoadingEvent event) {
		if (!WorldgenBenchmark.isVanillaBaseline()
				&& event.getCategory() != BiomeCategory.NONE && placedFeature != null) {
			event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES).add(placedFeature);
		}
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BakedOre[] ores = oresByDimension.getOrDefault(world.getLevel().dimension(), NO_ORES);
		if (ores.length == 0) {
			return false;
		}

		ChunkAccess chunk = world.getChunk(context.origin());
		ChunkPos chunkPos = chunk.getPos();
		int centerX = chunkPos.getMinBlockX() + 8;
		int centerZ = chunkPos.getMinBlockZ() + 8;
		GenerationScratch scratch = GENERATION_SCRATCH.get();
		int geome = -1;
		if (Level.OVERWORLD.equals(world.getLevel().dimension())) {
			scratch.cursor.set(centerX, Math.max(chunk.getMinBuildHeight(), 0), centerZ);
			Holder<Biome> biome = world.getBiome(scratch.cursor);
			geome = classifier(world.getSeed()).classifyColumn(biome.value(), centerX, centerZ,
					scratch.geomeValues(geomeConfig.geomeCount()));
		}

		Random random = context.random();
		boolean changed = false;
		for (BakedOre ore : ores) {
			double frequency = ore.frequency;
			if (geome >= 0) {
				frequency *= ore.geomeWeights[geome];
			}
			int attempts = attemptsForFrequency(random, frequency);
			for (int attempt = 0; attempt < attempts; attempt++) {
				changed |= placeAttempt(chunk, random, ore, geome, scratch.cursor);
			}
		}
		if (changed) {
			chunk.setUnsaved(true);
		}
		return changed;
	}

	private static boolean placeAttempt(ChunkAccess chunk, Random random, BakedOre ore, int geome,
			BlockPos.MutableBlockPos cursor) {
		int minY = Math.max(ore.minY, chunk.getMinBuildHeight());
		int maxY = Math.min(ore.maxY, chunk.getMaxBuildHeight() - 1);
		if (maxY < minY) {
			return false;
		}
		int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
		int y = minY + random.nextInt((maxY - minY) + 1);
		int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);
		return placeVein(chunk, random, ore, geome, x, y, z, cursor);
	}

	private static boolean placeVein(ChunkAccess chunk, Random random, BakedOre ore, int geome,
			int originX, int originY, int originZ, BlockPos.MutableBlockPos cursor) {
		float angle = random.nextFloat() * (float) Math.PI;
		double reach = ore.quantity / 8.0D;
		double startX = originX + Math.sin(angle) * reach;
		double endX = originX - Math.sin(angle) * reach;
		double startZ = originZ + Math.cos(angle) * reach;
		double endZ = originZ - Math.cos(angle) * reach;
		double startY = originY + random.nextInt(3) - 1;
		double endY = originY + random.nextInt(3) - 1;
		boolean changed = false;

		for (int step = 0; step < ore.quantity; step++) {
			double progress = ore.quantity == 1 ? 0.5D : step / (double) (ore.quantity - 1);
			double centerX = lerp(progress, startX, endX);
			double centerY = lerp(progress, startY, endY);
			double centerZ = lerp(progress, startZ, endZ);
			double scale = random.nextDouble() * ore.quantity / 16.0D;
			double diameter = (Math.sin(Math.PI * progress) + 1.0D) * scale + 1.0D;
			double radius = diameter / 2.0D;
			int minX = Math.max(chunk.getPos().getMinBlockX(), (int) Math.floor(centerX - radius));
			int maxX = Math.min(chunk.getPos().getMaxBlockX(), (int) Math.floor(centerX + radius));
			int minY = Math.max(chunk.getMinBuildHeight(), (int) Math.floor(centerY - radius));
			int maxY = Math.min(chunk.getMaxBuildHeight() - 1, (int) Math.floor(centerY + radius));
			int minZ = Math.max(chunk.getPos().getMinBlockZ(), (int) Math.floor(centerZ - radius));
			int maxZ = Math.min(chunk.getPos().getMaxBlockZ(), (int) Math.floor(centerZ + radius));
			double inverseRadius = radius <= 0.0D ? 1.0D : 1.0D / radius;

			for (int x = minX; x <= maxX; x++) {
				double dx = (x + 0.5D - centerX) * inverseRadius;
				double dx2 = dx * dx;
				if (dx2 >= 1.0D) {
					continue;
				}
				for (int y = minY; y <= maxY; y++) {
					double dy = (y + 0.5D - centerY) * inverseRadius;
					double dxy2 = dx2 + (dy * dy);
					if (dxy2 >= 1.0D) {
						continue;
					}
					for (int z = minZ; z <= maxZ; z++) {
						double dz = (z + 0.5D - centerZ) * inverseRadius;
						if (dxy2 + (dz * dz) >= 1.0D) {
							continue;
						}
						cursor.set(x, y, z);
						BlockState existing = chunk.getBlockState(cursor);
						if (ore.accepts(existing, geomeConfig)) {
							chunk.setBlockState(cursor, ore.output, false);
							changed = true;
						}
					}
				}
			}
		}
		return changed;
	}

	private static Map<ResourceKey<Level>, BakedOre[]> bakeOres(JsonObject profile, BakedGeomeConfig config) {
		if (!profile.has("ores") || !profile.get("ores").isJsonObject()) {
			return EMPTY_DIMENSIONS;
		}
		Map<TagKey<Block>, Set<Block>> resolvedTags = new HashMap<>();
		Map<ResourceKey<Level>, List<BakedOre>> grouped = new LinkedHashMap<>();
		for (Entry<String, JsonElement> oreEntry : profile.getAsJsonObject("ores").entrySet()) {
			if (!oreEntry.getValue().isJsonObject()) {
				continue;
			}
			JsonObject oreJson = oreEntry.getValue().getAsJsonObject();
			if (!bool(oreJson, "enabled", true)) {
				continue;
			}
			ResourceLocation oreId = resource(oreEntry.getKey());
			Block output = oreId == null ? null : ForgeRegistries.BLOCKS.getValue(oreId);
			if (output == null || output == Blocks.AIR || !oreJson.has("dimensions")
					|| !oreJson.get("dimensions").isJsonObject()) {
				LOGGER.warn("Ignoring invalid Mineralogy-managed ore '{}'", oreEntry.getKey());
				continue;
			}

			for (Entry<String, JsonElement> dimensionEntry : oreJson.getAsJsonObject("dimensions").entrySet()) {
				if (!dimensionEntry.getValue().isJsonObject()) {
					continue;
				}
				JsonObject dimension = dimensionEntry.getValue().getAsJsonObject();
				if (!bool(dimension, "enabled", true)) {
					continue;
				}
				ResourceLocation dimensionId = resource(dimensionEntry.getKey());
				if (dimensionId == null) {
					continue;
				}
				ResourceKey<Level> dimensionKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimensionId);
				BakedOre baked = bakeOre(output.defaultBlockState(), dimension, config, resolvedTags);
				if (baked != null) {
					grouped.computeIfAbsent(dimensionKey, ignored -> new ArrayList<>()).add(baked);
				}
			}
		}

		Map<ResourceKey<Level>, BakedOre[]> result = new HashMap<>();
		for (Entry<ResourceKey<Level>, List<BakedOre>> entry : grouped.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toArray(new BakedOre[entry.getValue().size()]));
		}
		LOGGER.info("Baked {} Mineralogy-managed ore definitions across {} dimensions",
				grouped.values().stream().mapToInt(List::size).sum(), result.size());
		return Collections.unmodifiableMap(result);
	}

	private static BakedOre bakeOre(BlockState output, JsonObject json, BakedGeomeConfig config,
			Map<TagKey<Block>, Set<Block>> resolvedTags) {
		int minY = integer(json, "min_y", -64);
		int maxY = integer(json, "max_y", 320);
		double frequency = decimal(json, "frequency", 0.0D);
		int quantity = integer(json, "quantity", 0);
		if (minY > maxY || frequency <= 0.0D || quantity <= 0) {
			return null;
		}

		Set<Block> hostBlocks = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
		addBlocks(hostBlocks, json.get("host_blocks"));
		addTags(hostBlocks, json.get("host_tags"), resolvedTags);
		int familyMask = 0;
		if (json.has("host_families") && json.get("host_families").isJsonArray()) {
			for (JsonElement familyElement : json.getAsJsonArray("host_families")) {
				try {
					familyMask |= 1 << RockFamily.fromConfigName(familyElement.getAsString()).ordinal();
				} catch (RuntimeException ignored) {
					// Validation reports bad provider data; pack overrides are skipped here.
				}
			}
		}
		if (hostBlocks.isEmpty() && familyMask == 0) {
			return null;
		}

		double[] geomeWeights = new double[config.geomeCount()];
		java.util.Arrays.fill(geomeWeights, 1.0D);
		if (json.has("geomes") && json.get("geomes").isJsonObject()) {
			for (Entry<String, JsonElement> entry : json.getAsJsonObject("geomes").entrySet()) {
				int index = config.geomeIndex(entry.getKey());
				if (index >= 0) {
					geomeWeights[index] = Math.max(0.0D, entry.getValue().getAsDouble());
				}
			}
		}
		return new BakedOre(output, minY, maxY, Math.min(64.0D, frequency), Math.min(64, quantity),
				hostBlocks, familyMask, geomeWeights);
	}

	private static void addBlocks(Set<Block> target, JsonElement element) {
		if (element == null || !element.isJsonArray()) {
			return;
		}
		for (JsonElement value : element.getAsJsonArray()) {
			ResourceLocation id = resource(value.getAsString());
			Block block = id == null ? null : ForgeRegistries.BLOCKS.getValue(id);
			if (block != null && block != Blocks.AIR) {
				target.add(block);
			}
		}
	}

	private static void addTags(Set<Block> target, JsonElement element,
			Map<TagKey<Block>, Set<Block>> resolvedTags) {
		if (element == null || !element.isJsonArray()) {
			return;
		}
		for (JsonElement value : element.getAsJsonArray()) {
			ResourceLocation id = resource(value.getAsString());
			if (id == null) {
				continue;
			}
			TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, id);
			Set<Block> blocks = resolvedTags.computeIfAbsent(tag, MineralogyOreGeneration::resolveTag);
			target.addAll(blocks);
		}
	}

	private static Set<Block> resolveTag(TagKey<Block> tag) {
		Set<Block> result = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (block.defaultBlockState().is(tag)) {
				result.add(block);
			}
		}
		return result;
	}

	private static GeomeGeology classifier(long seed) {
		GeomeGeology current = classifier;
		if (current == null || classifierSeed != seed) {
			synchronized (CLASSIFIER_LOCK) {
				if (classifier == null || classifierSeed != seed) {
					classifier = new GeomeGeology(seed, geomeConfig);
					classifierSeed = seed;
				}
				current = classifier;
			}
		}
		return current;
	}

	private static int attemptsForFrequency(Random random, double frequency) {
		int attempts = (int) frequency;
		if (random.nextDouble() < frequency - attempts) {
			attempts++;
		}
		return attempts;
	}

	private static double lerp(double value, double start, double end) {
		return start + value * (end - start);
	}

	private static ResourceLocation resource(String value) {
		try {
			return new ResourceLocation(value);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static boolean bool(JsonObject json, String key, boolean fallback) {
		try {
			return json.has(key) ? json.get(key).getAsBoolean() : fallback;
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static int integer(JsonObject json, String key, int fallback) {
		try {
			return json.has(key) ? json.get(key).getAsInt() : fallback;
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static double decimal(JsonObject json, String key, double fallback) {
		try {
			return json.has(key) ? json.get(key).getAsDouble() : fallback;
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static final class BakedOre {
		final BlockState output;
		final int minY;
		final int maxY;
		final double frequency;
		final int quantity;
		final Set<Block> hostBlocks;
		final int familyMask;
		final double[] geomeWeights;

		BakedOre(BlockState output, int minY, int maxY, double frequency, int quantity,
				Set<Block> hostBlocks, int familyMask, double[] geomeWeights) {
			this.output = output;
			this.minY = minY;
			this.maxY = maxY;
			this.frequency = frequency;
			this.quantity = quantity;
			this.hostBlocks = hostBlocks;
			this.familyMask = familyMask;
			this.geomeWeights = geomeWeights;
		}

		boolean accepts(BlockState state, BakedGeomeConfig config) {
			if (hostBlocks.contains(state.getBlock())) {
				return true;
			}
			RockFamily family = config.familyOf(state);
			return family != null && config.isOreReplaceable(state)
					&& (familyMask & (1 << family.ordinal())) != 0;
		}
	}

	private static final class GenerationScratch {
		final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		private double[] geomeValues = new double[0];

		double[] geomeValues(int count) {
			if (geomeValues.length != count) {
				geomeValues = new double[count];
			}
			return geomeValues;
		}
	}
}
