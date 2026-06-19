package com.mcmoddev.mineralogy;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

public final class MineralogyConfig {
	public static final ForgeConfigSpec SPEC;

	private static final ForgeConfigSpec.DoubleValue ROCK_LAYER_NOISE;
	private static final ForgeConfigSpec.IntValue GEOME_SIZE;
	private static final ForgeConfigSpec.IntValue ROCK_LAYER_THICKNESS;
	private static final ForgeConfigSpec.EnumValue<GeologyMode> GEOLOGY_MODE;
	private static final ForgeConfigSpec.BooleanValue PLACE_MINERALOGY_ROCK;
	private static final ForgeConfigSpec.BooleanValue SMELTABLE_GRAVEL;
	private static final ForgeConfigSpec.BooleanValue DROP_COBBLESTONE;
	private static final ForgeConfigSpec.BooleanValue PATCH_UPDATE;
	private static final ForgeConfigSpec.BooleanValue COBBLESTONE_EQUIVILENT;
	private static final ForgeConfigSpec.BooleanValue GENERATE_RELIEFS;
	private static final ForgeConfigSpec.BooleanValue GENERATE_ROCKSTAIRS;
	private static final ForgeConfigSpec.BooleanValue GENERATE_ROCKFURNACE;
	private static final ForgeConfigSpec.BooleanValue GENERATE_ROCKSLAB;
	private static final ForgeConfigSpec.BooleanValue GENERATE_ROCKWALL;
	private static final ForgeConfigSpec.BooleanValue GENERATE_BRICK;
	private static final ForgeConfigSpec.BooleanValue GENERATE_BRICKFURNACE;
	private static final ForgeConfigSpec.BooleanValue GENERATE_BRICKSTAIRS;
	private static final ForgeConfigSpec.BooleanValue GENERATE_BRICKSLAB;
	private static final ForgeConfigSpec.BooleanValue GENERATE_BRICKWALL;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTH;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHFURNACE;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHSTAIRS;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHSLAB;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHWALL;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHBRICK;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHBRICKFURNACE;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHBRICKSTAIRS;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHBRICKSLAB;
	private static final ForgeConfigSpec.BooleanValue GENERATE_SMOOTHBRICKWALL;
	private static final ForgeConfigSpec.BooleanValue GROUP_TABS_BY_TYPE;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNEOUS_BLACKLIST;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SEDIMENTARY_BLACKLIST;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_BLACKLIST;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNEOUS_WHITELIST;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SEDIMENTARY_WHITELIST;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_WHITELIST;
	private static final OreConfigSpec SULFUR_ORE;
	private static final OreConfigSpec PHOSPHOROUS_ORE;
	private static final OreConfigSpec NITRATE_ORE;

	private static double rockLayerNoise = 32.0D;
	private static int geomeSize = 100;
	private static int geomLayerThickness = 8;
	private static GeologyMode geologyMode = GeologyMode.GEOME;
	private static boolean placeMineralogyRock = true;
	private static boolean smeltableGravel = true;
	private static boolean dropCobblestone = false;
	private static boolean patchUpdate = true;
	private static boolean makeRockCobblestoneEquivilent = true;
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
	private static boolean groupCreativeTabItemsByType = false;
	private static List<String> igneousWhitelist = Collections.emptyList();
	private static List<String> igneousBlacklist = Collections.emptyList();
	private static List<String> sedimentaryWhitelist = Collections.emptyList();
	private static List<String> sedimentaryBlacklist = Collections.emptyList();
	private static List<String> metamorphicWhitelist = Collections.emptyList();
	private static List<String> metamorphicBlacklist = Collections.emptyList();
	private static OreGenerationSettings sulfurOre = new OreGenerationSettings(16, 64, 1.0D, 16);
	private static OreGenerationSettings phosphorousOre = new OreGenerationSettings(16, 64, 1.0D, 16);
	private static OreGenerationSettings nitrateOre = new OreGenerationSettings(16, 64, 1.0D, 16);
	private static boolean recipeConditionsRegistered = false;
	private static boolean advancementPredicatesRegistered = false;
	private static final ResourceLocation CONFIG_CONDITION_ID = new ResourceLocation(Mineralogy.MODID, "config");

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.push("options");
		PLACE_MINERALOGY_ROCK = builder
				.comment("If true, then generic stone is replaced with Mineralogy rocks.")
				.define("PLACE_MINERALOGY_ROCK", placeMineralogyRock);
		PATCH_UPDATE = builder
				.comment("If true, then the world patching path may fix compatibility-breaking changes from older versions.")
				.define("patch_world", patchUpdate);
		SMELTABLE_GRAVEL = builder
				.comment("If true, then gravel can be smelted into generic stone. Data recipes may need a restart after changing this.")
				.define("SMELTABLE_GRAVEL", smeltableGravel);
		DROP_COBBLESTONE = builder
				.comment("If true, then ordinary rock blocks also drop cobblestone.")
				.define("DROP_COBBLESTONE", dropCobblestone);
		COBBLESTONE_EQUIVILENT = builder
				.comment("If true, then rock blocks are treated as cobblestone equivalents where the current port supports it.")
				.define("COBBLESTONE_EQUIVILENT", makeRockCobblestoneEquivilent);
		GENERATE_RELIEFS = builder.comment("If true, then rock reliefs will be generated.")
				.define("GENERATE_RELIEFS", generateReliefs);
		GENERATE_ROCKSTAIRS = builder.comment("If true, then rock stairs will be generated.")
				.define("GENERATE_ROCKSTAIRS", generateRockStairs);
		GENERATE_ROCKFURNACE = builder.comment("If true, then rock furnaces will be generated.")
				.define("GENERATE_ROCKFURNACE", generateRockFurnace);
		GENERATE_ROCKSLAB = builder.comment("If true, then rock slabs will be generated.")
				.define("GENERATE_ROCKSLAB", generateRockSlab);
		GENERATE_ROCKWALL = builder.comment("If true, then rock walls will be generated.")
				.define("GENERATE_ROCKWALL", generateRockWall);
		GENERATE_BRICK = builder.comment("If true, then rock brick blocks will be generated.")
				.define("GENERATE_BRICK", generateBrick);
		GENERATE_BRICKFURNACE = builder.comment("If true, then brick furnaces will be generated.")
				.define("GENERATE_BRICKFURNACE", generateBrickFurnace);
		GENERATE_BRICKSTAIRS = builder.comment("If true, then brick stairs will be generated.")
				.define("GENERATE_BRICKSTAIRS", generateBrickStairs);
		GENERATE_BRICKSLAB = builder.comment("If true, then brick slabs will be generated.")
				.define("GENERATE_BRICKSLAB", generateBrickSlab);
		GENERATE_BRICKWALL = builder.comment("If true, then brick walls will be generated.")
				.define("GENERATE_BRICKWALL", generateBrickWall);
		GENERATE_SMOOTH = builder.comment("If true, then polished rock will be generated.")
				.define("GENERATE_SMOOTH", generateSmooth);
		GENERATE_SMOOTHFURNACE = builder.comment("If true, then smooth furnaces will be generated.")
				.define("GENERATE_SMOOTHFURNACE", generateSmoothFurnace);
		GENERATE_SMOOTHSTAIRS = builder.comment("If true, then polished rock stairs will be generated.")
				.define("GENERATE_SMOOTHSTAIRS", generateSmoothStairs);
		GENERATE_SMOOTHSLAB = builder.comment("If true, then polished rock slabs will be generated.")
				.define("GENERATE_SMOOTHSLAB", generateSmoothSlab);
		GENERATE_SMOOTHWALL = builder.comment("If true, then polished walls will be generated.")
				.define("GENERATE_SMOOTHWALL", generateSmoothWall);
		GENERATE_SMOOTHBRICK = builder.comment("If true, then polished brick blocks will be generated.")
				.define("GENERATE_SMOOTHBRICK", generateSmoothBrick);
		GENERATE_SMOOTHBRICKFURNACE = builder.comment("If true, then smooth brick furnaces will be generated.")
				.define("GENERATE_SMOOTHBRICKFURNACE", generateSmoothBrickFurnace);
		GENERATE_SMOOTHBRICKSTAIRS = builder.comment("If true, then polished brick stairs will be generated.")
				.define("GENERATE_SMOOTHBRICKSTAIRS", generateSmoothBrickStairs);
		GENERATE_SMOOTHBRICKSLAB = builder.comment("If true, then polished brick slabs will be generated.")
				.define("GENERATE_SMOOTHBRICKSLAB", generateSmoothBrickSlab);
		GENERATE_SMOOTHBRICKWALL = builder.comment("If true, then polished brick walls will be generated.")
				.define("GENERATE_SMOOTHBRICKWALL", generateSmoothBrickWall);
		GROUP_TABS_BY_TYPE = builder.comment("If true, then creative tabs will be split by item type where supported.")
				.define("GROUP_TABS_BY_TYPE", groupCreativeTabItemsByType);
		builder.pop();

		builder.push("world-gen");
		GEOME_SIZE = builder
				.comment("Making this value larger increases the size of regions of igneous, sedimentary, and metamorphic rocks.")
				.defineInRange("GEOME_SIZE", geomeSize, 4, Short.MAX_VALUE);
		GEOLOGY_MODE = builder
				.comment("Controls Mineralogy stone replacement. GEOME is the biome-influenced geology model; LEGACY is the old random layer model.")
				.defineEnum("GEOLOGY_MODE", geologyMode);
		ROCK_LAYER_NOISE = builder
				.comment("Changing this value will change the waviness of the layers.")
				.defineInRange("ROCK_LAYER_NOISE", rockLayerNoise, 1.0D, Short.MAX_VALUE);
		ROCK_LAYER_THICKNESS = builder
				.comment("Changing this value will change the height of individual layers.")
				.defineInRange("ROCK_LAYER_THICKNESS", geomLayerThickness, 1, 255);
		IGNEOUS_BLACKLIST = builder.comment("Ban blocks from spawning in igneous rock layers. Use mod:block entries.")
				.defineList("igneous_blacklist", Collections.emptyList(), MineralogyConfig::isString);
		SEDIMENTARY_BLACKLIST = builder.comment("Ban blocks from spawning in sedimentary rock layers. Use mod:block entries.")
				.defineList("sedimentary_blacklist", Collections.emptyList(), MineralogyConfig::isString);
		METAMORPHIC_BLACKLIST = builder.comment("Ban blocks from spawning in metamorphic rock layers. Use mod:block entries.")
				.defineList("metamorphic_blacklist", Collections.emptyList(), MineralogyConfig::isString);
		IGNEOUS_WHITELIST = builder.comment("Add blocks to igneous rock layers. Use mod:block entries.")
				.defineList("igneous_whitelist", Collections.emptyList(), MineralogyConfig::isString);
		SEDIMENTARY_WHITELIST = builder.comment("Add blocks to sedimentary rock layers. Use mod:block entries.")
				.defineList("sedimentary_whitelist", Collections.emptyList(), MineralogyConfig::isString);
		METAMORPHIC_WHITELIST = builder.comment("Add blocks to metamorphic rock layers. Use mod:block entries.")
				.defineList("metamorphic_whitelist", Collections.emptyList(), MineralogyConfig::isString);
		builder.pop();

		builder.push("ores");
		SULFUR_ORE = defineOre(builder, "sulfur_ore", sulfurOre);
		PHOSPHOROUS_ORE = defineOre(builder, "phosphorous_ore", phosphorousOre);
		NITRATE_ORE = defineOre(builder, "nitrate_ore", nitrateOre);
		builder.pop();

		SPEC = builder.build();
	}

	private MineralogyConfig() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static void register() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, Mineralogy.MODID + "-common.toml");
	}

	public static void registerRecipeConditions() {
		if (recipeConditionsRegistered) {
			return;
		}

		CraftingHelper.register(new ConfigConditionSerializer());
		recipeConditionsRegistered = true;
	}

	public static void registerAdvancementPredicates() {
		if (advancementPredicatesRegistered) {
			return;
		}

		ItemPredicate.register(new ResourceLocation(Mineralogy.MODID, "config_item"),
				MineralogyConfig::configItemPredicate);
		advancementPredicatesRegistered = true;
	}

	public static void bake() {
		rockLayerNoise = ROCK_LAYER_NOISE.get();
		geomeSize = GEOME_SIZE.get();
		geomLayerThickness = ROCK_LAYER_THICKNESS.get();
		geologyMode = GEOLOGY_MODE.get();
		placeMineralogyRock = PLACE_MINERALOGY_ROCK.get();
		smeltableGravel = SMELTABLE_GRAVEL.get();
		dropCobblestone = DROP_COBBLESTONE.get();
		patchUpdate = PATCH_UPDATE.get();
		makeRockCobblestoneEquivilent = COBBLESTONE_EQUIVILENT.get();
		generateReliefs = GENERATE_RELIEFS.get();
		generateRockStairs = GENERATE_ROCKSTAIRS.get();
		generateRockFurnace = GENERATE_ROCKFURNACE.get();
		generateRockSlab = GENERATE_ROCKSLAB.get();
		generateRockWall = GENERATE_ROCKWALL.get();
		generateBrick = GENERATE_BRICK.get();
		generateBrickFurnace = GENERATE_BRICKFURNACE.get();
		generateBrickStairs = GENERATE_BRICKSTAIRS.get();
		generateBrickSlab = GENERATE_BRICKSLAB.get();
		generateBrickWall = GENERATE_BRICKWALL.get();
		generateSmooth = GENERATE_SMOOTH.get();
		generateSmoothFurnace = GENERATE_SMOOTHFURNACE.get();
		generateSmoothStairs = GENERATE_SMOOTHSTAIRS.get();
		generateSmoothSlab = GENERATE_SMOOTHSLAB.get();
		generateSmoothWall = GENERATE_SMOOTHWALL.get();
		generateSmoothBrick = GENERATE_SMOOTHBRICK.get();
		generateSmoothBrickFurnace = GENERATE_SMOOTHBRICKFURNACE.get();
		generateSmoothBrickStairs = GENERATE_SMOOTHBRICKSTAIRS.get();
		generateSmoothBrickSlab = GENERATE_SMOOTHBRICKSLAB.get();
		generateSmoothBrickWall = GENERATE_SMOOTHBRICKWALL.get();
		groupCreativeTabItemsByType = GROUP_TABS_BY_TYPE.get();
		igneousBlacklist = cleanList(IGNEOUS_BLACKLIST.get());
		sedimentaryBlacklist = cleanList(SEDIMENTARY_BLACKLIST.get());
		metamorphicBlacklist = cleanList(METAMORPHIC_BLACKLIST.get());
		igneousWhitelist = cleanList(IGNEOUS_WHITELIST.get());
		sedimentaryWhitelist = cleanList(SEDIMENTARY_WHITELIST.get());
		metamorphicWhitelist = cleanList(METAMORPHIC_WHITELIST.get());
		sulfurOre = SULFUR_ORE.bake();
		phosphorousOre = PHOSPHOROUS_ORE.bake();
		nitrateOre = NITRATE_ORE.bake();
	}

	private static OreConfigSpec defineOre(ForgeConfigSpec.Builder builder, String oreName,
			OreGenerationSettings defaults) {
		ForgeConfigSpec.IntValue minY = builder
				.comment("Minimum " + oreName + " spawn height.")
				.defineInRange(oreName + ".minY", defaults.minY(), 1, 255);
		ForgeConfigSpec.IntValue maxY = builder
				.comment("Maximum " + oreName + " spawn height.")
				.defineInRange(oreName + ".maxY", defaults.maxY(), 1, 255);
		ForgeConfigSpec.DoubleValue frequency = builder
				.comment("Number of " + oreName + " deposits per chunk. Fractional values are supported.")
				.defineInRange(oreName + ".frequency", defaults.frequency(), 0.0D, 63.0D);
		ForgeConfigSpec.IntValue quantity = builder
				.comment("Size of " + oreName + " deposit.")
				.defineInRange(oreName + ".quantity", defaults.quantity(), 0, 63);

		return new OreConfigSpec(minY, maxY, frequency, quantity);
	}

	private static boolean isString(Object value) {
		return value instanceof String;
	}

	private static List<String> cleanList(List<? extends String> entries) {
		return entries.stream()
				.map(String::trim)
				.filter(entry -> !entry.isEmpty())
				.collect(Collectors.toList());
	}

	private static ItemPredicate configItemPredicate(JsonObject json) {
		List<BooleanSupplier> flags = new ArrayList<>();
		if (json.has("flags")) {
			JsonArray flagArray = GsonHelper.getAsJsonArray(json, "flags");
			for (JsonElement flag : flagArray) {
				flags.add(configFlagCondition(GsonHelper.convertToString(flag, "flag")));
			}
		} else {
			flags.add(configFlagCondition(GsonHelper.getAsString(json, "flag")));
		}

		return new ConfigItemPredicate(flags, new ResourceLocation(GsonHelper.getAsString(json, "item")));
	}

	private static BooleanSupplier configFlagCondition(String flag) {
		switch (flag) {
			case "SMELTABLE_GRAVEL":
				return configFlag(() -> smeltableGravel);
			case "GENERATE_RELIEFS":
				return configFlag(() -> generateReliefs);
			case "GENERATE_ROCKSTAIRS":
				return configFlag(() -> generateRockStairs);
			case "GENERATE_ROCKFURNACE":
				return configFlag(() -> generateRockFurnace);
			case "GENERATE_ROCKSLAB":
				return configFlag(() -> generateRockSlab);
			case "GENERATE_ROCKWALL":
				return configFlag(() -> generateRockWall);
			case "GENERATE_BRICK":
				return configFlag(() -> generateBrick);
			case "GENERATE_BRICKFURNACE":
				return configFlag(() -> generateBrickFurnace);
			case "GENERATE_BRICKSTAIRS":
				return configFlag(() -> generateBrickStairs);
			case "GENERATE_BRICKSLAB":
				return configFlag(() -> generateBrickSlab);
			case "GENERATE_BRICKWALL":
				return configFlag(() -> generateBrickWall);
			case "GENERATE_SMOOTH":
				return configFlag(() -> generateSmooth);
			case "GENERATE_SMOOTHFURNACE":
				return configFlag(() -> generateSmoothFurnace);
			case "GENERATE_SMOOTHSTAIRS":
				return configFlag(() -> generateSmoothStairs);
			case "GENERATE_SMOOTHSLAB":
				return configFlag(() -> generateSmoothSlab);
			case "GENERATE_SMOOTHWALL":
				return configFlag(() -> generateSmoothWall);
			case "GENERATE_SMOOTHBRICK":
				return configFlag(() -> generateSmoothBrick);
			case "GENERATE_SMOOTHBRICKFURNACE":
				return configFlag(() -> generateSmoothBrickFurnace);
			case "GENERATE_SMOOTHBRICKSTAIRS":
				return configFlag(() -> generateSmoothBrickStairs);
			case "GENERATE_SMOOTHBRICKSLAB":
				return configFlag(() -> generateSmoothBrickSlab);
			case "GENERATE_SMOOTHBRICKWALL":
				return configFlag(() -> generateSmoothBrickWall);
			default:
				throw new JsonSyntaxException("Unknown Mineralogy recipe config flag: " + flag);
		}
	}

	private static BooleanSupplier configFlag(BooleanSupplier flag) {
		return () -> {
			bakeIfConfigured();
			return flag.getAsBoolean();
		};
	}

	private static final class ConfigCondition implements ICondition {
		private final String flagName;
		private final BooleanSupplier flag;

		private ConfigCondition(String flagName) {
			this.flagName = flagName;
			this.flag = configFlagCondition(flagName);
		}

		@Override
		public ResourceLocation getID() {
			return CONFIG_CONDITION_ID;
		}

		@Override
		public boolean test() {
			return flag.getAsBoolean();
		}
	}

	private static final class ConfigConditionSerializer implements IConditionSerializer<ConfigCondition> {
		@Override
		public void write(JsonObject json, ConfigCondition value) {
			json.addProperty("flag", value.flagName);
		}

		@Override
		public ConfigCondition read(JsonObject json) {
			return new ConfigCondition(GsonHelper.getAsString(json, "flag"));
		}

		@Override
		public ResourceLocation getID() {
			return CONFIG_CONDITION_ID;
		}
	}

	private static void bakeIfConfigured() {
		try {
			bake();
		} catch (NullPointerException e) {
			if (!isEarlyConfigAccess(e)) {
				throw e;
			}
		}
	}

	private static boolean isEarlyConfigAccess(NullPointerException e) {
		for (StackTraceElement element : e.getStackTrace()) {
			if ("net.minecraftforge.common.ForgeConfigSpec$ConfigValue".equals(element.getClassName())
					&& "get".equals(element.getMethodName())) {
				return true;
			}
		}
		return false;
	}

	public static double rockLayerNoise() {
		return rockLayerNoise;
	}

	public static int geomeSize() {
		return geomeSize;
	}

	public static int geomLayerThickness() {
		return geomLayerThickness;
	}

	public static GeologyMode geologyMode() {
		return geologyMode;
	}

	public static boolean placeMineralogyRock() {
		return placeMineralogyRock;
	}

	public static boolean smeltableGravel() {
		return smeltableGravel;
	}

	public static boolean dropCobblestone() {
		return dropCobblestone;
	}

	public static boolean makeRockCobblestoneEquivilent() {
		return makeRockCobblestoneEquivilent;
	}

	public static boolean patchUpdate() {
		try {
			patchUpdate = PATCH_UPDATE.get();
		} catch (NullPointerException e) {
			if (!isEarlyConfigAccess(e)) {
				throw e;
			}
		}
		return patchUpdate;
	}

	public static boolean generateRockFurnace() {
		return generateRockFurnace;
	}

	public static boolean generateRockStairs() {
		return generateRockStairs;
	}

	public static boolean generateReliefs() {
		return generateReliefs;
	}

	public static boolean generateRockSlab() {
		return generateRockSlab;
	}

	public static boolean generateRockWall() {
		return generateRockWall;
	}

	public static boolean generateBrick() {
		return generateBrick;
	}

	public static boolean generateBrickFurnace() {
		return generateBrickFurnace;
	}

	public static boolean generateBrickStairs() {
		return generateBrickStairs;
	}

	public static boolean generateBrickSlab() {
		return generateBrickSlab;
	}

	public static boolean generateBrickWall() {
		return generateBrickWall;
	}

	public static boolean generateSmooth() {
		return generateSmooth;
	}

	public static boolean generateSmoothFurnace() {
		return generateSmoothFurnace;
	}

	public static boolean generateSmoothStairs() {
		return generateSmoothStairs;
	}

	public static boolean generateSmoothSlab() {
		return generateSmoothSlab;
	}

	public static boolean generateSmoothWall() {
		return generateSmoothWall;
	}

	public static boolean generateSmoothBrick() {
		return generateSmoothBrick;
	}

	public static boolean generateSmoothBrickFurnace() {
		return generateSmoothBrickFurnace;
	}

	public static boolean generateSmoothBrickStairs() {
		return generateSmoothBrickStairs;
	}

	public static boolean generateSmoothBrickSlab() {
		return generateSmoothBrickSlab;
	}

	public static boolean generateSmoothBrickWall() {
		return generateSmoothBrickWall;
	}

	public static List<String> igneousWhitelist() {
		return igneousWhitelist;
	}

	public static List<String> igneousBlacklist() {
		return igneousBlacklist;
	}

	public static List<String> sedimentaryWhitelist() {
		return sedimentaryWhitelist;
	}

	public static List<String> sedimentaryBlacklist() {
		return sedimentaryBlacklist;
	}

	public static List<String> metamorphicWhitelist() {
		return metamorphicWhitelist;
	}

	public static List<String> metamorphicBlacklist() {
		return metamorphicBlacklist;
	}

	public static boolean groupCreativeTabItemsByType() {
		try {
			groupCreativeTabItemsByType = GROUP_TABS_BY_TYPE.get();
		} catch (NullPointerException e) {
			if (!isEarlyConfigAccess(e)) {
				throw e;
			}
		}
		return groupCreativeTabItemsByType;
	}

	public static OreGenerationSettings sulfurOre() {
		return sulfurOre;
	}

	public static OreGenerationSettings phosphorousOre() {
		return phosphorousOre;
	}

	public static OreGenerationSettings nitrateOre() {
		return nitrateOre;
	}

	private static final class OreConfigSpec {
		private final ForgeConfigSpec.IntValue minY;
		private final ForgeConfigSpec.IntValue maxY;
		private final ForgeConfigSpec.DoubleValue frequency;
		private final ForgeConfigSpec.IntValue quantity;

		private OreConfigSpec(ForgeConfigSpec.IntValue minY, ForgeConfigSpec.IntValue maxY,
				ForgeConfigSpec.DoubleValue frequency, ForgeConfigSpec.IntValue quantity) {
			this.minY = minY;
			this.maxY = maxY;
			this.frequency = frequency;
			this.quantity = quantity;
		}

		private OreGenerationSettings bake() {
			return new OreGenerationSettings(minY.get(), maxY.get(), frequency.get(), quantity.get());
		}
	}

	public static final class OreGenerationSettings {
		private final int minY;
		private final int maxY;
		private final double frequency;
		private final int quantity;

		private OreGenerationSettings(int minY, int maxY, double frequency, int quantity) {
			this.minY = minY;
			this.maxY = maxY;
			this.frequency = frequency;
			this.quantity = quantity;
		}

		public int minY() {
			return minY;
		}

		public int maxY() {
			return maxY;
		}

		public double frequency() {
			return frequency;
		}

		public int quantity() {
			return quantity;
		}
	}

	private static final class ConfigItemPredicate extends ItemPredicate {
		private final List<BooleanSupplier> flags;
		private final ResourceLocation itemName;

		private ConfigItemPredicate(List<BooleanSupplier> flags, ResourceLocation itemName) {
			this.flags = flags;
			this.itemName = itemName;
		}

		@Override
		public boolean matches(ItemStack stack) {
			for (BooleanSupplier flag : flags) {
				if (!flag.getAsBoolean()) {
					return false;
				}
			}

			Item item = ForgeRegistries.ITEMS.getValue(itemName);
			return item != null && stack.getItem() == item;
		}
	}

	public static enum GeologyMode {
		GEOME,
		LEGACY
	}
}
