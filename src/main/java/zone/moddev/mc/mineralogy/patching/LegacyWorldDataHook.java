package zone.moddev.mc.mineralogy.patching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Dynamic;

import sun.misc.Unsafe;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.migration.LegacyMineralogy6ConfigMigrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Converts pre-flattening Mineralogy block IDs before vanilla chunk datafixing. */
public final class LegacyWorldDataHook {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<ResourceLocation, ResourceLocation> BLOCK_ALIASES = new HashMap<>();
	private static final BitSet LEGACY_MINERALOGY_BLOCK_IDS = new BitSet();
	private static final BitSet LEGACY_ROCK_FURNACE_BLOCK_IDS = new BitSet();
	private static final Set<Long> LEGACY_MINERALOGY_CHUNKS = ConcurrentHashMap.newKeySet();
	private static final String PRESERVE_CHUNK_MARKER = "MineralogyLegacyPreserveChunk";
	private static final String ROCK_FURNACE_TILE_ENTITY = "mineralogy:rock_furnace";
	private static final String SIDECAR_NAME = "mineralogy_legacy_registry.dat";
	private static volatile boolean legacyWorldActive;

	static {
		BLOCK_ALIASES.put(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "pummice"),
				ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "pumice"));
		BLOCK_ALIASES.put(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "saprolite"),
				ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "limestone"));
	}

	private LegacyWorldDataHook() {
	}

	/** Called from Forge's raw additional-level-data reader before legacy FML data is discarded. */
	public static void captureLegacyLevelData(LevelStorageSource.LevelStorageAccess access,
			LevelStorageSource.LevelDirectory levelDirectory) {
		if (access == null || levelDirectory == null) {
			return;
		}
		CompoundTag root;
		try {
			root = access.getDataTagRaw(false);
		} catch (IOException primaryFailure) {
			try {
				root = access.getDataTagRaw(true);
			} catch (IOException fallbackFailure) {
				LOGGER.warn("Could not inspect primary or fallback level data in '{}' for legacy Mineralogy mappings",
						levelDirectory.path(), fallbackFailure);
				return;
			}
		}
		Path levelPath = levelDirectory.path();
		if (root.contains("FML", 10)) {
			prepareLegacyWorld(levelPath.toFile(), root.getCompound("FML"));
		} else if (root.contains("fml", 10)) {
			prepareLegacyWorld(levelPath.toFile(), root.getCompound("fml"));
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
			if (root.contains("FML", 10)) {
				CompoundTag fml = root.getCompound("FML");
				CompoundTag registries = fml.getCompound("Registries");
				if (registries.contains("minecraft:blocks", 10)) {
					CompoundTag blocks = registries.getCompound("minecraft:blocks");
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
				install(levelDat.getParentFile(), NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap()).getCompound("Blocks"));
			} catch (IOException e) {
				LOGGER.warn("Could not read legacy Mineralogy registry sidecar '{}'", sidecar, e);
			}
		}
	}

	private static synchronized void prepareLegacyWorld(File worldDirectory, CompoundTag fmlData) {
		legacyWorldActive = false;
		LEGACY_MINERALOGY_CHUNKS.clear();
		if (fmlData.contains("Registries", 10)) {
			CompoundTag registries = fmlData.getCompound("Registries");
			if (registries.contains("minecraft:blocks", 10)) {
				CompoundTag blocks = registries.getCompound("minecraft:blocks");
				install(worldDirectory, blocks);
				writeSidecar(worldDirectory, blocks);
				return;
			}
		}

		File sidecar = sidecar(worldDirectory);
		if (sidecar.isFile()) {
			try (FileInputStream input = new FileInputStream(sidecar)) {
				install(worldDirectory, NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap()).getCompound("Blocks"));
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
		File regionDirectory = new File(worldDirectory, "region");
		File[] regionFiles = regionDirectory.listFiles((directory, name) ->
				(name.endsWith(".mca") || name.endsWith(".mcr")) && name.startsWith("r."));
		if (regionFiles == null) {
			return 0;
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
		return LEGACY_MINERALOGY_CHUNKS.size();
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

	/** Called by the chunk-loader coremod immediately before vanilla datafixing. */
	public static void prepareLegacyChunk(CompoundTag root) {
		if (!legacyWorldActive || root == null || !root.contains("Level", 10)) {
			return;
		}

		CompoundTag level = root.getCompound("Level");
		if (!containsLegacyMineralogyBlock(level)) {
			return;
		}
		LEGACY_MINERALOGY_CHUNKS.add(chunkKey(level.getInt("xPos"), level.getInt("zPos")));
		rewriteLegacyRockFurnaceTileEntities(level);
		level.putBoolean("TerrainPopulated", true);
		level.putBoolean("LightPopulated", true);
		level.putBoolean(PRESERVE_CHUNK_MARKER, true);
	}

	/** Called by the chunk-loader coremod after vanilla datafixing. */
	public static CompoundTag finalizeLegacyChunk(CompoundTag root) {
		if (root == null) {
			return root;
		}
		CompoundTag level = root.contains("Level", 10) ? root.getCompound("Level") : root;
		boolean preserve = level.getBoolean(PRESERVE_CHUNK_MARKER)
				|| LEGACY_MINERALOGY_CHUNKS.contains(chunkKey(level.getInt("xPos"), level.getInt("zPos")));
		if (preserve) {
			level.putString("Status", "full");
			level.remove(PRESERVE_CHUNK_MARKER);
		}
		return root;
	}

	private static int installLegacyBlockStates(CompoundTag blockSnapshot) {
		LEGACY_MINERALOGY_BLOCK_IDS.clear();
		LEGACY_ROCK_FURNACE_BLOCK_IDS.clear();
		Map<ResourceLocation, Integer> mineralogyIds = new HashMap<>();
		ListTag savedIds = blockSnapshot.getList("ids", 10);
		int highestStateId = 0;
		for (int index = 0; index < savedIds.size(); ++index) {
			CompoundTag savedId = savedIds.getCompound(index);
			String key = savedId.getString("K");
			if (!key.startsWith(Mineralogy.MODID + ":")) {
				continue;
			}
			ResourceLocation id = ResourceLocation.parse(key);
			int numericId = savedId.getInt("V");
			mineralogyIds.put(id, numericId);
			highestStateId = Math.max(highestStateId, (numericId << 4) | 15);
		}
		Dynamic<?>[] legacyStates = expandFlatteningTable(highestStateId + 1);
		int mapped = 0;
		for (Map.Entry<ResourceLocation, Integer> entry : mineralogyIds.entrySet()) {
			ResourceLocation oldId = entry.getKey();
			LEGACY_MINERALOGY_BLOCK_IDS.set(entry.getValue());
			Block block = resolveCurrentBlock(oldId);
			if (block instanceof RockFurnace) {
				LEGACY_ROCK_FURNACE_BLOCK_IDS.set(entry.getValue());
			}
			for (int meta = 0; meta < 16; ++meta) {
				String stateNbt = NbtUtils.writeBlockState(legacyState(block, meta)).toString();
				int stateId = (entry.getValue() << 4) | meta;
				// The private vanilla register method may retain a JIT-compiled reference to
				// its original final 4,096-entry array. Write the expanded array directly;
				// Mineralogy has no legacy aliases that need its auxiliary name maps.
				legacyStates[stateId] = BlockStateData.parse(stateNbt);
				++mapped;
			}
		}
		return mapped;
	}

	/**
	 * Minecraft 1.21.1 still fixes the pre-flattening state table at 4,096 entries,
	 * while Forge 1.12 worlds commonly assign mod blocks higher numeric IDs.
	 * Replace that exact static-final array before writing any recovered states.
	 */
	private static Dynamic<?>[] expandFlatteningTable(int requiredLength) {
		try {
			Unsafe unsafe = unsafe();
			for (Field field : BlockStateData.class.getDeclaredFields()) {
				Class<?> type = field.getType();
				if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) || !type.isArray()
						|| type.getComponentType() != Dynamic.class) {
					continue;
				}
				Object base = unsafe.staticFieldBase(field);
				long offset = unsafe.staticFieldOffset(field);
				Dynamic<?>[] current = (Dynamic<?>[]) unsafe.getObject(base, offset);
				if (current == null || current.length < 4096) {
					continue;
				}
				if (current.length >= requiredLength) {
					return current;
				}
				Dynamic<?>[] expanded = Arrays.copyOf(current, requiredLength);
				unsafe.putObjectVolatile(base, offset, expanded);
				return expanded;
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not access Minecraft's legacy block-state flattening table", e);
		}
		throw new IllegalStateException("Could not locate Minecraft's legacy block-state flattening table");
	}

	private static Unsafe unsafe() throws ReflectiveOperationException {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (Unsafe) field.get(null);
	}

	private static boolean containsLegacyMineralogyBlock(CompoundTag level) {
		ListTag sections = level.getList("Sections", 10);
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			CompoundTag section = sections.getCompound(sectionIndex);
			byte[] blocks = section.getByteArray("Blocks");
			if (blocks.length != 4096) {
				continue;
			}
			byte[] add = section.getByteArray("Add");
			for (int blockIndex = 0; blockIndex < blocks.length; ++blockIndex) {
				if (LEGACY_MINERALOGY_BLOCK_IDS.get(blockId(blocks, add, blockIndex))) {
					return true;
				}
			}
		}
		return false;
	}

	private static void rewriteLegacyRockFurnaceTileEntities(CompoundTag level) {
		ListTag tileEntities = level.getList("TileEntities", 10);
		for (int index = 0; index < tileEntities.size(); ++index) {
			CompoundTag tileEntity = tileEntities.getCompound(index);
			int blockId = getLegacyBlockId(level, tileEntity.getInt("x"), tileEntity.getInt("y"),
					tileEntity.getInt("z"));
			if (LEGACY_ROCK_FURNACE_BLOCK_IDS.get(blockId)) {
				tileEntity.putString("id", ROCK_FURNACE_TILE_ENTITY);
			}
		}
	}

	private static int getLegacyBlockId(CompoundTag level, int x, int y, int z) {
		if (y < 0 || y > 255) {
			return -1;
		}
		ListTag sections = level.getList("Sections", 10);
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			CompoundTag section = sections.getCompound(sectionIndex);
			if ((section.getByte("Y") & 0xFF) != y >> 4) {
				continue;
			}
			byte[] blocks = section.getByteArray("Blocks");
			if (blocks.length != 4096) {
				return -1;
			}
			int index = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
			return blockId(blocks, section.getByteArray("Add"), index);
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

	private static Block resolveCurrentBlock(ResourceLocation oldId) {
		ResourceLocation target = BLOCK_ALIASES.getOrDefault(oldId, oldId);
		if (!ForgeRegistries.BLOCKS.containsKey(target)) {
			throw new IllegalStateException("Legacy Mineralogy block has no current replacement: " + oldId);
		}
		return ForgeRegistries.BLOCKS.getValue(target);
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
