package com.mcmoddev.mineralogy.patching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.blocks.RockFurnace;
import com.mcmoddev.mineralogy.blocks.RockSaltLamp;
import com.mcmoddev.mineralogy.blocks.RockSaltStreetLamp;
import com.mcmoddev.mineralogy.blocks.RockSlab;
import com.mojang.datafixers.Dynamic;

import cpw.mods.modlauncher.api.INameMappingService;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Converts pre-flattening Mineralogy block IDs before vanilla chunk datafixing. */
public final class LegacyWorldDataHook {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<ResourceLocation, ResourceLocation> BLOCK_ALIASES = new HashMap<>();
	private static final BitSet LEGACY_MINERALOGY_BLOCK_IDS = new BitSet();
	private static final BitSet LEGACY_ROCK_FURNACE_BLOCK_IDS = new BitSet();
	private static final String PRESERVE_CHUNK_MARKER = "MineralogyLegacyPreserveChunk";
	private static final String ROCK_FURNACE_TILE_ENTITY = "mineralogy:rock_furnace";
	private static final String SIDECAR_NAME = "mineralogy_legacy_registry.dat";
	private static volatile boolean legacyWorldActive;

	static {
		BLOCK_ALIASES.put(new ResourceLocation(Mineralogy.MODID, "pummice"),
				new ResourceLocation(Mineralogy.MODID, "pumice"));
		BLOCK_ALIASES.put(new ResourceLocation(Mineralogy.MODID, "saprolite"),
				new ResourceLocation(Mineralogy.MODID, "limestone"));
	}

	private LegacyWorldDataHook() {
	}

	public static void onServerAboutToStart(FMLServerAboutToStartEvent event) {
		File levelDat = event.getServer().getActiveAnvilConverter()
				.getFile(event.getServer().getFolderName(), "level.dat");
		prepareLegacyWorld(levelDat);
	}

	private static synchronized void prepareLegacyWorld(File levelDat) {
		legacyWorldActive = false;
		if (!levelDat.isFile()) {
			return;
		}

		try (FileInputStream input = new FileInputStream(levelDat)) {
			CompoundNBT root = CompressedStreamTools.readCompressed(input);
			if (root.contains("FML", 10)) {
				CompoundNBT fml = root.getCompound("FML");
				CompoundNBT registries = fml.getCompound("Registries");
				if (registries.contains("minecraft:blocks", 10)) {
					CompoundNBT blocks = registries.getCompound("minecraft:blocks");
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
				install(levelDat.getParentFile(), CompressedStreamTools.readCompressed(input).getCompound("Blocks"));
			} catch (IOException e) {
				LOGGER.warn("Could not read legacy Mineralogy registry sidecar '{}'", sidecar, e);
			}
		}
	}

	private static void install(File worldDirectory, CompoundNBT blockSnapshot) {
		int mappedStates = installLegacyBlockStates(blockSnapshot);
		legacyWorldActive = mappedStates > 0;
		if (legacyWorldActive) {
			LOGGER.info("Prepared {} legacy Mineralogy block states from '{}'", mappedStates, worldDirectory);
		}
	}

	private static void writeSidecar(File worldDirectory, CompoundNBT blockSnapshot) {
		File sidecar = sidecar(worldDirectory);
		if (sidecar.isFile()) {
			return;
		}
		File parent = sidecar.getParentFile();
		File temporary = new File(parent, SIDECAR_NAME + ".tmp");
		try {
			Files.createDirectories(parent.toPath());
			CompoundNBT root = new CompoundNBT();
			root.put("Blocks", blockSnapshot.copy());
			try (FileOutputStream output = new FileOutputStream(temporary)) {
				CompressedStreamTools.writeCompressed(root, output);
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
	public static void prepareLegacyChunk(CompoundNBT root) {
		if (!legacyWorldActive || root == null || !root.contains("Level", 10)) {
			return;
		}

		CompoundNBT level = root.getCompound("Level");
		if (!containsLegacyMineralogyBlock(level)) {
			return;
		}
		rewriteLegacyRockFurnaceTileEntities(level);
		level.putBoolean("TerrainPopulated", true);
		level.putBoolean("LightPopulated", true);
		level.putBoolean(PRESERVE_CHUNK_MARKER, true);
	}

	/** Called by the chunk-loader coremod after vanilla datafixing. */
	public static CompoundNBT finalizeLegacyChunk(CompoundNBT root) {
		if (root == null || !root.contains("Level", 10)) {
			return root;
		}
		CompoundNBT level = root.getCompound("Level");
		if (level.getBoolean(PRESERVE_CHUNK_MARKER)) {
			level.putString("Status", "full");
			level.remove(PRESERVE_CHUNK_MARKER);
		}
		return root;
	}

	private static int installLegacyBlockStates(CompoundNBT blockSnapshot) {
		LEGACY_MINERALOGY_BLOCK_IDS.clear();
		LEGACY_ROCK_FURNACE_BLOCK_IDS.clear();
		Map<ResourceLocation, Integer> mineralogyIds = new HashMap<>();
		ListNBT savedIds = blockSnapshot.getList("ids", 10);
		int highestStateId = 0;
		for (int index = 0; index < savedIds.size(); ++index) {
			CompoundNBT savedId = savedIds.getCompound(index);
			String key = savedId.getString("K");
			if (!key.startsWith(Mineralogy.MODID + ":")) {
				continue;
			}
			ResourceLocation id = new ResourceLocation(key);
			int numericId = savedId.getInt("V");
			mineralogyIds.put(id, numericId);
			highestStateId = Math.max(highestStateId, (numericId << 4) | 15);
		}
		expandFlatteningTable(highestStateId + 1);

		Method addEntry = ObfuscationReflectionHelper.findMethod(BlockStateFlatteningMap.class,
				"func_199194_a", int.class, String.class, String[].class);
		int mapped = 0;
		for (Map.Entry<ResourceLocation, Integer> entry : mineralogyIds.entrySet()) {
			ResourceLocation oldId = entry.getKey();
			LEGACY_MINERALOGY_BLOCK_IDS.set(entry.getValue());
			Block block = resolveCurrentBlock(oldId);
			if (block instanceof RockFurnace) {
				LEGACY_ROCK_FURNACE_BLOCK_IDS.set(entry.getValue());
			}
			for (int meta = 0; meta < 16; ++meta) {
				String stateNbt = NBTUtil.writeBlockState(legacyState(block, meta)).toString();
				try {
					addEntry.invoke(null, (entry.getValue() << 4) | meta, stateNbt, new String[0]);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException("Could not register legacy block state " + oldId + ":" + meta, e);
				}
				++mapped;
			}
		}
		return mapped;
	}

	private static boolean containsLegacyMineralogyBlock(CompoundNBT level) {
		ListNBT sections = level.getList("Sections", 10);
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			CompoundNBT section = sections.getCompound(sectionIndex);
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

	private static void rewriteLegacyRockFurnaceTileEntities(CompoundNBT level) {
		ListNBT tileEntities = level.getList("TileEntities", 10);
		for (int index = 0; index < tileEntities.size(); ++index) {
			CompoundNBT tileEntity = tileEntities.getCompound(index);
			int blockId = getLegacyBlockId(level, tileEntity.getInt("x"), tileEntity.getInt("y"),
					tileEntity.getInt("z"));
			if (LEGACY_ROCK_FURNACE_BLOCK_IDS.get(blockId)) {
				tileEntity.putString("id", ROCK_FURNACE_TILE_ENTITY);
			}
		}
	}

	private static int getLegacyBlockId(CompoundNBT level, int x, int y, int z) {
		if (y < 0 || y > 255) {
			return -1;
		}
		ListNBT sections = level.getList("Sections", 10);
		for (int sectionIndex = 0; sectionIndex < sections.size(); ++sectionIndex) {
			CompoundNBT section = sections.getCompound(sectionIndex);
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

	private static Block resolveCurrentBlock(ResourceLocation oldId) {
		ResourceLocation target = BLOCK_ALIASES.getOrDefault(oldId, oldId);
		if (!ForgeRegistries.BLOCKS.containsKey(target)) {
			throw new IllegalStateException("Legacy Mineralogy block has no current replacement: " + oldId);
		}
		return ForgeRegistries.BLOCKS.getValue(target);
	}

	private static BlockState legacyState(Block block, int meta) {
		BlockState state = block.getDefaultState();
		if (block instanceof RockSlab) {
			return state.with(RockSlab.FACING, Direction.byIndex(meta));
		}
		if (block instanceof RockSaltLamp) {
			return state.with(RockSaltLamp.FACING, legacyLampFacing(meta));
		}
		if (block instanceof RockSaltStreetLamp) {
			return state.with(RockSaltStreetLamp.FACING, Direction.UP);
		}
		if (block instanceof RockFurnace) {
			Direction facing = Direction.byIndex(meta);
			return state.with(RockFurnace.FACING,
					facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : facing);
		}
		if (block instanceof StairsBlock) {
			Direction facing = Direction.byIndex(5 - (meta & 3));
			return state.with(StairsBlock.FACING, facing)
					.with(StairsBlock.HALF, (meta & 4) == 0 ? Half.BOTTOM : Half.TOP)
					.with(StairsBlock.SHAPE, StairsShape.STRAIGHT)
					.with(StairsBlock.WATERLOGGED, Boolean.FALSE);
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

	@SuppressWarnings("unchecked")
	private static void expandFlatteningTable(int requiredLength) {
		try {
			String fieldName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD,
					"field_199200_b");
			Field valuesField = BlockStateFlatteningMap.class.getDeclaredField(fieldName);
			valuesField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(valuesField, valuesField.getModifiers() & ~Modifier.FINAL);
			Dynamic<?>[] current = (Dynamic<?>[]) valuesField.get(null);
			if (current.length < requiredLength) {
				valuesField.set(null, Arrays.copyOf(current, requiredLength));
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not expand Minecraft's legacy block-state flattening table", e);
		}
	}
}
