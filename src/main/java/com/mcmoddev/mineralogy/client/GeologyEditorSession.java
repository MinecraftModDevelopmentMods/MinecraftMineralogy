package com.mcmoddev.mineralogy.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.mineralogy.worldgen.RockFamily;
import com.mcmoddev.mineralogy.worldgen.WorldGeologyProfile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/** Mutable client-side copy used until the Create World settings are accepted. */
final class GeologyEditorSession {
	enum MaterialTab {
		SEDIMENTARY("sedimentary"),
		METAMORPHIC("metamorphic"),
		IGNEOUS("igneous"),
		ORES("ores"),
		UNASSIGNED("unassigned");

		final String key;

		MaterialTab(String key) {
			this.key = key;
		}
	}

	static final Set<String> BUILT_IN_GEOMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"stable_craton", "mountain_belt", "volcanic_arc", "sedimentary_basin",
			"coastal_shelf", "arid_basin", "wetland_basin", "glacial_highland")));

	private final WorldGeologyProfile originalProfile;
	private final JsonObject original;
	private final JsonObject root;

	GeologyEditorSession(WorldGeologyProfile profile) {
		originalProfile = profile;
		original = profile.rootCopy();
		root = profile.rootCopy();
		normalizeRegistrySections(original);
		normalizeRegistrySections(root);
	}

	JsonObject root() {
		return root;
	}

	WorldGeologyProfile profile() {
		return originalProfile.withRoot(root);
	}

	void applyProfile(WorldGeologyProfile profile) {
		root.entrySet().clear();
		for (Entry<String, JsonElement> entry : profile.rootCopy().entrySet()) {
			root.add(entry.getKey(), entry.getValue().deepCopy());
		}
		normalizeRegistrySections(root);
	}

	JsonObject section(String key) {
		return object(root, key);
	}

	List<String> materialIds(MaterialTab tab, String search, boolean showAll) {
		String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
		List<String> result = new ArrayList<>();
		if (tab == MaterialTab.ORES) {
			result.addAll(section("ores").keySet());
		} else if (tab == MaterialTab.UNASSIGNED) {
			return availableBlockIds(query, "", showAll);
		} else {
			for (Entry<String, JsonElement> entry : section("rocks").entrySet()) {
				if (!entry.getValue().isJsonObject()) {
					continue;
				}
				String family = string(entry.getValue().getAsJsonObject(), "family", "");
				if ((tab == MaterialTab.SEDIMENTARY && "sedimentary".equals(family))
						|| (tab == MaterialTab.METAMORPHIC && "metamorphic".equals(family))
						|| (tab == MaterialTab.IGNEOUS && family.startsWith("igneous_"))) {
					result.add(entry.getKey());
				}
			}
		}
		result.removeIf(id -> !query.isEmpty() && !id.toLowerCase(Locale.ROOT).contains(query));
		Collections.sort(result);
		return result;
	}

	List<String> availableBlockIds(String search, String namespace, boolean showAll) {
		String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
		String mod = namespace == null ? "" : namespace.trim().toLowerCase(Locale.ROOT);
		Set<String> assigned = new HashSet<>(section("rocks").keySet());
		assigned.addAll(section("ores").keySet());
		List<String> result = new ArrayList<>();
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
			if (id == null || assigned.contains(id.toString()) || isWorldgenAliasSource(id.toString())
					|| (!mod.isEmpty() && !mod.equals(id.getNamespace()))
					|| (!query.isEmpty() && !id.toString().toLowerCase(Locale.ROOT).contains(query))
					|| !isSelectable(block, showAll)) {
				continue;
			}
			result.add(id.toString());
		}
		Collections.sort(result);
		return result;
	}

	List<String> installedBlockNamespaces() {
		TreeSet<String> namespaces = new TreeSet<>();
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
			if (id != null && block != Blocks.AIR && block.asItem() != Items.AIR) {
				namespaces.add(id.getNamespace());
			}
		}
		List<String> result = new ArrayList<>();
		result.add("");
		result.addAll(namespaces);
		return result;
	}

	void assignRock(String id, RockFamily family) {
		String canonicalId = canonicalBlockId(id);
		if (canonicalId == null) {
			return;
		}
		JsonObject rock = new JsonObject();
		rock.addProperty("enabled", true);
		rock.addProperty("family", family.configName);
		rock.addProperty("depth_peak", defaultPeak(family));
		rock.addProperty("depth_spread", 40);
		rock.addProperty("min_y", -64);
		rock.addProperty("max_y", 319);
		rock.addProperty("weight", 1.0D);
		rock.addProperty("ore_replaceable", true);
		rock.add("geomes", new JsonObject());
		section("rocks").add(canonicalId, rock);
		disableOrRemoveOre(canonicalId);
	}

	void assignOre(String id) {
		String canonicalId = canonicalBlockId(id);
		if (canonicalId == null) {
			return;
		}
		section("rocks").remove(canonicalId);
		JsonObject ore = new JsonObject();
		ore.addProperty("enabled", true);
		ResourceLocation blockId = new ResourceLocation(canonicalId);
		ore.addProperty("source_mod", blockId.getNamespace());
		JsonObject dimensions = new JsonObject();
		JsonObject overworld = defaultOreDimension();
		dimensions.add("minecraft:overworld", overworld);
		ore.add("dimensions", dimensions);
		section("ores").add(canonicalId, ore);
	}

	void removeRock(String id) {
		section("rocks").remove(id);
	}

	void disableOrRemoveOre(String id) {
		JsonObject ores = section("ores");
		if (!ores.has(id) || !ores.get(id).isJsonObject()) {
			return;
		}
		JsonObject ore = ores.getAsJsonObject(id);
		if (ore.has("source_provider")) {
			ore.addProperty("enabled", false);
			ore.addProperty("unassigned", true);
		} else {
			ores.remove(id);
		}
	}

	void resetEntry(String section, String id) {
		JsonObject originalSection = object(original, section);
		if (originalSection.has(id)) {
			section(section).add(id, originalSection.get(id).deepCopy());
		}
	}

	JsonObject rock(String id) {
		return objectEntry(section("rocks"), id);
	}

	JsonObject ore(String id) {
		return objectEntry(section("ores"), id);
	}

	List<String> geomeIds() {
		List<String> ids = new ArrayList<>(section("geomes").keySet());
		Collections.sort(ids);
		return ids;
	}

	List<String> configuredBiomeIds() {
		TreeSet<String> ids = new TreeSet<>(section("biomes").keySet());
		for (net.minecraft.world.level.biome.Biome biome : ForgeRegistries.BIOMES.getValues()) {
			ResourceLocation id = ForgeRegistries.BIOMES.getKey(biome);
			if (id != null) {
				ids.add(id.toString());
			}
		}
		return new ArrayList<>(ids);
	}

	List<String> dictionaryIds() {
		List<String> ids = new ArrayList<>(section("biome_dictionary").keySet());
		Collections.sort(ids);
		return ids;
	}

	JsonObject weightMap(String section, String id) {
		JsonObject parent = section(section);
		return objectEntry(parent, id);
	}

	void addGeome(String id) {
		String normalized = id.trim().toLowerCase(Locale.ROOT);
		if (!normalized.matches("[a-z0-9_.-]+") || section("geomes").has(normalized)) {
			return;
		}
		JsonObject geome = new JsonObject();
		geome.addProperty("base", 1.0D);
		JsonObject families = new JsonObject();
		for (RockFamily family : RockFamily.values()) {
			families.addProperty(family.configName, 1.0D);
		}
		geome.add("families", families);
		section("geomes").add(normalized, geome);
	}

	void removeGeome(String id) {
		if (BUILT_IN_GEOMES.contains(id)) {
			return;
		}
		section("geomes").remove(id);
		removeWeightKey(section("biomes"), id);
		removeWeightKey(section("biome_dictionary"), id);
		for (Entry<String, JsonElement> rock : section("rocks").entrySet()) {
			if (rock.getValue().isJsonObject() && rock.getValue().getAsJsonObject().has("geomes")) {
				rock.getValue().getAsJsonObject().getAsJsonObject("geomes").remove(id);
			}
		}
		for (Entry<String, JsonElement> ore : section("ores").entrySet()) {
			if (!ore.getValue().isJsonObject()) continue;
			JsonObject oreObject = ore.getValue().getAsJsonObject();
			if (!oreObject.has("dimensions") || !oreObject.get("dimensions").isJsonObject()) continue;
			for (Entry<String, JsonElement> dimension : oreObject.getAsJsonObject("dimensions").entrySet()) {
				if (dimension.getValue().isJsonObject() && dimension.getValue().getAsJsonObject().has("geomes")) {
					dimension.getValue().getAsJsonObject().getAsJsonObject("geomes").remove(id);
				}
			}
		}
	}

	List<String> validate() {
		List<String> errors = new ArrayList<>();
		JsonObject geomes = section("geomes");
		if (geomes.entrySet().isEmpty()) {
			errors.add("At least one geome is required.");
		}
		for (Entry<String, JsonElement> entry : geomes.entrySet()) {
			if (!entry.getKey().matches("[a-z0-9_.-]+") || !entry.getValue().isJsonObject()) {
				errors.add("Invalid geome: " + entry.getKey());
				continue;
			}
			JsonObject definition = entry.getValue().getAsJsonObject();
			JsonObject families = definition.has("families") && definition.get("families").isJsonObject()
					? definition.getAsJsonObject("families") : new JsonObject();
			double total = 0.0D;
			for (RockFamily family : RockFamily.values()) {
				double value = decimal(families, family.configName, 0.0D);
				if (value < 0.0D || !Double.isFinite(value)) errors.add("Invalid family weight in " + entry.getKey());
				total += Math.max(0.0D, value);
			}
			if (total <= 0.0D) errors.add("Geome has no available rock families: " + entry.getKey());
		}
		int sedimentary = 0;
		int metamorphic = 0;
		int igneous = 0;
		for (Entry<String, JsonElement> entry : section("rocks").entrySet()) {
			if (!validBlock(entry.getKey()) || !entry.getValue().isJsonObject()) {
				errors.add("Invalid rock block: " + entry.getKey());
				continue;
			}
			JsonObject rock = entry.getValue().getAsJsonObject();
			if (!bool(rock, "enabled", true) || decimal(rock, "weight", 1.0D) <= 0.0D) {
				continue;
			}
			String family = string(rock, "family", "");
			try {
				RockFamily.fromConfigName(family);
			} catch (RuntimeException e) {
				errors.add("Invalid family for " + entry.getKey());
				continue;
			}
			if (integer(rock, "min_y", -64) > integer(rock, "max_y", 319)) {
				errors.add("Minimum Y is above maximum Y for " + entry.getKey());
			}
			validateGeomeWeights(errors, entry.getKey(), rock.get("geomes"), geomes);
			if ("sedimentary".equals(family)) sedimentary++;
			else if ("metamorphic".equals(family)) metamorphic++;
			else igneous++;
		}
		if (sedimentary == 0) errors.add("At least one enabled sedimentary rock is required.");
		if (metamorphic == 0) errors.add("At least one enabled metamorphic rock is required.");
		if (igneous == 0) errors.add("At least one enabled igneous rock is required.");

		for (Entry<String, JsonElement> entry : section("ores").entrySet()) {
			if (!entry.getValue().isJsonObject() || !validBlock(entry.getKey())) {
				errors.add("Invalid ore block: " + entry.getKey());
				continue;
			}
			JsonObject ore = entry.getValue().getAsJsonObject();
			if (!bool(ore, "enabled", true)) {
				continue;
			}
			if (!ore.has("dimensions") || !ore.get("dimensions").isJsonObject()
					|| ore.getAsJsonObject("dimensions").entrySet().isEmpty()) {
				errors.add("Ore has no dimension rules: " + entry.getKey());
				continue;
			}
			for (Entry<String, JsonElement> dimension : ore.getAsJsonObject("dimensions").entrySet()) {
				if (!validResource(dimension.getKey()) || !dimension.getValue().isJsonObject()) {
					errors.add("Invalid dimension for " + entry.getKey());
					continue;
				}
				JsonObject rule = dimension.getValue().getAsJsonObject();
				if (integer(rule, "min_y", -64) > integer(rule, "max_y", 320)
						|| decimal(rule, "frequency", 0.0D) < 0.0D
						|| integer(rule, "quantity", 0) < 1) {
					errors.add("Invalid placement values for " + entry.getKey() + " in " + dimension.getKey());
				}
				if (bool(rule, "enabled", true)) {
					boolean hosts = validBlockArray(rule.get("host_blocks"), errors, entry.getKey())
							|| validIdArray(rule.get("host_tags"));
					if (rule.has("host_families") && rule.get("host_families").isJsonArray()) {
						for (JsonElement family : rule.getAsJsonArray("host_families")) {
							try { RockFamily.fromConfigName(family.getAsString()); hosts = true; }
							catch (RuntimeException e) { errors.add("Invalid host family for " + entry.getKey()); }
						}
					}
					if (!hosts) errors.add("Enabled ore has no hosts: " + entry.getKey());
				}
				validateGeomeWeights(errors, entry.getKey(), rule.get("geomes"), geomes);
			}
		}
		validateRuleGeomes(errors, section("biomes"), geomes, "biome");
		validateRuleGeomes(errors, section("biome_dictionary"), geomes, "biome type");
		return errors;
	}

	private static void validateRuleGeomes(List<String> errors, JsonObject rules, JsonObject geomes, String label) {
		for (Entry<String, JsonElement> entry : rules.entrySet()) {
			validateGeomeWeights(errors, label + " " + entry.getKey(), entry.getValue(), geomes);
		}
	}

	private static void validateGeomeWeights(List<String> errors, String owner, JsonElement element,
			JsonObject geomes) {
		if (element == null) return;
		if (!element.isJsonObject()) {
			errors.add("Invalid geome weights for " + owner);
			return;
		}
		for (Entry<String, JsonElement> weight : element.getAsJsonObject().entrySet()) {
			if (!geomes.has(weight.getKey())) {
				errors.add("Unknown geome '" + weight.getKey() + "' in " + owner);
				continue;
			}
			try {
				double value = weight.getValue().getAsDouble();
				if (!Double.isFinite(value) || value < 0.0D) throw new NumberFormatException();
			} catch (RuntimeException e) {
				errors.add("Invalid geome weight in " + owner);
			}
		}
	}

	private static boolean validIdArray(JsonElement element) {
		if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() == 0) return false;
		boolean found = false;
		for (JsonElement value : element.getAsJsonArray()) {
			if (!validResource(value.getAsString())) return false;
			found = true;
		}
		return found;
	}

	private static boolean validBlockArray(JsonElement element, List<String> errors, String oreId) {
		if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() == 0) return false;
		boolean found = false;
		for (JsonElement value : element.getAsJsonArray()) {
			String id;
			try {
				id = value.getAsString();
			} catch (RuntimeException e) {
				errors.add("Invalid host block for " + oreId);
				continue;
			}
			if (!validBlock(id)) {
				errors.add("Unknown host block '" + id + "' for " + oreId);
				continue;
			}
			found = true;
		}
		return found;
	}

	static JsonObject defaultOreDimension() {
		JsonObject dimension = new JsonObject();
		dimension.addProperty("enabled", true);
		dimension.addProperty("min_y", -64);
		dimension.addProperty("max_y", 64);
		dimension.addProperty("frequency", 1.0D);
		dimension.addProperty("quantity", 8);
		JsonArray families = new JsonArray();
		for (RockFamily family : RockFamily.values()) {
			families.add(family.configName);
		}
		dimension.add("host_families", families);
		JsonArray tags = new JsonArray();
		tags.add("minecraft:stone_ore_replaceables");
		tags.add("minecraft:deepslate_ore_replaceables");
		dimension.add("host_tags", tags);
		return dimension;
	}

	private static boolean isSelectable(Block block, boolean showAll) {
		if (block == Blocks.AIR || block.asItem() == Items.AIR || block instanceof LiquidBlock) {
			return false;
		}
		if (showAll) {
			return true;
		}
		return !(block instanceof EntityBlock)
				&& block.defaultBlockState().getMaterial().blocksMotion()
				&& Block.isShapeFullBlock(block.defaultBlockState().getCollisionShape(
						EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
	}

	String canonicalBlockId(String id) {
		try {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
			ResourceLocation canonical = block == null ? null : ForgeRegistries.BLOCKS.getKey(block);
			return block == Blocks.AIR || canonical == null ? null : canonical.toString();
		} catch (RuntimeException e) {
			return null;
		}
	}

	private void normalizeRegistrySections(JsonObject profileRoot) {
		normalizeRegistrySection(profileRoot, "rocks", true);
		normalizeRegistrySection(profileRoot, "ores", false);
	}

	private void normalizeRegistrySection(JsonObject profileRoot, String sectionName, boolean applyAliases) {
		JsonObject source = object(profileRoot, sectionName);
		Map<String, JsonElement> normalized = new LinkedHashMap<>();
		JsonObject aliases = profileRoot.has("worldgen_aliases")
				&& profileRoot.get("worldgen_aliases").isJsonObject()
				? profileRoot.getAsJsonObject("worldgen_aliases") : new JsonObject();
		for (Entry<String, JsonElement> entry : source.entrySet()) {
			String id = canonicalBlockId(entry.getKey());
			if (id == null) id = entry.getKey();
			if (applyAliases && aliases.has(id)) {
				try {
					String target = canonicalBlockId(aliases.get(id).getAsString());
					if (target != null) id = target;
				} catch (RuntimeException ignored) { }
			}
			normalized.putIfAbsent(id, entry.getValue().deepCopy());
		}
		JsonObject replacement = new JsonObject();
		for (Entry<String, JsonElement> entry : normalized.entrySet()) {
			replacement.add(entry.getKey(), entry.getValue());
		}
		profileRoot.add(sectionName, replacement);
	}

	private boolean isWorldgenAliasSource(String id) {
		JsonObject aliases = section("worldgen_aliases");
		if (!aliases.has(id)) return false;
		try {
			return !id.equals(new ResourceLocation(aliases.get(id).getAsString()).toString());
		} catch (RuntimeException e) {
			return false;
		}
	}

	private static int defaultPeak(RockFamily family) {
		switch (family) {
		case SEDIMENTARY: return 60;
		case METAMORPHIC: return 28;
		case IGNEOUS_INTRUSIVE: return 24;
		case IGNEOUS_VOLCANIC: return 72;
		default: return 48;
		}
	}

	private static void removeWeightKey(JsonObject rules, String geome) {
		for (Entry<String, JsonElement> rule : rules.entrySet()) {
			if (rule.getValue().isJsonObject()) {
				rule.getValue().getAsJsonObject().remove(geome);
			}
		}
	}

	private static JsonObject object(JsonObject parent, String key) {
		if (!parent.has(key) || !parent.get(key).isJsonObject()) {
			JsonObject result = new JsonObject();
			parent.add(key, result);
			return result;
		}
		return parent.getAsJsonObject(key);
	}

	private static JsonObject objectEntry(JsonObject parent, String key) {
		if (!parent.has(key) || !parent.get(key).isJsonObject()) {
			JsonObject result = new JsonObject();
			parent.add(key, result);
			return result;
		}
		return parent.getAsJsonObject(key);
	}

	private static boolean validBlock(String id) {
		if (!validResource(id)) return false;
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
		return block != null && block != Blocks.AIR;
	}

	private static boolean validResource(String id) {
		try {
			new ResourceLocation(id);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	static String string(JsonObject json, String key, String fallback) {
		try { return json.has(key) ? json.get(key).getAsString() : fallback; }
		catch (RuntimeException e) { return fallback; }
	}

	static boolean bool(JsonObject json, String key, boolean fallback) {
		try { return json.has(key) ? json.get(key).getAsBoolean() : fallback; }
		catch (RuntimeException e) { return fallback; }
	}

	static int integer(JsonObject json, String key, int fallback) {
		try { return json.has(key) ? json.get(key).getAsInt() : fallback; }
		catch (RuntimeException e) { return fallback; }
	}

	static double decimal(JsonObject json, String key, double fallback) {
		try { return json.has(key) ? json.get(key).getAsDouble() : fallback; }
		catch (RuntimeException e) { return fallback; }
	}
}
