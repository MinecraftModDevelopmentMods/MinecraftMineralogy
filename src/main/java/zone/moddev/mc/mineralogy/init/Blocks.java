package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Constants;
import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.blocks.Chalk;
import zone.moddev.mc.mineralogy.blocks.Chert;
import zone.moddev.mc.mineralogy.blocks.DoubleSlab;
import zone.moddev.mc.mineralogy.blocks.DryWall;
import zone.moddev.mc.mineralogy.blocks.Gypsum;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSalt;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;
import zone.moddev.mc.mineralogy.data.Material;
import zone.moddev.mc.mineralogy.data.MaterialData;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.lib.interfaces.IDynamicTabProvider;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;
import zone.moddev.mc.mineralogy.util.BlockItemPair;
import zone.moddev.mc.mineralogy.util.RegistrationHelper;

import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Blocks {
    private static boolean initDone = false;

    protected Blocks() {
        throw new IllegalAccessError("Not a instantiable class");
    }

    /**
     *
     */
    public static void init() {
        if (initDone) {
            return;
        }

        MinIoC IoC =  MinIoC.getInstance();

        BlockItemPair blockGypsum;
        BlockItemPair blockChalk;
        BlockItemPair blockRocksalt;
        BlockItemPair blockPumice;

        GameRegistry.registerTileEntity(TileEntityRockFurnace.class, "rockfurnace");

        MaterialData.toArray().forEach(material -> addStoneType(material));

        RegistrationHelper.registerBlock(new Chert(), Constants.CHERT, Constants.BLOCK_CHERT);

        blockGypsum = RegistrationHelper.registerBlock(new Gypsum(), Constants.GYPSUM.toLowerCase(), Constants.BLOCK_GYPSUM);

        blockChalk = RegistrationHelper.registerBlock(new Chalk(), Constants.CHALK.toLowerCase(), Constants.BLOCK_CHALK);

        blockRocksalt = RegistrationHelper.registerBlock(new RockSalt(), Constants.ROCKSALT.toLowerCase(), Constants.BLOCK_ROCKSALT);

        addStoneType(MaterialData.ROCK_SALT, blockRocksalt);

        IoC.register(BlockItemPair.class, blockGypsum, Constants.BLOCK_GYPSUM, Mineralogy.MODID);
        IoC.register(BlockItemPair.class, blockChalk, Constants.BLOCK_CHALK, Mineralogy.MODID);
        IoC.register(BlockItemPair.class, blockRocksalt, Constants.BLOCK_ROCKSALT, Mineralogy.MODID);

        blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.STONE), Constants.PUMICE, Constants.BLOCK_PUMICE);

        IoC.register(BlockItemPair.class, blockPumice, Constants.BLOCK_PUMICE, Mineralogy.MODID);

        RegistrationHelper.registerBlock(new RockSaltLamp(), "rocksaltlamp", "lampRocksalt");
        RegistrationHelper.registerBlock(new RockSaltStreetLamp(), "rocksaltstreetlamp", "lampRocksaltStreet", 16);

        IDynamicTabProvider tabProvider = IoC.resolve(IDynamicTabProvider.class);

        for (int i = 0; i < 16; i++) {
            if (MineralogyConfig.groupCreativeTabItemsByType())
                tabProvider.setTabItemMapping("Item", Constants.DRYWALL + "_" + Constants.colorSuffixes[i]);

            BlockItemPair drywall = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
                    Constants.DRYWALL + Constants.colorSuffixesTwo[i]);

            IoC.register(BlockItemPair.class, drywall, Constants.DRYWALL + Constants.colorSuffixesTwo[i], Mineralogy.MODID);
        }

        initDone = true;
    }

    private static void generateReliefs(String materialName, double hardness, double blastResistance,
            int toolHardnessLevel) {
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_blank",
                Constants.RELIEF + "Blank" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_axe",
                Constants.RELIEF + "Axe" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_cross",
                Constants.RELIEF + "Cross" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_hammer",
                Constants.RELIEF + "Hammer" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_hoe",
                Constants.RELIEF + "Hoe" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_horizontal",
                Constants.RELIEF + "Horizontal" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_left",
                Constants.RELIEF + "Left" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_pickaxe",
                Constants.RELIEF + "Pickaxe" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_plus",
                Constants.RELIEF + "Plus" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_right",
                Constants.RELIEF + "Right" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_sword",
                Constants.RELIEF + "Sword" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_i",
                Constants.RELIEF + "I" + materialName);
        RegistrationHelper.registerBlock(new RockRelief((float) hardness, (float) blastResistance / 2,
                toolHardnessLevel, SoundType.STONE), materialName + "_relief_vertical",
                Constants.RELIEF + "Vertical" + materialName);
    }

    protected static void addStoneType(Material materialType, BlockItemPair rockPair) {

        String name = materialType.materialName.toLowerCase();
        float burnModifier = (float) (1 + ((materialType.hardness - 3) / 10));

        BlockItemPair rockSlabPair = null;
        BlockItemPair brickPair = null;
        BlockItemPair brickSlabPair = null;
        BlockItemPair smoothPair = null;
        BlockItemPair smoothSlabPair = null;
        BlockItemPair smoothBrickPair = null;
        BlockItemPair smoothBrickSlabPair = null;


        GameRegistry.addSmelting(rockPair.PairedItem, new ItemStack(net.minecraft.init.Blocks.STONE), 0.1F);

        // no point in ore dicting these recipes I think
        if (MineralogyConfig.generateRockStairs()) {
            RegistrationHelper.registerBlock(new RockStairs(rockPair.PairedBlock, (float) materialType.hardness,
                    (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name + "_" + Constants.STAIRS,
                    Constants.STAIRS + materialType.materialName);
        }

        if (MineralogyConfig.generateRockSlab()) {
            rockSlabPair = RegistrationHelper.registerBlock(
                    new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_double_" + Constants.SLAB),
                    name + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName, true, 64, true);

            RegistrationHelper.registerBlock(
                    new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, rockSlabPair.PairedBlock, rockPair.PairedBlock),
                    name + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName, false, 64, false);

            if (MineralogyConfig.generateRockFurnace()) {
                RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                        (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.FURNACE,
                        Constants.FURNACE + materialType.materialName, true, 1, false);
                RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                        (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.FURNACE,
                        Constants.FURNACE + "Lit" + materialType.materialName, false, 1, true);

            }
        }

        if (MineralogyConfig.generateRockWall()) {
            RegistrationHelper.registerBlock(
                    new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                    name + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
        }

        if (MineralogyConfig.generateBrick()) {
            brickPair = RegistrationHelper.registerBlock(
                    new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                    name + "_" + Constants.BRICK, "stone" + materialType.materialName + "Brick");

            if (MineralogyConfig.generateBrickStairs()) {
                RegistrationHelper.registerBlock(
                        new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
                                materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Brick");

            }

            if (MineralogyConfig.generateBrickSlab()) {
                brickSlabPair = RegistrationHelper.registerBlock(
                        new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.BRICK + "_double_" + Constants.SLAB),
                        name + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Brick", true, 64, true);

                RegistrationHelper.registerBlock(
                        new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, brickSlabPair.PairedBlock, brickPair.PairedBlock),
                        name + "_" + Constants.BRICK + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "Brick", false, 64, false);

                if (MineralogyConfig.generateBrickFurnace()) {
                    RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                            Constants.FURNACE + materialType.materialName, true, 1, false);
                    RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                            Constants.FURNACE + "Lit" +  materialType.materialName, false, 1, false);

                }
            }

            if (MineralogyConfig.generateBrickWall()) {
                RegistrationHelper.registerBlock(
                        new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
            }
        }

        if (MineralogyConfig.generateSmooth()) {
            smoothPair = RegistrationHelper.registerBlock(
                    new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                    name + "_" + Constants.SMOOTH, "stone" + materialType.materialName + "Smooth");

            if(MineralogyConfig.generateReliefs()) {
                generateReliefs(name, materialType.hardness, materialType.blastResistance, materialType.toolHardnessLevel);
            }

            if (MineralogyConfig.generateSmoothStairs()) {
                RegistrationHelper.registerBlock(
                        new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
                                materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Smooth");
            }

            if (MineralogyConfig.generateSmoothSlab()) {
                smoothSlabPair = RegistrationHelper.registerBlock(
                        new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.SMOOTH + "_double_" + Constants.SLAB),
                        name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Smooth", true, 64, true);
                RegistrationHelper.registerBlock(
                        new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, smoothSlabPair.PairedBlock, smoothPair.PairedBlock),
                        name + "_" + Constants.SMOOTH + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "Smooth", false, 64, false);

                if (MineralogyConfig.generateSmoothFurnace()) {
                    RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE,
                            Constants.FURNACE + materialType.materialName, true, 1, false);
                    RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE,
                            Constants.FURNACE + "Lit" +  materialType.materialName, false, 1, false);

                }
            }

            if (MineralogyConfig.generateSmoothWall()) {
                RegistrationHelper.registerBlock(
                        new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.SMOOTH + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
            }

            if (MineralogyConfig.generateSmoothBrick()) {
                smoothBrickPair = RegistrationHelper.registerBlock(
                        new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, "stone" + materialType.materialName + "SmoothBrick");

                if (MineralogyConfig.generateSmoothBrickStairs()) {
                    RegistrationHelper.registerBlock(
                            new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
                                    materialType.toolHardnessLevel, SoundType.STONE),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "SmoothBrick");
                }

                if (MineralogyConfig.generateSmoothBrickSlab()) {
                    smoothBrickSlabPair = RegistrationHelper.registerBlock(
                            new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_double_" + Constants.SLAB),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "SmoothBrick", true, 64, true);
                    RegistrationHelper.registerBlock(
                            new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, smoothBrickSlabPair.PairedBlock, smoothBrickPair.PairedBlock),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "SmoothBrick", false, 64, false);

                    if (MineralogyConfig.generateSmoothBrickFurnace()) {
                        RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                        (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                                Constants.FURNACE + materialType.materialName, true, 1, false);
                        RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                        (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                                Constants.FURNACE + "Lit" +  materialType.materialName, false, 1, false);

                    }
                }

                if (MineralogyConfig.generateSmoothBrickWall()) {
                    RegistrationHelper.registerBlock(
                            new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
                }
            }
        }

    }

    protected static void addStoneType(Material materialType) {
        String name = materialType.materialName.toLowerCase();
        final BlockItemPair rockPair = RegistrationHelper.registerBlock(new Rock(true, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name, "stone" + materialType.materialName);

        addStoneType(materialType, rockPair);
    }
}
