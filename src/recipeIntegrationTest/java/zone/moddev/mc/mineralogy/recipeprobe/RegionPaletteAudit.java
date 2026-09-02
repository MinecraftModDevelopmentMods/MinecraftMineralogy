package zone.moddev.mc.mineralogy.recipeprobe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

/**
 * Build-only reader for the block-state palettes in disposable Anvil worlds.
 * It deliberately lives beside the loader recipe probe so it cannot enter a
 * production source set or release artifact.
 */
public final class RegionPaletteAudit {
	private static final int SECTOR_BYTES = 4096;
	private static final int LOCATION_COUNT = 1024;

	private RegionPaletteAudit() {
	}

	public static void main(String[] arguments) throws Exception {
		if (arguments.length != 3) {
			throw new IllegalArgumentException("Expected: <region directory> <output JSON> <semicolon-separated targets>");
		}
		Path regionDirectory = Path.of(arguments[0]).toAbsolutePath().normalize();
		Path output = Path.of(arguments[1]).toAbsolutePath().normalize();
		if (!Files.isDirectory(regionDirectory)) {
			throw new IllegalArgumentException("Region directory does not exist: " + regionDirectory);
		}

		Map<String, Long> targets = new LinkedHashMap<>();
		for (String target : arguments[2].split(";")) {
			String trimmed = target.trim();
			if (!trimmed.isEmpty()) targets.put(trimmed, 0L);
		}

		Audit audit = new Audit(targets);
		try (var paths = Files.list(regionDirectory)) {
			for (Path path : paths.filter(candidate -> candidate.getFileName().toString().endsWith(".mca")).sorted().toList()) {
				audit.regions++;
				auditRegion(path, audit);
			}
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("region_directory", regionDirectory.toString());
		result.put("regions", audit.regions);
		result.put("chunks", audit.chunks);
		result.put("palette_entries", audit.paletteEntries);
		result.put("targets", audit.targets);
		result.put("oil_blocks", audit.oilBlocks);
		result.put("oil_min_y", audit.oilBlocks == 0L ? null : audit.oilMinY);
		result.put("oil_max_y", audit.oilBlocks == 0L ? null : audit.oilMaxY);
		result.put("oil_columns", audit.oilColumns);
		result.put("oil_columns_with_two_solid_blocks_above", audit.coveredOilColumns);
		result.put("oil_columns_without_two_solid_blocks_above", audit.uncoveredOilColumns);
		result.put("missing", audit.targets.entrySet().stream()
				.filter(entry -> entry.getValue() == 0L).map(Map.Entry::getKey).toList());
		result.put("mineralogy_entries", new TreeMap<>(audit.mineralogyEntries));
		result.put("rock_furnaces", audit.rockFurnaces);
		Files.createDirectories(output.getParent());
		Files.writeString(output,
				new GsonBuilder().setPrettyPrinting().create().toJson(result) + System.lineSeparator(),
				StandardCharsets.UTF_8);
		System.out.println("MINERALOGY_REGION_AUDIT regions=" + audit.regions + " chunks=" + audit.chunks
				+ " targets=" + audit.targets + " output=" + output);
	}

	private static void auditRegion(Path path, Audit audit) throws IOException {
		try (RandomAccessFile region = new RandomAccessFile(path.toFile(), "r")) {
			if (region.length() < SECTOR_BYTES * 2L) return;
			for (int index = 0; index < LOCATION_COUNT; index++) {
				region.seek(index * 4L);
				int location = region.readInt();
				int sectorOffset = location >>> 8;
				int sectorCount = location & 0xFF;
				if (sectorOffset == 0 || sectorCount == 0) continue;
				long byteOffset = sectorOffset * (long) SECTOR_BYTES;
				if (byteOffset + 5L > region.length()) continue;
				region.seek(byteOffset);
				int length = region.readInt();
				if (length <= 1 || length > sectorCount * SECTOR_BYTES - 4) {
					throw new IOException("Invalid chunk length " + length + " in " + path + " at index " + index);
				}
				int compression = region.readUnsignedByte();
				if ((compression & 0x80) != 0) {
					throw new IOException("External chunk streams are not supported by this bounded audit: " + path);
				}
				byte[] payload = new byte[length - 1];
				region.readFully(payload);
				try (InputStream compressed = new ByteArrayInputStream(payload);
						InputStream decoded = decompress(compression, compressed);
						DataInputStream input = new DataInputStream(decoded)) {
					auditChunk(NbtIo.read(input, NbtAccounter.unlimitedHeap()), audit);
				}
			}
		}
	}

	private static InputStream decompress(int compression, InputStream input) throws IOException {
		return switch (compression) {
			case 1 -> new GZIPInputStream(input);
			case 2 -> new InflaterInputStream(input);
			case 3 -> input;
			default -> throw new IOException("Unsupported region compression type " + compression);
		};
	}

	private static void auditChunk(CompoundTag chunk, Audit audit) {
		audit.chunks++;
		ListTag sections = chunk.getList("sections", Tag.TAG_COMPOUND);
		Map<Integer, CompoundTag> sectionsByY = new HashMap<>();
		boolean containsOil = false;
		for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++) {
			CompoundTag section = sections.getCompound(sectionIndex);
			sectionsByY.put((int) section.getByte("Y"), section);
			CompoundTag blockStates = section.getCompound("block_states");
			ListTag palette = blockStates.getList("palette", Tag.TAG_COMPOUND);
			for (int paletteIndex = 0; paletteIndex < palette.size(); paletteIndex++) {
				String name = palette.getCompound(paletteIndex).getString("Name");
				containsOil |= "mineralogy:crude_oil".equals(name);
				audit.paletteEntries++;
				if (audit.targets.containsKey(name)) audit.targets.merge(name, 1L, Long::sum);
				if (name.startsWith("mineralogy:")) audit.mineralogyEntries.merge(name, 1L, Long::sum);
			}
		}
		if (containsOil) auditOilCover(sectionsByY, audit);
		auditRockFurnaces(chunk.getList("block_entities", Tag.TAG_COMPOUND), audit);
	}

	private static void auditRockFurnaces(ListTag blockEntities, Audit audit) {
		for (int index = 0; index < blockEntities.size(); index++) {
			CompoundTag blockEntity = blockEntities.getCompound(index);
			if (!"mineralogy:rock_furnace".equals(blockEntity.getString("id"))) continue;
			Map<String, Object> furnace = new LinkedHashMap<>();
			furnace.put("id", blockEntity.getString("id"));
			furnace.put("x", blockEntity.getInt("x"));
			furnace.put("y", blockEntity.getInt("y"));
			furnace.put("z", blockEntity.getInt("z"));
			furnace.put("burn_time", numericValue(blockEntity, "BurnTime", "burn_time"));
			furnace.put("cook_time", numericValue(blockEntity, "CookTime", "cook_time"));
			furnace.put("cook_total", numericValue(blockEntity, "CookTimeTotal", "cook_time_total"));
			List<Map<String, Object>> items = new ArrayList<>();
			ListTag itemTags = blockEntity.getList("Items", Tag.TAG_COMPOUND);
			for (int itemIndex = 0; itemIndex < itemTags.size(); itemIndex++) {
				CompoundTag item = itemTags.getCompound(itemIndex);
				Map<String, Object> value = new LinkedHashMap<>();
				value.put("slot", numericValue(item, "Slot", "slot"));
				value.put("id", item.getString("id"));
				value.put("count", numericValue(item, "Count", "count"));
				items.add(value);
			}
			furnace.put("items", items);
			audit.rockFurnaces.add(furnace);
		}
	}

	private static int numericValue(CompoundTag tag, String legacyName, String currentName) {
		return tag.contains(currentName, Tag.TAG_ANY_NUMERIC) ? tag.getInt(currentName) : tag.getInt(legacyName);
	}

	private static void auditOilCover(Map<Integer, CompoundTag> sectionsByY, Audit audit) {
		Map<Integer, String[]> decoded = new HashMap<>();
		for (Map.Entry<Integer, CompoundTag> entry : sectionsByY.entrySet()) {
			decoded.put(entry.getKey(), decodeSection(entry.getValue().getCompound("block_states")));
		}
		Map<Integer, Integer> highestOilByColumn = new HashMap<>();
		for (Map.Entry<Integer, String[]> entry : decoded.entrySet()) {
			int sectionY = entry.getKey();
			String[] states = entry.getValue();
			for (int index = 0; index < states.length; index++) {
				if (!"mineralogy:crude_oil".equals(states[index])) continue;
				int localX = index & 15;
				int localZ = (index >>> 4) & 15;
				int localY = index >>> 8;
				int y = sectionY * 16 + localY;
				int column = (localZ << 4) | localX;
				highestOilByColumn.merge(column, y, Math::max);
				audit.oilBlocks++;
				audit.oilMinY = Math.min(audit.oilMinY, y);
				audit.oilMaxY = Math.max(audit.oilMaxY, y);
			}
		}
		for (Map.Entry<Integer, Integer> entry : highestOilByColumn.entrySet()) {
			int localX = entry.getKey() & 15;
			int localZ = (entry.getKey() >>> 4) & 15;
			int topOilY = entry.getValue();
			audit.oilColumns++;
			if (isSolidCover(blockAt(decoded, localX, topOilY + 1, localZ))
					&& isSolidCover(blockAt(decoded, localX, topOilY + 2, localZ))) {
				audit.coveredOilColumns++;
			} else {
				audit.uncoveredOilColumns++;
			}
		}
	}

	private static String[] decodeSection(CompoundTag blockStates) {
		ListTag palette = blockStates.getList("palette", Tag.TAG_COMPOUND);
		String[] names = new String[palette.size()];
		for (int index = 0; index < palette.size(); index++) {
			names[index] = palette.getCompound(index).getString("Name");
		}
		String[] states = new String[4096];
		if (names.length == 0) return states;
		if (names.length == 1 || !blockStates.contains("data", Tag.TAG_LONG_ARRAY)) {
			java.util.Arrays.fill(states, names[0]);
			return states;
		}
		int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(names.length - 1));
		int valuesPerLong = 64 / bits;
		long mask = (1L << bits) - 1L;
		long[] data = blockStates.getLongArray("data");
		for (int index = 0; index < states.length; index++) {
			int longIndex = index / valuesPerLong;
			if (longIndex >= data.length) break;
			int paletteIndex = (int) ((data[longIndex] >>> ((index % valuesPerLong) * bits)) & mask);
			if (paletteIndex < names.length) states[index] = names[paletteIndex];
		}
		return states;
	}

	private static String blockAt(Map<Integer, String[]> decoded, int x, int y, int z) {
		int sectionY = Math.floorDiv(y, 16);
		String[] states = decoded.get(sectionY);
		if (states == null) return "minecraft:air";
		int localY = Math.floorMod(y, 16);
		return states[(localY << 8) | (z << 4) | x];
	}

	private static boolean isSolidCover(String block) {
		return block != null && !block.equals("minecraft:air") && !block.equals("minecraft:cave_air")
				&& !block.equals("minecraft:void_air") && !block.equals("minecraft:water")
				&& !block.equals("minecraft:lava") && !block.equals("mineralogy:crude_oil");
	}

	private static final class Audit {
		private final Map<String, Long> targets;
		private final Map<String, Long> mineralogyEntries = new LinkedHashMap<>();
		private final List<Map<String, Object>> rockFurnaces = new ArrayList<>();
		private int regions;
		private long chunks;
		private long paletteEntries;
		private long oilBlocks;
		private int oilMinY = Integer.MAX_VALUE;
		private int oilMaxY = Integer.MIN_VALUE;
		private long oilColumns;
		private long coveredOilColumns;
		private long uncoveredOilColumns;

		private Audit(Map<String, Long> targets) {
			this.targets = targets;
		}
	}
}
