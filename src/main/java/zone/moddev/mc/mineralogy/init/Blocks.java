package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.blocks.Chalk;
import zone.moddev.mc.mineralogy.blocks.Chert;
import zone.moddev.mc.mineralogy.blocks.DoubleSlab;
import zone.moddev.mc.mineralogy.blocks.DryWall;
import zone.moddev.mc.mineralogy.blocks.Gypsum;
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

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class Blocks {

    public static final Rock andesite = null;
    public static final Rock basalt = null;
    public static final Rock diorite = null;
    public static final Rock granite = null;
    public static final Rock rhyolite = null;
    public static final Rock pegmatite = null;
    public static final Rock shale = null;
    public static final Rock conglomerate = null;
    public static final Rock dolomite = null;
    public static final Rock limestone = null;
    public static final Rock marble = null;
    public static final Rock slate = null;
    public static final Rock schist = null;
    public static final Rock gneiss = null;
    public static final Rock phyllite = null;
    public static final Rock amphibolite = null;

    public static final Rock andesite_smooth = null;
    public static final Rock basalt_smooth = null;
    public static final Rock diorite_smooth = null;
    public static final Rock granite_smooth = null;
    public static final Rock rhyolite_smooth = null;
    public static final Rock pegmatite_smooth = null;
    public static final Rock shale_smooth = null;
    public static final Rock conglomerate_smooth = null;
    public static final Rock dolomite_smooth = null;
    public static final Rock limestone_smooth = null;
    public static final Rock marble_smooth = null;
    public static final Rock slate_smooth = null;
    public static final Rock schist_smooth = null;
    public static final Rock gneiss_smooth = null;
    public static final Rock phyllite_smooth = null;
    public static final Rock amphibolite_smooth = null;

    public static final RockStairs andesite_stairs = null;
    public static final RockStairs basalt_stairs = null;
    public static final RockStairs diorite_stairs = null;
    public static final RockStairs granite_stairs = null;
    public static final RockStairs rhyolite_stairs = null;
    public static final RockStairs pegmatite_stairs = null;
    public static final RockStairs shale_stairs = null;
    public static final RockStairs conglomerate_stairs = null;
    public static final RockStairs dolomite_stairs = null;
    public static final RockStairs limestone_stairs = null;
    public static final RockStairs marble_stairs = null;
    public static final RockStairs slate_stairs = null;
    public static final RockStairs schist_stairs = null;
    public static final RockStairs gneiss_stairs = null;
    public static final RockStairs phyllite_stairs = null;
    public static final RockStairs amphibolite_stairs = null;

    public static final RockStairs andesite_smooth_stairs = null;
    public static final RockStairs basalt_smooth_stairs = null;
    public static final RockStairs diorite_smooth_stairs = null;
    public static final RockStairs granite_smooth_stairs = null;
    public static final RockStairs rhyolite_smooth_stairs = null;
    public static final RockStairs pegmatite_smooth_stairs = null;
    public static final RockStairs shale_smooth_stairs = null;
    public static final RockStairs conglomerate_smooth_stairs = null;
    public static final RockStairs dolomite_smooth_stairs = null;
    public static final RockStairs limestone_smooth_stairs = null;
    public static final RockStairs marble_smooth_stairs = null;
    public static final RockStairs slate_smooth_stairs = null;
    public static final RockStairs schist_smooth_stairs = null;
    public static final RockStairs gneiss_smooth_stairs = null;
    public static final RockStairs phyllite_smooth_stairs = null;
    public static final RockStairs amphibolite_smooth_stairs = null;

    public static final Rock andesite_brick = null;
    public static final Rock basalt_brick = null;
    public static final Rock diorite_brick = null;
    public static final Rock granite_brick = null;
    public static final Rock rhyolite_brick = null;
    public static final Rock pegmatite_brick = null;
    public static final Rock shale_brick = null;
    public static final Rock conglomerate_brick = null;
    public static final Rock dolomite_brick = null;
    public static final Rock limestone_brick = null;
    public static final Rock marble_brick = null;
    public static final Rock slate_brick = null;
    public static final Rock schist_brick = null;
    public static final Rock gneiss_brick = null;
    public static final Rock phyllite_brick = null;
    public static final Rock amphibolite_brick = null;

    public static final Rock andesite_smooth_brick = null;
    public static final Rock basalt_smooth_brick = null;
    public static final Rock diorite_smooth_brick = null;
    public static final Rock granite_smooth_brick = null;
    public static final Rock rhyolite_smooth_brick = null;
    public static final Rock pegmatite_smooth_brick = null;
    public static final Rock shale_smooth_brick = null;
    public static final Rock conglomerate_smooth_brick = null;
    public static final Rock dolomite_smooth_brick = null;
    public static final Rock limestone_smooth_brick = null;
    public static final Rock marble_smooth_brick = null;
    public static final Rock slate_smooth_brick = null;
    public static final Rock schist_smooth_brick = null;
    public static final Rock gneiss_smooth_brick = null;
    public static final Rock phyllite_smooth_brick = null;
    public static final Rock amphibolite_smooth_brick = null;

    public static final RockStairs andesite_brick_stairs = null;
    public static final RockStairs basalt_brick_stairs = null;
    public static final RockStairs diorite_brick_stairs = null;
    public static final RockStairs granite_brick_stairs = null;
    public static final RockStairs rhyolite_brick_stairs = null;
    public static final RockStairs pegmatite_brick_stairs = null;
    public static final RockStairs shale_brick_stairs = null;
    public static final RockStairs conglomerate_brick_stairs = null;
    public static final RockStairs dolomite_brick_stairs = null;
    public static final RockStairs limestone_brick_stairs = null;
    public static final RockStairs marble_brick_stairs = null;
    public static final RockStairs slate_brick_stairs = null;
    public static final RockStairs schist_brick_stairs = null;
    public static final RockStairs gneiss_brick_stairs = null;
    public static final RockStairs phyllite_brick_stairs = null;
    public static final RockStairs amphibolite_brick_stairs = null;

    public static final RockStairs andesite_smooth_brick_stairs = null;
    public static final RockStairs basalt_smooth_brick_stairs = null;
    public static final RockStairs diorite_smooth_brick_stairs = null;
    public static final RockStairs granite_smooth_brick_stairs = null;
    public static final RockStairs rhyolite_smooth_brick_stairs = null;
    public static final RockStairs pegmatite_smooth_brick_stairs = null;
    public static final RockStairs shale_smooth_brick_stairs = null;
    public static final RockStairs conglomerate_smooth_brick_stairs = null;
    public static final RockStairs dolomite_smooth_brick_stairs = null;
    public static final RockStairs limestone_smooth_brick_stairs = null;
    public static final RockStairs marble_smooth_brick_stairs = null;
    public static final RockStairs slate_smooth_brick_stairs = null;
    public static final RockStairs schist_smooth_brick_stairs = null;
    public static final RockStairs gneiss_smooth_brick_stairs = null;
    public static final RockStairs phyllite_smooth_brick_stairs = null;
    public static final RockStairs amphibolite_smooth_brick_stairs = null;

    public static final RockWall andesite_wall = null;
    public static final RockWall basalt_wall = null;
    public static final RockWall diorite_wall = null;
    public static final RockWall granite_wall = null;
    public static final RockWall rhyolite_wall = null;
    public static final RockWall pegmatite_wall = null;
    public static final RockWall shale_wall = null;
    public static final RockWall conglomerate_wall = null;
    public static final RockWall dolomite_wall = null;
    public static final RockWall limestone_wall = null;
    public static final RockWall marble_wall = null;
    public static final RockWall slate_wall = null;
    public static final RockWall schist_wall = null;
    public static final RockWall gneiss_wall = null;
    public static final RockWall phyllite_wall = null;
    public static final RockWall amphibolite_wall = null;

	public static final RockWall andesite_smooth_wall = null;
    public static final RockWall basalt_smooth_wall = null;
    public static final RockWall diorite_smooth_wall = null;
    public static final RockWall granite_smooth_wall = null;
    public static final RockWall rhyolite_smooth_wall = null;
    public static final RockWall pegmatite_smooth_wall = null;
    public static final RockWall shale_smooth_wall = null;
    public static final RockWall conglomerate_smooth_wall = null;
    public static final RockWall dolomite_smooth_wall = null;
    public static final RockWall limestone_smooth_wall = null;
    public static final RockWall marble_smooth_wall = null;
    public static final RockWall slate_smooth_wall = null;
    public static final RockWall schist_smooth_wall = null;
    public static final RockWall gneiss_smooth_wall = null;
    public static final RockWall phyllite_smooth_wall = null;
    public static final RockWall amphibolite_smooth_wall = null;

	public static final RockWall andesite_brick_wall = null;
    public static final RockWall basalt_brick_wall = null;
    public static final RockWall diorite_brick_wall = null;
    public static final RockWall granite_brick_wall = null;
    public static final RockWall rhyolite_brick_wall = null;
    public static final RockWall pegmatite_brick_wall = null;
    public static final RockWall shale_brick_wall = null;
    public static final RockWall conglomerate_brick_wall = null;
    public static final RockWall dolomite_brick_wall = null;
    public static final RockWall limestone_brick_wall = null;
    public static final RockWall marble_brick_wall = null;
    public static final RockWall slate_brick_wall = null;
    public static final RockWall schist_brick_wall = null;
    public static final RockWall gneiss_brick_wall = null;
    public static final RockWall phyllite_brick_wall = null;
    public static final RockWall amphibolite_brick_wall = null;

	public static final RockWall andesite_smooth_brick_wall = null;
    public static final RockWall basalt_smooth_brick_wall = null;
    public static final RockWall diorite_smooth_brick_wall = null;
    public static final RockWall granite_smooth_brick_wall = null;
    public static final RockWall rhyolite_smooth_brick_wall = null;
    public static final RockWall pegmatite_smooth_brick_wall = null;
    public static final RockWall shale_smooth_brick_wall = null;
    public static final RockWall conglomerate_smooth_brick_wall = null;
    public static final RockWall dolomite_smooth_brick_wall = null;
    public static final RockWall limestone_smooth_brick_wall = null;
    public static final RockWall marble_smooth_brick_wall = null;
    public static final RockWall slate_smooth_brick_wall = null;
    public static final RockWall schist_smooth_brick_wall = null;
    public static final RockWall gneiss_smooth_brick_wall = null;
    public static final RockWall phyllite_smooth_brick_wall = null;
    public static final RockWall amphibolite_smooth_brick_wall = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
	for (zone.moddev.mc.mineralogy.data.Material material : MaterialData.toArray()) {
			registerMaterialFamily(event, material, material.toRock(false, false));
		}

		registerMaterialFamily(event, MaterialData.ROCK_SALT, new RockSalt());

		registerSpecialGeologyBlocks(event);
		registerDryWalls(event);

		event.getRegistry().registerAll(
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

	private static void registerSpecialGeologyBlocks(RegistryEvent.Register<Block> event) {
		Block chert = new Chert();
		Block gypsum = new Gypsum();
		Block chalk = new Chalk();
		Block pumice = new Rock(false, 0.5F, 5.0F, 0, SoundType.STONE, "pumice");

		event.getRegistry().registerAll(chert, gypsum, chalk, pumice);

	}

	private static void registerDryWalls(RegistryEvent.Register<Block> event) {
		for (String color : zone.moddev.mc.mineralogy.Constants.colorSuffixes) {
			event.getRegistry().register(new DryWall(color));
		}
	}

	private static void registerMaterialFamily(RegistryEvent.Register<Block> event,
			zone.moddev.mc.mineralogy.data.Material material, Rock baseRock) {
		event.getRegistry().register(baseRock);

		if (MineralogyConfig.generateRockSlab()) {
			registerSlabPair(event, createSlab(material, false, false), baseRock, material, false, false);
			if (MineralogyConfig.generateRockFurnace()) {
				registerFurnacePair(event, material, false, false);
			}
		}
		if (MineralogyConfig.generateRockStairs()) {
			event.getRegistry().register(createStairs(baseRock, material, false, false));
		}
		if (MineralogyConfig.generateRockWall()) {
			event.getRegistry().register(createWall(baseRock, material, false, false));
		}

		Rock smoothRock = null;
		if (MineralogyConfig.generateSmooth()) {
			smoothRock = material.toRock(true, false);
			event.getRegistry().register(smoothRock);

			if (MineralogyConfig.generateReliefs()) {
				registerReliefs(event, material);
			}
			if (MineralogyConfig.generateSmoothSlab()) {
				registerSlabPair(event, createSlab(material, true, false), smoothRock, material, true, false);
				if (MineralogyConfig.generateSmoothFurnace()) {
					registerFurnacePair(event, material, true, false);
				}
			}
			if (MineralogyConfig.generateSmoothStairs()) {
				event.getRegistry().register(createStairs(smoothRock, material, true, false));
			}
			if (MineralogyConfig.generateSmoothWall()) {
				event.getRegistry().register(createWall(smoothRock, material, true, false));
			}
		}

		Rock brickRock = null;
		if (MineralogyConfig.generateBrick()) {
			brickRock = material.toRock(false, true);
			event.getRegistry().register(brickRock);

			if (MineralogyConfig.generateBrickSlab()) {
				registerSlabPair(event, createSlab(material, false, true), brickRock, material, false, true);
				if (MineralogyConfig.generateBrickFurnace()) {
					registerFurnacePair(event, material, false, true);
				}
			}
			if (MineralogyConfig.generateBrickStairs()) {
				event.getRegistry().register(createStairs(brickRock, material, false, true));
			}
			if (MineralogyConfig.generateBrickWall()) {
				event.getRegistry().register(createWall(brickRock, material, false, true));
			}
		}

		if (smoothRock != null && MineralogyConfig.generateSmoothBrick()) {
			Rock smoothBrickRock = material.toRock(true, true);
			event.getRegistry().register(smoothBrickRock);

			if (MineralogyConfig.generateSmoothBrickSlab()) {
				registerSlabPair(event, createSlab(material, true, true), smoothBrickRock, material, true, true);
				if (MineralogyConfig.generateSmoothBrickFurnace()) {
					registerFurnacePair(event, material, true, true);
				}
			}
			if (MineralogyConfig.generateSmoothBrickStairs()) {
				event.getRegistry().register(createStairs(smoothBrickRock, material, true, true));
			}
			if (MineralogyConfig.generateSmoothBrickWall()) {
				event.getRegistry().register(createWall(smoothBrickRock, material, true, true));
			}
		}
	}

	private static void registerSlabPair(RegistryEvent.Register<Block> event, RockSlab slab, Block fullBlock,
			zone.moddev.mc.mineralogy.data.Material material, boolean isSmooth, boolean isBrick) {
		event.getRegistry().register(slab);
		event.getRegistry().register(createDoubleSlab(slab, fullBlock, material, isSmooth, isBrick));
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

	private static DoubleSlab createDoubleSlab(Block sourceBlock, Block fullBlock,
			zone.moddev.mc.mineralogy.data.Material material,
			boolean isSmooth, boolean isBrick) {
		return new DoubleSlab((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, SoundType.STONE, sourceBlock, fullBlock,
				getVariantName(material, isSmooth, isBrick) + "_double_slab");
	}

	private static void registerFurnacePair(RegistryEvent.Register<Block> event,
			zone.moddev.mc.mineralogy.data.Material material, boolean isSmooth, boolean isBrick) {
		String name = getVariantName(material, isSmooth, isBrick) + "_furnace";
		float burnModifier = (float) (1.0D + ((material.hardness - 3.0D) / 10.0D));

		event.getRegistry().register(new RockFurnace((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, false, burnModifier, name));
		event.getRegistry().register(new RockFurnace((float) material.hardness, (float) material.blastResistance,
				material.toolHardnessLevel, true, burnModifier, "lit_" + name));
	}

	private static void registerReliefs(RegistryEvent.Register<Block> event,
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
			event.getRegistry().register(new RockRelief((float) material.hardness,
					(float) material.blastResistance / 2.0F, material.toolHardnessLevel,
					SoundType.STONE, name + "_relief_" + suffix));
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

}

