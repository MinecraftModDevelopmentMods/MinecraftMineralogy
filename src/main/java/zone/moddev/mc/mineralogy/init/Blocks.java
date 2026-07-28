package zone.moddev.mc.mineralogy.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.RockType;
import zone.moddev.mc.mineralogy.blocks.Chalk;
import zone.moddev.mc.mineralogy.blocks.Chert;
import zone.moddev.mc.mineralogy.blocks.DoubleSlab;
import zone.moddev.mc.mineralogy.blocks.DryWall;
import zone.moddev.mc.mineralogy.blocks.Gypsum;
import zone.moddev.mc.mineralogy.blocks.NamedMineralogyBlock;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSalt;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;
import zone.moddev.mc.mineralogy.data.MaterialData;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {

    public static Rock andesite = null;
    public static Rock basalt = null;
    public static Rock diorite = null;
    public static Rock granite = null;
    public static Rock rhyolite = null;
    public static Rock pegmatite = null;
    public static Rock shale = null;
    public static Rock conglomerate = null;
    public static Rock dolomite = null;
    public static Rock limestone = null;
    public static Rock marble = null;
    public static Rock slate = null;
    public static Rock schist = null;
    public static Rock gneiss = null;
    public static Rock phyllite = null;
    public static Rock amphibolite = null;

    public static Rock andesite_smooth = null;
    public static Rock basalt_smooth = null;
    public static Rock diorite_smooth = null;
    public static Rock granite_smooth = null;
    public static Rock rhyolite_smooth = null;
    public static Rock pegmatite_smooth = null;
    public static Rock shale_smooth = null;
    public static Rock conglomerate_smooth = null;
    public static Rock dolomite_smooth = null;
    public static Rock limestone_smooth = null;
    public static Rock marble_smooth = null;
    public static Rock slate_smooth = null;
    public static Rock schist_smooth = null;
    public static Rock gneiss_smooth = null;
    public static Rock phyllite_smooth = null;
    public static Rock amphibolite_smooth = null;

    public static RockStairs andesite_stairs = null;
    public static RockStairs basalt_stairs = null;
    public static RockStairs diorite_stairs = null;
    public static RockStairs granite_stairs = null;
    public static RockStairs rhyolite_stairs = null;
    public static RockStairs pegmatite_stairs = null;
    public static RockStairs shale_stairs = null;
    public static RockStairs conglomerate_stairs = null;
    public static RockStairs dolomite_stairs = null;
    public static RockStairs limestone_stairs = null;
    public static RockStairs marble_stairs = null;
    public static RockStairs slate_stairs = null;
    public static RockStairs schist_stairs = null;
    public static RockStairs gneiss_stairs = null;
    public static RockStairs phyllite_stairs = null;
    public static RockStairs amphibolite_stairs = null;

    public static RockStairs andesite_smooth_stairs = null;
    public static RockStairs basalt_smooth_stairs = null;
    public static RockStairs diorite_smooth_stairs = null;
    public static RockStairs granite_smooth_stairs = null;
    public static RockStairs rhyolite_smooth_stairs = null;
    public static RockStairs pegmatite_smooth_stairs = null;
    public static RockStairs shale_smooth_stairs = null;
    public static RockStairs conglomerate_smooth_stairs = null;
    public static RockStairs dolomite_smooth_stairs = null;
    public static RockStairs limestone_smooth_stairs = null;
    public static RockStairs marble_smooth_stairs = null;
    public static RockStairs slate_smooth_stairs = null;
    public static RockStairs schist_smooth_stairs = null;
    public static RockStairs gneiss_smooth_stairs = null;
    public static RockStairs phyllite_smooth_stairs = null;
    public static RockStairs amphibolite_smooth_stairs = null;

    public static Rock andesite_brick = null;
    public static Rock basalt_brick = null;
    public static Rock diorite_brick = null;
    public static Rock granite_brick = null;
    public static Rock rhyolite_brick = null;
    public static Rock pegmatite_brick = null;
    public static Rock shale_brick = null;
    public static Rock conglomerate_brick = null;
    public static Rock dolomite_brick = null;
    public static Rock limestone_brick = null;
    public static Rock marble_brick = null;
    public static Rock slate_brick = null;
    public static Rock schist_brick = null;
    public static Rock gneiss_brick = null;
    public static Rock phyllite_brick = null;
    public static Rock amphibolite_brick = null;

    public static Rock andesite_smooth_brick = null;
    public static Rock basalt_smooth_brick = null;
    public static Rock diorite_smooth_brick = null;
    public static Rock granite_smooth_brick = null;
    public static Rock rhyolite_smooth_brick = null;
    public static Rock pegmatite_smooth_brick = null;
    public static Rock shale_smooth_brick = null;
    public static Rock conglomerate_smooth_brick = null;
    public static Rock dolomite_smooth_brick = null;
    public static Rock limestone_smooth_brick = null;
    public static Rock marble_smooth_brick = null;
    public static Rock slate_smooth_brick = null;
    public static Rock schist_smooth_brick = null;
    public static Rock gneiss_smooth_brick = null;
    public static Rock phyllite_smooth_brick = null;
    public static Rock amphibolite_smooth_brick = null;

    public static RockStairs andesite_brick_stairs = null;
    public static RockStairs basalt_brick_stairs = null;
    public static RockStairs diorite_brick_stairs = null;
    public static RockStairs granite_brick_stairs = null;
    public static RockStairs rhyolite_brick_stairs = null;
    public static RockStairs pegmatite_brick_stairs = null;
    public static RockStairs shale_brick_stairs = null;
    public static RockStairs conglomerate_brick_stairs = null;
    public static RockStairs dolomite_brick_stairs = null;
    public static RockStairs limestone_brick_stairs = null;
    public static RockStairs marble_brick_stairs = null;
    public static RockStairs slate_brick_stairs = null;
    public static RockStairs schist_brick_stairs = null;
    public static RockStairs gneiss_brick_stairs = null;
    public static RockStairs phyllite_brick_stairs = null;
    public static RockStairs amphibolite_brick_stairs = null;

    public static RockStairs andesite_smooth_brick_stairs = null;
    public static RockStairs basalt_smooth_brick_stairs = null;
    public static RockStairs diorite_smooth_brick_stairs = null;
    public static RockStairs granite_smooth_brick_stairs = null;
    public static RockStairs rhyolite_smooth_brick_stairs = null;
    public static RockStairs pegmatite_smooth_brick_stairs = null;
    public static RockStairs shale_smooth_brick_stairs = null;
    public static RockStairs conglomerate_smooth_brick_stairs = null;
    public static RockStairs dolomite_smooth_brick_stairs = null;
    public static RockStairs limestone_smooth_brick_stairs = null;
    public static RockStairs marble_smooth_brick_stairs = null;
    public static RockStairs slate_smooth_brick_stairs = null;
    public static RockStairs schist_smooth_brick_stairs = null;
    public static RockStairs gneiss_smooth_brick_stairs = null;
    public static RockStairs phyllite_smooth_brick_stairs = null;
    public static RockStairs amphibolite_smooth_brick_stairs = null;

    public static RockWall andesite_wall = null;
    public static RockWall basalt_wall = null;
    public static RockWall diorite_wall = null;
    public static RockWall granite_wall = null;
    public static RockWall rhyolite_wall = null;
    public static RockWall pegmatite_wall = null;
    public static RockWall shale_wall = null;
    public static RockWall conglomerate_wall = null;
    public static RockWall dolomite_wall = null;
    public static RockWall limestone_wall = null;
    public static RockWall marble_wall = null;
    public static RockWall slate_wall = null;
    public static RockWall schist_wall = null;
    public static RockWall gneiss_wall = null;
    public static RockWall phyllite_wall = null;
    public static RockWall amphibolite_wall = null;

	public static RockWall andesite_smooth_wall = null;
    public static RockWall basalt_smooth_wall = null;
    public static RockWall diorite_smooth_wall = null;
    public static RockWall granite_smooth_wall = null;
    public static RockWall rhyolite_smooth_wall = null;
    public static RockWall pegmatite_smooth_wall = null;
    public static RockWall shale_smooth_wall = null;
    public static RockWall conglomerate_smooth_wall = null;
    public static RockWall dolomite_smooth_wall = null;
    public static RockWall limestone_smooth_wall = null;
    public static RockWall marble_smooth_wall = null;
    public static RockWall slate_smooth_wall = null;
    public static RockWall schist_smooth_wall = null;
    public static RockWall gneiss_smooth_wall = null;
    public static RockWall phyllite_smooth_wall = null;
    public static RockWall amphibolite_smooth_wall = null;

	public static RockWall andesite_brick_wall = null;
    public static RockWall basalt_brick_wall = null;
    public static RockWall diorite_brick_wall = null;
    public static RockWall granite_brick_wall = null;
    public static RockWall rhyolite_brick_wall = null;
    public static RockWall pegmatite_brick_wall = null;
    public static RockWall shale_brick_wall = null;
    public static RockWall conglomerate_brick_wall = null;
    public static RockWall dolomite_brick_wall = null;
    public static RockWall limestone_brick_wall = null;
    public static RockWall marble_brick_wall = null;
    public static RockWall slate_brick_wall = null;
    public static RockWall schist_brick_wall = null;
    public static RockWall gneiss_brick_wall = null;
    public static RockWall phyllite_brick_wall = null;
    public static RockWall amphibolite_brick_wall = null;

	public static RockWall andesite_smooth_brick_wall = null;
    public static RockWall basalt_smooth_brick_wall = null;
    public static RockWall diorite_smooth_brick_wall = null;
    public static RockWall granite_smooth_brick_wall = null;
    public static RockWall rhyolite_smooth_brick_wall = null;
    public static RockWall pegmatite_smooth_brick_wall = null;
    public static RockWall shale_smooth_brick_wall = null;
    public static RockWall conglomerate_smooth_brick_wall = null;
    public static RockWall dolomite_smooth_brick_wall = null;
    public static RockWall limestone_smooth_brick_wall = null;
    public static RockWall marble_smooth_brick_wall = null;
    public static RockWall slate_smooth_brick_wall = null;
    public static RockWall schist_smooth_brick_wall = null;
    public static RockWall gneiss_smooth_brick_wall = null;
    public static RockWall phyllite_smooth_brick_wall = null;
    public static RockWall amphibolite_smooth_brick_wall = null;

    private static final Map<String, Field> LEGACY_BLOCK_FIELDS = createLegacyBlockFieldMap();

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
		if (!ForgeRegistries.Keys.BLOCKS.equals(event.getRegistryKey())) {
			return;
		}
		IForgeRegistry<Block> registry = event.getForgeRegistry();

		for (zone.moddev.mc.mineralogy.data.Material material : MaterialData.toArray()) {
			registerMaterialFamily(registry, material, material.toRock(false, false), true);
		}

		registerMaterialFamily(registry, MaterialData.ROCK_SALT, new RockSalt(), true);

		registerSpecialGeologyBlocks(registry);
		registerDryWalls(registry);

		registerAll(registry,
				new RockSaltLamp(),
				new RockSaltStreetLamp(),
				new Ore("sulfur_ore", "sulfur_dust", 1, 4, 0),
				new Ore("phosphorous_ore", "phosphorous_dust", 1, 4, 0),
				new Ore("nitrate_ore", "nitrate_dust", 1, 4, 0),
				new Rock(false, 1.5F, 10.0F, 0, SoundType.STONE, "sulfur_block"),
				new Rock(false, 1.5F, 10.0F, 0, SoundType.STONE, "phosphorous_block"),
				new Rock(false, 1.5F, 10.0F, 0, SoundType.STONE, "nitrate_block")
		);

		//event.getRegistry().register(MaterialData.BASALT.toRockWall(false, false));


    }

	private static void registerSpecialGeologyBlocks(IForgeRegistry<Block> registry) {
		Block chert = new Chert();
		Block gypsum = new Gypsum();
		Block chalk = new Chalk();
		Block pumice = new Rock(false, 0.5F, 5.0F, 0, SoundType.STONE, "pumice");

		registerAll(registry, chert, gypsum, chalk, pumice);

		MineralogyRegistry.sedimentaryStones.add(net.minecraft.world.level.block.Blocks.SANDSTONE);
		MineralogyRegistry.sedimentaryStones.add(chert);
		MineralogyRegistry.sedimentaryStones.add(gypsum);
		MineralogyRegistry.sedimentaryStones.add(chalk);
		MineralogyRegistry.igneousStones.add(pumice);
	}

	private static void registerDryWalls(IForgeRegistry<Block> registry) {
		for (String color : zone.moddev.mc.mineralogy.Constants.colorSuffixes) {
			register(registry, new DryWall(color));
		}
	}

	private static void registerMaterialFamily(IForgeRegistry<Block> registry,
			zone.moddev.mc.mineralogy.data.Material material, Rock baseRock, boolean addToGeology) {
		register(registry, baseRock);
		if (addToGeology) {
			addStoneType(material.rockType, baseRock);
		}

		if (MineralogyConfig.generateRockSlab()) {
			registerSlabPair(registry, createSlab(material, false, false), material, false, false);
			if (MineralogyConfig.generateRockFurnace()) {
				registerFurnacePair(registry, material, false, false);
			}
		}
		if (MineralogyConfig.generateRockStairs()) {
			register(registry, createStairs(baseRock, material, false, false));
		}
		if (MineralogyConfig.generateRockWall()) {
			register(registry, createWall(baseRock, material, false, false));
		}

		Rock smoothRock = null;
		if (MineralogyConfig.generateSmooth()) {
			smoothRock = material.toRock(true, false);
			register(registry, smoothRock);

			if (MineralogyConfig.generateReliefs()) {
				registerReliefs(registry, material);
			}
			if (MineralogyConfig.generateSmoothSlab()) {
				registerSlabPair(registry, createSlab(material, true, false), material, true, false);
				if (MineralogyConfig.generateSmoothFurnace()) {
					registerFurnacePair(registry, material, true, false);
				}
			}
			if (MineralogyConfig.generateSmoothStairs()) {
				register(registry, createStairs(smoothRock, material, true, false));
			}
			if (MineralogyConfig.generateSmoothWall()) {
				register(registry, createWall(smoothRock, material, true, false));
			}
		}

		Rock brickRock = null;
		if (MineralogyConfig.generateBrick()) {
			brickRock = material.toRock(false, true);
			register(registry, brickRock);

			if (MineralogyConfig.generateBrickSlab()) {
				registerSlabPair(registry, createSlab(material, false, true), material, false, true);
				if (MineralogyConfig.generateBrickFurnace()) {
					registerFurnacePair(registry, material, false, true);
				}
			}
			if (MineralogyConfig.generateBrickStairs()) {
				register(registry, createStairs(brickRock, material, false, true));
			}
			if (MineralogyConfig.generateBrickWall()) {
				register(registry, createWall(brickRock, material, false, true));
			}
		}

		if (smoothRock != null && MineralogyConfig.generateSmoothBrick()) {
			Rock smoothBrickRock = material.toRock(true, true);
			register(registry, smoothBrickRock);

			if (MineralogyConfig.generateSmoothBrickSlab()) {
				registerSlabPair(registry, createSlab(material, true, true), material, true, true);
				if (MineralogyConfig.generateSmoothBrickFurnace()) {
					registerFurnacePair(registry, material, true, true);
				}
			}
			if (MineralogyConfig.generateSmoothBrickStairs()) {
				register(registry, createStairs(smoothBrickRock, material, true, true));
			}
			if (MineralogyConfig.generateSmoothBrickWall()) {
				register(registry, createWall(smoothBrickRock, material, true, true));
			}
		}
	}

	private static void registerSlabPair(IForgeRegistry<Block> registry, RockSlab slab,
			zone.moddev.mc.mineralogy.data.Material material, boolean isSmooth, boolean isBrick) {
		register(registry, slab);
		register(registry, createDoubleSlab(slab, material, isSmooth, isBrick));
	}

	private static RockStairs createStairs(Block sourceBlock, zone.moddev.mc.mineralogy.data.Material material,
			boolean isSmooth, boolean isBrick) {
		return new RockStairs(sourceBlock, (float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, SoundType.STONE, getVariantName(material, isSmooth, isBrick) + "_stairs");
	}

	private static RockWall createWall(Block sourceBlock, zone.moddev.mc.mineralogy.data.Material material,
			boolean isSmooth, boolean isBrick) {
		return new RockWall(sourceBlock, (float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, SoundType.STONE, getVariantName(material, isSmooth, isBrick) + "_wall");
	}

	private static RockSlab createSlab(zone.moddev.mc.mineralogy.data.Material material, boolean isSmooth,
			boolean isBrick) {
		String name = getVariantName(material, isSmooth, isBrick);
		return new RockSlab((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, SoundType.STONE, name + "_slab", name + "_double_slab");
	}

	private static DoubleSlab createDoubleSlab(Block sourceBlock, zone.moddev.mc.mineralogy.data.Material material,
			boolean isSmooth, boolean isBrick) {
		return new DoubleSlab((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, SoundType.STONE, sourceBlock,
				getVariantName(material, isSmooth, isBrick) + "_double_slab");
	}

	private static void registerFurnacePair(IForgeRegistry<Block> registry,
			zone.moddev.mc.mineralogy.data.Material material, boolean isSmooth, boolean isBrick) {
		String name = getVariantName(material, isSmooth, isBrick) + "_furnace";
		float burnModifier = (float) (1.0D + ((material.hardness - 3.0D) / 10.0D));

		register(registry, new RockFurnace((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, false, burnModifier, name));
		register(registry, new RockFurnace((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, true, burnModifier, "lit_" + name));
	}

	private static void registerReliefs(IForgeRegistry<Block> registry,
			zone.moddev.mc.mineralogy.data.Material material) {
		String name = material.materialName.toLowerCase();
		String[] suffixes = new String[] {
				"blank",
				"axe",
				"cross",
				"hammer",
				"hoe",
				"horizontal",
				"left",
				"pickaxe",
				"plus",
				"right",
				"sword",
				"i",
				"vertical"
		};

		for (String suffix : suffixes) {
			register(registry, new RockRelief((float) material.hardness,
					(float) material.blastResistance / 2.0F, material.toolHardnessLevel,
					SoundType.STONE, name + "_relief_" + suffix));
		}
	}

	private static void registerAll(IForgeRegistry<Block> registry, Block... blocks) {
		for (Block block : blocks) {
			register(registry, block);
		}
	}

	private static <T extends Block> T register(IForgeRegistry<Block> registry, T block) {
		if (!(block instanceof NamedMineralogyBlock)) {
			throw new IllegalArgumentException("Mineralogy block has no stable registry path: " + block.getClass());
		}
		String path = ((NamedMineralogyBlock) block).mineralogyRegistryPath();
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Mineralogy block has an empty registry path: " + block.getClass());
		}
		registry.register(new ResourceLocation(Mineralogy.MODID, path), block);
		bindLegacyBlockField(path, block);
		return block;
	}

	private static Map<String, Field> createLegacyBlockFieldMap() {
		Map<String, Field> fields = new HashMap<>();
		for (Field field : Blocks.class.getDeclaredFields()) {
			int modifiers = field.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
					&& Block.class.isAssignableFrom(field.getType())) {
				fields.put(field.getName(), field);
			}
		}
		return Collections.unmodifiableMap(fields);
	}

	private static void bindLegacyBlockField(String path, Block block) {
		Field field = LEGACY_BLOCK_FIELDS.get(path);
		if (field == null) {
			return;
		}
		if (!field.getType().isInstance(block)) {
			throw new IllegalStateException("Legacy block field type mismatch for " + path);
		}
		try {
			field.set(null, block);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot bind legacy block field " + path, e);
		}
	}

	private static String getVariantName(zone.moddev.mc.mineralogy.data.Material material, boolean isSmooth,
			boolean isBrick) {
		String name = material.materialName.toLowerCase();

		if (isSmooth) {
			name = name + "_smooth";
		}
		if (isBrick) {
			name = name + "_brick";
		}

		return name;
	}

	private static void addStoneType(RockType rockType, Block block) {
		switch (rockType) {
			case IGNEOUS:
				MineralogyRegistry.igneousStones.add(block);
				break;
			case METAMORPHIC:
				MineralogyRegistry.metamorphicStones.add(block);
				break;
			case SEDIMENTARY:
				MineralogyRegistry.sedimentaryStones.add(block);
				break;
			case ANY:
				MineralogyRegistry.igneousStones.add(block);
				MineralogyRegistry.metamorphicStones.add(block);
				MineralogyRegistry.sedimentaryStones.add(block);
				break;
			default:
				break;
		}
	}
}
