package zone.moddev.mc.mineralogy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

/** Mineralogy content settings. OreSpawn owns all terrain and deposit settings. */
public final class MineralogyConfig {
	public static final ForgeConfigSpec SPEC;

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

	private static boolean smeltableGravel = true;
	private static boolean dropCobblestone;
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
	private static boolean groupCreativeTabItemsByType;
	private static boolean recipeConditionsRegistered;
	private static boolean advancementPredicatesRegistered;

	private static final ResourceLocation CONFIG_CONDITION_ID =
			new ResourceLocation(Mineralogy.MODID, "config");

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("options");
		PATCH_UPDATE = builder
				.comment("Allow compatibility patching for worlds created by older Mineralogy versions.")
				.define("patch_world", patchUpdate);
		SMELTABLE_GRAVEL = builder
				.comment("Allow gravel to be smelted into generic stone. Recipe changes require a restart.")
				.define("SMELTABLE_GRAVEL", smeltableGravel);
		DROP_COBBLESTONE = builder
				.comment("Make ordinary rock blocks also drop cobblestone.")
				.define("DROP_COBBLESTONE", dropCobblestone);
		COBBLESTONE_EQUIVILENT = builder
				.comment("Treat rock blocks as cobblestone equivalents where supported.")
				.define("COBBLESTONE_EQUIVILENT", makeRockCobblestoneEquivilent);
		GENERATE_RELIEFS = flag(builder, "GENERATE_RELIEFS", "Register rock reliefs.", generateReliefs);
		GENERATE_ROCKSTAIRS = flag(builder, "GENERATE_ROCKSTAIRS", "Register rock stairs.", generateRockStairs);
		GENERATE_ROCKFURNACE = flag(builder, "GENERATE_ROCKFURNACE", "Register rock furnaces.", generateRockFurnace);
		GENERATE_ROCKSLAB = flag(builder, "GENERATE_ROCKSLAB", "Register rock slabs.", generateRockSlab);
		GENERATE_ROCKWALL = flag(builder, "GENERATE_ROCKWALL", "Register rock walls.", generateRockWall);
		GENERATE_BRICK = flag(builder, "GENERATE_BRICK", "Register rock bricks.", generateBrick);
		GENERATE_BRICKFURNACE = flag(builder, "GENERATE_BRICKFURNACE", "Register brick furnaces.", generateBrickFurnace);
		GENERATE_BRICKSTAIRS = flag(builder, "GENERATE_BRICKSTAIRS", "Register brick stairs.", generateBrickStairs);
		GENERATE_BRICKSLAB = flag(builder, "GENERATE_BRICKSLAB", "Register brick slabs.", generateBrickSlab);
		GENERATE_BRICKWALL = flag(builder, "GENERATE_BRICKWALL", "Register brick walls.", generateBrickWall);
		GENERATE_SMOOTH = flag(builder, "GENERATE_SMOOTH", "Register polished rock.", generateSmooth);
		GENERATE_SMOOTHFURNACE = flag(builder, "GENERATE_SMOOTHFURNACE", "Register polished furnaces.", generateSmoothFurnace);
		GENERATE_SMOOTHSTAIRS = flag(builder, "GENERATE_SMOOTHSTAIRS", "Register polished stairs.", generateSmoothStairs);
		GENERATE_SMOOTHSLAB = flag(builder, "GENERATE_SMOOTHSLAB", "Register polished slabs.", generateSmoothSlab);
		GENERATE_SMOOTHWALL = flag(builder, "GENERATE_SMOOTHWALL", "Register polished walls.", generateSmoothWall);
		GENERATE_SMOOTHBRICK = flag(builder, "GENERATE_SMOOTHBRICK", "Register polished bricks.", generateSmoothBrick);
		GENERATE_SMOOTHBRICKFURNACE = flag(builder, "GENERATE_SMOOTHBRICKFURNACE", "Register polished brick furnaces.", generateSmoothBrickFurnace);
		GENERATE_SMOOTHBRICKSTAIRS = flag(builder, "GENERATE_SMOOTHBRICKSTAIRS", "Register polished brick stairs.", generateSmoothBrickStairs);
		GENERATE_SMOOTHBRICKSLAB = flag(builder, "GENERATE_SMOOTHBRICKSLAB", "Register polished brick slabs.", generateSmoothBrickSlab);
		GENERATE_SMOOTHBRICKWALL = flag(builder, "GENERATE_SMOOTHBRICKWALL", "Register polished brick walls.", generateSmoothBrickWall);
		GROUP_TABS_BY_TYPE = builder
				.comment("Split creative tabs by item type where supported.")
				.define("GROUP_TABS_BY_TYPE", groupCreativeTabItemsByType);
		builder.pop();
		SPEC = builder.build();
	}

	private MineralogyConfig() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	private static ForgeConfigSpec.BooleanValue flag(ForgeConfigSpec.Builder builder,
			String key, String comment, boolean value) {
		return builder.comment(comment).define(key, value);
	}

	public static void register() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC,
				Mineralogy.MODID + "-common.toml");
	}

	public static void registerRecipeConditions() {
		if (!recipeConditionsRegistered) {
			CraftingHelper.register(new ConfigConditionSerializer());
			recipeConditionsRegistered = true;
		}
	}

	public static void registerAdvancementPredicates() {
		if (!advancementPredicatesRegistered) {
			ItemPredicate.register(new ResourceLocation(Mineralogy.MODID, "config_item"),
					MineralogyConfig::configItemPredicate);
			advancementPredicatesRegistered = true;
		}
	}

	public static void bake() {
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
		return new ConfigItemPredicate(flags,
				new ResourceLocation(GsonHelper.getAsString(json, "item")));
	}

	private static BooleanSupplier configFlagCondition(String name) {
		switch (name) {
		case "SMELTABLE_GRAVEL": return configFlag(() -> smeltableGravel);
		case "GENERATE_RELIEFS": return configFlag(() -> generateReliefs);
		case "GENERATE_ROCKSTAIRS": return configFlag(() -> generateRockStairs);
		case "GENERATE_ROCKFURNACE": return configFlag(() -> generateRockFurnace);
		case "GENERATE_ROCKSLAB": return configFlag(() -> generateRockSlab);
		case "GENERATE_ROCKWALL": return configFlag(() -> generateRockWall);
		case "GENERATE_BRICK": return configFlag(() -> generateBrick);
		case "GENERATE_BRICKFURNACE": return configFlag(() -> generateBrickFurnace);
		case "GENERATE_BRICKSTAIRS": return configFlag(() -> generateBrickStairs);
		case "GENERATE_BRICKSLAB": return configFlag(() -> generateBrickSlab);
		case "GENERATE_BRICKWALL": return configFlag(() -> generateBrickWall);
		case "GENERATE_SMOOTH": return configFlag(() -> generateSmooth);
		case "GENERATE_SMOOTHFURNACE": return configFlag(() -> generateSmoothFurnace);
		case "GENERATE_SMOOTHSTAIRS": return configFlag(() -> generateSmoothStairs);
		case "GENERATE_SMOOTHSLAB": return configFlag(() -> generateSmoothSlab);
		case "GENERATE_SMOOTHWALL": return configFlag(() -> generateSmoothWall);
		case "GENERATE_SMOOTHBRICK": return configFlag(() -> generateSmoothBrick);
		case "GENERATE_SMOOTHBRICKFURNACE": return configFlag(() -> generateSmoothBrickFurnace);
		case "GENERATE_SMOOTHBRICKSTAIRS": return configFlag(() -> generateSmoothBrickStairs);
		case "GENERATE_SMOOTHBRICKSLAB": return configFlag(() -> generateSmoothBrickSlab);
		case "GENERATE_SMOOTHBRICKWALL": return configFlag(() -> generateSmoothBrickWall);
		default: throw new JsonSyntaxException("Unknown Mineralogy recipe config flag: " + name);
		}
	}

	private static BooleanSupplier configFlag(BooleanSupplier flag) {
		return () -> {
			bakeIfConfigured();
			return flag.getAsBoolean();
		};
	}

	private static void bakeIfConfigured() {
		try {
			bake();
		} catch (NullPointerException e) {
			if (!isEarlyConfigAccess(e)) throw e;
		}
	}

	private static boolean isEarlyConfigAccess(NullPointerException e) {
		for (StackTraceElement element : e.getStackTrace()) {
			if ("net.minecraftforge.common.ForgeConfigSpec$ConfigValue".equals(element.getClassName())
					&& "get".equals(element.getMethodName())) return true;
		}
		return false;
	}

	public static boolean smeltableGravel() { return smeltableGravel; }
	public static boolean dropCobblestone() { return dropCobblestone; }
	public static boolean makeRockCobblestoneEquivilent() { return makeRockCobblestoneEquivilent; }
	public static boolean generateRockFurnace() { return generateRockFurnace; }
	public static boolean generateRockStairs() { return generateRockStairs; }
	public static boolean generateReliefs() { return generateReliefs; }
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

	public static boolean patchUpdate() {
		try {
			patchUpdate = PATCH_UPDATE.get();
		} catch (NullPointerException e) {
			if (!isEarlyConfigAccess(e)) throw e;
		}
		return patchUpdate;
	}

	public static boolean groupCreativeTabItemsByType() {
		try {
			groupCreativeTabItemsByType = GROUP_TABS_BY_TYPE.get();
		} catch (NullPointerException e) {
			if (!isEarlyConfigAccess(e)) throw e;
		}
		return groupCreativeTabItemsByType;
	}

	private static final class ConfigCondition implements ICondition {
		private final String flagName;
		private final BooleanSupplier flag;

		private ConfigCondition(String flagName) {
			this.flagName = flagName;
			this.flag = configFlagCondition(flagName);
		}

		@Override public ResourceLocation getID() { return CONFIG_CONDITION_ID; }
		@Override public boolean test() { return flag.getAsBoolean(); }
	}

	private static final class ConfigConditionSerializer
			implements IConditionSerializer<ConfigCondition> {
		@Override public void write(JsonObject json, ConfigCondition value) {
			json.addProperty("flag", value.flagName);
		}
		@Override public ConfigCondition read(JsonObject json) {
			return new ConfigCondition(GsonHelper.getAsString(json, "flag"));
		}
		@Override public ResourceLocation getID() { return CONFIG_CONDITION_ID; }
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
			for (BooleanSupplier flag : flags) if (!flag.getAsBoolean()) return false;
			Item item = ForgeRegistries.ITEMS.getValue(itemName);
			return item != null && stack.getItem() == item;
		}
	}
}
