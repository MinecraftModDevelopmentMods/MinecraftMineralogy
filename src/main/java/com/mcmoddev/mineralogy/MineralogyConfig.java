package com.mcmoddev.mineralogy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class MineralogyConfig {

	private static double rockLayerNoise = 32; // size of rock layers
	private static int geomeSize = 100; // size of mineral biomes
	private static int geomLayerThickness = 8; // thickness of rock layers

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
	
	private static List<String> igneousWhitelist = new ArrayList<>();
	private static List<String> igneousBlacklist = new ArrayList<>();
	private static List<String> sedimentaryWhitelist = new ArrayList<>();
	private static List<String> sedimentaryBlacklist = new ArrayList<>();
	private static List<String> metamorphicWhitelist = new ArrayList<>();
	private static List<String> metamorphicBlacklist = new ArrayList<>();

	private static Configuration config;

	public static void preInit(FMLPreInitializationEvent event) {
		// load config
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		final String OPTIONS = "options";
		final String WORLD_GEN = "world-gen";

		placeMineralogyRock = config.getBoolean("PLACE_MINERALOGY_ROCK", OPTIONS, placeMineralogyRock,
				"If true, then generic stone is replace for mineralogy rocks");
		
		patchUpdate = config.getBoolean("patch_world", OPTIONS, patchUpdate,
				"If true, then the world will be patched to fix compatibility-breaking "
						+ "changes to this mod by adding-back mock-ups of old obsolete blocks and "
						+ "then replacing obsolete blocks with newer blocks.");

		smeltableGravel = config.getBoolean("SMELTABLE_GRAVEL", OPTIONS, smeltableGravel,
				"If true, then gravel can be smelted into generic stone");
		
		dropCobblestone = config.getBoolean("DROP_COBBLESTONE", OPTIONS, dropCobblestone,
				"If true, then rock blocks will drop cobblestone instead of themselves");
		
		makeRockCobblestoneEquivilent = config.getBoolean("COBBLESTONE_EQUIVILENT", OPTIONS, makeRockCobblestoneEquivilent,
				"If true, then rock blocks will be equivilent of cobblestone for recipes (cobblestone ore dict entry)");
		
		geomeSize = config.getInt("GEOME_SIZE", WORLD_GEN, geomeSize, 4, Short.MAX_VALUE,
				"Making this value larger increases the size of regions of igneous, sedimentary, and metamorphic rocks");
		rockLayerNoise = (double) config.getFloat("ROCK_LAYER_NOISE", WORLD_GEN, (float) rockLayerNoise, 1.0f,
				(float) Short.MAX_VALUE, "Changing this value will change the 'waviness' of the layers.");
		geomLayerThickness = config.getInt("ROCK_LAYER_THICKNESS", WORLD_GEN, geomLayerThickness, 1, 255,
				"Changing this value will change the height of individual layers.");

		generateReliefs = config.getBoolean("GENERATE_RELIEFS", OPTIONS, generateReliefs,
				"If true, then rock reliefs will be generated");
		generateRockStairs = config.getBoolean("GENERATE_ROCKSTAIRS", OPTIONS, generateRockStairs,
				"If true, then rock stairs will be generated");
		generateRockFurnace = config.getBoolean("GENERATE_ROCKFURNACE", OPTIONS, generateRockFurnace,
				"If true, then rock furnaces will be generated");
		generateRockSlab = config.getBoolean("GENERATE_ROCKSLAB", OPTIONS, generateRockSlab,
				"If true, then rock slabs will be generated");
		generateRockWall = config.getBoolean("GENERATE_ROCKWALL", OPTIONS, generateRockWall,
				"If true, then rock walls will be generated");
		generateBrick = config.getBoolean("GENERATE_BRICK", OPTIONS, generateBrick,
				"If true, then rock brick blocks will be generated");
		generateBrickFurnace = config.getBoolean("GENERATE_BRICKFURNACE", OPTIONS, generateBrickFurnace,
				"If true, then brick furnaces will be generated");
		generateBrickStairs = config.getBoolean("GENERATE_BRICKSTAIRS", OPTIONS, generateBrickStairs,
				"If true, then brick stairs will be generated");
		generateBrickSlab = config.getBoolean("GENERATE_BRICKSLAB", OPTIONS, generateBrickSlab,
				"If true, then brick slabs will be generated");
		generateBrickWall = config.getBoolean("GENERATE_BRICKWALL", OPTIONS, generateBrickWall,
				"If true, then brick walls will be generated");
		generateSmooth = config.getBoolean("GENERATE_SMOOTH", OPTIONS, generateSmooth,
				"If true, then polished rock will be generated");
		generateSmoothFurnace = config.getBoolean("GENERATE_SMOOTHFURNACE", OPTIONS, generateSmoothFurnace,
				"If true, then smooth furnaces will be generated");
		generateSmoothStairs = config.getBoolean("GENERATE_SMOOTHSTAIRS", OPTIONS, generateSmoothStairs,
				"If true, then polished rock stairs will be generated");
		generateSmoothSlab = config.getBoolean("GENERATE_SMOOTHSLAB", OPTIONS, generateSmoothSlab,
				"If true, then polished rock slabs will be generated");
		generateSmoothWall = config.getBoolean("GENERATE_SMOOTHWALL", OPTIONS, generateSmoothWall,
				"If true, then polished walls will be generated");
		generateSmoothBrick = config.getBoolean("GENERATE_SMOOTHBRICK", OPTIONS, generateSmoothBrick,
				"If true, then polished brick blocks will be generated");
		generateSmoothBrickFurnace = config.getBoolean("GENERATE_SMOOTHBRICKFURNACE", OPTIONS, generateSmoothBrickFurnace,
				"If true, then smooth brick furnaces will be generated");
		generateSmoothBrickStairs = config.getBoolean("GENERATE_SMOOTHBRICKSTAIRS", OPTIONS,
				generateSmoothBrickStairs, "If true, then polished brick stairs will be generated");
		generateSmoothBrickSlab = config.getBoolean("GENERATE_SMOOTHBRICKSLAB", OPTIONS, generateSmoothBrickSlab,
				"If true, then polished brick slabs will be generated");
		generateSmoothBrickWall = config.getBoolean("GENERATE_SMOOTHBRICKWALL", OPTIONS, generateSmoothBrickWall,
				"If true, then polished brick walls will be generated");
		
		groupCreativeTabItemsByType = config.getBoolean("GROUP_TABS_BY_TYPE", OPTIONS, groupCreativeTabItemsByType,
				"If true, then the creative tabs will be split by item type");

		final String FORMAT_LIST_MESSAGE = "(format is mod:block as a semicolon (;) delimited list)";

		igneousBlacklist.addAll(asList(config.getString("igneous_blacklist", WORLD_GEN, "",
				"Ban blocks from spawning in rock layers " + FORMAT_LIST_MESSAGE), ";"));
		sedimentaryBlacklist.addAll(asList(config.getString("sedimentary_blacklist", WORLD_GEN, "",
				"Ban blocks from spawning in rock layers " + FORMAT_LIST_MESSAGE), ";"));
		metamorphicBlacklist.addAll(asList(config.getString("metamorphic_blacklist", WORLD_GEN, "",
				"Ban blocks from spawning in rock layers " + FORMAT_LIST_MESSAGE), ";"));

		igneousWhitelist.addAll(asList(config.getString("igneous_whitelist", WORLD_GEN, "",
				"Adds blocks to rock layers " + FORMAT_LIST_MESSAGE), ";"));
		sedimentaryWhitelist.addAll(asList(config.getString("sedimentary_whitelist", WORLD_GEN, "",
				"Adds blocks to rock layers " + FORMAT_LIST_MESSAGE), ";"));
		metamorphicWhitelist.addAll(asList(config.getString("metamorphic_whitelist", WORLD_GEN, "",
				"Adds blocks to rock layers " + FORMAT_LIST_MESSAGE), ";"));
		
		
	}

	private static List<String> asList(String list, String delimiter) {
		String[] a = list.split(delimiter);
		return Arrays.asList(a);
	}

	public static double rockLayerNoise() {
		return rockLayerNoise;
	}

	public static int geomeSize() {
		return geomeSize;
	}

	public static boolean placeMineralogyRock() {
		return placeMineralogyRock;
	}
	
	public static int geomLayerThickness() {
		return geomLayerThickness;
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
		return groupCreativeTabItemsByType;
	}
	public static Configuration config() {
		return config;
	}
}
