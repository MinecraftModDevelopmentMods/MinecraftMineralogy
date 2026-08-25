package zone.moddev.mc.mineralogy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Content-only Mineralogy configuration.
 *
 * <p>Existing Mineralogy 5 TOML files are read without being rewritten because
 * OreSpawn uses their retired geology and ore values during world migration.</p>
 */
public final class MineralogyConfig {
    public static final String FILE_NAME = "mineralogy-common.toml";

    private static boolean smeltableGravel = true;
    private static boolean dropCobblestone;
    private static boolean patchUpdate = true;
    private static boolean cobblestoneEquivalent = true;
    private static boolean generateReliefs = true;
    private static boolean generateRockStairs = true;
    private static boolean generateRockFurnace = true;
    private static boolean generateRockSlab = true;
    private static boolean generateRockWall = true;
    private static boolean generateBrick = true;
    private static boolean generateBrickFurnace = true;
    private static boolean generateBrickStairs = true;
    private static boolean generateBrickSlab = true;
    private static boolean generateBrickWall = true;
    private static boolean generateSmooth = true;
    private static boolean generateSmoothFurnace = true;
    private static boolean generateSmoothStairs = true;
    private static boolean generateSmoothSlab = true;
    private static boolean generateSmoothWall = true;
    private static boolean generateSmoothBrick = true;
    private static boolean generateSmoothBrickFurnace = true;
    private static boolean generateSmoothBrickStairs = true;
    private static boolean generateSmoothBrickSlab = true;
    private static boolean generateSmoothBrickWall = true;
    private static ContentPolicy contentPolicy = ContentPolicy.defaults();
    private static CreativeTabPolicy creativeTabPolicy = new CreativeTabPolicy(false);
    private static Path configFile;
    private static boolean recipeConditionsRegistered;
    private static boolean advancementPredicatesRegistered;

    private MineralogyConfig() {
    }

    public static void load() {
        load(FMLPaths.CONFIGDIR.get());
    }

    static void load(Path configDirectory) {
        resetDefaults();
        configFile = configDirectory.resolve(FILE_NAME);
        boolean existing = Files.isRegularFile(configFile);
        Map<String, String> values = existing ? readToml(configFile) : Collections.emptyMap();

        patchUpdate = option(values, "patch_world", patchUpdate);
        smeltableGravel = option(values, "SMELTABLE_GRAVEL", smeltableGravel);
        dropCobblestone = option(values, "DROP_COBBLESTONE", dropCobblestone);
        cobblestoneEquivalent = option(values, "COBBLESTONE_EQUIVILENT", cobblestoneEquivalent);
        generateReliefs = option(values, "GENERATE_RELIEFS", generateReliefs);
        generateRockStairs = option(values, "GENERATE_ROCKSTAIRS", generateRockStairs);
        generateRockFurnace = option(values, "GENERATE_ROCKFURNACE", generateRockFurnace);
        generateRockSlab = option(values, "GENERATE_ROCKSLAB", generateRockSlab);
        generateRockWall = option(values, "GENERATE_ROCKWALL", generateRockWall);
        generateBrick = option(values, "GENERATE_BRICK", generateBrick);
        generateBrickFurnace = option(values, "GENERATE_BRICKFURNACE", generateBrickFurnace);
        generateBrickStairs = option(values, "GENERATE_BRICKSTAIRS", generateBrickStairs);
        generateBrickSlab = option(values, "GENERATE_BRICKSLAB", generateBrickSlab);
        generateBrickWall = option(values, "GENERATE_BRICKWALL", generateBrickWall);
        generateSmooth = option(values, "GENERATE_SMOOTH", generateSmooth);
        generateSmoothFurnace = option(values, "GENERATE_SMOOTHFURNACE", generateSmoothFurnace);
        generateSmoothStairs = option(values, "GENERATE_SMOOTHSTAIRS", generateSmoothStairs);
        generateSmoothSlab = option(values, "GENERATE_SMOOTHSLAB", generateSmoothSlab);
        generateSmoothWall = option(values, "GENERATE_SMOOTHWALL", generateSmoothWall);
        generateSmoothBrick = option(values, "GENERATE_SMOOTHBRICK", generateSmoothBrick);
        generateSmoothBrickFurnace = option(values, "GENERATE_SMOOTHBRICKFURNACE", generateSmoothBrickFurnace);
        generateSmoothBrickStairs = option(values, "GENERATE_SMOOTHBRICKSTAIRS", generateSmoothBrickStairs);
        generateSmoothBrickSlab = option(values, "GENERATE_SMOOTHBRICKSLAB", generateSmoothBrickSlab);
        generateSmoothBrickWall = option(values, "GENERATE_SMOOTHBRICKWALL", generateSmoothBrickWall);

        contentPolicy = new ContentPolicy(
                option(values, ContentPolicy.ENABLE_DRYWALLS, true),
                option(values, ContentPolicy.ENABLE_ROCK_SALT_LAMPS, true),
                option(values, ContentPolicy.ENABLE_MINERAL_DUSTS, true),
                option(values, ContentPolicy.ENABLE_MINERAL_FERTILIZER, true));
        creativeTabPolicy = new CreativeTabPolicy(option(values, "GROUP_TABS_BY_TYPE", false));

        if (!existing) writeCleanConfig(configFile);
    }

    private static void resetDefaults() {
        smeltableGravel = true;
        dropCobblestone = false;
        patchUpdate = true;
        cobblestoneEquivalent = true;
        generateReliefs = true;
        generateRockStairs = true;
        generateRockFurnace = true;
        generateRockSlab = true;
        generateRockWall = true;
        generateBrick = true;
        generateBrickFurnace = true;
        generateBrickStairs = true;
        generateBrickSlab = true;
        generateBrickWall = true;
        generateSmooth = true;
        generateSmoothFurnace = true;
        generateSmoothStairs = true;
        generateSmoothSlab = true;
        generateSmoothWall = true;
        generateSmoothBrick = true;
        generateSmoothBrickFurnace = true;
        generateSmoothBrickStairs = true;
        generateSmoothBrickSlab = true;
        generateSmoothBrickWall = true;
        contentPolicy = ContentPolicy.defaults();
        creativeTabPolicy = new CreativeTabPolicy(false);
    }

    private static Map<String, String> readToml(Path file) {
        Map<String, String> values = new LinkedHashMap<>();
        String section = "";
        try {
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1).trim().toLowerCase(Locale.ROOT);
                    continue;
                }
                int equals = line.indexOf('=');
                if (equals <= 0 || line.startsWith("#")) continue;
                String key = line.substring(0, equals).trim().toLowerCase(Locale.ROOT);
                values.put(section + "." + key, stripInlineComment(line.substring(equals + 1)).trim());
            }
        } catch (IOException e) {
            Mineralogy.LOGGER.warn("Could not read {}; using compatibility-safe defaults", file, e);
        }
        return values;
    }

    private static String stripInlineComment(String value) {
        boolean quoted = false;
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '"' && !escaped) quoted = !quoted;
            if (current == '#' && !quoted) return value.substring(0, i);
            escaped = current == '\\' && !escaped;
            if (current != '\\') escaped = false;
        }
        return value;
    }

    public static Map<String, String> readLegacyValues(Path file) {
        return Collections.unmodifiableMap(readToml(file));
    }

    private static boolean option(Map<String, String> values, String name, boolean fallback) {
        String text = values.get("options." + name.toLowerCase(Locale.ROOT));
        return text == null ? fallback : Boolean.parseBoolean(text);
    }

    private static void writeCleanConfig(Path destination) {
        String content = "# Mineralogy 6 content configuration. OreSpawn owns all terrain and ore generation.\n"
                + "# Changes require a restart.\n\n[options]\n" + optionLines();
        Path temporary = destination.resolveSibling(destination.getFileName().toString() + ".tmp");
        try {
            Files.createDirectories(destination.getParent());
            Files.write(temporary, content.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination);
            }
        } catch (IOException e) {
            Mineralogy.LOGGER.warn("Could not create clean Mineralogy configuration {}", destination, e);
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
        }
    }

    private static String optionLines() {
        StringBuilder out = new StringBuilder();
        append(out, "patch_world", true);
        append(out, "SMELTABLE_GRAVEL", true);
        append(out, "DROP_COBBLESTONE", false);
        append(out, "COBBLESTONE_EQUIVILENT", true);
        append(out, "GROUP_TABS_BY_TYPE", false);
        append(out, ContentPolicy.ENABLE_DRYWALLS, true);
        append(out, ContentPolicy.ENABLE_ROCK_SALT_LAMPS, true);
        append(out, ContentPolicy.ENABLE_MINERAL_DUSTS, true);
        append(out, ContentPolicy.ENABLE_MINERAL_FERTILIZER, true);
        for (String key : new String[] { "GENERATE_RELIEFS", "GENERATE_ROCKSTAIRS",
                "GENERATE_ROCKFURNACE", "GENERATE_ROCKSLAB", "GENERATE_ROCKWALL",
                "GENERATE_BRICK", "GENERATE_BRICKFURNACE", "GENERATE_BRICKSTAIRS",
                "GENERATE_BRICKSLAB", "GENERATE_BRICKWALL", "GENERATE_SMOOTH",
                "GENERATE_SMOOTHFURNACE", "GENERATE_SMOOTHSTAIRS", "GENERATE_SMOOTHSLAB",
                "GENERATE_SMOOTHWALL", "GENERATE_SMOOTHBRICK", "GENERATE_SMOOTHBRICKFURNACE",
                "GENERATE_SMOOTHBRICKSTAIRS", "GENERATE_SMOOTHBRICKSLAB",
                "GENERATE_SMOOTHBRICKWALL" }) append(out, key, true);
        return out.toString();
    }

    private static void append(StringBuilder out, String key, boolean value) {
        out.append('\t').append(key).append(" = ").append(value).append('\n');
    }

    public static void registerRecipeConditions() {
        if (!recipeConditionsRegistered) {
            CraftingHelper.register(new ResourceLocation(Mineralogy.MODID, "config"),
                    json -> configFlagCondition(JsonUtils.getString(json, "flag")));
            CraftingHelper.register(new ResourceLocation(Mineralogy.MODID, "item_tag_not_empty"),
                    json -> itemTagNotEmptyCondition(JsonUtils.getString(json, "tag")));
            recipeConditionsRegistered = true;
        }
    }

    private static BooleanSupplier itemTagNotEmptyCondition(String name) {
        ResourceLocation tagName = new ResourceLocation(name);
        return () -> {
            Tag<Item> tag = ItemTags.getCollection().get(tagName);
            return tag != null && !tag.getAllElements().isEmpty();
        };
    }

    public static void registerAdvancementPredicates() {
        if (!advancementPredicatesRegistered) {
            ItemPredicate.register(new ResourceLocation(Mineralogy.MODID, "config_item"),
                    MineralogyConfig::configItemPredicate);
            advancementPredicatesRegistered = true;
        }
    }

    private static ItemPredicate configItemPredicate(JsonObject json) {
        List<BooleanSupplier> flags = new ArrayList<>();
        if (json.has("flags")) {
            JsonArray array = JsonUtils.getJsonArray(json, "flags");
            for (JsonElement flag : array) flags.add(configFlagCondition(JsonUtils.getString(flag, "flag")));
        } else {
            flags.add(configFlagCondition(JsonUtils.getString(json, "flag")));
        }
        return new ConfigItemPredicate(flags, new ResourceLocation(JsonUtils.getString(json, "item")));
    }

    private static BooleanSupplier configFlagCondition(String flag) {
        switch (flag) {
            case "SMELTABLE_GRAVEL": return () -> smeltableGravel;
            case "GENERATE_RELIEFS": return () -> generateReliefs;
            case "GENERATE_ROCKSTAIRS": return () -> generateRockStairs;
            case "GENERATE_ROCKFURNACE": return () -> generateRockFurnace;
            case "GENERATE_ROCKSLAB": return () -> generateRockSlab;
            case "GENERATE_ROCKWALL": return () -> generateRockWall;
            case "GENERATE_BRICK": return () -> generateBrick;
            case "GENERATE_BRICKFURNACE": return () -> generateBrickFurnace;
            case "GENERATE_BRICKSTAIRS": return () -> generateBrickStairs;
            case "GENERATE_BRICKSLAB": return () -> generateBrickSlab;
            case "GENERATE_BRICKWALL": return () -> generateBrickWall;
            case "GENERATE_SMOOTH": return () -> generateSmooth;
            case "GENERATE_SMOOTHFURNACE": return () -> generateSmoothFurnace;
            case "GENERATE_SMOOTHSTAIRS": return () -> generateSmoothStairs;
            case "GENERATE_SMOOTHSLAB": return () -> generateSmoothSlab;
            case "GENERATE_SMOOTHWALL": return () -> generateSmoothWall;
            case "GENERATE_SMOOTHBRICK": return () -> generateSmoothBrick;
            case "GENERATE_SMOOTHBRICKFURNACE": return () -> generateSmoothBrickFurnace;
            case "GENERATE_SMOOTHBRICKSTAIRS": return () -> generateSmoothBrickStairs;
            case "GENERATE_SMOOTHBRICKSLAB": return () -> generateSmoothBrickSlab;
            case "GENERATE_SMOOTHBRICKWALL": return () -> generateSmoothBrickWall;
            case ContentPolicy.ENABLE_DRYWALLS: return () -> contentPolicy.drywallsEnabled();
            case ContentPolicy.ENABLE_ROCK_SALT_LAMPS: return () -> contentPolicy.rockSaltLampsEnabled();
            case ContentPolicy.ENABLE_MINERAL_DUSTS: return () -> contentPolicy.mineralDustsEnabled();
            case ContentPolicy.ENABLE_MINERAL_FERTILIZER: return () -> contentPolicy.mineralFertilizerEnabled();
            default: throw new JsonSyntaxException("Unknown Mineralogy recipe config flag: " + flag);
        }
    }

    public static Path configFile() { return configFile; }
    public static boolean smeltableGravel() { return smeltableGravel; }
    public static boolean dropCobblestone() { return dropCobblestone; }
    public static boolean patchUpdate() { return patchUpdate; }
    public static boolean makeRockCobblestoneEquivilent() { return cobblestoneEquivalent; }
    public static boolean groupCreativeTabItemsByType() { return creativeTabPolicy.groupTabsByType(); }
    public static ContentPolicy contentPolicy() { return contentPolicy; }
    public static CreativeTabPolicy creativeTabPolicy() { return creativeTabPolicy; }
    public static boolean generateReliefs() { return generateReliefs; }
    public static boolean generateRockStairs() { return generateRockStairs; }
    public static boolean generateRockFurnace() { return generateRockFurnace; }
    public static boolean generateRockSlab() { return generateRockSlab; }
    public static boolean generateRockWall() { return generateRockWall; }
    public static boolean generateBrick() { return generateBrick; }
    public static boolean generateBrickFurnace() { return generateBrickFurnace; }
    public static boolean generateBrickStairs() { return generateBrickStairs; }
    public static boolean generateBrickSlab() { return generateBrickSlab; }
    public static boolean generateBrickWall() { return generateBrickWall; }
    public static boolean generateSmooth() { return generateSmooth; }
    public static boolean generateSmoothFurnace() { return generateSmoothFurnace; }
    public static boolean generateSmoothStairs() { return generateSmoothStairs; }
    public static boolean generateSmoothSlab() { return generateSmoothSlab; }
    public static boolean generateSmoothWall() { return generateSmoothWall; }
    public static boolean generateSmoothBrick() { return generateSmoothBrick; }
    public static boolean generateSmoothBrickFurnace() { return generateSmoothBrickFurnace; }
    public static boolean generateSmoothBrickStairs() { return generateSmoothBrickStairs; }
    public static boolean generateSmoothBrickSlab() { return generateSmoothBrickSlab; }
    public static boolean generateSmoothBrickWall() { return generateSmoothBrickWall; }

    public static boolean isCreativeVisible(String name) {
        if (name.startsWith("drywall_")) return contentPolicy.drywallsEnabled();
        if ("rocksaltlamp".equals(name) || "rocksaltstreetlamp".equals(name)) return contentPolicy.rockSaltLampsEnabled();
        if ("sulfur_dust".equals(name) || "phosphorous_dust".equals(name)
                || "nitrate_dust".equals(name) || "sulfur_block".equals(name)
                || "phosphorous_block".equals(name) || "nitrate_block".equals(name)) {
            return contentPolicy.mineralDustsEnabled();
        }
        return !"mineral_fertilizer".equals(name) || contentPolicy.mineralFertilizerEnabled();
    }

    private static final class ConfigItemPredicate extends ItemPredicate {
        private final List<BooleanSupplier> flags;
        private final ResourceLocation itemName;
        private ConfigItemPredicate(List<BooleanSupplier> flags, ResourceLocation itemName) {
            this.flags = flags;
            this.itemName = itemName;
        }
        @Override public boolean test(ItemStack stack) {
            for (BooleanSupplier flag : flags) if (!flag.getAsBoolean()) return false;
            Item item = IRegistry.field_212630_s.func_212608_b(itemName);
            return item != null && stack.getItem() == item;
        }
    }
}
