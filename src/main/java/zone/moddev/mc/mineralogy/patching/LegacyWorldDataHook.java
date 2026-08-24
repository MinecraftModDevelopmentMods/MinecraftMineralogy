package zone.moddev.mc.mineralogy.patching;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import com.mojang.datafixers.Dynamic;

import cpw.mods.modlauncher.api.INameMappingService;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.WorldPersistenceHooks;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Restores the uppercase Forge registry snapshot written by 1.10 and 1.12.
 * Forge 1.13 reads its own lowercase {@code fml} tag, so without this bridge
 * old mod block IDs reach Mojang's vanilla-only flattening table as air.
 */
public final class LegacyWorldDataHook implements WorldPersistenceHooks.WorldPersistenceHook {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LegacyWorldDataHook INSTANCE = new LegacyWorldDataHook();
	private static final ResourceLocation BLOCK_REGISTRY = new ResourceLocation("minecraft", "blocks");
	private static final Map<String, NBTTagCompound> LEGACY_WORLD_DATA = new ConcurrentHashMap<>();
	private static final Map<ResourceLocation, ResourceLocation> BLOCK_ALIASES = new HashMap<>();
	private static final BitSet LEGACY_MINERALOGY_BLOCK_IDS = new BitSet();
	private static final BitSet LEGACY_ROCK_FURNACE_BLOCK_IDS = new BitSet();
	private static final Set<Long> LEGACY_MINERALOGY_CHUNKS = ConcurrentHashMap.newKeySet();
	private static final String PRESERVE_CHUNK_MARKER = "MineralogyLegacyPreserveChunk";
	private static final String ROCK_FURNACE_TILE_ENTITY = "mineralogy:rock_furnace";
	private static boolean legacyWorldActive;
	private static boolean registered;

	static {
		BLOCK_ALIASES.put(new ResourceLocation(Mineralogy.MODID, "pummice"),
				new ResourceLocation(Mineralogy.MODID, "pumice"));
		BLOCK_ALIASES.put(new ResourceLocation(Mineralogy.MODID, "saprolite"),
				new ResourceLocation(Mineralogy.MODID, "limestone"));
	}

	private LegacyWorldDataHook() {
	}

	public static synchronized void register() {
		if (!registered) {
			WorldPersistenceHooks.addHook(INSTANCE);
			registered = true;
		}
	}

	public static synchronized void prepareLegacyWorld(File levelDat) {
		legacyWorldActive = false;
		LEGACY_MINERALOGY_CHUNKS.clear();
		if (!levelDat.isFile()) {
			return;
		}

		try (FileInputStream input = new FileInputStream(levelDat)) {
			NBTTagCompound root = CompressedStreamTools.readCompressed(input);
			if (root.contains("FML", 10)) {
				prepareLegacyData(levelDat.getParentFile(), root.getCompound("FML"));
			}
		} catch (IOException e) {
			LOGGER.warn("Could not inspect '{}' for legacy Mineralogy registry data", levelDat, e);
		}
	}

	/** Called by the 1.13 chunk-loader coremod before vanilla flattening. */
	public static synchronized void prepareLegacyChunk(NBTTagCompound root) {
		if (!legacyWorldActive || root == null || !root.contains("Level", 10)) {
			return;
		}

		NBTTagCompound level = root.getCompound("Level");
		if (!containsLegacyMineralogyBlock(level)) {
			return;
		}
		rewriteLegacyRockFurnaceTileEntities(level);

		// 1.13 otherwise regenerates old edge chunks whose lighting/population
		// flags were unfinished, replacing already-saved Mineralogy terrain.
		level.setBoolean("TerrainPopulated", true);
		level.setBoolean("LightPopulated", true);
		level.setBoolean(PRESERVE_CHUNK_MARKER, true);
	}

	/** Called by the chunk-loader coremod after vanilla datafixing. */
	public static NBTTagCompound finalizeLegacyChunk(NBTTagCompound root) {
		if (root == null || !root.contains("Level", 10)) {
			return root;
		}

		NBTTagCompound level = root.getCompound("Level");
		if (level.getBoolean(PRESERVE_CHUNK_MARKER)) {
			level.setString("Status", "postprocessed");
			level.removeTag(PRESERVE_CHUNK_MARKER);
		}
		return root;
	}

	@Override
	public String getModId() {
		return "FML";
	}

	@Override
	public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
		NBTTagCompound legacy = LEGACY_WORLD_DATA.get(worldKey(handler));
		return legacy == null ? new NBTTagCompound() : legacy.copy();
	}

	@Override
	public void readData(SaveHandler handler, WorldInfo info, NBTTagCompound tag) {
		prepareLegacyData(handler.getWorldDirectory(), tag);
	}

	private static synchronized void prepareLegacyData(File worldDirectory, NBTTagCompound tag) {
		if (!tag.contains("Registries", 10)) {
			return;
		}

		NBTTagCompound registries = tag.getCompound("Registries");
		if (!registries.contains(BLOCK_REGISTRY.toString(), 10)) {
			throw new IllegalStateException("Legacy Mineralogy world has no saved block registry snapshot");
		}

		String worldKey = worldKey(worldDirectory);
		boolean firstPreparation = !LEGACY_WORLD_DATA.containsKey(worldKey);
		int mappedStates = installLegacyBlockStates(registries.getCompound(BLOCK_REGISTRY.toString()));
		if (firstPreparation) {
			LEGACY_WORLD_DATA.put(worldKey, tag.copy());
		}
		legacyWorldActive = true;
		int protectedChunks = indexLegacyChunks(worldDirectory);
		if (firstPreparation) {
			LOGGER.info("Prepared {} legacy Mineralogy block states and protected {} existing Overworld chunks "
					+ "from '{}' for safe 1.13 flattening", mappedStates, protectedChunks, worldDirectory);
		}
	}

	/** Reads only Anvil location tables so old chunks are protected before their NBT is loaded. */
	private static int indexLegacyChunks(File worldDirectory) {
		LEGACY_MINERALOGY_CHUNKS.clear();
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
			} catch (NumberFormatException exception) {
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
			} catch (IOException exception) {
				LOGGER.warn("Could not inspect legacy chunk locations in '{}'", regionFile, exception);
			}
		}
		return LEGACY_MINERALOGY_CHUNKS.size();
	}

	/** Prevents neighboring new-chunk features from rewriting already populated legacy chunks. */
	public static boolean shouldBlockWorldgenWrite(net.minecraft.util.math.BlockPos position) {
		return legacyWorldActive && position != null
				&& LEGACY_MINERALOGY_CHUNKS.contains(chunkKey(position.getX() >> 4, position.getZ() >> 4));
	}

	private static long chunkKey(int chunkX, int chunkZ) {
		return ((long) chunkX & 0xFFFFFFFFL) << 32 | ((long) chunkZ & 0xFFFFFFFFL);
	}

	private static int installLegacyBlockStates(NBTTagCompound blockSnapshot) {
		LEGACY_MINERALOGY_BLOCK_IDS.clear();
		LEGACY_ROCK_FURNACE_BLOCK_IDS.clear();
		Map<ResourceLocation, Integer> mineralogyIds = new HashMap<>();
		NBTTagList savedIds = blockSnapshot.getList("ids", 10);
		int highestStateId = 0;
		for (int index = 0; index < savedIds.size(); ++index) {
			NBTTagCompound savedId = savedIds.getCompound(index);
			String key = savedId.getString("K");
			if (!key.startsWith(Mineralogy.MODID + ":")) {
				continue;
			}
			ResourceLocation id = new ResourceLocation(key);
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
				IBlockState state = legacyState(block, meta);
				String stateNbt = NBTUtil.writeBlockState(state).toString();
				int stateId = (entry.getValue() << 4) | meta;
				// addEntry is hot during Minecraft's static initialization and the JVM may
				// compile its final-array reference before this table is expanded. Writing
				// the replacement array directly is equivalent here because Mineralogy does
				// not provide any legacy state aliases to addEntry's auxiliary maps.
				legacyStates[stateId] = BlockStateFlatteningMap.makeDynamic(stateNbt);
				++mapped;
			}
		}
		return mapped;
	}

	private static boolean containsLegacyMineralogyBlock(NBTTagCompound level) {
		NBTTagList sections = level.getList("Sections", 10);
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			NBTTagCompound section = sections.getCompound(sectionIndex);
			byte[] blocks = section.getByteArray("Blocks");
			if (blocks.length != 4096) {
				continue;
			}

			byte[] add = section.getByteArray("Add");
			for (int blockIndex = 0; blockIndex < blocks.length; ++blockIndex) {
				int highBits = add.length == 2048
						? (add[blockIndex >> 1] >> ((blockIndex & 1) * 4)) & 0x0F
						: 0;
				int blockId = (blocks[blockIndex] & 0xFF) | (highBits << 8);
				if (LEGACY_MINERALOGY_BLOCK_IDS.get(blockId)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void rewriteLegacyRockFurnaceTileEntities(NBTTagCompound level) {
		NBTTagList tileEntities = level.getList("TileEntities", 10);
		for (int index = 0; index < tileEntities.size(); ++index) {
			NBTTagCompound tileEntity = tileEntities.getCompound(index);
			int blockId = getLegacyBlockId(level, tileEntity.getInt("x"), tileEntity.getInt("y"),
					tileEntity.getInt("z"));
			if (LEGACY_ROCK_FURNACE_BLOCK_IDS.get(blockId)) {
				// 1.12 saved this as "rockfurnace". In 1.10 repeated registration
				// saved the final rock-specific furnace path for every rock furnace.
				tileEntity.setString("id", ROCK_FURNACE_TILE_ENTITY);
			}
		}
	}

	private static int getLegacyBlockId(NBTTagCompound level, int x, int y, int z) {
		if (y < 0 || y > 255) {
			return -1;
		}

		NBTTagList sections = level.getList("Sections", 10);
		int targetSection = y >> 4;
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			NBTTagCompound section = sections.getCompound(sectionIndex);
			if ((section.getByte("Y") & 0xFF) != targetSection) {
				continue;
			}

			byte[] blocks = section.getByteArray("Blocks");
			if (blocks.length != 4096) {
				return -1;
			}
			int blockIndex = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
			byte[] add = section.getByteArray("Add");
			int highBits = add.length == 2048
					? (add[blockIndex >> 1] >> ((blockIndex & 1) * 4)) & 0x0F
					: 0;
			return (blocks[blockIndex] & 0xFF) | (highBits << 8);
		}
		return -1;
	}

	private static Block resolveCurrentBlock(ResourceLocation oldId) {
		ResourceLocation target = BLOCK_ALIASES.getOrDefault(oldId, oldId);
		if (!ForgeRegistries.BLOCKS.containsKey(target)) {
			throw new IllegalStateException("Legacy Mineralogy block has no current replacement: " + oldId);
		}
		return ForgeRegistries.BLOCKS.getValue(target);
	}

	private static IBlockState legacyState(Block block, int meta) {
		IBlockState state = block.getDefaultState();
		if (block instanceof RockSlab) {
			return state.with(RockSlab.FACING, EnumFacing.byIndex(meta));
		}
		if (block instanceof RockSaltLamp) {
			return state.with(RockSaltLamp.FACING, legacyLampFacing(meta));
		}
		if (block instanceof RockSaltStreetLamp) {
			return state.with(RockSaltStreetLamp.FACING, EnumFacing.UP);
		}
		if (block instanceof RockFurnace) {
			EnumFacing facing = EnumFacing.byIndex(meta);
			return state.with(RockFurnace.FACING,
					facing.getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : facing);
		}
		if (block instanceof BlockStairs) {
			EnumFacing facing = EnumFacing.byIndex(5 - (meta & 3));
			Half half = (meta & 4) == 0 ? Half.BOTTOM : Half.TOP;
			return state.with(BlockStairs.FACING, facing)
					.with(BlockStairs.HALF, half)
					.with(BlockStairs.SHAPE, StairsShape.STRAIGHT)
					.with(BlockStairs.WATERLOGGED, Boolean.FALSE);
		}
		return state;
	}

	private static EnumFacing legacyLampFacing(int meta) {
		switch (meta) {
			case 1:
				return EnumFacing.EAST;
			case 2:
				return EnumFacing.WEST;
			case 3:
				return EnumFacing.SOUTH;
			case 4:
				return EnumFacing.NORTH;
			case 5:
				return EnumFacing.DOWN;
			default:
				return EnumFacing.UP;
		}
	}

	@SuppressWarnings("unchecked")
	private static Dynamic<?>[] expandFlatteningTable(int requiredLength) {
		try {
			String fieldName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD,
					"field_199200_b");
			Field valuesField = BlockStateFlatteningMap.class.getDeclaredField(fieldName);
			valuesField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(valuesField, valuesField.getModifiers() & ~Modifier.FINAL);

			Dynamic<?>[] current = (Dynamic<?>[]) valuesField.get(null);
			if (current.length >= requiredLength) {
				return current;
			}
			Dynamic<?>[] expanded = Arrays.copyOf(current, requiredLength);
			valuesField.set(null, expanded);
			return expanded;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not expand Minecraft's legacy block-state flattening table", e);
		}
	}

	private static String worldKey(SaveHandler handler) {
		return worldKey(handler.getWorldDirectory());
	}

	private static String worldKey(File worldDirectory) {
		try {
			return worldDirectory.getCanonicalPath();
		} catch (java.io.IOException e) {
			return worldDirectory.getAbsolutePath();
		}
	}
}
