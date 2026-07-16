package com.mcmoddev.mineralogy.worldgen;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;
import com.mcmoddev.mineralogy.MineralogyConfig.OilGenerationSettings;
import com.mcmoddev.mineralogy.init.MineralogyFluids;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public final class OilDepositFeature extends Feature<NoneFeatureConfiguration> {
	public static final OilDepositFeature FEATURE = new OilDepositFeature();

	private static final int CHUNK_WIDTH = 16;

	private static Holder<PlacedFeature> placedFeature;
	private static Set<net.minecraft.world.level.block.Block> sedimentaryTargets = Collections.emptySet();
	private static BakedGeomeConfig geomeConfig;
	private static boolean useGeomeTaxonomy;
	private static boolean placeCrudeOil;
	private static BlockState oilState;

	private OilDepositFeature() {
		super(NoneFeatureConfiguration.CODEC);
		setRegistryName(Mineralogy.MODID, "crude_oil_deposit");
	}

	public static void registerConfiguredFeature() {
		ResourceLocation id = new ResourceLocation(Mineralogy.MODID, "crude_oil_deposit");
		Holder<ConfiguredFeature<?, ?>> configured = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
				id, new ConfiguredFeature<NoneFeatureConfiguration, OilDepositFeature>(FEATURE,
						NoneFeatureConfiguration.INSTANCE));
		placedFeature = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id,
				new PlacedFeature(configured, Collections.emptyList()));
		refreshWorldConfig();
		oilState = MineralogyFluids.crudeOilBlock().defaultBlockState().setValue(LiquidBlock.LEVEL, 0);
	}

	public static void refreshWorldConfig() {
		geomeConfig = GeomeConfig.baked();
		useGeomeTaxonomy = WorldGeologyProfileManager.geologyMode() == GeologyMode.GEOME;
		placeCrudeOil = WorldGeologyProfileManager.placeCrudeOil();
		sedimentaryTargets = bakeSedimentaryTargets();
	}

	public static void onBiomeLoading(BiomeLoadingEvent event) {
		if (event.getCategory() == BiomeCategory.OCEAN && placedFeature != null) {
			event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES).add(placedFeature);
		}
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		if (!placeCrudeOil || world.getLevel().dimension() != Level.OVERWORLD) {
			return false;
		}

		OilGenerationSettings settings = MineralogyConfig.crudeOil();
		if (settings.frequency() <= 0.0D || settings.maxY() < settings.minY()) {
			return false;
		}

		int attempts = attemptsForFrequency(context.random(), settings.frequency());
		if (attempts <= 0) {
			return false;
		}

		ChunkAccess chunk = world.getChunk(context.origin());
		boolean changed = false;
		for (int i = 0; i < attempts; i++) {
			changed |= placeDeposit(chunk, context.random(), settings);
		}

		if (changed) {
			chunk.setUnsaved(true);
		}
		return changed;
	}

	private static boolean placeDeposit(ChunkAccess chunk, Random random, OilGenerationSettings settings) {
		int radius = randomBetween(random, settings.minRadius(), settings.maxRadius());
		int verticalRadius = randomBetween(random, settings.minVerticalRadius(), settings.maxVerticalRadius());
		int dx = random.nextInt(CHUNK_WIDTH);
		int dz = random.nextInt(CHUNK_WIDTH);
		int oceanFloor = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);
		int maxCenterY = Math.min(settings.maxY(), oceanFloor - 1 - settings.minSolidCover() - verticalRadius);
		int minCenterY = Math.max(settings.minY(), chunk.getMinBuildHeight() + verticalRadius);
		if (maxCenterY < minCenterY) {
			return false;
		}

		int centerX = chunk.getPos().getMinBlockX() + dx;
		int centerY = randomBetween(random, minCenterY, maxCenterY);
		int centerZ = chunk.getPos().getMinBlockZ() + dz;
		int lobes = randomBetween(random, 1, settings.maxLobes());
		boolean changed = false;

		for (int lobe = 0; lobe < lobes; lobe++) {
			int lobeX = centerX;
			int lobeY = centerY;
			int lobeZ = centerZ;
			if (lobe > 0) {
				lobeX += randomBetween(random, -radius / 2, radius / 2);
				lobeY += randomBetween(random, -verticalRadius, verticalRadius);
				lobeZ += randomBetween(random, -radius / 2, radius / 2);
			}

			int lobeRadius = Math.max(2, radius - random.nextInt(Math.max(1, (radius / 3) + 1)));
			int lobeVerticalRadius = Math.max(1,
					verticalRadius - random.nextInt(Math.max(1, (verticalRadius / 2) + 1)));
			changed |= placeLobe(chunk, lobeX, lobeY, lobeZ, lobeRadius, lobeVerticalRadius,
					settings.minSolidCover());
		}

		return changed;
	}

	private static boolean placeLobe(ChunkAccess chunk, int centerX, int centerY, int centerZ,
			int radius, int verticalRadius, int minSolidCover) {
		int chunkMinX = chunk.getPos().getMinBlockX();
		int chunkMinZ = chunk.getPos().getMinBlockZ();
		int minX = Math.max(chunkMinX, centerX - radius);
		int maxX = Math.min(chunkMinX + CHUNK_WIDTH - 1, centerX + radius);
		int minY = Math.max(chunk.getMinBuildHeight(), centerY - verticalRadius);
		int maxY = Math.min(chunk.getMaxBuildHeight() - 1, centerY + verticalRadius);
		int minZ = Math.max(chunkMinZ, centerZ - radius);
		int maxZ = Math.min(chunkMinZ + CHUNK_WIDTH - 1, centerZ + radius);
		double inverseRadiusSquared = 1.0D / (radius * radius);
		double inverseVerticalRadiusSquared = 1.0D / (verticalRadius * verticalRadius);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		boolean changed = false;

		for (int x = minX; x <= maxX; x++) {
			int localX = x - chunkMinX;
			double xDistance = x - centerX;
			for (int z = minZ; z <= maxZ; z++) {
				int localZ = z - chunkMinZ;
				double zDistance = z - centerZ;
				double horizontalShape = ((xDistance * xDistance) + (zDistance * zDistance))
						* inverseRadiusSquared;
				if (horizontalShape > 1.0D) {
					continue;
				}

				int oceanFloor = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
				int topLimit = oceanFloor - 1 - minSolidCover;
				for (int y = minY; y <= maxY && y <= topLimit; y++) {
					double yDistance = y - centerY;
					if (horizontalShape + ((yDistance * yDistance) * inverseVerticalRadiusSquared) > 1.0D) {
						continue;
					}

					cursor.set(x, y, z);
					BlockState existing = chunk.getBlockState(cursor);
					if (isSedimentaryRock(existing) && hasSolidCover(chunk, cursor, x, y, z, minSolidCover)) {
						cursor.set(x, y, z);
						chunk.setBlockState(cursor, oilState, false);
						changed = true;
					}
				}
			}
		}

		return changed;
	}

	private static boolean hasSolidCover(ChunkAccess chunk, BlockPos.MutableBlockPos cursor, int x, int y, int z,
			int minSolidCover) {
		for (int offset = 1; offset <= minSolidCover; offset++) {
			cursor.set(x, y + offset, z);
			BlockState cover = chunk.getBlockState(cursor);
			if (!cover.getMaterial().blocksMotion() || !cover.getFluidState().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSedimentaryRock(BlockState state) {
		return useGeomeTaxonomy && geomeConfig != null
				? geomeConfig.isSedimentaryRock(state)
				: sedimentaryTargets.contains(state.getBlock());
	}

	private static Set<net.minecraft.world.level.block.Block> bakeSedimentaryTargets() {
		Set<net.minecraft.world.level.block.Block> targets = new HashSet<net.minecraft.world.level.block.Block>();
		for (net.minecraft.world.level.block.Block block : MineralogyRegistry.sedimentaryStones) {
			targets.add(GeologyBlockAliases.aliasState(block.defaultBlockState()).getBlock());
		}
		return targets;
	}

	private static int attemptsForFrequency(Random random, double frequency) {
		int attempts = (int) frequency;
		if (random.nextDouble() < frequency - attempts) {
			attempts++;
		}
		return attempts;
	}

	private static int randomBetween(Random random, int min, int max) {
		if (max <= min) {
			return min;
		}
		return min + random.nextInt((max - min) + 1);
	}
}
