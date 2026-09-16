package zone.moddev.mc.mineralogy.patching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Dynamic;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.migration.LegacyMineralogy6ConfigMigrator;
import zone.moddev.mc.mineralogy.mixin.BlockStateDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Converts pre-flattening Mineralogy block IDs before vanilla chunk datafixing. */
public final class LegacyWorldDataHook {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<Identifier, Identifier> BLOCK_ALIASES = new HashMap<>();
	private static final BitSet LEGACY_MINERALOGY_BLOCK_IDS = new BitSet();
	private static final BitSet LEGACY_ROCK_FURNACE_BLOCK_IDS = new BitSet();
	private static final Set<Long> LEGACY_MINERALOGY_CHUNKS = ConcurrentHashMap.newKeySet();
	private static final String PRESERVE_CHUNK_MARKER = "MineralogyLegacyPreserveChunk";
	private static final String ROCK_FURNACE_TILE_ENTITY = "mineralogy:rock_furnace";
	private static final String SIDECAR_NAME = "mineralogy_legacy_registry.dat";
	private static volatile boolean legacyWorldActive;

	static {
		BLOCK_ALIASES.put(Identifier.fromNamespaceAndPath(Mineralogy.MODID, "pummice"),
				Identifier.fromNamespaceAndPath(Mineralogy.MODID, "pumice"));
		BLOCK_ALIASES.put(Identifier.fromNamespaceAndPath(Mineralogy.MODID, "saprolite"),
				Identifier.fromNamespaceAndPath(Mineralogy.MODID, "limestone"));
	}

	private LegacyWorldDataHook() {
	}

	/** Called from NeoForge's additional-level-data reader before legacy FML data is discarded. */
	public static void captureLegacyLevelData(CompoundTag root,
			LevelStorageSource.LevelDirectory levelDirectory) {
		if (root == null || levelDirectory == null) {
			return;
		}
		Path levelPath = levelDirectory.path();
		if (root.contains("FML")) {
			prepareLegacyWorld(levelPath.toFile(), root.getCompoundOrEmpty("FML"));
		} else if (root.contains("fml")) {
			prepareLegacyWorld(levelPath.toFile(), root.getCompoundOrEmpty("fml"));
		} else {
			prepareLegacyWorld(levelPath.resolve("level.dat").toFile());
		}
	}

	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		LegacyMineralogy6ConfigMigrator.migrateWorldProfile(
				event.getServer().getWorldPath(LevelResource.ROOT), LOGGER);
		File levelDat = event.getServer().getWorldPath(LevelResource.LEVEL_DATA_FILE).toFile();
		prepareLegacyWorld(levelDat);
	}

	private static synchronized void prepareLegacyWorld(File levelDat) {
		legacyWorldActive = false;
		LEGACY_MINERALOGY_CHUNKS.clear();
		if (!levelDat.isFile()) {
			return;
		}

		try (FileInputStream input = new FileInputStream(levelDat)) {
			CompoundTag root = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
			if (root.contains("FML")) {
				CompoundTag fml = root.getCompoundOrEmpty("FML");
				CompoundTag registries = fml.getCompoundOrEmpty("Registries");
				if (registries.contains("minecraft:blocks")) {
					CompoundTag blocks = registries.getCompoundOrEmpty("minecraft:blocks");
					install(levelDat.getParentFile(), blocks);
					writeSidecar(levelDat.getParentFile(), blocks);
					return;
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Could not inspect '{}' for legacy Mineralogy registry data", levelDat, e);
			return;
		}

		File sidecar = sidecar(levelDat.getParentFile());
		if (sidecar.isFile()) {
			try (FileInputStream input = new FileInputStream(sidecar)) {
				install(levelDat.getParentFile(), NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("Blocks"));
			} catch (IOException e) {
				LOGGER.warn("Could not read legacy Mineralogy registry sidecar '{}'", sidecar, e);
			}
		}
	}

	private static synchronized void prepareLegacyWorld(File worldDirectory, CompoundTag fmlData) {
		legacyWorldActive = false;
		LEGACY_MINERALOGY_CHUNKS.clear();
		if (fmlData.contains("Registries")) {
			CompoundTag registries = fmlData.getCompoundOrEmpty("Registries");
			if (registries.contains("minecraft:blocks")) {
				CompoundTag blocks = registries.getCompoundOrEmpty("minecraft:blocks");
				install(worldDirectory, blocks);
				writeSidecar(worldDirectory, blocks);
				return;
			}
		}

		File sidecar = sidecar(worldDirectory);
		if (sidecar.isFile()) {
			try (FileInputStream input = new FileInputStream(sidecar)) {
				install(worldDirectory, NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("Blocks"));
			} catch (IOException e) {
				LOGGER.warn("Could not read legacy Mineralogy registry sidecar '{}'", sidecar, e);
			}
		}
	}

	private static void install(File worldDirectory, CompoundTag blockSnapshot) {
		int mappedStates = installLegacyBlockStates(blockSnapshot);
		legacyWorldActive = mappedStates > 0;
		if (legacyWorldActive) {
			int protectedChunks = indexLegacyChunks(worldDirectory);
			LOGGER.info("Prepared {} legacy Mineralogy block states and protected {} existing Overworld chunks from '{}'",
					mappedStates, protectedChunks, worldDirectory);
		}
	}

	/** Reads only Anvil location tables so old chunks are protected before their NBT is loaded. */
	private static int indexLegacyChunks(File worldDirectory) {
		indexLegacyRegionDirectory(new File(worldDirectory, "region"));
		indexLegacyRegionDirectory(new File(worldDirectory,
				"dimensions/minecraft/overworld/region"));
		return LEGACY_MINERALOGY_CHUNKS.size();
	}

	private static void indexLegacyRegionDirectory(File regionDirectory) {
		File[] regionFiles = regionDirectory.listFiles((directory, name) ->
				(name.endsWith(".mca") || name.endsWith(".mcr")) && name.startsWith("r."));
		if (regionFiles == null) {
			return;
		}

		byte[] locations = new byte[4096];
		for (File regionFile : regionFiles) {
			String[] nameParts = regionFile.getName().split("\\.");
			if (nameParts.length != 4) {
				continue;
			}
			final int regionX;
			final int regionZ;
			try {
				regionX = Integer.parseInt(nameParts[1]);
				regionZ = Integer.parseInt(nameParts[2]);
			} catch (NumberFormatException e) {
				continue;
			}

			try (InputStream input = Files.newInputStream(regionFile.toPath())) {
				int read = 0;
				while (read < locations.length) {
					int count = input.read(locations, read, locations.length - read);
					if (count < 0) {
						break;
					}
					read += count;
				}
				for (int index = 0; index < read / 4; ++index) {
					int offset = index * 4;
					if ((locations[offset] | locations[offset + 1] | locations[offset + 2]
							| locations[offset + 3]) != 0) {
						int chunkX = regionX * 32 + (index & 31);
						int chunkZ = regionZ * 32 + (index >> 5);
						LEGACY_MINERALOGY_CHUNKS.add(chunkKey(chunkX, chunkZ));
					}
				}
			} catch (IOException e) {
				LOGGER.warn("Could not inspect legacy chunk locations in '{}'", regionFile, e);
			}
		}
	}

	private static void writeSidecar(File worldDirectory, CompoundTag blockSnapshot) {
		File sidecar = sidecar(worldDirectory);
		if (sidecar.isFile()) {
			return;
		}
		File parent = sidecar.getParentFile();
		File temporary = new File(parent, SIDECAR_NAME + ".tmp");
		try {
			Files.createDirectories(parent.toPath());
			CompoundTag root = new CompoundTag();
			root.put("Blocks", blockSnapshot.copy());
			try (FileOutputStream output = new FileOutputStream(temporary)) {
				NbtIo.writeCompressed(root, output);
			}
			try {
				Files.move(temporary.toPath(), sidecar.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary.toPath(), sidecar.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not preserve legacy Mineralogy registry data in '{}'", sidecar, e);
		}
	}

	private static File sidecar(File worldDirectory) {
		return new File(new File(worldDirectory, "data"), SIDECAR_NAME);
	}

	/** Called by the chunk-storage mixin immediately before vanilla datafixing. */
	public static void prepareLegacyChunk(CompoundTag root) {
		if (!legacyWorldActive || root == null || !root.contains("Level")) {
			return;
		}

		CompoundTag level = root.getCompoundOrEmpty("Level");
		if (!containsLegacyMineralogyBlock(level)) {
			return;
		}
		LEGACY_MINERALOGY_CHUNKS.add(chunkKey(level.getIntOr("xPos", 0), level.getIntOr("zPos", 0)));
		rewriteLegacyRockFurnaceTileEntities(level);
		level.putBoolean("TerrainPopulated", true);
		level.putBoolean("LightPopulated", true);
		level.putBoolean(PRESERVE_CHUNK_MARKER, true);
	}

	/** Called by the chunk-storage mixin after vanilla datafixing. */
	public static CompoundTag finalizeLegacyChunk(CompoundTag root) {
		if (root == null) {
			return root;
		}
		CompoundTag level = root.contains("Level") ? root.getCompoundOrEmpty("Level") : root;
		boolean preserve = level.getBooleanOr(PRESERVE_CHUNK_MARKER, false)
				|| LEGACY_MINERALOGY_CHUNKS.contains(chunkKey(level.getIntOr("xPos", 0), level.getIntOr("zPos", 0)));
		if (preserve) {
			level.putString("Status", "full");
			level.remove(PRESERVE_CHUNK_MARKER);
		}
		return root;
	}

	private static int installLegacyBlockStates(CompoundTag blockSnapshot) {
		LEGACY_MINERALOGY_BLOCK_IDS.clear();
		LEGACY_ROCK_FURNACE_BLOCK_IDS.clear();
		Map<Identifier, Integer> mineralogyIds = new HashMap<>();
		ListTag savedIds = blockSnapshot.getListOrEmpty("ids");
		int highestStateId = 0;
		for (int index = 0; index < savedIds.size(); ++index) {
			CompoundTag savedId = savedIds.getCompoundOrEmpty(index);
			String key = savedId.getStringOr("K", "");
			if (!key.startsWith(Mineralogy.MODID + ":")) {
				continue;
			}
			Identifier id = Identifier.parse(key);
			int numericId = savedId.getIntOr("V", -1);
			if (numericId < 0) {
				continue;
			}
			mineralogyIds.put(id, numericId);
			highestStateId = Math.max(highestStateId, (numericId << 4) | 15);
		}
		Dynamic<?>[] legacyStates = expandFlatteningTable(highestStateId + 1);
		int mapped = 0;
		for (Map.Entry<Identifier, Integer> entry : mineralogyIds.entrySet()) {
			Identifier oldId = entry.getKey();
			LEGACY_MINERALOGY_BLOCK_IDS.set(entry.getValue());
			Block block = resolveCurrentBlock(oldId);
			if (block instanceof RockFurnace) {
				LEGACY_ROCK_FURNACE_BLOCK_IDS.set(entry.getValue());
			}
			for (int meta = 0; meta < 16; ++meta) {
				int stateId = (entry.getValue() << 4) | meta;
				// The private vanilla register method may retain a JIT-compiled reference to
				// its original final 4,096-entry array. Write the expanded array directly;
				// Mineralogy has no legacy aliases that need its auxiliary name maps.
				legacyStates[stateId] = new Dynamic<>(NbtOps.INSTANCE,
						writeLegacyBlockState(legacyState(block, meta)));
				++mapped;
			}
		}
		return mapped;
	}

	/**
	 * Serializes the input expected by the pre-flattening {@code BlockStateData}
	 * data fixer. Minecraft 26.3's normal block-state writer emits the modern
	 * lowercase {@code id}/{@code properties} schema, but this table is itself
	 * the source for that conversion and must retain legacy
	 * {@code Name}/{@code Properties} fields.
	 */
	static CompoundTag writeLegacyBlockState(BlockState state) {
		Map<String, String> values = new LinkedHashMap<>();
		state.getValues().forEach(value ->
				values.put(value.property().getName(), value.valueName()));
		return writeLegacyBlockState(
				BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), values);
	}

	static CompoundTag writeLegacyBlockState(String blockName, Map<String, String> values) {
		CompoundTag result = new CompoundTag();
		result.putString("Name", blockName);
		if (!values.isEmpty()) {
			CompoundTag properties = new CompoundTag();
			values.forEach(properties::putString);
			result.put("Properties", properties);
		}
		return result;
	}

	/** Returns the lookup table expanded by the required BlockStateData mixin. */
	private static Dynamic<?>[] expandFlatteningTable(int requiredLength) {
		Dynamic<?>[] states = BlockStateDataAccessor.mineralogy$getLegacyStateMap();
		if (states.length < requiredLength) {
			throw new IllegalStateException("Legacy block-state table has length " + states.length
					+ " but Mineralogy needs at least " + requiredLength);
		}
		return states;
	}

	private static boolean containsLegacyMineralogyBlock(CompoundTag level) {
		ListTag sections = level.getListOrEmpty("Sections");
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			CompoundTag section = sections.getCompoundOrEmpty(sectionIndex);
			byte[] blocks = section.getByteArray("Blocks").orElseGet(() -> new byte[0]);
			if (blocks.length != 4096) {
				continue;
			}
			byte[] add = section.getByteArray("Add").orElseGet(() -> new byte[0]);
			for (int blockIndex = 0; blockIndex < blocks.length; ++blockIndex) {
				if (LEGACY_MINERALOGY_BLOCK_IDS.get(blockId(blocks, add, blockIndex))) {
					return true;
				}
			}
		}
		return false;
	}

	private static void rewriteLegacyRockFurnaceTileEntities(CompoundTag level) {
		ListTag tileEntities = level.getListOrEmpty("TileEntities");
		for (int index = 0; index < tileEntities.size(); ++index) {
			CompoundTag tileEntity = tileEntities.getCompoundOrEmpty(index);
			int blockId = getLegacyBlockId(level, tileEntity.getIntOr("x", 0), tileEntity.getIntOr("y", -1),
					tileEntity.getIntOr("z", 0));
			if (LEGACY_ROCK_FURNACE_BLOCK_IDS.get(blockId)) {
				tileEntity.putString("id", ROCK_FURNACE_TILE_ENTITY);
			}
		}
	}

	private static int getLegacyBlockId(CompoundTag level, int x, int y, int z) {
		if (y < 0 || y > 255) {
			return -1;
		}
		ListTag sections = level.getListOrEmpty("Sections");
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			CompoundTag section = sections.getCompoundOrEmpty(sectionIndex);
			if ((section.getByteOr("Y", (byte) -1) & 0xFF) != y >> 4) {
				continue;
			}
			byte[] blocks = section.getByteArray("Blocks").orElseGet(() -> new byte[0]);
			if (blocks.length != 4096) {
				return -1;
			}
			int index = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
			return blockId(blocks, section.getByteArray("Add").orElseGet(() -> new byte[0]), index);
		}
		return -1;
	}

	private static int blockId(byte[] blocks, byte[] add, int index) {
		int highBits = add.length == 2048 ? (add[index >> 1] >> ((index & 1) * 4)) & 0x0F : 0;
		return (blocks[index] & 0xFF) | (highBits << 8);
	}

	public static boolean isLegacyMineralogyChunk(int chunkX, int chunkZ) {
		return legacyWorldActive && LEGACY_MINERALOGY_CHUNKS.contains(chunkKey(chunkX, chunkZ));
	}

	/** Prevents neighboring new-chunk features from rewriting already populated legacy chunks. */
	public static boolean shouldBlockWorldgenWrite(BlockPos position) {
		return legacyWorldActive && position != null
				&& LEGACY_MINERALOGY_CHUNKS.contains(chunkKey(position.getX() >> 4, position.getZ() >> 4));
	}

	private static long chunkKey(int chunkX, int chunkZ) {
		return ((long) chunkX & 0xFFFFFFFFL) << 32 | ((long) chunkZ & 0xFFFFFFFFL);
	}

	private static Block resolveCurrentBlock(Identifier oldId) {
		Identifier target = BLOCK_ALIASES.getOrDefault(oldId, oldId);
		if (!BuiltInRegistries.BLOCK.containsKey(target)) {
			throw new IllegalStateException("Legacy Mineralogy block has no current replacement: " + oldId);
		}
		return BuiltInRegistries.BLOCK.getValue(target);
	}

	private static BlockState legacyState(Block block, int meta) {
		BlockState state = block.defaultBlockState();
		if (block instanceof RockSlab) {
			return state.setValue(RockSlab.FACING, Direction.from3DDataValue(meta));
		}
		if (block instanceof RockSaltLamp) {
			return state.setValue(RockSaltLamp.FACING, legacyLampFacing(meta));
		}
		if (block instanceof RockSaltStreetLamp) {
			return state.setValue(RockSaltStreetLamp.FACING, Direction.UP);
		}
		if (block instanceof RockFurnace) {
			Direction facing = Direction.from3DDataValue(meta);
			return state.setValue(RockFurnace.FACING,
					facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : facing);
		}
		if (block instanceof StairBlock) {
			Direction facing = Direction.from3DDataValue(5 - (meta & 3));
			return state.setValue(StairBlock.FACING, facing)
					.setValue(StairBlock.HALF, (meta & 4) == 0 ? Half.BOTTOM : Half.TOP)
					.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT)
					.setValue(StairBlock.WATERLOGGED, Boolean.FALSE);
		}
		return state;
	}

	private static Direction legacyLampFacing(int meta) {
		switch (meta) {
			case 1: return Direction.EAST;
			case 2: return Direction.WEST;
			case 3: return Direction.SOUTH;
			case 4: return Direction.NORTH;
			case 5: return Direction.DOWN;
			default: return Direction.UP;
		}
	}

}
