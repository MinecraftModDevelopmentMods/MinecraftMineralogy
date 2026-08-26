package zone.moddev.mc.mineralogy.patching;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts pre-namespaced tile-entity identifiers into values Forge 28 can
 * safely parse. Minecraft 1.14 constructs a ResourceLocation before it looks
 * up the tile type, so an otherwise harmless missing legacy tile such as
 * {@code Chest} aborts the complete chunk future because of its uppercase
 * character.
 */
final class LegacyTileEntityIdNormalizer {
	private static final Map<String, String> VANILLA_IDS;

	static {
		Map<String, String> ids = new HashMap<>();
		ids.put("Furnace", "minecraft:furnace");
		ids.put("Chest", "minecraft:chest");
		ids.put("EnderChest", "minecraft:ender_chest");
		ids.put("RecordPlayer", "minecraft:jukebox");
		ids.put("Trap", "minecraft:dispenser");
		ids.put("Dropper", "minecraft:dropper");
		ids.put("Sign", "minecraft:sign");
		ids.put("MobSpawner", "minecraft:mob_spawner");
		ids.put("Music", "minecraft:noteblock");
		ids.put("Piston", "minecraft:piston");
		ids.put("Cauldron", "minecraft:brewing_stand");
		ids.put("EnchantTable", "minecraft:enchanting_table");
		ids.put("Airportal", "minecraft:end_portal");
		ids.put("Control", "minecraft:command_block");
		ids.put("Beacon", "minecraft:beacon");
		ids.put("Skull", "minecraft:skull");
		ids.put("DLDetector", "minecraft:daylight_detector");
		ids.put("Hopper", "minecraft:hopper");
		ids.put("Comparator", "minecraft:comparator");
		ids.put("FlowerPot", "minecraft:flower_pot");
		ids.put("Banner", "minecraft:banner");
		ids.put("Structure", "minecraft:structure_block");
		ids.put("EndGateway", "minecraft:end_gateway");
		ids.put("Bed", "minecraft:bed");
		ids.put("ShulkerBox", "minecraft:shulker_box");
		VANILLA_IDS = Collections.unmodifiableMap(ids);
	}

	private LegacyTileEntityIdNormalizer() {
	}

	static String normalize(String id) {
		String vanilla = VANILLA_IDS.get(id);
		if (vanilla != null) {
			return vanilla;
		}
		if (isValid(id)) {
			return id;
		}

		int separator = id.indexOf(':');
		String namespace = separator >= 0 ? id.substring(0, separator) : "minecraft";
		String path = separator >= 0 ? id.substring(separator + 1) : id;
		return sanitize(namespace) + ':' + sanitize(path);
	}

	private static boolean isValid(String id) {
		if (id == null || id.isEmpty()) {
			return false;
		}
		int separator = id.indexOf(':');
		if (separator != id.lastIndexOf(':')) {
			return false;
		}
		String namespace = separator >= 0 ? id.substring(0, separator) : "minecraft";
		String path = separator >= 0 ? id.substring(separator + 1) : id;
		return !namespace.isEmpty() && !path.isEmpty()
				&& validPart(namespace, false) && validPart(path, true);
	}

	private static boolean validPart(String value, boolean path) {
		for (int index = 0; index < value.length(); ++index) {
			char character = value.charAt(index);
			boolean valid = character >= 'a' && character <= 'z'
					|| character >= '0' && character <= '9'
					|| character == '_' || character == '-' || character == '.'
					|| path && character == '/';
			if (!valid) {
				return false;
			}
		}
		return true;
	}

	private static String sanitize(String value) {
		String lower = value == null ? "" : value.toLowerCase(Locale.ROOT);
		StringBuilder sanitized = new StringBuilder(lower.length());
		for (int index = 0; index < lower.length(); ++index) {
			char character = lower.charAt(index);
			boolean valid = character >= 'a' && character <= 'z'
					|| character >= '0' && character <= '9'
					|| character == '_' || character == '-' || character == '.'
					|| character == '/';
			sanitized.append(valid ? character : '_');
		}
		return sanitized.length() == 0 ? "unknown" : sanitized.toString();
	}
}
